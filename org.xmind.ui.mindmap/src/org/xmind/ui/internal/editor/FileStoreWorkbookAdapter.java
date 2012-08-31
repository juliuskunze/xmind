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
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.IStorage;
import org.xmind.ui.mindmap.MindMapUI;

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
            try {
                //checkFileValidity(monitor, workbook, tempFile);
                fileStore.delete(0, monitor);
                tempFile.move(fileStore, EFS.OVERWRITE, monitor);
            } finally {
                try {
                    if (tempFile.fetchInfo().exists()) {
                        tempFile.delete(0, monitor);
                    }
                } catch (Exception e) {
                    //ignore
                }
            }
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

//    /**
//     * Check if the file saved as the same as the workbook to be saved.
//     * 
//     * @param monitor
//     * @param workbook
//     * @param tempFile
//     */
//    private void checkFileValidity(IProgressMonitor monitor,
//            final IWorkbook workbook, IFileStore tempFile)
//            throws CoreException, org.xmind.core.CoreException, IOException {
//        InputStream input = tempFile.openInputStream(0, monitor);
//        try {
//            IStorage tempStorage = WorkbookRef.createStorage();
//            IWorkbook workbook2 = Core.getWorkbookBuilder().loadFromStream(
//                    input, tempStorage, new IEncryptionHandler() {
//
//                        String password = workbook.getPassword();
//
//                        public String retrievePassword()
//                                throws org.xmind.core.CoreException {
//                            return password;
//                        }
//
//                    });
//            checkValidity(ArchiveConstants.CONTENT_XML, workbook, workbook2);
//            checkValidity(ArchiveConstants.META_XML, workbook.getMeta(),
//                    workbook2.getMeta());
//            checkValidity(ArchiveConstants.MANIFEST_XML,
//                    workbook.getManifest(), workbook2.getManifest());
//            checkValidity(ArchiveConstants.MARKER_SHEET_XML,
//                    workbook.getMarkerSheet(), workbook2.getMarkerSheet());
//            checkValidity(ArchiveConstants.STYLES_XML,
//                    workbook.getStyleSheet(), workbook2.getStyleSheet());
//            IInputSource source = tempStorage.getInputSource();
//            for (IFileEntry entry : workbook.getManifest().getFileEntries()) {
//                if (entry.hasBeenReferred()) {
//                    if (!source.hasEntry(entry.getPath())) {
//                        throw new CoreException(new Status(IStatus.ERROR,
//                                MindMapUIPlugin.PLUGIN_ID,
//                                MindMapMessages.WorkbookSavedIncorrectly_error
//                                        + entry.getPath()));
//                    }
//                }
//            }
//
//            tempStorage.clear();
//        } finally {
//            input.close();
//        }
//    }
//
//    private void checkValidity(String name, IAdaptable a1, IAdaptable a2)
//            throws CoreException {
//        if (!elementEquals(a1, a2))
//            throw new CoreException(new Status(IStatus.ERROR,
//                    MindMapUIPlugin.PLUGIN_ID,
//                    MindMapMessages.WorkbookSavedIncorrectly_error + name));
//    }
//
//    private boolean elementEquals(IAdaptable a1, IAdaptable a2) {
//        Node n1 = (Node) a1.getAdapter(Node.class);
//        if (n1 == null)
//            return false;
//        Node n2 = (Node) a2.getAdapter(Node.class);
//        if (n2 == null)
//            return false;
//        String s1 = toString(n1);
//        if (s1 == null)
//            return false;
//        String s2 = toString(n2);
//        if (s2 == null)
//            return false;
//        return s1.equals(s2);
//    }
//
//    private String toString(Node node) {
//        String string = null;
//        try {
//            ByteArrayOutputStream output = new ByteArrayOutputStream(1000);
//            try {
//                DOMUtils.save(node, output, true);
//                string = output.toString();
//            } finally {
//                output.close();
//            }
//        } catch (IOException e) {
//        } catch (org.xmind.core.CoreException e) {
//        }
//        return string;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.editor.IWorkbookSaver#isOverwriting()
     */
    public boolean canSaveToTarget() {
        return fileStore.fetchInfo().exists();
    }

}
