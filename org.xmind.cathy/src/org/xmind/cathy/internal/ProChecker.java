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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Properties;

import net.xmind.signin.ISignInListener;
import net.xmind.signin.XMindNetEntry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmind.ui.internal.actions.TryProAction;

/*
 * @author Frank Shaka
 */
@Deprecated
public class ProChecker implements ISignInListener {

    private static boolean DEBUG = false;

    private class CheckProUserJob extends Job {

        /**
         * @param name
         */
        public CheckProUserJob() {
            super(WorkbenchMessages.ProChecker_CheckPro_jobName);
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (!isApplicationPro()) {

                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                if (isUserPro()) {

                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;

                    downloadAndInstall();
                }
            }
            return Status.OK_STATUS;
        }

        /**
         * @throws MalformedURLException
         * 
         */
        private void downloadAndInstall() {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            Display display = workbench.getDisplay();
            if (display != null && !display.isDisposed()) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        IWorkbenchWindow window = workbench
                                .getActiveWorkbenchWindow();
                        new TryProAction(window).run();
                    }
                });
            }
//            new File(downloadPath).mkdir();
//            fileName = DOWNLOAD_URL
//                    .substring(DOWNLOAD_URL.lastIndexOf('/') + 1);
//
//            DownloadJob downloadJob = new DownloadJob("Downloading XMind Pro",
//                    DOWNLOAD_URL, new File(downloadPath, fileName)
//                            .getAbsolutePath());
//            downloadJob.addJobChangeListener(new JobChangeAdapter() {
//                /*
//                 * (non-Javadoc)
//                 * 
//                 * @see
//                 * org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse
//                 * .core.runtime.jobs.IJobChangeEvent)
//                 */
//                @Override
//                public void done(IJobChangeEvent event) {
//                    if (event.getResult().getCode() == DownloadJob.SUCCESS) {
//                        install();
//                    }
//                }
//            });
//            downloadJob.schedule();
        }

        /**
         * @return
         */
        private boolean isApplicationPro() {
            return Platform.getBundle("org.xmind.meggy") != null; //$NON-NLS-1$
        }

        /**
         * @return
         */
        private boolean isUserPro() {
            Properties userInfo = XMindNetEntry.getCurrentUserInfo();
            if (userInfo == null)
                return false;

            String userId = userInfo.getProperty(XMindNetEntry.USER_ID);
            String token = userInfo.getProperty(XMindNetEntry.TOKEN);
            String url = "http://www.xmind.net/xmind/verify/?user=" + userId //$NON-NLS-1$
                    + "&token=" + token; //$NON-NLS-1$
            GetMethod method = new GetMethod(url);
            method.setRequestHeader("AuthToken", token); //$NON-NLS-1$
            method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$

            HttpClient client = new HttpClient();
            try {
                int result = client.executeMethod(method);
                debug("result: " + result); //$NON-NLS-1$
                if (HttpStatus.SC_OK == result) {
                    boolean expired = isExpired(client, method);
                    debug("valid: " + (!expired)); //$NON-NLS-1$
                    return !expired;
                }
            } catch (Throwable e) {
            }
            return false;
        }

        private boolean isExpired(HttpClient client, GetMethod method)
                throws JSONException, IOException {
            String resp = method.getResponseBodyAsString();
            debug("response: " + resp); //$NON-NLS-1$
            JSONObject json = new JSONObject(resp);
            boolean expired = json.getBoolean("expired"); //$NON-NLS-1$
            if (expired)
                return true;
            long expireDate = json.getLong("expireDate"); //$NON-NLS-1$
            if (expireDate < 0)
                return true;
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(expireDate);
            return date.before(Calendar.getInstance());
        }

    }

    private CheckProUserJob currentJob = null;

    /*
     * (non-Javadoc)
     * 
     * @see net.xmind.signin.ISignInListener#postSignIn(java.util.Properties)
     */

    public void postSignIn(Properties userInfo) {
        if (currentJob != null) {
            currentJob.cancel();
        }
        currentJob = new CheckProUserJob();
        currentJob.schedule();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xmind.signin.ISignInListener#postSignOut()
     */
    public void postSignOut() {
    }

    private static void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

}
