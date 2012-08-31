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
import java.io.IOException;

import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.share.XmindSharePlugin;
import net.xmind.signin.IAccountInfo;
import net.xmind.signin.IAuthenticationListener;
import net.xmind.signin.XMindNet;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.CoreException;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;
import org.xmind.ui.dialogs.SimpleInfoPopupDialog;

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

    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = doRun(monitor);

        // prompt completion information to user
        if (status.isOK()) {
            promptCompletion();
            return status;
        }

        if (status.matches(IStatus.ERROR)) {
            // show error dialog
            status = promptError(session.getStatus(), status);

            if (status.isOK() || !status.matches(IStatus.ERROR))
                return status;

            // log this error, but prevent system from prompting dialogs,
            // because we have shown our own dialogs above.
            XmindSharePlugin.getDefault().getLog().log(status);
            return new Status(IStatus.WARNING, status.getPlugin(),
                    status.getCode(), status.getMessage() == null
                            || "".equals(status.getMessage()) ? //$NON-NLS-1$ 
                    Messages.UploadJob_Failure_message
                            : status.getMessage(), status.getException());
        }

        // other status
        return status;
    }

    private IStatus doRun(IProgressMonitor monitor) {
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
        savePermalink();
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
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

    private void savePermalink() {
        if (session.getPermalink() == null)
            return;

        IWorkbook workbook = (IWorkbook) info.getProperty(Info.WORKBOOK);
        if (workbook != null) {
            workbook.getMeta().setValue(Info.SHARE + IMeta.SEP + "SourceUrl", //$NON-NLS-1$
                    session.getPermalink());
            File file = (File) info.getProperty(Info.FILE);
            if (file != null) {
                try {
                    workbook.saveTemp();
                    workbook.save(file.getAbsolutePath());
                } catch (IOException ignore) {
                } catch (CoreException ignore) {
                }
            } else {
                try {
                    workbook.save();
                } catch (IOException ignore) {
                } catch (CoreException ignore) {
                }
            }
        }
    }

    private void promptCompletion() {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(new Runnable() {
            public void run() {
                final SimpleInfoPopupDialog[] dialogs = new SimpleInfoPopupDialog[1];
                IAction viewAction = new Action() {
                    public void run() {
                        showUploadedMap(session.getViewLink());
                        if (dialogs[0] != null)
                            dialogs[0].close();
                    }
                };
                viewAction.setText(Messages.UploadJob_View_text);

                SimpleInfoPopupDialog dialog = new SimpleInfoPopupDialog(null,
                        null, Messages.UploadJob_OpenMap_message, 0, null,
                        viewAction);
                dialog.setDuration(10000);
                dialog.setGroupId("org.xmind.notifications"); //$NON-NLS-1$
                dialog.popUp();
                dialogs[0] = dialog;
            }
        });
    }

    private void showUploadedMap(String url) {
        if (url != null) {
            XMindNet.gotoURL(true, url);
            return;
        }

        IAccountInfo accountInfo = XMindNet.getAccountInfo();
        if (accountInfo == null)
            return;

        String userId = accountInfo.getUser();
        String token = accountInfo.getAuthToken();
        XMindNet.gotoURL(
                String.format("http://www.xmind.net/xmind/account/%s/%s/", //$NON-NLS-1$
                        userId, token), true);
    }

    private IStatus promptError(int uploadStatus, IStatus error) {
        int code = error.getCode();
        String message = null;
        boolean tryAgainAllowed = true;
        if (uploadStatus == UploadSession.PREPARING) {
            if (code > 0) {
                if (code == HttpStatus.SC_UNAUTHORIZED) {
                    resignin();
                    return Status.CANCEL_STATUS;
                }
            }
        } else if (uploadStatus == UploadSession.UPLOADING) {
            if (code > 0) {
                if (code == HttpStatus.SC_NOT_FOUND) {
                    return Status.CANCEL_STATUS;
                } else if (code == UploadSession.CODE_VERIFICATION_FAILURE) {
                    message = Messages.ErrorDialog_Unauthorized_message;
                    tryAgainAllowed = false;
                }
            }
        }

        if (message == null)
            message = Messages.ErrorDialog_message;

        promptErrorMessage(message, tryAgainAllowed);
        return error;
    }

    private void promptErrorMessage(final String message,
            final boolean tryAgainAllowed) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.asyncExec(new Runnable() {
            public void run() {
                if (tryAgainAllowed) {
                    if (MessageDialog.openQuestion(null,
                            Messages.ErrorDialog_title, message)) {
                        schedule();
                    }
                } else {
                    MessageDialog.openError(null, Messages.ErrorDialog_title,
                            message);
                }
            }
        });
    }

    private void resignin() {
        XMindNet.signOut();
        XMindNet.signIn(new IAuthenticationListener() {

            public void postSignIn(IAccountInfo accountInfo) {
                info.setProperty(Info.USER_ID, accountInfo.getUser());
                info.setProperty(Info.TOKEN, accountInfo.getAuthToken());
                schedule();

            }

            public void postSignOut(IAccountInfo oldAccountInfo) {
            }
        }, false);
    }

    @Override
    protected void canceling() {
        if (session != null) {
            session.cancel();
        }
        super.canceling();
    }
}