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
package org.xmind.gef.ui.editor;

import org.eclipse.jface.action.IToolBarManager;

public class MiniBarContributor implements IMiniBarContributor {

    private IMiniBar bar;

    private IGraphicalEditor editor;

    private IGraphicalEditorPage activePage;

    public void init(IMiniBar bar, IGraphicalEditor editor) {
        this.editor = editor;
        init(bar);
    }

    public void init(IMiniBar bar) {
        this.bar = bar;
        contributeToToolBar(bar.getToolBarManager());
    }

    public void dispose() {
    }

    public IMiniBar getBar() {
        return bar;
    }

    public IGraphicalEditor getEditor() {
        return editor;
    }

    public void contributeToToolBar(IToolBarManager toolBar) {
    }

    public void setActivePage(IGraphicalEditorPage page) {
        if (page == this.activePage)
            return;

        if (this.activePage != null) {
            unhookPage(this.activePage);
        }
        this.activePage = page;
        if (page != null) {
            hookPage(page);
        }
        pageChanged(page);
    }

    public IGraphicalEditorPage getActivePage() {
        return activePage;
    }

    protected void pageChanged(IGraphicalEditorPage page) {

    }

    protected void hookPage(IGraphicalEditorPage page) {

    }

    protected void unhookPage(IGraphicalEditorPage page) {

    }

}