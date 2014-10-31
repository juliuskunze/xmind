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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.xmind.core.sharing.ISharingService;

public class ToggleSharingServiceStatusJob extends Job {

    private static Job CURRENT_JOB = null;

    private final ISharingService sharingService;

    private final boolean toActivate;

    private final Runnable onFinish;

    private boolean ignoreError;

    private ToggleSharingServiceStatusJob(ISharingService sharingService,
            boolean toActivate, Runnable onFinish, boolean ignoreError) {
        super(
                toActivate ? SharingMessages.TurnLocalNetworkSharingServiceOnlineJob_jobName
                        : SharingMessages.TurnLocalNetworkSharingServiceOfflineJob_jobName);
        this.sharingService = sharingService;
        this.toActivate = toActivate;
        this.onFinish = onFinish;
        this.ignoreError = ignoreError;
    }

    protected IStatus run(IProgressMonitor monitor) {
        IStatus toggled;
        if (toActivate) {
            toggled = activate(monitor);
            if (toggled.isOK()) {
                handleFinish(true);
            }
        } else {
            toggled = deactivate(monitor);
            handleFinish(false);
        }
        return toggled;
    }

    private void handleFinish(boolean activated) {
        LocalNetworkSharingUI
                .getDefault()
                .getPreferenceStore()
                .setValue(LocalNetworkSharingUI.PREF_SERVICE_ACTIVATED,
                        activated);
        if (onFinish != null) {
            try {
                onFinish.run();
            } catch (Throwable e) {
                LocalNetworkSharingUI
                        .log("Error while running extra job on toggle sharing service status job finish.", //$NON-NLS-1$
                                e);
            }
        }
    }

    private IStatus activate(IProgressMonitor monitor) {
        IStatus status = sharingService.activate(monitor);
        if (status.getSeverity() == IStatus.ERROR) {
            return handleActivationError(status);
        }
        return status;
    }

    private IStatus deactivate(IProgressMonitor monitor) {
        return sharingService.deactivate(monitor);
    }

    private IStatus handleActivationError(IStatus status) {
        if (status.getCode() == 23333) {
            // DNSSD unavailable:
//            LocalNetworkSharingUI
//                    .getDefault()
//                    .getPreferenceStore()
//                    .setValue(LocalNetworkSharingUI.PREF_FEATURE_ENABLED, false);
//            LocalNetworkSharingUI.getDefault().getPreferenceStore()
//                    .setValue(LocalNetworkSharingUI.PREF_NO_BONJOUR, true);
            LocalNetworkSharingUI.getDefault().setBonjourInstalled(false);

            String os = Platform.getOS();
            if (Platform.OS_WIN32.equals(os)) {
                // Missing Apple Bonjour installation:
                LocalNetworkSharingUI.getDefault().getBonjourInstaller()
                        .installBonjour(true);
            } else if (Platform.OS_MACOSX.equals(os)) {
                // Broken Bonjour on Mac:
                SharingUtils.showDialog(SharingUtils.DIALOG_INFO,
                        SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                        status.getMessage());
            } else {
                // Not supported on other platforms:
                SharingUtils
                        .showDialog(
                                SharingUtils.DIALOG_INFO,
                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                SharingMessages.ToggleLocalNetworkSharingServiceStatusJob_NoSupportForYourOperatingSystem_dialogMessage);
            }
            return new Status(IStatus.WARNING, status.getPlugin(),
                    status.getCode(), status.getMessage(),
                    status.getException());
        }

        if (ignoreError) {
            return new Status(IStatus.WARNING, status.getPlugin(),
                    status.getCode(), status.getMessage(),
                    status.getException());
        }
        return status;
    }

    private static synchronized void setCurrentJob(Job job) {
        CURRENT_JOB = job;
    }

    private static synchronized Job getCurrentJob() {
        return CURRENT_JOB;
    }

    public static Job startToggle(ISharingService sharingService,
            boolean toActivate, Runnable onFinish, boolean ignoreError) {
        Job job = getCurrentJob();
        if (job != null) {
            job.cancel();
            setCurrentJob(null);
        }
        return startToggleJob(sharingService, toActivate, onFinish, ignoreError);
    }

    private static Job startToggleJob(ISharingService sharingService,
            boolean toActivate, Runnable onFinish, boolean ignoreError) {
        Job job = new ToggleSharingServiceStatusJob(sharingService, toActivate,
                onFinish, ignoreError);
        sharingService.registerJob(job);
        if (toActivate) {
            job.setRule(sharingService);
        }
        setCurrentJob(job);
        job.schedule();
        return job;
    }

}
