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
package org.xmind.cathy.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.xmind.cathy.internal.actions.SimpleOpenAction;
import org.xmind.ui.internal.editor.WorkbookEditorInput;
import org.xmind.ui.internal.editor.WorkbookRefManager;
import org.xmind.ui.internal.workbench.Util;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class CathyWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    private class ListLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof IEditorInput)
                return ((IEditorInput) element).getName();
            return null;
        }

        public Image getImage(Object element) {
            if (element instanceof IEditorInput) {
                ImageDescriptor image = MindMapUI.getImages().get(
                        IMindMapImages.XMIND_ICON);
                if (image != null)
                    return image.createImage();
            }
            return null;
        }
    }

    public CathyWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(
            IActionBarConfigurer configurer) {
        return new CathyWorkbenchActionBuilder(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(Util.getInitialWindowSize());
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
        configurer.setTitle(WorkbenchMessages.AppWindowTitle);
    }

    public void postWindowOpen() {
        super.postWindowOpen();
        final IWorkbenchWindow window = getWindowConfigurer().getWindow();
        if (window != null) {
            CoolBarManager coolBar = ((WorkbenchWindow) window)
                    .getCoolBarManager();
            if (coolBar != null) {
                coolBar.setLockLayout(true);
            }

            window.getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    postOpen(window);
                }
            });
        }
    }

    private void postOpen(final IWorkbenchWindow window) {
        checkLog(window);
        Object[] lastSession = checkLastSession();
        if (lastSession != null && lastSession.length > 0) {
            for (Object input : lastSession) {
                new WorkbookEditorInput();
                openEditor((IEditorInput) input, window);
            }
        } else {
            openEditor(null, window);
        }

        window.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                WorkbookRefManager.getInstance().clearLastSession();
            }
        });
    }

    private void openEditor(final IEditorInput input,
            final IWorkbenchWindow window) {
        window.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        IWorkbenchPage activePage = window.getActivePage();
                        if (input != null) {
                            activePage.openEditor(input,
                                    MindMapUI.MINDMAP_EDITOR_ID);
                        } else {
                            if (window.getActivePage().getActiveEditor() == null)
                                activePage.openEditor(
                                        new WorkbookEditorInput(),
                                        MindMapUI.MINDMAP_EDITOR_ID);
                        }
                    }
                });
            }
        });
    }

    private Object[] checkLastSession() {
        List<IEditorInput> session = WorkbookRefManager.getInstance()
                .loadLastSession();
        if (session == null || session.isEmpty())
            return null;
        ListSelectionDialog dialog = new ListSelectionDialog(null, session,
                new ArrayContentProvider(), new ListLabelProvider(),
                WorkbenchMessages.appWindow_ListSelectionDialog_Text);
        dialog.setTitle(WorkbenchMessages.appWindow_ListSelectionDialog_Title);
        dialog.setInitialElementSelections(session);
        dialog.open();
        return dialog.getResult();
    }

    private void checkLog(IWorkbenchWindow window) {
        Log opening = Log.get(Log.OPENING);
        if (opening.exists()) {
            boolean presentation = false;
            List<String> files = new ArrayList<String>();
            String[] contents = opening.getContents();
            for (String line : contents) {
                if ("-p".equals(line)) { //$NON-NLS-1$
                    presentation = true;
                } else
                    files.add(line);
            }
            if (files.isEmpty())
                return;
            for (String file : files) {
                open(window, file, presentation);
                if (presentation)
                    presentation = false;
            }
            files.clear();
            opening.delete();
        }
    }

    private void open(IWorkbenchWindow window, String path, boolean presentation) {
        File file = new File(path);
        if (file.isFile() && file.canRead()) {
            window.getShell().getDisplay().asyncExec(
                    new SimpleOpenAction(window, path, presentation));
        }
    }
}