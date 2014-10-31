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

import net.xmind.signin.internal.XMindUpdater;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbench;

public class CheckForUpdatesJob extends Job {

    private IWorkbench workbench;

    private XMindUpdater updater = null;

    private String skippable = XMindUpdater.SKIPPABLE_NO;

    public CheckForUpdatesJob(IWorkbench workbench) {
        super(Messages.CheckForUpdatesJob_jobName);
        this.workbench = workbench;
    }

    public CheckForUpdatesJob(IWorkbench workbench, String skippable) {
        super(Messages.CheckForUpdatesJob_jobName);
        this.workbench = workbench;
        this.skippable = skippable;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        updater = new XMindUpdater(workbench, skippable);
        return updater.run(monitor);
    }

    @Override
    protected void canceling() {
        if (updater != null) {
            updater.abort();
        }
        super.canceling();
    }

}
