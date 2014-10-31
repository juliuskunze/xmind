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

import java.beans.PropertyChangeListener;
import java.io.File;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.core.sharing.ILocalSharedLibrary;
import org.xmind.core.sharing.ILocalSharedMap;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingListener;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.sharing.SharingEvent;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.editor.MME;

public class MindMapSharingStatusItem extends ContributionItem implements
        IPropertyListener, ISharingListener, Listener, PropertyChangeListener {

    private class OpenLocalNetworkSharingViewAction extends Action {

        public OpenLocalNetworkSharingViewAction(String text) {
            super(text, LocalNetworkSharingUI.imageDescriptorFromPlugin(
                    LocalNetworkSharingUI.PLUGIN_ID, "icons/localnetwork.gif")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window == null)
                return;

            final IWorkbenchPage page = window.getActivePage();
            if (page == null)
                return;

            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    page.showView(LocalNetworkSharingUI.VIEW_ID);
                }
            });
        }

    }

    private class DeleteSharedMapsAction extends Action {

        private final ISharedMap localMap;

        public DeleteSharedMapsAction(String text, ISharedMap localMap) {
            super(text, LocalNetworkSharingUI.imageDescriptorFromPlugin(
                    LocalNetworkSharingUI.PLUGIN_ID, "icons/delete.gif")); //$NON-NLS-1$
            this.localMap = localMap;
        }

        @Override
        public void run() {
            if (sharingService == null)
                return;

            final ILocalSharedLibrary library = sharingService
                    .getLocalLibrary();
            if (!library.hasMaps())
                return;

            if (localMap != null) {
                if (!MessageDialog
                        .openConfirm(
                                Display.getCurrent().getActiveShell(),
                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                NLS.bind(
                                        SharingMessages.ConfirmDeleteSingleSharedMap_dialogMessage,
                                        localMap.getResourceName())))
                    return;
            }

            BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                public void run() {
                    library.removeSharedMap(localMap);
                }
            });
        }
    }

    private static final String SHARE_COMMAND_ID = "org.xmind.ui.command.sharing.localnetwork.shareOpenedMap"; //$NON-NLS-1$

    private IGraphicalEditor editor;

    private ISharingService sharingService;

    private ToolItem item = null;

    private ToolItem separator = null;

    public MindMapSharingStatusItem(IGraphicalEditor editor,
            ISharingService sharingService) {
        super("org.xmind.ui.sharing.MindMapEditorSharingStatus"); //$NON-NLS-1$
        this.editor = editor;
        this.sharingService = sharingService;
        editor.addPropertyListener(this);
        sharingService.addSharingListener(this);
        LocalNetworkSharingUI
                .getDefault()
                .getServiceStatusSupport()
                .addPropertyChangeListener(
                        LocalNetworkSharingUI.PREF_FEATURE_ENABLED, this);
        setVisible(LocalNetworkSharingUI.getDefault().isLNSServiceAvailable());
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void dispose() {
        if (separator != null) {
            separator.dispose();
            separator = null;
        }
        if (item != null) {
            item.dispose();
            item = null;
        }
        LocalNetworkSharingUI
                .getDefault()
                .getServiceStatusSupport()
                .removePropertyChangeListener(
                        LocalNetworkSharingUI.PREF_FEATURE_ENABLED, this);
        if (sharingService != null) {
            sharingService.removeSharingListener(this);
            sharingService = null;
        }
        if (editor != null) {
            editor.removePropertyListener(this);
            editor = null;
        }
        super.dispose();
    }

    @Override
    public void fill(ToolBar parent, int index) {
        if (sharingService == null)
            return;

        if (index < 0) {
            item = new ToolItem(parent, SWT.PUSH);
        } else {
            item = new ToolItem(parent, SWT.PUSH, index++);
        }
        item.addListener(SWT.Selection, this);

        update(null);
    }

    @Override
    public void update(String id) {
        if (item != null && !item.isDisposed()) {
            if (editor != null && sharingService != null) {
                IEditorInput input = editor.getEditorInput();
                if (isSharedByLocalUser(input)) {
                    item.setText(NLS
                            .bind(SharingMessages.MindMapSharingStatusItem_Shared_buttonText_withLocalUserName,
                                    sharingService.getLocalLibrary().getName()));
                } else if (isSharedByRemoteUser(input)) {
                    item.setText(NLS
                            .bind(SharingMessages.MindMapSharingStatusItem_Shared_buttonText_withRemoteUserName,
                                    ((SharedWorkbookEditorInput) input)
                                            .getSourceMap().getSharedLibrary()
                                            .getName()));
                } else {
                    item.setText(SharingMessages.MindMapSharingStatusItem_ToShare_buttonText);
                }
            }
        }
    }

    private boolean isSharedByLocalUser(IEditorInput input) {
        File file = MME.getFile(input);
        return file != null && findSharedMapByLocalFile(file) != null;
    }

    private ISharedMap findSharedMapByLocalFile(File file) {
        ISharedMap[] localMaps = sharingService.getLocalLibrary().getMaps();
        for (int i = 0; i < localMaps.length; i++) {
            ILocalSharedMap localMap = (ILocalSharedMap) localMaps[i];
            if (new File(localMap.getResourcePath()).equals(file)) {
                return localMap;
            }
        }
        return null;
    }

    private boolean isSharedByRemoteUser(IEditorInput input) {
        return input instanceof SharedWorkbookEditorInput;
    }

    public void propertyChanged(Object source, int propId) {
        if (propId == IEditorPart.PROP_INPUT) {
            refresh();
        }
    }

    public void handleSharingEvent(SharingEvent event) {
        if (event.getType() == SharingEvent.Type.SHARED_MAP_ADDED
                || event.getType() == SharingEvent.Type.SHARED_MAP_REMOVED
                || event.getType() == SharingEvent.Type.SHARED_MAP_UPDATED) {
            refresh();
        }
    }

    private void refresh() {
        if (editor == null)
            return;

        Display display = editor.getSite().getWorkbenchWindow().getWorkbench()
                .getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.asyncExec(new Runnable() {
            public void run() {
                if (editor == null || sharingService == null)
                    return;

                update(null);
                setVisible(LocalNetworkSharingUI.getDefault()
                        .isLNSServiceAvailable());
                getParent().update(true);
            }
        });
    }

    public void handleEvent(Event event) {
        if (event.type == SWT.Selection) {
            handleWidgetSelection(event);
        }
    }

    private void handleWidgetSelection(Event event) {
        if (editor == null || sharingService == null)
            return;

        IEditorInput input = editor.getEditorInput();
        File file = MME.getFile(input);
        ISharedMap localMap = file == null ? null
                : findSharedMapByLocalFile(file);
        if (localMap != null) {
            showPopupMenu(localMap);
        } else if (isSharedByRemoteUser(input)) {
            revealSharedMap(((SharedWorkbookEditorInput) input).getSourceMap());
        } else {
            executeShareCommand(event);
        }
    }

    private void showPopupMenu(ISharedMap localMap) {
        MenuManager popupMenu = new MenuManager();
        popupMenu
                .add(new OpenLocalNetworkSharingViewAction(
                        SharingMessages.SharingServiceStatusItem_ShowLocalNetworkSharingViewAction_text));

        popupMenu.add(new Separator());

        popupMenu
                .add(new DeleteSharedMapsAction(
                        SharingMessages.MindMapSharingStatusItem_Shared_popupMenuItem_stopSharingLabel,
                        localMap));

        Rectangle itemBounds = item.getBounds();
        Menu menuWidget = popupMenu.createContextMenu(item.getParent());
        menuWidget.setLocation(item.getParent().toDisplay(itemBounds.x,
                itemBounds.y));
        menuWidget.setVisible(true);
    }

    private void revealSharedMap(ISharedMap map) {
        final IWorkbenchPage page = editor.getSite().getPage();
        if (page != null) {
            final IViewPart[] view = new IViewPart[] { null };
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    view[0] = page.showView(LocalNetworkSharingUI.VIEW_ID);
                }
            });
            if (view[0] != null) {
                ISelectionProvider selectionProvider = view[0].getSite()
                        .getSelectionProvider();
                if (selectionProvider != null) {
                    selectionProvider
                            .setSelection(new StructuredSelection(map));
                }
            }
        }
    }

    private void executeShareCommand(final Event event) {
        final IHandlerService handlerService = (IHandlerService) editor
                .getSite().getService(IHandlerService.class);
        if (handlerService == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                handlerService.executeCommand(SHARE_COMMAND_ID, event);
            }
        });
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (LocalNetworkSharingUI.PREF_FEATURE_ENABLED.equals(evt
                .getPropertyName())) {
            refresh();
        }
    }
}
