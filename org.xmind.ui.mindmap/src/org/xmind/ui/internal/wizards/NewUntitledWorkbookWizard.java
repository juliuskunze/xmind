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
package org.xmind.ui.internal.wizards;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public class NewUntitledWorkbookWizard extends Wizard implements INewWizard {

    private IWorkbench workbench;

    public boolean performFinish() {
        if (workbench == null)
            return false;

        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null)
            return false;

        final IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return false;

        final boolean[] b = new boolean[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                page.openEditor(MME.createNonExistingEditorInput(),
                        MindMapUI.MINDMAP_EDITOR_ID);
                b[0] = true;
            }

            public void handleException(Throwable e) {
                b[0] = false;
                super.handleException(e);
            }
        });
        return b[0];
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
    }

}