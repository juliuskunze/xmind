package org.xmind.ui.internal.sharing;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.sharing.ILocalSharedMap;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingService;
import org.xmind.ui.internal.editor.MME;

public class ShareLibraryMapHandler extends AbstractHandler implements IFilter {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final ISharingService sharingService = LocalNetworkSharingUI
                .getDefault().getSharingService();
        if (sharingService == null) {
            LocalNetworkSharingUI
                    .log("Failed to share opened file in local network: No sharing service available.", //$NON-NLS-1$
                            null);
            return null;
        }

        if (!PlatformUI.isWorkbenchRunning())
            return null;

        IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return null;

        final IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();

        final IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return null;

        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return null;

        List<ISharedMap> maps = SharingUtils.getSharedMapsFrom(
                (IStructuredSelection) selection, this);
        if (maps.isEmpty())
            return null;

        final ILocalSharedMap[] map = new ILocalSharedMap[1];
        for (ISharedMap m : maps) {
            if (m.getSharedLibrary().isLocal()) {
                map[0] = (ILocalSharedMap) m;
                break;
            }
        }

        if (map[0] == null)
            return null;

        display.syncExec(new Runnable() {
            public void run() {
                Shell parentShell = window == null ? display.getActiveShell()
                        : window.getShell();

                String mapPath = map[0].getResourcePath();
                if (!new File(mapPath).exists()) {
                    MessageDialog
                            .openWarning(
                                    parentShell,
                                    SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                    SharingMessages.ShareLibraryMapHandler_mapMissingTipText);
                    return;
                }

                IEditorPart editor = findDirtyEditor(page, mapPath);
                if (editor != null) {
                    MessageDialog
                            .openConfirm(
                                    Display.getCurrent().getActiveShell(),
                                    SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                    SharingMessages.ShareOpenedFileHandler_SaveTipMessageDialog_text);
                    return;
                }

                SharingUtils.updateSharedMaps(parentShell, sharingService,
                        map[0]);
            }
        });

        return null;
    }

    public boolean select(Object toTest) {
        ISharedMap map = (ISharedMap) toTest;
        return map.getSharedLibrary().isLocal();
    }

    private IEditorPart findDirtyEditor(IWorkbenchPage page, String targetPath) {
        IEditorPart[] dirtyEditors = page.getDirtyEditors();
        for (IEditorPart editor : dirtyEditors) {
            File file = MME.getFile(editor.getEditorInput());
            if (file != null && file.exists()) {
                if (file.getAbsolutePath().equals(targetPath))
                    return editor;
            }
        }
        return null;
    }

}
