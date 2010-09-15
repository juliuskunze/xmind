package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.Core;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.IStorage;

public class FileStoreWorkbookAdapter implements IWorkbookLoader,
        IWorkbookSaver {

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
                IWorkbook workbook = Core.getWorkbookBuilder().loadFromFile(
                        file, storage, encryptionHandler);
                workbook.setFile(file.getAbsolutePath());
                return workbook;
            }
            throw new FileNotFoundException(file.getAbsolutePath());
        } else {
            InputStream input = fileStore.openInputStream(0, monitor);
            IWorkbook workbook = Core.getWorkbookBuilder().loadFromStream(
                    input, storage, encryptionHandler);
            return workbook;
        }
    }

    private static IFileStore createTempFile(IFileStore fileStore) {
        IFileStore parent = fileStore.getParent();
        return (parent != null) ? createTempFile(fileStore, parent) : null;
    }

    private static IFileStore createTempFile(IFileStore fileStore,
            IFileStore parent) {
        IFileInfo info = fileStore.fetchInfo();
        if (!info.exists())
            return null;
        String name = info.getName();
        int i = 1;
        String newName = name + "." + i + ".tmp"; //$NON-NLS-1$ //$NON-NLS-2$
        IFileStore newFile = parent.getChild(newName);
        while (newFile.fetchInfo().exists()) {
            i++;
            newName = name + "." + i + ".tmp"; //$NON-NLS-1$ //$NON-NLS-2$
            newFile = parent.getChild(newName);
        }
        return newFile;
    }

    public void save(IProgressMonitor monitor, final IWorkbook workbook)
            throws IOException, org.xmind.core.CoreException, CoreException {
        IFileStore tempFile = createTempFile(fileStore);
        if (tempFile != null) {
            tempFile.getParent().mkdir(0, monitor);
            OutputStream output = tempFile.openOutputStream(0, monitor);
            try {
                workbook.save(output);
            } finally {
                output.close();
            }
            fileStore.delete(0, monitor);
            tempFile.move(fileStore, EFS.OVERWRITE, monitor);
        } else {
            OutputStream output = fileStore.openOutputStream(0, monitor);
            try {
                workbook.save(output);
            } finally {
                output.close();
            }
        }
        workbook.setFile(fileStore.toLocalFile(0, monitor).getAbsolutePath());
    }

}
