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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.Core;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.io.MonitoredInputStream;
import org.xmind.ui.io.MonitoredOutputStream;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class FileStoreWorkbookAdapter implements IWorkbookLoader,
        IWorkbookSaver, IWorkbookBackupMaker {

    private static final String SUBDIR_WORKBOOK_BACKUP = "WorkbookBackups"; //$NON-NLS-1$

    private static class FileBackup {
        long lastTimestamp;
        File tempLocation;
    }

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
            if (!file.isFile() || !file.canRead())
                throw new FileNotFoundException(file.getAbsolutePath());
            IWorkbook workbook = Core.getWorkbookBuilder().loadFromFile(file,
                    storage, encryptionHandler);
            workbook.setFile(file.getAbsolutePath());
            ((WorkbookImpl) workbook).setSkipRevisionsWhenSaving(file.getName()
                    .endsWith(MindMapUI.FILE_EXT_TEMPLATE));
            return workbook;
        } else {
            InputStream input = fileStore.openInputStream(0, monitor);
            IWorkbook workbook = Core.getWorkbookBuilder().loadFromStream(
                    input, storage, encryptionHandler);
            return workbook;
        }
    }

    public void save(IProgressMonitor monitor, final IWorkbook workbook)
            throws IOException, org.xmind.core.CoreException, CoreException {
        OutputStream output = fileStore.openOutputStream(0, monitor);
        workbook.save(output);
        workbook.setFile(fileStore.toLocalFile(0, monitor).getAbsolutePath());
    }

    public boolean willOverwriteTarget() {
        return fileStore.fetchInfo().exists();
    }

    public Object backup(IProgressMonitor monitor, Object previousBackup) {
        long timestamp = fileStore.fetchInfo().getLastModified();
        if (previousBackup != null && (previousBackup instanceof FileBackup)
                && ((FileBackup) previousBackup).lastTimestamp >= timestamp) {
            return previousBackup;
        }

        File tempLocation = (previousBackup != null && previousBackup instanceof FileBackup) ? ((FileBackup) previousBackup).tempLocation
                : null;
        if (tempLocation == null) {
            tempLocation = new File(Core.getWorkspace().getTempFile(
                    SUBDIR_WORKBOOK_BACKUP + File.separator
                            + Core.getIdFactory().createId()
                            + FileUtils.getExtension(fileStore.getName())));
        }
        if (tempLocation.getParentFile() != null)
            tempLocation.getParentFile().mkdirs();

        try {
            InputStream input = new MonitoredInputStream(
                    fileStore.openInputStream(0, monitor), monitor);
            try {
                OutputStream output = new MonitoredOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                tempLocation), 4096), monitor);
                try {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = input.read(buffer)) >= 0) {
                        output.write(buffer, 0, read);
                    }

                    FileBackup backup = new FileBackup();
                    backup.lastTimestamp = timestamp;
                    backup.tempLocation = tempLocation;
                    return backup;
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
            Logger.log(e);
            return previousBackup;
        }
    }

    public void restore(IProgressMonitor monitor, Object backup) {
        if (backup == null || !(backup instanceof FileBackup))
            return;
        File tempLocation = ((FileBackup) backup).tempLocation;
        try {
            InputStream input = new MonitoredInputStream(
                    new BufferedInputStream(new FileInputStream(tempLocation),
                            4096), monitor);
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
                    "Failed to resotre backup: " + tempLocation.getAbsolutePath()); //$NON-NLS-1$
        }
    }

}
