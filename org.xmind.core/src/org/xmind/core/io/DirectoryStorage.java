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
import java.io.FileFilter;

import org.xmind.core.CoreException;
import org.xmind.core.util.FileUtils;

/**
 * @author frankshaka
 * 
 */
public class DirectoryStorage implements IStorage {

    private File dir;

    private FileFilter filter;

    /**
     * 
     */
    public DirectoryStorage(File dir) {
        this(dir, null);
    }

    /**
     * 
     */
    public DirectoryStorage(File dir, FileFilter filter) {
        this.dir = dir;
        this.filter = filter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getFullPath()
     */
    public String getFullPath() {
        return dir.getAbsolutePath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getInputSource()
     */
    public IInputSource getInputSource() throws CoreException {
        return new DirectoryInputSource(dir, filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getName()
     */
    public String getName() {
        return dir.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getOutputTarget()
     */
    public IOutputTarget getOutputTarget() throws CoreException {
        return new DirectoryOutputTarget(dir);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IStorage#clear()
     */
    public void clear() {
//        FileUtils.clearDir(dir);
        FileUtils.delete(dir);
    }
}
