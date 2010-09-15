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
import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.ZoomingAndPanningRevealService;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;

public class CenteredRevealService extends ZoomingAndPanningRevealService {

    private double cachedScale = -1;

    public CenteredRevealService(IGraphicalViewer viewer,
            boolean autoRevealSelection) {
        super(viewer, autoRevealSelection);
    }

    protected boolean exclude(IPart part) {
        return part instanceof ISheetPart;
    }

    protected void startReveal(ISelection selection) {
        cachedScale = -1;
        super.startReveal(selection);
    }

    protected boolean isAnimationEnabled() {
        return MindMapUI.isAnimationEnabled();
    }

    protected PrecisionPoint calcTargetCenter(List<IGraphicalPart> toReveal,
            Rectangle revealBounds, double targetScale) {
        return new PrecisionPoint(revealBounds.getCenter());
    }

    protected double calcTargetScale(List<IGraphicalPart> toReveal,
            Rectangle revealBounds) {
        if (!isActive())
            return 1.0;

        if (cachedScale > 0)
            return cachedScale;

        Rectangle clientArea = getViewer().getClientArea();
        int width = revealBounds.width;
        int height = revealBounds.height;

        double scale = 2.3d;

        double w = width * scale;
        double h = height * scale;

        double minWidth = clientArea.width * 0.08d;
        double minHeight = clientArea.height * 0.08d;

        if (w < minWidth || h < minHeight) {
            double s1 = w < minWidth ? minWidth / width : scale;
            double s2 = h < minHeight ? minHeight / height : scale;
            scale = Math.max(s1, s2);
            w = width * scale;
            h = height * scale;
        }

        double maxWidth = clientArea.width * 0.6d;
        double maxHeight = clientArea.height * 0.6d;
        if (w > maxWidth || h > maxHeight) {
            double s1 = w > maxWidth ? maxWidth / width : scale;
            double s2 = h > maxHeight ? maxHeight / height : scale;
            scale = Math.min(s1, s2);
        }

        cachedScale = scale;
        return scale;
    }

}