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
package org.xmind.ui.internal.properties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IUseTransparency;
import org.xmind.gef.util.Properties;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.viewers.MButton;
import org.xmind.ui.viewers.SWTUtils;
import org.xmind.ui.viewers.SliderViewer;

public class WallpaperPropertySectionPart extends StyledPropertySectionPart {

    private class SelectWallpaperDialog extends PopupDialog implements
            IOpenListener {

        private class WallpaperLabelProvider extends LabelProvider {

            public Image getImage(Object element) {
                return getWallpaperPreviewImage(element);
            }

        }

        private Control handle;

        private GalleryViewer viewer;

        public SelectWallpaperDialog(Shell parent, Control handle) {
            super(parent, SWT.RESIZE, true, true, true, false, false, null,
                    null);
            this.handle = handle;
        }

        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);

            viewer = new GalleryViewer();
            Properties properties = viewer.getProperties();
            properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
            properties.set(GalleryViewer.Wrap, Boolean.TRUE);
            properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
            properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
            properties.set(GalleryViewer.Layout, new GalleryLayout(
                    GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_FILL, 1,
                    1, new Insets(5)));
            properties.set(GalleryViewer.FrameContentSize,
                    new Dimension(48, 48));
            properties.set(GalleryViewer.TitlePlacement,
                    GalleryViewer.TITLE_BOTTOM);
            properties.set(GalleryViewer.SingleClickToOpen, Boolean.TRUE);
            properties.set(GalleryViewer.HideTitle, Boolean.TRUE);
            properties.set(GalleryViewer.SolidFrames, Boolean.FALSE);

            viewer.setLabelProvider(new WallpaperLabelProvider());
            viewer.addOpenListener(this);

            EditDomain editDomain = new EditDomain();
            editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
            viewer.setEditDomain(editDomain);

            viewer.createControl(composite);
            GridData galleryData = new GridData(GridData.FILL, GridData.FILL,
                    true, true);
            galleryData.widthHint = 360;
            galleryData.heightHint = 300;
            viewer.getControl().setLayoutData(galleryData);

            final Display display = parent.getDisplay();
            viewer.getControl().setBackground(
                    display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            if (allImageFiles != null && loadedImageFiles != null
                    && loadedImageFiles.containsAll(allImageFiles)) {
                viewer.setInput(loadedImageFiles.toArray());
            } else {
                viewer.setInput(new Object[0]);
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            startLoadingImages(display);
                        }
                    }
                });
            }

            Composite bottom = new Composite(composite, SWT.NONE);
            GridLayout bottomLayout = new GridLayout();
            bottomLayout.marginWidth = 0;
            bottomLayout.marginHeight = 0;
            bottom.setLayout(bottomLayout);
            bottom.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
                    true, false));

            Button chooseFromLocalButton = new Button(bottom, SWT.PUSH);
            chooseFromLocalButton.setText(PropertyMessages.LocalImage_text);
            chooseFromLocalButton.setLayoutData(new GridData(GridData.CENTER,
                    GridData.CENTER, true, false));
            chooseFromLocalButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    close();
                    openLocalImageFileDialog();
                }
            });

            return composite;
        }

        private void startLoadingImages(final Display display) {
            if (imageLoader != null) {
                imageLoader.cancel();
                imageLoader = null;
            }

            imageLoader = new Job(PropertyMessages.LoadWallpapers_jobName) {

                private Runnable refreshJob = null;

                protected IStatus run(IProgressMonitor monitor) {
                    if (allImageFiles == null) {
                        collectImageFiles(getWallpapersPath());
                        if (allImageFiles == null) {
                            allImageFiles = Collections.emptyList();
                        }
                    }

                    if (allImageFiles.isEmpty()) {
                        if (loadedImageFiles == null
                                || !loadedImageFiles.isEmpty())
                            loadedImageFiles = Collections.emptyList();
                        refreshViewer(display);
                    } else if (loadedImageFiles != null
                            && loadedImageFiles.containsAll(allImageFiles)) {
                        refreshViewer(display);
                    } else {
                        monitor.beginTask(null, allImageFiles.size());
                        if (loadedImageFiles == null)
                            loadedImageFiles = new ArrayList<String>(
                                    allImageFiles.size());
                        long lastRefresh = System.currentTimeMillis();
                        for (Object o : allImageFiles.toArray()) {
                            if (monitor.isCanceled()) {
                                break;
                            }

                            if (!loadedImageFiles.contains(o)) {
                                final String path = (String) o;
                                monitor.subTask(new File(path).getName());

                                Image image = getWallpaperPreviewImage(display,
                                        path);
                                if (image != null) {
                                    loadedImageFiles.add(path);
                                } else {
                                    allImageFiles.remove(path);
                                }
                            }

                            monitor.worked(1);

                            if ((System.currentTimeMillis() - lastRefresh) > 50) {
                                refreshViewer(display);
                                lastRefresh = System.currentTimeMillis();
                            }

                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }

                    if (!monitor.isCanceled()) {
                        monitor.done();
                    }
                    imageLoader = null;
                    refreshViewer(display);
                    return new Status(IStatus.OK, MindMapUIPlugin.PLUGIN_ID,
                            IStatus.OK, "Wallpaper images loaded.", null); //$NON-NLS-1$
                }

                private void refreshViewer(final Display display) {
                    if (refreshJob != null)
                        return;

                    refreshJob = new Runnable() {
                        public void run() {
                            if (viewer != null && viewer.getControl() != null
                                    && !viewer.getControl().isDisposed()
                                    && loadedImageFiles != null) {
                                viewer.setInput(loadedImageFiles.toArray());
                                viewer.getControl().getParent().layout();
                            }
                            refreshJob = null;
                        }
                    };
                    display.asyncExec(refreshJob);
                }

                private void collectImageFiles(String path) {
                    File file = new File(path);
                    if (file.isDirectory() && path.equals(getWallpapersPath())) {
                        for (String name : file.list()) {
                            collectImageFiles(new File(file, name)
                                    .getAbsolutePath());
                        }
                    } else if (file.isFile()) {
                        String ext = FileUtils.getExtension(path);
                        ImageFormat format = ImageFormat.findByExtension(ext,
                                null);
                        if (format != null) {
                            if (allImageFiles == null)
                                allImageFiles = new ArrayList<String>();
                            allImageFiles.add(path);
                        }
                    }
                }
            };
            imageLoader.schedule();
        }

        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.addListener(SWT.Deactivate, new Listener() {
                public void handleEvent(Event event) {
                    event.display.asyncExec(new Runnable() {
                        public void run() {
                            close();
                        }
                    });
                }
            });
            shell.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    if (imageLoader != null) {
                        imageLoader.cancel();
                        imageLoader = null;
                    }
                }
            });
        }

        @SuppressWarnings("unchecked")
        protected List getBackgroundColorExclusions() {
            List list = super.getBackgroundColorExclusions();
            if (viewer != null) {
                list.add(viewer.getControl());
            }
            return list;
        }

        protected Point getInitialLocation(Point initialSize) {
            if (handle != null && !handle.isDisposed()) {
                Point loc = handle.toDisplay(handle.getLocation());
                return new Point(loc.x, loc.y + handle.getBounds().height);
            }
            return super.getInitialLocation(initialSize);
        }

        protected IDialogSettings getDialogSettings() {
            return MindMapUIPlugin.getDefault().getDialogSettings(
                    MindMapUI.POPUP_DIALOG_SETTINGS_ID);
        }

        public void open(OpenEvent event) {
            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof String) {
                String path = (String) o;
                close();
                changeWallpaper(path);
            }
        }

    }

    private class OpacityLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int value = getOpacityValueFromScaleElement(element);
            if (value >= 0) {
                return NLS.bind(PropertyMessages.WallpaperOpacity_pattern,
                        value);
            }
            return super.getText(element);
        }
    }

    private class OpacitySelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            int value = getOpacityValueFromScaleSelection(event.getSelection());
            if (value >= 0) {
                opacityInput.setText(String.valueOf(value));
                previewOpacity(value);
            }
        }

    }

    private class OpacityPostSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            int value = getOpacityValueFromScaleSelection(event.getSelection());
            if (value >= 0) {
                changeOpacity(value);
                selectAllOpacityText(true);
            }
        }

    }

    private class RemoveWallpaperAction extends Action {

        public RemoveWallpaperAction() {
            super(null, AS_PUSH_BUTTON);
            setImageDescriptor(MindMapUI.getImages().get(IMindMapImages.REMOVE,
                    true));
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.REMOVE, false));
            setToolTipText(PropertyMessages.RemoveWallpaper_toolTip);
        };

        public void run() {
            removeWallpaper();
        }
    }

    private static final Dimension FRAME_IMAGE_SIZE = new Dimension(64, 64);

    private static String WallpapersPath = null;

    private MButton selectWallpaperWidget;

    private PopupDialog selectWallpaperDialog;

    private IAction removeWallpaperAction;

    private Composite opacityGroup;

    private Text opacityInput;

    private SliderViewer opacityScale;

    private Image selectWallpaperImage;

    private Image opacityImage;

    private Map<Object, Image> wallpaperPreviewImages;

    private Job imageLoader;

    private List<String> allImageFiles;

    private List<String> loadedImageFiles;

    protected void createContent(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout1 = new GridLayout(2, false);
        layout1.marginWidth = 0;
        layout1.marginHeight = 0;
        layout1.horizontalSpacing = 3;
        line1.setLayout(layout1);
        createLineContent1(line1);

        opacityGroup = new Composite(parent, SWT.NONE);
        opacityGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
                true, false));
        GridLayout layout2 = new GridLayout(4, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 3;
        opacityGroup.setLayout(layout2);
        createLineContent2(opacityGroup);

        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                releaseImages();
            }
        });
    }

    private void createLineContent1(Composite parent) {
        selectWallpaperWidget = new MButton(parent, MButton.NORMAL);
        selectWallpaperWidget.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        selectWallpaperWidget.setImage(getSelectWallpaperImage());
        selectWallpaperWidget.setText(PropertyMessages.SelectWallpaper_text);
        selectWallpaperWidget.getControl().setToolTipText(
                PropertyMessages.SelectWallpaper_toolTip);
        selectWallpaperWidget.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                openSelectWallpaperDialog();
            }
        });

        removeWallpaperAction = new RemoveWallpaperAction();
        ToolBarManager removeWallpaperBar = new ToolBarManager(SWT.FLAT);
        removeWallpaperBar.add(removeWallpaperAction);
        removeWallpaperBar.createControl(parent);
        removeWallpaperBar.getControl().setLayoutData(
                new GridData(GridData.END, GridData.CENTER, false, false));
    }

    private void createLineContent2(Composite parent) {
        Label opacityLabel = new Label(parent, SWT.NONE);
        opacityLabel.setImage(getOpacityImage());
        opacityLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false));

        opacityInput = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.TRAIL);
        SWTUtils.makeNumeralInput(opacityLabel, false, false);
        GridData inputData = new GridData(GridData.FILL, GridData.CENTER,
                false, false);
        inputData.widthHint = 25;
        opacityInput.setLayoutData(inputData);
        opacityInput.setTextLimit(3);
        opacityInput.setToolTipText(PropertyMessages.WallpaperOpacity_toolTip);
        Listener inputEventHandler = new Listener() {
            public void handleEvent(Event event) {
                if (event.type == SWT.DefaultSelection) {
                    int value = getOpacityValueFromTextWidget();
                    if (value >= 0) {
                        changeOpacity(value);
                        selectAllOpacityText(false);
                    }
                } else if (event.type == SWT.KeyDown) {
                    if (event.character == SWT.ESC) {
                        updateOpacityToWidget();
                    }
                } else if (event.type == SWT.FocusIn) {
                    selectAllOpacityText(false);
                }
            }
        };
        opacityInput.addListener(SWT.DefaultSelection, inputEventHandler);
        opacityInput.addListener(SWT.KeyDown, inputEventHandler);
        opacityInput.addListener(SWT.FocusIn, inputEventHandler);

        Label percentageLabel = new Label(parent, SWT.NONE);
        percentageLabel.setText("%"); //$NON-NLS-1$
        percentageLabel.setLayoutData(new GridData(GridData.FILL,
                GridData.CENTER, false, false));

        opacityScale = new SliderViewer(parent, SWT.HORIZONTAL);
        opacityScale.setLabelProvider(new OpacityLabelProvider());
        opacityScale.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false));
        opacityScale.getControl().setBackground(parent.getBackground());
        opacityScale
                .addSelectionChangedListener(new OpacitySelectionChangedListener());
        opacityScale
                .addPostSelectionChangedListener(new OpacityPostSelectionChangedListener());
    }

    private void selectAllOpacityText(final boolean takeFocus) {
        if (opacityInput != null && !opacityInput.isDisposed()) {
            opacityInput.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (opacityInput != null && !opacityInput.isDisposed()) {
                        if (takeFocus)
                            opacityInput.setFocus();
                        opacityInput.selectAll();
                    }
                }
            });
        }
    }

    private int getOpacityValueFromTextWidget() {
        if (opacityInput != null && !opacityInput.isDisposed()) {
            try {
                return Math.min(100, Integer.parseInt(opacityInput.getText()));
            } catch (NumberFormatException e) {
            }
        }
        return -1;
    }

    private int getOpacityValueFromScaleSelection(ISelection selection) {
        Object o = ((IStructuredSelection) selection).getFirstElement();
        return getOpacityValueFromScaleElement(o);
    }

    private int getOpacityValueFromScaleElement(Object o) {
        if (o instanceof Double) {
            double value = ((Double) o).doubleValue();
            return (int) Math.round(value * 100);
        }
        return -1;
    }

    private Image getSelectWallpaperImage() {
        if (selectWallpaperImage == null || selectWallpaperImage.isDisposed()) {
            ImageDescriptor icon = MindMapUI.getImages().get(
                    IMindMapImages.INSERT_IMAGE, true);
            if (icon != null) {
                selectWallpaperImage = icon.createImage(false);
            }
        }
        return selectWallpaperImage;
    }

    private Image getOpacityImage() {
        if (opacityImage == null) {
            ImageDescriptor icon = MindMapUI.getImages().get(
                    IMindMapImages.OPAQUE);
            if (icon != null) {
                opacityImage = icon.createImage(false);
            }
        }
        return opacityImage;
    }

    private Image getWallpaperPreviewImage(Object element) {
        return getWallpaperPreviewImage(Display.getCurrent(), element);
    }

    private Image getWallpaperPreviewImage(Display display, Object element) {
        Image image = null;
        if (wallpaperPreviewImages != null) {
            image = wallpaperPreviewImages.get(element);
        }
        if (image == null) {
            if (element instanceof String) {
                String path = (String) element;
                try {
                    image = new Image(display, path);
                } catch (IllegalArgumentException e) {
                } catch (SWTException e) {
                } catch (SWTError e) {
                }
                if (image != null) {
                    Image filled = createFilledImage(display, image,
                            FRAME_IMAGE_SIZE);
                    if (filled != null) {
                        image.dispose();
                        image = filled;
                    }
                }
            }
            if (image != null) {
                cacheWallpaperPreviewImage(element, image);
            }
        }
        return image;
    }

    private void cacheWallpaperPreviewImage(Object element, Image image) {
        if (wallpaperPreviewImages == null)
            wallpaperPreviewImages = new HashMap<Object, Image>();
        wallpaperPreviewImages.put(element, image);
    }

    private void releaseImages() {
        if (selectWallpaperImage != null) {
            selectWallpaperImage.dispose();
            selectWallpaperImage = null;
        }
        if (opacityImage != null) {
            opacityImage.dispose();
            opacityImage = null;
        }
        releaseWallpaperPreviewImages();
    }

    private void releaseWallpaperPreviewImages() {
        if (wallpaperPreviewImages != null) {
            for (Image image : wallpaperPreviewImages.values()) {
                image.dispose();
            }
            wallpaperPreviewImages = null;
        }
    }

    public void setFocus() {
        if (selectWallpaperWidget != null
                && !selectWallpaperWidget.getControl().isDisposed()) {
            selectWallpaperWidget.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        selectWallpaperDialog = null;
        selectWallpaperWidget = null;
        removeWallpaperAction = null;
        opacityGroup = null;
        opacityInput = null;
        opacityScale = null;
    }

    protected void doRefresh() {
        String value = getStyleValue(Styles.Background, null);
        boolean hasWallpaper = value != null;
        if (removeWallpaperAction != null)
            removeWallpaperAction.setEnabled(hasWallpaper);
        if (opacityGroup != null && !opacityGroup.isDisposed())
            Utils.setAllEnabled(opacityGroup, hasWallpaper);
        updateOpacityToWidget();
    }

    private void updateOpacityToWidget() {
        String value = getStyleValue(Styles.Opacity, null);
        double doubleValue = parseOpacityValue(value);
        doubleValue = Math.max(0, Math.min(1, doubleValue));
        if (opacityInput != null && !opacityInput.isDisposed()) {
            opacityInput.setText(String.valueOf((int) Math
                    .round(doubleValue * 100)));
        }
        if (opacityScale != null && !opacityScale.getControl().isDisposed()) {
            opacityScale.setSelection(new StructuredSelection(Double
                    .valueOf(doubleValue)));
        }
    }

    private double parseOpacityValue(String value) {
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
            }
        }
        return 1;
    }

    private void openSelectWallpaperDialog() {
        if (selectWallpaperWidget != null
                && selectWallpaperWidget.getControl() != null
                && !selectWallpaperWidget.getControl().isDisposed()) {
            if (selectWallpaperDialog == null) {
                Control handle = selectWallpaperWidget.getControl();
                selectWallpaperDialog = new SelectWallpaperDialog(handle
                        .getShell(), handle);
            }
            selectWallpaperDialog.open();
            Shell shell = selectWallpaperDialog.getShell();
            if (shell != null && !shell.isDisposed()) {
                selectWallpaperWidget.setForceFocus(true);
                shell.addListener(SWT.Dispose, new Listener() {
                    public void handleEvent(Event event) {
                        if (selectWallpaperWidget != null
                                && !selectWallpaperWidget.getControl()
                                        .isDisposed()) {
                            selectWallpaperWidget.setForceFocus(false);
                        }
                    }
                });
            }
        }
    }

    private void openLocalImageFileDialog() {
        if (selectWallpaperWidget == null
                || selectWallpaperWidget.getControl() == null
                || selectWallpaperWidget.getControl().isDisposed())
            return;

        FileDialog dialog = new FileDialog(selectWallpaperWidget.getControl()
                .getShell(), SWT.OPEN | SWT.SINGLE);
        DialogUtils.makeDefaultImageSelectorDialog(dialog, true);
        dialog.setFilterPath(getWallpapersPath());
        dialog.setText(PropertyMessages.WallpaperDialog_title);
        String path = dialog.open();
        if (path != null) {
            changeWallpaper(path);
        }
    }

    private void changeWallpaper(String path) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyWallpaper);
        addStyle(request, Styles.Background, path);
        sendRequest(request);
    }

    private void removeWallpaper() {
        Request request = createStyleRequest(CommandMessages.Command_ModifyWallpaper);
        addStyle(request, Styles.Background, null);
        sendRequest(request);
    }

    private void changeOpacity(int opacityValue) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyWallpaperOpacity);
        String value = String.valueOf((double) opacityValue * 1.0 / 100);
        addStyle(request, Styles.Opacity, value);
        sendRequest(request);
    }

    private void previewOpacity(int value) {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null) {
            Layer layer = viewer.getLayer(GEF.LAYER_BACKGROUND);
            if (layer != null && layer instanceof IUseTransparency) {
                int alpha = value * 255 / 100;
                ((IUseTransparency) layer).setSubAlpha(alpha);
            }
        }
    }

    private static String getWallpapersPath() {
        if (WallpapersPath == null) {
            WallpapersPath = createWallpapersPath();
        }
        return WallpapersPath;
    }

    private static String createWallpapersPath() {
        URL url = FileLocator.find(Platform.getBundle(MindMapUI.PLUGIN_ID),
                new Path("wallpaper"), null); //$NON-NLS-1$
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
        }
        String path = url.getFile();
        if ("".equals(path)) { //$NON-NLS-1$
            path = new File(System.getProperty("user.home"), "Pictures") //$NON-NLS-1$ //$NON-NLS-2$
                    .getAbsolutePath();
        }
        return path;
    }

    private static Image createFilledImage(Display display, Image src,
            Dimension size) {
        int height = size.height;
        int width = size.width;

        ImageData srcData = src.getImageData();
        int srcWidth = srcData.width;
        int srcHeight = srcData.height;

        if (srcWidth == width && srcHeight == height)
            return null;

        ImageData destData = new ImageData(width, height, srcData.depth,
                srcData.palette);
        destData.type = srcData.type;
        destData.transparentPixel = srcData.transparentPixel;
        destData.alpha = srcData.alpha;

        if (srcData.transparentPixel != -1) {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    destData.setPixel(x, y, srcData.transparentPixel);
        } else {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    destData.setAlpha(x, y, 0);
        }

        int[] pixels = new int[srcWidth];
        byte[] alphas = null;
        for (int startX = 0; startX < width; startX += srcWidth) {
            int length = Math.min(srcWidth, width - startX);
            if (length > 0) {
                for (int startY = 0; startY < height; startY += srcHeight) {
                    for (int y = 0; y < srcHeight && startY + y < height; y++) {
                        srcData.getPixels(0, y, srcWidth, pixels, 0);
                        destData.setPixels(startX, startY + y, length, pixels,
                                0);
                        if (srcData.alpha == -1 && srcData.alphaData != null) {
                            if (alphas == null)
                                alphas = new byte[srcWidth];
                            srcData.getAlphas(0, y, srcWidth, alphas, 0);
                        } else if (srcData.alpha != -1 && alphas == null) {
                            alphas = new byte[srcWidth];
                            for (int i = 0; i < alphas.length; i++)
                                alphas[i] = (byte) srcData.alpha;
                        } else if (alphas == null) {
                            alphas = new byte[srcWidth];
                            for (int i = 0; i < alphas.length; i++)
                                alphas[i] = (byte) 0xff;
                        }
                        destData.setAlphas(startX, startY + y, length, alphas,
                                0);
                    }
                }
            }
        }

        Image image = new Image(display, destData);
        return image;
    }

}