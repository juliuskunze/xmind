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
package org.xmind.ui.internal.protocols;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Karelun Huang
 */
public class FilePathParser {

    private static final String SEP = System.getProperty("file.separator"); //$NON-NLS-1$

    public static String toPath(String uri) {
        if (uri == null)
            return null;
        String path;
        try {
            path = new URI(uri).getPath();
        } catch (Exception e) {
            if (uri.startsWith("file:")) //$NON-NLS-1$
                path = uri.substring(5);
            else
                path = uri;
        }
        if (path.startsWith("//")) //$NON-NLS-1$
            path = path.substring(2);
        if (path.startsWith("/") //$NON-NLS-1$
                && "win32".equals(System.getProperty("osgi.os"))) { //$NON-NLS-1$ //$NON-NLS-2$
            path = path.substring(1);
        }
        try {
            path = URLDecoder.decode(path, "utf-8"); //$NON-NLS-1$
        } catch (Exception e) {
        }
        return path;
    }

    public static String toURI(String path, boolean relative) {
        if (path == null)
            return null;
        if (File.separatorChar != '/')
            path = path.replace(File.separatorChar, '/');
        try {
            return new URI("file", null, relative ? path : "//" + path, null).toString(); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
        }
        return relative ? "file:" + path : "file://" + path; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean isPathRelative(String path) {
        return !new File(path).isAbsolute();
    }

    private static List<File> getRoutine(File file, List<File> routine) {
        File parent = file.getParentFile();
        if (parent != null) {
            routine.add(0, parent);
            return getRoutine(parent, routine);
        }
        return routine;
    }

    private static int findStart(List<File> r1, List<File> r2) {
        int start;
        for (start = 0; start < r1.size() && start < r2.size(); start++) {
            if (!r1.get(start).equals(r2.get(start)))
                break;
        }
        return start;
    }

    public static String toRelativePath(String base, String absolutePath) {
        File file = new File(absolutePath);
        File baseFile = new File(base);
        List<File> routine = getRoutine(file, new ArrayList<File>());
        List<File> baseRoutine = new ArrayList<File>();
        baseRoutine.add(baseFile);
        baseRoutine = getRoutine(baseFile, baseRoutine);
        int start = findStart(routine, baseRoutine);
        StringBuilder sb = new StringBuilder(20);
        String sep = SEP;
        for (int i = start; i < baseRoutine.size(); i++) {
            sb.append(".."); //$NON-NLS-1$
            sb.append(sep);
        }
        for (int i = routine.size() - 1; i >= start; i--) {
            sb.append(routine.get(i).getName());
            sb.append(sep);
        }
        sb.append(file.getName());
        return sb.toString();
    }

    public static String toAbsolutePath(String base, String relativePath) {
        try {
            return new File(base, relativePath).getCanonicalPath();
        } catch (IOException e) {
            return new File(base, relativePath).getAbsolutePath();
        }
    }

//    public static void main(String[] args) {
//        String relativePath = toRelativePath("/Users/frankshaka/Music", //$NON-NLS-1$
//                "/Users/frankshaka/Desktop/a.xmind"); //$NON-NLS-1$
//        System.out.println(relativePath);
//        System.out.println(isPathRelative(relativePath));
//        System.out.println(toAbsolutePath("/Users/frankshaka/Music", //$NON-NLS-1$
//                relativePath));
//    }

}
