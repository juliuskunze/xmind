package org.xmind.ui.internal.dialogs;

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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.ISheet;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.properties.PropertyMessages;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.ImageFormat;

public class WallpaperDialog extends PopupDialog implements IOpenListener {

    private static final String LOCAL_WALLPAPER_DIALOG_PATH = "org.xmind.ui.localWallpaperDialogPath"; //$NON-NLS-1$
    private static final Dimension FRAME_IMAGE_SIZE = new Dimension(64, 64);

    private class WallpaperLabelProvider extends LabelProvider {

        public Image getImage(Object element) {
            return getWallpaperPreviewImage(element);
        }

    }

    private static String WallpapersPath = null;

    private Control initLocationControl;

    private IGraphicalViewer mindMapViewer;

    private GalleryViewer viewer;

    private Map<Object, Image> wallpaperPreviewImages;

    private Job imageLoader;

    private List<String> allImageFiles;

    private List<String> loadedImageFiles;

    private String selectedWallpaperPath;

    public WallpaperDialog(Shell parent, Control initLocationControl) {
        super(parent, SWT.RESIZE, true, true, true, false, false, null, null);
        this.initLocationControl = initLocationControl;
        initLocationControl.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                releaseWallpaperPreviewImages();
            }
        });
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
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_FILL, 1, 1,
                new Insets(5)));
        properties.set(GalleryViewer.FrameContentSize, new Dimension(48, 48));
        properties
                .set(GalleryViewer.TitlePlacement, GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.TRUE);
        properties.set(GalleryViewer.HideTitle, Boolean.TRUE);
        properties.set(GalleryViewer.SolidFrames, Boolean.FALSE);

        viewer.setLabelProvider(new WallpaperLabelProvider());
        viewer.addOpenListener(this);

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        viewer.setEditDomain(editDomain);

        viewer.createControl(composite);
        GridData galleryData = new GridData(GridData.FILL, GridData.FILL, true,
                true);
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
        bottom.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));

        Button chooseFromLocalButton = new Button(bottom, SWT.PUSH);
        chooseFromLocalButton.setText(PropertyMessages.LocalImage_text);
        chooseFromLocalButton.setLayoutData(new GridData(GridData.CENTER,
                GridData.CENTER, true, false));
        chooseFromLocalButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                openLocalImageFileDialog();
                Shell shell = getShell();
                if (shell != null && !shell.isDisposed())
                    shell.close();
                close();
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
                    if (loadedImageFiles == null || !loadedImageFiles.isEmpty())
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
                    ImageFormat format = ImageFormat.findByExtension(ext, null);
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

    public void setMindMapViewer(IGraphicalViewer viewer) {
        this.mindMapViewer = viewer;
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
        if (initLocationControl != null && !initLocationControl.isDisposed()) {
            Point loc = initLocationControl.toDisplay(initLocationControl
                    .getLocation());
            return new Point(loc.x, loc.y
                    + initLocationControl.getBounds().height);
        } else if (mindMapViewer != null) {
            return new Point(50, 50);
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
            selectedWallpaperPath = path;
            Shell shell = getShell();
            if (shell != null && !shell.isDisposed())
                shell.close();
            close();
            changeWallpaper(path);
        }
    }

    public String getSelectedWallpaperPath() {
        return selectedWallpaperPath;
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

    private void releaseWallpaperPreviewImages() {
        if (wallpaperPreviewImages != null) {
            for (Image image : wallpaperPreviewImages.values()) {
                image.dispose();
            }
            wallpaperPreviewImages = null;
        }
    }

    private void openLocalImageFileDialog() {

        FileDialog dialog = new FileDialog(getParentShell(), SWT.OPEN
                | SWT.SINGLE);
        DialogUtils.makeDefaultImageSelectorDialog(dialog, true);
        dialog.setText(PropertyMessages.WallpaperDialog_title);

        IDialogSettings settings = MindMapUIPlugin.getDefault()
                .getDialogSettings();
        String filterPath = settings.get(LOCAL_WALLPAPER_DIALOG_PATH);
        if (filterPath == null || "".equals(filterPath) //$NON-NLS-1$
                || !new File(filterPath).exists()) {
            filterPath = getWallpapersPath();
        }
        dialog.setFilterPath(filterPath);
        String path = dialog.open();
        if (path == null)
            return;

        selectedWallpaperPath = path;

        filterPath = new File(path).getParent();
        settings.put(LOCAL_WALLPAPER_DIALOG_PATH, filterPath);
        changeWallpaper(path);
    }

    private void changeWallpaper(String path) {
        if (mindMapViewer == null)
            return;
        Request request = new Request(MindMapUI.REQ_MODIFY_STYLE)
                .setViewer(mindMapViewer);
        request.setParameter(MindMapUI.PARAM_COMMAND_LABEL,
                CommandMessages.Command_ModifySheetBackgroundColor);
        request.setParameter(MindMapUI.PARAM_STYLE_PREFIX + Styles.Background,
                path);
        request.setTargets(fillParts());
        mindMapViewer.getEditDomain().handleRequest(request);
    }

    private List<IPart> fillParts() {
        List<IPart> parts = new ArrayList<IPart>();
        Object input = mindMapViewer.getInput();
        if (input instanceof IMindMap) {
            ISheet sheet = ((IMindMap) input).getSheet();
            IPart part = mindMapViewer.findPart(sheet);
            parts.add(part);
        }
        return parts;
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