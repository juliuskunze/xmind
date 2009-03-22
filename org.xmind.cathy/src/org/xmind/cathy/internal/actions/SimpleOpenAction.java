/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package org.xmind.cathy.internal.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.actions.NewFromTemplateFileAction;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.editor.WorkbookEditorInput;
import org.xmind.ui.internal.imports.freemind.FreeMindImporter;
import org.xmind.ui.internal.imports.mm.MindManagerImporter;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.MindMapImporter;

/**
 * @author briansun
 * 
 */
public class SimpleOpenAction implements Runnable {

    private IWorkbenchWindow window;

    private String filename;

    public SimpleOpenAction(IWorkbenchWindow window, String filename) {
        this.window = window;
        this.filename = filename;
    }

    public void run() {
        if (window != null && !window.getShell().isDisposed()) {
            open(window, filename);
        } else {
            open(filename);
        }
    }

    public static void open(String filename) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        open(window, filename);
    }

    public static void open(final IWorkbenchWindow window, String filename) {
        if (window == null)
            return;

        File file = new File(filename);
        if (!file.exists() || !file.isFile())
            return;

        final String path = filename;
        String extension = FileUtils.getExtension(path);

        if (MindMapUI.FILE_EXT_TEMPLATE.equalsIgnoreCase(extension)) {
            newFromTemplate(window, path);
        } else if (MindMapUI.FILE_EXT_MARKER_PACKAGE
                .equalsIgnoreCase(extension)) {
            importMarkers(path);
        } else if (".mmap".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            importMindManagerFile(window, path);
        } else if (".mm".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            importFreeMindFile(window, path);
        } else {
            // assumes we're opening xmind files
            openMindMap(window, path);
        }
    }

    private static void importMindManagerFile(final IWorkbenchWindow window,
            String path) {
        final MindMapImporter importer = new MindManagerImporter(path);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                importer.build();
            }
        });
        IWorkbook workbook = importer.getTargetWorkbook();
        if (workbook == null)
            return;

        final WorkbookEditorInput input = new WorkbookEditorInput(workbook);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }

    private static void importFreeMindFile(final IWorkbenchWindow window,
            String path) {
        final MindMapImporter importer = new FreeMindImporter(path);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                importer.build();
            }
        });
        IWorkbook workbook = importer.getTargetWorkbook();
        if (workbook == null)
            return;

        final WorkbookEditorInput input = new WorkbookEditorInput(workbook);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }

    private static void importMarkers(final String path) {
        try {
            MarkerImpExpUtils.importMarkerPackage(path);
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    PreferencesUtil.createPreferenceDialogOn(null,
                            MarkerManagerPrefPage.ID, null, null).open();
                }
            });
        } catch (IOException e) {
        }
    }

    private static void newFromTemplate(final IWorkbenchWindow window,
            final String path) {
        new NewFromTemplateFileAction(window, path).run();
    }

    private static void openMindMap(final IWorkbenchWindow window,
            final String path) {
        String errMessage = NLS.bind(
                DialogMessages.FailedToLoadWorkbook_message, path);
        SafeRunner.run(new SafeRunnable(errMessage) {
            public void run() throws Exception {
//                IWorkbook contents = Core.getWorkbookBuilder().loadFromPath(
//                        path);
//                WorkbookEditorInput input = new WorkbookEditorInput(contents,
//                        path);
                IEditorInput input = MME.createFileEditorInput(path);
                window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }
}