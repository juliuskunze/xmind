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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class InsertAttachmentActionDelegate implements IEditorActionDelegate {

    private IEditorPart editor;

    private ISelection selection;

    public InsertAttachmentActionDelegate() {
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = targetEditor;
    }

    public void run(IAction action) {
        System.out.println(editor);
        System.out.println(selection);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

}