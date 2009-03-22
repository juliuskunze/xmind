/*
 * Copyright (c) 2006-2008 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.core.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.xmind.core.Core;
import org.xmind.core.internal.security.BouncyCastleSecurityProvider;
import org.xmind.core.internal.security.Crypto;
import org.xmind.core.util.FileUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class XmindCore extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.core.runtime"; //$NON-NLS-1$

    // The shared instance
    private static XmindCore plugin;

    /**
     * The constructor
     */
    public XmindCore() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        InternalCore.getInstance().setLogger(new DefaultLogger());

        String workspacePath = findWorkspacePath();
        String path = new File(workspacePath, ".xmind").getAbsolutePath(); //$NON-NLS-1$
        Core.getWorkspace().setWorkingDirectory(path);

//        Crypto.setProvider(new BouncyCastleProvider());
        Crypto.setProvider(new BouncyCastleSecurityProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        FileUtils.delete(new File(Core.getWorkspace().getTempDir()));
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static XmindCore getDefault() {
        return plugin;
    }

    /**
     * @return
     */
    //TODO use a workspace independent from the Eclipse's workspace
    private String findWorkspacePath() {
        URL url = Platform.getInstanceLocation().getURL();
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
        }
        String file = url.getFile();
        if (file != null && !"".equals(file)) //$NON-NLS-1$
            return file;

        return url.toExternalForm();
    }

}