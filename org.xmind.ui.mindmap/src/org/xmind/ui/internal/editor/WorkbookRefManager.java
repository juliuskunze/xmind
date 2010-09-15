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
package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.CommandStack;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefManager;
import org.xmind.ui.util.Logger;

public class WorkbookRefManager implements IWorkbookRefManager {

    private static final String TAG_OPENED_EDITORS = "editors"; //$NON-NLS-1$
    private static final String TAG_OPENED_EDITOR = "editor"; //$NON-NLS-1$
    private static final String TAG_OPENED_INPUT = "input"; //$NON-NLS-1$
    private static final String ATTR_OPENED_ID = "factoryID"; //$NON-NLS-1$
    private static final String TAG_OPENED_TEMPLOCATION = "tempLocation"; //$NON-NLS-1$
    private static final String ATTR_OPENED_FILE = "path"; //$NON-NLS-1$
    private static final String ATTR_OPENED_TITLE = "title"; //$NON-NLS-1$

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

    private Map<IEditorInput, String> lastSession = null;

    private static WorkbookRefManager instance;

    private Thread autoHibernateThread = null;

    private long autoHibernateIntervals = 60000;

    private File location = null;

    private WorkbookRefManager() {
    }

    public synchronized WorkbookRef addReferrer(Object source,
            IWorkbookReferrer referrer) throws CoreException {
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

    private synchronized WorkbookRef createWorkbookRef(Object source,
            IWorkbookReferrer referrer) throws CoreException {
        WorkbookRef ref = new WorkbookRef();
        WorkbookRefInitializer.getInstance().initialize(ref, source, referrer);
        if (!ref.isReady()) {
            IStatus status = new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID,
                    "Failed to create a workbook reference with valid source."); //$NON-NLS-1$
            throw new CoreException(status);
        }
        initializeRef((WorkbookRef) ref, source);

        if (lastSession != null) {
            String tempLocation = lastSession.get(source);
            if (tempLocation != null) {
                File file = MME.getFile(source);
                ref.setWorkbookLoader(new TempWorkbookLoader(ref, tempLocation,
                        file == null ? null : file.getAbsolutePath()));
            }
        }

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

    public synchronized void removeReferrer(Object source,
            IWorkbookReferrer referrer) {
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

    public synchronized IWorkbookRef createRef(IEditorInput editorInput,
            IEditorPart editor) {
        IWorkbookReferrer referrer = findWorkbookReferrer(editor);
        if (referrer == null)
            return null;
        try {
            return addReferrer(editorInput, referrer);
        } catch (CoreException e) {
            Logger.log(e);
            return null;
        }
    }

    private IWorkbookReferrer findWorkbookReferrer(IEditorPart editor) {
        IWorkbookReferrer referrer = null;
        if (editor instanceof IWorkbookReferrer)
            referrer = (IWorkbookReferrer) editor;
        else
            referrer = (IWorkbookReferrer) editor
                    .getAdapter(IWorkbookReferrer.class);
        return referrer;
    }

    public synchronized void disposeRef(IEditorInput editorInput,
            IEditorPart editor) {
        IWorkbookReferrer referrer = findWorkbookReferrer(editor);
        if (referrer != null)
            removeReferrer(editorInput, referrer);
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

    void changeKey(WorkbookRef ref, Object oldKey, Object newKey) {
        WorkbookRef oldRef = registry.remove(oldKey);
        if (oldRef != ref)
            return;
        registry.put(newKey, ref);
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

        XMLMemento memento = null;
        synchronized (this) {
            for (Entry<Object, WorkbookRef> en : registry.entrySet()) {
                WorkbookRef ref = en.getValue();
                IWorkbook workbook = ref.getWorkbook();
                if (workbook != null) {
                    try {
                        workbook.saveTemp();
                    } catch (Throwable e) {
                    }
                    Object key = en.getKey();
                    if (key instanceof IEditorInput) {
                        if (memento == null) {
                            memento = XMLMemento
                                    .createWriteRoot(TAG_OPENED_EDITORS);
                        }
                        saveMemento(memento, (IEditorInput) key, workbook
                                .getTempLocation());
                    }
                }
            }
        }
        if (location == null) {
            location = new File(Core.getWorkspace().getTempFile(".opened")); //$NON-NLS-1$
        }
        if (memento != null) {
            try {
                memento.save(new OutputStreamWriter(new FileOutputStream(
                        location), "utf-8")); //$NON-NLS-1$
            } catch (IOException e) {
                Logger.log(e, "Failed to save session log."); //$NON-NLS-1$
            }
        } else {
            location.delete();
        }
    }

    private void saveMemento(XMLMemento memento, IEditorInput input, String path) {
        IMemento editorMem = memento.createChild(TAG_OPENED_EDITOR);
        IMemento inputMem = editorMem.createChild(TAG_OPENED_INPUT);
        IPersistableElement p = input.getPersistable();
        if (p != null) {
            p.saveState(inputMem);
            String id = p.getFactoryId();
            inputMem.putString(ATTR_OPENED_ID, id);
        } else {
            inputMem.putString(ATTR_OPENED_TITLE, input.getName());
        }

        IMemento pathMem = editorMem.createChild(TAG_OPENED_TEMPLOCATION);
        pathMem.putString(ATTR_OPENED_FILE, path);
    }

    private void stopAutoHibernate() {
        if (autoHibernateThread != null) {
            autoHibernateThread.interrupt();
        }
        autoHibernateThread = null;
    }

    public List<IEditorInput> loadLastSession() {
        File file = new File(Core.getWorkspace().getTempFile(".opened")); //$NON-NLS-1$
        if (!file.exists())
            return null;
        lastSession = null;
        return loadSessionFromFile(file);
    }

    private List<IEditorInput> loadSessionFromFile(File file) {
        List<IEditorInput> list = null;
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "utf-8"); //$NON-NLS-1$
        } catch (Exception e1) {
            Logger.log(e1, "Failed to read file"); //$NON-NLS-1$
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        try {
            XMLMemento memento = XMLMemento.createReadRoot(reader);
            IMemento[] elements = memento.getChildren(TAG_OPENED_EDITOR);
            for (IMemento editorMem : elements) {
                IMemento inputMem = editorMem.getChild(TAG_OPENED_INPUT);
                IEditorInput input = null;
                if (inputMem == null)
                    continue;
                String factoryId = inputMem.getString(ATTR_OPENED_ID);
                if (factoryId != null) {
                    IElementFactory factory = PlatformUI.getWorkbench()
                            .getElementFactory(factoryId);
                    input = (IEditorInput) factory.createElement(inputMem);
                } else {
                    String title = inputMem.getString(ATTR_OPENED_TITLE);
                    input = new WorkbookEditorInput(title);
                }
                IMemento tempMem = editorMem.getChild(TAG_OPENED_TEMPLOCATION);
                String path = tempMem.getString(ATTR_OPENED_FILE);

                if (lastSession == null)
                    lastSession = new HashMap<IEditorInput, String>();
                lastSession.put(input, path);
                if (list == null)
                    list = new ArrayList<IEditorInput>();
                list.add(input);
            }
            return list;
        } catch (Exception e) {
            Logger.log(e, "Failed to load session log."); //$NON-NLS-1$
        } finally {
            try {
                reader.close();
            } catch (IOException e2) {
            }
        }
        return list;
    }

    public void clearLastSession() {
        lastSession = null;
    }

}