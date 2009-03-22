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
package org.xmind.cathy.internal;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * This class controls all aspects of the application's execution
 */
public class CathyApplication implements IApplication {

    public static final String ARG_ACTIVATE = "--activate"; //$NON-NLS-1$

    /**
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
     */
    public Object start(IApplicationContext context) throws Exception {
        String[] args = Platform.getApplicationArgs();
        if (args != null && args.length > 0) {
            logArgs(args);
        }
        if (shouldExitEarly())
            return EXIT_OK;

        Display display = PlatformUI.createDisplay();
        try {
            int returnCode = PlatformUI.createAndRunWorkbench(display,
                    new CathyWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return EXIT_RESTART;
            }
            return EXIT_OK;
        } finally {
            display.dispose();
        }
    }

    private void logArgs(String[] args) {
        Log opening = Log.get(Log.OPENING);
        for (String arg : args) {
            if (new File(arg).isFile()) {
                try {
                    arg = new File(arg).getCanonicalPath();
                } catch (Exception e) {
                    arg = new File(arg).getAbsolutePath();
                }
                opening.append(arg);
            }
        }
    }

    private boolean shouldExitEarly() {
        Bundle bundle = CathyPlugin.getDefault().getBundle();
        try {
            Class clazz = bundle
                    .loadClass("org.xmind.cathy.internal.ApplicationValidator"); //$NON-NLS-1$
            if (IApplicationValidator.class.isAssignableFrom(clazz)) {
                try {
                    return ((IApplicationValidator) clazz.newInstance())
                            .shouldApplicationExitEarly();
                } catch (Exception e) {
                }
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

}