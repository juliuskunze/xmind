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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.xmind.core.internal.InternalCore;
import org.xmind.ui.blackbox.BlackBox;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

/**
 * @author Frank Shaka
 * 
 */
public class BackgroundWorkbookSaver {

    private static final BackgroundWorkbookSaver INSTANCE = new BackgroundWorkbookSaver();

    private static boolean DEBUGGING = MindMapUIPlugin
            .isDebugging("/debug/autosave"); //$NON-NLS-1$

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
            monitor.beginTask(null, 1);
            try {
                do {
                    IStatus slept = sleep(monitor);
                    if (slept != null && !slept.isOK())
                        return slept;

                    if (DEBUGGING)
                        System.out.println("AutoSave starts now..."); //$NON-NLS-1$

                    Object[] refs = WorkbookRefManager.getInstance()
                            .getWorkbookRefs().toArray();
                    for (int i = 0; i < refs.length; i++) {
                        if (refs[i] instanceof WorkbookRef) {
                            WorkbookRef ref = (WorkbookRef) refs[i];
                            if (ref.getWorkbook() != null)
                                BlackBox.doBackup(ref.getWorkbook().getFile());
                            save(monitor, ref);
                        }
                        if (monitor.isCanceled())
                            return Status.CANCEL_STATUS;
                    }

                    if (DEBUGGING)
                        System.out.println("AutoSave finishes."); //$NON-NLS-1$

                } while (!monitor.isCanceled());
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;
                return Status.OK_STATUS;
            } catch (Throwable e) {
                if (e instanceof InterruptedException)
                    return Status.CANCEL_STATUS;

                if (DEBUGGING) {
                    System.err.println("AutoSave error:"); //$NON-NLS-1$
                    e.printStackTrace();
                }

                String msg = "Background workbook saver daemon ended with unknown error"; //$NON-NLS-1$
                Logger.log(e, msg);
                return new Status(IStatus.WARNING, MindMapUI.PLUGIN_ID,
                        IStatus.ERROR, msg, e);
            }
        }

        private IStatus sleep(IProgressMonitor monitor) {
            int total = intervals;
            try {
                if (DEBUGGING && total > 5000) {
                    Thread.sleep(total - 5000);
                    System.out.println("AutoSave will start in 5 seconds..."); //$NON-NLS-1$
                    Thread.sleep(3000);
                    System.out.println("AutoSave will start in 2 seconds..."); //$NON-NLS-1$
                    Thread.sleep(2000);
                } else {
                    if (DEBUGGING)
                        System.out.println("AutoSave will start in " //$NON-NLS-1$
                                + (total / 1000) + " seconds..."); //$NON-NLS-1$
                    Thread.sleep(total);
                }
            } catch (InterruptedException e) {
                return Status.CANCEL_STATUS;
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            return Status.OK_STATUS;
        }

        private void save(IProgressMonitor monitor, WorkbookRef ref) {
            synchronized (ref.getIOLock()) {
                if (!ref.isReady() || !ref.isSaveable()
                        || ref.getWorkbook() == null || !ref.isContentDirty())
                    return;

                IWorkbookSaver saver = ref.getWorkbookSaver();
                if (saver != null && saver.willOverwriteTarget()) {
                    try {
                        if (DEBUGGING)
                            System.out.println("AutoSave start: " + ref); //$NON-NLS-1$
                        ref.saveWorkbook(new SubProgressMonitor(monitor, 0),
                                null, true);
                        if (InternalCore.DEBUG_WORKBOOK_SAVE)
                            Logger.log("BackgroundWorkbookSaver: Finished saving workbook in background: " //$NON-NLS-1$
                                    + ref.getKey().toString());
                        if (DEBUGGING)
                            System.out.println("AutoSave finished: " + ref); //$NON-NLS-1$
                    } catch (Throwable e) {
                        if (DEBUGGING) {
                            System.err.println("AutoSave error: " + ref); //$NON-NLS-1$
                            e.printStackTrace();
                        }
                        Logger.log(e,
                                "BackgroundWorkbookSaver: Failed to save workbook: " //$NON-NLS-1$
                                        + ref.getKey().toString());
                    }
                }
            }
        }

        protected void canceling() {
            super.canceling();
            Thread t = getThread();
            if (t != null)
                t.interrupt();
        }

    }

    private DaemonJob daemon = null;

    public synchronized void reset(int intervals, boolean enabled) {
        stopAll();
        if (enabled) {
            daemon = new DaemonJob(intervals);
            daemon.schedule();
        }
    }

    public synchronized boolean isRunning() {
        return daemon != null;
    }

    /**
     * 
     */
    public synchronized void stopAll() {
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
        return INSTANCE;
    }

}
