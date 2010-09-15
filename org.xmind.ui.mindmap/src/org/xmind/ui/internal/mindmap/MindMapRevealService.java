/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
package org.xmind.ui.internal.mindmap;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IGraphicalPart;

public class MindMapRevealService extends CenteredRevealService {

    private boolean centered = false;

    public MindMapRevealService(IGraphicalViewer viewer) {
        super(viewer, false);
    }

    public void startCenteredReveal() {
        this.centered = true;
    }

    /**
     * @return the centered
     */
    public boolean isCentered() {
        return centered;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.ZoomingAndPanningRevealService#revealJobCanceled
     * (java.util.List)
     */
    @Override
    protected void revealJobCanceled(List<IGraphicalPart> toReveal) {
        super.revealJobCanceled(toReveal);
        this.centered = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.service.ZoomingAndPanningRevealService#revealJobFinished
     * (java.util.List)
     */
    @Override
    protected void revealJobFinished(List<IGraphicalPart> toReveal) {
        super.revealJobFinished(toReveal);
        this.centered = false;
    }

    protected double calcTargetScale(List<IGraphicalPart> toReveal,
            Rectangle revealBounds) {
        return -1;
    }

    protected PrecisionPoint calcTargetCenter(List<IGraphicalPart> toReveal,
            Rectangle revealBounds, double targetScale) {
        if (isCentered())
            return super.calcTargetCenter(toReveal, revealBounds, targetScale);

        revealBounds.expand(20, 20);
        Rectangle clientArea = getViewerClientArea();
        if (!clientArea.contains(revealBounds)
                && !revealBounds.contains(clientArea)) {
            int dx = 0;
            int dy = 0;
            int margin = 20;
            if (revealBounds.width > clientArea.width)
                dx = revealBounds.getCenter().x - clientArea.getCenter().x;
            else if (revealBounds.x < clientArea.x)
                dx = revealBounds.x - clientArea.x - margin;
            else if (revealBounds.right() > clientArea.right())
                dx = revealBounds.right() - clientArea.right() + margin;
            if (revealBounds.height > clientArea.height)
                dy = revealBounds.getCenter().y - clientArea.getCenter().y;
            else if (revealBounds.y < clientArea.y)
                dy = revealBounds.y - clientArea.y - margin;
            else if (revealBounds.bottom() > clientArea.bottom())
                dy = revealBounds.bottom() - clientArea.bottom() + margin;
            return getViewerCenterPoint(getViewerScale()).translate(dx, dy);
        }
        return null;
    }

    protected Rectangle getViewerClientArea() {
        Rectangle clientArea = getViewer().getClientArea();
        return getViewer().getZoomManager().getAntiScaled(clientArea);
    }

}