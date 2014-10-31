package org.xmind.ui.internal.evernote.export;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.dialogs.Notification;
import org.xmind.ui.evernote.EvernotePlugin;
import org.xmind.ui.evernote.signin.Evernote;
import org.xmind.ui.evernote.signin.IEvernoteAccount;
import org.xmind.ui.internal.evernote.ErrorStatusDialog;
import org.xmind.ui.internal.evernote.EvernoteMessages;
import org.xmind.ui.mindmap.IMindMapViewer;

import com.evernote.edam.error.EDAMErrorCode;

/**
 * @author Jason Wong
 */
public class EvernoteExportHandler extends AbstractHandler {

    private EvernoteExportJob job = null;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEvernoteAccount accountInfo = Evernote.signIn();
        export(findCurrentViewer(event), accountInfo);
        return null;
    }

    private void export(IMindMapViewer viewer, IEvernoteAccount accountInfo) {
        if (viewer == null || accountInfo == null)
            return;

        final Control control = viewer.getControl();
        if (control == null || control.isDisposed())
            return;

        final Display display = control.getDisplay();
        final Shell shell = control.getShell();

        job = new EvernoteExportJob(viewer, accountInfo);
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                final IStatus result = event.getResult();
                if (result.getSeverity() == IStatus.WARNING) {
                    display.syncExec(new Runnable() {
                        public void run() {
                            Dialog errorDialog = new ErrorStatusDialog(shell,
                                    result.getMessage());
                            errorDialog.open();
                        }
                    });

                    if (isAuthFailed(result.getCode())) {
                        Evernote.signOut();
                        Evernote.signIn();
                    }
                } else if (result.getSeverity() == IStatus.OK) {
                    display.syncExec(new Runnable() {
                        public void run() {
                            showNotification();
                        }
                    });
                }

                if (job == event.getJob()) {
                    job = null;
                }
            }
        });
        job.setUser(false);
        job.setSystem(false);
        job.schedule();

        block(display);
    }

    private void block(Display display) {
        while (job != null) {
            if (display == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        }
    }

    private IMindMapViewer findCurrentViewer(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor != null && editor instanceof IGraphicalEditor) {
            IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                    .getActivePageInstance();
            if (page != null) {
                IGraphicalViewer viewer = page.getViewer();
                if (viewer instanceof IMindMapViewer)
                    return (IMindMapViewer) viewer;
            }
        }
        return null;
    }

    private boolean isAuthFailed(int code) {
        if (code == EDAMErrorCode.AUTH_EXPIRED.getValue() //
                || code == EDAMErrorCode.INVALID_AUTH.getValue())
            return true;
        return false;
    }

    private void showNotification() {
        /** Notification */
        final Notification[] notification = new Notification[1];
        IAction okAction = new Action() {
            @Override
            public void run() {
                notification[0].close();
            }
        };
        okAction.setText(EvernoteMessages.EvernoteExportHandler_okActionLabel);
        okAction.setImageDescriptor(EvernotePlugin.imageDescriptorFromPlugin(
                EvernotePlugin.PLUGIN_ID, "icons/evernote.png")); //$NON-NLS-1$

        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        Shell parentShell = window == null ? Display.getCurrent()
                .getActiveShell() : window.getShell();
        notification[0] = new Notification(parentShell, null,
                EvernoteMessages.EvernoteExportHandler_successfullySaveText, okAction,
                null);

        notification[0].setGroupId(EvernotePlugin.PLUGIN_ID);
        notification[0].setCenterPopUp(true);
        notification[0].setDuration(-1);
        notification[0].popUp();
    }

}
