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
package org.xmind.cathy.internal;

import java.util.Arrays;

import net.xmind.signin.internal.XMindUpdater;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * This class controls all aspects of the application's execution
 */
public class CathyApplication implements IApplication {

    public static final String SYS_VERSION = "org.xmind.product.version"; //$NON-NLS-1$

    public static final String SYS_BUILDID = "org.xmind.product.buildid"; //$NON-NLS-1$

    public static final String SYS_APP_STATUS = "org.xmind.cathy.app.status"; //$NON-NLS-1$

    public static final String APP_VERSION = "3.3.1"; //$NON-NLS-1$

    public static final String ARG_ACTIVATE = "--activate"; //$NON-NLS-1$

    /**
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
     */
    public Object start(IApplicationContext context) throws Exception {
        // Add product information to system properties:
        System.setProperty(SYS_VERSION, APP_VERSION);
        System.setProperty(SYS_BUILDID, context.getBrandingBundle()
                .getVersion().toString());

        // Check if there's already a running XMind instance:
        if (shouldExitEarly()) {
            // Log all application arguments to local disk to exchange 
            // between running XMind instances:
            String[] args = Platform.getApplicationArgs();
            if (args != null && args.length > 0) {
                logArgs(args);
            }
            return EXIT_OK;
        }

        Display display = PlatformUI.createDisplay();
        try {
            // Install global OpenDocument listener:
            OpenDocumentQueue.getInstance().hook(display);

            // Check if this is an expired beta version:
            if (checkBetaExpired())
                return EXIT_OK;

            // Check if there's previously downloaded software updates:
            if (checkSoftwareUpdateOnStart())
                return EXIT_OK;

            // Log all application arguments to local disk to exchange 
            // between running XMind instances:
            String[] args = Platform.getApplicationArgs();
            if (args != null && args.length > 0) {
                logArgs(args);
            }

            // Mark application status to 'starting':
            System.setProperty(SYS_APP_STATUS, "starting"); //$NON-NLS-1$

            // Set cookies to let web pages loaded within internal web browser
            // to recognize the environment:
            initializeInternalBrowserCookies();

            // Launch workbench and get return code:
            int returnCode = PlatformUI.createAndRunWorkbench(display,
                    new CathyWorkbenchAdvisor());

            if (returnCode == PlatformUI.RETURN_RESTART) {
                // Restart:
                return EXIT_RESTART;
            }

            // Quit:
            return EXIT_OK;
        } finally {
            display.dispose();
        }
    }

    private void initializeInternalBrowserCookies() {
        Browser.setCookie(
                "_env=xmind_" + APP_VERSION + "; path=/; domain=.xmind.net", //$NON-NLS-1$ //$NON-NLS-2$
                "http://www.xmind.net/"); //$NON-NLS-1$
    }

    private boolean checkBetaExpired() {
        long time = System.currentTimeMillis();
        if (time < 0) {
            showBetaExpired();
            return true;
        }
        return false;
    }

    private void showBetaExpired() {
        Shell shell = new Shell(Display.getCurrent());
        try {
            MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
            mb.setText("XMind Beta Expired"); //$NON-NLS-1$
            mb.setMessage("XMind v3.3.1(Beta) has expired. Thank you for helping us evaluate it. You can download the latest version at http://www.xmind.net/ ."); //$NON-NLS-1$
            mb.open();
            Program.launch("http://www.xmind.net/xmind/downloads/"); //$NON-NLS-1$
        } finally {
            shell.dispose();
        }
    }

    private void logArgs(String[] args) {
        CathyPlugin.log("Application arguments to be logged: " //$NON-NLS-1$
                + Arrays.toString(args));
        Log opening = Log.get(Log.OPENING);
        for (String arg : args) {
            if ("-p".equals(arg)) {//$NON-NLS-1$
                System.setProperty(
                        "org.xmind.cathy.startup.presentation", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (!arg.startsWith("-")) { //$NON-NLS-1$
                opening.append(arg);
            }
        }
    }

    private boolean shouldExitEarly() throws Exception {
        Bundle bundle = CathyPlugin.getDefault().getBundle();
        try {
            Class clazz = bundle
                    .loadClass("org.xmind.cathy.internal.ApplicationValidator"); //$NON-NLS-1$
            if (IApplicationValidator.class.isAssignableFrom(clazz)) {
                return ((IApplicationValidator) clazz.newInstance())
                        .shouldApplicationExitEarly();
            }
        } catch (ClassNotFoundException e) {
        }
        return false;
    }

    /**
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    public void stop() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return;
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                if (!display.isDisposed())
                    workbench.close();
            }
        });
    }

    private boolean checkSoftwareUpdateOnStart() {
        return XMindUpdater.checkSoftwareUpdateOnStart();
    }
}