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
package org.xmind.ui.internal.wizards;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.internal.MindMapTemplateManager;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.views.ThemesViewer;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;

public class ChooseTemplateWizardPage extends WizardPage implements
        ISelectionChangedListener, IOpenListener, SelectionListener {

    public static final String PAGE_NAME = "org.xmind.ui.wizard.newWorkbookWizard..chooseTemplatePage"; //$NON-NLS-1$

    private static final String SELECTED_TEMPLATE = "selectedTemplate"; //$NON-NLS-1$

    private static final int FRAME_WIDTH = 130;
    private static final int FRAME_HEIGHT = 90;

    private static class TemplateThemesViewer extends ThemesViewer {

        public TemplateThemesViewer(Composite parent) {
            super(parent);
        }

        @Override
        protected void init() {
            super.init();
            EditDomain editDomain = getEditDomain();
            editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
            editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                    new GalleryNavigablePolicy());
            editDomain.uninstallEditPolicy(GEF.TOOL_EDIT);

            Properties properties = getProperties();
            properties.set(GalleryViewer.TitlePlacement,
                    GalleryViewer.TITLE_BOTTOM);
            properties.set(GalleryViewer.FlatFrames, true);
            properties.set(GalleryViewer.Layout, new GalleryLayout(
                    GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_TOPLEFT,
                    10, 10, 10, 10, 10, 10));
        }

        @Override
        public Control createControl(Composite parent, int style) {
            return super.createControl(parent, style | SWT.BORDER);
        }

    }

    private ITemplateDescriptor selectedTemplate;

    private IViewer templatesViewer;

    private IViewer themesViewer;

    private TabItem templatesItem;

    private TabItem themesItem;

    private Button deleteTemplateButton;

    protected ChooseTemplateWizardPage() {
        super(PAGE_NAME, WizardMessages.ChooseTemplateWizardPage_title, null);
        setDescription(WizardMessages.ChooseTemplateWizardPage_description);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 5;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);
        setControl(composite);

        createTabFolder(composite);
        createExtraButtonBar(composite);
        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                saveSettings();
            }
        });
    }

    private void createExtraButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(5, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 5;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        createAddCustomTemplateButton(composite);
        createDeleteCustomTemplateButton(composite);
    }

    private void createAddCustomTemplateButton(Composite parent) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        button.setText(WizardMessages.ChooseTemplateWizardPage_AddTemplate_text);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                String ext = "*" + MindMapUI.FILE_EXT_TEMPLATE; //$NON-NLS-1$
                dialog.setFilterExtensions(new String[] { ext });
                dialog.setFilterNames(new String[] { NLS.bind("{0} ({1})", //$NON-NLS-1$
                        DialogMessages.TemplateFilterName, ext) });
                String path = dialog.open();
                if (path == null)
                    return;

                ITemplateDescriptor template = MindMapTemplateManager
                        .getInstance().importCustomTemplate(path);
                if (template != null) {
                    selectViewer(templatesItem);
                    templatesViewer.setInput(loadTemplatesViewerInput());
                    templatesViewer.setSelection(new StructuredSelection(
                            template), true);
                }
            }
        });
    }

    private void createDeleteCustomTemplateButton(Composite parent) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        button.setText(WizardMessages.ChooseTemplateWizardPage_DeleteTemplate_text);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                ITemplateDescriptor template = selectedTemplate;
                if (template != null
                        && template instanceof FileTemplateDescriptor) {
                    File file = ((FileTemplateDescriptor) template).getFile();
                    file.delete();
                    templatesViewer.setInput(loadTemplatesViewerInput());
                }
            }
        });
        deleteTemplateButton = button;
    }

    private void createTabFolder(Composite parent) {
        TabFolder folder = new TabFolder(parent, SWT.TOP);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) folder.getLayoutData()).widthHint = 650;
        ((GridData) folder.getLayoutData()).heightHint = 320;

        templatesItem = new TabItem(folder, SWT.NONE);
        templatesItem
                .setText(WizardMessages.ChooseTemplateWizardPage_TemplatesGroup_text);
        Control templatesContainer = createTemplatesContainer(folder);
        templatesItem.setControl(templatesContainer);

        themesItem = new TabItem(folder, SWT.NONE);
        themesItem
                .setText(WizardMessages.ChooseTemplateWizardPage_ThemesGroup_text);
        Control themesContainer = createThemesContainer(folder);
        themesItem.setControl(themesContainer);

        loadSelectedTemplate();
        folder.addSelectionListener(this);
    }

    private Control createTemplatesContainer(Composite parent) {
        GalleryViewer viewer = new GalleryViewer();

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        viewer.setEditDomain(editDomain);

        Properties properties = viewer.getProperties();
        properties.set(GalleryViewer.Wrap, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.Horizontal, true);
        properties
                .set(GalleryViewer.TitlePlacement, GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FrameContentSize, new Dimension(
                FRAME_WIDTH, FRAME_HEIGHT));
        properties.set(GalleryViewer.ImageConstrained, true);
        properties.set(GalleryViewer.Layout, new GalleryLayout(
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_TOPLEFT, 10,
                10, 10, 10, 10, 10));

        Control control = viewer.createControl(parent, SWT.BORDER);
        createTemplateDndSupport(control, viewer);
        viewer.setLabelProvider(new TemplateLabelProvider());
        List<ITemplateDescriptor> templates = loadTemplatesViewerInput();
        viewer.setInput(templates);

        if (templates.size() > 0) {
            viewer.setSelection(new StructuredSelection(templates.get(0)), true);
        }
        viewer.addSelectionChangedListener(this);
        viewer.addOpenListener(this);

        templatesViewer = viewer;

        return control;
    }

    private void createTemplateDndSupport(Control control,
            final ISelectionProvider viewer) {
        final boolean[] dragging = new boolean[1];
        dragging[0] = false;
        DropTarget target = new DropTarget(control, DND.DROP_DEFAULT
                | DND.DROP_COPY | DND.DROP_LINK);
        target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent event) {
                if (dragging[0]) {
                    event.detail = DND.DROP_NONE;
                } else {
                    event.detail = DND.DROP_COPY;
                }
            }

            public void dragOperationChanged(DropTargetEvent event) {
                if (dragging[0]) {
                    event.detail = DND.DROP_NONE;
                } else {
                    event.detail = DND.DROP_COPY;
                }
            }

            public void drop(DropTargetEvent event) {
                if (dragging[0] || event.data == null) {
                    event.detail = DND.DROP_NONE;
                } else {
                    importTemplates((String[]) event.data);
                }
            }
        });

        DragSource source = new DragSource(control, DND.DROP_COPY);
        source.setTransfer(new Transfer[] { FileTransfer.getInstance() });
        source.addDragListener(new DragSourceAdapter() {
            List<String> fileNames = new ArrayList<String>();

            public void dragStart(DragSourceEvent event) {
                dragging[0] = true;
                fileNames.clear();
                IStructuredSelection selection = (IStructuredSelection) viewer
                        .getSelection();
                for (Object element : selection.toList()) {
                    if (element instanceof FileTemplateDescriptor) {
                        fileNames.add(((FileTemplateDescriptor) element)
                                .getFile().getAbsolutePath());
                    }
                }
                event.doit = !fileNames.isEmpty();
            }

            public void dragSetData(DragSourceEvent event) {
                event.data = fileNames.toArray(new String[fileNames.size()]);
            }

            public void dragFinished(DragSourceEvent event) {
                fileNames.clear();
                dragging[0] = false;
            }
        });
    }

    private void importTemplates(String[] fileNames) {
        List<ITemplateDescriptor> importedTemplates = MindMapTemplateManager
                .getInstance().importTemplates(fileNames);
        if (!importedTemplates.isEmpty()) {
            selectViewer(templatesItem);
            templatesViewer.setInput(loadTemplatesViewerInput());
            templatesViewer.setSelection(new StructuredSelection(
                    importedTemplates), true);
        }
    }

    private Control createThemesContainer(Composite parent) {
        TemplateThemesViewer viewer = new TemplateThemesViewer(parent);
        IStyle theme = MindMapUI.getResourceManager().getDefaultTheme();
        if (theme != null)
            viewer.setDefaultTheme(theme);
        List<IStyle> themes = loadThemesViewerInput();
        viewer.setInput(themes);
        if (themes.size() > 0) {
            viewer.setSelection(new StructuredSelection(themes.get(0)), true);
        }
        viewer.addSelectionChangedListener(this);
        viewer.addOpenListener(this);

        themesViewer = viewer;

        return viewer.getControl();
    }

    private void updateButtons() {
        setPageComplete(selectedTemplate != null);
        if (deleteTemplateButton != null) {
            deleteTemplateButton.setEnabled(selectedTemplate != null
                    && selectedTemplate instanceof FileTemplateDescriptor);
        }
    }

    private void setSelectedTemplate(ITemplateDescriptor template) {
        this.selectedTemplate = template;
        updateButtons();
    }

    public ITemplateDescriptor getTemplate() {
        return selectedTemplate;
    }

    private List<ITemplateDescriptor> loadTemplatesViewerInput() {
        return MindMapTemplateManager.getInstance().loadAllTemplates();
    }

    private List<IStyle> loadThemesViewerInput() {
        IResourceManager resourceManager = MindMapUI.getResourceManager();
        IStyleSheet systemThemeSheets = resourceManager.getSystemThemeSheet();
        Set<IStyle> systemThemes = systemThemeSheets
                .getStyles(IStyleSheet.MASTER_STYLES);

        IStyleSheet userThemeSheets = resourceManager.getUserThemeSheet();
        Set<IStyle> userThemes = userThemeSheets
                .getStyles(IStyleSheet.MASTER_STYLES);

        List<IStyle> list = new ArrayList<IStyle>(systemThemes.size()
                + userThemes.size() + 1);
        list.add(resourceManager.getBlankTheme());
        list.addAll(systemThemes);
        list.addAll(userThemes);
        return list;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        ITemplateDescriptor template = findTemplate(event.getSelection());
        setSelectedTemplate(template);
    }

    public void open(OpenEvent event) {
        ITemplateDescriptor template = findTemplate(event.getSelection());
        if (template != null) {
            applyTemplate(template);
        }
    }

    private ITemplateDescriptor findTemplate(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection)
                    .getFirstElement();
            if (element instanceof ITemplateDescriptor) {
                return (ITemplateDescriptor) element;
            } else if (element instanceof IStyle) {
                return new ThemeTemplateDescriptor((IStyle) element);
            }
        }
        return null;
    }

    private void applyTemplate(ITemplateDescriptor template) {
        setSelectedTemplate(template);
        if (getWizard().canFinish()) {
            if (getWizard().performFinish()) {
                getContainer().getShell().dispose();
            }
        } else if (canFlipToNextPage()) {
            getContainer().showPage(getNextPage());
        }
    }

    public void widgetSelected(SelectionEvent e) {
        TabItem item = (TabItem) e.item;
        selectViewer(item);
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    private void selectViewer(TabItem item) {
        if (item == templatesItem) {
            setSelectedTemplate(findTemplate(templatesViewer.getSelection()));
        } else if (item == themesItem) {
            setSelectedTemplate(findTemplate(themesViewer.getSelection()));
        }
        item.getParent().setSelection(item);
        item.getControl().setFocus();
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setPageComplete(isPageComplete());
    }

    private void saveSettings() {
        getDialogSettings().put(SELECTED_TEMPLATE,
                getTemplateSymbolicName(this.selectedTemplate));
    }

    private String getTemplateSymbolicName(ITemplateDescriptor template) {
        if (template instanceof URLTemplateDescriptor) {
            return ((URLTemplateDescriptor) template).getURL().toExternalForm();
        } else if (template instanceof FileTemplateDescriptor) {
            return "file:" + ((FileTemplateDescriptor) template).getFile().getAbsolutePath(); //$NON-NLS-1$
        } else if (template instanceof ThemeTemplateDescriptor) {
            IStyle theme = ((ThemeTemplateDescriptor) template).getTheme();
            if (theme == MindMapUI.getResourceManager().getBlankTheme()) {
                return "theme:blank"; //$NON-NLS-1$
            } else {
                return "theme:" + theme.getId(); //$NON-NLS-1$
            }
        } else if (template instanceof DefaultTemplateDescriptor) {
            return "default:" + ((DefaultTemplateDescriptor) template).getId(); //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    private void loadSelectedTemplate() {
        String uri = getDialogSettings().get(SELECTED_TEMPLATE);
        if (uri != null && !"".equals(uri)) { //$NON-NLS-1$
            if (uri.startsWith("theme:")) { //$NON-NLS-1$
                IStyle theme = findTheme(uri);
                if (theme != null) {
                    selectTheme(theme);
                }
            } else if (uri.startsWith("file:")) { //$NON-NLS-1$
                String path = uri.substring(5);
                File file = new File(path);
                if (file.isFile() && file.canRead()) {
                    ITemplateDescriptor template = new FileTemplateDescriptor(
                            file);
                    selectTemplate(template);
                } else {
                    loadSelectedTemplateFromURL(uri);
                }
            } else if (uri.startsWith("default:")) { //$NON-NLS-1$
                String id = uri.substring(8);
                selectTemplate(new DefaultTemplateDescriptor(id, "")); //$NON-NLS-1$
            } else {
                loadSelectedTemplateFromURL(uri);
            }
        }
    }

    private void loadSelectedTemplateFromURL(String uri) {
        try {
            URL url = new URL(uri);
            ITemplateDescriptor template = new URLTemplateDescriptor(url, ""); //$NON-NLS-1$
            selectTemplate(template);
        } catch (MalformedURLException e) {
        }
    }

    private void selectTheme(IStyle theme) {
        selectViewer(themesItem);
        themesViewer.setSelection(new StructuredSelection(theme), true);
    }

    private void selectTemplate(ITemplateDescriptor template) {
        selectViewer(templatesItem);
        templatesViewer.setSelection(new StructuredSelection(template), true);
    }

    private IStyle findTheme(String uri) {
        String styleId = uri.substring(6);
        IResourceManager resourceManager = MindMapUI.getResourceManager();
        if ("blank".equals(styleId)) { //$NON-NLS-1$
            return resourceManager.getBlankTheme();
        }
        IStyleSheet systemThemeSheet = resourceManager.getSystemThemeSheet();
        IStyle style = systemThemeSheet.findStyle(styleId);
        if (style != null)
            return style;
        IStyleSheet userThemeSheet = resourceManager.getUserThemeSheet();
        return userThemeSheet.findStyle(styleId);
    }

}