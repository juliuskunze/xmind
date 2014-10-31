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
package org.xmind.ui.internal.sharing;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.sharing.ISharingService;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharingServiceLauncher implements IStartup, IWorkbenchListener {

    private ISharingService sharingService = null;

    private IWorkbench workbench = null;

    /**
     * Initialize the local network sharing service to let nearby machines
     * discover this local library on workbench startup. Will be called in a
     * separate thread after the workbench initializes.
     */
    public void earlyStartup() {
        sharingService = LocalNetworkSharingUI.getDefault().getSharingService();
        if (sharingService == null)
            return;

        workbench = PlatformUI.getWorkbench();
        if (workbench == null || workbench.isClosing())
            return;

        workbench.addWorkbenchListener(this);

        final IPreferenceStore prefStore = LocalNetworkSharingUI.getDefault()
                .getPreferenceStore();
        boolean featureEnabled = prefStore
                .getBoolean(LocalNetworkSharingUI.PREF_FEATURE_ENABLED);
        boolean toActivateService = prefStore
                .getBoolean(LocalNetworkSharingUI.PREF_SERVICE_ACTIVATED);
        final boolean autoEnable = !prefStore
                .getBoolean(LocalNetworkSharingUI.PREF_SKIP_AUTO_ENABLE);
        if (autoEnable) {
            // We try enabling LNS service on the first time launch:
            featureEnabled = true;
            toActivateService = true;
            sharingService.getLocalLibrary().setName(
                    SharingUtils.getComputerName());
            prefStore.setValue(LocalNetworkSharingUI.PREF_SKIP_AUTO_ENABLE,
                    true);
        }
        if (featureEnabled && toActivateService) {
            // Enable LNS service:
            Job enableJob = ToggleSharingServiceStatusJob.startToggle(
                    sharingService, true, new Runnable() {
                        public void run() {
                            if (autoEnable) {
                                prefStore
                                        .setValue(
                                                LocalNetworkSharingUI.PREF_FEATURE_ENABLED,
                                                true);
                            }
                        }
                    }, true);
            try {
                enableJob.join();
            } catch (InterruptedException e) {
                enableJob.cancel();
            }
        }

    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        return true;
    }

    public void postShutdown(IWorkbench workbench) {
        try {
            SharingUtils.run(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(
                            SharingMessages.TurnLocalNetworkSharingServiceOfflineJob_jobName,
                            100);
                    try {
                        IStatus disconnected = sharingService
                                .deactivate(new SubProgressMonitor(
                                        monitor,
                                        100,
                                        SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
                        if (monitor.isCanceled())
                            throw new InterruptedException();
                        if (disconnected != null
                                && (disconnected.getSeverity() & (IStatus.ERROR
                                        | IStatus.WARNING | IStatus.INFO)) != 0) {
                            LocalNetworkSharingUI.log(disconnected);
                        }
                    } catch (Throwable e) {
                        throw new InvocationTargetException(e);
                    }
                }
            }, workbench.getDisplay());
        } finally {
            this.sharingService = null;
            if (this.workbench != null) {
                this.workbench.removeWorkbenchListener(this);
                this.workbench = null;
            }
        }
    }

}
