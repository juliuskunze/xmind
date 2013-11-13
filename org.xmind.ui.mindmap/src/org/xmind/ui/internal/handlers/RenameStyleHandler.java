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
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.style.IStyle;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.viewers.ICategorizedContentProvider;

/**
 * The command handler that retrieves a new name from the user and set it to all
 * selected styles.
 * 
 * @author Frank Shaka
 */
public class RenameStyleHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        renameStyle(event);
        return null;
    }

    private void renameStyle(ExecutionEvent event) {
        IStyle style = MindMapHandlerUtil.findStyle(event,
                MindMapHandlerUtil.MATCH_MODIFIABLE);
        if (style == null)
            return;

        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (MindMapUI.VIEW_STYLES.equals(part.getSite().getId())) {
            renameStyleInView(style, part);
        } else {
            renameStyleInDialog(style, part.getSite().getShell());
        }
    }

    private void renameStyleInView(IStyle style, IWorkbenchPart view) {
        ContentViewer viewer = (ContentViewer) view
                .getAdapter(ContentViewer.class);
        if (viewer == null)
            return;

        IContentProvider contentProvider = viewer.getContentProvider();
        if (!(contentProvider instanceof ICategorizedContentProvider))
            return;

        Object category = ((ICategorizedContentProvider) contentProvider)
                .getCategory(style);
        System.out.println(category);
    }

    private void renameStyleInDialog(IStyle style, Shell parentShell) {

    }

}
