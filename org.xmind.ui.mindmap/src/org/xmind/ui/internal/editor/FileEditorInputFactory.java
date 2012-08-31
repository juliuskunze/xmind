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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.xmind.ui.util.Logger;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class FileEditorInputFactory implements IElementFactory {

    private static final String ID = "org.xmind.ui.WorkbookEditorInputFactory"; //$NON-NLS-1$

    private static final String TAG_PATH = "path"; //$NON-NLS-1$

    /**
     * For backward compatability with 3.0.0/1
     */
    private static final String TAG_RESOURCE_PATH = "resourcePath"; //$NON-NLS-1$

    public IAdaptable createElement(IMemento memento) {
        String path = memento.getString(TAG_PATH);
        if (path != null) {
            try {
                return MME.createFileEditorInput(path);
            } catch (CoreException e) {
                Logger.log(e);
                return null;
            }
        }

        // For backward compatability
        path = memento.getString(TAG_RESOURCE_PATH);
        if (path != null) {
            try {
                return createResourceFileEditorInput(path);
            } catch (CoreException e) {
                Logger.log(e);
            }
        }
        return null;
    }

    /**
     * The 'resource path' is stored by XMind 3.0.0/1 for the sake of
     * representing an IFile object and create an old WorkbookEditorInput. Now
     * we take into account the creation of IFile and its corresponding editor
     * input ONLY when org.eclipse.ui.ide plugin exists in the runtime
     * environment.
     * 
     * @throws CoreException
     */
    private IEditorInput createResourceFileEditorInput(String resourcePath)
            throws CoreException {
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
                new Path(resourcePath));
        return MME.createFileEditorInput(file);
    }

    public static String getFactoryId() {
        return ID;
    }

    /**
     * Save the file editor input to the memeneto. Only the 'path' attribute
     * will be saved.
     * 
     * @param memento
     * @param input
     */
    static void saveState(IMemento memento, FileEditorInput input) {
        File file = input.getFile();
        memento.putString(TAG_PATH, file.getAbsolutePath());
    }

//    private static final String ID_FACTORY = "org.xmind.ui.WorkbookEditorInputFactory"; //$NON-NLS-1$
//
//    private static final String TAG_ABSOLUTE_PATH = "path"; //$NON-NLS-1$
//
//    private static final String TAG_RESOURCE_PATH = "resourcePath"; //$NON-NLS-1$

//    public IAdaptable createElement(IMemento memento) {
//        String resPath = memento.getString(TAG_RESOURCE_PATH);
//        if (resPath != null) {
//            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
//                    new Path(resPath));
//            if (file != null) {
//                try {
//                    IWorkbook workbook = Core.getWorkbookBuilder()
//                            .loadFromStream(
//                                    file.getContents(),
//                                    MindMapUI.getWorkbookRefManager()
//                                            .createTempLocation());
//                    return new WorkbookEditorInput(workbook, file);
//                } catch (Exception e) {
//                    Logger.log(e);
//                }
//            }
//        }
//
//        String path = memento.getString(TAG_ABSOLUTE_PATH);
//        if (path != null) {
//            File file = new File(path);
//            if (file.exists() && file.canRead()) {
//                try {
//                    IWorkbook workbook = Core.getWorkbookBuilder()
//                            .loadFromFile(file);
//                    return new WorkbookEditorInput(workbook, path);
//                } catch (Throwable e) {
//                    Logger.log(e);
//                }
//            }
//        }
//
//        return null;
//    }
//
//    public static String getFactoryId() {
//        return ID_FACTORY;
//    }
//
//    public static void saveState(IMemento memento, WorkbookEditorInput input) {
//        String path = input.getFile();
//        if (path != null) {
//            memento.putString(TAG_ABSOLUTE_PATH, path);
//        }
//        IFile resFile = input.getResourceFile();
//        if (resFile != null) {
//            memento.putString(TAG_RESOURCE_PATH, resFile.getFullPath()
//                    .toString());
//        }
//
//    }

}