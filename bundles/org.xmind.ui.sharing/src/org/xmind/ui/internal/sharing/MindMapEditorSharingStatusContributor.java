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
package org.xmind.ui.internal.sharing;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.xmind.core.sharing.ISharingService;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.editor.IMiniBar;
import org.xmind.ui.mindmap.IMindMapEditorConfigurer;

public class MindMapEditorSharingStatusContributor implements
        IMindMapEditorConfigurer {

    public MindMapEditorSharingStatusContributor() {
    }

    public void configureEditor(IGraphicalEditor editor) {
        ISharingService sharingService = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (sharingService == null)
            return;

        IMiniBar bar = (IMiniBar) editor.getAdapter(IMiniBar.class);
        if (bar == null)
            return;

        IToolBarManager toolBar = bar.getToolBarManager();
        if (toolBar == null)
            return;

        toolBar.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS,
                new Separator());
        toolBar.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS,
                new MindMapSharingStatusItem(editor, sharingService));
        toolBar.update(true);
    }

    public void configureEditorPage(IGraphicalEditorPage page) {
    }

}
