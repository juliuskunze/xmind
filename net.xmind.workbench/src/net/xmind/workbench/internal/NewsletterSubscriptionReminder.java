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
package net.xmind.workbench.internal;

import net.xmind.signin.XMindNet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.dialogs.NotificationWindow;

public class NewsletterSubscriptionReminder {

    private static final String SETTINGS_SECTION = "newsletterSubscription"; //$NON-NLS-1$

    private static final String KEY_LAUNCH_COUNT = "launchCount"; //$NON-NLS-1$

    private static final String KEY_NEVER_REMIND = "neverRemind"; //$NON-NLS-1$

    private static final int DISPLAY_DURATION = 1000 * 60 * 5;

    private static IDialogSettings SETTINGS = null;

    private IWorkbench workbench;

    public NewsletterSubscriptionReminder(IWorkbench workbench) {
        this.workbench = workbench;
    }

    public void start() {
        IDialogSettings settings = getSettings();
        int launchCount = getInt(settings, KEY_LAUNCH_COUNT, 0);
        if (launchCount >= 3 && !settings.getBoolean(KEY_NEVER_REMIND)) {
            scheduleReminder();
        }
        if (launchCount < Integer.MAX_VALUE) {
            launchCount++;
            settings.put(KEY_LAUNCH_COUNT, launchCount);
        }
    }

    private void scheduleReminder() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                // Wait for workbench actually ready:
                try {
                    while (!"workbenchReady".equals(System.getProperty("org.xmind.cathy.app.status"))) { //$NON-NLS-1$ //$NON-NLS-2$
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    return;
                }

                // Wait for 10 seconds to give user a break:
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }

                // Shows the reminder notification:
                final Display display = workbench.getDisplay();
                if (display != null && !display.isDisposed()) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            showReminder(display);
                        }
                    });
                }
            }
        });
        thread.setName("ShowNewsletterSubscriptionReminder"); //$NON-NLS-1$
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    private void showReminder(Display display) {
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null)
            return;

        final Shell shell = window.getShell();
        if (shell == null || shell.isDisposed())
            return;

        IAction action = new Action() {
            public void run() {
                openSubscriptionForm(shell);
            }
        };
        action.setText(Messages.NewsletterSubscriptionReminder_message);
        new NotificationWindow(shell, null, action, null, DISPLAY_DURATION)
                .open();
        neverRemind();
    }

    private void openSubscriptionForm(Shell shell) {
        XMindNet.gotoURL(XMindNetWorkbench.URL_SUBSCRIBE_NEWSLETTER);
    }

    private static int getInt(IDialogSettings settings, String key,
            int defaultValue) {
        try {
            return settings.getInt(key);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    private static IDialogSettings getSettings() {
        if (SETTINGS == null) {
            IDialogSettings mainSettings = XMindNetWorkbench.getDefault()
                    .getDialogSettings();
            SETTINGS = mainSettings.getSection(SETTINGS_SECTION);
            if (SETTINGS == null) {
                SETTINGS = mainSettings.addNewSection(SETTINGS_SECTION);
            }
        }
        return SETTINGS;
    }

    public static void neverRemind() {
        getSettings().put(KEY_NEVER_REMIND, true);
    }

}
