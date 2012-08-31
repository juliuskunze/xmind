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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.util.MindMapUtils;

public class NorthEastNormal extends AbstractSubFishboneDirection {

    public NorthEastNormal() {
        super(false, false, false, EAST, WEST);
    }

    public ISubDirection getSubDirection() {
        return NER;
    }

    public void fillFishboneData(IBranchPart branch, FishboneData data,
            IPrecisionTransformer h, PrecisionRotator r, double spacing,
            List<IBranchPart> subbranches) {

        PrecisionPoint origin = h.getOrigin();
        PrecisionInsets hTopicIns = h.ti(data.topicRefIns);
        PrecisionRectangle hBranchBounds = h.ti(data.branchRefIns).getBounds(
                origin);
        PrecisionRectangle rhBranchBounds = h.ti(data.rBranchRefIns).getBounds(
                origin);
        double sin = r.sin();
        double ctg = r.cos() / r.sin();

        PrecisionPoint joint = origin.getTranslated(hTopicIns.right,
                hTopicIns.bottom);

        IPlusMinusPart plusMinus = branch.getPlusMinus();
        if (plusMinus != null) {
            joint.x += plusMinus.getFigure().getPreferredSize().width;
        }
        PrecisionPoint joint2 = joint.getCopy();

        IInsertion insertion = (IInsertion) MindMapUtils.getCache(branch,
                IInsertion.CACHE_INSERTION);
        int insIndex = insertion == null ? -1 : insertion.getIndex();
        Dimension insSize = insertion == null ? null : insertion.getSize();
        double insHeight = insSize == null ? 0 : insSize.height;

        IStructure structure = branch.getBranchPolicy().getStructure(branch);
        BoundaryLayoutHelper helper = ((SubFishboneStructure) structure)
                .getBoundaryLayoutHelper(branch);
        //to track record the longest topic's length while it in horizon direction
        double dxT = 0.0;
        double width = 0.0;
        double len = 0.0;
        for (int i = 0; i < subbranches.size(); i++) {
            IBranchPart subBranch = subbranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();

            PrecisionInsets hChildBranch;
            PrecisionInsets rhChildBranch;

            Insets ins = helper.getInsets(subBranch);
            PrecisionInsets hChildBorder = h.t(new PrecisionInsets(ins));
            PrecisionInsets rhChildBorder = r.ti(hChildBorder);

            IStructure bsa = subBranch.getBranchPolicy()
                    .getStructure(subBranch);
            if (bsa instanceof SubFishboneStructure) {
                SubFishboneStructure sfsa = (SubFishboneStructure) bsa;
                FishboneData subData = sfsa.getCastedData(subBranch)
                        .getFishboneData();
                hChildBranch = h.ti(subData.branchRefIns);

                rhChildBranch = h.ti(subData.rBranchRefIns);
            } else {
                PrecisionInsets childBranchNormal = new PrecisionInsets(
                        ((IReferencedFigure) subBranchFigure)
                                .getReferenceDescription());
                rhChildBranch = h.ti(childBranchNormal);
                hChildBranch = r.ti(rhChildBranch);
            }

            double dy = -rhChildBranch.bottom - hChildBorder.bottom;
            double dx1 = Math.abs(dy * ctg);
            double dx;
            if (hChildBorder.right != 0) {
                dx = hChildBorder.right + rhChildBranch.left;
            } else {
                double dx2 = Math.abs(hChildBranch.top / sin);
                dx = dx1 + dx2;
            }

            if (insIndex >= 0 && i == insIndex) {
                dx += Math.abs(insHeight / sin);
            }

            PrecisionPoint hChildRef;
            if (hChildBorder.right != 0) {
                joint.x = joint2.x;
                hChildRef = joint2.getTranslated(dx, dy);
            } else {
                joint2.x = joint.x;
                hChildRef = joint.getTranslated(dx, dy);
            }
            hChildRef.y -= hChildBorder.bottom;

            if (hChildBorder.getHeight() != 0) {
                if (dxT <= hChildBranch.getWidth() * r.cos() + joint.x)
                    //the longest topic's length while it in horizon direction
                    dxT = hChildBranch.getWidth() * r.cos() + joint.x;
                if (width <= rhChildBranch.right) {
                    width = rhChildBranch.right;
                    len = joint.x + dx + width;
                }
            }

            PrecisionPoint rhChildRef = r.tp(hChildRef);

            hBranchBounds.union(rhChildBranch.getBounds(hChildRef).expand(
                    hChildBorder));
            rhBranchBounds.union(hChildBranch.getBounds(rhChildRef).expand(
                    rhChildBorder));

            data.addChildOffset(subBranch, h.rp(hChildRef));

            double jdx;
            if (hChildBorder.left != 0) {
                double dx3 = Math.abs(hChildBranch.bottom / sin);
                //the whole boundary's length in horizon direction
                double dxB = joint.x + dx1 - dx3 + rhChildBranch.right
                        + hChildBorder.left;
                if (dxT < dxB)
                    dx += hChildBorder.left + rhChildBranch.right;
                else {
                    double d = joint.x + dx;
                    double cha = len - d;
                    dx += rhChildBorder.left + cha;
                }
                jdx = dx;
            } else {
                double dx3 = Math.abs(hChildBranch.bottom / sin);
                jdx = dx - dx1 + dx3;
                dx += rhChildBranch.right;
            }

            joint2.x += dx + spacing;
            joint.x += jdx + spacing;

        }
        data.branchRefIns = h.rr(hBranchBounds).getInsets(origin);
        data.rBranchRefIns = h.rr(rhBranchBounds).getInsets(origin);
    }

}