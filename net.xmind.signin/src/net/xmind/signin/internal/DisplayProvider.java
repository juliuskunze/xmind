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

package net.xmind.signin.internal;

import org.eclipse.swt.widgets.Display;

public class DisplayProvider implements IDisplayProvider {

    private Display display;

    private boolean shouldDispose;

    public DisplayProvider(Display existingDisplay) {
        this(existingDisplay, false);
    }

    /**
     * 
     */
    public DisplayProvider(Display display, boolean shouldDispose) {
        this.display = display;
        this.shouldDispose = shouldDispose;
    }

    /* (non-Javadoc)
     * @see net.xmind.signin.internal.IDisplayProvider#getDisplay()
     */
    public Display getDisplay() {
        return display;
    }

    /* (non-Javadoc)
     * @see net.xmind.signin.internal.IDisplayProvider#dispose()
     */
    public void dispose() {
        if (shouldDispose) {
            display.dispose();
        }
    }
}