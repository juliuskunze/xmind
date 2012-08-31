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
package org.xmind.ui.viewers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.misc.StringMatcher;

public class FileUtils {

    private FileUtils() {
    }

    private static final long K = 1 << 10;
    private static final long M = 1 << 20;

    public static String fileLengthToString(long length) {
        if (length < K) {
            return Long.toString(length) + " B"; //$NON-NLS-1$
        } else if (length >= K && length < M) {
            double tmp = ((double) length) / (double) K;
            return String.format("%,.2f KB", tmp); //$NON-NLS-1$
        }
        double tmp = ((double) length) / (double) (M);
        return String.format("%,.2f MB", tmp); //$NON-NLS-1$
    }

    public static String[] list(File path) {
        return list(path, null);
    }

    public static String[] list(File path, final FilenameFilter filter) {
        return list(path, false, filter);
    }

    public static String[] list(File path, boolean extend,
            final FilenameFilter filter) {
        String pathName = path.getAbsolutePath();
        if (path.isFile() && path.exists()) {
            if (extend)
                return list(
                        new File(path.getParentFile(), path.getName() + "*"), false, filter); //$NON-NLS-1$
            if (filter == null
                    || filter.accept(path.getParentFile(), path.getName()))
                return new String[] { pathName };
        } else if (path.isDirectory() && path.exists()) {
            File[] subFiles = path.listFiles(filter);
            if (subFiles != null) {
                List<String> list = new ArrayList<String>();
                for (File subFile : subFiles) {
                    list.add(subFile.getAbsolutePath());
                }
                return list.toArray(new String[0]);
            }
        } else {
            int index = pathName.lastIndexOf(File.separatorChar);
            if (index >= 0) {
                String dirName = pathName.substring(0, index + 1);
                String filePattern = pathName.substring(index + 1, pathName
                        .length());
                if (extend && !filePattern.endsWith("*")) //$NON-NLS-1$
                    filePattern += "*"; //$NON-NLS-1$
                File dir = new File(dirName);
                if (dir.isDirectory()) {
                    final StringMatcher matcher = new StringMatcher(
                            filePattern, true, false);
                    return list(dir, false, new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return matcher.match(name)
                                    && (filter == null || filter.accept(dir,
                                            name));
                        }
                    });
                }
            }
        }
        return new String[0];
    }
}