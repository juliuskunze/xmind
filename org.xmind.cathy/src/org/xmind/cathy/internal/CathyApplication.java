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

import java.io.File;

import net.xmind.signin.internal.XMindNetErrorReporter;
import net.xmind.signin.internal.XMindUpdater;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.xmind.ui.internal.statushandlers.IErrorReporter;

/**
 * This class controls all aspects of the application's execution
 */
public class CathyApplication implements IApplication {

    public static final String SYS_VERSION = "org.xmind.product.version"; //$NON-NLS-1$

    public static final String SYS_BUILDID = "org.xmind.product.buildid"; //$NON-NLS-1$

    public static final String SYS_APP_STATUS = "org.xmind.cathy.app.status"; //$NON-NLS-1$

    public static final String APP_VERSION = "3.4.0"; //$NON-NLS-1$

    public static final String ARG_ACTIVATE = "--activate"; //$NON-NLS-1$

    /**
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
     */
    public Object start(IApplicationContext context) throws Exception {
        // Add product information to system properties:
        System.setProperty(SYS_VERSION, APP_VERSION);
        System.setProperty(SYS_BUILDID, getBuildId(context));
        IErrorReporter.Default.setDelegate(new XMindNetErrorReporter());

        // Check if there's already a running XMind instance:
        if (shouldExitEarly()) {
            // Log all application arguments to local disk to exchange
            // between running XMind instances:
            logApplicationArgs();
            return EXIT_OK;
        }

        Display display = PlatformUI.createDisplay();
        try {
            // Install global OpenDocument listener:
            OpenDocumentQueue.getInstance().hook(display);

            // Check if there's previously downloaded software updates:
            if (checkSoftwareUpdateOnStart())
                return EXIT_OK;

            // Log all application arguments to local disk to exchange
            // between running XMind instances:
            logApplicationArgs();

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

    private String getBuildId(IApplicationContext context) {
        String buildId = System.getProperty("eclipse.buildId"); //$NON-NLS-1$
        if (buildId != null && !"".equals(buildId)) //$NON-NLS-1$
            return buildId;
        return context.getBrandingBundle().getVersion().toString();
    }

    private void initializeInternalBrowserCookies() {
        Browser.setCookie(
                "_env=xmind_" + APP_VERSION + "; path=/; domain=.xmind.net", //$NON-NLS-1$ //$NON-NLS-2$
                "http://www.xmind.net/"); //$NON-NLS-1$
    }

    private void logApplicationArgs() {
        final String[] args = Platform.getApplicationArgs();
        if (args == null || args.length == 0)
            return;

        Log openingLog = Log.get(Log.OPENING);
        for (String arg : args) {
            if ("-p".equals(arg)) {//$NON-NLS-1$
                // The "-p" argument is used to start Presentation Mode
                // immediately on startup:
                System.setProperty(
                        "org.xmind.cathy.startup.presentation", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (arg.startsWith("xmind:") || new File(arg).exists()) { //$NON-NLS-1$
                // Add xmind command or existing file path to '.opening' log:
                openingLog.append(arg);
            } else if (!arg.startsWith("-psn_0_")) { //$NON-NLS-1$
                // The "-psn_0_<ProcessSerialNumber>" argument is passed in by
                // Mac OS X for each GUI application. No need to log that.
                // Log any other unknown command line argument for debugging:
                CathyPlugin.log("Skip unrecognized command line argument: '" //$NON-NLS-1$
                        + arg + "'"); //$NON-NLS-1$
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

    private boolean checkSoftwareUpdateOnStart() {
        return XMindUpdater.checkSoftwareUpdateOnStart();
    }

    /**
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    public void stop() {
        if (!PlatformUI.isWorkbenchRunning())
            return;

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return;

        Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.syncExec(new Runnable() {
            public void run() {
                workbench.close();
            }
        });
    }

}
