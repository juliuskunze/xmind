package org.xmind.cathy.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public class StartupJob extends Job {

    private final IWorkbench workbench;

    public StartupJob(IWorkbench workbench) {
        super(WorkbenchMessages.StartupJob_jobName);
        this.workbench = workbench;
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 4);
        doStartup(monitor);
        monitor.done();
        return Status.OK_STATUS;
    }

    protected void doStartup(IProgressMonitor monitor) {
        checkAndRecoverFiles(monitor);
        checkAndOpenFiles(monitor);
        openBootstrapEditor(monitor);
        checkUpdates(monitor);
    }

    /**
     * Check and recover files opened in the last workbench session. This method
     * consumes 2 ticks from the given progress monitor.
     * 
     * @param monitor
     */
    protected void checkAndRecoverFiles(IProgressMonitor monitor) {
        Job subJob = new CheckRecoverFilesJob(workbench);
        subJob.setProgressGroup(monitor, 1);
        subJob.schedule();
        try {
            subJob.join();
        } catch (InterruptedException e) {
        }
    }

    protected void checkAndOpenFiles(IProgressMonitor monitor) {
        Job subJob = new CheckOpenFilesJob(workbench);
        subJob.setProgressGroup(monitor, 1);
        subJob.schedule();
        try {
            subJob.join();
        } catch (InterruptedException e) {
        }
    }

    private void openBootstrapEditor(final IProgressMonitor monitor) {
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window != null) {
                    final IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        IEditorPart editor = page.getActiveEditor();
                        if (editor == null) {
                            monitor
                                    .subTask(WorkbenchMessages.StartupJob_OpenBootstrapEditor_name);
                            SafeRunner.run(new SafeRunnable() {
                                public void run() throws Exception {
                                    page.openEditor(MME
                                            .createNonExistingEditorInput(),
                                            MindMapUI.MINDMAP_EDITOR_ID);
                                }
                            });
                        }
                    }
                }
            }
        });
        monitor.worked(1);
    }

    private void checkUpdates(IProgressMonitor monitor) {
        if (CathyPlugin.getDefault().getPreferenceStore().getBoolean(
                CathyPlugin.CHECK_UPDATES_ON_STARTUP)) {
            Job subJob = new CheckUpdatesJob(workbench, false);
            subJob.setProgressGroup(monitor, 1);
            subJob.schedule();
            try {
                subJob.join();
            } catch (InterruptedException e) {
            }
        } else {
            monitor.worked(1);
        }
    }

}
