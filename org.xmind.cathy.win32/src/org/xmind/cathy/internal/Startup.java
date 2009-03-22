/*
 * Copyright (c) 2006-2008 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.cathy.internal;

import java.io.File;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.actions.SimpleOpenAction;

public class Startup implements IStartup {

    private IWorkbenchWindow primaryWindow;

    public void earlyStartup() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        display.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window != null) {
                    hookWindow(window);
                }
            }
        });
    }

    private void hookWindow(IWorkbenchWindow window) {
        this.primaryWindow = window;
        Shell shell = window.getShell();
        if (shell != null && !shell.isDisposed()) {
            int hWnd = shell.handle;
            logPrimaryWindow(hWnd);
            shell.addShellListener(new ShellAdapter() {
                public void shellActivated(ShellEvent e) {
                    super.shellActivated(e);
                    checkLog();
                }
            });
            shell.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    Log.get(Log.SINGLETON).delete();
                    e.display.asyncExec(new Runnable() {
                        public void run() {
                            checkRemainingWindow();
                        }
                    });
                }
            });
        }
    }

    private void checkRemainingWindow() {
        if (!PlatformUI.isWorkbenchRunning())
            return;
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }
        hookWindow(window);
    }

    private void logPrimaryWindow(int hWnd) {
        Log log = Log.get(Log.SINGLETON);
        if (log.exists()) {
            log.delete();
        }
        log.getProperties().setProperty(Log.K_PRIMARY_WINDOW,
                String.valueOf(hWnd));
        log.saveProperties();
    }

    private void checkLog() {
        Log log = Log.get(Log.OPENING);
        if (log.exists()) {
            String[] contents = log.getContents();
            for (String line : contents) {
                open(line);
            }
            log.delete();
        }
    }

    private void open(String path) {
        File file = new File(path);
        if (file.isFile() && file.canRead()) {
            if (primaryWindow == null)
                SimpleOpenAction.open(path);
            else
                SimpleOpenAction.open(primaryWindow, path);
        }
    }

    //    public void earlyStartup() {
    ////        initXULRunnerPath();
    //        //initProxyService();
    //    }

    //    private static final String KEY_XULRUNNER_PATH = "org.eclipse.swt.browser.XULRunnerPath"; //$NON-NLS-1$

    //    private void initXULRunnerPath() {
    //        String xulRunnerPath = System.getProperty(KEY_XULRUNNER_PATH);
    //        if (xulRunnerPath == null) {
    //            xulRunnerPath = getXULRunnerPath();
    //            if (xulRunnerPath != null) {
    //                System.setProperty(KEY_XULRUNNER_PATH, xulRunnerPath);
    //            }
    //        }
    //    }
    //    
    //    private String getXULRunnerPath() {
    //        URL instUrl = Platform.getInstallLocation().getURL();
    //        try {
    //            instUrl = FileLocator.toFileURL(instUrl);
    //        } catch (IOException e1) {
    //        }
    //        String path = instUrl.getFile();
    //        if (path != null && !"".equals(path)) { //$NON-NLS-1$
    //            File dir = new File(new File(path, "misc"), "xulrunner-1.8.1.3");
    //            if (dir.exists())
    //                return dir.getAbsolutePath();
    //        }
    //        return null;
    //    }
    //
}