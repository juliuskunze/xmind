package org.xmind.ui.internal.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.PageBook;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.dialogs.HyperlinkPage;
import org.xmind.ui.dialogs.IHyperlinkPage;
import org.xmind.ui.dialogs.IHyperlinkPageContainer;
import org.xmind.ui.gallery.FrameFigure;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GalleryPartFactory;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.gallery.ShadowedLayer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IProtocolDescriptor;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.Logger;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

public class HyperlinkDialog2 extends TitleAreaDialog implements
        IHyperlinkPageContainer {

    private static class NullHyperlinkPage extends HyperlinkPage {

        private Label label;

        public void init(IStructuredSelection selection) {
        }

        public void createControl(Composite parent) {
            label = new Label(parent, SWT.NONE);
            label
                    .setText(DialogMessages.HyperlinkDialog2_FailCreatePage_message);
        }

        public void dispose() {
        }

        public Control getControl() {
            return label;
        }

        public void setActionBars(IActionBars actionBars) {
        }

        public void setFocus() {
            if (label != null && !label.isDisposed()) {
                label.setFocus();
            }
        }

    }

    private static class TypeItemPart extends FramePart {

        /**
         * @param model
         */
        public TypeItemPart(Object model) {
            super(model);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.gallery.FramePart#createFigure()
         */
        @Override
        protected IFigure createFigure() {
            IFigure fig = super.createFigure();
            FrameFigure ff = (FrameFigure) fig;
            ShadowedLayer content = ff.getContentPane();
            content.setBackgroundColor(null);
            content.setForegroundColor(null);
            content.setShadowVisible(false);
            content.setShadowAlpha(0);
            content.setBorderWidth(0);
            content.setOpaque(false);
            return fig;
        }

    }

    private static class TypePartFactory extends GalleryPartFactory {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.gallery.GalleryPartFactory#createPart(org.xmind.gef.
         * part.IPart, java.lang.Object)
         */
        public IPart createPart(IPart context, Object model) {
            return super.createPart(context, model);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.gallery.GalleryPartFactory#createFramePart(org.xmind
         * .gef.part.IPart, java.lang.Object)
         */
        @Override
        protected IPart createFramePart(IPart parent, Object model) {
            return new TypeItemPart(model);
        }

    }

//    private class TypeViewerContentProvider implements ITreeContentProvider {
//
//        public Object[] getElements(Object inputElement) {
//            if (inputElement instanceof HyperlinkPageRegistry) {
//                return ((HyperlinkPageRegistry) inputElement)
//                        .getHyperlinkPageDescriptors().toArray();
//            }
//            return new Object[0];
//        }
//
//        public boolean hasChildren(Object element) {
//            return false;
//        }
//
//        public Object[] getChildren(Object parentElement) {
//            return new Object[0];
//        }
//
//        public Object getParent(Object element) {
//            return null;
//        }
//
//        public void dispose() {
//        }
//
//        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
//        }
//    }

    private class TypeViewerLableProvider extends ImageCachedLabelProvider
            implements IFontProvider {
        public String getText(Object element) {
            if (element instanceof HyperlinkPageDescriptor)
                return ((HyperlinkPageDescriptor) element).getName();
            return super.getText(element);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.viewers.ImageCachedLabelProvider#createImage(java.lang
         * .Object)
         */
        @Override
        protected Image createImage(Object element) {
            if (element instanceof HyperlinkPageDescriptor) {
                ImageDescriptor icon = ((HyperlinkPageDescriptor) element)
                        .getIcon();
                if (icon != null)
                    return icon.createImage(false);
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            return FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 2);
        }

    }

    public static final int REMOVE = 3;

    private static final int REMOVE_ID = IDialogConstants.CLIENT_ID + 1;

    private static final String KEY_SASH_WEIGHTS = "sashFormWeights"; //$NON-NLS-1$

    private String value;

    private IStructuredSelection selection;

    private Map<String, IHyperlinkPage> pages = new HashMap<String, IHyperlinkPage>();

    private IHyperlinkPage currentPage;

    // The current page message and description
    private String pageMessage;

    private int pageMessageType = IMessageProvider.NONE;

    private String pageDescription;

    private PageBook pageBook;

//    private StructuredViewer typeViewer;
    private GalleryViewer typeViewer;

    private SashForm form;

    public HyperlinkDialog2(Shell s) {
        super(s);
    }

    public HyperlinkDialog2(Shell parentShell, IStructuredSelection selection) {
        super(parentShell);
        this.selection = selection;
        setShellStyle(SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return MindMapUIPlugin.getDefault().getDialogSettings(
                "org.xmind.ui.HyperlinkDialog"); //$NON-NLS-1$
    }

    public void create() {
        super.create();
        setTitle(DialogMessages.HyperlinkDialog_title);
        pageDescription = DialogMessages.HyperlinkDialog_description;
        setMessage(pageDescription);

        showInitContents();
        updateButtons();
        updateMessage();
    }

    private void showInitContents() {
        List<HyperlinkPageDescriptor> descriptors = HyperlinkPageRegistry
                .getInstance().getHyperlinkPageDescriptors();
        if (!descriptors.isEmpty()) {
            value = getHyperlinkValue(selection);

            HyperlinkPageDescriptor initDescriptor = null;
            if (value != null && !"".equals(value)) { //$NON-NLS-1$
                initDescriptor = findInitDescriptor(descriptors, value);
            }
            if (initDescriptor == null) {
                initDescriptor = descriptors.get(0);
            }

            typeViewer.setSelection(new StructuredSelection(initDescriptor),
                    false);
            if (this.currentPage != null) {
                this.currentPage.setValue(value);
                this.currentPage.setFocus();
            }
        }
    }

    private String getHyperlinkValue(IStructuredSelection selection) {
        ITopicPart last = null;
        String lastHyperlink = null;
        for (Object topic : selection.toArray()) {
            if (topic instanceof ITopicPart) {
                String h = ((ITopicPart) topic).getTopic().getHyperlink();
                if (last != null) {
                    if (h != lastHyperlink
                            && (h == null || !h.equals(lastHyperlink))) {
                        return null;
                    }
                } else {
                    last = ((ITopicPart) topic);
                    lastHyperlink = h;
                }
            }
        }
        return lastHyperlink;
    }

    private HyperlinkPageDescriptor findInitDescriptor(
            List<HyperlinkPageDescriptor> descriptors, String value) {
        IProtocolDescriptor protocolDescriptor = MindMapUI.getProtocolManager()
                .findProtocolDescriptor(value);
        if (protocolDescriptor != null) {
            String id = protocolDescriptor.getId();
            for (HyperlinkPageDescriptor pageDescriptor : descriptors) {
                if (id.equals(pageDescriptor.getProtocolId()))
                    return pageDescriptor;
            }
        }
        return null;
    }

    public void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(DialogMessages.HyperlinkDialog_windowTitle);
        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose(e);
            }
        });
    }

    private void handleDispose(DisposeEvent e) {
        disposePages();
    }

    /**
     * 
     */
    private void disposePages() {
        if (!pages.isEmpty()) {
            for (IHyperlinkPage page : pages.values()) {
                page.dispose();
            }
            pages.clear();
        }
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        form = new SashForm(composite, SWT.HORIZONTAL | SWT.SMOOTH);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) form.getLayoutData()).widthHint = 400;
        ((GridData) form.getLayoutData()).heightHint = 300;
        form.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));
        form.setSashWidth(4);

        createTypeViewer(form);
        createPageBook(form);

        int[] weights = loadSashFormWeights();
        if (weights != null) {
            form.setWeights(weights);
        } else {
            form.setWeights(new int[] { 25, 75 });
        }

        Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        return composite;
    }

    /**
     * @param parent
     */
    private void createPageBook(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));

        pageBook = new PageBook(composite, SWT.NONE);
        pageBook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    /**
     * @param parent
     */
    private void createTypeViewer(Composite parent) {
        typeViewer = new GalleryViewer();

        Properties properties = typeViewer.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.FALSE);
        properties.set(GalleryViewer.Wrap, Boolean.FALSE);
        properties.set(GalleryViewer.TitlePlacement, PositionConstants.RIGHT);
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout(
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_FILL, 0, 1,
                new Insets(1)));
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GEF.SelectionConstraint, GEF.SEL_SINGLE);
//        properties.set(GalleryViewer.FrameContentSize, new Dimension(32, 32));
//        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        editDomain.setViewer(typeViewer);

        typeViewer.setPartFactory(new TypePartFactory());
        typeViewer.setLabelProvider(new TypeViewerLableProvider());
        typeViewer.createControl(parent);

        typeViewer.setInput(HyperlinkPageRegistry.getInstance()
                .getHyperlinkPageDescriptors());

//        typeViewer = new TableViewer(parent, SWT.SINGLE | SWT.NO_SCROLL
//                | SWT.FULL_SELECTION);
//        Control control = typeViewer.getControl();
//        control.setFont(FontUtils.getRelativeHeight(
//                JFaceResources.DEFAULT_FONT, 2));
//
//        typeViewer.setContentProvider(new TreeViewerContentProvider());
//        typeViewer.setLabelProvider(new TreeViewerLableProvider());
//
//        typeViewer.setInput(HyperlinkPageRegistry.getInstance());

        typeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event
                        .getSelection();
                HyperlinkPageDescriptor desc = (HyperlinkPageDescriptor) selection
                        .getFirstElement();
                showPage(desc);
            }
        });
    }

    public IHyperlinkPage getCurrentPage() {
        return currentPage;
    }

    protected void showPage(HyperlinkPageDescriptor desc) {
        String id = desc.getId();
        IHyperlinkPage page = pages.get(id);
        if (page == null) {
            page = createPage(desc);
            page.init(selection);
            page.setContainer(this);
            pages.put(id, page);
        }
        showPage(page, desc);
    }

    private IHyperlinkPage createPage(HyperlinkPageDescriptor desc) {
        try {
            return desc.createPage();
        } catch (CoreException e) {
            Logger.log(e);
        }
        return new NullHyperlinkPage();
    }

    private void showPage(IHyperlinkPage page,
            HyperlinkPageDescriptor descriptor) {
        Control control = getPageControl(page, descriptor);
        pageBook.showPage(control);
        this.currentPage = page;
        updateButtons();
        updateMessage();
    }

    /**
     * @param page
     * @return
     */
    private Control getPageControl(IHyperlinkPage page,
            HyperlinkPageDescriptor descriptor) {
        Control control = page.getControl();
        if (control == null) {
            page.createControl(pageBook);
            control = page.getControl();
            Assert.isNotNull(control);
        }
        return control;
//        Composite container = pageContainer.get(page);
//        if (container == null || container.isDisposed()) {
//            container = createPageContainer();
////            createPageTitle(container, descriptor);
////            createSeparator(container);
////            Composite realContainer = createRealPageContainer(container);
//            page.createControl(pageBook);
//            pageContainer.put(page, page.getControl());
//        }
//        return container;
    }

//    /**
//     * @param container
//     * @return
//     */
//    private Composite createRealPageContainer(Composite parent) {
//        Composite composite = new Composite(parent, SWT.NONE);
//        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//        return composite;
//    }
//
//    /**
//     * @param container
//     */
//    private void createSeparator(Composite parent) {
//        Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
//        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//    }
//
//    /**
//     * @param container
//     * @param descriptor
//     */
//    private void createPageTitle(Composite parent,
//            HyperlinkPageDescriptor descriptor) {
//        Composite composite = new Composite(parent, SWT.NONE);
//        GridLayout gridLayout = new GridLayout(1, false);
//        gridLayout.marginWidth = 5;
//        gridLayout.marginHeight = 5;
//        gridLayout.verticalSpacing = 0;
//        gridLayout.horizontalSpacing = 0;
//        composite.setLayout(gridLayout);
//
//        Label label = new Label(composite, SWT.NONE);
//        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
//                false));
//        label.setText(descriptor.getName());
//        label.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
//                2));
//    }
//
//    /**
//     * @return
//     */
//    private Composite createPageContainer() {
//        Composite composite = new Composite(pageBook, SWT.NONE);
//        GridLayout layout = new GridLayout(1, false);
//        layout.marginWidth = 5;
//        layout.marginHeight = 5;
//        layout.verticalSpacing = 5;
//        layout.horizontalSpacing = 5;
//        composite.setLayout(layout);
//        return composite;
//    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, REMOVE_ID, DialogMessages.HyperlinkDialog_Remove,
                false);
        super.createButtonsForButtonBar(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.TrayDialog#close()
     */
    @Override
    public boolean close() {
        if (form != null && !form.isDisposed()) {
            saveSashFormWeights(form);
        }
        return super.close();
    }

    /**
     * @param form
     */
    private void saveSashFormWeights(SashForm form) {
        IDialogSettings settings = getDialogBoundsSettings();
        if (settings == null)
            return;
        int[] weights = form.getWeights();
        if (weights == null || weights.length != 2)
            return;
        settings.put(KEY_SASH_WEIGHTS, String.format("%d,%d", weights[0], //$NON-NLS-1$
                weights[1]));
    }

    private int[] loadSashFormWeights() {
        IDialogSettings settings = getDialogBoundsSettings();
        if (settings == null)
            return null;
        String value = settings.get(KEY_SASH_WEIGHTS);
        if (value == null)
            return null;
        int index = value.indexOf(',');
        if (index < 0)
            return null;
        int w1, w2;
        try {
            w1 = Integer.parseInt(value.substring(0, index));
            w2 = Integer.parseInt(value.substring(index + 1));
        } catch (NumberFormatException e) {
            return null;
        }
        return new int[] { w1, w2 };
    }

    public void updateButtons() {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (okButton != null && !okButton.isDisposed()) {
            okButton.setEnabled(isOkButtonEnabled());
        }
    }

    private boolean isOkButtonEnabled() {
        if (currentPage == null)
            return false;
        return currentPage.canFinish();
    }

    public void updateMessage() {
        if (currentPage == null) {
            return;
        }

        pageMessage = currentPage.getMessage();
        if (pageMessage != null) {
            pageMessageType = currentPage.getMessageType();
        } else {
            pageMessageType = IMessageProvider.NONE;
        }
        if (pageMessage == null) {
            setMessage(pageDescription);
        } else {
            setMessage(pageMessage, pageMessageType);
        }
        setErrorMessage(currentPage.getErrorMessage());
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == REMOVE_ID) {
            removePressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    protected void removePressed() {
        setReturnCode(REMOVE);
        close();
    }

    protected void okPressed() {
        if (currentPage != null) {
            this.value = currentPage.getValue();
        }
        super.okPressed();
    }

    protected void cancelPressed() {
        super.cancelPressed();
    }

    public String getValue() {
        return value;
    }
}
