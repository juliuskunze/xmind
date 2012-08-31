package org.xmind.cathy.internal.jobs;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.editor.WorkbookRefManager;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class CheckRecoverFilesJob extends AbstractCheckFilesJob {

    private class ListLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof IEditorInput)
                return ((IEditorInput) element).getName();
            return null;
        }

        public Image getImage(Object element) {
            if (element instanceof IEditorInput) {
                ImageDescriptor image = MindMapUI.getImages().get(
                        IMindMapImages.XMIND_ICON);
                if (image != null)
                    return image.createImage();
            }
            return null;
        }
    }

    private List<IEditorInput> loadedFiles;

    public CheckRecoverFilesJob(IWorkbench workbench) {
        super(workbench, WorkbenchMessages.CheckRecoverFilesJob_jobName);
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 3);
        checkAndRecoverFiles(monitor);
        monitor.done();
        return Status.OK_STATUS;
    }

    /**
     * Check and recover files opened in the last workbench session. This method
     * consumes 2 ticks from the given progress monitor.
     * 
     * @param monitor
     */
    protected void checkAndRecoverFiles(IProgressMonitor monitor) {
        loadFilesToRecover(new SubProgressMonitor(monitor, 1));

        if (loadedFiles != null && !loadedFiles.isEmpty()) {
            filterFiles(new SubProgressMonitor(monitor, 1));
            openEditors(monitor,
                    WorkbenchMessages.CheckRecoverFilesJob_RecoverFiles_name,
                    1, false);
        } else {
            monitor.worked(2);
        }

        WorkbookRefManager.getInstance().clearLastSession();
    }

    private void loadFilesToRecover(IProgressMonitor monitor) {
        monitor.beginTask(
                WorkbenchMessages.CheckRecoverFilesJob_LoadFiles_name, 1);
        loadedFiles = WorkbookRefManager.getInstance().loadLastSession();
        monitor.done();
    }

    private void filterFiles(IProgressMonitor monitor) {
        monitor.beginTask(
                WorkbenchMessages.CheckRecoverFilesJob_FilterFiles_name, 1);
        getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                ListSelectionDialog dialog = new ListSelectionDialog(null,
                        loadedFiles, new ArrayContentProvider(),
                        new ListLabelProvider(),
                        WorkbenchMessages.appWindow_ListSelectionDialog_Text);
                dialog.setTitle(WorkbenchMessages.appWindow_ListSelectionDialog_Title);
                dialog.setInitialElementSelections(loadedFiles);
                int ret = dialog.open();
                if (ret == ListSelectionDialog.CANCEL)
                    return;
                Object[] result = dialog.getResult();
                for (Object input : result) {
                    addEditorToOpen((IEditorInput) input);
                }
            }
        });
        monitor.done();
    }

}
