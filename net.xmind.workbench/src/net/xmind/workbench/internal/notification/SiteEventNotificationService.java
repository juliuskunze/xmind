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
package net.xmind.workbench.internal.notification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import net.xmind.signin.IDataStore;
import net.xmind.signin.ILicenseInfo;
import net.xmind.signin.ILicenseListener;
import net.xmind.signin.XMindNet;
import net.xmind.signin.internal.XMindNetRequest;
import net.xmind.workbench.internal.Messages;
import net.xmind.workbench.internal.XMindNetWorkbench;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.xmind.core.Core;
import org.xmind.ui.dialogs.NotificationWindow;

public class SiteEventNotificationService implements ILicenseListener {

    private static int POPUP_DURATION = 60000;

    private class CheckSiteEventJob extends Job {

        private XMindNetRequest request = null;

        private boolean startup;

        public CheckSiteEventJob(boolean startup) {
            super("Check Site Events"); //$NON-NLS-1$
            setSystem(true);
            setPriority(DECORATE);
            this.startup = startup;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            request = new XMindNetRequest();
            request.path("/_api/events"); //$NON-NLS-1$
            request.addParameter("version", productVersion); //$NON-NLS-1$
            String buildId = System.getProperty("org.xmind.product.buildid"); //$NON-NLS-1$
            if (buildId != null) {
                request.addParameter("buildid", buildId); //$NON-NLS-1$
            }
            request.addParameter("os", Platform.getOS()); //$NON-NLS-1$
            request.addParameter("arch", Platform.getOSArch()); //$NON-NLS-1$
            request.addParameter("nl", Platform.getNL()); //$NON-NLS-1$
            request.addParameter("distrib", getDistributionId()); //$NON-NLS-1$
            request.addParameter("account_type", getAccountType()); //$NON-NLS-1$
            request.get();

            if (monitor.isCanceled() || request.isAborted())
                return Status.CANCEL_STATUS;

            int code = request.getStatusCode();
            IDataStore data = request.getData();
            if (code == XMindNetRequest.HTTP_OK) {
                if (data != null) {
                    return handleSiteEvents(monitor, data);
                } else {
                    XMindNetWorkbench.log(request.getError(),
                            "Failed to parse response of site events."); //$NON-NLS-1$
                }
            } else if (code == XMindNetRequest.HTTP_ERROR) {
                XMindNetWorkbench.log(request.getError(),
                        "Failed to connect to xmind.net for site events."); //$NON-NLS-1$
            } else {
                XMindNetWorkbench.log(request.getError(),
                        "Failed to retrieve site events: " + code); //$NON-NLS-1$
            }
            return Status.CANCEL_STATUS;
        }

        protected IStatus handleSiteEvents(IProgressMonitor monitor,
                IDataStore data) {
            List<ISiteEvent> siteEvents = new ArrayList<ISiteEvent>();
            for (IDataStore siteData : data.getChildren("events")) { //$NON-NLS-1$
                siteEvents.add(new DataStoreSiteEvent(siteData));
            }
            // for test:
//          JSONObject js = new JSONObject();
//          js.put("id", "9999");
//          js.put("prompt", "always");
//          js.put("title", "XMind official group on Biggerplate");
//          js.put("url", "http://blog.xmind.net/en/2012/02/biggerplate/");
//          siteEvents.add(new JSONSiteEvent(js));
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            SiteEventStore localStore = getLocalEventStore();
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            List<ISiteEvent> newEvents = localStore.calcNewEvents(siteEvents,
                    startup);
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            if (!newEvents.isEmpty()) {
                showNotifications(newEvents);
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            setLocalEventStore(new SiteEventStore(siteEvents));
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            try {
                saveLocalEventStore();
            } catch (IOException e) {
                XMindNetWorkbench.log(e, "Failed to save site events."); //$NON-NLS-1$
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            return Status.OK_STATUS;
        }

        @Override
        protected void canceling() {
            if (request != null) {
                request.abort();
            }
            super.canceling();
        }

    }

    private static class URLAction extends Action {

        private String url;

        private boolean openExternal;

        public URLAction(String text, String url, boolean openExternal) {
            super(
                    text == null ? Messages.SiteEventNotificationService_View_text
                            : text);
            this.url = url;
            this.openExternal = openExternal;
        }

        @Override
        public void run() {
            XMindNet.gotoURL(openExternal, url);
        }
    }

    private IWorkbench workbench;

    private CheckSiteEventJob job;

    private SiteEventStore localEventStore;

    private String licenseType = "free"; //$NON-NLS-1$

    private boolean startup = true;

    private IPreferenceStore prefStore;

    private String productVersion;

    public SiteEventNotificationService(IWorkbench workbench) {
        Assert.isNotNull(workbench);
        this.workbench = workbench;
        prefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
                "org.xmind.cathy"); //$NON-NLS-1$
        productVersion = System.getProperty("org.xmind.product.version"); //$NON-NLS-1$
    }

    private SiteEventStore getLocalEventStore() {
        if (localEventStore == null) {
            File file = getLocalFile();
            if (file.isFile()) {
                try {
                    localEventStore = new SiteEventStore(new InputStreamReader(
                            new FileInputStream(file), "UTF-8")); //$NON-NLS-1$
                } catch (IOException e) {
                    localEventStore = new SiteEventStore();
                }
            } else {
                localEventStore = new SiteEventStore();
            }
        }
        return localEventStore;
    }

    private void setLocalEventStore(SiteEventStore store) {
        this.localEventStore = store;
    }

    private void saveLocalEventStore() throws IOException {
        if (localEventStore == null)
            return;

        File file = getLocalFile();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        localEventStore.save(new OutputStreamWriter(new FileOutputStream(file),
                "UTF-8")); //$NON-NLS-1$
    }

    private static File getLocalFile() {
        return new File(Core.getWorkspace().getAbsolutePath("site-events.xml")); //$NON-NLS-1$
    }

    private void checkEvent() {
        if (isNotificationAllowed()) {
            if (job != null && job.getResult() == null)
                return;
            job = new CheckSiteEventJob(startup);
            job.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(IJobChangeEvent event) {
                    if (job == event.getJob()) {
                        job = null;
                    }
                }
            });
            job.schedule();
            startup = false;
        } else {
            stop();
        }
    }

    private boolean isNotificationAllowed() {
        return prefStore.getBoolean("checkUpdatesOnStartup"); //$NON-NLS-1$
    }

    private void showNotifications(final List<ISiteEvent> events) {
        if (workbench == null)
            return;
        if (canShowNotifications()) {
            doShowNotifications(events);
        } else {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    while (!canShowNotifications()) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    doShowNotifications(events);
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            thread.setName("WaitToShowNotifications"); //$NON-NLS-1$
            thread.start();
        }
    }

    private boolean canShowNotifications() {
        if (workbench == null)
            return false;

        final IWorkbench wb = workbench;
        final Display display = wb.getDisplay();
        final boolean[] active = new boolean[1];
        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
                active[0] = window != null
                        && display.getActiveShell() == window.getShell();
            }
        });
        return active[0];
    }

    private void doShowNotifications(final List<ISiteEvent> events) {
        if (workbench == null)
            return;
        final IWorkbench wb = workbench;
        final Display display = wb.getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                final Shell shell = window.getShell();
                if (shell == null || shell.isDisposed())
                    return;

                for (ISiteEvent event : events) {
                    String caption = event.getCaption();
                    String url = event.getInternalUrl();
                    if (url == null) {
                        url = event.getEventUrl();
                    }
                    String moreUrl = event.getMoreUrl();
                    String text = event.getHTML();
                    if (text == null) {
                        text = event.getText();
                    }
                    int duration = event.getDuration();
                    if (duration < 0) { // long time stay
                        duration = 0;
                    } else if (duration == 0) { // default value
                        duration = POPUP_DURATION;
                    }
                    if (text != null) {
                        IAction action = new URLAction(text, url, event
                                .isOpenExternal());
                        IAction moreAction = moreUrl == null ? null
                                : new URLAction(
                                        Messages.SiteEventNotificationService_More_text,
                                        moreUrl, event.isOpenExternal());
                        new NotificationWindow(shell, caption, action,
                                moreAction, duration).setCloseOnMoreLink(true)
                                .open();
                    }
                }
            }
        });
    }

    private void stop() {
        if (job != null) {
            Thread thread = job.getThread();
            job.cancel();
            if (thread != null) {
                thread.interrupt();
            }
            job = null;
        }
    }

    protected void parseLicenseType(int type) {
        if ((type & ILicenseInfo.VALID_PRO_LICENSE) != 0) {
            licenseType = "pro_license"; //$NON-NLS-1$
        } else if ((type & ILicenseInfo.VALID_PLUS_LICENSE) != 0) {
            licenseType = "plus_license"; //$NON-NLS-1$
        } else if ((type & ILicenseInfo.VALID_PRO_SUBSCRIPTION) != 0) {
            licenseType = "pro"; //$NON-NLS-1$
        } else {
            licenseType = "free"; //$NON-NLS-1$
        }
    }

    public void start() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return;
        }
        if (productVersion != null && !"".equals(productVersion)) { //$NON-NLS-1$
            int type = XMindNet.getLicenseInfo().getType();
            if (type == ILicenseInfo.VERIFYING) {
                XMindNet.addLicenseListener(this);
            } else {
                parseLicenseType(type);
                checkEvent();
            }
        }
    }

    public void shutdown() {
        stop();
        XMindNet.removeLicenseListener(this);
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        stop();
        return true;
    }

    private static final String getDistributionId() {
        String distribId = System
                .getProperty("org.xmind.product.distribution.id"); //$NON-NLS-1$
        if (distribId == null || "".equals(distribId)) { //$NON-NLS-1$
            distribId = "cathy_portable"; //$NON-NLS-1$
        }
        return distribId;
    }

    private String getAccountType() {
        return licenseType;
    }

    public void licenseVerified(ILicenseInfo info) {
        XMindNet.removeLicenseListener(this);
        parseLicenseType(info.getType());
        checkEvent();
    }

//    public void authorized(IAccountInfo accountInfo) {
//        accountType = accountInfo.hasValidSubscription() ? "pro" : "free"; //$NON-NLS-1$ //$NON-NLS-2$
//        XMindNet.removeAuthorizationListener(this);
//        checkEvent();
//    }
//
//    public void unauthorized(IStatus result, IAccountInfo accountInfo) {
//        accountType = "free"; //$NON-NLS-1$
//        XMindNet.removeAuthorizationListener(this);
//        checkEvent();
//    }

}
