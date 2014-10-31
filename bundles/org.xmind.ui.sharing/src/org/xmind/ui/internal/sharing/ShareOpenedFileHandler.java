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
package org.xmind.ui.internal.sharing;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.sharing.ISharingService;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class ShareOpenedFileHandler extends AbstractHandler {

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

        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return null;

        display.syncExec(new Runnable() {

            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                final IWorkbenchPage page = window.getActivePage();
                if (page == null)
                    return;

                IEditorPart editor = page.getActiveEditor();
                if (editor == null
                        || !(MindMapUI.MINDMAP_EDITOR_ID.equals(editor
                                .getSite().getId())))
                    return;

                File file = MME.getFile(editor.getEditorInput());
                if (file == null || !file.exists()) {
                    if (editor instanceof ISaveablePart) {
                        if (!MessageDialog
                                .openConfirm(
                                        Display.getCurrent().getActiveShell(),
                                        SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                        SharingMessages.ShareOpenedFileHandler_SaveTipMessageDialog_text))
                            return;

                        ((ISaveablePart) editor).doSaveAs();
                        file = MME.getFile(editor.getEditorInput());
                        if (file == null || !file.exists())
                            // We don't show warning here because user must have
                            // canceled the Save As process.
                            return;
                    } else {
                        MessageDialog.openInformation(
                                window.getShell(),
                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                SharingMessages.OpenedEditorHasNoXMindFileToShare_dialogMessage);
                        return;
                    }
                }

//                final ILocalSharedLibrary library = sharingService
//                        .getLocalLibrary();
//                List<ISharedMap> oldMaps = Arrays.asList(library.getMaps());

//                final ISharedMap[] map = new ISharedMap[1];
//                final File fileToAdd = file;
//                BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
//                    public void run() {
//                        map[0] = library.addSharedMap(fileToAdd);
//                    }
//                });

                SharingUtils.addSharedMaps(display.getActiveShell(),
                        sharingService, new File[] { file });
//                if (map[0] == null)
//                    return;

//                final IViewPart[] view = new IViewPart[1];
//                SafeRunner.run(new SafeRunnable() {
//                    public void run() throws Exception {
//                        view[0] = page.showView(LocalNetworkSharingUI.VIEW_ID);
//                    }
//                });
//
//                if (view[0] != null) {
//                    ISelectionProvider selectionProvider = view[0].getSite()
//                            .getSelectionProvider();
//                    if (selectionProvider != null) {
//                        selectionProvider.setSelection(new StructuredSelection(
//                                map[0]));
//                    }

//                    SharedLibrariesViewer viewer = (SharedLibrariesViewer) view[0]
//                            .getAdapter(SharedLibrariesViewer.class);
//                    if (viewer != null) {
//                        viewer.setExpanded(library, true);
//                        viewer.setSelection(new StructuredSelection(map[0]),
//                                true);
//                    }
//                }

//                if (oldMaps.contains(map[0])) {
//                    MessageDialog.openInformation(window.getShell(), NLS.bind(
//                            "{0} - Local Network Sharing",
//                            IDialogConstants.COMMON_TITLE), NLS.bind(
//                            "File ''{0}'' has already been shared.",
//                            map[0].getResourceName()));
//                }
            }
        });

        return null;
    }

}
