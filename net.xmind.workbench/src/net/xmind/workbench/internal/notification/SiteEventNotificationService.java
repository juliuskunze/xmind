package net.xmind.workbench.internal.notification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.IAuthorizationListener;
import net.xmind.signin.XMindNet;
import net.xmind.workbench.internal.Messages;
import net.xmind.workbench.internal.XMindNetWorkbench;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmind.core.Core;
import org.xmind.ui.dialogs.SimpleInfoPopupDialog;

public class SiteEventNotificationService implements IStartup,
        IWorkbenchListener, IAuthorizationListener {

    //private static final int INTERVALS = 1000 * 60 * 60;

    private class CheckSiteEventJob extends Job {

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

            String url = "http://www.xmind.net/_api/events?version=3.2.1&os=" + Platform.getOS() //$NON-NLS-1$
                    + "&arch=" + Platform.getOSArch() //$NON-NLS-1$
                    + "&nl=" + Platform.getNL() //$NON-NLS-1$
                    + "&distrib=" + getDistributionId() //$NON-NLS-1$
                    + "&account_type=" + getAccountType(); //$NON-NLS-1$
            GetMethod method = new GetMethod(url);
            int code;
            try {
                code = new HttpClient().executeMethod(method);
            } catch (Exception e) {
                code = -1;
                XMindNetWorkbench.log(e,
                        "Failed to connect to xmind.net for site event."); //$NON-NLS-1$
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            if (code == HttpStatus.SC_OK) {
                List<ISiteEvent> siteEvents = new ArrayList<ISiteEvent>();
                try {
                    parseEventUpdates(method, siteEvents);
                } catch (Exception e) {
                    XMindNetWorkbench.log(e, "Failed to parse site events."); //$NON-NLS-1$
                    return new Status(IStatus.WARNING,
                            XMindNetWorkbench.PLUGIN_ID,
                            "Failed to parse site events.", e); //$NON-NLS-1$
                }
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                SiteEventStore localStore = getLocalEventStore();
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                List<ISiteEvent> newEvents = localStore.calcNewEvents(
                        siteEvents, startup);
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
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            if (job == this)
                job = null;
            return Status.OK_STATUS;
        }

        private void parseEventUpdates(GetMethod method,
                List<ISiteEvent> updates) throws Exception {
            String jsonString = method.getResponseBodyAsString();
            JSONObject json = new JSONObject(jsonString);
            JSONArray events = json.getJSONArray("events"); //$NON-NLS-1$
            for (int i = 0; i < events.length(); i++) {
                updates.add(new JSONSiteEvent(events.getJSONObject(i)));
            }
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
            if (openExternal) {
                Program.launch(url);
            } else {
                XMindNet.gotoURL(url);
            }
        }
    }

    private IWorkbench workbench;

    private CheckSiteEventJob job;

    private SiteEventStore localEventStore;

    private String accountType = "free"; //$NON-NLS-1$

    private boolean startup = true;

    private IPreferenceStore prefStore;

    public SiteEventNotificationService() {
        prefStore = new ScopedPreferenceStore(new InstanceScope(),
                "org.xmind.cathy"); //$NON-NLS-1$
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
            if (job != null)
                return;
            job = new CheckSiteEventJob(startup);
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

        final Display display = workbench.getDisplay();
        final boolean[] active = new boolean[1];
        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                active[0] = window != null
                        && display.getActiveShell() == window.getShell();
            }
        });
        return active[0];
    }

    private void doShowNotifications(final List<ISiteEvent> events) {
        if (workbench == null)
            return;
        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                final Shell shell = window.getShell();
                if (shell == null || shell.isDisposed())
                    return;

                for (ISiteEvent event : events) {
                    SimpleInfoPopupDialog dialog = new SimpleInfoPopupDialog(
                            shell,
                            null,
                            event.getTitle(),
                            0,
                            event.getMoreUrl() == null ? null
                                    : new URLAction(
                                            Messages.SiteEventNotificationService_More_text,
                                            event.getMoreUrl(), event
                                                    .isOpenExternal()),
                            new URLAction(event.getActionText(), event
                                    .getEventUrl(), event.isOpenExternal()));
                    dialog.setDuration(10000);
                    dialog.setGroupId("org.xmind.notifications"); //$NON-NLS-1$
                    dialog.popUp(shell);
                }
            }
        });
    }

    private void stop() {
        if (job != null) {
            job.cancel();
            job = null;
        }
    }

    public void earlyStartup() {
        this.workbench = PlatformUI.getWorkbench();
        workbench.addWorkbenchListener(this);
        XMindNet.addAuthorizationListener(this);
    }

    public void postShutdown(IWorkbench workbench) {
        if (this.workbench == null)
            return;
        this.workbench.removeWorkbenchListener(this);
        this.workbench = null;
        XMindNet.removeAuthorizationListener(this);
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
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
        return accountType;
    }

    public void authorized(IAccountInfo accountInfo) {
        accountType = accountInfo.hasValidSubscription() ? "pro" : "free"; //$NON-NLS-1$ //$NON-NLS-2$
        XMindNet.removeAuthorizationListener(this);
        checkEvent();
    }

    public void unauthorized(IStatus result, IAccountInfo accountInfo) {
        accountType = "free"; //$NON-NLS-1$
        XMindNet.removeAuthorizationListener(this);
        checkEvent();
    }

}
