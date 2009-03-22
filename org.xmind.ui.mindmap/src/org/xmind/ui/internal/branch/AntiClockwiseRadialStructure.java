package org.xmind.ui.internal.branch;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public class AntiClockwiseRadialStructure extends BaseRadialStructure {

    @Override
    public boolean isChildLeft(IBranchPart branch, IBranchPart child) {
        if (branch.isCentral()) {
            Point pos = (Point) MindMapUtils.getCache(child,
                    IBranchPart.CACHE_PREF_POSITION);
            if (pos != null) {
                return RadialUtils.isLeft(0, pos.x);
            }
        }
        if (calculatingBranches.contains(branch)) {
            // avoid recursively calling
            return false;
        }
        calculatingBranches.add(branch);
        boolean left;
        int index = branch.getSubBranches().indexOf(child);
        if (index >= 0) {
            left = !(index >= getRadialData(branch).getNumRight());
        } else if (branch.getSummaryBranches().contains(child)) {
            left = !(isSummaryChildLeft(branch, child));
        } else {
            left = RadialUtils.isLeft(getReference(branch).x,
                    getReference(child).x);
        }
        calculatingBranches.remove(branch);
        return left;
    }

    @Override
    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {

        RadialData cache = getRadialData(branch);
        int numRight = cache.getNumRight();

        int[] childrenSpacings = cache.getChildrenSpacings();
        int num = subBranches.size();
        boolean right = false;
        RadiationInsertion insertion = getCurrentInsertion(branch);
        int insHeight = insertion == null ? 0 : insertion.getSize().height;

        int y = -cache.getRightSumSpacing() / 2;
        if (insertion != null && !insertion.right) {
            y -= insHeight / 2;
        }

        Point ref = info.getReference(); // the Center Topic's location 

        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = cache.getLeftSumSpacing() / 2;
                if (insertion != null)
                    if (insertion.right) {
                        y += insHeight / 2;
                    }
                right = true;
            }

            if (insertion != null && i == insertion.getIndex()) {
                if (i != numRight || insertion.right) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils.getPrefBounds(insertion
                            .getSize(), p, right);
                    info.add(insBounds);
                    if (insertion.right)
                        y -= insHeight;
                    else
                        y += insHeight;
                }
            }

            IBranchPart subBranch = subBranches.get(i); // to obtain the i st subTopic's bracnch.
            Rectangle r;
            Dimension offset = getOffset(subBranch);

            IFigure subFigure = subBranch.getFigure(); // the SubTopic's figure
            if (offset != null && subFigure instanceof IReferencedFigure) {
                Point subRef = ref.getTranslated(offset);
                r = ((IReferencedFigure) subFigure).getPreferredBounds(subRef);

            } else {
                int x = cache.getX(y, right);
                Point subRef = ref.getTranslated(x, y);
                r = RadialUtils.getPrefBounds(subBranch, subRef, right);

            }
            info.put(subFigure, r);

            if (i < numRight)
                y += childrenSpacings[i];
            else
                y -= childrenSpacings[i];

            if (insertion != null) {
                if ((i == numRight - 1 && insertion.getIndex() == numRight && !insertion.right)
                        || i == num) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils.getPrefBounds(insertion
                            .getSize(), p, right);
                    info.add(insBounds);

                    y += insHeight;
                }
            }
        }

    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {

        List<IBranchPart> subBranches = branch.getSubBranches();
        if (subBranches.isEmpty())
            return withDisabled ? 0 : -1;

        if (branch.isFolded())
            return withDisabled ? 0 : -1;

        ITopicPart topic = branch.getTopicPart();
        if (topic == null)
            return withDisabled ? 0 : -1;

        Point childRef = key.getFigure().getReference();
        Point ref = ((IReferencedFigure) topic.getFigure()).getReference();

        RadialData cache = getRadialData(branch);
        int numRight = cache.getNumRight();

        int[] childrenSpacings = cache.getChildrenSpacings();

        int num = subBranches.size();
        boolean right = true;

        Dimension insSize = calcInsSize(branch, key);
        int insHeight = insSize.height;
        boolean insRight = calcInsSide(branch, ref, key);

        int startY = ref.y;
        int y = startY - cache.getRightSumSpacing() / 2;
        if (!insRight) {
            y -= insHeight / 2;
        }

        int ret = 0;
        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = startY + cache.getLeftSumSpacing() / 2;
                if (insRight) {
                    y += insHeight / 2;
                }
                //ret = num - 1;
                right = false;
            }

            IBranchPart subbranch = subBranches.get(i);
            IFigure subFigure = subbranch.getFigure();
            Insets refIns = RadialUtils.getRefInsets(subFigure, right);
            int hint;
            if (i < numRight) {
                hint = y - refIns.top + (refIns.getHeight() + insHeight) / 2;
            } else {
                hint = y + refIns.top - (refIns.getHeight() + insHeight) / 2;
            }
            if (i < numRight) {
                if (!insRight && childRef.y <= hint)
                    return ret;
                if (withDisabled || subFigure.isEnabled())
                    ret++;
                if (i == numRight - 1 && childRef.x < ref.x
                        && childRef.y >= hint)
                    return ret;
            } else {
                if (insRight && childRef.y > hint)//childRef.y >= hint)
                    return ret;
                if (withDisabled || subFigure.isEnabled())
                    ret++;
//                if (i == numRight && childRef.x > ref.x && childRef.y >= hint)
//                    return ret;
            }
            if (i < numRight)
                y += childrenSpacings[i];
            else
                y -= childrenSpacings[i];
        }
        return withDisabled ? num : -1;
    }

    public IPart calcChildNavigation(IBranchPart branch, // centre Topic 
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        int numRight = getRadialData(branch).getNumRight();
        int numLeft = getRadialData(branch).getNumLeft();
        int index = sourceChild.getBranchIndex();
        int num = branch.getSubBranches().size();

        if (GEF.REQ_NAV_UP.equals(navReqType)) { //UP
            if (index == 0)
                return getSubTopicPart(branch, num - 1);
            else if (index == numRight - 1)
                return getSubTopicPart(branch, index - 1);
            else if (index == numRight)
                return getSubTopicPart(branch, index + 1);
            else if (index > numRight) {
                if (index == num - 1)
                    return getSubTopicPart(branch, 0);
                else
                    return getSubTopicPart(branch, index + 1);
            } else
                return getSubTopicPart(branch, index - 1);

        } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) { // DOWN
            if (index == 0)
                return getSubTopicPart(branch, index + 1);
            else if (index == numRight - 1)
                return getSubTopicPart(branch, index + 1);
            else if (index == numRight)
                return getSubTopicPart(branch, index - 1);
            else if (index > numRight)
                return getSubTopicPart(branch, index - 1);
            else
                return getSubTopicPart(branch, index + 1);

        } else if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            if (index < numRight)
                return null;
            else
                return branch.getTopicPart();
        } else if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
            if (index >= numLeft)
                return null;
            else
                return branch.getTopicPart();
        }

        else if (!sequential) {
            if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                int numFirst = getRadialData(branch).getNumRight();
                if (sourceChild.getBranchIndex() >= numFirst) {
                    return branch.getTopicPart();
                }
            } else if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                int numFirst = getRadialData(branch).getNumRight();
                if (sourceChild.getBranchIndex() < numFirst) {
                    return branch.getTopicPart();
                }
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        int num = branch.getSubBranches().size();
        if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            if (!branch.getSubBranches().isEmpty())
                return getSubTopicPart(branch, 0);
        } else if (GEF.REQ_NAV_RIGHT.equals(navReqType))
            return getSubTopicPart(branch, num - 1);
        return super.calcNavigation(branch, navReqType);
    }

    public Dimension calcInsSize(IBranchPart branch, ParentSearchKey key) {
        return key.getFigure().getSize();
    }

    public boolean calcInsSide(IBranchPart branch, Point branchRef,
            ParentSearchKey key) {
        Point childRef = key.getFigure().getReference();
        return childRef.x > branchRef.x;
        // if Child on the right of Branch, return true;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        List<IBranchPart> enclosingBranches = summary.getEnclosingBranches();
        if (!enclosingBranches.isEmpty()) {
            IBranchPart subBranch = enclosingBranches.get(0);
            int index = subBranch.getBranchIndex();
            if (index >= 0) {
                if (index >= getRadialData(branch).getNumRight()) {
                    return PositionConstants.EAST;
                }
            }
        }
        return PositionConstants.WEST;
    }
}
