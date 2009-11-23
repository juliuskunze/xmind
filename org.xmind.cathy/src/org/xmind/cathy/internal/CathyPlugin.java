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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.xmind.core.internal.XmindCore;

/**
 * The main plugin class to be used in the desktop.
 */
public class CathyPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.xmind.cathy"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String AUTO_SAVE_ENABLED = "autoSaveEnabled"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String AUTO_SAVE_INTERVALS = "autoSaveIntervals"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String RESTORE_LAST_SESSION = "restoreLastSession"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String CHECK_UPDATES_ON_STARTUP = "checkUpdatesOnStartup"; //$NON-NLS-1$

//    /**
//     * 
//     */
//    public static final String RECENT_FILE_COUNT = "recentFileCount"; //$NON-NLS-1$

    // The shared instance.
    private static CathyPlugin plugin;

    /**
     * The constructor.
     */
    public CathyPlugin() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        // Activate XMind Core
        XmindCore.getDefault();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance.
     */
    public static CathyPlugin getDefault() {
        return plugin;
    }

    public static void log(Throwable e, String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        getDefault().getLog().log(
                new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    public static void log(String message) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        getDefault().getLog()
                .log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

}