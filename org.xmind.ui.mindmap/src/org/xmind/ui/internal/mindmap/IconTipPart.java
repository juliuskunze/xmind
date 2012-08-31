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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IImageRegistryService;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.ui.internal.decorators.IconTipDecorator;
import org.xmind.ui.mindmap.IIconTipPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.resources.FontUtils;

public class IconTipPart extends MindMapPartBase implements IIconTipPart,
        IPropertyChangeListener {

    private IAction action;

    private Menu menu;

    private String actionId;

    private ImageDescriptor imageDescriptor;

    private Image image;

    private boolean imageCreatable = true;

    private boolean imageNeedsDispose = false;

    public IconTipPart() {
        setDecorator(IconTipDecorator.getInstance());
    }

    protected IFigure createFigure() {
        return new SizeableImageFigure();
    }

    public IAction getAction() {
        return action;
    }

    public Image getImage() {
        if (image == null && imageCreatable) {
            image = createImage();
        }
        return image;
    }

    public SizeableImageFigure getImageFigure() {
        return (SizeableImageFigure) super.getFigure();
    }

    public Menu getPopupMenu() {
        return menu;
    }

    public ITopic getTopic() {
        return (ITopic) super.getRealModel();
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).removeIconTip(this);
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).addIconTip(this);
        }
    }

    protected void register() {
        super.register();
        if (getModel() instanceof IconTip) {
            setAction(((IconTip) getModel()).getAction());
        }
    }

    protected void unregister() {
        setAction(null);
        super.unregister();
    }

    public void setAction(IAction action) {
        if (action == this.action)
            return;

        if (this.action != null)
            this.action.removePropertyChangeListener(this);
        if (actionId != null)
            unregisterAction(actionId, this.action);

        this.action = action;

        actionId = action == null ? null : action.getId();
        if (actionId != null)
            registerAction(action);
        if (action != null)
            action.addPropertyChangeListener(this);
    }

    private void unregisterAction(String actionId, IAction action) {
        ITopicPart topicPart = getTopicPart();
        if (topicPart == null)
            return;

        IActionRegistry actionRegistry = (IActionRegistry) topicPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry == null)
            return;

        if (actionRegistry.getAction(actionId) == action) {
            actionRegistry.removeAction(actionId);
        }
    }

    private void registerAction(IAction action) {
        ITopicPart topicPart = getTopicPart();
        if (topicPart == null)
            return;

        IActionRegistry actionRegistry = (IActionRegistry) topicPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry == null)
            return;

        actionRegistry.addAction(action);
    }

    private Image createImage() {
        IViewer viewer = getSite().getViewer();
        ImageDescriptor descriptor = getImageDescriptor();
        if (viewer != null) {
            IImageRegistryService service = (IImageRegistryService) viewer
                    .getService(IImageRegistryService.class);
            if (service != null) {
                return service.getImage(descriptor, false, this);
            }
        }
        imageNeedsDispose = true;
        return descriptor == null ? null : descriptor.createImage(false,
                Display.getCurrent());
    }

    public ImageDescriptor getImageDescriptor() {
        if (imageDescriptor == null) {
            imageDescriptor = action == null ? null : action
                    .getImageDescriptor();
        }
        return imageDescriptor;
    }

    protected void onDeactivated() {
        imageCreatable = false;
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
        super.onDeactivated();
    }

    protected void updateView() {
        super.updateView();
        updateToolTip();
    }

    protected IFigure createToolTip() {
        if (action != null) {
            String text = action.getText();
            String tooltip = action.getToolTipText();
            if (text != null || tooltip != null) {
                IFigure fig = new Figure();

                fig.setBorder(new MarginBorder(1, 3, 1, 3));

                ToolbarLayout layout = new ToolbarLayout(false);
                layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
                layout.setSpacing(7);
                fig.setLayoutManager(layout);

                if (text != null) {
                    text = Action.removeAcceleratorText(text);
                    text = Action.removeMnemonics(text);
                    Label title = new Label(text);
                    title.setFont(FontUtils
                            .getBold(JFaceResources.DEFAULT_FONT));
                    fig.add(title);
                }

                if (tooltip != null) {
                    RotatableWrapLabel description = new RotatableWrapLabel(
                            tooltip, RotatableWrapLabel.NORMAL);
                    description.setTextAlignment(PositionConstants.LEFT);
                    description.setPrefWidth(Display.getCurrent()
                            .getClientArea().width / 3);
                    description.setFont(FontUtils.getRelativeHeight(
                            JFaceResources.DEFAULT_FONT, -1));
                    description.setForegroundColor(ColorConstants.gray);
                    fig.add(description);
                }

                return fig;
            }
        }
        return super.createToolTip();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == Image.class)
            return getImage();
        if (adapter == IAction.class)
            return getAction();
        if (adapter == IMenuManager.class)
            return getPopupMenu();
        return super.getAdapter(adapter);
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE, NullEditPolicy
                .getInstance());
    }

    protected IFeedback createFeedback() {
        return new SimpleSelectionFeedback(this);
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new SelectionFeedbackHelper();
    }

    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (IAction.TEXT.equals(property)
                || IAction.TOOL_TIP_TEXT.equals(property)) {
            updateToolTip();
        } else if (IAction.IMAGE.equals(property)) {
            image = null;
            imageDescriptor = null;
            update();
        }
    }

//    private ICacheManager cache = null;
//
//    private ImageDescriptor imageDescriptor = null;
//
//    private Image image = null;
//
//    private IconTipContributionDescriptor contributionDescriptor = null;
//
//    private IIconTipContribution contribution = null;
//
//    private boolean imageCreatedByContribution = false;
//
//
//    public void setContributionDescriptor(
//            IconTipContributionDescriptor contributionDescriptor) {
//        this.contributionDescriptor = contributionDescriptor;
//    }
//
//    public IconTipContributionDescriptor getContributionDescriptor() {
//        return contributionDescriptor;
//    }
//
//    public IIconTipContribution getContribution() {
//        if (contribution != null)
//            return contribution;
//
//        if (contributionDescriptor == null)
//            return NullIconTipContribution.getInstance();
//
//        contribution = contributionDescriptor.getContribution();
//        return contribution;
//    }
//
////    public IIconTipContribution getAdvisor() {
////        return contribution;
////    }
////
////    public void setAdvisor(IIconTipContribution advisor) {
////        this.contribution = advisor;
////    }
//
//
//    public void setParent(IPart parent) {
//        if (getParent() instanceof TopicPart) {
//            ((TopicPart) getParent()).removeIconTip(this);
//        }
//        super.setParent(parent);
//        if (getParent() instanceof TopicPart) {
//            ((TopicPart) getParent()).addIconTip(this);
//        }
//    }
//
//    public Object getAdapter(Class adapter) {
//        if (adapter == ITopic.class)
//            return getTopic();
//        if (adapter == Image.class)
//            return getImage();
//        if (adapter == ICacheManager.class)
//            return getCacheManager();
//        return super.getAdapter(adapter);
//    }
//
//    public Image getImage() {
//        if (image == null) {
//            image = createImage();
//        }
//        return image;
//    }
//
//    private Image createImage() {
//        return createImage(imageDescriptor);
//    }
//
//    private Image createImage(ImageDescriptor imageDescriptor) {
//        imageCreatedByContribution = false;
//        if (imageDescriptor == null)
//            return null;
//        Image img = getContribution().createImage(this, imageDescriptor);
//        if (img != null) {
//            imageCreatedByContribution = true;
//            return img;
//        }
//        return imageDescriptor.createImage(false);
//    }
//
//    public SizeableImageFigure getImageFigure() {
//        return (SizeableImageFigure) super.getFigure();
//    }
//
//    public void setImageDescriptor(ImageDescriptor image) {
//        if (image == this.imageDescriptor)
//            return;
//        this.imageDescriptor = image;
//        imageChanged();
//    }
//
//    private void imageChanged() {
//        releaseImage();
//        image = createImage();
//    }
//
//    private void releaseImage() {
//        if (image != null) {
//            releaseImage(image);
//            image = null;
//        }
//    }
//
//    private void releaseImage(Image image) {
//        if (imageCreatedByContribution) {
//            getContribution().releaseImage(this, image);
//        } else {
//            image.dispose();
//        }
//        imageCreatedByContribution = false;
//    }
//
//    public void updateView() {
//        getContribution().update(this);
//        super.updateView();
//    }
//
//    protected void onActivated() {
//        super.onActivated();
//        getContribution().activate(this);
//    }
//
//    protected void onDeactivated() {
//        releaseImage();
//        getContribution().deactivate(this);
//        super.onDeactivated();
//    }
//
////    protected void registerCoreEvents(ICoreEventSource source,
////            ICoreEventRegister register) {
////        super.registerCoreEvents(source, register);
////        String[] types = contributionDescriptor.getUpdateTypes();
////        for (String type : types) {
////            register.register(type);
////        }
//////            contribution.registerCoreEvents(this, getTopic(), register);
////    }
////
////    protected void handleModelChange(CoreEvent event) {
////        String type = event.getType();
////        String[] types = contributionDescriptor.getUpdateTypes();
////        for (String t : types) {
////            if (type.equals(t)) {
////                update();
////                return;
////            }
////        }
////        //getContribution().handleEvent(this, event);
//////        super.handleModelChange(event);
////    }
//
//    public ImageDescriptor getImageDescriptor() {
//        return imageDescriptor;
//    }
//
//
////    protected void uninstallPolicies(IRequestProcessor reqProc) {
////        reqProc.uninstallPolicy(GEF.ROLE_SELECTABLE);
////        super.uninstallPolicies(reqProc);
////    }
//
//
//    public void handleMouseClick(MouseEvent me) {
//        getContribution().handleMouseClick(this, me);
//    }
//
//    public ICacheManager getCacheManager() {
//        if (cache == null)
//            cache = new CacheManager(this);
//        return cache;
//    }
//
}