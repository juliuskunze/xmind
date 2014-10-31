/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 *
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL),
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 *
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.Core;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.io.DirectoryOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.io.MonitoredInputStream;
import org.xmind.ui.io.MonitoredOutputStream;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class FileStoreWorkbookAdapter implements IWorkbookLoader,
        IWorkbookSaver, IWorkbookBackupFactory {

    private static final String SUBDIR_WORKBOOK_BACKUP = "WorkbookBackups"; //$NON-NLS-1$

    private static class FileStoreWorkbookBackup implements IWorkbookBackup {

        private IFileStore fileStore;

        private final File tempFile;

        private final long lastTimestamp;

        public FileStoreWorkbookBackup(IFileStore fileStore, File tempFile,
                long timestamp) {
            this.fileStore = fileStore;
            this.tempFile = tempFile;
            this.lastTimestamp = timestamp;
        }

        public void restore(IProgressMonitor monitor) {
            try {
                InputStream input = new MonitoredInputStream(
                        new FileInputStream(tempFile), monitor);
                try {
                    OutputStream output = new MonitoredOutputStream(
                            fileStore.openOutputStream(0, monitor), monitor);
                    try {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, read);
                        }
                    } finally {
                        try {
                            output.close();
                        } catch (IOException e) {
                        }
                    }
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                    }
                }

            } catch (Throwable e) {
                Logger.log(
                        e,
                        "Failed to resotre backup: " + tempFile.getAbsolutePath()); //$NON-NLS-1$
            }
        }

        public void dispose() {
            tempFile.delete();
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof FileStoreWorkbookBackup))
                return false;
            FileStoreWorkbookBackup that = (FileStoreWorkbookBackup) obj;
            return this.tempFile.equals(that.tempFile);
        }

    }

    private IFileStore fileStore;

    public FileStoreWorkbookAdapter(IFileStore fileStore) {
        this.fileStore = fileStore;
    }

    public IFileStore getFileStore() {
        return fileStore;
    }

    private String getFilePath() {
        URI uri = fileStore.toURI();
        if ("file".equals(uri.getScheme())) //$NON-NLS-1$
            return new File(uri).getAbsolutePath();
        return null;
    }

    public IWorkbook loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
            throws CoreException {
        IWorkbook workbook;
        IFileInfo fileInfo = fileStore.fetchInfo();
        if (fileInfo.isDirectory()) {
            DirectoryInputSource source = new DirectoryInputSource(new File(
                    fileStore.toURI()));
            try {
                workbook = Core.getWorkbookBuilder().loadFromInputSource(
                        source, storage, encryptionHandler);
            } catch (Throwable e) {
                throw new CoreException(new Status(IStatus.ERROR,
                        MindMapUIPlugin.PLUGIN_ID, e.getLocalizedMessage(), e));
            }
        } else {
            InputStream input = fileStore.openInputStream(EFS.NONE, monitor);
            try {
                workbook = Core.getWorkbookBuilder().loadFromStream(input,
                        storage, encryptionHandler);
            } catch (Throwable e) {
                throw new CoreException(new Status(IStatus.ERROR,
                        MindMapUIPlugin.PLUGIN_ID, e.getLocalizedMessage(), e));
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore input stream close exception.
                }
            }
        }
        workbook.setFile(getFilePath());
        if (workbook instanceof WorkbookImpl)
            ((WorkbookImpl) workbook).setSkipRevisionsWhenSaving(fileStore
                    .getName().toLowerCase()
                    .endsWith(MindMapUI.FILE_EXT_TEMPLATE));
        return workbook;
    }

    public void save(IProgressMonitor monitor, final IWorkbook workbook)
            throws CoreException {
        String oldFilePath = workbook.getFile();
        String newFilePath = getFilePath();

        OutputStream output = null;
        try {
            workbook.setFile(newFilePath);
            updateAllRelativeFileHyperlinks(workbook, oldFilePath, newFilePath);

            IFileInfo fileInfo = fileStore.fetchInfo();
            if (fileInfo.isDirectory()) {
                clearDir(fileStore);
                workbook.save(new DirectoryOutputTarget(new File(fileStore
                        .toURI())));
                IFileInfo newFileInfo = EFS.createFileInfo();
                newFileInfo.setLastModified(System.currentTimeMillis());
                fileStore.putInfo(newFileInfo, EFS.SET_LAST_MODIFIED, null);
            } else {
                output = fileStore.openOutputStream(0, monitor);
                workbook.save(output);
            }

        } catch (Throwable e) {
            workbook.setFile(oldFilePath);
            if (e instanceof CoreException)
                throw (CoreException) e;
            throw new CoreException(new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID, null, e));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean willOverwriteTarget() {
        return fileStore.fetchInfo().exists();
    }

    public IWorkbookBackup createWorkbookBackup(IProgressMonitor monitor,
            IWorkbookBackup previousBackup) {
        IFileInfo fileInfo = fileStore.fetchInfo();
        if (!fileInfo.exists())
            return previousBackup;

        long timestamp = fileInfo.getLastModified();
        if (previousBackup != null
                && (previousBackup instanceof FileStoreWorkbookBackup)
                && ((FileStoreWorkbookBackup) previousBackup).lastTimestamp >= timestamp) {
            return previousBackup;
        }

        File tempFile = (previousBackup != null && previousBackup instanceof FileStoreWorkbookBackup) ? ((FileStoreWorkbookBackup) previousBackup).tempFile
                : null;
        if (tempFile == null) {
            tempFile = new File(Core.getWorkspace().getTempFile(
                    SUBDIR_WORKBOOK_BACKUP + File.separator
                            + Core.getIdFactory().createId()
                            + FileUtils.getExtension(fileStore.getName())));
        }
        if (tempFile.getParentFile() != null)
            tempFile.getParentFile().mkdirs();

        try {
            transferAll(fileStore, tempFile, monitor);
            return new FileStoreWorkbookBackup(fileStore, tempFile, timestamp);
        } catch (Throwable e) {
            Logger.log(e);
            return previousBackup;
        }
    }

    private static void transferAll(IFileStore source, File target,
            IProgressMonitor monitor) throws IOException, CoreException {
        IFileInfo sourceInfo = source.fetchInfo();
        if (sourceInfo.isDirectory()) {
            target.mkdirs();
            String[] childNames = source.childNames(EFS.NONE, monitor);
            for (int i = 0; i < childNames.length; i++) {
                String childName = childNames[i];
                transferAll(source.getChild(childName), new File(target,
                        childName), monitor);
            }
        } else {
            transferFile(source, target, monitor);
        }
    }

    private static void transferFile(IFileStore sourceFile, File targetFile,
            IProgressMonitor monitor) throws IOException, CoreException {
        InputStream input = new MonitoredInputStream(
                sourceFile.openInputStream(EFS.NONE, monitor), monitor);
        try {
            OutputStream output = new MonitoredOutputStream(
                    new FileOutputStream(targetFile), monitor);
            try {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, read);
                }
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

    private void updateAllRelativeFileHyperlinks(IWorkbook workbook,
            String oldFilePath, String newFilePath) {
        String oldBase;
        if (oldFilePath == null) {
            oldBase = FilePathParser.ABSTRACT_FILE_BASE;
        } else {
            oldBase = new File(oldFilePath).getParent();
        }

        String newBase = new File(newFilePath).getParent();
        if (oldBase.equals(newBase))
            return;

        List<ISheet> sheets = workbook.getSheets();
        for (ISheet sheet : sheets) {
            updateRelativeFileHyperlinks(sheet.getRootTopic(), oldBase, newBase);
        }
    }

    private void updateRelativeFileHyperlinks(ITopic topic, String oldBase,
            String newBase) {
        String hyperlink = topic.getHyperlink();
        if (FilePathParser.isFileURI(hyperlink)) {
            String path = FilePathParser.toPath(hyperlink);
            if (FilePathParser.isPathRelative(path)) {
                String absolutePath = FilePathParser.toAbsolutePath(oldBase,
                        path);
                hyperlink = FilePathParser
                        .toRelativePath(newBase, absolutePath);
                topic.setHyperlink(FilePathParser.toURI(hyperlink, true));
            }
        }
        List<ITopic> topics = topic.getAllChildren();
        if (topics != null) {
            for (ITopic temptopic : topics) {
                updateRelativeFileHyperlinks(temptopic, oldBase, newBase);
            }
        }
    }

    private void clearDir(IFileStore dir) {
        try {
            IFileStore[] children = dir.childStores(EFS.NONE, null);
            for (int i = 0; i < children.length; i++) {
                IFileStore child = children[i];
                IFileInfo childInfo = child.fetchInfo();
                if (childInfo.isDirectory()) {
                    clearDir(child);
                }
                child.delete(EFS.NONE, null);
            }
        } catch (CoreException e) {
            // Ignore directory clearing exception.
        }
    }

}
