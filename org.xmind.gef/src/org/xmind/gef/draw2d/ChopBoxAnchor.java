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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

/**
 * @author Frank Shaka
 */
public class ChopBoxAnchor extends AbstractAnchor {

    private static final Rectangle BOX = new Rectangle();

    /**
     * @param owner
     */
    public ChopBoxAnchor(IFigure owner) {
        super(owner);
    }

    public PrecisionPoint getLocation(double x, double y, double expansion) {
        return Geometry.getChopBoxLocation(x, y, getBox(), expansion);
    }

    protected Rectangle getBox() {
        BOX.setBounds(getOwner().getBounds());
        BOX.width = Math.max(0, BOX.width - 1);
        BOX.height = Math.max(0, BOX.height - 1);
        return BOX;
    }

}