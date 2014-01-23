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

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ShowViewAction;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

public class MindMapViewsMenu extends ContributionItem {

    private static class ShowViewAction2 extends ShowViewAction {

        protected ShowViewAction2(IWorkbenchWindow window,
                IViewDescriptor desc, boolean makeFast) {
            super(window, desc, makeFast);
        }

    }

    private IWorkbenchWindow window;

    protected boolean dirty = true;

    private IMenuListener menuListener = new IMenuListener() {
        public void menuAboutToShow(IMenuManager manager) {
            manager.markDirty();
            dirty = true;
        }
    };

    public MindMapViewsMenu(IWorkbenchWindow window) {
        this.window = window;
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Overridden to always return true and force dynamic menu building.
     */
    public boolean isDynamic() {
        return true;
    }

    public void fill(Menu menu, int index) {
        if (getParent() instanceof MenuManager) {
            ((MenuManager) getParent()).addMenuListener(menuListener);
        }

        if (!dirty) {
            return;
        }

        MenuManager manager = new MenuManager();
        fillMenu(manager);
        IContributionItem items[] = manager.getItems();
        if (items.length > 0) {
            for (int i = 0; i < items.length; i++) {
                items[i].fill(menu, index++);
            }
        }
        dirty = false;
    }

    private void fillMenu(MenuManager manager) {
        IViewRegistry viewRegistry = window.getWorkbench().getViewRegistry();
        IViewDescriptor[] views = viewRegistry.getViews();
        for (IViewDescriptor view : views) {
            String viewId = view.getId();
            if (viewId.startsWith("org.xmind.ui.")) { //$NON-NLS-1$
                addShowViewAction(manager, viewId, view);
            }
        }
    }

    private void addShowViewAction(MenuManager manager, String viewId,
            IViewDescriptor view) {
        manager.add(new ShowViewAction2(window, view, false));
    }

}