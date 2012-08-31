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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.ui.mindmap.AbstractIconTipContributor;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class DrillUpIconTipContributor extends AbstractIconTipContributor {

    private static class IconTipDrillUpAction extends Action {

        private IViewer viewer;

        public IconTipDrillUpAction(IViewer viewer) {
            this.viewer = viewer;
        }

        public void run() {
            EditDomain domain = viewer.getEditDomain();
            if (domain == null)
                return;

            domain.handleRequest(MindMapUI.REQ_DRILLUP, viewer);
        }
    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        IViewer viewer = topicPart.getSite().getViewer();
        if (viewer != null && topic.equals(viewer.getAdapter(ITopic.class))
                && topic.getParent() != null && hasTraceService(viewer)) {
            return new IconTipDrillUpAction(viewer);
        }
        return null;
    }

    private boolean hasTraceService(IViewer viewer) {
        if (viewer instanceof IGraphicalViewer) {
            return ((IGraphicalViewer) viewer)
                    .getService(IDrillDownTraceService.class) != null;
        }
        return false;
    }

    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
    }

    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
    }

}