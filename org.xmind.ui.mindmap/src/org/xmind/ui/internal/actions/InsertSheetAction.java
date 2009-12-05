/* ******************************************************************************
 * Copyright (c) 2006-2009 XMind Ltd. and others.
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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.CreateSheetCommandBuilder;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author karelun Huang
 * 
 */
public class InsertSheetAction extends RequestAction implements
        ISelectionAction {

    public InsertSheetAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.INSERT_SHEET_FROM.getId(), page,
                MindMapUI.REQ_CREATE_SHEET);
    }

    public void run() {
        if (isDisposed())
            return;

        IGraphicalEditor editor = getEditor();
        IGraphicalViewer viewer = getViewer();

        ICommandStack commandStack = getCommandStack();
        CreateSheetCommandBuilder builder = new CreateSheetCommandBuilder(
                editor, viewer, commandStack);

        if (!builder.canStart())
            return;

        builder.start();
        builder.addCommand();
        builder.end();

    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection));
    }
}