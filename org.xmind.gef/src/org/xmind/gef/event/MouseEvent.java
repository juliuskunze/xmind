/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package org.xmind.gef.event;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 * @version 2005
 */
public class MouseEvent {

    public IPart target;

    public boolean leftOrRight;

    public Point cursorLocation;

    protected org.eclipse.swt.events.MouseEvent currentSWTEvent;

    private boolean consumed = false;

    public MouseEvent(org.eclipse.swt.events.MouseEvent swtEvent, IPart target,
            boolean leftOrRight, Point location) {
        this.target = target;
        this.leftOrRight = leftOrRight;
        this.cursorLocation = location;
        this.currentSWTEvent = swtEvent;
    }

    public static MouseEvent createEvent(
            org.eclipse.swt.events.MouseEvent swtEvent, IPart host,
            Point location) {
        return new MouseEvent(swtEvent, host, getButtonState(swtEvent.button),
                location);
    }

//    public static MouseEvent createEvent( org.eclipse.swt.events.MouseEvent me, IPart host ) {
//        return new MouseEvent( me, host, getButton( me.button ), new Point( me.x, me.y ) );
//    }

    public static boolean getButtonState(int button) {
        return (button == 1 || button == 0);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return cursorLocation
                + "," + (leftOrRight ? "left" : "right") + "," + target; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

    public org.eclipse.swt.events.MouseEvent getCurrentSWTEvent() {
        return currentSWTEvent;
    }

    //    public void consume() {
    //        if (source!=null) source.consume();
    //    }

}