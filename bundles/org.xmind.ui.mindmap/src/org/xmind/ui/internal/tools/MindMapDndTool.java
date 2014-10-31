/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.tools;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.event.DragDropEvent;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.GraphicalTool;
import org.xmind.ui.branch.ILockableBranchStructureExtension;
import org.xmind.ui.branch.IMovableBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;
import org.xmind.ui.tools.ITopicMoveToolHelper;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.tools.ParentSearcher;

public class MindMapDndTool extends GraphicalTool {

    private static ITopicMoveToolHelper defaultHelper = null;

    private BranchDummy dummy = null;

    private IBranchPart targetParent = null;

    private ParentSearcher parentSearcher = null;

    private ParentSearchKey key = null;

    private boolean insideTopicAllowed = false;

    private ITopicMoveToolHelper helper = null;

    private Request request = null;

    private Image image = null;

    protected boolean acceptEvent(DragDropEvent de) {
        return true;
    }

    protected boolean handleDragStarted(DragDropEvent de) {
        if (image != null) {
            image.dispose();
            image = null;
        }
        targetParent = null;
        request = null;
        insideTopicAllowed = isInsideTopicAllowed(de);
        if (acceptEvent(de)) {
            createDummy(de);
            lockBranchStructures(getTargetViewer().getRootPart());
            return true;
        }
        return false;
    }

    private void lockBranchStructures(IPart part) {
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            IStructure sa = branch.getBranchPolicy().getStructure(branch);
            if (sa instanceof ILockableBranchStructureExtension) {
                ((ILockableBranchStructureExtension) sa).lock(branch);
            }
        }
        for (IPart child : part.getChildren()) {
            lockBranchStructures(child);
        }
    }

    private void unlockBranchStructures(IPart part) {
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            IStructure sa = branch.getBranchPolicy().getStructure(branch);
            if (sa instanceof ILockableBranchStructureExtension) {
                ((ILockableBranchStructureExtension) sa).unlock(branch);
            }
        }
        for (IPart child : part.getChildren()) {
            unlockBranchStructures(child);
        }
    }

    private void createDummy(DragDropEvent de) {
        if (dummy != null) {
            dummy.dispose();
            dummy = null;
        }
        dummy = new BranchDummy(getTargetViewer(), false);
        decorateDummy(dummy, de);
    }

    protected void decorateDummy(BranchDummy dummy, DragDropEvent de) {
        dummy.setStyle(Styles.ShapeClass, Styles.TOPIC_SHAPE_NO_BORDER);
        dummy.getTopic().setTitleText(""); //$NON-NLS-1$
        dummy.getBranch().refresh();
    }

    protected boolean isInsideTopicAllowed(DragDropEvent de) {
        return true;
    }

    protected boolean handleDragOver(DragDropEvent de) {
        if (acceptEvent(de)) {
            if (dummy != null) {
                key = new ParentSearchKey(null, (IReferencedFigure) dummy
                        .getBranch().getTopicPart().getFigure(),
                        getCursorPosition());
                key.setFeedback(dummy.getBranch());
                targetParent = updateTargetParent();
                updateWithParent(targetParent);
                return true;
            }
        }
        return false;
    }

    private IBranchPart updateTargetParent() {
        return getParentSearcher().searchTargetParent(
                getTargetViewer().getRootPart(), key);
    }

    private void updateWithParent(IBranchPart parent) {
        updateDummyWithParent(parent);
        updateHelperWithParent(parent);
    }

    private void updateDummyWithParent(IBranchPart parent) {
        updateDummyPosition(getCursorPosition());
    }

    protected void updateDummyPosition(Point pos) {
        IFigure fig = dummy.getBranch().getFigure();
        if (fig != null) {
            if (fig instanceof IReferencedFigure) {
                ((IReferencedFigure) fig).setReference(pos);
            } else {
                fig.setLocation(pos);
            }
        }
    }

    private void updateHelperWithParent(IBranchPart parent) {
        ITopicMoveToolHelper oldHelper = this.helper;
        ITopicMoveToolHelper newHelper = getHelper(parent);
        if (newHelper != oldHelper) {
            if (oldHelper != null)
                oldHelper.deactivate(getDomain(), getTargetViewer());
            if (newHelper != null)
                newHelper.activate(getDomain(), getTargetViewer());
            this.helper = newHelper;
        }
        if (helper != null) {
            helper.update(parent, key);
        }
    }

    private ITopicMoveToolHelper getHelper(IBranchPart parent) {
        return getDefaultHelper();
    }

    protected static ITopicMoveToolHelper getDefaultHelper() {
        if (defaultHelper == null) {
            defaultHelper = new TopicMoveToolHelper();
        }
        return defaultHelper;
    }

    protected ParentSearcher getParentSearcher() {
        if (parentSearcher == null) {
            parentSearcher = new ParentSearcher(insideTopicAllowed);
        }
        return parentSearcher;
    }

    protected boolean handleDragDismissed(DragDropEvent de) {
        if (acceptEvent(de)) {
            request = createRequest(de);
            destroyDummy();
            changeActiveTool(GEF.TOOL_DEFAULT);
            return true;
        }
        return false;
    }

    private IPart findDropTarget(IPart target) {
        if (target == null)
            return null;
        if (target.hasRole(GEF.ROLE_DROP_TARGET))
            return target;
        return findDropTarget(target.getParent());
    }

    private Request createRequest(DragDropEvent de) {
        Request req = new Request(GEF.REQ_DROP);
        IPart target = findDropTarget(de.target);
        if (target == null) {
            target = (ISheetPart) getTargetViewer()
                    .getAdapter(ISheetPart.class);
        }
        req.setPrimaryTarget(target);
        ITopicPart targetTopic = targetParent == null ? null : targetParent
                .getTopicPart();
        if (targetTopic != null) {
            req.setParameter(GEF.PARAM_PARENT, targetTopic);
            int targetIndex = -1;
            if (targetParent != null) {
                targetIndex = getParentSearcher().getIndex(targetParent, key);
            }
            req.setParameter(GEF.PARAM_INDEX, targetIndex);
            req.setParameter(GEF.PARAM_POSITION_ABSOLUTE, getCursorPosition());
        }
        if (targetParent == null) {
            Point position = getCursorPosition();
            req.setParameter(GEF.PARAM_POSITION, position);
        }
        req.setParameter(GEF.PARAM_DROP_OPERATION, de.detail);
        if (targetParent != null) {
            IStructure structure = targetParent.getBranchPolicy().getStructure(
                    targetParent);
            if (structure instanceof IMovableBranchStructureExtension) {
                ((IMovableBranchStructureExtension) structure)
                        .decorateMoveInRequest(targetParent, key, null, req);
            }
        }
        return req;
    }

    private void destroyDummy() {
        unlockBranchStructures(getTargetViewer().getRootPart());
        ITopicMoveToolHelper oldHelper = this.helper;
        BranchDummy oldDummy = this.dummy;
        Image oldImage = this.image;
        if (oldHelper != null) {
            oldHelper.deactivate(getDomain(), getTargetViewer());
        }
        if (oldDummy != null) {
            oldDummy.dispose();
        }
        if (oldImage != null) {
            oldImage.dispose();
        }
        this.helper = null;
        this.dummy = null;
        this.image = null;
    }

    protected boolean handleDrop(DragDropEvent de) {
        try {
            if (acceptEvent(de)) {
                final Request req = this.request;
                if (req != null) {
                    req.setParameter(MindMapUI.PARAM_DND_DATA, de.dndData);
                    Display.getCurrent().asyncExec(new Runnable() {
                        public void run() {
                            SafeRunner.run(new SafeRunnable() {
                                public void run() throws Exception {
                                    BusyIndicator.showWhile(
                                            Display.getCurrent(),
                                            new Runnable() {
                                                public void run() {
                                                    getDomain().handleRequest(
                                                            req);
                                                }
                                            });
                                }
                            });
                        }
                    });
                    return true;
                }
            }
            return false;
        } finally {
            this.request = null;
        }
    }

    @Override
    protected boolean handleKeyTraversed(KeyEvent ke) {
        if (ke.traverse == SWT.TRAVERSE_ESCAPE) {
            destroyDummy();
            changeActiveTool(GEF.TOOL_DEFAULT);
            ke.consume();
            return true;
        }
        return super.handleKeyTraversed(ke);
    }

    private boolean mouseDown = false;

    @Override
    protected boolean handleMouseDown(MouseEvent me) {
        mouseDown = true;
        return super.handleMouseDown(me);
    }

    @Override
    protected boolean handleMouseUp(MouseEvent me) {
        if (mouseDown) {
            mouseDown = false;
            destroyDummy();
            changeActiveTool(GEF.TOOL_DEFAULT);
            return true;
        }
        return super.handleMouseUp(me);
    }
}