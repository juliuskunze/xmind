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
package org.xmind.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmind.core.CoreException;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;

/**
 * @author briansun
 * 
 */
public class FileUtils {

    /**
     * @param f
     * @return
     */
    public static File ensureFileParent(File f) {
        ensureDirectory(f.getParentFile());
        return f;
    }

    /**
     * @param dir
     * @return
     */
    public static File ensureDirectory(File dir) {
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

    public static void copy(String src, String dest) throws IOException {
        FileInputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dest);
        transfer(is, os, true);
    }

    public static void copy(File src, File dest) throws IOException {
        FileInputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dest);
        transfer(is, os, true);
    }

    public static void transfer(IStorage oldStorage, IStorage newStorage)
            throws IOException, CoreException {
        IInputSource inSource = oldStorage.getInputSource();
        IOutputTarget outTarget = newStorage.getOutputTarget();
        Iterator<String> entries = inSource.getEntries();
        while (entries.hasNext()) {
            String stream = entries.next();
            InputStream is = inSource.getEntryStream(stream);
            if (is != null) {
                OutputStream os = outTarget.getEntryStream(stream);
                if (os != null) {
                    transfer(is, os);
                }
            }
        }
    }

    public static void transfer(InputStream is, OutputStream os)
            throws IOException {
        transfer(is, os, true, null);
    }

    public static void transfer(InputStream is, OutputStream os,
            boolean closeOnFinish) throws IOException {
        transfer(is, os, closeOnFinish, null);
    }

    public static void transfer(InputStream is, OutputStream os,
            boolean closeOnFinish, String taskName) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int num = is.read(buffer);
                if (num <= 0)
                    break;
                os.write(buffer, 0, num);
            }
        } finally {
            if (closeOnFinish) {
                try {
                    is.close();
                } finally {
                    os.close();
                }
            }
        }
    }

    /**
     * Deletes the given file and, if it is a directory, delete all its
     * sub-directories and sub-files.
     * 
     * @param f
     *            The file or directory to delete
     * @return Whether the given file or directory is successfully deleted.
     */
    public static boolean delete(File f) {
        if (f.isFile())
            return f.delete();
        else if (f.isDirectory()) {
            boolean b = clearDir(f);
            b &= f.delete();
            return b;
        } else
            return false;
    }

    /**
     * Deletes all sub-files and sub-directories in the given directory.
     * 
     * @param dir
     *            The directory to clear
     * @return Whether the given directory is successfully cleared.
     */
    public static boolean clearDir(File dir) {
        if (!dir.isDirectory())
            return false;
        File[] files = dir.listFiles();
        if (files == null || files.length == 0)
            return true;
        boolean cleared = true;
        for (File sub : files) {
            cleared &= delete(sub);
        }
        return cleared;
    }

    /**
     * Determines corresponding media type of the given path.
     * 
     * @param path
     * @return
     */
    public static String getMediaType(String path) {
        if (path != null) {
            String ext = getExtension(path);
            if (".jpg".equals(ext) || ".jpeg".equals(ext)) //$NON-NLS-1$ //$NON-NLS-2$
                return "image/jpeg"; //$NON-NLS-1$
            if (".png".equals(ext)) //$NON-NLS-1$
                return "image/png"; //$NON-NLS-1$
            if (".bmp".equals(ext)) //$NON-NLS-1$
                return "image/bmp"; //$NON-NLS-1$
            if (".gif".equals(ext)) //$NON-NLS-1$
                return "image/gif"; //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    private static Pattern FileNamePattern = null;

    public static String getFileName(String fullPath) {
        if (FileNamePattern == null)
            FileNamePattern = Pattern.compile("([^/\\\\]*)[/|\\\\]?$"); //$NON-NLS-1$
        Matcher m = FileNamePattern.matcher(fullPath);
        if (m.find())
            return m.group(1);
        return fullPath;
    }

    /**
     * Returns the extension part of a file path, e.g., <code>.jpg</code>,
     * <code>.html</code>. If the file does not have an extension, an empty
     * string is returned.
     * 
     * @param fullPath
     * @return
     */
    public static String getExtension(String fullPath) {
        String fileName = getFileName(fullPath);
        int i = fileName.lastIndexOf('.');
        if (i >= 0)
            return fileName.substring(i);
        return ""; //$NON-NLS-1$
    }

    public static String getNoExtensionFileName(String fullPath) {
        String fileName = getFileName(fullPath);
        int i = fileName.lastIndexOf('.');
        if (i >= 0)
            return fileName.substring(0, i);
        return fileName;
    }

}