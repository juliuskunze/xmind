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

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.PlatformUI;

/**
 * This installer is intended to run only on Windows platform.
 * 
 * @author Frank Shaka
 */
public class BonjourInstaller implements ISchedulingRule {

    private boolean installing = false;

    private Job currentJob = null;

    BonjourInstaller() {
    }

    public void dispose() {
        Job job = currentJob;
        if (job != null) {
            job.cancel();
        }
    }

    public Job installBonjour(final boolean needConfirm) {
        final Object lock = this;
        synchronized (lock) {
            if (installing)
                return null;

            installing = true;
        }

        Job installJob = new Job(SharingMessages.InstallBonjourJob_jobName) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                final File installer = findBonjourInstallerFile();
                if (installer == null)
                    return Status.CANCEL_STATUS;

                if (needConfirm
                        && !SharingUtils
                                .showDialog(
                                        SharingUtils.DIALOG_CONFIRM,
                                        SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                        SharingMessages.InstallBonjourJob_ConfirmInstallBonjour_dialogMessage)) {
                    return Status.CANCEL_STATUS;
                }

                Process p = null;
                try {
                    p = Runtime.getRuntime().exec(new String[] { "cmd", "/c", //$NON-NLS-1$ //$NON-NLS-2$
                            installer.getAbsolutePath() });
                    int exitValue = p.waitFor();
                    if (exitValue != 0)
                        return new Status(IStatus.WARNING,
                                LocalNetworkSharingUI.PLUGIN_ID,
                                "Bonjour installer quit with unexpected code: " //$NON-NLS-1$
                                        + exitValue);
                } catch (InterruptedException e) {
                    if (p != null) {
                        p.destroy();
                    }
                    return Status.CANCEL_STATUS;
                } catch (Throwable e) {
                    return new Status(
                            IStatus.ERROR,
                            LocalNetworkSharingUI.PLUGIN_ID,
                            SharingMessages.InstallBonjourJob_ErrorOccurredWhileExecutingBonjourInstaller_errorMessage,
                            e);
                }

                LocalNetworkSharingUI.getDefault().setBonjourInstalled(true);
//                LocalNetworkSharingUI.getDefault().getPreferenceStore()
//                        .setValue(LocalNetworkSharingUI.PREF_NO_BONJOUR, false);
                LocalNetworkSharingUI
                        .getDefault()
                        .getPreferenceStore()
                        .setValue(LocalNetworkSharingUI.PREF_SKIP_AUTO_ENABLE,
                                false);

                monitor.done();

                if (SharingUtils
                        .showDialog(
                                SharingUtils.DIALOG_CONFIRM,
                                SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                SharingMessages.InstallBonjourJob_BonjourInstalledSuccessfully_dialogMessage)) {
                    PlatformUI.getWorkbench().getDisplay()
                            .asyncExec(new Runnable() {
                                public void run() {
                                    PlatformUI.getWorkbench().restart();
                                }
                            });
                }
                return Status.OK_STATUS;
            }

            @Override
            protected void canceling() {
                super.canceling();
                Thread t = getThread();
                if (t != null) {
                    t.interrupt();
                }
            }
        };
        installJob.setRule(this);
        installJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                super.done(event);
                synchronized (lock) {
                    installing = false;
                    currentJob = null;

                    if (event.getResult().getSeverity() == IStatus.CANCEL) {
                        IPreferenceStore prefStore = LocalNetworkSharingUI
                                .getDefault().getPreferenceStore();
                        prefStore.setValue(
                                LocalNetworkSharingUI.PREF_FEATURE_ENABLED,
                                false);
                        prefStore.setValue(
                                LocalNetworkSharingUI.PREF_SERVICE_ACTIVATED,
                                false);
                    }
                }
            }
        });
        currentJob = installJob;
        installJob.schedule();
        return installJob;
    }

    private File findBonjourInstallerFile() {
        String installerPathKey = String.format(
                "org.xmind.ui.sharing.bonjourinstaller.path.%s", //$NON-NLS-1$
                getComputerArch());
        String installerPath = System.getProperty(installerPathKey);
        if (installerPath == null || "".equals(installerPath)) { //$NON-NLS-1$
            LocalNetworkSharingUI
                    .log("Bonjour Installer Not Found: No installer path set in system properties.", //$NON-NLS-1$
                            null);
            return null;
        }

        Location base = Platform.getInstallLocation();
        if (base == null) {
            LocalNetworkSharingUI
                    .log("Bonjour Installer Not Found: Product installation location not set.", //$NON-NLS-1$
                            null);
            return null;
        }

        URL baseURL = base.getURL();
        if (baseURL == null) {
            LocalNetworkSharingUI
                    .log("Bonjour Installer Not Found: Product installation location not initialized.", //$NON-NLS-1$
                            null);
            return null;
        }

        if (!"file".equals(baseURL.getProtocol())) { //$NON-NLS-1$
            LocalNetworkSharingUI.log(
                    "Bonjour Installer Not Found: Product installation location is not a file: " //$NON-NLS-1$
                            + baseURL.toExternalForm(), null);
            return null;
        }

        File baseDir = new File(baseURL.getPath());
        File bonjourInstaller = new File(baseDir, installerPath);
        if (!bonjourInstaller.exists() || !bonjourInstaller.isFile()) {
            LocalNetworkSharingUI.log(
                    "Bonjour Installer Not Found: Bonjour installer not exists: " //$NON-NLS-1$
                            + bonjourInstaller.getAbsolutePath(), null);
            return null;
        }

        return bonjourInstaller;
    }

    /**
     * Get the actual processor architecture of the local machine.
     * 
     * @return <code>"x86_64"</code> when running in 64-bit Windows (regardless
     *         of JVM type), or any other value reported by
     *         {@link Platform#getOSArch()}
     */
    private static String getComputerArch() {
        // Reference:
        // http://blogs.msdn.com/b/david.wang/archive/2006/03/26/howto-detect-process-bitness.aspx
        String arch = System.getenv("PROCESSOR_ARCHITECTURE"); //$NON-NLS-1$
        String archWoW64 = System.getenv("PROCESSOR_ARCHITEW6432"); //$NON-NLS-1$
        if ((arch != null && arch.endsWith("64")) //$NON-NLS-1$
                || (archWoW64 != null && archWoW64.endsWith("64"))) //$NON-NLS-1$
            return "x86_64"; //$NON-NLS-1$
        return Platform.getOSArch();
    }

    public boolean contains(ISchedulingRule rule) {
        return rule == this;
    }

    public boolean isConflicting(ISchedulingRule rule) {
        return rule == this;
    }

}
