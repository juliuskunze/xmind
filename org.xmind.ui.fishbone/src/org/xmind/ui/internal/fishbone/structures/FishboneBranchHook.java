/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.mindmap.IBranchPart;

public class FishboneBranchHook implements IBranchHook, ICoreEventListener,
        FigureListener {

    private IBranchPart branch;

    private List<ICoreEventRegistration> topicListeners = new ArrayList<ICoreEventRegistration>();

    private Map<Object, List<ICoreEventRegistration>> rangeListeners = new HashMap<Object, List<ICoreEventRegistration>>();

    public void hook(IBranchPart branch) {
        this.branch = branch;
        branch.getFigure().addFigureListener(this);
        ITopic topic = branch.getTopic();
        if (topic instanceof ICoreEventSource) {
            ICoreEventSource source = (ICoreEventSource) topic;
            topicListeners.add(source.registerCoreEventListener(
                    Core.BoundaryAdd, this));
            topicListeners.add(source.registerCoreEventListener(
                    Core.BoundaryRemove, this));
            topicListeners.add(source.registerCoreEventListener(
                    Core.SummaryAdd, this));
            topicListeners.add(source.registerCoreEventListener(
                    Core.SummaryRemove, this));
            topicListeners.add(source.registerCoreEventListener(Core.TopicAdd,
                    this));
            topicListeners.add(source.registerCoreEventListener(
                    Core.TopicRemove, this));
        }
        for (IBoundary boundary : topic.getBoundaries()) {
            registerRangeListeners(boundary);
        }
        for (ISummary summary : topic.getSummaries()) {
            registerRangeListeners(summary);
        }
    }

    public void unhook(IBranchPart branch) {
        unregister(topicListeners);
        for (List<ICoreEventRegistration> regs : rangeListeners.values()) {
            unregister(regs);
        }
        branch.getFigure().removeFigureListener(this);
        updateSubBranches(branch);
    }

    private void unregister(List<ICoreEventRegistration> listeners) {
        for (ICoreEventRegistration reg : listeners) {
            reg.unregister();
        }
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.BoundaryAdd.equals(type) || Core.SummaryAdd.equals(type)) {
            Object range = event.getTarget();
            registerRangeListeners(range);
            updateSubBranches(branch);
        } else if (Core.BoundaryRemove.equals(type)
                || Core.SummaryRemove.equals(type)) {
            Object range = event.getTarget();
            List<ICoreEventRegistration> list = rangeListeners.remove(range);
            if (list != null) {
                unregister(list);
            }
            updateSubBranches(branch);
        } else if (Core.Range.equals(type)) {// || Core.EndIndex.equals(type)) {
//        } else if (Core.Range.equals(type) || Core.EndIndex.equals(type)) {
            if (isHeadBranch(branch)) {
                branch.getFigure().invalidate();
                branch.update();
                updateSubBranches(branch);
            }
        } else if (Core.TopicAdd.equals(type) || Core.TopicRemove.equals(type)) {
            if (ITopic.ATTACHED.equals(event.getData())) {
                updateSubBranches(branch);
            }
        }
    }

    private boolean isHeadBranch(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent == null)
            return true;
        String parentPolicyId = parent.getBranchPolicyId();
        return !branch.getBranchPolicyId().equals(parentPolicyId);
//        String structureId = (String) branch.getCacheManager().getCache(
//                IBranchPolicy.CACHE_STRUCTURE_ALGORITHM_ID);
//        return Fishbone.STRUCTURE_LEFT_HEADED.equals(structureId)
//                || Fishbone.STRUCTURE_RIGHT_HEADED.equals(structureId);
    }

    private void registerRangeListeners(Object range) {
        if (range instanceof ICoreEventSource) {
            ICoreEventSource source = (ICoreEventSource) range;
            List<ICoreEventRegistration> list = new ArrayList<ICoreEventRegistration>();
            list.add(source.registerCoreEventListener(Core.Range, this));
            //list.add(source.registerCoreEventListener(Core.EndIndex, this));
            rangeListeners.put(range, list);
        }
    }

    public void figureMoved(IFigure source) {
        IDecoration decoration = ((IDecoratedFigure) branch.getFigure())
                .getDecoration();
        if (decoration != null) {
            decoration.invalidate();
        }
    }

    private void updateSubBranches(IBranchPart branch) {
        for (IBranchPart subBranch : branch.getSubBranches()) {
            flushChildStructureType(subBranch);
            subBranch.treeUpdate(false);
        }
    }

    private void flushChildStructureType(IBranchPart subBranch) {
        subBranch.getBranchPolicy().flushStructureCache(subBranch, false, true);
    }

}