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
package org.xmind.ui.internal.mindmap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.core.Core;
import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IImageRegistryService;
import org.xmind.gef.util.GEFUtils;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class ImagePart extends MindMapPartBase implements IImagePart {

    private ImageDescriptor imageDescriptor = null;

    private Image image = null;

    private boolean imageCreatable = true;

    private boolean imageNeedsDispose = false;

    public ImagePart() {
        setDecorator(ImageDecorator.getInstance());
    }

    protected IFigure createFigure() {
        return new SizeableImageFigure();
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public void setImageDescriptor(ImageDescriptor imageDescriptor) {
        if (imageDescriptor == this.imageDescriptor)
            return;
        releaseImage();
        this.imageDescriptor = imageDescriptor;
    }

    private void releaseImage() {
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            IImageRegistryService service = (IImageRegistryService) viewer
                    .getService(IImageRegistryService.class);
            if (service != null) {
                service.decreaseRef(getImageDescriptor(), this);
            }
        }
        if (image != null && imageNeedsDispose) {
            image.dispose();
        }
        image = null;
        imageNeedsDispose = false;
    }

    public Image getImage() {
        if (image == null && imageCreatable) {
            image = createImage();
        }
        return image;
    }

    private Image createImage() {
        if (getImageDescriptor() == null)
            return null;

        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            IImageRegistryService service = (IImageRegistryService) viewer
                    .getService(IImageRegistryService.class);
            if (service != null) {
                return service.getImage(getImageDescriptor(), true, this);
            }
        }
        imageNeedsDispose = true;
        return getImageDescriptor().createImage(false, Display.getCurrent());
    }

    public SizeableImageFigure getImageFigure() {
        return (SizeableImageFigure) super.getFigure();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == ITopicPart.class)
            return getTopicPart();
        if (adapter == IImage.class)
            return getImageModel();
        if (adapter == Image.class)
            return getImage();
        return super.getAdapter(adapter);
    }

    public void setModel(Object model) {
        super.setModel(model);
        setImageDescriptor(createNewImageDescriptor());
    }

    protected void onDeactivated() {
        ImageDownloadCenter.getInstance().cancel(getTopic());
        if (updateBusyImageThread != null) {
            updateBusyImageThread.interrupt();
            updateBusyImageThread = null;
        }
        imageCreatable = false;
        getImageFigure().setImage(null);
//        depressImageCreation = true;
        releaseImage();
        super.onDeactivated();
    }

    public IImage getImageModel() {
        return (IImage) super.getRealModel();
    }

    protected void register() {
        registerModel(getImageModel());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getImageModel());
    }

    public ITopic getTopic() {
        return getImageModel().getParent();
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            TopicPart topicPart = (TopicPart) getParent();
            if (topicPart.getImagePart() == this)
                topicPart.setImagePart(null);
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            TopicPart topicPart = (TopicPart) getParent();
            topicPart.setImagePart(this);
        }
    }

    protected void registerCoreEvents(ICoreEventSource source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.ImageAlignment);
        register.register(Core.ImageHeight);
        register.register(Core.ImageSource);
        register.register(Core.ImageWidth);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.ImageAlignment.equals(type)) {
            getFigure().revalidate();
        } else if (Core.ImageHeight.equals(type)
                || Core.ImageWidth.equals(type)) {
            update();
        } else if (Core.ImageSource.equals(type)) {
            setImageDescriptor(createNewImageDescriptor());
            update();
        } else {
            super.handleCoreEvent(event);
        }
    }

    private ImageDescriptor createNewImageDescriptor() {
        if (isDownloading()) {
            return getBusyImageDescriptor(0);
        }
        if (updateBusyImageThread != null) {
            updateBusyImageThread.interrupt();
            updateBusyImageThread = null;
        }

        IImage imageModel = getImageModel();
        String source = imageModel.getSource();
        if (source != null) {
            if (HyperlinkUtils.isAttachmentURL(source)) {
                String path = HyperlinkUtils.toAttachmentPath(source);
                return AttachmentImageDescriptor.createFromEntryPath(imageModel
                        .getOwnedWorkbook(), path);
            }
        }
        return getWarningImageDescriptor();
    }

    private static List<ImageDescriptor> BusyImages = null;

    private static ImageDescriptor WarningImage = null;

    private static List<ImageDescriptor> getBusyImageDescriptors() {
        if (BusyImages == null) {
            BusyImages = findBusyImageDescriptors();
        }
        return BusyImages;
    }

    private static ImageDescriptor getWarningImageDescriptor() {
        if (WarningImage == null)
            WarningImage = ImageDescriptor.createFromImage(Display.getCurrent()
                    .getSystemImage(SWT.ICON_WARNING));
        return WarningImage;
    }

    private static List<ImageDescriptor> findBusyImageDescriptors() {
        List<ImageDescriptor> list = new ArrayList<ImageDescriptor>();
        for (int index = 1; index <= 12; index++) {
            String path = String.format("/icons/busy/busy_f%02d.gif", index); //$NON-NLS-1$
            ImageDescriptor img = AbstractUIPlugin.imageDescriptorFromPlugin(
                    "org.xmind.ui.browser", path); //$NON-NLS-1$
            if (img != null) {
                list.add(img);
            }
        }
        if (list.isEmpty()) {
            list.add(MindMapUI.getImages().get(IMindMapImages.STOP, true));
        }
        return list;
    }

    private static ImageDescriptor getBusyImageDescriptor(int index) {
        return getBusyImageDescriptors().get(index);
    }

    private Thread updateBusyImageThread;

    public boolean isDownloading() {
        return ImageDownloadCenter.getInstance().isDownloading(getTopic());
    }

    public boolean hasProblem() {
        return getImageDescriptor() != null
                && getImageDescriptor() == WarningImage;
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE, NullEditPolicy
                .getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_IMAGE_MOVABLE);
    }

    protected IFeedback createFeedback() {
        return new ImageFeedback(this);
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new ImageSelectionHelper();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.part.GraphicalEditPart#findAt(org.eclipse.draw2d.geometry
     * .Point)
     */
    @Override
    public IPart findAt(Point position) {
        if (!getStatus().isSelected()) {
            ITopicPart topicPart = getTopicPart();
            if (topicPart != null) {
                if (!topicPart.getStatus().isSelected())
                    return null;
            }
        }
        return super.findAt(position);
    }

    public boolean containsPoint(Point position) {
        return super.containsPoint(position)
                || (getSelectionOrientation(position) != PositionConstants.NONE);
    }

    public Cursor getCursor(Point pos) {
        int orientation = getSelectionOrientation(pos);
        if (orientation != PositionConstants.NONE) {
            return GEFUtils.getPositionCursor(orientation);
        }
        return super.getCursor(pos);
    }

    private int getSelectionOrientation(Point point) {
        if (getStatus().isPreSelected() || getStatus().isSelected()) {
            return ((ImageFeedback) getFeedback()).getOrientation(point);
        }
        return PositionConstants.NONE;
    }

}