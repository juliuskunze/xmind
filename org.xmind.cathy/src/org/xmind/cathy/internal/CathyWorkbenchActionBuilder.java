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
package org.xmind.cathy.internal;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IMenuService;
import org.xmind.cathy.internal.actions.HelpAction;
import org.xmind.cathy.internal.actions.ShowKeyAssistAction;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.internal.actions.NewWorkbookAction;
import org.xmind.ui.internal.actions.NewWorkbookWizardAction;
import org.xmind.ui.internal.actions.OpenHomeMapAction;
import org.xmind.ui.internal.actions.OpenWorkbookAction;
import org.xmind.ui.internal.actions.ReopenWorkbookMenu;
import org.xmind.ui.internal.editor.SaveActionUpdater;

public class CathyWorkbenchActionBuilder extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.

    private IWorkbenchWindow window;

    private IWorkbenchAction newWizardAction;

    private IWorkbenchAction newBlankAction;

    private IWorkbenchAction openAction;

    private IWorkbenchAction openHomeMapAction;

    private IWorkbenchAction closeAction;

    private IWorkbenchAction closeAllAction;

    private IWorkbenchAction saveAction;

    private IWorkbenchAction saveAsAction;

    private IWorkbenchAction saveAllAction;

    private IWorkbenchAction exportAction;

    private IWorkbenchAction importAction;

    private IWorkbenchAction exitAction;

    private IWorkbenchAction undoAction;

    private IWorkbenchAction redoAction;

    private IWorkbenchAction renameAction;

    private IWorkbenchAction openPreferencesAction;

    private IWorkbenchAction findAction;

    private IWorkbenchAction helpAction;

    private IWorkbenchAction aboutAction;

    private IAction reopenEditors;

    private IAction keyAssistAction;

    private SaveActionUpdater saveActionUpdater;

    public CathyWorkbenchActionBuilder(IActionBarConfigurer configurer) {
        super(configurer);
        this.window = configurer.getWindowConfigurer().getWindow();
    }

    public IWorkbenchWindow getWindow() {
        return window;
    }

    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml
        // file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        newWizardAction = new NewWorkbookWizardAction(window);
        register(newWizardAction);

        newBlankAction = new NewWorkbookAction(window);
        register(newBlankAction);

        openAction = new OpenWorkbookAction(window);
        register(openAction);

        openHomeMapAction = new OpenHomeMapAction(window);
        register(openHomeMapAction);

        closeAction = ActionFactory.CLOSE.create(window);
        register(closeAction);

        closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        register(closeAllAction);

        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);

        saveAsAction = ActionFactory.SAVE_AS.create(window);
        register(saveAsAction);

        saveAllAction = ActionFactory.SAVE_ALL.create(window);
        register(saveAllAction);

        exportAction = ActionFactory.EXPORT.create(window);
        register(exportAction);

        importAction = ActionFactory.IMPORT.create(window);
        register(importAction);

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        undoAction = ActionFactory.UNDO.create(window);
        register(undoAction);

        redoAction = ActionFactory.REDO.create(window);
        register(redoAction);

        renameAction = ActionFactory.RENAME.create(window);
        register(renameAction);

        openPreferencesAction = ActionFactory.PREFERENCES.create(window);
        register(openPreferencesAction);

        findAction = ActionFactory.FIND.create(window);
        register(findAction);

        reopenEditors = new ReopenWorkbookMenu(window);

        // For Help Menu:
        helpAction = new HelpAction("org.xmind.ui.help", window); //$NON-NLS-1$
        register(helpAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        keyAssistAction = new ShowKeyAssistAction();
        register(keyAssistAction);

        this.saveActionUpdater = new SaveActionUpdater(window, saveAction);
    }

    @Override
    public void dispose() {
        if (saveActionUpdater != null) {
            saveActionUpdater.dispose();
            saveActionUpdater = null;
        }
        super.dispose();
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(createHelpMenu());
    }

    private MenuManager createFileMenu() {
        MenuManager menu = new MenuManager(WorkbenchMessages.File_menu_text,
                IWorkbenchActionConstants.M_FILE);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));

        menu.add(newWizardAction);
        menu.add(newBlankAction);

        menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        menu.add(new Separator());

        menu.add(openAction);
        menu.add(openHomeMapAction);
        menu.add(reopenEditors);
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator());

        menu.add(closeAction);
        menu.add(closeAllAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
        menu.add(new Separator());

        menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
        menu.add(saveAction);
        menu.add(saveAsAction);
        menu.add(saveAllAction);
        menu.add(new Separator());
        menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
        menu.add(new Separator());

        menu.add(getPrintItem());
        menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        menu.add(new Separator());

        menu.add(importAction);
        menu.add(exportAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        // If we're on OS X we shouldn't show this command in the File menu. It
        // should be invisible to the user. However, we should not remove it -
        // the carbon UI code will do a search through our menu structure
        // looking for it when Cmd-Q is invoked (or Quit is chosen from the
        // application menu.
        ActionContributionItem exitItem = new ActionContributionItem(exitAction);
        exitItem.setVisible(!Util.isMac());
        menu.add(exitItem);

        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
        return menu;
    }

    private MenuManager createEditMenu() {
        MenuManager menu = new MenuManager(WorkbenchMessages.Edit_menu_text,
                IWorkbenchActionConstants.M_EDIT);
        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

        menu.add(undoAction);
        menu.add(redoAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        menu.add(new Separator());

        menu.add(getCutItem());
        menu.add(getCopyItem());
        menu.add(getPasteItem());
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new Separator());

        menu.add(getDeleteItem());
        menu.add(new Separator());

        menu.add(getSelectAllItem());
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator());

        menu.add(findAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
        menu.add(new Separator());

        ActionContributionItem openPreferencesItem = new ActionContributionItem(
                openPreferencesAction);
        openPreferencesItem.setVisible(!Util.isMac());
        menu.add(openPreferencesItem);
        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
        return menu;
    }

    private MenuManager createHelpMenu() {
        MenuManager menu = new MenuManager(WorkbenchMessages.Help_menu_text,
                IWorkbenchActionConstants.M_HELP);

        menu.add(new GroupMarker("group.welcome")); //$NON-NLS-1$
        menu.add(new Separator());

        menu.add(new Separator("group.intro")); //$NON-NLS-1$
        menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$

        menu.add(new Separator("group.main")); //$NON-NLS-1$
        menu.add(helpAction);
        menu.add(new GroupMarker("group.assist")); //$NON-NLS-1$
        menu.add(keyAssistAction);
        menu.add(new GroupMarker("group.xmind")); //$NON-NLS-1$
        menu.add(new Separator());

        menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
        menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
        menu.add(new Separator("group.tutorials")); //$NON-NLS-1$
        menu.add(new Separator("group.tools")); //$NON-NLS-1$
        menu.add(new Separator("group.updates")); //$NON-NLS-1$

        menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator("group.about")); //$NON-NLS-1$

        ActionContributionItem aboutItem = new ActionContributionItem(
                aboutAction);
        aboutItem.setVisible(!Util.isMac());
        menu.add(aboutItem);
        menu.add(new GroupMarker("about.ext")); //$NON-NLS-1$
        return menu;
    }

    protected void fillCoolBar(ICoolBarManager coolBar) {
        super.fillCoolBar(coolBar);
        IActionBarConfigurer2 actionBarConfigurer = (IActionBarConfigurer2) getActionBarConfigurer();

        coolBar.add(new GroupMarker(ActionConstants.GROUP_FILE));

        IToolBarManager fileBar = actionBarConfigurer.createToolBarManager();
        fileBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_GROUP));
//        fileBar.add(newMenu.getNewWorkbookAction());
        fileBar.add(newBlankAction);
        fileBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        fileBar.add(openAction);
        fileBar.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        fileBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
        fileBar.add(saveAction);
        fileBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
        fileBar.add(getPrintItem());
        fileBar.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        fileBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        coolBar.add(actionBarConfigurer.createToolBarContributionItem(fileBar,
                IWorkbenchActionConstants.TOOLBAR_FILE));

        coolBar.add(new GroupMarker(ActionConstants.GROUP_UNDO));

        IToolBarManager undoBar = actionBarConfigurer.createToolBarManager();
        undoBar.add(undoAction);
        undoBar.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        undoBar.add(redoAction);
        undoBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        coolBar.add(actionBarConfigurer.createToolBarContributionItem(undoBar,
                ActionConstants.TOOLBAR_UNDO));

        coolBar.add(new GroupMarker(ActionConstants.GROUP_EDIT));

        IToolBarManager editBar = actionBarConfigurer.createToolBarManager();
        editBar.add(getCutItem());
        editBar.add(getCopyItem());
//        editBar.add(cutAction);
//        editBar.add(copyAction);
        editBar.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        editBar.add(getPasteItem());
        editBar.add(getDeleteItem());
//        editBar.add(pasteAction);
//        editBar.add(deleteAction);
        editBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        coolBar.add(actionBarConfigurer.createToolBarContributionItem(editBar,
                ActionConstants.TOOLBAR_EDIT));

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.application.ActionBarAdvisor#fillStatusLine(org.eclipse
     * .jface.action.IStatusLineManager)
     */
    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        super.fillStatusLine(statusLine);

        if (statusLine instanceof ContributionManager) {
            IMenuService ms = (IMenuService) getActionBarConfigurer()
                    .getWindowConfigurer().getWindow()
                    .getService(IMenuService.class);
            if (ms != null) {
                ms.populateContributionManager(
                        (ContributionManager) statusLine,
                        "toolbar:org.xmind.ui.statusbar"); //$NON-NLS-1$
            }
        }

        statusLine.update(true);
    }

    private IContributionItem getCutItem() {
        return getItem(ActionFactory.CUT.getId(),
                ActionFactory.CUT.getCommandId(), ISharedImages.IMG_TOOL_CUT,
                ISharedImages.IMG_TOOL_CUT_DISABLED,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_cut,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_cutToolTip,
                null);
    }

    private IContributionItem getCopyItem() {
        return getItem(
                ActionFactory.COPY.getId(),
                ActionFactory.COPY.getCommandId(),
                ISharedImages.IMG_TOOL_COPY,
                ISharedImages.IMG_TOOL_COPY_DISABLED,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_copy,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_copyToolTip,
                null);
    }

    private IContributionItem getPasteItem() {
        return getItem(
                ActionFactory.PASTE.getId(),
                ActionFactory.PASTE.getCommandId(),
                ISharedImages.IMG_TOOL_PASTE,
                ISharedImages.IMG_TOOL_PASTE_DISABLED,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_paste,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_pasteToolTip,
                null);
    }

    private IContributionItem getSelectAllItem() {
        return getItem(
                ActionFactory.SELECT_ALL.getId(),
                ActionFactory.SELECT_ALL.getCommandId(),
                null,
                null,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_selectAll,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_selectAllToolTip,
                null);
    }

    private IContributionItem getDeleteItem() {
        return getItem(
                ActionFactory.DELETE.getId(),
                ActionFactory.DELETE.getCommandId(),
                ISharedImages.IMG_TOOL_DELETE,
                ISharedImages.IMG_TOOL_DELETE_DISABLED,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_delete,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_deleteToolTip,
                IWorkbenchHelpContextIds.DELETE_RETARGET_ACTION);
    }

    private IContributionItem getPrintItem() {
        return getItem(
                ActionFactory.PRINT.getId(),
                ActionFactory.PRINT.getCommandId(),
                ISharedImages.IMG_ETOOL_PRINT_EDIT,
                ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_print,
                org.eclipse.ui.internal.WorkbenchMessages.Workbench_printToolTip,
                null);
    }

    private IContributionItem getItem(String actionId, String commandId,
            String image, String disabledImage, String label, String tooltip,
            String helpContextId) {
        ISharedImages sharedImages = getWindow().getWorkbench()
                .getSharedImages();

        IActionCommandMappingService acms = (IActionCommandMappingService) getWindow()
                .getService(IActionCommandMappingService.class);
        acms.map(actionId, commandId);

        CommandContributionItemParameter commandParm = new CommandContributionItemParameter(
                getWindow(), actionId, commandId, null,
                sharedImages.getImageDescriptor(image),
                sharedImages.getImageDescriptor(disabledImage), null, label,
                null, tooltip, CommandContributionItem.STYLE_PUSH, null, false);
        return new CommandContributionItem(commandParm);
    }
}