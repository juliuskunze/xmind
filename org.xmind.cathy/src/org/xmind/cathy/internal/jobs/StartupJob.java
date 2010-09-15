package org.xmind.cathy.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;

public class StartupJob extends Job {

    private final IWorkbench workbench;

    public StartupJob(IWorkbench workbench) {
        super(WorkbenchMessages.StartupJob_jobName);
        this.workbench = workbench;
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 4);
        doStartup(monitor);
        System.setProperty("org.xmind.ui.WorkbenchReady", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        monitor.done();
        return Status.OK_STATUS;
    }

    protected void doStartup(IProgressMonitor monitor) {
        openStartupMap(monitor);
        checkAndRecoverFiles(monitor);
        checkAndOpenFiles(monitor);
        openBootstrapEditor(monitor);
    }

    protected void openStartupMap(IProgressMonitor monitor) {
        int startupAction = CathyPlugin.getDefault().getPreferenceStore()
                .getInt(CathyPlugin.STARTUP_ACTION);
        if (startupAction == CathyPlugin.STARTUP_ACTION_HOME) {
            monitor.subTask(WorkbenchMessages.StartupJob_OpenHomeMap);
            final String location = MindMapUIPlugin.getDefault()
                    .getPreferenceStore().getString(
                            PrefConstants.HOME_MAP_LOCATION);
            if (location == null || "".equals(location)) { //$NON-NLS-1$
                openBlankMap();
            } else {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        openEditor(MME.createFileEditorInput(location));
                    }
                });
            }
        } else if (startupAction == CathyPlugin.STARTUP_ACTION_BLANK) {
            monitor.subTask(WorkbenchMessages.StartupJob_OpenBlankMap);
            openBlankMap();
        } else {
            monitor.subTask(WorkbenchMessages.StartupJob_OpenLastSession);
        }
        monitor.worked(1);
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
                        IEditorReference[] editors = page.getEditorReferences();
                        if (editors == null || editors.length == 0) {
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

    private void openBlankMap() {
        openEditor(MME.createNonExistingEditorInput());
    }

    private void openEditor(final IEditorInput editorInput) {
        workbench.getDisplay().syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window != null) {
                    final IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        SafeRunner.run(new SafeRunnable() {
                            public void run() throws Exception {
                                page.openEditor(editorInput,
                                        MindMapUI.MINDMAP_EDITOR_ID);
                            }
                        });
                    }
                }
            }
        });
    }

}
