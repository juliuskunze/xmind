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
package org.xmind.ui.richtext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;

public abstract class RichTextActionBarContributor implements
        IRichTextActionBarContributor {

    private IRichTextEditViewer viewer;

    private List<IRichTextAction> richTextActions = new ArrayList<IRichTextAction>();

    public IRichTextEditViewer getViewer() {
        return viewer;
    }

    public void dispose() {
        for (IRichTextAction action : richTextActions) {
            action.dispose();
        }
        richTextActions.clear();
        viewer = null;
    }

    public void fillMenu(IMenuManager menu) {
    }

    public void fillToolBar(IToolBarManager toolbar) {
    }

    public void init(IRichTextEditViewer viewer) {
        this.viewer = viewer;
        makeActions(viewer);
    }

    protected abstract void makeActions(IRichTextEditViewer viewer);

    protected void addRichTextAction(IRichTextAction action) {
        richTextActions.add(action);
    }

    public void selectionChanged(ISelection selection, boolean enabled) {
        for (IRichTextAction action : richTextActions) {
            action.selctionChanged(viewer, selection);
            action.setEnabled(enabled);
        }
    }

}