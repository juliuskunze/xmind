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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.decorators.BoundaryTitleTextDecorator;
import org.xmind.ui.mindmap.IBoundaryPart;

public class BoundaryTitleTextPart extends TitleTextPart {

    public BoundaryTitleTextPart() {
        setDecorator(BoundaryTitleTextDecorator.getInstance());
    }

    protected IFigure createFigure() {
        IFigure figure = super.createFigure();
        RotatableWrapLabel label = (RotatableWrapLabel) figure;
        label.setAbbreviated(true);
        label.setTextAlignment(PositionConstants.LEFT);
        label.setSingleLine(true);
        return figure;
    }

    public void setParent(IPart parent) {
        IBoundaryPart boundaryPart = getBoundaryPart();
        if (boundaryPart instanceof BoundaryPart
                && ((BoundaryPart) boundaryPart).getTitle() == this) {
            ((BoundaryPart) boundaryPart).setTitle(null);
        }
        super.setParent(parent);
        boundaryPart = getBoundaryPart();
        if (boundaryPart instanceof BoundaryPart) {
            ((BoundaryPart) boundaryPart).setTitle(this);
        }
    }

    public IBoundaryPart getBoundaryPart() {
        if (getParent() instanceof IBoundaryPart)
            return (IBoundaryPart) getParent();
        return null;
    }

    public IPart findAt(Point position) {
        IPart ret = super.findAt(position);
        if (ret == this) {
            IBoundaryPart boundaryPart = getBoundaryPart();
            if (boundaryPart != null)
                return boundaryPart;
        }
        return ret;
    }

}