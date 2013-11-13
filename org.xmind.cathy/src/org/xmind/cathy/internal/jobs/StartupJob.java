package org.xmind.cathy.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.actions.OpenHomeMapAction;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.editor.NewWorkbookEditor;
import org.xmind.ui.mindmap.MindMapUI;

public class StartupJob extends Job {

    private final IWorkbench workbench;

    public StartupJob(IWorkbench workbench, boolean showProgress) {
        super(WorkbenchMessages.StartupJob_jobName);
        this.workbench = workbench;
        setUser(showProgress);
        setSystem(!showProgress);
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 3);
        doStartup(monitor);
        Display display = workbench.getDisplay();
        if (display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    System.setProperty(
                            "org.xmind.cathy.app.status", "workbenchReady"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            });
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    protected void doStartup(IProgressMonitor monitor) {
        checkAndRecoverFiles(monitor);

        if (monitor.isCanceled())
            return;
        checkAndOpenFiles(monitor);

        if (monitor.isCanceled())
            return;
        waitForInitialFilesLoaded(monitor);

        if (monitor.isCanceled())
            return;
        openStartupMap(monitor);
    }

    /**
     * Wait for a short while before initial files are loaded. On Mac OS X,
     * files opened by double clicking or 'open' command are passed to Eclipse
     * RCP via <code>SWT.OpenDocument</code> event which comes a slight later
     * than workbench startup.
     * 
     * @param monitor
     */
    protected void waitForInitialFilesLoaded(IProgressMonitor monitor) {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            monitor.setCanceled(true);
        }
    }

    protected void openStartupMap(IProgressMonitor monitor) {
        if (!hasOpenedEditors()) {
            int action = CathyPlugin.getDefault().getPreferenceStore()
                    .getInt(CathyPlugin.STARTUP_ACTION);
            if (action == CathyPlugin.STARTUP_ACTION_HOME) {
                monitor.subTask(WorkbenchMessages.StartupJob_OpenHomeMap);
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        workbench.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                IWorkbenchWindow window = workbench
                                        .getActiveWorkbenchWindow();
                                if (window != null) {
                                    final IWorkbenchPage page = window
                                            .getActivePage();
                                    Shell shell = window.getShell();
                                    if (page != null) {
                                        OpenHomeMapAction.openHomeMap(shell,
                                                page);
                                    }
                                }
                            }
                        });
                    }
                });
            } else if (action == CathyPlugin.STARTUP_ACTION_BLANK) {
                monitor.subTask(WorkbenchMessages.StartupJob_OpenBlankMap);
                openBlankMap();
            }

            if (!hasOpenedEditors()) {
                showStartupDialog();
            }
        }
        monitor.worked(1);
    }

    private void showStartupDialog() {
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window != null) {
                    NewWorkbookEditor.showIn(window);
//                    NewWorkbookWizardDialog.openWizard(window, true);
                }
            }
        });
    }

    private boolean hasOpenedEditors() {
        final boolean[] ret = new boolean[1];
        ret[0] = false;
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        if (page.getEditorReferences().length > 0) {
                            ret[0] = true;
                            return;
                        }
                    }
                }
            }
        });
        return ret[0];
    }

    /**
     * Check and recover files opened in the last workbench session. This method
     * consumes 2 ticks from the given progress monitor.
     * 
     * @param monitor
     */
    protected void checkAndRecoverFiles(IProgressMonitor monitor) {
        Job subJob = new CheckRecoverFilesJob(workbench);
        subJob.setUser(isUser());
        subJob.setSystem(isSystem());
        subJob.setProgressGroup(monitor, 1);
        subJob.schedule();
        try {
            subJob.join();
        } catch (InterruptedException e) {
        }
    }

    protected void checkAndOpenFiles(IProgressMonitor monitor) {
        Job subJob = new CheckOpenFilesJob(workbench);
        subJob.setUser(isUser());
        subJob.setSystem(isSystem());
        subJob.setProgressGroup(monitor, 1);
        subJob.schedule();
        try {
            subJob.join();
        } catch (InterruptedException e) {
        }
    }

    private IEditorPart openBlankMap() {
        return openEditor(MME.createNonExistingEditorInput());
    }

    private IEditorPart openEditor(final IEditorInput editorInput) {
        final IEditorPart[] editor = new IEditorPart[1];
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window != null) {
                    final IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        SafeRunner.run(new SafeRunnable() {
                            public void run() throws Exception {
                                editor[0] = page.openEditor(editorInput,
                                        MindMapUI.MINDMAP_EDITOR_ID);
                            }
                        });
                    }
                }
            }
        });
        return editor[0];
    }

}
