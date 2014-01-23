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
package net.xmind.workbench.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;

public class CheckForUpdatesCommandHandler implements ICommandHandler {

    public CheckForUpdatesCommandHandler() {
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) {
            String skippable = command.getArgument("skippable"); //$NON-NLS-1$
            new CheckForUpdatesJob(workbench, skippable).schedule();
            return Status.OK_STATUS;
        }
        return null;
    }

}
