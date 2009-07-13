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
package org.xmind.cathy.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;
import org.xmind.cathy.internal.WorkbenchMessages;

/**
 * @author briansun
 * 
 */
public class UpdateAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    /**
     * @param id
     * @param window
     */
    public UpdateAction(String id, IWorkbenchWindow window) {
        super(WorkbenchMessages.Update_text);
        setId(id);
        setToolTipText(WorkbenchMessages.Update_toolTip);
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        UpdateJob job = new UpdateJob(WorkbenchMessages.Update_jobName, false,
                false);
        UpdateManagerUI.openInstaller(window.getShell(), job);
    }

    /**
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        window = null;
    }

}