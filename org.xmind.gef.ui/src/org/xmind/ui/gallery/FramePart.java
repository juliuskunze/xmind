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
package org.xmind.ui.gallery;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.status.StatusEvent;

public class FramePart extends GraphicalEditPart {

    public FramePart(Object model) {
        setModel(model);
        setDecorator(FrameDecorator.DEFAULT);
    }

    protected IFigure createFigure() {
        return new FrameFigure();
    }

    public FrameFigure getFigure() {
        return (FrameFigure) super.getFigure();
    }

    public IFigure getContentPane() {
        return ((FrameFigure) super.getFigure()).getContentPane();
    }

    protected Object[] getModelChildren(Object model) {
        return new Object[] { model };
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_NAVIGABLE,
                GalleryViewer.POLICY_NAVIGABLE);
    }

    protected void register() {
        registerModel(getModel());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getModel());
    }

    protected void handleStatusChanged(StatusEvent event) {
        if ((event.key & GEF.PART_SELECTED) != 0) {
            setSelected(event.newValue);
        } else if ((event.key & GEF.PART_PRESELECTED) != 0) {
            setPreselected(event.newValue);
        } else if ((event.key & GEF.PART_FOCUSED) != 0) {
            getFigure().repaint();
        } else {
            super.handleStatusChanged(event);
        }
    }

    protected void setSelected(boolean selected) {
        getFigure().setSelected(selected);
    }

    protected void setPreselected(boolean preselected) {
        getFigure().setPreselected(preselected);
    }

    protected IPart findChildAt(IPart child, Point position) {
        if (!child.hasRole(GEF.ROLE_SELECTABLE))
            return null;
        return super.findChildAt(child, position);
    }

    public Cursor getCursor(Point pos) {
        if (getContentPane().containsPoint(pos)
                && !getSite().getProperties().getBoolean(
                        GalleryViewer.SolidFrames, false))
            return Cursors.HAND;
        if (getFigure().getTitle().containsPoint(pos)) {
            EditDomain domain = getSite().getDomain();
            if (domain != null && domain.hasTool(GEF.TOOL_EDIT))
                return Cursors.HAND;
        }
        return null;
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        for (IPart child : getChildren()) {
            child.update();
        }
    }
}