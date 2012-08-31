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

package org.xmind.ui.internal.editor;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

/**
 * @author Frank Shaka
 * 
 */
public class BackgroundWorkbookSaver {

    private static BackgroundWorkbookSaver INSTANCE = null;

    private static class DaemonJob extends Job {

        private int intervals;

        /**
         * @param name
         */
        public DaemonJob(int intervals) {
            super("Background Workbook Saver Daemon"); //$NON-NLS-1$
            setSystem(true);
            setPriority(LONG);
            this.intervals = intervals;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                runWithProgress(monitor);
            } catch (Throwable e) {
                if (monitor.isCanceled() || e instanceof InterruptedException)
                    return Status.CANCEL_STATUS;
                String msg = "Background workbook saver daemon ended with unknown error"; //$NON-NLS-1$
                Logger.log(e, msg);
                return new Status(IStatus.WARNING, MindMapUI.PLUGIN_ID,
                        IStatus.ERROR, msg, e);
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            return Status.OK_STATUS;
        }

        private void runWithProgress(IProgressMonitor monitor) throws Throwable {
            do {
                sleep(monitor);

                if (monitor.isCanceled())
                    return;

                try {
                    invoke(monitor);
                    if (monitor.isCanceled())
                        return;
                } catch (Throwable e) {
                    if (monitor.isCanceled())
                        return;
                    Logger.log(e);
                }
            } while (!monitor.isCanceled());
        }

        private void invoke(IProgressMonitor monitor) {
            BackupJob job = new BackupJob();
            job.schedule();
            try {
                job.join();
            } catch (InterruptedException e) {
            }
        }

        private void sleep(IProgressMonitor monitor) {
            long start = System.currentTimeMillis();
            long end = start + intervals;
            while (System.currentTimeMillis() < end) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                if (monitor.isCanceled())
                    return;
            }
        }

    }

    private static class BackupJob extends Job {

        /**
         * @param name
         */
        public BackupJob() {
            super("Save Workbooks In Background"); //$NON-NLS-1$
            setSystem(true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                runWithProgress(monitor);
            } catch (Throwable e) {
                if (monitor.isCanceled() || e instanceof InterruptedException)
                    return Status.CANCEL_STATUS;
                String msg = "Background workbook saver ended with unknown error"; //$NON-NLS-1$
                Logger.log(e, msg);
                return new Status(IStatus.WARNING, MindMapUI.PLUGIN_ID,
                        IStatus.ERROR, msg, e);
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            return Status.OK_STATUS;
        }

        private void runWithProgress(IProgressMonitor monitor) throws Throwable {
            Collection<IWorkbookRef> refs = WorkbookRefManager.getInstance()
                    .getWorkbookRefs();
            monitor.beginTask(
                    MindMapMessages.BackgroundWorkbookSaver_SaveWorkbook_taskName,
                    refs.size());
            for (IWorkbookRef ref : refs) {
                try {
                    save(ref, monitor);
                    if (monitor.isCanceled())
                        return;
                } catch (Throwable e) {
                    if (monitor.isCanceled())
                        return;
                    Logger.log(e);
                }
                monitor.worked(1);
            }
        }

        private void save(IWorkbookRef ref, IProgressMonitor monitor)
                throws Throwable {
            WorkbookRef r = (WorkbookRef) ref;
            if (!r.isReady() || !r.isSaveable())
                return;

            IWorkbook workbook = ref.getWorkbook();
            if (workbook == null)
                return;

            if (!r.isContentDirty())
                return;

            monitor.subTask(NLS
                    .bind(MindMapMessages.BackgroundWorkbookSaver_SavingWorkbook_taskNamePattern,
                            workbook.getFile()));

            IWorkbookSaver saver = r.getWorkbookSaver();
            if (saver != null && saver.canSaveToTarget()) {
                r.saveWorkbook(monitor, null, true);
            }
        }

    }

    private DaemonJob daemon = null;

    public void runWith(int intervals, boolean enabled) {
        stopAll();
        if (enabled) {
            daemon = new DaemonJob(intervals);
            daemon.schedule();
        }
    }

    public boolean isRunning() {
        return daemon != null;
    }

    /**
     * 
     */
    public void stopAll() {
        if (daemon != null) {
            Thread thread = daemon.getThread();
            daemon.cancel();
            if (thread != null) {
                thread.interrupt();
            }
            daemon = null;
        }
    }

    public static BackgroundWorkbookSaver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BackgroundWorkbookSaver();
        }
        return INSTANCE;
    }

}
