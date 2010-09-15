/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

/**
 * @author Frank Shaka
 * 
 */
public class AutoSaveService implements IStartup, IWorkbenchListener,
        IPropertyChangeListener {

    private class AutoSaveJob {

        /**
         * @author Frank Shaka
         * 
         */
        private class AutoSaveJobImpl extends Job {
            /**
             * @param name
             */
            private AutoSaveJobImpl(String name) {
                super(name);
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime
             * .IProgressMonitor)
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (monitor.isCanceled() || workbench == null)
                    return Status.CANCEL_STATUS;

                try {
                    Thread.sleep(intervals);
                } catch (InterruptedException e) {
                    if (monitor.isCanceled() || workbench == null)
                        return Status.CANCEL_STATUS;
                }

                while (!monitor.isCanceled()) {

                    doSaveAll();

                    if (monitor.isCanceled() || workbench == null)
                        return Status.CANCEL_STATUS;

                    try {
                        Thread.sleep(intervals);
                    } catch (InterruptedException e) {
                        if (monitor.isCanceled() || workbench == null)
                            return Status.CANCEL_STATUS;
                    }

                }

                return Status.OK_STATUS;
            }
        }

        private Job job;

        private int intervals = -1;

        /**
         * @param name
         */
        public AutoSaveJob(int intervals) {
            this.intervals = intervals;
            this.job = new AutoSaveJobImpl("Auto Save All Editors"); //$NON-NLS-1$
            this.job.setSystem(true);
            this.job.setPriority(Job.LONG);
            this.job.schedule();
        }

        public void stop() {
            job.cancel();
            Thread thread = job.getThread();
            if (thread != null) {
                thread.interrupt();
            }
        }

        /**
         * @param intervals
         *            the intervals to set
         */
        public void setIntervals(int intervals) {
            this.intervals = intervals;
            Thread thread = job.getThread();
            if (thread != null) {
                thread.interrupt();
            }
        }

    }

    private IWorkbench workbench;

    private AutoSaveJob job = null;

    /**
     * 
     */
    public AutoSaveService() {
    }

//    public void dispose() {
//        stopJob();
//        workbench = null;
//        CathyPlugin.getDefault().getPreferenceStore()
//                .removePropertyChangeListener(this);
//    }

    /**
     * 
     */
    private void checkState() {
        if (isEnabled()) {
            ensureJobRunning();
        } else {
            stopJob();
        }
    }

    /**
     * 
     */
    private void ensureJobRunning() {
        if (workbench == null)
            return;

        if (job != null)
            return;

        job = new AutoSaveJob(getIntervals());
    }

    /**
     * 
     */
    private void stopJob() {
        if (job != null) {
            job.stop();
            job = null;
        }
    }

    /**
     * @return
     */
    private boolean isEnabled() {
        return CathyPlugin.getDefault().getPreferenceStore().getBoolean(
                CathyPlugin.AUTO_SAVE_ENABLED);
    }

    private int getIntervals() {
        return CathyPlugin.getDefault().getPreferenceStore().getInt(
                CathyPlugin.AUTO_SAVE_INTERVALS) * 60000;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
     * .jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (CathyPlugin.AUTO_SAVE_ENABLED.equals(property)) {
            checkState();
        } else if (CathyPlugin.AUTO_SAVE_INTERVALS.equals(property)) {
            if (job != null) {
                job.setIntervals(getIntervals());
            }
        }
    }

    /**
     * 
     */
    private void doSaveAll() {
        if (workbench == null)
            return;

        try {
            workbench.getDisplay().syncExec(new Runnable() {
                public void run() {
                    if (workbench == null)
                        return;

                    workbench.saveAllEditors(false);
                }
            });
        } catch (Throwable e) {
            CathyPlugin.log(e, "Error occurred while auto saving."); //$NON-NLS-1$
        }
    }

    public void earlyStartup() {
        this.workbench = PlatformUI.getWorkbench();
        this.workbench.addWorkbenchListener(this);
        CathyPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
        checkState();
    }

    public void postShutdown(IWorkbench workbench) {
        if (this.workbench == null)
            return;
        this.workbench.removeWorkbenchListener(this);
        this.workbench = null;
        CathyPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
        stopJob();
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        return true;
    }

}
