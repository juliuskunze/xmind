package org.xmind.ui.internal.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MindMapUIPlugin;

public class PreLoadedWorkbookLoader implements IWorkbookLoader {

    private IWorkbook workbook;

    public PreLoadedWorkbookLoader(IWorkbook workbook) {
        this.workbook = workbook;
    }

    public IWorkbook loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
            throws CoreException {
        IStorage oldStorage = workbook.getTempStorage();

        if (oldStorage != null) {
            try {
                FileUtils.transfer(oldStorage, storage);
            } catch (Throwable e) {
                throw new CoreException(new Status(IStatus.ERROR,
                        MindMapUIPlugin.PLUGIN_ID, null, e));
            }
        }
        workbook.setTempStorage(storage);
        return workbook;
    }

}
