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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IImageRegistryService;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class MarkerPart extends MindMapPartBase implements IMarkerPart {

    private ImageDescriptor imgDesc = null;

    private Image image = null;

    private boolean imageCreatable = true;

    private boolean imageNeedsDispose = false;

    public MarkerPart() {
        setDecorator(MarkerDecorator.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IMarkerRef.class))
            return getMarkerRef();
        if (adapter.isAssignableFrom(IMarker.class))
            return getMarker();
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == Image.class)
            return getImage();
        return super.getAdapter(adapter);
    }

    protected IFigure createFigure() {
        return new SizeableImageFigure();
    }

    public IMarkerRef getMarkerRef() {
        return (IMarkerRef) super.getRealModel();
    }

    protected void register() {
        registerModel(getMarkerRef());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getMarkerRef());
    }

    public IMarker getMarker() {
        IMarkerRef markerRef = getMarkerRef();
        return markerRef == null ? null : markerRef.getMarker();
    }

    public ITopic getTopic() {
        return getMarkerRef().getParent();
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).removeMarker(this);
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).addMarker(this);
        }
    }

    public Image getImage() {
        if (image == null && imageCreatable) {
            image = createImage();
        }
        return image;
    }

    private Image createImage() {
        IViewer viewer = getSite().getViewer();
        if (viewer != null) {
            IImageRegistryService service = (IImageRegistryService) viewer
                    .getService(IImageRegistryService.class);
            if (service != null) {
                return service.getImage(getImageDescriptor(), false, this);
            }
        }
        imageNeedsDispose = true;
        return getImageDescriptor().createImage(false, Display.getCurrent());
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
            image = null;
        }
        super.onDeactivated();
    }

    private ImageDescriptor getImageDescriptor() {
        if (imgDesc == null) {
            imgDesc = MarkerImageDescriptor.createFromMarkerRef(getMarkerRef());
        }
        return imgDesc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.part.GraphicalEditPart#containsPoint(org.eclipse.draw2d
     * .geometry.Point)
     */
    @Override
    public boolean containsPoint(Point position) {
        return super.containsPoint(position);
    }

    @Override
    protected void updateView() {
        updateToolTip();
        super.updateView();
    }

    public IFigure findTooltipAt(Point position) {
        if (containsPoint(position)) {
            IFigure toolTip = getFigure().getToolTip();
            if (toolTip != null)
                return toolTip;
            return new Label(getMarkerRef().getDescription());
        }
        return null;
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_MARKER_MOVABLE);
    }

    protected IFeedback createFeedback() {
        return new SimpleSelectionFeedback(this);
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new SelectionFeedbackHelper();
    }

}