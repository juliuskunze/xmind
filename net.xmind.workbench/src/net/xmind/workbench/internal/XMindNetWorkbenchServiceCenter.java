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
package net.xmind.workbench.internal;

import net.xmind.workbench.internal.notification.SiteEventNotificationService;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

public class XMindNetWorkbenchServiceCenter implements IStartup,
        IWorkbenchListener {

    private IWorkbench workbench;

    private SiteEventNotificationService eventService = null;

    public void earlyStartup() {
        this.workbench = PlatformUI.getWorkbench();
        if (this.workbench != null) {
            startServices();
            this.workbench.addWorkbenchListener(this);
        }
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        return true;
    }

    public void postShutdown(IWorkbench workbench) {
        stopServices();
    }

    private void startServices() {
        if (eventService == null) {
            eventService = new SiteEventNotificationService(workbench);
        }
        eventService.start();
        new NewsletterSubscriptionReminder(workbench).start();
    }

    private void stopServices() {
        if (eventService != null) {
            eventService.shutdown();
            eventService = null;
        }
    }

}
