package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.Core;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.IStorage;

public class FileStoreWorkbookAdapter implements IWorkbookLoader, IWorkbookSaver {

    private IFileStore fileStore;

    public FileStoreWorkbookAdapter(IFileStore fileStore) {
        this.fileStore = fileStore;
    }

    public IFileStore getFileStore() {
        return fileStore;
    }

    public IWorkbook loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
            throws IOException, org.xmind.core.CoreException, CoreException {
        File file = fileStore.toLocalFile(0, monitor);
        if (file != null) {
            if (file.isFile()) {
                return Core.getWorkbookBuilder().loadFromFile(file, storage,
                        encryptionHandler);
            }
        } else {
            InputStream input = fileStore.openInputStream(0, monitor);
            return Core.getWorkbookBuilder().loadFromStream(input, storage,
                    encryptionHandler);
        }
        return null;
    }

    public void save(IProgressMonitor monitor, IWorkbook workbook)
            throws IOException, org.xmind.core.CoreException, CoreException {
        OutputStream output = fileStore.openOutputStream(0, monitor);
        workbook.save(output);
    }

}
