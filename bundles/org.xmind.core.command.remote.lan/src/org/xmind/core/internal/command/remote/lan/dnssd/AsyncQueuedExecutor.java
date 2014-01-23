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
package org.xmind.core.internal.command.remote.lan.dnssd;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.xmind.core.internal.command.remote.lan.LANRemoteCommandPlugin;

/**
 * This class runs tasks sequentially in a dedicated thread.
 * 
 * @author Frank Shaka
 */
public class AsyncQueuedExecutor implements Executor {

    private Thread thread;

    private Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

    public AsyncQueuedExecutor(String threadName) {
        thread = new Thread(new Runnable() {
            public void run() {
                mainLoop();
            }
        }, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    public void dispose() {
        synchronized (this) {
            Thread theThread = this.thread;
            this.taskQueue.clear();
            this.thread = null;
            if (theThread != null) {
                theThread.interrupt();
            }
        }
    }

    public void execute(final Runnable task) {
        synchronized (this) {
            if (thread == null)
                return;
            taskQueue.offer(task);
        }
    }

    private void mainLoop() {
        try {
            Runnable task;
            while (thread != null) {
                if ((task = taskQueue.poll()) != null) {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        handleTaskException(task, e);
                    }
                }
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            // quit on interrupt
        }
    }

    protected void handleTaskException(Runnable task, Throwable e) {
        LANRemoteCommandPlugin.log(null, e);
    }

}
