/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
package org.xmind.ui.internal.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.tabfolder.IPageClosedListener;

public class WorkbookOverviewPage extends Page implements IPageChangedListener,
        IPageClosedListener {

    private IGraphicalEditor editor;

    private PageBook pageBook;

    private Map<IGraphicalEditorPage, Page> pageMap = new HashMap<IGraphicalEditorPage, Page>();

    public WorkbookOverviewPage(IGraphicalEditor editor) {
        this.editor = editor;
    }

    @Override
    public void createControl(Composite parent) {
        pageBook = new PageBook(parent, SWT.NONE);
        editor.addPageChangedListener(this);
        editor.addPageClosedListener(this);

        IGraphicalEditorPage page = editor.getActivePageInstance();
        if (page != null) {
            setActivePage(page);
        }
    }

    @Override
    public Control getControl() {
        return pageBook;
    }

    @Override
    public void setFocus() {
        pageBook.setFocus();
    }

    @Override
    public void dispose() {
        editor.removePageClosedListener(this);
        editor.removePageChangedListener(this);
        super.dispose();
    }

    public void pageChanged(PageChangedEvent event) {
        Object page = event.getSelectedPage();
        if (page instanceof IGraphicalEditorPage) {
            setActivePage((IGraphicalEditorPage) page);
        }
    }

    private void setActivePage(IGraphicalEditorPage sheetPage) {
        Page overviewPage = pageMap.get(sheetPage);
        if (overviewPage == null) {
            overviewPage = createPage(sheetPage);
            pageMap.put(sheetPage, overviewPage);
        }
        if (pageBook != null && !pageBook.isDisposed()) {
            Control pageControl = overviewPage.getControl();
            if (pageControl != null && !pageControl.isDisposed()) {
                pageBook.showPage(pageControl);
            }
        }
    }

    private Page createPage(IGraphicalEditorPage sheetPage) {
        Page page = new SheetOverviewPage(sheetPage);
        page.init(getSite());
        if (pageBook != null && !pageBook.isDisposed()) {
            page.createControl(pageBook);
        }
        return page;
    }

    public void pageClosed(Object sheetPage) {
        Page overviewPage = pageMap.remove(sheetPage);
        if (overviewPage == null)
            return;
        Control control = overviewPage.getControl();
        if (control != null && !control.isDisposed()) {
            control.dispose();
        }
        overviewPage.dispose();
    }

}
