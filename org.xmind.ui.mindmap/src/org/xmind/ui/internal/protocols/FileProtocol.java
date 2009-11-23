package org.xmind.ui.internal.protocols;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IProtocol;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class FileProtocol implements IProtocol {

    private static class OpenFileAction extends Action {

        private IWorkbenchWindow window;

        private String path;

        public OpenFileAction(IWorkbenchWindow window, String path) {
            this.window = window;
            this.path = path;
        }

        public void run() {
            open(path);
        }

        private void open(String path) {
            if (!new File(path).exists()) {
                MessageDialog
                        .openInformation(
                                window.getShell(),
                                DialogMessages.InfoFileNotExists_title,
                                NLS
                                        .bind(
                                                DialogMessages.InfoFileNotExists_message,
                                                path));
                return;
            }
            String extension = FileUtils.getExtension(path);
            if (MindMapUI.FILE_EXT_TEMPLATE.equalsIgnoreCase(extension)) {
                if (window != null) {
                    if (openTemplate(window, path))
                        return;
                }
            } else if (MindMapUI.FILE_EXT_XMIND.equalsIgnoreCase(extension)) {
                if (window != null) {
                    if (openMindMap(window, path))
                        return;
                }
            } else if (MindMapUI.FILE_EXT_MARKER_PACKAGE
                    .equalsIgnoreCase(extension)) {
                if (importMarkers(path))
                    return;
            }

            try {
                Program.launch(new File(path).getCanonicalPath());
            } catch (Exception e) {
                Program.launch(path);
            }
        }

        private boolean openTemplate(IWorkbenchWindow window, String path) {
            return openMindMap(window, path);
        }

        private boolean openMindMap(final IWorkbenchWindow window,
                final String path) {
            String errMessage = NLS.bind(
                    DialogMessages.FailedToLoadWorkbook_message, path);
            final boolean[] ret = new boolean[1];
            SafeRunner.run(new SafeRunnable(errMessage) {
                public void run() throws Exception {
                    IEditorInput input = MME.createFileEditorInput(path);
                    window.getActivePage().openEditor(input,
                            MindMapUI.MINDMAP_EDITOR_ID);
                    ret[0] = true;
                }
            });
            return ret[0];
        }

        /**
         * @param path
         */
        private boolean importMarkers(String path) {
            try {
                MarkerImpExpUtils.importMarkerPackage(path);
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        PreferencesUtil.createPreferenceDialogOn(null,
                                MarkerManagerPrefPage.ID, null, null).open();
                    }
                });
                return true;
            } catch (IOException e) {
            }
            return false;
        }

    }

    public FileProtocol() {
    }

    public IAction createOpenHyperlinkAction(Object context, final String uri) {
        IWorkbenchWindow window = getWindow(context);
        String path = FilePathParser.toPath(uri);
        String absolutePath = getAbsolutePath(context, path);
        File file = new File(absolutePath);
        ImageDescriptor image = MindMapUI.getImages().getFileIcon(absolutePath,
                true);
        if (image == null) {
            if (file.isDirectory()) {
                image = MindMapUI.getImages().get(IMindMapImages.OPEN, true);
            } else {
                image = MindMapUI.getImages().get(IMindMapImages.UNKNOWN_FILE,
                        true);
            }
        }
        String text;
        if (file.isDirectory()) {
            text = MindMapMessages.FileProtocol_OpenFolder_text;
        } else {
            text = MindMapMessages.FileProtocol_OpenFile_text;
        }
        OpenFileAction action = new OpenFileAction(window, absolutePath);
        action.setText(text);
        action.setImageDescriptor(image);
        action.setToolTipText(absolutePath);
        return action;
    }

    public static String getAbsolutePath(Object context, String path) {
        if (FilePathParser.isPathRelative(path)) {
            IWorkbook workbook = MindMapUtils.findWorkbook(context);
            if (workbook != null) {
                String base = workbook.getFile();
                if (base != null) {
                    base = new File(base).getParent();
                    if (base != null) {
                        return FilePathParser.toAbsolutePath(base, path);
                    }
                }
            }
            return FilePathParser.toAbsolutePath(System
                    .getProperty("user.home"), path); //$NON-NLS-1$
        }
        return path;
    }

    private static IWorkbenchWindow getWindow(Object context) {
        if (context instanceof IAdaptable) {
            Object adapter = ((IAdaptable) context)
                    .getAdapter(IWorkbenchWindow.class);
            if (adapter == null) {
                adapter = ((IAdaptable) context).getAdapter(IEditorPart.class);
                if (adapter == null) {
                    adapter = ((IAdaptable) context)
                            .getAdapter(IWorkbenchPart.class);
                }
                if (adapter instanceof IWorkbenchPart)
                    adapter = ((IWorkbenchPart) adapter).getSite()
                            .getWorkbenchWindow();
            }
            if (adapter instanceof IWorkbenchWindow)
                return (IWorkbenchWindow) adapter;
        }
        if (context instanceof IWorkbenchWindow)
            return (IWorkbenchWindow) context;
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }

    public boolean isHyperlinkModifiable(Object source, String uri) {
        return true;
    }
}
