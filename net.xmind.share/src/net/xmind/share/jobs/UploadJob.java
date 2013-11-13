/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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

import net.xmind.share.FileValidationException;
import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.share.Uploader;
import net.xmind.share.XmindSharePlugin;
import net.xmind.signin.internal.Activator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;

public class UploadJob extends Job {

    private class TransferWorker implements Runnable {

        private Thread thread = null;

        public void start() {
            if (thread != null)
                return;
            thread = new Thread(this);
            thread.setName("Upload:" + info.getString(Info.TITLE)); //$NON-NLS-1$
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.start();
        }

        public void run() {
            session.transfer();
        }

    }

    private class ProgressWorker implements Runnable {

        private IProgressMonitor monitor;

        private int ticks;

        private Thread thread = null;

        private int ticksUploaded = 0;

        public ProgressWorker(IProgressMonitor monitor, int ticks) {
            this.monitor = monitor;
            this.ticks = ticks;
        }

        public void start() {
            if (thread != null)
                return;
            thread = new Thread(this);
            thread.setName("ProgressWorker:" + info.getString(Info.TITLE)); //$NON-NLS-1$
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

        public void cancel() {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }

        public boolean isCompleted() {
            return session.getStatus() == UploadSession.COMPLETED;
        }

        public void run() {
            while (!monitor.isCanceled() && !session.hasError()
                    && !isCompleted()) {

                session.retrieveProgress();
                if (monitor.isCanceled() || session.hasError())
                    return;

                if (isCompleted()) {
                    monitor.worked(ticks - ticksUploaded);
                    ticksUploaded = ticks;
                    return;
                }

                int newUploaded = (int) (session.getUploadProgress() * ticks);
                if (newUploaded > ticksUploaded) {
                    monitor.worked(newUploaded - ticksUploaded);
                    ticksUploaded = newUploaded;
                }

                try {
                    Thread.sleep(760);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

    }

    private Info info;

    private UploadSession session;

    private TransferWorker transferWorker;

    public UploadJob(Info info) {
        super(NLS.bind(Messages.UploadJob_name, info.getString(Info.TITLE)));
        this.info = info;
    }

    /**
     * @return the session
     */
    public UploadSession getSession() {
        return session;
    }

    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = upload(monitor);
        if (status.matches(IStatus.ERROR)) {
            // log this error, but prevent system from prompting error dialogs,
            // leaving error displaying for Uploader.
            XmindSharePlugin.getDefault().getLog().log(status);
            status = new Status(IStatus.WARNING, status.getPlugin(),
                    status.getCode(), status.getMessage() == null
                            || "".equals(status.getMessage()) ? //$NON-NLS-1$ 
                    Messages.UploadJob_Failure_message
                            : status.getMessage(), status.getException());
        }
        return status;
    }

    private IStatus upload(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);
        session = new UploadSession(info);

        // retrieve session and url
        monitor.subTask(Messages.UploadJob_Task_Prepare);
        session.prepare();
        if (session.hasError())
            return session.getError();
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(5);

        // save the permalink into file
        String permalink = session.getPermalink();
        if (permalink == null)
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "Upload session is prepared, but has no permalink to use."); //$NON-NLS-1$
        IStatus saved = savePermalink(permalink);
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        if (saved != null && !saved.isOK())
            return saved;
        monitor.worked(5);

        // start transfering file data up to XMind server
        monitor.subTask(Messages.UploadJob_Task_TransferFile);
        transferWorker = new TransferWorker();
        transferWorker.start();
        if (session.hasError())
            return session.getError();
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        // wait for processing completion, error or user canceling
        // while retrieving transfer progress
        ProgressWorker progressWorker = new ProgressWorker(monitor, 89);
        progressWorker.start();
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return Status.CANCEL_STATUS;
            }

            // uploading failed
            if (session.hasError())
                return session.getError();

            // cancel uploading
            if (monitor.isCanceled()) {
                monitor.subTask(Messages.UploadJob_Task_Cancel);
                session.cancel();
                progressWorker.cancel();
                if (session.hasError())
                    return session.getError();
                return Status.CANCEL_STATUS;
            }

        } while (!progressWorker.isCompleted());

        // uploading completed
        monitor.done();
        return Status.OK_STATUS;
    }

    private IStatus savePermalink(String permalink) {
        IWorkbook workbook = (IWorkbook) info.getProperty(Info.WORKBOOK);
        if (workbook == null)
            return new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID,
                    "Failed to save source URL. No workbook available"); //$NON-NLS-1$

        workbook.getMeta().setValue(Info.SHARE + IMeta.SEP + "SourceUrl", //$NON-NLS-1$
                permalink);
        File file = (File) info.getProperty(Info.FILE);
        if (file == null)
            return new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID,
                    "Failed to save source URL. No file available."); //$NON-NLS-1$

        String path = file.getAbsolutePath();
        try {
            workbook.save(path);
        } catch (Throwable e) {
            return new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID,
                    "Failed to save source URL due to error: " //$NON-NLS-1$
                            + e.getLocalizedMessage(), e);
        }
        try {
            Uploader.validateUploadFile(path);
        } catch (FileValidationException e) {
            return new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID,
                    "Failed to save source URL due to file corruption: " //$NON-NLS-1$
                            + e.getLocalizedMessage(), e);
        }
        return Status.OK_STATUS;
    }

    @Override
    protected void canceling() {
        if (session != null) {
            session.cancel();
        }
        super.canceling();
    }
}