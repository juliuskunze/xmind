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
package org.xmind.ui.internal.fishbone.structures;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.IRotatableReferencedFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.HorizontalFlipper;
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionLine.LineType;
import org.xmind.gef.draw2d.geometry.PrecisionLine.Side;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

public class SubFishboneStructure extends AbstractBranchStructure {

    private ISubDirection direction;

    private ITransformer t = new HorizontalFlipper();

    public SubFishboneStructure(ISubDirection direction) {
        this.direction = direction;
        this.t.setEnabled(false);
    }

    private double getPadding(IBranchPart branch) {
        return getCastedData(branch).getPadding();
    }

    public void fillLayoutData(IBranchPart branch, ReferencedLayoutData data) {
        super.fillLayoutData(branch, data);
        data.addMargins(new Insets((int) Math.ceil(getPadding(branch))));
    }

    protected void doFillPlusMinus(IBranchPart branch,
            IPlusMinusPart plusMinus, LayoutInfo info) {
        Point ref = info.getReference();
        SubFishboneData fd = getCastedData(branch);
        fd.r1.setOrigin(ref.x, ref.y);
        IFigure figure = plusMinus.getFigure();
        Dimension size = figure.getPreferredSize();
        PrecisionRectangle topicBounds = getNormalTopicBounds(branch, ref);
        int orientation = getSourceOrientation(branch);
        double halfWidth = size.width * 0.5d;
        double halfHeight = size.height * 0.5d;
        double centerX = orientation == PositionConstants.WEST ? topicBounds.x
                - halfWidth : topicBounds.right() + halfWidth;
        PrecisionPoint center = fd.r1.tp(new PrecisionPoint(centerX, ref.y));
        Point loc = center.translate(-halfWidth, -halfHeight).toDraw2DPoint();
        info.put(figure, new Rectangle(loc, size));
    }

    private PrecisionRectangle getNormalTopicBounds(IBranchPart branch,
            Point ref) {
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            IFigure figure = topic.getFigure();
            if (figure instanceof IRotatableReferencedFigure)
                return ((IRotatableReferencedFigure) figure)
                        .getNormalPreferredBounds(ref);
        }
        return new PrecisionRectangle(ref.x, ref.y, 0, 0);
    }

    @Override
    protected BoundaryLayoutHelper getBoundaryLayoutHelper(IBranchPart branch) {
        return super.getBoundaryLayoutHelper(branch);
    }

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {

        Point ref = info.getReference();
        FishboneData fd = getCastedData(branch).getFishboneData();
        for (IBranchPart subBranch : subBranches) {
            IFigure figure = subBranch.getFigure();
            Rectangle rect = fd.getChildPrefBounds(subBranch,
                    new PrecisionPoint(ref));

            if (rect != null)
                info.put(figure, rect);
        }
    }

    protected Object createStructureData(IBranchPart branch) {
        return new SubFishboneData(branch, direction);
    }

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof SubFishboneData);
    }

    protected SubFishboneData getCastedData(IBranchPart branch) {
        return (SubFishboneData) super.getStructureData(branch);
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        PrecisionLine boneLine = getBoneLine(branch);
        PrecisionPoint source = calcSourceLocation(branch, boneLine);
        PrecisionLine sourceRay = getBoneRay(source);
        List<IBranchPart> subBranches = branch.getSubBranches();
        boolean folded = branch.isFolded();
//        PrecisionPoint offset = calcChildOffset(branch, key.getFeedback(),
//                boneLine, sourceRay);
        PrecisionLine childBoneLine = getChildBoneLine(branch, key
                .getFeedback());
        double offset = calcChildOffset(boneLine, sourceRay, childBoneLine);
        if (offset > 0) {
            double range;
            if (!subBranches.isEmpty() && !folded) {
                int lastIndex = direction.isChildrenTraverseReversed() ? 0
                        : subBranches.size() - 1;
                IBranchPart lastChild = subBranches.get(lastIndex);
//                PrecisionPoint lastOffset = calcChildOffset(branch, lastChild,
//                        boneLine, sourceRay);
                double lastOffset = calcChildOffset(boneLine, sourceRay,
                        getChildBoneLine(branch, lastChild));
                range = lastOffset + MindMapUI.SEARCH_RANGE;
            } else {
                range = MindMapUI.SEARCH_RANGE;
            }
            if (offset < range) {
                double distance = calcChildDistance(boneLine, childBoneLine);
                if (distance > 0 && distance < MindMapUI.SEARCH_RANGE)
                    return Math.max(1, (int) distance);
            }
        }
        return super.calcChildDistance(branch, key);
    }

    private double calcChildDistance(PrecisionLine boneLine,
            PrecisionLine childLine) {
        PrecisionPoint target = childLine.getOrigin();
        Side childSide = boneLine.getSide(target);
        if (needsCalcChildDistance(childSide))
            return Geometry.getDistance(target, boneLine);
        return -1;
    }

    private double calcChildOffset(PrecisionLine boneLine,
            PrecisionLine sourceRay, PrecisionLine childLine) {
        PrecisionPoint joint = boneLine.intersect(childLine);
        PrecisionDimension offset = joint.getDifference(sourceRay.getOrigin());
        double off = offset.getDiagonal();
        if (!sourceRay.contains(joint)) {
            off = -off;
        }
        return off;
    }

    private boolean needsCalcChildDistance(Side childSide) {
        if (childSide == Side.Right)
            return direction == ISubDirection.NER
                    || direction == ISubDirection.SE
                    || direction == ISubDirection.NW
                    || direction == ISubDirection.SWR;
        if (childSide == Side.Left)
            return direction == ISubDirection.NE
                    || direction == ISubDirection.SER
                    || direction == ISubDirection.NWR
                    || direction == ISubDirection.SW;
        return false;
    }

//    private PrecisionPoint calcChildOffset(IBranchPart branch,
//            IBranchPart child, PrecisionPoint source) {
//        
//        PrecisionPoint target = calcChildTargetLocation(branch, child, source);
//        PrecisionDimension d = target.getDifference(source);
//        SubFishboneStructureData fd = getCastedData(branch);
//        double w = d.height * fd.r1.cos() / fd.r1.sin();
//        double jointOffset = d.width - w;
//        if (direction.isRightHeaded())
//            jointOffset = -jointOffset;
//        double distance = direction.isDownwards() ? d.height : -d.height;
//        PrecisionPoint offset = new PrecisionPoint(jointOffset, distance);
//        return direction.isRotated() ? offset.transpose() : offset;
//    }

    private PrecisionLine getBoneLine(IBranchPart branch) {
        IAnchor anchor = ((INodePart) branch.getTopicPart())
                .getSourceAnchor(branch);
        int orientation = getSourceOrientation(branch);
        PrecisionPoint p1 = anchor.getLocation(Geometry
                .getOppositePosition(orientation), 0);
        PrecisionPoint p2 = anchor.getLocation(orientation, 0);
        return new PrecisionLine(p1, p2, LineType.Line);
    }

    private PrecisionLine getChildBoneLine(IBranchPart branch, IBranchPart child) {
        IAnchor anchor = ((INodePart) child.getTopicPart())
                .getTargetAnchor(branch);
        int orientation = getChildTargetOrientation(branch, child);
        PrecisionPoint p1 = anchor.getLocation(orientation, 0);
        double angle = direction.getSubDirection().getRotateAngle();
        PrecisionPoint p2 = p1.getMoved(Math.toRadians(angle), 100);
        return new PrecisionLine(p1, p2, LineType.Line);
    }

    private PrecisionPoint calcSourceLocation(IBranchPart branch,
            PrecisionLine boneLine) {
        if (!branch.getSubBranches().isEmpty() && !branch.isFolded()
                && direction.isRotated()) {
            PrecisionPoint ref = new PrecisionPoint(((IReferencedFigure) branch
                    .getTopicPart().getFigure()).getReference());
            PrecisionPoint p = ref.getMoved(Math.toRadians(-direction
                    .getRotateAngle()), 100);
            return boneLine.intersect(new PrecisionLine(ref, p, LineType.Line));
        }
        return ((INodePart) branch.getTopicPart()).getSourceAnchor(branch)
                .getLocation(getSourceOrientation(branch), 0);
    }

    private PrecisionLine getBoneRay(PrecisionPoint p) {
        double angle = direction.getRotateAngle();
        if (direction == ISubDirection.SER || direction == ISubDirection.NWR
                || direction == ISubDirection.NW
                || direction == ISubDirection.SW) {
            angle = 180 + angle;
        }
        return new PrecisionLine(p, p.getMoved(Math.toRadians(angle), 100),
                LineType.Ray);
    }

    public int calcChildIndex(IBranchPart branch, ParentSearchKey key) {
        return calcInsIndex(branch, key.getFeedback(), false);
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return new Insertion(branch, calcInsIndex(branch, key.getFeedback(),
                true), key.getFigure().getSize());
    }

    private int calcInsIndex(IBranchPart branch, IBranchPart child,
            boolean withDisabled) {
        if (branch.getSubBranches().isEmpty() || branch.isFolded())
            return withDisabled ? 0 : -1;

        PrecisionLine boneLine = getBoneLine(branch);
        PrecisionPoint source = calcSourceLocation(branch, boneLine);
        PrecisionLine sourceRay = getBoneRay(source);
        double offset = calcChildOffset(boneLine, sourceRay, getChildBoneLine(
                branch, child));
        boolean reversed = direction.isChildrenTraverseReversed();
        List<IBranchPart> subBranches = branch.getSubBranches();
        int num = subBranches.size();
        int ret = 0;
        for (IBranchPart subBranch : subBranches) {
            double subOffset = calcChildOffset(boneLine, sourceRay,
                    getChildBoneLine(branch, subBranch));
            if (reversed) {
                if (offset > subOffset)
                    return ret;
            } else {
                if (offset < subOffset)
                    return ret;
            }
            if (withDisabled || subBranch.getFigure().isEnabled()) {
                ret++;
            }
        }
        return withDisabled ? num : -1;
    }

//    private PrecisionPoint calcSourceLocation(IBranchPart branch) {
////        return new PrecisionPoint(((IReferencedFigure) branch.getTopicPart()
////                .getFigure()).getReference());
//        return ((INodePart) branch.getTopicPart()).getSourceAnchor(branch)
//                .getLocation(calcSourceOrientation(branch), 0);
//    }
//
//    private PrecisionPoint calcChildTargetLocation(IBranchPart branch,
//            IBranchPart child, PrecisionPoint source) {
//        ITopicPart topic = child.getTopicPart();
//        if (topic instanceof INodePart) {
//            IAnchor anchor = ((INodePart) topic).getTargetAnchor(branch);
//            if (anchor != null) {
//                return anchor.getLocation(calcChildTargetOrientation(branch,
//                        child), 0);
//            }
//        }
//        return new PrecisionPoint(getReference(child));
//    }

//    private Point getReference(IBranchPart branch) {
//        ITopicPart topic = branch.getTopicPart();
//        if (topic != null)
//            return ((IReferencedFigure) topic.getFigure()).getReference();
//        return ((IReferencedFigure) branch.getFigure()).getReference();
//    }

//    public Object calcNavigation(IBranchPart branch, int direction) {
//        int nav = this.direction.calcNavigation(direction);
//        if (nav == GEF.NAVI_PREV && branch.getBranchIndex() == 0)
//            nav = GEF.NAVI_PARENT;
//        return nav;
//    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (direction.isRotated()) {
            if (GEF.REQ_NAV_UP.equals(navReqType)) {
                return getSubTopicPart(branch, sourceChild.getBranchIndex() - 1);
            } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
                return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
            } else if (!sequential) {
                if (direction.isRightHeaded()) {
                    if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                        return branch.getTopicPart();
                    }
                } else {
                    if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                        return branch.getTopicPart();
                    }
                }
            }
        } else {
            String prevType = direction.isRightHeaded() ? GEF.REQ_NAV_RIGHT
                    : GEF.REQ_NAV_LEFT;
            String nextType = direction.isRightHeaded() ? GEF.REQ_NAV_LEFT
                    : GEF.REQ_NAV_RIGHT;
            if (prevType.equals(navReqType)) {
                ITopicPart prev = getSubTopicPart(branch, sourceChild
                        .getBranchIndex() - 1);
                if (prev == null && !sequential)
                    return branch.getTopicPart();
                return prev;
            } else if (nextType.equals(navReqType)) {
                return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (isNavChild(branch, navReqType)) {
            if (direction.isChildrenTraverseReversed())
                return getSubTopicPart(branch,
                        branch.getSubBranches().size() - 1);
            return getSubTopicPart(branch, 0);
        }
        return super.calcNavigation(branch, navReqType);
    }

    private boolean isNavChild(IBranchPart branch, String navReqType) {
        if (direction.isRotated()) {
            if (direction.isDownwards()) {
                if (GEF.REQ_NAV_DOWN.equals(navReqType))
                    return true;
            } else {
                if (GEF.REQ_NAV_UP.equals(navReqType))
                    return true;
            }
        }
        if (direction.isRightHeaded())
            return GEF.REQ_NAV_LEFT.equals(navReqType);
        return GEF.REQ_NAV_RIGHT.equals(navReqType);
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        if (!branch.getSubBranches().contains(subBranch)) {
            return direction.isRightHeaded() ? PositionConstants.EAST
                    : PositionConstants.WEST;
        }
        return direction.getChildTargetOrientation();
    }

    public int getSourceOrientation(IBranchPart branch) {
        return direction.getSourceOrientation();
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return getRangeGrowthDirection();
    }

    private int getRangeGrowthDirection() {
        if (direction.isRotated())
            return PositionConstants.SOUTH;
        return direction.isRightHeaded() ? PositionConstants.EAST
                : PositionConstants.WEST;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        if (direction.isRotated())
            return direction.isRightHeaded() ? PositionConstants.EAST
                    : PositionConstants.EAST;
        return direction.isDownwards() ? PositionConstants.SOUTH
                : PositionConstants.NORTH;
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        int rangeGrowthDirection = getRangeGrowthDirection();
        if (direction == rangeGrowthDirection)
            return 1;
        if (direction == Geometry.getOppositePosition(rangeGrowthDirection))
            return -1;
        return super.getQuickMoveOffset(branch, child, direction);
    }
}