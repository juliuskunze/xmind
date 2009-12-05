/* ******************************************************************************
 * Copyright (c) 2006-2009 XMind Ltd. and others.
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
package org.xmind.cathy.internal;

import java.util.Properties;

import org.eclipse.swt.internal.win32.OS;

public class ApplicationValidator implements IApplicationValidator {

    public ApplicationValidator() {
    }

    public boolean shouldApplicationExitEarly() {
        Log log = Log.get(Log.SINGLETON);
        if (!log.exists())
            return false;

        Properties properties = log.getProperties();
        if (properties.isEmpty())
            return false;

        int hWnd = getOpenedWindowHandle(properties);
        if (hWnd == 0 || !isValidWindow(hWnd))
            return false;

        notifyOpenedWindow(hWnd);
        return true;
    }

    private boolean isValidWindow(int hWnd) {
        int length = OS.GetWindowTextLength(hWnd);
        return length > 0;
    }

    private int getOpenedWindowHandle(Properties properties) {
        String value = properties.getProperty(Log.K_PRIMARY_WINDOW);
        if (value != null && !"".equals(value)) { //$NON-NLS-1$
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void notifyOpenedWindow(int hWnd) {
        OS.SetForegroundWindow(hWnd);
        OS.SetFocus(hWnd);
    }

}