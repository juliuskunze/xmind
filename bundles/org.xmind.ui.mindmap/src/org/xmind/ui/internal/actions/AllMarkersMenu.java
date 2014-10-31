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
package org.xmind.ui.internal.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.MindMapEditor;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class AllMarkersMenu extends ContributionItem implements
        IWorkbenchContribution {

    private class MarkerAction extends Action {

        private IMarker marker;

        public MarkerAction(IMarker marker) {
            this.marker = marker;
            setId("#" + marker.getId()); //$NON-NLS-1$
            setText(marker.getName());
            setImageDescriptor(MarkerImageDescriptor.createFromMarker(marker));
        }

        public void run() {
            if (window == null)
                return;
            IWorkbenchPage activePage = window.getActivePage();
            if (activePage == null)
                return;

            IEditorPart editor = activePage.getActiveEditor();
            if (!(editor instanceof IGraphicalEditor))
                return;

            IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                    .getActivePageInstance();

            if (page == null)
                return;

            EditDomain domain = page.getEditDomain();
            if (domain == null)
                return;

            domain.handleRequest(new Request(MindMapUI.REQ_ADD_MARKER)
                    .setViewer(page.getViewer()).setParameter(
                            MindMapUI.PARAM_MARKER_ID, marker.getId()));
        }
    }

    private IWorkbenchWindow window;

    private Listener toolItemListener;
    private ToolItem widget = null;
    private MenuManager dropDownMenuManager = null;

    protected boolean dirty = true;

    private IMenuListener menuListener = new IMenuListener() {
        public void menuAboutToShow(IMenuManager manager) {
            manager.markDirty();
            dirty = true;
        }
    };

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Overridden to always return true and force dynamic menu building.
     */
    public boolean isDynamic() {
        return true;
    }

    public AllMarkersMenu() {
    }

    @Override
    public void fill(ToolBar parent, int index) {
        ToolItem toolItem;
        if (index >= 0)
            toolItem = new ToolItem(parent, SWT.DROP_DOWN, index);
        else
            toolItem = new ToolItem(parent, SWT.DROP_DOWN);
        widget = toolItem;
//        toolItem.setText("Markers");
        toolItem.setImage(MindMapUI.getImages()
                .get(IMindMapImages.MARKER_SHORTCUT, true).createImage());
        toolItem.setDisabledImage(MindMapUI.getImages()
                .get(IMindMapImages.MARKER_SHORTCUT, false).createImage());
        toolItem.setToolTipText(MindMapMessages.AllMarkersMenu_Markers_tooltip);
        toolItem.addListener(SWT.Dispose, getToolItemListener());
        toolItem.addListener(SWT.Selection, getToolItemListener());
        toolItem.setEnabled(false);

    }

    private Listener getToolItemListener() {
        if (toolItemListener == null) {
            toolItemListener = new Listener() {
                public void handleEvent(Event event) {
                    switch (event.type) {
                    case SWT.Dispose:
                        handleWidgetDispose(event);
                        break;
                    case SWT.Selection:
                        Widget ew = event.widget;
                        if (ew != null) {
                            handleWidgetSelection(event,
                                    ((ToolItem) ew).getSelection());
                        }
                        break;
                    }
                }
            };
        }
        return toolItemListener;
    }

    private void handleWidgetSelection(Event event, boolean selection) {
        if (event.widget == widget) {
            MenuManager menuMan = getDropDownMenuManager();
            Menu menu = menuMan.createContextMenu(widget.getParent());
            if (menu != null) {
                Rectangle b = widget.getBounds();
                Point p = widget.getParent().toDisplay(b.x, b.y + b.height);
                menu.setLocation(p.x, p.y);
                menu.setVisible(true);
            }
        }

    }

    private MenuManager getDropDownMenuManager() {
        if (dropDownMenuManager == null) {
            dropDownMenuManager = new MenuManager();
            dropDownMenuManager.setRemoveAllWhenShown(true);
            dropDownMenuManager.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {
                    fillMenu(manager);
                }
            });
        }
        return dropDownMenuManager;
    }

    private void handleWidgetDispose(Event event) {
        if (event.widget == widget) {
            if (dropDownMenuManager != null) {
                dropDownMenuManager.dispose();
                dropDownMenuManager = null;
            }
            widget = null;
        }
    }

    @Override
    public void fill(Menu menu, int index) {
        if (window == null)
            return;

        if (getParent() instanceof MenuManager) {
            ((MenuManager) getParent()).addMenuListener(menuListener);
        }

        if (!dirty) {
            return;
        }

        MenuManager manager = new MenuManager();
        fillMenu(manager);
        IContributionItem items[] = manager.getItems();
        if (items.length > 0) {
            for (int i = 0; i < items.length; i++) {
                items[i].fill(menu, index++);
            }
        }
        dirty = false;

    }

    private void fillMenu(IMenuManager menu) {
        fillMarkerSheet(MindMapUI.getResourceManager().getSystemMarkerSheet(),
                menu);
        menu.add(new Action(MindMapMessages.AllMarkersMenu_Markers_More_text) {
            @Override
            public void run() {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        if (window == null)
                            return;

                        IWorkbenchPage activePage = window.getActivePage();
                        if (activePage != null)
                            activePage.showView("org.xmind.ui.MarkerView"); //$NON-NLS-1$
                    }
                });
            }
        });
        //fillMarkerSheet(MindMapUI.getResourceManager().getUserMarkerSheet());
    }

    private void fillMarkerSheet(IMarkerSheet sheet, IMenuManager menu) {
        for (final IMarkerGroup group : sheet.getMarkerGroups()) {
            MenuManager groupMenu = new MenuManager(group.getName(), "#" //$NON-NLS-1$
                    + group.getId());
            if (!group.isHidden()) {
                fillGroup(group, groupMenu);
                menu.add(groupMenu);
            }
        }
    }

    private void fillGroup(IMarkerGroup group, IMenuManager groupMenu) {
        for (IMarker marker : group.getMarkers()) {
            if (!marker.isHidden())
                groupMenu.add(new MarkerAction(marker));
        }
    }

    public void initialize(IServiceLocator serviceLocator) {
        window = (IWorkbenchWindow) serviceLocator
                .getService(IWorkbenchWindow.class);
        if (window == null)
            return;
        installListener();
    }

    private void installListener() {
        IPartService partService = window.getPartService();
        if (partService == null)
            return;
        partService.addPartListener(new IPartListener() {

            public void partOpened(IWorkbenchPart part) {
            }

            public void partDeactivated(IWorkbenchPart part) {
            }

            public void partClosed(IWorkbenchPart part) {
            }

            public void partBroughtToTop(IWorkbenchPart part) {
            }

            public void partActivated(IWorkbenchPart part) {
                if (!(part instanceof MindMapEditor) && widget != null
                        && !widget.isDisposed())
                    widget.setEnabled(false);
            }
        });

        ISelectionService selectionService = window.getSelectionService();
        if (selectionService == null)
            return;
        selectionService.addSelectionListener(new ISelectionListener() {

            public void selectionChanged(IWorkbenchPart part,
                    ISelection selection) {
                if (widget != null && !widget.isDisposed()) {
                    if (part instanceof MindMapEditor) {
                        if (selection instanceof StructuredSelection) {
                            Iterator iterator = ((StructuredSelection) selection)
                                    .iterator();
                            while (iterator.hasNext()) {
                                if (!(iterator.next() instanceof ITopic)) {
                                    widget.setEnabled(false);
                                    return;
                                }
                            }
                            widget.setEnabled(true);
                        } else if (selection instanceof ITopic) {
                            widget.setEnabled(true);
                        }
                    } else {
                        widget.setEnabled(false);
                    }
                }
            }
        });
    }

}