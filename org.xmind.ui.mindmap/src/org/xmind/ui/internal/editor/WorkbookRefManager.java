/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.CommandStack;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefManager;
import org.xmind.ui.util.Logger;

public class WorkbookRefManager implements IWorkbookRefManager {

    private class AutoHibernateJob implements Runnable {

        public void run() {
            try {
                Thread.sleep(getAutoHibernateIntervals());
            } catch (InterruptedException e) {
            }

            while (autoHibernateThread == Thread.currentThread()) {

                hibernateAll();

                if (autoHibernateThread != Thread.currentThread())
                    break;

                try {
                    Thread.sleep(getAutoHibernateIntervals());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

    }

    private Map<Object, WorkbookRef> registry = new HashMap<Object, WorkbookRef>();

    private static WorkbookRefManager instance;

    private Thread autoHibernateThread = null;

    private long autoHibernateIntervals = 60000;

    private WorkbookRefManager() {
    }

    public WorkbookRef addReferrer(Object source, IEditorPart referrer)
            throws CoreException {
        WorkbookRef ref = registry.get(source);
        if (ref != null) {
            ref.addReferrer(referrer);
            return ref;
        }
        ref = createWorkbookRef(source, referrer);
        ref.addReferrer(referrer);
        ensureAutoHibernateStarted();
        return ref;
    }

    private WorkbookRef createWorkbookRef(Object source, IEditorPart referrer)
            throws CoreException {
        WorkbookRef ref = new WorkbookRef();
        WorkbookRefInitializer.getInstance().initialize(ref, source, referrer);
        if (!ref.isReady()) {
            IStatus status = new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID,
                    "Failed to create a workbook reference with valid source."); //$NON-NLS-1$
            throw new CoreException(status);
        }
        initializeRef((WorkbookRef) ref, source);
        registry.put(source, ref);
        return ref;
    }

    private void initializeRef(WorkbookRef ref, Object source)
            throws CoreException {
        if (ref.getCommandStack() == null) {
            ref.setCommandStack(new CommandStack());
        }
        if (ref.getKey() == null) {
            ref.setKey(source);
        }
    }

    public void removeReferrer(Object source, IEditorPart referrer) {
        WorkbookRef ref = registry.get(source);
        try {
            if (ref != null) {
                try {
                    ref.removeReferrer(referrer);
                } finally {
                    if (!ref.isReferred()) {
                        registry.remove(source);

                        // Have to check if there's still other references
                        // relying on this workbook's resources.
                        boolean noRelatedRef = findRef(ref.getWorkbook()) == null;

                        ref.dispose(noRelatedRef);
                    }
                }
            }
        } finally {
            if (registry.isEmpty()) {
                stopAutoHibernate();
            }
        }
    }

    public IWorkbookRef createRef(IEditorInput editorInput, IEditorPart editor) {
        try {
            return addReferrer(editorInput, editor);
        } catch (CoreException e) {
            Logger.log(e);
            return null;
        }
    }

    public void disposeRef(IEditorInput editorInput, IEditorPart editor) {
        removeReferrer(editorInput, editor);
    }

    public IWorkbookRef findRef(IWorkbook workbook) {
        if (workbook == null)
            return null;
        for (IWorkbookRef ref : registry.values()) {
            if (workbook.equals(ref.getWorkbook()))
                return ref;
        }
        return null;
    }

    public static WorkbookRefManager getInstance() {
        if (instance == null) {
            instance = new WorkbookRefManager();
        }
        return instance;
    }

    public void changeKey(Object oldKey, Object newKey, Object referrer)
            throws CoreException {
        WorkbookRef ref = registry.remove(oldKey);
        if (ref == null)
            return;
        ref.setKey(newKey);
        registry.put(newKey, ref);
        ref.setWorkbookLoader(null).setWorkbookSaver(null);
        WorkbookRefInitializer.getInstance().initialize(ref, newKey, referrer);
    }

    public long getAutoHibernateIntervals() {
        return autoHibernateIntervals;
    }

    public void setAutoHibernateIntervals(long autoHibernateIntervals) {
        this.autoHibernateIntervals = autoHibernateIntervals;
    }

    private void ensureAutoHibernateStarted() {
        if (autoHibernateThread != null)
            return;

        autoHibernateThread = new Thread(new AutoHibernateJob());
        autoHibernateThread.setDaemon(true);
        autoHibernateThread.setName("XMind: Auto Save Temporary Workbooks"); //$NON-NLS-1$
        autoHibernateThread.setPriority(Thread.MIN_PRIORITY);
        autoHibernateThread.start();
    }

    private void hibernateAll() {
        if (registry.isEmpty()) {
            stopAutoHibernate();
            return;
        }

        for (WorkbookRef ref : registry.values()) {
            IWorkbook workbook = ref.getWorkbook();
            if (workbook != null) {
                try {
                    workbook.saveTemp();
                } catch (Throwable e) {
                    // do users want to see this exception?
                }
            }
        }
    }

    private void stopAutoHibernate() {
        if (autoHibernateThread != null) {
            autoHibernateThread.interrupt();
        }
        autoHibernateThread = null;
    }

}