package org.xmind.cathy.internal;

import java.util.Collections;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.jobs.DownloadAndOpenFileJob;
import org.xmind.cathy.internal.jobs.OpenFilesJob;
import org.xmind.ui.comm.IXMindCommand;
import org.xmind.ui.comm.IXMindCommandHandler;

public class OpenFileCommandHandler implements IXMindCommandHandler {

    public boolean handleXMindCommand(IXMindCommand command, String... args) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return false;

        String file = command.getArgument("file"); //$NON-NLS-1$
        if (file != null)
            return openFile(workbench, file);

        String url = command.getArgument("url"); //$NON-NLS-1$
        if (url != null)
            return openURL(workbench, url, command.getArgument("name")); //$NON-NLS-1$

        return false;
    }

    private boolean openFile(IWorkbench workbench, String filePath) {
        CathyPlugin.log("Opening file through 'xmind:' protocol: " + filePath); //$NON-NLS-1$
        new OpenFilesJob(workbench,
                WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name,
                Collections.singletonList(filePath)).schedule();
        return true;
    }

    private boolean openURL(IWorkbench workbench, String url, String name) {
        CathyPlugin.log("Opening URL through 'xmind:' protocol: " + url); //$NON-NLS-1$
        new DownloadAndOpenFileJob(workbench, url, name).schedule();
        return false;
    }

}
