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
package org.xmind.gef.part;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.IViewer;

/**
 * @author Administrator
 * 
 */
public class GraphicalRootEditPart extends GraphicalEditPart implements
        IGraphicalRootPart {

    private IViewer viewer = null;

    private IGraphicalEditPart contents = null;

    public IPart getContents() {
        return contents;
    }

    public void setContents(IPart part) {
        if (contents != null)
            removeChild(contents);
        contents = (IGraphicalEditPart) part;
        if (contents != null)
            addChild(contents, 0);
    }

    public IViewer getViewer() {
        return viewer;
    }

    public void setViewer(IViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * @see org.xmind.gef.part.GraphicalEditPart#findAt(org.eclipse.draw2d.geometry.Point)
     */
    @Override
    public IPart findAt(Point position) {
        IPart ret = super.findAt(position);
        if (ret != null)
            return ret;
        return this;
    }

    protected IFigure createFigure() {
        return new Viewport(true);
    }

//    protected IFigure createFigure(IGenre genre) {
//        return genre.createRootFigure(this, (IGraphicalViewer) getViewer());
//    }

    protected void addChildView(IPart child, int index) {
        if (getContentPane() instanceof Viewport) {
            ((Viewport) getContentPane()).setContents(((IGraphicalPart) child)
                    .getFigure());
        } else {
            super.addChildView(child, index);
        }
    }

    protected void removeChildView(IPart child) {
        if (getContentPane() instanceof Viewport) {
            Viewport viewport = (Viewport) getContentPane();
            IFigure childFigure = ((IGraphicalPart) child).getFigure();
            if (childFigure == viewport.getContents()) {
                viewport.setContents(null);
                return;
            }
        }
        super.removeChildView(child);
    }

}