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
package org.xmind.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.xmind.core.Core;
import org.xmind.core.util.FileUtils;

public class DirectoryOutputTarget implements IOutputTarget {

    private File dir;

    public DirectoryOutputTarget(String path) {
        this.dir = new File(path);
    }

    public DirectoryOutputTarget(File file) {
        this.dir = file;
    }

    public OutputStream getEntryStream(String entryName) {
        if (!isAvailable())
            return null;

        try {
            File file = new File(dir, entryName);
            FileUtils.ensureFileParent(file);
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Core.getLogger().log(e,
                    "Failed to get entry output stream: " + entryName); //$NON-NLS-1$
        }
        return null;
    }

    public boolean isEntryAvaialble(String entryName) {
        return isAvailable();
    }

    public boolean isAvailable() {
        FileUtils.ensureDirectory(dir);
        return dir.exists() && dir.isDirectory();
    }

//    public boolean closeEntryStream(String entryPath, OutputStream stream) {
//        try {
//            stream.close();
//            return true;
//        } catch (IOException e) {
//        }
//        return false;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IOutputTarget#setEntryTime(java.lang.String, long)
     */
    public void setEntryTime(String entryName, long time) {
        File f = new File(dir, entryName);
        if (f.exists()) {
            f.setLastModified(time);
        }
    }

}