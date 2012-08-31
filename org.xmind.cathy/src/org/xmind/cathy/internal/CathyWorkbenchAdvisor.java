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
package org.xmind.cathy.internal;

import net.xmind.signin.ILicenseInfo;
import net.xmind.signin.ILicenseListener;
import net.xmind.signin.XMindNet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.xmind.cathy.internal.jobs.StartupJob;
import org.xmind.ui.internal.dialogs.DialogMessages;

public class CathyWorkbenchAdvisor extends WorkbenchAdvisor implements
        ILicenseListener {

    private static final String PERSPECTIVE_ID = "org.xmind.ui.perspective.mindmapping"; //$NON-NLS-1$

    private OpenDocumentHandler openDocumentHandler;

    /**
     * 
     */
    public CathyWorkbenchAdvisor(OpenDocumentHandler openDocumentHandler) {
        this.openDocumentHandler = openDocumentHandler;
    }

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer) {
        return new CathyWorkbenchWindowAdvisor(configurer);
    }

    public String getInitialWindowPerspectiveId() {
        if (Platform.getBundle("org.xmind.meggy") != null) { //$NON-NLS-1$
            return "org.xmind.ui.meggy.perspective"; //$NON-NLS-1$
        }
        return PERSPECTIVE_ID;
    }

    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        configurer.setSaveAndRestore(true);
        configurer.setExitOnLastWindowClose(true);
    }

    @Override
    public void preStartup() {
        super.preStartup();
        XMindNet.addLicenseListener(this);
        licenseVerified(XMindNet.getLicenseInfo());
    }

    @Override
    public void postStartup() {
        super.postStartup();
        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        new StartupJob(workbench, false).schedule();
    }

    @Override
    public void postShutdown() {
        XMindNet.removeLicenseListener(this);
        super.postShutdown();
    }

    public boolean preShutdown() {
        boolean readyToShutdown = super.preShutdown();
        if (readyToShutdown) {
            readyToShutdown = saveAllEditorsOnClose();
        }
        if (readyToShutdown) {
            readyToShutdown = saveWorkbenchSession();
        }
        return readyToShutdown;
    }

    private boolean saveWorkbenchSession() {
        if (CathyPlugin.getDefault().getPreferenceStore()
                .getInt(CathyPlugin.STARTUP_ACTION) != CathyPlugin.STARTUP_ACTION_LAST) {
            boolean allEditorsClosed = closeAllEditors();
            if (!allEditorsClosed)
                return false;
        }
        return true;
    }

    private boolean saveAllEditorsOnClose() {
        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            IWorkbenchPage page = window.getActivePage();
            for (IEditorReference editorRef : page.getEditorReferences()) {
                final IEditorPart editor = editorRef.getEditor(false);
                if (editor != null && editor.isDirty()) {
                    int answer = promptToSaveOnClose(window, page, editor);
                    if (answer == ISaveablePart2.CANCEL)
                        return false;
                    if (answer != ISaveablePart2.NO) {
                        if (!doSaveEditor(window, editor)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private int promptToSaveOnClose(IWorkbenchWindow window,
            IWorkbenchPage page, IEditorPart editor) {
        if (editor instanceof ISaveablePart2) {
            int answer = ((ISaveablePart2) editor).promptToSaveOnClose();
            if (answer != ISaveablePart2.DEFAULT)
                return answer;
        }
        page.activate(editor);
        MessageDialog dialog = new MessageDialog(window.getShell(),
                DialogMessages.Save_title, null, NLS.bind(
                        WorkbenchMessages.PromptSaveEditorOnClosing_message,
                        editor.getTitle()), MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        int answerIndex = dialog.open();
        switch (answerIndex) {
        case 0:
            return ISaveablePart2.YES;
        case 1:
            return ISaveablePart2.NO;
        default:
            return ISaveablePart2.CANCEL;
        }
    }

    private boolean doSaveEditor(final IWorkbenchWindow window,
            final IEditorPart editor) {
        final boolean[] saved = new boolean[1];
        saved[0] = false;
        window.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        final IProgressMonitor monitor = new NullProgressMonitor();
                        editor.doSave(monitor);
                        if (!monitor.isCanceled()) {
                            saved[0] = true;
                        }
                    }
                });
            }
        });
        return saved[0];
    }

    private boolean closeAllEditors() {
        boolean closed = false;
        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            closed |= window.getActivePage().closeAllEditors(false);
        }
        return closed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.application.WorkbenchAdvisor#eventLoopIdle(org.eclipse
     * .swt.widgets.Display)
     */
    @Override
    public void eventLoopIdle(Display display) {
        if (openDocumentHandler != null) {
            openDocumentHandler.checkAndOpenFiles(getWorkbenchConfigurer()
                    .getWorkbench());
        }
        super.eventLoopIdle(display);
    }

    public void licenseVerified(ILicenseInfo info) {
        String name = info.getLicensedTo();
        if (name != null && !"".equals(name)) { //$NON-NLS-1$
            System.setProperty("org.xmind.product.distribution.description", //$NON-NLS-1$
                    NLS.bind(WorkbenchMessages.About_LicensedTo, name));
        } else {
            System.setProperty("org.xmind.product.distribution.description", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        int type = info.getType();
        String licenseType;
        if ((type & ILicenseInfo.VALID_PRO_LICENSE) != 0) {
            licenseType = WorkbenchMessages.About_ProTitle;
        } else if ((type & ILicenseInfo.VALID_PLUS_LICENSE) != 0) {
            licenseType = WorkbenchMessages.About_PlusTitle;
        } else if ((type & ILicenseInfo.VALID_PRO_SUBSCRIPTION) != 0) {
            licenseType = WorkbenchMessages.About_ProSubscriptionTitle;
        } else {
            licenseType = null;
        }
        if (licenseType == null) {
            licenseType = WorkbenchMessages.About_LicenseType_Unactivated;
        } else {
            licenseType = NLS.bind(WorkbenchMessages.About_LicenseTypePattern,
                    licenseType);
        }
        System.setProperty("org.xmind.product.license.type", //$NON-NLS-1$ 
                licenseType);
    }

}