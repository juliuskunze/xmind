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
package org.xmind.gef.ui.editor;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.ui.IEditorPart;
import org.xmind.gef.command.ICommandStack;

public interface IGraphicalEditor extends IEditorPart, IPageChangeProvider {

    public void addPage(IGraphicalEditorPage page);

    public void removePage(IGraphicalEditorPage page);

    public IGraphicalEditorPage getPage(int pageIndex);

    public int getPageCount();

    public int findPage(IGraphicalEditorPage page);

//    public boolean isDirty();

//    public void forceDirty();

//    public void fireDirty();

    public void setCommandStack(ICommandStack commandStack);

    public ICommandStack getCommandStack();

    public int getActivePage();

    public void setActivePage(int index);

    public IGraphicalEditorPage getActivePageInstance();

    public IGraphicalEditorPage[] getPages();

    public IGraphicalEditorPage findPage(Object pageInput);

    public IGraphicalEditorPage ensurePageVisible(Object pageInput);

    public boolean navigateTo(Object pageInput, Object... elements);

    public void movePageTo(int oldIndex, int newIndex);

    public String getPageText(int index);

    public void setPageText(int index, String text);

}