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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicRange;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.status.IStatusListener;
import org.xmind.gef.status.StatusEvent;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.branch.ILockableBranchStructureExtension;
import org.xmind.ui.branch.IMovableBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.DummyMoveTool;
import org.xmind.ui.tools.ITopicMoveToolHelper;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.tools.ParentSearcher;
import org.xmind.ui.util.MindMapUtils;

public class TopicMoveTool extends DummyMoveTool implements IStatusListener {

    private static ITopicMoveToolHelper defaultHelper = null;

    private ParentSearcher parentSearcher = null;

    private boolean slightMove = false;

    private ITopicMoveToolHelper helper = null;

    private ParentSearchKey key = null;

    private BranchDummy branchDummy = null;

    private IBranchPart targetParent = null;

    private List<IFigure> disabledFigures = null;

    public TopicMoveTool() {
        getStatus().addStatusListener(this);
    }

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof ITopicPart);
        super.setSource(source);
    }

    protected ITopicPart getSourceTopic() {
        return (ITopicPart) super.getSource();
    }

    protected IBranchPart getSourceBranch() {
        return (IBranchPart) getSourceTopic().getParent();
    }

    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        lockBranchStructures(getTargetViewer().getRootPart());
        collectDisabledBranches();
        if (!isCopyMove()) {
            disableFigures();
        }
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

    protected IFigure createDummy() {
        slightMove = true;
        if (branchDummy == null) {
            branchDummy = new BranchDummy(getTargetViewer(), getSourceBranch());
        }
        return branchDummy.getBranch().getFigure();
    }

    protected void destroyDummy(IFigure dummy) {
        if (branchDummy != null) {
            branchDummy.dispose();
            branchDummy = null;
        }
        super.destroyDummy(dummy);
    }

    private void collectDisabledBranches() {
        List<IPart> selectedParts = getSelectedParts(getTargetViewer());
        for (IPart part : selectedParts) {
            addDisabledPart(part);
        }
        List<ITopic> topics = MindMapUtils.getTopics(selectedParts);
        Set<ITopicRange> ranges = MindMapUtils.findContainedRanges(topics,
                true, false);
        if (!ranges.isEmpty()) {
            for (ITopicRange r : ranges) {
                ITopic st = ((ISummary) r).getTopic();
                if (st != null) {
                    addDisabledPart(getTargetViewer().findPart(st));
                }
            }
        }
    }

    private void addDisabledPart(IPart part) {
        if (part instanceof ITopicPart) {
            ITopicPart topic = (ITopicPart) part;
            addDisabledFigure(topic.getFigure());
            IBranchPart branch = topic.getOwnerBranch();
            if (branch != null) {
                addDisabledFigure(branch.getFigure());
            }
        }
    }

    private void addDisabledFigure(IFigure figure) {
        if (disabledFigures == null)
            disabledFigures = new ArrayList<IFigure>();
        disabledFigures.add(figure);
    }

    private void clearDisabledFigures() {
        disabledFigures = null;
    }

    private void disableFigures() {
        if (disabledFigures != null) {
            for (IFigure figure : disabledFigures) {
                figure.setEnabled(false);
            }
        }
    }

    private void enableFigures() {
        if (disabledFigures != null) {
            for (IFigure figure : disabledFigures) {
                figure.setEnabled(true);
            }
        }
    }

    protected void onMoving(Point currentPos, MouseDragEvent me) {
        if (slightMove) {
            if (!((IGraphicalEditPart) getSourceTopic())
                    .containsPoint(currentPos)) {
                slightMove = false;
            }
        }
        super.onMoving(currentPos, me);
        if (branchDummy != null) {
            key = new ParentSearchKey(getSourceTopic(),
                    (IReferencedFigure) branchDummy.getBranch().getTopicPart()
                            .getFigure(), currentPos);
            key.setFeedback(branchDummy.getBranch());
            targetParent = updateTargetParent();
            updateWithParent(targetParent);
        }
    }

    private IBranchPart updateTargetParent() {
        if (isFloatMove())
            return null;
        if (isFreeMove() || isSlightMove()) {
            IPart parent = getSourceBranch().getParent();
            return parent instanceof IBranchPart ? (IBranchPart) parent : null;
        }
        return getParentSearcher().searchTargetParent(
                getTargetViewer().getRootPart(), key);
    }

    private void updateWithParent(IBranchPart parent) {
        updateDummyWithParent(parent);
        updateHelperWithParent(parent);
    }

    private void updateDummyWithParent(IBranchPart parent) {
    }

    protected void updateDummyPosition(Point pos) {
        super.updateDummyPosition(pos);
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
//        if (parent != null) {
//            ITopicMoveToolHelper helper = (ITopicMoveToolHelper) parent
//                    .getBranchPolicy().getToolHelper(parent,
//                            ITopicMoveToolHelper.class);
//            if (helper != null)
//                return helper;
//        }
        return getDefaultHelper();
    }

    protected static ITopicMoveToolHelper getDefaultHelper() {
        if (defaultHelper == null) {
            defaultHelper = new TopicMoveToolHelper();
        }
        return defaultHelper;
    }

    private boolean isSlightMove() {
        if (isFloatMove() || isAlreadyFloat())
            return false;
        if (isFreeable()) {
            if (isFreeMove() || isAlreadyFree())
                return false;
        }
        return slightMove;
    }

    private boolean isFloatMove() {
        return getStatus().isStatus(GEF.ST_SHIFT_PRESSED);
    }

    private boolean isFreeMove() {
        if (Util.isMac())
            return getStatus().isStatus(GEF.ST_CONTROL_PRESSED);
        return getStatus().isStatus(GEF.ST_ALT_PRESSED);
    }

    private boolean isCopyMove() {
        if (Util.isMac())
            return getStatus().isStatus(GEF.ST_ALT_PRESSED);
        return getStatus().isStatus(GEF.ST_CONTROL_PRESSED);
    }

    protected ParentSearcher getParentSearcher() {
        if (parentSearcher == null) {
            parentSearcher = new ParentSearcher();
        }
        return parentSearcher;
    }

    protected void end() {
        if (helper != null) {
            helper.deactivate(getDomain(), getTargetViewer());
            helper = null;
        }
        super.end();
        targetParent = null;
        parentSearcher = null;
        key = null;
        enableFigures();
        clearDisabledFigures();
        unlockBranchStructures(getTargetViewer().getRootPart());
    }

    protected Request createRequest() {
        if (isSlightMove())
            return null;

        IBranchPart targetParentBranch = this.targetParent;
        ITopicPart targetParent = targetParentBranch == null ? null
                : targetParentBranch.getTopicPart();
        boolean relative = true;//isRelative();
        Point position = relative ? getRelativePosition()
                : getAbsolutePosition();
        boolean copy = isCopyMove();
        int index = -1;
        boolean free = isFreeMove();

        if (!isFloatMove()) {
            index = getParentSearcher().getIndex(targetParentBranch, key);
            if (free) {
                if (!isFreeable() && !isAlreadyFloat()) {
                    free = false;
                }
            } else {
                if (targetParent != null) {
                    if (targetParent == getSourceParentTopic()) {
                        if (isFreeable()) {
                            free = isAlreadyFree();
                        }
                    } else {//if ((!isAlreadyFloat() && !isAlreadyFree())) {
                        position = null;
                    }
                }
            }
            if (!free && targetParent != null) {
                position = null;
            }
        }

        String reqType = copy ? GEF.REQ_COPYTO : GEF.REQ_MOVETO;
        Request request = new Request(reqType);
        request.setDomain(getDomain());
        request.setViewer(getTargetViewer());
        List<IPart> parts = new ArrayList<IPart>();
        for (IPart p : getSelectedParts(getTargetViewer())) {
            if (p.hasRole(GEF.ROLE_MOVABLE)) {
                parts.add(p);
            }
        }
        request.setTargets(parts);
//        fillTargets(request, getTargetViewer(), false);
        request.setPrimaryTarget(getSourceTopic());
        request.setParameter(GEF.PARAM_POSITION, position);
        request.setParameter(GEF.PARAM_POSITION_RELATIVE, Boolean
                .valueOf(relative));
        request.setParameter(GEF.PARAM_PARENT, targetParent);
        request.setParameter(GEF.PARAM_INDEX, Integer.valueOf(index));
        request.setParameter(MindMapUI.PARAM_COPY, Boolean.valueOf(copy));
        request.setParameter(MindMapUI.PARAM_FREE, Boolean.valueOf(free));

        IBranchPart sourceParent;
        IBranchPart sourceBranch = getSourceBranch();
        if (sourceBranch != null) {
            sourceParent = sourceBranch.getParentBranch();
            if (sourceParent != null) {
                IStructure structure = sourceParent.getBranchPolicy()
                        .getStructure(sourceParent);
                if (structure instanceof IMovableBranchStructureExtension) {
                    ((IMovableBranchStructureExtension) structure)
                            .decorateMoveOutRequest(sourceParent, key,
                                    targetParentBranch, request);
                }
            }
        } else {
            sourceParent = null;
        }

        if (targetParentBranch != null) {
            IStructure structure = targetParentBranch.getBranchPolicy()
                    .getStructure(targetParentBranch);
            if (structure instanceof IMovableBranchStructureExtension) {
                ((IMovableBranchStructureExtension) structure)
                        .decorateMoveInRequest(targetParentBranch, key,
                                sourceParent, request);
            }
        }
        return request;
    }

    private boolean isAlreadyFree() {
        return getSourceTopic().getTopic().getPosition() != null;
    }

    private boolean isAlreadyFloat() {
        return !getSourceTopic().getTopic().isAttached();
    }

    private boolean isFreeable() {
        if (!MindMapUI.isFreePositionMoveAllowed())
            return false;

        IBranchPart branch = getSourceBranch();
        return branch != null
                && MindMapUtils.isSubBranchesFreeable(branch.getParentBranch());
    }

    private ITopicPart getSourceParentTopic() {
        IBranchPart sourceBranch = getSourceBranch();
        if (sourceBranch != null) {
            IPart p = sourceBranch.getParent();
            if (p instanceof IBranchPart) {
                return ((IBranchPart) p).getTopicPart();
            }
        }
        return null;
    }

//    private boolean isRelative() {
//        return targetParent != null;
//    }

    private Point getRelativePosition() {
        Dimension off = getCursorPosition()
                .getDifference(getStartingPosition());
        return new Point(off.width, off.height);
    }

    private Point getAbsolutePosition() {
        return getCursorPosition();
    }

    public void statusChanged(StatusEvent event) {
        int k = event.key;
        if (getStatus().isStatus(GEF.ST_ACTIVE)) {
            if (k == GEF.ST_SHIFT_PRESSED || k == GEF.ST_CONTROL_PRESSED
                    || k == GEF.ST_ALT_PRESSED) {
                updateDisabilities();
                updateDummyPosition(getCursorPosition());
                targetParent = updateTargetParent();
                updateWithParent(targetParent);
            }
        }
    }

    private void updateDisabilities() {
        if (isCopyMove()) {
            enableFigures();
        } else {
            disableFigures();
        }
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        if (isCopyMove())
            return MindMapUI.getImages().getCursor(IMindMapImages.CURSOR_ADD);
        return super.getCurrentCursor(pos, host);
    }

}