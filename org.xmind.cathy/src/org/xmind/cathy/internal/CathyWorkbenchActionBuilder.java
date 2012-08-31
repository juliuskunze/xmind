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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
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
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.xmind.cathy.internal.actions.HelpAction;
import org.xmind.cathy.internal.actions.ShowKeyAssistAction;
import org.xmind.cathy.internal.actions.UpdateAction;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.internal.actions.NewWorkbookAction;
import org.xmind.ui.internal.actions.NewWorkbookWizardAction;
import org.xmind.ui.internal.actions.OpenHomeMapAction;
import org.xmind.ui.internal.actions.OpenWorkbookAction;

public class CathyWorkbenchActionBuilder extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.

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

    private IWorkbenchAction copyAction;

    private IWorkbenchAction cutAction;

    private IWorkbenchAction pasteAction;

    private IWorkbenchAction deleteAction;

    private IWorkbenchAction selectAllAction;

    private IWorkbenchAction renameAction;

    private IWorkbenchAction openPreferencesAction;

    private IWorkbenchAction printAction;

    private IWorkbenchAction findAction;

    private IWorkbenchAction helpAction;

    private IWorkbenchAction updateAction;

    private IWorkbenchAction aboutAction;

    private IContributionItem reopenEditors;

    private IAction keyAssistAction;

//    private NewMenu newMenu;

    public CathyWorkbenchActionBuilder(IActionBarConfigurer configurer) {
        super(configurer);
    }

    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml
        // file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

//        this.newMenu = new NewMenu(window);
//        register(newMenu.getNewWorkbookAction());
//        register(newMenu.getNewFromTemplateAction());
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
        saveAction.setText(WorkbenchMessages.SaveAction_text);
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

        copyAction = ActionFactory.COPY.create(window);
        register(copyAction);

        cutAction = ActionFactory.CUT.create(window);
        register(cutAction);

        pasteAction = ActionFactory.PASTE.create(window);
        register(pasteAction);

        deleteAction = ActionFactory.DELETE.create(window);
        register(deleteAction);

        selectAllAction = ActionFactory.SELECT_ALL.create(window);
        register(selectAllAction);

        renameAction = ActionFactory.RENAME.create(window);
        register(renameAction);

        openPreferencesAction = ActionFactory.PREFERENCES.create(window);
        register(openPreferencesAction);

        printAction = ActionFactory.PRINT.create(window);
        register(printAction);

        findAction = ActionFactory.FIND.create(window);
        register(findAction);

        reopenEditors = ContributionItemFactory.REOPEN_EDITORS.create(window);

        // For Help Menu:
        helpAction = new HelpAction("org.xmind.ui.help", window); //$NON-NLS-1$
        register(helpAction);

        updateAction = new UpdateAction("org.xmind.ui.update", window); //$NON-NLS-1$
        register(updateAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        keyAssistAction = new ShowKeyAssistAction();
        register(keyAssistAction);
    }

    private static boolean isPro() {
        return Platform.getBundle("org.xmind.meggy") != null; //$NON-NLS-1$
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

//        String newText = MindMapMessages.NewWorkbook_text;
//        String newId = ActionFactory.NEW.getId();
//        MenuManager newMenu = new MenuManager(newText, newId);
//        newMenu.setActionDefinitionId("org.eclipse.ui.file.newQuickMenu"); //$NON-NLS-1$
//        newMenu.add(new Separator(newId));
//        newMenu.add(this.newMenu);
//        newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//        menu.add(newMenu);
        menu.add(newWizardAction);
        menu.add(newBlankAction);

        menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        menu.add(new Separator());

        menu.add(openAction);
        menu.add(openHomeMapAction);
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

        menu.add(printAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        menu.add(new Separator());

        menu.add(importAction);
        menu.add(exportAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(reopenEditors);
        menu.add(new Separator());

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

        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new Separator());

        menu.add(deleteAction);
        menu.add(new Separator());

        menu.add(selectAllAction);
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

        if (!CathyPlugin.getDistributionId().startsWith("vindy")) { //$NON-NLS-1$
            menu.add(updateAction);
        }

        if (!isPro()) {
            menu.add(new GroupMarker("group.upgrade")); //$NON-NLS-1$
        }
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
        fileBar.add(printAction);
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
        editBar.add(cutAction);
        editBar.add(copyAction);
        editBar.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        editBar.add(pasteAction);
        editBar.add(deleteAction);
        editBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        coolBar.add(actionBarConfigurer.createToolBarContributionItem(editBar,
                ActionConstants.TOOLBAR_EDIT));

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
    }

//    @Override
//    protected void fillStatusLine(IStatusLineManager statusLine) {
//        super.fillStatusLine(statusLine);
//        Bundle signInBundle = Platform.getBundle("net.xmind.signin"); //$NON-NLS-1$
//        if (signInBundle != null) {
//            try {
//                Class<?> clazz = signInBundle
//                        .loadClass("net.xmind.signin.internal.AccountStatusContribution"); //$NON-NLS-1$
//                IContributionItem xmindAccountContribution = (IContributionItem) clazz
//                        .newInstance();
//                statusLine.add(xmindAccountContribution); //$NON-NLS-1$
//            } catch (Exception e) {
//                //ignore
//            }
//        }
//    }

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
        AutoBackupIndicator item = new AutoBackupIndicator();
        statusLine.add(item);
        statusLine.update(true);
    }

}