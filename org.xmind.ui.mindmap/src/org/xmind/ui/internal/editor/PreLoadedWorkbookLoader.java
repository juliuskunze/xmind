package org.xmind.ui.internal.editor;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;

public class PreLoadedWorkbookLoader implements IWorkbookLoader {

    private IWorkbook workbook;

    public PreLoadedWorkbookLoader(IWorkbook workbook) {
        this.workbook = workbook;
    }

    public IWorkbook loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
            throws IOException, CoreException,
            org.eclipse.core.runtime.CoreException {
        IStorage oldStorage = workbook.getTempStorage();

        if (oldStorage != null) {
            FileUtils.transfer(oldStorage, storage);
        }
        workbook.setTempStorage(storage);
        return workbook;
    }

}
