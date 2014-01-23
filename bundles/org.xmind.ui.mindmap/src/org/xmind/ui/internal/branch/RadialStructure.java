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
package org.xmind.ui.internal.branch;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISummaryPart;

public class RadialStructure extends BaseRadialStructure {

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        RadialData cache = getRadialData(branch);
        int numRight = cache.getNumRight();
        int[] childrenSpacings = cache.getChildrenSpacings();
        int num = subBranches.size();
        boolean right = true;
        RadiationInsertion insertion = getCurrentInsertion(branch);
        int insHeight = insertion == null ? 0 : insertion.getSize().height;

        int y = -cache.getRightSumSpacing() / 2;
        if (insertion != null && insertion.right) {
            y -= insHeight / 2;
        }

        Point ref = info.getReference();
        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = -cache.getLeftSumSpacing() / 2;
                if (insertion != null && !insertion.right) {
                    y -= insHeight / 2;
                }
                right = false;
            }

            if (insertion != null && i == insertion.getIndex()) {
                if (i != numRight || !insertion.right) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils.getPrefBounds(insertion
                            .getSize(), p, right);
                    info.add(insBounds);
                    y += insHeight;
                }
            }

            IBranchPart subBranch = subBranches.get(i);
            Rectangle r;
            Dimension offset = getOffset(subBranch);
            IFigure subFigure = subBranch.getFigure();
            if (offset != null && subFigure instanceof IReferencedFigure) {
                Point subRef = ref.getTranslated(offset);
                r = ((IReferencedFigure) subFigure).getPreferredBounds(subRef);
            } else {
                int x = cache.getX(y, right);
                Point subRef = ref.getTranslated(x, y);
                r = RadialUtils.getPrefBounds(subBranch, subRef, right);
            }
            info.put(subFigure, r);
            y += childrenSpacings[i];

            if (insertion != null) {
                if ((i == numRight - 1 && insertion.getIndex() == numRight && insertion.right)
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

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_UP.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() - 1);
        } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
        } else if (!sequential) {
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

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return super.getSummaryDirection(branch, summary);
    }
}