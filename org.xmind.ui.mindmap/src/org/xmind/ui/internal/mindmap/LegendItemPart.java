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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.marker.IMarker;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.ui.internal.decorators.LegendItemDecorator;
import org.xmind.ui.internal.figures.LegendItemFigure;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class LegendItemPart extends MindMapPartBase implements ILegendItemPart {

    private ImageDescriptor icon = null;

    private Image image = null;

    private boolean deactivated = false;

    public LegendItemPart() {
        setDecorator(LegendItemDecorator.getInstance());
    }

    protected IFigure createFigure() {
        return new LegendItemFigure();
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof LegendPart) {
            LegendPart legend = (LegendPart) getParent();
            legend.removeItem(this);
        }
        super.setParent(parent);
        if (getParent() instanceof LegendPart) {
            LegendPart legend = (LegendPart) getParent();
            legend.addItem(this);
        }
    }

    public ILegendPart getOwnedLegend() {
        if (getParent() instanceof ILegendPart)
            return (ILegendPart) getParent();
        return null;
    }

    public LegendItem getItem() {
        return (LegendItem) super.getModel();
    }

    public String getMarkerId() {
        return getItem().getMarkerId();
    }

    public String getDescription() {
        return getItem().getDescription();
    }

    public Image getIconImage() {
        if (image == null && !deactivated) {
            image = createImage();
        }
        return image;
    }

    private Image createImage() {
        if (icon == null) {
            IMarker marker = getItem().getMarker();
            if (marker == null)
                return null;
            icon = MarkerImageDescriptor.createFromMarker(marker);
        }
        if (icon == null)
            return null;
        return icon.createImage(false);
    }

    protected void onDeactivated() {
        deactivated = true;
        if (image != null) {
            image.dispose();
            image = null;
        }
        super.onDeactivated();
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_LEGEND_MOVABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EDITABLE, NullEditPolicy
                .getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_MODIFIABLE,
                MindMapUI.POLICY_LEGEND_ITEM_MODIFIABLE);
    }
}