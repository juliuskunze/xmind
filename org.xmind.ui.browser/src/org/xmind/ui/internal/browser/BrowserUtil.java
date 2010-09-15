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
package org.xmind.ui.internal.browser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class BrowserUtil {

    private static final String BROWSER_PACKAGE_NAME = "org.eclipse.swt.browser.Browser"; //$NON-NLS-1$

    private static Boolean isInternalBrowserOperational = null;

    private BrowserUtil() {
    }

    public static Object getWindowKey(IWorkbenchWindow window) {
        return new Integer(window.hashCode());
    }

    public static boolean gotoUrl(String url) {
        if (url != null && !BrowserUtil.isWindows()) {
            int index = url.indexOf(" "); //$NON-NLS-1$
            while (index >= 0) {
                url = url.substring(0, index) + "%20" //$NON-NLS-1$
                        + url.substring(index + 1);
                index = url.indexOf(" "); //$NON-NLS-1$
            }
        }
        return Program.launch(url);
    }

    /**
     * Returns true if we're running on Windows.
     * 
     * @return boolean
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        if (os != null && os.toLowerCase().indexOf("win") >= 0) //$NON-NLS-1$
            return true;
        return false;
    }

    public static boolean canUseInternalWebBrowser() {
        // if we have already figured this out, don't do it again.
        if (isInternalBrowserOperational != null)
            return isInternalBrowserOperational.booleanValue();

        // check for the class
        try {
            Class.forName(BROWSER_PACKAGE_NAME);
        } catch (ClassNotFoundException e) {
            isInternalBrowserOperational = new Boolean(false);
            return false;
        }

        // try loading it
        Shell shell = null;
        try {
            shell = new Shell(PlatformUI.getWorkbench().getDisplay());
            new Browser(shell, SWT.NONE);
            isInternalBrowserOperational = new Boolean(true);
            return true;
        } catch (Throwable t) {
            BrowserPlugin
                    .getDefault()
                    .getLog()
                    .log(
                            new Status(
                                    IStatus.WARNING,
                                    BrowserPlugin.PLUGIN_ID,
                                    0,
                                    "Internal browser is not available: " + t.getMessage(), null)); //$NON-NLS-1$
            isInternalBrowserOperational = new Boolean(false);
            return false;
        } finally {
            if (shell != null)
                shell.dispose();
        }
    }

    public static boolean canUseSystemBrowser() {
        // Disabling system browser on Solaris due to bug 94497
        if (Platform.OS_SOLARIS.equals(Platform.getOS()))
            return false;
        return true; //Program.findProgram("html") != null;
    }

    public static String encodeStyle(String clientId, int style) {
        return clientId + "-" + style; //$NON-NLS-1$
    }

    public static int decodeStyle(String id) {
        return Integer.parseInt(id.substring(id.lastIndexOf('-') + 1));
    }

    public static String decodeClientId(String id) {
        return id.substring(0, id.lastIndexOf('-'));
    }
}