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

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.xmind.cathy.internal.jobs.StartupJob;

public class CathyWorkbenchAdvisor extends WorkbenchAdvisor {

    private static final String PERSPECTIVE_ID = "org.xmind.ui.perspective.mindmapping"; //$NON-NLS-1$

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer) {
        return new CathyWorkbenchWindowAdvisor(configurer);
    }

    public String getInitialWindowPerspectiveId() {
        return PERSPECTIVE_ID;
    }

    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        configurer.setSaveAndRestore(true);
        configurer.setExitOnLastWindowClose(true);
    }

    @Override
    public void postStartup() {
        super.postStartup();
        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        new StartupJob(workbench).schedule();
    }

    public boolean preShutdown() {
        boolean readyToShutDown = super.preShutdown();
        if (readyToShutDown) {
//            if (!CathyPlugin.getDefault().getPreferenceStore().getBoolean(
//                    CathyPlugin.RESTORE_LAST_SESSION)) {
            if (CathyPlugin.getDefault().getPreferenceStore().getInt(
                    CathyPlugin.STARTUP_ACTION) != CathyPlugin.STARTUP_ACTION_LAST) {
                if (!closeAllEditors())
                    return false;
            }
        }
        return readyToShutDown;
    }

    private boolean closeAllEditors() {
        boolean closed = false;
        IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            closed |= window.getActivePage().closeAllEditors(true);
        }
        return closed;
    }

}