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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.Bundle;
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

    private boolean presentation;

    private IEditorPart resultEditor;

    public SimpleOpenAction(IWorkbenchWindow window, String filename,
            boolean presentation) {
        this.window = window;
        this.filename = filename;
        this.presentation = presentation;
    }

    public SimpleOpenAction(IWorkbenchWindow window, String filename) {
        this(window, filename, false);
    }

    public SimpleOpenAction(String filename) {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), filename,
                false);
    }

    public SimpleOpenAction(String filename, boolean presentataion) {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), filename,
                presentataion);
    }

    public void run() {
        if (window != null && !window.getShell().isDisposed()) {
            resultEditor = open(window, filename);
        } else {
            resultEditor = open(filename);
        }
        if (resultEditor != null && presentation) {
            startPresentation(resultEditor);
        }
    }

    private void startPresentation(IEditorPart sourceEditor) {
        final IEditorActionDelegate ad = createPresentationDelegate();
        if (ad == null)
            return;

        final IAction action = new Action() {
        };
        ad.setActiveEditor(action, sourceEditor);
        sourceEditor.getSite().getWorkbenchWindow().getWorkbench().getDisplay()
                .asyncExec(new Runnable() {
                    public void run() {
                        ad.run(action);
                    }
                });
    }

    private IEditorActionDelegate createPresentationDelegate() {
        // TODO Auto-generated method stub
        String clazz = "org.xmind.ui.internal.presentation.ShowPresentationActionDelegate"; //$NON-NLS-1$
        Bundle bundle = Platform.getBundle("org.xmind.ui.presentation"); //$NON-NLS-1$
        if (bundle != null) {
            try {
                return (IEditorActionDelegate) bundle.loadClass(clazz)
                        .newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static IEditorPart open(String filename) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        return open(window, filename);
    }

    public IEditorPart getResultEditor() {
        return resultEditor;
    }

    public static IEditorPart open(final IWorkbenchWindow window,
            String filename) {
        if (window == null)
            return null;

        File file = new File(filename);
        if (!file.exists() || !file.isFile())
            return null;

        final String path = filename;
        String extension = FileUtils.getExtension(path);

        if (MindMapUI.FILE_EXT_TEMPLATE.equalsIgnoreCase(extension)) {
            return newFromTemplate(window, path);
        } else if (MindMapUI.FILE_EXT_MARKER_PACKAGE
                .equalsIgnoreCase(extension)) {
            return importMarkers(path);
        } else if (".mmap".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            return importMindManagerFile(window, path);
        } else if (".mm".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            return importFreeMindFile(window, path);
        } else {
            // assumes we're opening xmind files
            return openMindMap(window, path);
        }
    }

    private static IEditorPart importMindManagerFile(
            final IWorkbenchWindow window, String path) {
        final MindMapImporter importer = new MindManagerImporter(path);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                importer.build();
            }
        });
        IWorkbook workbook = importer.getTargetWorkbook();
        if (workbook == null)
            return null;

        final WorkbookEditorInput input = new WorkbookEditorInput(workbook);
        final IEditorPart[] e = new IEditorPart[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                e[0] = window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
        return e[0];
    }

    private static IEditorPart importFreeMindFile(
            final IWorkbenchWindow window, String path) {
        final MindMapImporter importer = new FreeMindImporter(path);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                importer.build();
            }
        });
        IWorkbook workbook = importer.getTargetWorkbook();
        if (workbook == null)
            return null;

        final WorkbookEditorInput input = new WorkbookEditorInput(workbook);
        final IEditorPart[] ediotPart = new IEditorPart[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                ediotPart[0] = window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
        return ediotPart[0];
    }

    private static IEditorPart importMarkers(final String path) {
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
        return null;
    }

    private static IEditorPart newFromTemplate(final IWorkbenchWindow window,
            final String path) {
        NewFromTemplateFileAction action = new NewFromTemplateFileAction(
                window, path);
        action.run();
        return action.getEditorPart();
    }

    private static IEditorPart openMindMap(final IWorkbenchWindow window,
            final String path) {
        String errMessage = NLS.bind(
                DialogMessages.FailedToLoadWorkbook_message, path);
        final IEditorPart[] editPart = new IEditorPart[1];
        SafeRunner.run(new SafeRunnable(errMessage) {
            public void run() throws Exception {
                IEditorInput input = MME.createFileEditorInput(path);
                editPart[0] = window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
        return editPart[0];
    }
}