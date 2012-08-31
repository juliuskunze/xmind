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
package org.xmind.ui.branch;

import static org.xmind.ui.style.StyleUtils.getInteger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.IRotatableFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.BoundaryLayoutHelper.BoundaryData;
import org.xmind.ui.decorations.ISummaryDecoration;
import org.xmind.ui.internal.figures.BranchFigure;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public abstract class AbstractBranchStructure implements IBranchStructure,
        IBranchStructureExtension, INavigableBranchStructureExtension,
        IInsertableBranchStructureExtension {

    protected static final String CACHE_STRUCTURE_DATA = "org.xmind.ui.branchCache.structureData"; //$NON-NLS-1$

    protected static final String CACHE_BOUNDARY_LAYOUT_HELPER = "org.xmind.ui.branchCache.boundaryLayoutHelper"; //$NON-NLS-1$

    protected static class LayoutInfo extends ReferencedLayoutData {

        private ReferencedLayoutData delegate;

        private boolean folded;

        private boolean minimized;

        private Rectangle minArea;

        public LayoutInfo(ReferencedLayoutData delegate, boolean folded,
                boolean minimized) {
            this.delegate = delegate;
            this.folded = folded;
            this.minimized = minimized;
            this.minArea = null;
        }

        public boolean isFolded() {
            return folded;
        }

        public boolean isMinimized() {
            return minimized;
        }

        public Rectangle getMinArea() {
            return minArea;
        }

        public void setMinArea(Rectangle minArea) {
            this.minArea = minArea;
        }

        public void putMinArea(IFigure figure) {
            if (minArea == null) {
                delegate.put(figure, delegate.createInitBounds());
            } else {
                delegate.put(figure, minArea.getCopy());
            }
        }

        public void add(Rectangle blankArea) {
            delegate.add(blankArea);
        }

        public void addMargins(Insets margin) {
            delegate.addMargins(margin);
        }

        public void addMargins(int top, int left, int bottom, int right) {
            delegate.addMargins(top, left, bottom, right);
        }

        public Rectangle createInitBounds() {
            return delegate.createInitBounds();
        }

        public Rectangle createInitBounds(Point ref) {
            return delegate.createInitBounds(ref);
        }

        public Rectangle get(Object figure) {
            return delegate.get(figure);
        }

        public Rectangle getCheckedClientArea() {
            return delegate.getCheckedClientArea();
        }

        public Rectangle getClientArea() {
            return delegate.getClientArea();
        }

        public Point getReference() {
            return delegate.getReference();
        }

        public void put(IFigure figure, Rectangle preferredBounds) {
            delegate.put(figure, preferredBounds);
        }

        public void translate(int dx, int dy) {
            delegate.translate(dx, dy);
        }

    }

    public void fillLayoutData(IBranchPart branch, ReferencedLayoutData data) {
        BranchFigure figure = (BranchFigure) branch.getFigure();
        boolean folded = figure.isFolded();
        boolean minimized = figure.isMinimized();
        LayoutInfo info = new LayoutInfo(data, folded, minimized);
        fillLayoutInfo(branch, info);
    }

    protected void fillLayoutInfo(IBranchPart branch, LayoutInfo info) {
        fillTopic(branch, info);
        fillPlusMinus(branch, info);
        fillLabel(branch, info);

        List<IBranchPart> subBranches = branch.getSubBranches();
        fillSubBranches(branch, subBranches, info);

        List<IBoundaryPart> boundaries = branch.getBoundaries();
        fillBoundaries(branch, boundaries, subBranches, info);

        List<ISummaryPart> summaries = branch.getSummaries();
        List<IBranchPart> summaryBranches = new ArrayList<IBranchPart>(
                branch.getSummaryBranches());
        fillSummaries(branch, summaries, summaryBranches, subBranches, info);
        fillUnhandledSummaryBranches(branch, summaryBranches, info);

        addExtraSpaces(branch, info);

        fillOverallBoundary(branch, boundaries, info);

    }

    protected void fillTopic(IBranchPart branch, LayoutInfo info) {
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            if (info.isMinimized()) {
                info.putMinArea(topic.getFigure());
            } else {
                doFillTopic(branch, topic, info);
            }
        }
    }

    protected void doFillTopic(IBranchPart branch, ITopicPart topicPart,
            LayoutInfo info) {
        IFigure fig = topicPart.getFigure();
        if (fig instanceof IReferencedFigure) {
            IReferencedFigure refFig = (IReferencedFigure) fig;
            info.put(refFig, refFig.getPreferredBounds(info.getReference()));
        } else {
            Dimension size = fig.getPreferredSize();
            Point ref = info.getReference();
            Rectangle r = new Rectangle(ref.x - size.width / 2, ref.y
                    - size.height / 2, size.width, size.height);
            info.put(fig, r);
        }
    }

    protected void fillPlusMinus(IBranchPart branch, LayoutInfo info) {
        IPlusMinusPart plusMinus = branch.getPlusMinus();
        if (plusMinus != null) {
            IFigure pmFigure = plusMinus.getFigure();
            if (info.isMinimized()) {
                info.putMinArea(pmFigure);
            } else {
                doFillPlusMinus(branch, plusMinus, info);
                if (info.get(pmFigure) == null) {
                    info.putMinArea(pmFigure);
                }
            }
        }
    }

    protected abstract void doFillPlusMinus(IBranchPart branch,
            IPlusMinusPart plusMinus, LayoutInfo info);

    protected void fillLabel(IBranchPart branch, LayoutInfo info) {
        ILabelPart label = branch.getLabel();
        if (label != null) {
            if (info.isMinimized() || !label.getFigure().isVisible()) {
                info.putMinArea(label.getFigure());
            } else {
                doFillLabel(branch, label, info);
            }
        }
    }

    protected void doFillLabel(IBranchPart branch, ILabelPart label,
            LayoutInfo info) {
        IFigure figure = label.getFigure();
        ITopicPart topicPart = branch.getTopicPart();
        Rectangle area;
        if (topicPart != null) {
            area = info.get(topicPart.getFigure());
        } else {
            area = info.createInitBounds();
        }
        if (figure instanceof IRotatableFigure) {
            IRotatableFigure f = (IRotatableFigure) figure;
            double angle = f.getRotationDegrees();
            if (!Geometry.isSameAngleDegree(angle, 0, 0.00001)) {
                Point ref = info.getReference();
                PrecisionRotator r = new PrecisionRotator();
                r.setOrigin(ref.x, ref.y);
                r.setAngle(angle);
                PrecisionRectangle rect = r.r(new PrecisionRectangle(area));
                PrecisionDimension size = f.getNormalPreferredSize(-1, -1);
                rect.x += (rect.width - size.width) / 2;
                rect.y = rect.bottom() - 2;
                rect.width = size.width;
                rect.height = size.height;
                r.t(rect);
                info.put(figure, rect.toDraw2DRectangle());
                return;
            }
        }
        Dimension size = figure.getPreferredSize();
        Rectangle r = new Rectangle(area.x + (area.width - size.width) / 2,
                area.bottom() - 2, size.width, size.height);
        info.put(figure, r);
    }

    protected void fillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        if (subBranches.isEmpty())
            return;
        if ((info.isFolded() || info.isMinimized())
                && minimizesSubBranchesToOnePoint()) {
            if (info.isFolded() && !info.isMinimized()) {
                info.setMinArea(info.createInitBounds(calcSubBranchesMinPoint(
                        branch, subBranches, info)));
            }
            for (IBranchPart subBranch : subBranches) {
                info.putMinArea(subBranch.getFigure());
            }
        } else {
            doFillSubBranches(branch, subBranches, info);
            for (IBranchPart subBranch : subBranches) {
                IFigure subBranchFigure = subBranch.getFigure();
                if (info.get(subBranchFigure) == null) {
                    info.putMinArea(subBranchFigure);
                }
            }
        }
    }

    protected abstract void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info);

    protected boolean minimizesSubBranchesToOnePoint() {
        return true;
    }

    protected Point calcSubBranchesMinPoint(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        IPlusMinusPart plusMinus = branch.getPlusMinus();
        if (plusMinus != null) {
            Rectangle pmBounds = info.get(plusMinus.getFigure());
            if (pmBounds != null) {
                return pmBounds.getCenter();
            }
        }
        return info.getReference();
    }

    protected void fillBoundaries(IBranchPart branch,
            List<IBoundaryPart> boundaries, List<IBranchPart> subBranches,
            LayoutInfo info) {

        if (boundaries.isEmpty())
            return;
        if (subBranches.isEmpty()
                || ((info.isFolded() || info.isMinimized()) && minimizesSubBranchesToOnePoint())) {
            for (IBoundaryPart b : boundaries) {
                info.putMinArea(b.getFigure());
            }
        } else {
            doFillBoundaries(branch, boundaries, info);
        }
    }

    protected void doFillBoundaries(IBranchPart branch,
            List<IBoundaryPart> boundaries, LayoutInfo info) {

        for (IBoundaryPart boundary : boundaries) {
            doFillBoundary(branch, boundary, info);
        }
    }

    protected void doFillBoundary(IBranchPart branch, IBoundaryPart boundary,
            LayoutInfo info) {
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        BoundaryData d = helper.getBoundaryData(boundary);

        if (d.isOverall()) {
            if (d != helper.getOverallBoundary()) {
                info.putMinArea(boundary.getFigure());
            }
            return;
        }
        Rectangle area = null;
        for (IBranchPart subBranch : d.getSubBranches()) {
            Insets ins = helper.getInnerInsets(
                    helper.getSubBranchData(subBranch), d);
            Rectangle r2 = info.get(subBranch.getFigure());
            area = Geometry.union(area, r2.getExpanded(ins));
        }
        if (area != null) {
            area.expand(boundary.getFigure().getInsets());
        }
        if (area == null) {
            info.putMinArea(boundary.getFigure());
        } else {
            info.put(boundary.getFigure(), area);
        }
    }

    protected void fillSummaries(IBranchPart branch,
            List<ISummaryPart> summaries, List<IBranchPart> summaryBranches,
            List<IBranchPart> subBranches, LayoutInfo info) {
        if (!summaries.isEmpty()) {
            if (subBranches.isEmpty()
                    || ((info.isFolded() || info.isMinimized()) && minimizesSubBranchesToOnePoint())) {
                for (ISummaryPart s : summaries) {
                    info.putMinArea(s.getFigure());
                }
            } else {
                doFillSummaries(branch, summaries, summaryBranches, info);
            }
        }
    }

    private void doFillSummaries(IBranchPart branch,
            List<ISummaryPart> summaries, List<IBranchPart> summaryBranches,
            LayoutInfo info) {
        for (ISummaryPart summary : summaries) {
            doFillSummary(branch, summary, summaryBranches, info);
        }
    }

    private void doFillSummary(IBranchPart branch, ISummaryPart summary,
            List<IBranchPart> summaryBranches, LayoutInfo info) {
        int direction = getSummaryDirection(branch, summary);
        Rectangle area = getSummaryArea(branch, summary, direction, info);
        if (area != null) {
            info.put(summary.getFigure(), area);
        } else {
            info.putMinArea(summary.getFigure());
        }
        IBranchPart conclusionBranch = getConclusionBranch(branch, summary,
                summaryBranches);
        if (conclusionBranch != null) {
            if (area == null) {
                info.putMinArea(conclusionBranch.getFigure());
            } else {
                Insets ins = getConclusionReferenceDescription(branch, summary,
                        conclusionBranch);
                int x, y;
                switch (direction) {
                case PositionConstants.NORTH:
                    x = area.x + area.width / 2;
                    y = area.y - ins.bottom;
                    break;
                case PositionConstants.SOUTH:
                    x = area.x + area.width / 2;
                    y = area.bottom() + ins.top;
                    break;
                case PositionConstants.WEST:
                    x = area.x - ins.right;
                    y = area.y + area.height / 2;
                    break;
                default:
                    x = area.right() + ins.left;
                    y = area.y + area.height / 2;
                }
                info.put(conclusionBranch.getFigure(),
                        Geometry.getExpanded(x, y, ins));
            }
        }
    }

    private IBranchPart getConclusionBranch(IBranchPart branch,
            ISummaryPart summary, List<IBranchPart> summaryBranches) {
        IGraphicalPart part = summary.getNode();
        if (part instanceof ITopicPart) {
            IBranchPart conclusionBranch = ((ITopicPart) part).getOwnerBranch();
            if (conclusionBranch != null
                    && summaryBranches.contains(conclusionBranch)) {
                summaryBranches.remove(conclusionBranch);
                return conclusionBranch;
            }
        }
        return null;
    }

    private Insets getConclusionReferenceDescription(IBranchPart branch,
            ISummaryPart summary, IGraphicalPart conclusion) {
        IFigure fig = conclusion.getFigure();
        if (fig instanceof IReferencedFigure)
            return ((IReferencedFigure) fig).getReferenceDescription();
        Dimension size = fig.getPreferredSize();
        int w = size.width / 2;
        int h = size.height / 2;
        return new Insets(h, w, size.height - h, size.width - w);
    }

    protected Rectangle getSummaryArea(IBranchPart branch,
            ISummaryPart summary, int direction, ReferencedLayoutData data) {
        Rectangle r = null;
        for (IBranchPart subBranch : summary.getEnclosingBranches()) {
            r = Geometry.union(r, data.get(subBranch.getFigure()));
        }
        if (r == null)
            return null;

        Rectangle area = data.createInitBounds();
        int width = getPreferredSummaryWidth(summary);
        switch (direction) {
        case PositionConstants.NORTH:
            area.x = r.x;
            area.width = r.width;
            area.y = r.y - width;
            area.height = width;
            break;
        case PositionConstants.SOUTH:
            area.x = r.x;
            area.width = r.width;
            area.y = r.bottom();
            area.height = width;
            break;
        case PositionConstants.WEST:
            area.x = r.x - width;
            area.width = width;
            area.y = r.y;
            area.height = r.height;
            break;
        default:
            area.x = r.right();
            area.width = width;
            area.y = r.y;
            area.height = r.height;
        }
        IStyleSelector ss = StyleUtils.getStyleSelector(summary);
        String shape = StyleUtils.getString(summary, ss, Styles.ShapeClass,
                null);
        int lineWidth = StyleUtils.getInteger(summary, ss, Styles.LineWidth,
                shape, 1);
        return area.expand(lineWidth, lineWidth);
    }

    private int getPreferredSummaryWidth(ISummaryPart summary) {
        IFigure figure = summary.getFigure();
        if (figure instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) figure)
                    .getDecoration();
            if (decoration instanceof ISummaryDecoration) {
                return ((ISummaryDecoration) decoration)
                        .getPreferredWidth(figure);
            }
        }
        return Styles.DEFAULT_SUMMARY_WIDTH + Styles.DEFAULT_SUMMARY_SPACING
                * 2;
    }

    protected void fillUnhandledSummaryBranches(IBranchPart branch,
            List<IBranchPart> summaryBranches, LayoutInfo info) {
        if (!summaryBranches.isEmpty()) {
            for (IBranchPart summaryBranch : summaryBranches) {
                info.putMinArea(summaryBranch.getFigure());
            }
        }
    }

    protected void addExtraSpaces(IBranchPart branch, ReferencedLayoutData data) {
        // may be subclassed
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return PositionConstants.EAST;
    }

    protected void fillOverallBoundary(IBranchPart branch,
            List<IBoundaryPart> boundaries, LayoutInfo info) {
        if (boundaries.isEmpty())
            return;

        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        BoundaryData overallBoundary = helper.getOverallBoundary();
        if (overallBoundary == null)
            return;

        if (info.isMinimized()) {
            info.putMinArea(overallBoundary.boundaryFigure);
        } else {
            Rectangle area = info.getCheckedClientArea();
            area = overallBoundary.expanded(area.getCopy());
            info.put(overallBoundary.boundaryFigure, area);
        }
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.SOUTH;
    }

    public void invalidate(IGraphicalPart part) {
        if (part instanceof IBranchPart) {
            invalidateBranch((IBranchPart) part);
        }
    }

    protected void invalidateBranch(IBranchPart branch) {
        MindMapUtils.flushCache(branch, CACHE_STRUCTURE_DATA);
        MindMapUtils.flushCache(branch, CACHE_BOUNDARY_LAYOUT_HELPER);
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            IFigure topicFigure = topic.getFigure();
            if (topicFigure != null) {
                topicFigure.invalidate();
            }
        }
    }

    protected BoundaryLayoutHelper getBoundaryLayoutHelper(IBranchPart branch) {
        BoundaryLayoutHelper helper = (BoundaryLayoutHelper) MindMapUtils
                .getCache(branch, CACHE_BOUNDARY_LAYOUT_HELPER);
        if (helper == null) {
            helper = new BoundaryLayoutHelper(branch, this);
            MindMapUtils.setCache(branch, CACHE_BOUNDARY_LAYOUT_HELPER, helper);
        }
        return helper;
    }

    protected Dimension getBorderedSize(IBranchPart branch,
            IBranchPart subBranch) {
        return getBoundaryLayoutHelper(branch).getBorderedSize(subBranch);
    }

    protected int getMinorSpacing(IBranchPart branch) {
        return getInteger(branch,
                branch.getBranchPolicy().getStyleSelector(branch),
                Styles.MinorSpacing, 5);
    }

    protected int getMajorSpacing(IBranchPart branch) {
        return StyleUtils.getMajorSpacing(branch, 5);
    }

    protected Object getStructureData(IBranchPart branch) {
        Object data = MindMapUtils.getCache(branch, CACHE_STRUCTURE_DATA);
        if (!isValidStructureData(branch, data)) {
            data = createStructureData(branch);
            if (data != null) {
                MindMapUtils.setCache(branch, CACHE_STRUCTURE_DATA, data);
            }
        }
        return data;
    }

    protected Object createStructureData(IBranchPart branch) {
        return null;
    }

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return data != null;
    }

//    public void calculateSubBranchInsets(BoundaryData boundary,
//            SubBranchData subBranch, Insets insets) {
//    }

    /*
     * Subclass may extend this method.
     * 
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcSourceOrientation(org.xmind.ui.parts.IBranchPart)
     */
    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.NONE;
    }

    /*
     * Subclass may extend this method.
     * 
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcChildTargetOrientation(org.xmind.ui.parts.IBranchPart,
     * org.xmind.ui.parts.IBranchPart)
     */
    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return PositionConstants.NONE;
    }

//    /*
//     * Subclass may extend this method.
//     * 
//     * @see org.xmind.ui.graphicalpolicies.IBranchStructure#calcChildTargetOrientation(org.xmind.ui.parts.IBranchPart,
//     *      org.xmind.gef.draw2d.IReferencedFigure)
//     */
//    public int calcChildTargetOrientation(IBranchPart branch,
//            IReferencedFigure childFigure) {
//        return PositionConstants.NONE;
//    }

    /*
     * Subclass may extend this method.
     * 
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcChildIndex(org.xmind.ui.tools.ParentSearchKey)
     */
    public int calcChildIndex(IBranchPart branch, ParentSearchKey key) {
        return -1;
    }

    /*
     * Subclass may extend this method.
     * 
     * @seeorg.xmind.ui.mindmap.graphicalpolicies.IBranchStructure#
     * calcChildDistance(org.xmind.ui.parts.IBranchPart,
     * org.xmind.ui.tools.ParentSearchKey)
     */
    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        return -1;
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return null;
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        return null;
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_BEGINNING.equals(navReqType)) {
            return getSubTopicPart(branch, 0);
        } else if (GEF.REQ_NAV_END.equals(navReqType)) {
            return getSubTopicPart(branch, branch.getSubBranches().size() - 1);
        }
        return null;
    }

    public void calcSequentialNavigation(IBranchPart branch,
            IBranchPart startChild, IBranchPart endChild,
            List<IBranchPart> results) {
        addSubBranches(branch, startChild.getBranchIndex(),
                endChild.getBranchIndex(), results);
    }

    public void calcTraversableBranches(IBranchPart branch,
            IBranchPart sourceChild, List<IBranchPart> results) {
        addSubBranch(branch, sourceChild.getBranchIndex() + 1, results);
        results.add(branch);
        addSubBranch(branch, sourceChild.getBranchIndex() - 1, results);
    }

    public void calcTraversableChildren(IBranchPart branch,
            List<IBranchPart> results) {
        addSubBranches(branch, 0, branch.getSubBranches().size() - 1, results);
    }

    protected void addSubBranches(IBranchPart branch, IBranchPart fromChild,
            IBranchPart toChild, List<IBranchPart> results) {
        addSubBranches(branch, branch.getSubBranches().indexOf(fromChild),
                branch.getSubBranches().indexOf(toChild), results);
    }

    protected void addSubBranches(IBranchPart branch, int fromIndex,
            int toIndex, List<IBranchPart> results) {
        boolean decreasing = toIndex < fromIndex;
        for (int i = fromIndex; decreasing ? i >= toIndex : i <= toIndex;) {
            addSubBranch(branch, i, results);
            if (decreasing) {
                i--;
            } else {
                i++;
            }
        }
    }

    protected void addSubBranch(IBranchPart branch, int index,
            List<IBranchPart> results) {
        if (index < 0 || index >= branch.getSubBranches().size())
            return;
        results.add(branch.getSubBranches().get(index));
    }

    protected IBranchPart getSubBranch(IBranchPart branch, int index) {
        if (index >= 0 && index < branch.getSubBranches().size()) {
            return branch.getSubBranches().get(index);
        }
        return null;
    }

    protected ITopicPart getSubTopicPart(IBranchPart branch, int index) {
        IBranchPart subBranch = getSubBranch(branch, index);
        if (subBranch != null)
            return subBranch.getTopicPart();
        return null;
    }

    protected IInsertion getCurrentInsertion(IBranchPart branch) {
        return (IInsertion) MindMapUtils.getCache(branch,
                IInsertion.CACHE_INSERTION);
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        return 0;
    }

}