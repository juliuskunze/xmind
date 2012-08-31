/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.datalocation.Location;
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

    private String stampFile = null;

    private FileOutputStream stream = null;

    private FileLock lock = null;

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

        Core.getWorkspace().setWorkingDirectory(makeWorkspacePath());

        createStampFile();

        Crypto.setProvider(new BouncyCastleSecurityProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        deleteStampFile();
        checkTempDir();
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static XmindCore getDefault() {
        return plugin;
    }

    private String findInstancePath() {
        Location instanceLocation = Platform.getInstanceLocation();
        if (instanceLocation == null) {
            return getDefaultInstancePath();
        }
        URL url = instanceLocation.getURL();
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
        }
        String file = url.getFile();
        if (file != null && !"".equals(file)) {//$NON-NLS-1$
            return file;
        }
        return url.toExternalForm();
    }

    protected String getDefaultInstancePath() {
        String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
        String os = Platform.getOS();
        if (Platform.OS_WIN32.equals(os)) {
            return new File(new File(new File(homeDir, "Application Data"), //$NON-NLS-1$
                    "XMind"), //$NON-NLS-1$
                    "workspace-cathy").getAbsolutePath(); //$NON-NLS-1$
        } else if (Platform.OS_MACOSX.equals(os)) {
            return new File(new File(new File(homeDir, "Library"), //$NON-NLS-1$
                    "XMind"), //$NON-NLS-1$
                    "workspace-cathy").getAbsolutePath(); //$NON-NLS-1$
        } else {
            return new File(new File(homeDir, ".xmind"), //$NON-NLS-1$
                    "workspace-cathy").getAbsolutePath(); //$NON-NLS-1$
        }
    }

    private String makeWorkspacePath() {
        String workspacePath = findInstancePath();
        File oldWorkspaceDir = new File(workspacePath, ".xmind"); //$NON-NLS-1$
        if (oldWorkspaceDir.isDirectory()) {
            return ensureDir(oldWorkspaceDir);
        }
        IProduct product = Platform.getProduct();
        if (product == null)
            return ensureDir(oldWorkspaceDir);
        if ("org.xmind.cathy.application".equals(product.getApplication())) { //$NON-NLS-1$
            return workspacePath;
        }
        return ensureDir(oldWorkspaceDir);
    }

    private String ensureDir(File dir) {
        dir.mkdirs();
        return dir.getAbsolutePath();
    }

    /**
     * Create a 'Workspace Stamp File' in the temp dir and lock it.
     * 
     * @throws CoreException
     *             when we fail to the create or lock of the 'Workspace Stamp
     *             File'
     */
    private void createStampFile() throws CoreException {
        stampFile = makeStampFilePath();
        if (stampFile == null)
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            PLUGIN_ID,
                            "Failed to prepare the XMind workspace: unable to create a name for the 'Workspace Stamp File'. Please make sure you have permission to modify files at " //$NON-NLS-1$
                                    + Core.getWorkspace().getTempDir()));
        File file = new File(stampFile);
        try {
            stream = new FileOutputStream(file);
        } catch (IOException e) {
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            PLUGIN_ID,
                            "Fail to prepare the XMind workspace: unable to create the 'Workspace Stamp File'. Please make sure you have permission to modify files at " //$NON-NLS-1$
                                    + Core.getWorkspace().getTempDir(), e));
        }
        for (int err = 0; err < 5; err++) {
            try {
                lock = stream.getChannel().tryLock();
            } catch (IOException e) {
            }
            if (lock != null)
                break;
        }
        if (lock == null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
            stream = null;
            file.delete();
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            PLUGIN_ID,
                            "Fail to prepare the XMind workspace: unable to lock the 'Workspace Stamp File'. Please make sure you have permission to modify files at " //$NON-NLS-1$
                                    + Core.getWorkspace().getTempDir()));
        }
    }

    private String makeStampFilePath() {
        String path = null;
        for (int error = 0; error < 5; error++) {
            path = Core.getWorkspace().getTempFile(
                    Long.toHexString(System.currentTimeMillis()) + '_'
                            + Integer.toHexString((int) (Math.random() * 1024))
                            + ".core"); //$NON-NLS-1$
            if (!new File(path).exists())
                break;
            path = null;
        }
        return path;
    }

    private void deleteStampFile() {
        if (lock != null) {
            try {
                lock.release();
            } catch (IOException e) {
            }
            lock = null;
        }
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
            stream = null;
        }
        if (stampFile != null) {
            new File(stampFile).delete();
            stampFile = null;
        }
    }

    /**
     * If no other XMind instance is running, clears the temp dir.
     */
    private void checkTempDir() {
        File dir = new File(Core.getWorkspace().getTempDir());
        if (!dir.exists() || !dir.canRead())
            return;
        String[] instanceMarkers = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".core"); //$NON-NLS-1$
            }
        });
        if (instanceMarkers != null && instanceMarkers.length > 0) {
            for (String name : instanceMarkers) {
                File file = new File(dir, name);
                if (isLocked(file))
                    return; // another XMind Core is using the temp dir
            }
        }
        deleteTempDir();
    }

    private boolean isLocked(File file) {
        try {
            FileOutputStream s = new FileOutputStream(file);
            try {
                FileLock l = s.getChannel().tryLock();
                if (l == null)
                    return true;

                try {
                    l.release();
                } catch (IOException e2) {
                }
                return false;
            } finally {
                s.close();
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private void deleteTempDir() {
        FileUtils.delete(new File(Core.getWorkspace().getTempDir()));
    }

}