/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.gef.GEF;
import org.xmind.gef.graphicalpolicy.IGraphicalPolicy;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchStyleSelector;
import org.xmind.ui.decorations.IBranchConnections;
import org.xmind.ui.internal.decorations.BranchConnections;
import org.xmind.ui.internal.decorators.BranchDecorator;
import org.xmind.ui.internal.figures.BranchFigure;
import org.xmind.ui.internal.layouts.BranchLayout;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author MANGOSOFT
 */
public class BranchPart extends MindMapPartBase implements IBranchPart {

    private ITopicPart topicPart = null;

    private List<IBranchPart> subBranches = null;

    private IPlusMinusPart plusMinus = null;

    private List<IBoundaryPart> boundaries = null;

    private List<ISummaryPart> summaries = null;

    private List<IBranchPart> summaryBranches = null;

    private ILabelPart label = null;

    private boolean central = false;

    private ChildSorter sorter = new ChildSorter(this);

    private BranchConnections connections = null;

    private String branchPolicyId = null;

    private int level = -1;

    public BranchPart() {
        setDecorator(BranchDecorator.getInstanceo());
    }

    public ITopic getTopic() {
        return (ITopic) getRealModel();
    }

    public IBranchPart getParentBranch() {
        if (getParent() instanceof IBranchPart)
            return (IBranchPart) getParent();
        return null;
    }

    public ITopicPart getTopicPart() {
        return topicPart;
    }

    public void setTopicPart(ITopicPart topicPart) {
        this.topicPart = topicPart;
    }

    public IPlusMinusPart getPlusMinus() {
        return plusMinus;
    }

    public void setPlusMinus(IPlusMinusPart plusMinus) {
        this.plusMinus = plusMinus;
    }

    public List<IBranchPart> getSubBranches() {
        if (subBranches == null) {
            subBranches = new ArrayList<IBranchPart>();
        }
        return subBranches;
    }

    public void addSubBranch(IBranchPart subBranch) {
        getSubBranches().add(subBranch);
        sorter.sort(getSubBranches());
        int index = getSubBranches().indexOf(subBranch);
        getConnections().add(getFigure(), index, null);
    }

    public void removeSubBranch(IBranchPart subBranch) {
        int index = getSubBranches().indexOf(subBranch);
        getConnections().remove(getFigure(), index);
        getSubBranches().remove(subBranch);
    }

    public List<ISummaryPart> getSummaries() {
        if (summaries == null) {
            summaries = new ArrayList<ISummaryPart>();
        }
        return summaries;
    }

    public void addSummary(ISummaryPart summary) {
        getSummaries().add(summary);
        sorter.sort(getSummaries());
    }

    public void removeSummary(ISummaryPart summary) {
        getSummaries().remove(summary);
    }

    public List<IBranchPart> getSummaryBranches() {
        if (summaryBranches == null)
            summaryBranches = new ArrayList<IBranchPart>();
        return summaryBranches;
    }

    public void addSummaryBranch(IBranchPart summaryBranch) {
        getSummaryBranches().add(summaryBranch);
        sorter.sort(getSummaryBranches());
    }

    public void removeSummaryBranch(IBranchPart summaryBranch) {
        getSummaryBranches().remove(summaryBranch);
    }

    public ILabelPart getLabel() {
        return label;
    }

    public void setLabel(ILabelPart label) {
        this.label = label;
    }

    protected IFigure createFigure() {
        return new BranchFigure();
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            BranchPart parentBranch = (BranchPart) getParent();
            parentBranch.removeSubBranch(this);
            parentBranch.removeSummaryBranch(this);
        } else if (getParent() instanceof SheetPart) {
            SheetPart sheet = (SheetPart) getParent();
            sheet.removeFloatingBranch(this);
            if (sheet.getCentralBranch() == this)
                sheet.setCentralBranch(null);
        }
        super.setParent(parent);
        central = getTopic().equals(
                getSite().getViewer().getAdapter(ITopic.class));
        if (getParent() instanceof BranchPart) {
            BranchPart parentBranch = (BranchPart) getParent();
            level = parentBranch.getLevel() + 1;
            if (ITopic.ATTACHED.equals(getTopic().getType())) {
                parentBranch.addSubBranch(this);
            } else if (ITopic.SUMMARY.equals(getTopic().getType())) {
                parentBranch.addSummaryBranch(this);
            }
        } else if (getParent() instanceof SheetPart) {
            SheetPart sheet = (SheetPart) getParent();
            if (getTopic().equals(sheet.getCentralTopic())) {
                level = 0;
                sheet.setCentralBranch(this);
            } else if (ITopic.DETACHED.equals(getTopic().getType())) {
                level = 1;
                sheet.addFloatingBranch(this);
            }
        }
        setBranchPolicyId(findNewBranchPolicyId());
    }

    public int getBranchIndex() {
        IBranchPart parentBranch = getParentBranch();
        if (parentBranch != null) {
            return parentBranch.getSubBranches().indexOf(this);
        }
        return 0;
    }

    public int getLevel() {
        return level;
    }

    public List<IBoundaryPart> getBoundaries() {
        if (boundaries == null) {
            boundaries = new ArrayList<IBoundaryPart>();
        }
        return boundaries;
    }

    public void addBoundary(IBoundaryPart boundary) {
        getBoundaries().add(boundary);
        sorter.sort(getBoundaries());
    }

    public void removeBoundary(IBoundaryPart boundary) {
        getBoundaries().remove(boundary);
    }

    public boolean isCentral() {
        return central;
    }

    public boolean isFolded() {
        return ((BranchFigure) getFigure()).isFolded();
    }

    public String getBranchType() {
        if (isCentral())
            return MindMapUI.BRANCH_CENTRAL;
        IPart p = getParent();
        if (p instanceof IBranchPart) {
            IBranchPart parentBranch = (IBranchPart) p;
            if (parentBranch.isCentral()
                    && parentBranch.getSubBranches().contains(this))
                return MindMapUI.BRANCH_MAIN;
            if (parentBranch.getSummaryBranches().contains(this))
                return MindMapUI.BRANCH_SUMMARY;
        }
        if (p instanceof ISheetPart
                && ((ISheetPart) p).getFloatingBranches().contains(this))
            return MindMapUI.BRANCH_FLOATING;
        return MindMapUI.BRANCH_SUB;
    }

    protected LayoutManager createLayoutManager() {
        return new BranchLayout(this);
    }

    protected Object[] getModelChildren(Object model) {
        List<Object> list = new ArrayList<Object>();
        ITopic topic = getTopic();
        boolean showsSubTopics = showsSubTopics();
        if (showsSubTopics) {
            for (IBoundary b : MindMapUtils.getSortedBoundaries(topic)) {
                list.add(new ViewerModel(BoundaryPart.class, b));
            }
        } else {
            for (IBoundary b : topic.getBoundaries()) {
                if (b.isMasterBoundary()) {
                    list.add(new ViewerModel(BoundaryPart.class, b));
                    break;
                }
            }
        }

        list.add(new ViewerModel(TopicPart.class, topic));

        if (!topic.getLabels().isEmpty()) {
            list.add(new ViewerModel(LabelPart.class, topic));
        }

        if (showsSubTopics) {
            List<ITopic> children = topic.getChildren(ITopic.ATTACHED);
            if (!children.isEmpty()) {
                list.add(new ViewerModel(PlusMinusPart.class, topic));
            }
            for (ITopic subtopic : children) {
                list.add(new ViewerModel(BranchPart.class, subtopic));
            }
            for (ISummary summary : topic.getSummaries()) {
                ITopic summaryTopic = summary.getTopic();
                if (summaryTopic != null) {
                    list.add(new SummaryViewerModel(SummaryPart.class, summary,
                            summaryTopic));
                }
                //list.add(new ViewerModel(SummaryPart.class, summary));
            }
            for (ITopic summaryTopic : topic.getChildren(ITopic.SUMMARY)) {
                list.add(new ViewerModel(BranchPart.class, summaryTopic));
            }
        }
        return list.toArray();
    }

    private boolean showsSubTopics() {
        int maxLevel = getSite().getViewer().getProperties().getInteger(
                IMindMapViewer.VIEWER_MAX_TOPIC_LEVEL, -1);
        return maxLevel < 0 || level < maxLevel;
    }

    protected void addChild(IPart child, int index) {
        super.addChild(child, index);
        update();
        if (child instanceof IBranchPart) {
            child.update();
        }
    }

    protected void removeChild(IPart child) {
        super.removeChild(child);
        update();
    }

    protected void reorderChild(IPart child, int index) {
        int oldBranchIndex = getSubBranches().indexOf(child);
        super.reorderChild(child, index);
        if (oldBranchIndex >= 0) {
            sorter.sort(getSubBranches());
            int newBranchIndex = getSubBranches().indexOf(child);
            IBranchConnections connections = getConnections();
            connections.move(getFigure(), oldBranchIndex, newBranchIndex);
        } else if (getBoundaries().contains(child)) {
            sorter.sort(getBoundaries());
        } else if (getSummaries().contains(child)) {
            sorter.sort(getSummaries());
        } else if (getSummaryBranches().contains(child)) {
            sorter.sort(getSummaryBranches());
        }
        update();
    }

    protected void registerCoreEvents(ICoreEventSource source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.TopicFolded);
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
        register.register(Core.Position);
        register.register(Core.BoundaryAdd);
        register.register(Core.BoundaryRemove);
        register.register(Core.SummaryAdd);
        register.register(Core.SummaryRemove);
        register.register(Core.Style);
        register.register(Core.StructureClass);
        register.register(Core.Labels);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TopicFolded.equals(type)) {
            treeUpdate(true);
        } else if (Core.TopicAdd.equals(type) || Core.TopicRemove.equals(type)) {
            Object topicType = event.getData();
            if (ITopic.ATTACHED.equals(topicType)) {
                refresh();
            } else if (ITopic.SUMMARY.equals(topicType)) {
                refresh();
            }
        } else if (Core.Position.equals(type)) {
            org.xmind.core.util.Point newPosition = (org.xmind.core.util.Point) event
                    .getNewValue();
            Point p = MindMapUtils.toGraphicalPosition(newPosition);
            getCacheManager().setCache(CACHE_PREF_POSITION, p);
            //update();
            getFigure().revalidate();
        } else if (Core.BoundaryAdd.equals(type)
                || Core.BoundaryRemove.equals(type)
                || Core.SummaryAdd.equals(type)
                || Core.SummaryRemove.equals(type)) {
            refresh();
        } else if (Core.Style.equals(type)) {
            refreshStyles();
        } else if (Core.StructureClass.equals(type)) {
            treeUpdateBranchPolicy();
            sendFakeStyleEvent();
        } else if (Core.Labels.equals(type)) {
            refresh();
            if (getLabel() != null) {
                getLabel().refresh();
            }
        } else {
            super.handleCoreEvent(event);
        }
    }

    // Force an 'unchanged' style change be notified, so that
    // property sections or other style listeners can update themselves.
    // TODO: replace this with a better way of event dispatching, 
    // e.g. Properties and PropertyChangeListener
    private void sendFakeStyleEvent() {
        ICoreEventSupport coreEventSupport = (ICoreEventSupport) getTopic()
                .getOwnedWorkbook().getAdapter(ICoreEventSupport.class);
        if (coreEventSupport != null) {
            ICoreEventSource source = (ICoreEventSource) getTopic();
            String styleId = getTopic().getStyleId();
            CoreEvent event = new CoreEvent(source, Core.Style, styleId,
                    styleId);
            coreEventSupport.dispatch(source, event);
        }
    }

    public void refreshStyles() {
        treeFlushStyleCaches();
        treeUpdate(true);
    }

    private void treeFlushStyleCaches() {
        IStyleSelector ss = getGraphicalPolicy().getStyleSelector(this);
        if (ss instanceof IBranchStyleSelector) {
            ((IBranchStyleSelector) ss).flushStyleCaches(this);
        }
        for (IBranchPart subBranch : getSubBranches()) {
            ((BranchPart) subBranch).treeFlushStyleCaches();
        }
        for (IBranchPart summaryBranch : getSummaryBranches()) {
            ((BranchPart) summaryBranch).treeFlushStyleCaches();
        }
    }

    protected void treeUpdateBranchPolicy() {
        updateBranchPolicy();
        updateView();
        refreshChildren();
        for (IBranchPart subBranch : getSubBranches()) {
            ((BranchPart) subBranch).treeUpdateBranchPolicy();
        }
        for (IBranchPart summaryBranch : getSummaryBranches()) {
            ((BranchPart) summaryBranch).treeUpdateBranchPolicy();
        }
        updateChildren();
    }

    private void updateBranchPolicy() {
        String newId = findNewBranchPolicyId();
        setBranchPolicyId(newId);
    }

    private void setBranchPolicyId(String newId) {
        IBranchPolicy newPolicy = MindMapUI.getBranchPolicyManager()
                .getBranchPolicy(newId);
        branchPolicyId = newId;
        setGraphicalPolicy(newPolicy);
        getFigure().revalidate();
    }

    public void setGraphicalPolicy(IGraphicalPolicy graphicalPolicy) {
        IGraphicalPolicy oldPolicy = getGraphicalPolicy();
        super.setGraphicalPolicy(graphicalPolicy);
        if (oldPolicy instanceof IBranchPolicy) {
            ((IBranchPolicy) oldPolicy).postDeactivate(this);
        }
    }

    public String getBranchPolicyId() {
        if (branchPolicyId == null) {
            branchPolicyId = findNewBranchPolicyId();
        }
        return branchPolicyId;
    }

    private String findNewBranchPolicyId() {
        return MindMapUI.getBranchPolicyManager().calculateBranchPolicyId(this,
                getTopic().getStructureClass());
    }

    public IBranchPolicy getBranchPolicy() {
        IGraphicalPolicy gp = getGraphicalPolicy();
        if (gp instanceof IBranchPolicy)
            return (IBranchPolicy) gp;
        return MindMapUI.getBranchPolicyManager().getDefaultBranchPolicy();
    }

    public void setModel(Object model) {
        super.setModel(model);
        Point p = MindMapUtils.toGraphicalPosition(getTopic().getPosition());
        getCacheManager().setCache(CACHE_PREF_POSITION, p);
    }

    protected void onActivated() {
        super.onActivated();
        //getBranchPolicy().activate(this);
    }

    protected void onDeactivated() {
//        getBranchPolicy().deactivate(this);
        super.onDeactivated();
    }

    protected void updateChildren() {
        super.updateChildren();
        if (getTopicPart() != null)
            getTopicPart().update();
        if (getPlusMinus() != null)
            getPlusMinus().update();
        if (getLabel() != null)
            getLabel().update();
        for (IBoundaryPart b : getBoundaries()) {
            b.update();
        }
        for (ISummaryPart s : getSummaries()) {
            s.update();
        }
    }

    public void treeUpdate(boolean updateParent) {
        if (updateParent) {
            IBranchPart parentBranch = getParentBranch();
            if (parentBranch != null) {
                ((BranchPart) parentBranch).updateChildren();
            }
        }
        updateView();
        for (IBranchPart subBranch : getSubBranches()) {
            subBranch.treeUpdate(false);
        }
        for (IBranchPart summaryBranch : getSummaryBranches()) {
            summaryBranch.treeUpdate(false);
        }
        updateChildren();
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_CREATABLE,
                MindMapUI.POLICY_TOPIC_CREATABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EXTENDABLE,
                MindMapUI.POLICY_EXTENDABLE);
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
    }

    public boolean isPropertyModifiable(String propertyName) {
        return isPropertyModifiable(propertyName, null);
    }

    public boolean isPropertyModifiable(String propertyName, String secondaryKey) {
        return getBranchPolicy().isPropertyModifiable(this, propertyName,
                secondaryKey);
    }

    public IPart findAt(Point position) {
        IPart ret;
        IPart focusedPart = getSite().getViewer().getFocusedPart();
        if (focusedPart instanceof IGraphicalEditPart
                && getBoundaries().contains(focusedPart)) {
            ret = ((IGraphicalEditPart) focusedPart).findAt(position);
            if (ret != null)
                return ret;
        }
        ret = super.findAt(position);
        if (ret == this)
            return null;
        return ret;
    }

    public boolean canSearchChild() {
        BranchFigure figure = (BranchFigure) getFigure();
        return !figure.isFolded()
                && !figure.isMinimized()
                && figure.isVisible()
                && figure.isEnabled()
                && (!getSubBranches().isEmpty() || !getSummaryBranches()
                        .isEmpty());
    }

    protected boolean isFigureAnimatable() {
        return true;
    }

    public IBranchConnections getConnections() {
        if (connections == null) {
            connections = new BranchConnections();
        }
        return connections;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == ITopicPart.class)
            return getTopicPart();
        if (adapter == IPlusMinusPart.class)
            return getPlusMinus();
        if (adapter == ILabelPart.class)
            return getLabel();
        return super.getAdapter(adapter);
    }

}