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

import org.eclipse.jface.action.IAction;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.mindmap.AbstractIconTipContributor;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class HyperlinkIconTipContributor extends AbstractIconTipContributor {

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        String hyperlink = topic.getHyperlink();
        if (hyperlink == null)
            return null;

        IAction action = MindMapUI.getProtocolManager()
                .createOpenHyperlinkAction(topicPart, hyperlink);
        if (action != null) {
            action.setId(MindMapActionFactory.OPEN_HYPERLINK.getId());
        }
        return action;
    }

    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
        register.register(Core.TopicHyperlink);
        register.setNextSupport((ICoreEventSupport) topic.getOwnedWorkbook()
                .getAdapter(ICoreEventSupport.class));
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
    }

    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
        if (Core.TopicAdd.equals(event.getType())
                || Core.TopicRemove.equals(event.getType())) {
            String hyperlink = topicPart.getTopic().getHyperlink();
            if (HyperlinkUtils.isInternalURL(hyperlink)) {
                Object target = HyperlinkUtils.findElement(hyperlink, topicPart
                        .getTopic().getOwnedWorkbook());
                if (target == event.getTarget()) {
                    topicPart.refresh();
                }
            }
        } else {
            topicPart.refresh();
        }
    }

}