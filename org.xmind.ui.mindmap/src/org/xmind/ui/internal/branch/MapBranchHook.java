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
package org.xmind.ui.internal.branch;

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
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ICacheValueProvider;
import org.xmind.ui.util.MindMapUtils;

public class MapBranchHook implements IBranchHook, FigureListener,
        ICoreEventListener {

    IBranchPart branch;

    private List<ICoreEventRegistration> topicListeners = new ArrayList<ICoreEventRegistration>();

    private Map<Object, List<ICoreEventRegistration>> rangeListeners = new HashMap<Object, List<ICoreEventRegistration>>();

    public void figureMoved(IFigure source) {
        if (needUpdate()) {
            branch.getBranchPolicy().flushStructureCache(branch, true, true);
            findTopBranch(branch, branch.getBranchPolicyId()).treeUpdate(false);
        }
    }

    private IBranchPart findTopBranch(IBranchPart branch, String policyId) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            String parentId = parent.getBranchPolicyId();
            if (policyId.equals(parentId))
                return findTopBranch(parent, policyId);
        }
        return branch;
    }

    private boolean needUpdate() {
        String oldValue = (String) MindMapUtils.getCache(branch,
                IBranchPolicy.CACHE_STRUCTURE_ID);
        String newValue = calcStructureId(branch);
        if (newValue == null)
            return false;
        return !newValue.equals(oldValue);
    }

    private String calcStructureId(IBranchPart branch) {
        ICacheValueProvider valueProvider = MindMapUtils
                .getCacheManager(branch).getValueProvider(
                        IBranchPolicy.CACHE_STRUCTURE_ID);
        if (valueProvider != null) {
            Object value = valueProvider.getValue(branch,
                    IBranchPolicy.CACHE_STRUCTURE_ID);
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }

    public void hook(IBranchPart branch) {
        this.branch = branch;
        branch.getFigure().addFigureListener(this);
        if (branch.isCentral()) {
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
            }
            for (IBoundary boundary : topic.getBoundaries()) {
                registerRangeListeners(boundary);
            }
            for (ISummary summary : topic.getSummaries()) {
                registerRangeListeners(summary);
            }
        }
    }

    public void unhook(IBranchPart branch) {
        branch.getFigure().removeFigureListener(this);
        unregister(topicListeners);
        for (List<ICoreEventRegistration> regs : rangeListeners.values()) {
            unregister(regs);
        }
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
        } else if (Core.BoundaryRemove.equals(type)
                || Core.SummaryRemove.equals(type)) {
            Object range = event.getTarget();
            List<ICoreEventRegistration> list = rangeListeners.remove(range);
            if (list != null) {
                unregister(list);
            }
        } else if (Core.StartIndex.equals(type) || Core.EndIndex.equals(type)) {
            branch.getFigure().invalidate();
            branch.treeUpdate(true);
        }
    }

    private void registerRangeListeners(Object range) {
        if (range instanceof ICoreEventSource) {
            ICoreEventSource source = (ICoreEventSource) range;
            List<ICoreEventRegistration> list = new ArrayList<ICoreEventRegistration>();
            list.add(source.registerCoreEventListener(Core.StartIndex, this));
            list.add(source.registerCoreEventListener(Core.EndIndex, this));
            rangeListeners.put(range, list);
        }
    }

}