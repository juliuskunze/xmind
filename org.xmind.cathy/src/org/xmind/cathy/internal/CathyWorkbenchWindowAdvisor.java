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
package org.xmind.cathy.internal;

import java.io.File;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.xmind.cathy.internal.actions.SimpleOpenAction;
import org.xmind.ui.internal.editor.WorkbookEditorInput;
import org.xmind.ui.internal.workbench.Util;
import org.xmind.ui.mindmap.MindMapUI;

public class CathyWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public CathyWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(
            IActionBarConfigurer configurer) {
        return new CathyWorkbenchActionBuilder(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(Util.getInitialWindowSize());
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
        configurer.setTitle(WorkbenchMessages.AppWindowTitle);
    }

    public void postWindowOpen() {
        super.postWindowOpen();
        final IWorkbenchWindow window = getWindowConfigurer().getWindow();
        if (window != null) {

            CoolBarManager coolBar = ((WorkbenchWindow) window)
                    .getCoolBarManager();
            if (coolBar != null) {
                coolBar.setLockLayout(true);
            }

            window.getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    postOpen(window);
                }
            });
        }
    }

    private void postOpen(final IWorkbenchWindow window) {
        checkLog(window);
        window.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (window.getActivePage().getActiveEditor() == null) {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            window.getActivePage().openEditor(
                                    //new WorkbookEditorInput(),
                                    new WorkbookEditorInput(),
                                    MindMapUI.MINDMAP_EDITOR_ID);
                        }
                    });
                }
            }
        });
    }

    private void checkLog(IWorkbenchWindow window) {
        Log opening = Log.get(Log.OPENING);
        if (opening.exists()) {
            String[] files = opening.getContents();
            for (String file : files) {
                open(window, file);
            }
            opening.delete();
        }
    }

    private void open(IWorkbenchWindow window, String path) {
        File file = new File(path);
        if (file.isFile() && file.canRead()) {
            window.getShell().getDisplay().asyncExec(
                    new SimpleOpenAction(window, path));
        }
    }

}