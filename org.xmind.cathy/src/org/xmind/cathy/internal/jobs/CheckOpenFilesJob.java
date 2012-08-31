package org.xmind.cathy.internal.jobs;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkbench;
import org.xmind.cathy.internal.Log;
import org.xmind.cathy.internal.WorkbenchMessages;

public class CheckOpenFilesJob extends OpenFilesJob {

    public CheckOpenFilesJob(IWorkbench workbench) {
        super(workbench, WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.cathy.internal.jobs.OpenFilesJob#filterFilesToOpen(java.util
     * .List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void filterFilesToOpen(List<String> filesToOpen,
            IProgressMonitor monitor) {
        monitor.beginTask(WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name,
                1);
        Log opening = Log.get(Log.OPENING);
        if (opening.exists()) {
            String[] contents = opening.getContents();
            for (String line : contents) {
                if (!line.startsWith("-")) { //$NON-NLS-1$
                    filesToOpen.add(line);
                }
            }
            opening.delete();
        }
        monitor.done();
    }

}
