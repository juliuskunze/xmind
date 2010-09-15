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
package org.xmind.gef.draw2d;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

public abstract class AbstractAnchor implements IAnchor, FigureListener {

    private List<IAnchorListener> listeners = null;

    private IFigure owner;

    protected AbstractAnchor() {
    }

    public AbstractAnchor(IFigure owner) {
        setOwner(owner);
    }

    protected void setOwner(IFigure owner) {
        if (owner == this.owner)
            return;

        if (getOwner() != null && hasAnchorListener()) {
            getOwner().removeFigureListener(this);
        }
        this.owner = owner;
        if (getOwner() != null && hasAnchorListener()) {
            getOwner().addFigureListener(this);
        }
    }

    public PrecisionPoint getLocation(int orientation, double expansion) {
        switch (orientation) {
        case PositionConstants.WEST:
            return getWest(expansion);
        case PositionConstants.SOUTH:
            return getSouth(expansion);
        case PositionConstants.NORTH:
            return getNorth(expansion);
        }
        return getEast(expansion);
    }

    protected PrecisionPoint getEast(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x + 100, ref.y, expansion);
    }

    protected PrecisionPoint getNorth(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x, ref.y - 100, expansion);
    }

    protected PrecisionPoint getSouth(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x, ref.y + 100, expansion);
    }

    protected PrecisionPoint getWest(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x - 100, ref.y, expansion);
    }

    public PrecisionPoint getLocation(PrecisionPoint reference, double expansion) {
        return getLocation(reference.x, reference.y, expansion);
    }

    public void addAnchorListener(IAnchorListener listener) {
        boolean hadListener = hasAnchorListener();
        if (listeners == null)
            listeners = new ArrayList<IAnchorListener>();
        listeners.add(listener);
        if (!hadListener && hasAnchorListener() && getOwner() != null) {
            getOwner().addFigureListener(this);
        }
    }

    public IFigure getOwner() {
        return owner;
    }

    public PrecisionPoint getReferencePoint() {
        if (getOwner() == null)
            return new PrecisionPoint();
        if (getOwner() instanceof IReferencedFigure) {
            return new PrecisionPoint(((IReferencedFigure) getOwner())
                    .getReference());
        }
        Point ref = getOwner().getBounds().getCenter();
        getOwner().translateToAbsolute(ref);
        return new PrecisionPoint(ref);
    }

    public void removeAnchorListener(IAnchorListener listener) {
        if (listeners == null)
            return;
        boolean hadListener = hasAnchorListener();
        listeners.remove(listener);
        if (hadListener && !hasAnchorListener() && getOwner() != null) {
            getOwner().removeFigureListener(this);
        }
    }

    public void figureMoved(IFigure source) {
        fireAnchorMoved();
    }

    protected void fireAnchorMoved() {
        if (listeners == null)
            return;
        for (Object listener : listeners.toArray()) {
            ((IAnchorListener) listener).anchorMoved(this);
        }
    }

    protected boolean hasAnchorListener() {
        return listeners != null && !listeners.isEmpty();
    }

}