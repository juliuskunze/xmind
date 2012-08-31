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
package org.xmind.ui.internal.views;

import static org.xmind.ui.mindmap.MindMapUI.REQ_ADD_MARKER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.forms.WidgetFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dnd.MindMapElementTransfer;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.internal.wizards.MarkerExportWizard;
import org.xmind.ui.internal.wizards.MarkerImportWizard;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class MarkerView extends ViewPart implements IContributedContentsView {

    private class ShowMarkerManagerAction extends Action {
        public ShowMarkerManagerAction() {
            super(MindMapMessages.ShowMarkerManager_text);
            setToolTipText(MindMapMessages.ShowMarkerManager_toolTip);
            setImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.ADD_MARKER, true));
        }

        public void run() {
            PreferencesUtil.createPreferenceDialogOn(null,
                    MarkerManagerPrefPage.ID, null, null).open();
        }
    }

    private class ImportMarkerAction extends Action {
        public ImportMarkerAction() {
            super(MindMapMessages.ImportMarkers_text);
            setToolTipText(MindMapMessages.ImportMarkers_toolTip);
            setImageDescriptor(MindMapUI.getImages().get(IMindMapImages.IMPORT,
                    true));
        }

        public void run() {
            MarkerImportWizard wizard = new MarkerImportWizard(false);
            wizard.init(PlatformUI.getWorkbench(), getCurrentSelection());
            WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
            dialog.create();
            dialog.open();
        }

    }

    private class ExportMarkerAction extends Action {
        public ExportMarkerAction() {
            super(MindMapMessages.ExportMarkers_text);
            setToolTipText(MindMapMessages.ExportMarkers_toolTip);
            setImageDescriptor(MindMapUI.getImages().get(IMindMapImages.EXPORT,
                    true));
        }

        public void run() {
            MarkerExportWizard wizard = new MarkerExportWizard();
            wizard.init(PlatformUI.getWorkbench(), getCurrentSelection());
            WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
            dialog.create();
            dialog.open();
        }
    }

    protected class MarkerAction extends Action {

        private IMarker marker;

        public MarkerAction(IMarker marker) {
            super();
            this.marker = marker;
            setImageDescriptor(MarkerImageDescriptor.createFromMarker(marker,
                    32, 32));
            setToolTipText(marker.getName());
        }

        public IMarker getMarker() {
            return marker;
        }

        public void run() {
            IViewer viewer = getCurrentViewer();
            if (viewer != null) {
                EditDomain domain = viewer.getEditDomain();
                if (domain != null) {
                    Request req = new Request(REQ_ADD_MARKER).setViewer(viewer)
                            .setDomain(domain).setParameter(
                                    MindMapUI.PARAM_MARKER_ID, marker.getId());
                    domain.handleRequest(req);
                    MindMapUI.getResourceManager().getRecentMarkerGroup()
                            .addMarker(marker);
                }
            }
        }

        private IViewer getCurrentViewer() {
            IWorkbenchPage page = getSite().getPage();
            if (page != null) {
                IEditorPart editor = page.getActiveEditor();
                if (editor != null && editor instanceof IGraphicalEditor) {
                    IGraphicalEditor ge = (IGraphicalEditor) editor;
                    IGraphicalEditorPage gp = ge.getActivePageInstance();
                    if (gp != null) {
                        return gp.getViewer();
                    }
                }
            }
            return null;
        }
    }

    private static interface ISectionPart {

        Control createControl(Composite parent);

        Control getControl();

        void refresh();

        void dispose();
    }

    protected class MarkerGroupPart implements ISectionPart, ICoreEventListener {

        private IMarkerGroup group;

        private boolean hasTitle;

        private ToolBarManager toolbar = null;

        private Control control = null;

        private ICoreEventRegister eventRegister = null;

        public MarkerGroupPart(IMarkerGroup group) {
            this(group, true);
        }

        public MarkerGroupPart(IMarkerGroup group, boolean hasTitle) {
            this.group = group;
            this.hasTitle = hasTitle;
        }

        public IMarkerGroup getMarkerGroup() {
            return group;
        }

        public Control createControl(Composite parent) {
            if (control == null) {
                if (toolbar == null) {
                    toolbar = new ToolBarManager(SWT.RIGHT | SWT.FLAT
                            | SWT.WRAP);
                }
                Composite c = factory.createComposite(parent);
                GridLayout layout = new GridLayout(1, true);
                layout.marginHeight = 2;
                layout.marginWidth = 2;
                c.setLayout(layout);

                if (hasTitle) {
                    factory.createLabel(c, group.getName());
                    layout.verticalSpacing = 0;
                } else {
                    layout.verticalSpacing = 2;
                }

                ToolBar tb = toolbar.createControl(c);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.widthHint = 250;
                tb.setLayoutData(data);
                addDragSource(tb);

                control = c;

                refresh();
                installListeners();
                control.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        dispose();
                    }
                });
            }
            return control;
        }

        private void addDragSource(final ToolBar toolbar) {
            final DragSource dragSource = new DragSource(toolbar, DND.DROP_COPY);
            dragSource.setTransfer(new Transfer[] { MindMapElementTransfer
                    .getInstance() });
            dragSource.addDragListener(new DragSourceListener() {

                ToolItem sourceItem;

                public void dragStart(DragSourceEvent event) {
                    sourceItem = toolbar.getItem(new Point(event.x, event.y));
                    if (sourceItem == null)
                        event.doit = false;
                    else {
                        event.image = sourceItem.getImage();
                    }
                }

                public void dragSetData(DragSourceEvent event) {
                    if (sourceItem == null)
                        return;

                    int index = toolbar.indexOf(sourceItem);
                    IMarker marker = group.getMarkers().get(index);
                    event.data = new Object[] { marker };
                }

                public void dragFinished(DragSourceEvent event) {
                }
            });
            toolbar.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    dragSource.dispose();
                }
            });
        }

        private void installListeners() {
            if (group instanceof ICoreEventSource) {
                eventRegister = new CoreEventRegister((ICoreEventSource) group,
                        this);
                eventRegister.register(Core.MarkerAdd);
                eventRegister.register(Core.MarkerRemove);
            }
        }

        private void uninstallListeners() {
            if (eventRegister != null)
                eventRegister.unregisterAll();
        }

        public Control getControl() {
            return control;
        }

        public void refresh() {
            if (toolbar == null || control == null || control.isDisposed())
                return;

            toolbar.removeAll();
            for (IMarker marker : group.getMarkers()) {
                toolbar.add(new MarkerAction(marker));
            }
            toolbar.update(false);

            ToolBar tb = toolbar.getControl();
            GridData data = (GridData) tb.getLayoutData();
            data.exclude = toolbar.isEmpty();

            form.reflow(true);
        }

        public void dispose() {
            uninstallListeners();
            if (toolbar != null) {
                toolbar.dispose();
                toolbar = null;
            }
            if (control != null) {
                control.dispose();
                control = null;
            }
        }

        public void handleCoreEvent(CoreEvent event) {
            String type = event.getType();
            if (Core.MarkerAdd.equals(type) || Core.MarkerRemove.equals(type)) {
                refresh();
            }
        }

    }

    protected class MarkerSheetPart implements ISectionPart, ICoreEventListener {

        private IMarkerSheet sheet;

        private Composite composite;

        private List<MarkerGroupPart> groupParts = new ArrayList<MarkerGroupPart>();

        private ICoreEventRegister eventRegister = null;

        public MarkerSheetPart(IMarkerSheet sheet) {
            this.sheet = sheet;
        }

        public Control getControl() {
            return composite;
        }

        public Control createControl(Composite parent) {
            if (composite == null) {
                composite = createComposite(parent);
                refresh();
                installListeners();
                composite.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        dispose();
                    }
                });
            }
            return composite;
        }

        private Composite createComposite(Composite parent) {
            Composite composite = factory.createComposite(parent);
            GridLayout layout = new GridLayout(1, true);
            layout.marginHeight = 1;
            layout.marginWidth = 1;
            layout.verticalSpacing = 7;
            composite.setLayout(layout);
            return composite;
        }

        public void refresh() {
            if (composite == null || composite.isDisposed())
                return;

            composite.setRedraw(false);
            List<IMarkerGroup> newGroups = sheet.getMarkerGroups();
            int i;
            for (i = 0; i < newGroups.size(); i++) {
                IMarkerGroup group = newGroups.get(i);
                if (i < groupParts.size()) {
                    MarkerGroupPart part = groupParts.get(i);
                    IMarkerGroup g = part.getMarkerGroup();
                    if (group.equals(g)) {
                        continue;
                    }
                }

                MarkerGroupPart part = groupToPart.get(group);
                if (part != null) {
                    reorderChild(part, i);
                } else {
                    part = createChild(group);
                    addChild(part, i);
                }
            }

            Object[] toTrim = groupParts.toArray();
            for (; i < toTrim.length; i++) {
                removeChild((MarkerGroupPart) toTrim[i]);
            }

            composite.setRedraw(true);
            form.reflow(true);
        }

        public List<MarkerGroupPart> getGroupParts() {
            return groupParts;
        }

        private void reorderChild(MarkerGroupPart part, int index) {
            Control c = part.getControl();
            if (c == null) {
                c = part.createControl(composite);
                c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }
            groupParts.remove(part);
            groupParts.add(index, part);
            if (index == 0) {
                c.moveAbove(null);
            } else {
                MarkerGroupPart g = groupParts.get(index - 1);
                c.moveAbove(g.getControl());
            }
        }

        private MarkerGroupPart createChild(IMarkerGroup group) {
            MarkerGroupPart part = new MarkerGroupPart(group, true);
            groupToPart.put(group, part);
            return part;
        }

        private void addChild(MarkerGroupPart part, int index) {
            groupParts.add(index, part);
            Control c = part.createControl(composite);
            c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        private void removeChild(MarkerGroupPart part) {
            groupParts.remove(part);
            groupToPart.remove(part.getMarkerGroup());
            part.dispose();
        }

        private void installListeners() {
            if (sheet instanceof ICoreEventSource) {
                eventRegister = new CoreEventRegister((ICoreEventSource) sheet,
                        this);
                eventRegister.register(Core.MarkerGroupAdd);
                eventRegister.register(Core.MarkerGroupRemove);
            }
        }

        private void uninstallListeners() {
            if (eventRegister != null)
                eventRegister.unregisterAll();
        }

        public void dispose() {
            uninstallListeners();
            if (composite != null) {
                composite.dispose();
                composite = null;
            }
            for (Object o : groupParts.toArray()) {
                MarkerGroupPart groupPart = (MarkerGroupPart) o;
                groupToPart.remove(groupPart.getMarkerGroup());
                groupPart.dispose();
            }
            groupParts.clear();
        }

        public void handleCoreEvent(CoreEvent event) {
            String type = event.getType();
            if (Core.MarkerGroupAdd.equals(type)
                    || Core.MarkerGroupRemove.equals(type)) {
                refresh();
            }
        }
    }

    private WidgetFactory factory;

    private ScrolledForm form;

    private MarkerGroupPart recentPart;

    private MarkerSheetPart systemPart;

    private MarkerSheetPart userPart;

    private Map<IMarkerGroup, MarkerGroupPart> groupToPart = new HashMap<IMarkerGroup, MarkerGroupPart>();

    private IAction showMarkerManagerAction;

    private IAction importMarkerAction;

    private IAction exportMarkerAction;

    public void createPartControl(Composite parent) {
        factory = new WidgetFactory(parent.getDisplay());
        form = createForm(parent, factory);
        form.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (factory != null) {
                    factory.dispose();
                    factory = null;
                }
            }
        });

        Composite composite = form.getBody();
        composite.setLayout(new GridLayout(1, true));

        createRecentSection(composite, factory);
        createSystemSection(composite, factory);
        createUserSection(composite, factory);

        form.reflow(true);

        showMarkerManagerAction = new ShowMarkerManagerAction();
        importMarkerAction = new ImportMarkerAction();
        exportMarkerAction = new ExportMarkerAction();

        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        menu.add(showMarkerManagerAction);
        menu.add(new Separator());
        menu.add(importMarkerAction);
        menu.add(exportMarkerAction);
        menu.add(new Separator());
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        IToolBarManager toolBar = getViewSite().getActionBars()
                .getToolBarManager();
        toolBar.add(showMarkerManagerAction);
        toolBar.add(new Separator());
        toolBar.add(importMarkerAction);
        toolBar.add(exportMarkerAction);
        toolBar.add(new Separator());
        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private ScrolledForm createForm(Composite parent, WidgetFactory factory) {
        return factory.createScrolledForm(parent);
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

    private void createUserSection(Composite composite, WidgetFactory factory) {
        userPart = new MarkerSheetPart(MindMapUI.getResourceManager()
                .getUserMarkerSheet());
        createSection(composite, MindMapMessages.MarkerView_UserMarkers_label,
                userPart, factory);
    }

    private void createSystemSection(Composite composite, WidgetFactory factory) {
        systemPart = new MarkerSheetPart(MindMapUI.getResourceManager()
                .getSystemMarkerSheet());
        createSection(composite, MindMapMessages.MarkerView_XMindMarkers_label,
                systemPart, factory);
    }

    private void createRecentSection(Composite composite, WidgetFactory factory) {
        recentPart = new MarkerGroupPart(MindMapUI.getResourceManager()
                .getRecentMarkerGroup(), false);
        createSection(composite, MindMapMessages.MarkerView_RecentlyUsed_label,
                recentPart, factory);
    }

    private void createSection(Composite parent, String title,
            final ISectionPart part, WidgetFactory factory) {
        Section section = factory.createSection(parent, Section.TITLE_BAR
                | Section.TWISTIE | Section.EXPANDED | SWT.BORDER);
        section.setText(title);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control c = part.createControl(section);
        section.setClient(c);
    }

    public void setFocus() {
        if (form != null)
            form.setFocus();
    }

    public IWorkbenchPart getContributingPart() {
        return getSite().getPage().getActiveEditor();
    }

    public void dispose() {
        super.dispose();
        factory = null;
        form = null;
        recentPart = null;
        systemPart = null;
        userPart = null;
        groupToPart.clear();
    }

    private IStructuredSelection getCurrentSelection() {
        ISelection selection = getSite().getPage().getSelection();
        if (selection instanceof IStructuredSelection)
            return (IStructuredSelection) selection;
        return StructuredSelection.EMPTY;
    }

}