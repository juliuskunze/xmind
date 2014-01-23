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
package org.xmind.ui.internal.notes;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.ui.actions.DelegatingAction;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.AbstractIconTipContributor;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class NotesIconTipContributor extends AbstractIconTipContributor {

    private static class ShowNotesAction extends Action {

        private ITopicPart topicPart;

        public ShowNotesAction(ITopicPart topicPart) {
            super(MindMapMessages.EditNotes_text, MindMapUI.getImages().get(
                    IMindMapImages.NOTES, true));
            setId(MindMapActionFactory.EDIT_NOTES.getId());
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.NOTES, false));
            this.topicPart = topicPart;
        }

        public void run() {
            if (!topicPart.getStatus().isActive())
                return;

            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window == null)
                return;

            IWorkbenchPage workbenchPage = window.getActivePage();
            if (workbenchPage != null) {
                IViewPart notesView = workbenchPage
                        .findView(MindMapUI.VIEW_NOTES);
                if (notesView != null) {
                    workbenchPage.activate(notesView);
                    return;
                }
            }

            NotesPopup popup = new NotesPopup(window, topicPart, true, false);
            popup.open();
        }
    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        INotes notes = topic.getNotes();
        if (notes.isEmpty())
            return null;

        IAction action = null;
        IActionRegistry actionRegistry = (IActionRegistry) topicPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry != null) {
            action = actionRegistry.getAction(MindMapActionFactory.EDIT_NOTES
                    .getId());
            if (action != null)
                action = new DelegatingAction(action);
        }

        if (action == null)
            action = new ShowNotesAction(topicPart);

        INotesContent content = notes.getContent(INotes.PLAIN);
        if (content instanceof IPlainNotesContent) {
            String text = ((IPlainNotesContent) content).getTextContent();
            if (text.length() > 500)
                text = text.substring(0, 500) + "...\n..."; //$NON-NLS-1$
            action.setToolTipText(text);
        }
        return action;
    }

    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
        register.register(Core.TopicNotes);
    }

    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
        topicPart.refresh();
    }

}