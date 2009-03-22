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
package org.xmind.ui.internal.actions;

import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.actions.DelegatingAction;
import org.xmind.ui.internal.MindMapMessages;

public class DropDownInsertImageAction extends DelegatingAction implements
        IMenuCreator {

    private List<IWorkbenchAction> imageActionExtensions;

    private Menu menu;

    public DropDownInsertImageAction(IAction delegate,
            List<IWorkbenchAction> imageActionExtensions) {
        super(delegate, AS_DROP_DOWN_MENU, TEXT, IMAGE, TOOL_TIP_TEXT);
        setId("org.xmind.ui.insertImageDropDown"); //$NON-NLS-1$
        setActionDefinitionId(null);
        this.imageActionExtensions = imageActionExtensions;
        setMenuCreator(this);
    }

    public Menu getMenu(Control parent) {
        if (menu != null) {
            menu.dispose();
        }

        menu = new Menu(parent);
        fillMenu(menu);
        return menu;
    }

    public Menu getMenu(Menu parent) {
        if (menu != null) {
            menu.dispose();
        }
        menu = new Menu(parent);
        fillMenu(menu);
        return menu;
    }

    private void fillMenu(Menu menu) {
        for (IWorkbenchAction action : imageActionExtensions) {
            ActionContributionItem item = new ActionContributionItem(action);
            item.fill(menu, -1);
            if ("org.xmind.ui.insertImage".equals(action.getId())) { //$NON-NLS-1$
                ((MenuItem) item.getWidget())
                        .setText(MindMapMessages.InsertImageFromFile_text);
            }
        }
    }

}