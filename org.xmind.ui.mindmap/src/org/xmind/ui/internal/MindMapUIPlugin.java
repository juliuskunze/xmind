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
package org.xmind.ui.internal;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.xmind.core.Core;
import org.xmind.core.internal.XmindCore;
import org.xmind.ui.internal.editor.BackgroundWorkbookSaver;

public class MindMapUIPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.ui.mindmap"; //$NON-NLS-1$

    // The shared instance.
    private static MindMapUIPlugin plugin;

    /**
     * The constructor
     */
    public MindMapUIPlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        // activate core runtime
        XmindCore.getDefault();
        //Shell shell = plugin.getWorkbench().getDisplay().getActiveShell();
        Core.getWorkbookBuilder().setDefaultEncryptionHandler(
                new PasswordProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
        BackgroundWorkbookSaver.getInstance().stopAll();
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static MindMapUIPlugin getDefault() {
        return plugin;
    }

    public IDialogSettings getDialogSettings(String sectionName) {
        IDialogSettings ds = getDialogSettings();
        IDialogSettings section = ds.getSection(sectionName);
        if (section == null) {
            section = ds.addNewSection(sectionName);
        }
        return section;
    }

}