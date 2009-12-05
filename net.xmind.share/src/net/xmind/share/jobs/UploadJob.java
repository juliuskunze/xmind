/*
 * Copyright (c) 2006-2009 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share.jobs;

import java.io.File;
import java.io.IOException;

import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.share.XmindSharePlugin;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.json.JSONException;
import org.xmind.core.CoreException;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;

public class UploadJob extends Job {

    private Info info;

    private HttpClient client;

    public UploadJob(Info info) {
        super(NLS.bind(Messages.UploadJob_name, info.getString(Info.TITLE)));
        this.info = info;
        this.client = new HttpClient();
    }

    protected IStatus run(IProgressMonitor monitor) {
        try {
            return runWithException(monitor);
        } catch (Throwable e) {
            // inform user the exception

            if (e instanceof AuthenticationException) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(
                        new Runnable() {
                            public void run() {
                                MessageDialog
                                        .openError(
                                                null,
                                                Messages.ErrorDialog_title,
                                                Messages.ErrorDialog_Unauthorized_message);
                            }
                        });
            } else {
                PlatformUI.getWorkbench().getDisplay().asyncExec(
                        new Runnable() {
                            public void run() {
                                if (MessageDialog.openQuestion(null,
                                        Messages.ErrorDialog_title,
                                        Messages.ErrorDialog_message)) {
                                    schedule();
                                }
                            }
                        });
            }
            // log this error, but don't let user see it.
            XmindSharePlugin.getDefault().getLog().log(
                    new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID,
                            Messages.UploadJob_Failure_message, e));
            return new Status(IStatus.WARNING, XmindSharePlugin.PLUGIN_ID,
                    IStatus.ERROR, Messages.UploadJob_Failure_message, e);
        }
    }

    private IStatus runWithException(IProgressMonitor monitor) throws Exception {
        String title = info.getString(Info.TITLE);
        Assert.isNotNull(title);
        String userName = info.getString(Info.USER_ID);
        Assert.isNotNull(userName);
        String token = info.getString(Info.TOKEN);
        Assert.isNotNull(token);
        File file = (File) info.getProperty(Info.FILE);

        monitor.beginTask(null, 100);

        // Retrieve session and url
        monitor.subTask(Messages.UploadJob_Task_Prepare);
        String[] data = HttpUtils.prepareUpload(client, userName, token, title);
        String session = data[0];
        String url = data[1];
        String mapname = data[2];

        if (url != null) {
            IWorkbook workbook = (IWorkbook) info.getProperty(Info.WORKBOOK);
            if (workbook != null) {
                workbook.getMeta().setValue(
                        Info.SHARE + IMeta.SEP + "SourceUrl", //$NON-NLS-1$
                        url);
                try {
                    workbook.save();
                } catch (IOException ignore) {
                } catch (CoreException ignore) {
                }
            }
        }

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        monitor.worked(10);

        monitor.subTask(Messages.UploadJob_Task_TransferFile);

        // Start file uploading.
        TransferFileJob uploadJob = new TransferFileJob(userName, session, file);
        Thread uploadThread = new Thread(uploadJob);
        uploadThread.setName("Upload Map (" + title + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        uploadThread.setPriority(Thread.NORM_PRIORITY);
        uploadThread.start();

        loopRetrieveProgress(monitor, userName, token, session, uploadJob);

        if (monitor.isCanceled()) {
            monitor.subTask(Messages.UploadJob_Task_Cancel);
            try {
                HttpUtils.cancelUploading(client, userName, session, token);
            } catch (Exception ignore) {
            }
            return Status.CANCEL_STATUS;
        } else {
            if (uploadJob.getException() != null) {
                throw uploadJob.getException();
            }
            monitor.done();
        }

        uploadJob = null;

        if (mapname != null) {
            final String mapURL = "http://www.xmind.net/xmind/map/" + userName //$NON-NLS-1$ 
                    + "/" + token + "/" + mapname; //$NON-NLS-1$ //$NON-NLS-2$
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    int retCode = new MessageDialog(null,
                            Messages.UploadJob_OpenMap_title, null,
                            Messages.UploadJob_OpenMap_message, 0,
                            new String[] { Messages.UploadJob_View_text,
                                    Messages.UploadJob_Close_text }, 0).open();
                    if (retCode == 0) {
                        new OpenMapJob(mapURL);
                    }
                }
            });
        }
        return Status.OK_STATUS;
    }

    private void loopRetrieveProgress(IProgressMonitor monitor,
            String userName, String token, String session,
            TransferFileJob uploadJob) throws HttpException, IOException {
        int uploaded = 0;
        while (!monitor.isCanceled()) {
            try {
                double progress = HttpUtils.retrieveUploadingProcess(client,
                        userName, session, token);
                if (progress < 0)
                    break;

                int newUploaded = (int) (progress * 90);
                if (newUploaded > uploaded) {
                    monitor.worked(newUploaded - uploaded);
                    uploaded = newUploaded;
                }
            } catch (JSONException ignore) {
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

}