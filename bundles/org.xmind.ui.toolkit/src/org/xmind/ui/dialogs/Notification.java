package org.xmind.ui.dialogs;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.viewers.ImageButton;

public class Notification extends SmoothPopupDialog {

    private static ImageDescriptor IMG_CLOSE_HOVER = null;

    private IAction leftAction;

    private IAction rightAction;

    private String infoText;

    private Shell sourceShell;

    private Control closeButton;

    public Notification(Shell parent, String title, String infoText,
            IAction leftAction, IAction rightAction) {
        super(parent, true, title);
        this.sourceShell = parent;
        this.infoText = infoText;
        this.leftAction = leftAction;
        this.rightAction = rightAction;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setForeground(parent.getForeground());
        composite.setBackground(parent.getBackground());
        GridLayout layout = ((GridLayout) composite.getLayout());
        layout.horizontalSpacing = 20;
        layout.marginLeft = 10;
        layout.marginWidth = 20;
        layout.marginHeight = 0;
        layout.marginBottom = 10;
        layout.numColumns = leftAction != null || rightAction != null ? 3 : 2;
        layout.makeColumnsEqualWidth = false;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                true, true));

        createImageSection(composite);
        createInfoSection(composite);
        createButtonSection(composite);

        return composite;
    }

    private void createImageSection(Composite parent) {
        Image image = null;
        final Image imageToDispose;
        if (leftAction != null) {
            ImageDescriptor icon = leftAction.getImageDescriptor();
            if (icon != null) {
                image = icon.createImage(false);
                imageToDispose = image;
            } else {
                imageToDispose = null;
            }
        } else {
            imageToDispose = null;
        }
        if (image == null && sourceShell != null && !sourceShell.isDisposed()) {
            image = findBrandingImage(sourceShell.getImage(),
                    sourceShell.getImages());
        }
        if (image != null) {
            Label iconLabel = new Label(parent, SWT.CENTER);
            iconLabel.setBackground(parent.getBackground());
            iconLabel.setForeground(parent.getForeground());
            iconLabel.setImage(image);

            iconLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
                    true));

            if (imageToDispose != null) {
                iconLabel.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        imageToDispose.dispose();
                    }
                });
            }
        }
    }

    private Image findBrandingImage(Image mainImage, Image[] images) {
        Image best = null;
        int scale = -1;
        Rectangle r;
        int s;
        if (mainImage != null) {
            r = mainImage.getBounds();
            s = Math.abs(r.width - 48) * Math.abs(r.height - 48);
            if (scale < 0 || s < scale) {
                best = mainImage;
                scale = s;
            }
        }
        for (Image img : images) {
            r = img.getBounds();
            s = Math.abs(r.width - 48) * Math.abs(r.height - 48);
            if (scale < 0 || s < scale) {
                best = img;
                scale = s;
            }
        }
        return best;
    }

    private void createInfoSection(Composite parent) {
        StyledLink link;
        String content = infoText;
        if (content.indexOf("<form>") >= 0) { //$NON-NLS-1$
            link = new StyledLink(parent, SWT.NONE);
        } else {
            link = new StyledLink(parent, SWT.SIMPLE);
        }
        link.setText(content);
        link.setBackground(parent.getBackground());
        link.setForeground(parent.getForeground());
//        link.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
//                Util.isMac() ? -2 : -1));
        link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (leftAction == null)
            link.setEnabled(false);
        final IAction theAction = this.leftAction;
        link.addHyperlinkListener(new IHyperlinkListener() {
            public void linkExited(HyperlinkEvent e) {
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        if (theAction != null)
                            theAction.run();
                    }
                });
            }
        });
    }

    private void createButtonSection(Composite parent) {
        boolean hasLeftAction = leftAction != null;
        boolean hasRightAction = rightAction != null;
        if (hasLeftAction || hasRightAction) {
            Composite actionBar = new Composite(parent, SWT.NO_FOCUS);
            actionBar.setForeground(parent.getForeground());
            actionBar.setBackground(parent.getBackground());
            int numColumns = hasLeftAction && hasRightAction ? 2 : 1;
            GridLayout buttonBarLayout = new GridLayout(numColumns, false);
            buttonBarLayout.marginWidth = 0;
            buttonBarLayout.marginHeight = 0;
            buttonBarLayout.horizontalSpacing = 10;
            actionBar.setLayout(buttonBarLayout);
            actionBar.setLayoutData(new GridData(GridData.BEGINNING,
                    GridData.CENTER, false, false));

            if (hasLeftAction) {
                Control left = createActionBar(actionBar, leftAction);
                GridData leftLayoutData = new GridData(SWT.BEGINNING,
                        SWT.CENTER, true, true);
                left.setLayoutData(leftLayoutData);
            }

            if (hasRightAction) {
                Control right = createActionBar(actionBar, rightAction);
                GridData rightLayoutData = new GridData(SWT.END, SWT.END, true,
                        true);
                right.setLayoutData(rightLayoutData);
            }
        }

    }

    private Control createActionBar(Composite parent, final IAction action) {
        ImageButton actionBar = new ImageButton(parent, SWT.NONE);
        actionBar.setNormalImageDescriptor(createNormalImageFrom(action
                .getText()));
        actionBar.setHoveredImageDescriptor(createHoverImageFrom(action
                .getText()));
        actionBar.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                close();
                action.run();
            }
        });
        return actionBar.getControl();
    }

    public static void setHoverCloseButtonImage(ImageDescriptor img) {
        IMG_CLOSE_HOVER = img;
    }

    @Override
    protected ImageDescriptor getHoverCloseButtonImage() {
        if (IMG_CLOSE_HOVER == null) {
            IMG_CLOSE_HOVER = createHoverCloseButtonImage();
        }
        return IMG_CLOSE_HOVER;
    }

    private ImageDescriptor createHoverCloseButtonImage() {
        Display display = Display.getCurrent();
        Image img = new Image(display, 16, 16);
        GC gc = new GC(img);
        gc.setBackground(ColorUtils.getColor(DEFAULT_BACKGROUDCOLOR_VALUE));
        gc.fillRectangle(0, 0, 16, 16);
        gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
        gc.setLineWidth(2);
        gc.drawLine(4, 4, 11, 11);
        gc.drawLine(4, 11, 11, 4);
        gc.dispose();
        ImageData data = img.getImageData();
        img.dispose();
        return ImageDescriptor.createFromImageData(data);
    }

    private ImageDescriptor createNormalImageFrom(String content) {
        Display display = Display.getCurrent();
        GC displayGC = new GC(display);
        displayGC.setTextAntialias(SWT.ON);
        displayGC.setAntialias(SWT.ON);
        Point size = displayGC.textExtent(content);
        displayGC.dispose();

        Image img = new Image(display, size.x + 18, size.y + 8);
        GC gc = new GC(img);
        gc.setTextAntialias(SWT.ON);
        gc.setAntialias(SWT.ON);
        gc.setBackground(ColorUtils.getColor(DEFAULT_BACKGROUDCOLOR_VALUE));
        gc.fillRectangle(0, 0, size.x + 18, size.y + 8);

        gc.setBackground(ColorConstants.lightGray);
        gc.fillRoundRectangle(0, 0, size.x + 18, size.y + 8, 6, 4);

        gc.setBackground(ColorUtils.getColor(DEFAULT_BACKGROUDCOLOR_VALUE));
        gc.fillRoundRectangle(1, 1, size.x + 16, size.y + 6, 6, 4);

        gc.setForeground(ColorConstants.gray);
        gc.setLineWidth(1);
        gc.drawRoundRectangle(1, 1, size.x + 15, size.y + 5, 6, 4);
        gc.drawText(content, 9, 3);
        gc.dispose();
        ImageData data = img.getImageData();
        img.dispose();
        return ImageDescriptor.createFromImageData(data);
    }

    private ImageDescriptor createHoverImageFrom(String content) {
        Display display = Display.getCurrent();
        GC displayGC = new GC(display);
        displayGC.setTextAntialias(SWT.ON);
        displayGC.setAntialias(SWT.ON);
        Point size = displayGC.textExtent(content);
        displayGC.dispose();

        Image img = new Image(display, size.x + 18, size.y + 8);
        GC gc = new GC(img);
        gc.setTextAntialias(SWT.ON);
        gc.setAntialias(SWT.ON);
        gc.setBackground(ColorUtils.getColor(DEFAULT_BACKGROUDCOLOR_VALUE));
        gc.fillRectangle(0, 0, size.x + 18, size.y + 8);

        gc.setBackground(ColorConstants.lightGray);
        gc.fillRoundRectangle(0, 0, size.x + 18, size.y + 8, 6, 4);

        gc.setBackground(ColorUtils.getColor(DEFAULT_BACKGROUDCOLOR_VALUE));
        gc.fillRoundRectangle(1, 1, size.x + 16, size.y + 6, 6, 4);

        gc.setForeground(ColorConstants.gray);
        gc.setLineWidth(1);
        gc.drawRoundRectangle(1, 1, size.x + 15, size.y + 5, 6, 4);

        gc.setForeground(ColorConstants.darkGray);
        gc.drawText(content, 9, 3);
        gc.dispose();
        ImageData data = img.getImageData();
        img.dispose();
        return ImageDescriptor.createFromImageData(data);
    }

    @Override
    public int open() {
        return super.open();
    }

    @Override
    public void setCenterPopUp(boolean centerPopUp) {
        super.setCenterPopUp(centerPopUp);
    }

    @Override
    protected Control createCloseButton(Composite parent) {
        closeButton = super.createCloseButton(parent);
        return closeButton;
    }

    public void setCloseButtonListener(int eventType, Listener listener) {
        if (closeButton == null || closeButton.isDisposed())
            return;

        closeButton.addListener(eventType, listener);
    }
}
