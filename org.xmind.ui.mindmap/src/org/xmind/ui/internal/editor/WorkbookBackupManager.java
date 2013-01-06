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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.xmind.ui.util.Logger;

public class WorkbookBackupManager {

    private class WorkbookBackupWorker implements Runnable {

        private WorkbookRef ref;

        private Object previousBackup;

        private Object backup = null;

        private IProgressMonitor monitor = new NullProgressMonitor();

        public WorkbookBackupWorker(WorkbookRef ref, Object previousBackup) {
            this.ref = ref;
            this.previousBackup = previousBackup;
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setName("WorkbookBackupWorker-" + ref.getKey().toString()); //$NON-NLS-1$
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

        public void cancel() {
            monitor.setCanceled(true);
        }

        public void run() {
            try {
                IWorkbookBackupMaker backupper = ref.getWorkbookBackupMaker();
                if (backupper != null) {
                    backup = backupper.backup(monitor, previousBackup);
                }
            } catch (Throwable e) {
                Logger.log(e);
                notifyBackupCanceled(ref);
            } finally {
                if (monitor.isCanceled()) {
                    notifyBackupCanceled(ref);
                } else {
                    notifyBackupFinish(ref, backup);
                }
            }
        }

    }

    private static final WorkbookBackupManager instance = new WorkbookBackupManager();

    private Map<WorkbookRef, Object> backups = new HashMap<WorkbookRef, Object>();

    private Map<WorkbookRef, WorkbookBackupWorker> workers = new HashMap<WorkbookRef, WorkbookBackupWorker>();

    private Queue<WorkbookBackupWorker> queue = new LinkedList<WorkbookBackupWorker>();

    private WorkbookBackupWorker working = null;

    private WorkbookBackupManager() {
    }

    public synchronized void addWorkbook(WorkbookRef ref) {
        if (backups.containsKey(ref))
            return;

        backups.put(ref, null);
        WorkbookBackupWorker worker = new WorkbookBackupWorker(ref, null);
        workers.put(ref, worker);
        schedule(worker);
    }

    public synchronized void removeWorkbook(WorkbookRef ref) {
        if (!backups.containsKey(ref))
            return;

        backups.remove(ref);
        WorkbookBackupWorker worker = workers.remove(ref);
        if (worker != null) {
            worker.cancel();
            queue.remove(worker);
        }
    }

    public Object ensureBackedUp(WorkbookRef ref) {
        if (!workers.containsKey(ref)) {
            WorkbookBackupWorker worker = new WorkbookBackupWorker(ref,
                    backups.get(ref));
            workers.put(ref, worker);
            schedule(worker);
        }
        while (workers.containsKey(ref)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
        return backups.get(ref);
    }

    private void schedule(WorkbookBackupWorker worker) {
        if (working != null) {
            queue.offer(worker);
        } else {
            working = worker;
            worker.start();
        }
    }

    private void notifyBackupCanceled(WorkbookRef ref) {
        WorkbookBackupWorker worker = workers.remove(ref);
        if (worker != null) {
            worker.cancel();
            queue.remove(worker);
        }
        pollQueue();
    }

    private void notifyBackupFinish(WorkbookRef ref, Object backup) {
        backups.put(ref, backup);
        WorkbookBackupWorker worker = workers.remove(ref);
        if (worker != null) {
            queue.remove(worker);
        }
        pollQueue();
    }

    private void pollQueue() {
        working = queue.poll();
        if (working != null) {
            working.start();
        }
    }

    public static WorkbookBackupManager getInstance() {
        return instance;
    }

}
