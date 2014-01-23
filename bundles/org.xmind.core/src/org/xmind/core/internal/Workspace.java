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
package org.xmind.core.internal;

import java.io.File;

import org.xmind.core.Core;
import org.xmind.core.IWorkspace;
import org.xmind.core.util.FileUtils;

public class Workspace implements IWorkspace {

    private static final String P_WORKSPACE = "org.xmind.core.workspace"; //$NON-NLS-1$

    private String workingDirectory = null;

    public String getAbsolutePath(String subPath) {
        if (subPath == null)
            return null;
        String wd = getWorkingDirectory();
        File f = new File(wd, subPath);
        return f.getAbsolutePath();
    }

    public String getWorkingDirectory() {
        if (workingDirectory == null) {
            workingDirectory = getDefaultWorkingDirectory();
            System.setProperty(P_WORKSPACE, workingDirectory);
        }
        return workingDirectory;
    }

    private String getDefaultWorkingDirectory() {
        String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
        if (homeDir != null) {
            return homeDir + "/Library/XMind/workspace-cathy/.xmind"; //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    public void setWorkingDirectory(String path) {
        this.workingDirectory = path;
        System.setProperty(P_WORKSPACE, path);
    }

    public String getTempDir() {
        return getAbsolutePath(DIR_TEMP);
    }

    public String getTempDir(String subPath) {
        return FileUtils.ensureDirectory(new File(getTempDir(), subPath))
                .getAbsolutePath();
    }

    public String getTempFile(String fileName) {
        return FileUtils.ensureFileParent(new File(getTempDir(), fileName))
                .getAbsolutePath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkspace#createTempFile(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public File createTempFile(String subPath, String prefix, String suffix) {
        String fileName = prefix + Core.getIdFactory().createId() + suffix;
        File file = new File(getTempFile(subPath), fileName);
        while (file.exists()) {
            fileName = prefix + Core.getIdFactory().createId() + suffix;
            file = new File(getTempFile(subPath), fileName);
        }
        return FileUtils.ensureFileParent(file);
    }

}