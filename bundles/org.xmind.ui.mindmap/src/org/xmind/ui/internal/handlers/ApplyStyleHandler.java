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
package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * The command handler that sends a 'modify style' request with the first
 * selected style to the current active editor.
 * 
 * @author Frank Shaka
 */
public class ApplyStyleHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        applyStyle(event);
        return null;
    }

    /**
     * @param event
     */
    private void applyStyle(ExecutionEvent event) {
        IStyle style = MindMapHandlerUtil.findStyle(event);
        if (style == null)
            return;

        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor == null)
            return;

        applyStyleToEditor(style, editor);
    }

    private void applyStyleToEditor(IStyle style, IEditorPart editor) {
        if (!(editor instanceof IGraphicalEditor))
            return;

        IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                .getActivePageInstance();
        if (page == null)
            return;

        EditDomain editDomain = page.getEditDomain();
        if (editDomain == null)
            return;

        editDomain.handleRequest(new Request(MindMapUI.REQ_MODIFY_STYLE)
                .setViewer(page.getViewer()).setDomain(editDomain)
                .setParameter(MindMapUI.PARAM_RESOURCE, style));
    }

}
