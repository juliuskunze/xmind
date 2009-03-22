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
package org.xmind.ui.util;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @author frankshaka
 */
public class ResourceFinder {

    //public static String DEFAULT_NL = "en_US"; //$NON-NLS-1$

    //public static String DEFAULT_PROVIDER = "brainy"; //$NON-NLS-1$

    //private static final String NL1_PLUGIN_ID = MindMapUI.PLUGIN_ID + ".nl1"; //$NON-NLS-1$

    private static final String SEP = "/"; //$NON-NLS-1$

    private static final String LINK = "_"; //$NON-NLS-1$

    private static final char Link = '_';

    protected ResourceFinder() {
    }

    /**
     * Find resource by the given path and the platform language.
     * 
     * @param mainPath
     * @param prefix
     * @param suffix
     * @return
     */
    public static URL findResource(String bundleName, String mainPath,
            String prefix, String suffix) {
        return findResource(bundleName, mainPath, prefix, suffix, Platform
                .getNL());
    }

    /**
     * Find the resource associated to the given path and the given language.
     * This method will look into both this plugin and the NL plugins.
     * <p>
     * If the desired resource is defined as:
     * </p>
     * 
     * <pre>
     * String mainPath = &quot;/markers/&quot;;   // same as &quot;markers&quot;, &quot;/markers&quot;, &quot;markers/&quot;
     * String prefix = &quot;markerSheet&quot;;
     * String suffix = &quot;.properties&quot;;
     * String nl = &quot;zh_CN&quot;;
     * URL resourceUrl = findResource(mainPath, prefix, suffix, nl);
     * ....
     * </pre>
     * <p>
     * Then the following possible resource paths will be checked from NL
     * plugins to this plugin:
     * <ul>
     * <li>/markers/zh/CN/markerSheet.properties</li>
     * <li>/markers/zh/markerSheet.properties</li>
     * <li>/markers/markerSheet_zh_CN.properties</li>
     * <li>/markers/markerSheet_zh.properties</li>
     * <li>/markers/markerSheet.properties</li>
     * </ul>
     * </p>
     * <p>
     * The returned value will be either <code>null</code> if no resource is
     * found available, or a bundle resource URL associated to the required
     * resource.
     * </p>
     * 
     * @param mainPath
     *            The main path of the resource
     * @param prefix
     *            The prefix of the resource name
     * @param suffix
     *            The suffix of the resource name
     * @param nl
     *            The language name, e.g. <code>en_US</code>, <code>zh_CN</code>
     *            , <code>fr</code>, etc.
     * @return A URL associated to the required resource, or <code>null</code>
     *         if no resource was found
     */
    public static URL findResource(String bundleName, String mainPath,
            String prefix, String suffix, String nl) {
        return findResource(Platform.getBundle(bundleName), mainPath, prefix,
                suffix, nl);
    }

    public static URL findResource(Bundle bundle, String mainPath,
            String prefix, String suffix) {
        return findResource(bundle, mainPath, prefix, suffix, Platform.getNL());
    }

    public static URL findResource(Bundle bundle, String mainPath,
            String prefix, String suffix, String nl) {
        if (bundle == null)
            return null;

        String ma = getMajorLang(nl);
        String mi = getMinorLang(nl);

        if (!mainPath.endsWith(SEP)) {
            mainPath += SEP;
        }

        URL url;
//        url = findResource(NL1_PLUGIN_ID, mainPath, prefix, suffix, nl, ma,
//                mi);
//        if (url != null)
//            return url;
        url = findResource(bundle, mainPath, prefix, suffix, nl, ma, mi);
        if (url != null)
            return url;

        return null;
    }

    private static URL findResource(Bundle bundle, String mainPath,
            String prefix, String suffix, String nl, String ma, String mi) {
        String path;
        URL url;
        if (mi != null) {
            // "/markers/zh/CN/markerSheet.properties"
            path = mainPath + ma + SEP + mi + SEP + prefix + suffix;
            url = FileLocator.find(bundle, new Path(path), null);
            if (url != null)
                return url;
        }

        // "/markers/zh/markerSheet.properties"
        path = mainPath + ma + SEP + prefix + suffix;
        url = FileLocator.find(bundle, new Path(path), null);
        if (url != null)
            return url;

        // "/markers/markerSheet_zh_CN.properties"
        path = mainPath + prefix + LINK + nl + suffix;
        url = FileLocator.find(bundle, new Path(path), null);
        if (url != null)
            return url;

        if (!ma.equals(nl)) {
            // "/markers/markerSheet_zh.properties"
            path = mainPath + prefix + LINK + ma + suffix;
            url = FileLocator.find(bundle, new Path(path), null);
            if (url != null)
                return url;

        }
        // "/markers/markerSheet.properties"
        path = mainPath + prefix + suffix;
        url = FileLocator.find(bundle, new Path(path), null);
        if (url != null)
            return url;

        return null;
    }

    public static String getMajorLang(String nl) {
        int i = nl.indexOf(Link);
        if (i > 0) {
            return nl.substring(0, i);
        }
        return nl;
    }

    public static String getMinorLang(String nl) {
        int i = nl.indexOf(Link);
        if (i > 0) {
            return nl.substring(i + 1);
        }
        return null;
    }

//    /**
//     * Set on plugin activating
//     */
//    private static Bundle DEFAULT_BUNDLE = null;

//    public static void setDefaultBundle(Bundle bundle) {
//        FileUtils.DEFAULT_BUNDLE = bundle;
//    }

//    public static String trim(String path) {
//        return path.replaceAll("\n", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("\r", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("\\\\", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("/", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("\\|", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll(":", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("\"", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll(">", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("<", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("\\*", " ") //$NON-NLS-1$ //$NON-NLS-2$
//                .replaceAll("\\?", " "); //$NON-NLS-1$ //$NON-NLS-2$
//    }

    // private static String template = IBrainyWorkspace.templatesDir;
    // //$NON-NLS-1$
    // private static String markers = IBrainyWorkspace.markersDir;
    // //$NON-NLS-1$
    // private static String user = "user"; //$NON-NLS-1$

//    public static void copyFile(String src, String dest) throws IOException {
//        copyFile(src, dest, false);
//    }
//
//    public static void copyFile(File src, File dest) throws IOException {
//        copyFile(src, dest, false);
//    }
//
//    public static void copyFile(String src, String dest,
//            boolean preserveAttributes) throws IOException {
//        File srcFile = new File(src);
//        File desFile = new File(dest);
//        copyFile(srcFile, desFile, preserveAttributes);
//    }
//
//    public static void copyFile(File src, File dest, boolean preserveAttributes)
//            throws IOException {
//        if (src.equals(dest))
//            return;
//        if (src.isDirectory()) {
//            dest.mkdirs();
//            for (String s : src.list()) {
////				File srcSub = new de.schlichtherle.io.File(src, s,
////						ArchiveDetector.NULL);
////				File destSub = new de.schlichtherle.io.File(dest, s,
////						ArchiveDetector.NULL);
//                File srcSub = new File(src, s);
//                File destSub = new File(dest, s);
//                copyFile(srcSub, destSub, preserveAttributes);
//            }
//            if (preserveAttributes) {
//                dest.setLastModified(src.lastModified());
//            }
//        } else if (src.isFile()) {
//            writeFile(new FileInputStream(src), dest);
//            if (preserveAttributes) {
//                dest.setLastModified(src.lastModified());
////                de.schlichtherle.io.File.cp_p(src, dest);
//            } else {
////                de.schlichtherle.io.File.cp(src, dest);
//            }
//        } else {
//            throw new IOException("Unrecognizable file type: " + src); //$NON-NLS-1$
//        }
//    }
//
//    public static void writeFile(InputStream srcIs, File dest)
//            throws IOException {
//        // if ( !dest.exists() ) {
//        // dest.getParentFile().mkdirs();
//        // }
//        OutputStream os = new FileOutputStream(ensureFileParent(dest));
//
//        // Transfer bytes from in to out
//        byte[] buf = new byte[1024];
//        int len;
//        while ((len = srcIs.read(buf)) > 0) {
//            os.write(buf, 0, len);
//        }
//        srcIs.close();
//        os.close();
//    }
//
//    public static boolean delete(File f) {
//        if (f.isFile())
//            return f.delete();
//        else if (f.isDirectory()) {
//            return clearDir(f) && f.delete();
//        } else
//            return false;
//    }
//
//    public static boolean clearDir(File f) {
//        if (!f.isDirectory())
//            return false;
//        File[] files = f.listFiles();
//        if (files == null || files.length == 0)
//            return true;
//        boolean cleared = true;
//        for (File sub : files) {
//            cleared &= delete(sub);
//        }
//        return cleared;
//    }
//
//    public static void deleteOnExit(File f) {
//        if (!f.exists())
//            return;
//        if (f.isFile()) {
//            f.deleteOnExit();
//        } else if (f.isDirectory()) {
//            File[] files = f.listFiles();
//            for (int i = 0; i < files.length; i++) {
//                deleteOnExit(files[i]);
//            }
//            f.deleteOnExit();
//        }
//    }
//
//    public static boolean copyAllExcept(File src, File dst,
//            String... exceptionalMembers) {
//        List<String> exceptions = Arrays.asList(exceptionalMembers);
//        if (contains(src, dst))
//            return false;
//        if (src.isDirectory()) {
//            if (!dst.mkdir() && !dst.isDirectory())
//                return false;
//            final String[] members = src.list();
//            for (int i = 0, l = members.length; i < l; i++) {
//                final String member = members[i];
//                if (exceptions.indexOf(member) < 0) {
//                    try {
//                        copyFile(new File(src, member), (new File(dst, member)));
//                    } catch (IOException e) {
//                        return false;
//                    }
//                }
//            }
//            long srcLastModified = src.lastModified();
//            // Use current time for copies of ghost directories!
//            if (srcLastModified > 0) // || !src.isEntry())
//                dst.setLastModified(srcLastModified);
//            return true;
//        } else if (src.isFile() && (!dst.exists() || dst.isFile())) {
//            try {
//                copyFile(src, dst);
//                return true;
//            } catch (IOException e) {
//            }
////            return src.archiveCopyTo(dst);
//        }
//        return false;
//    }
//
//    public static boolean contains(File parent, File child) {
//        File p = child.getParentFile();
//        if (p == null)
//            return false;
//        if (parent.equals(p))
//            return true;
//        return contains(parent, p);
//    }
//
//    // public static String getUserTemplateDir() {
//    // File file = new File( new File(
//    // BrainyPlatform.getWorkspace().getWorkingDir(), template ), user );
//    // if ( !file.exists() )
//    // file.mkdirs();
//    // return file.getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
//    // }
//
//    // public static String getUserMarkerDir() {
//    // File file = new File( new File(
//    // BrainyPlatform.getWorkspace().getWorkingDir(), markers ), user );
//    // //$NON-NLS-1$ //$NON-NLS-2$
//    // if ( !file.exists() )
//    // file.mkdirs();
//    // return file.getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
//    // }
//
//    public static String getFileWithoutSuffix(String path) {
//        int start = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
//        start = start < 0 ? 0 : start + 1;
//        int end = path.lastIndexOf('.');
//        end = end < 0 ? path.length() : end;
//        return path.substring(start, end);
//    }
//
//    public static File createNoneConflictingFile(String parentDir,
//            String subPath) {
//        return createNoneConflictingFile(new File(parentDir), subPath);
//    }
//
//    public static File createNoneConflictingFile(File parentDir, String subPath) {
//        File f = new File(parentDir, subPath);
//        while (f.exists()) {
//            subPath = increFileNumber(subPath);
//            f = new File(parentDir, subPath);
//        }
//        return f;
//    }
//
//    /**
//     * @param fileName
//     * @return
//     */
//    public static String increFileNumber(String fileName) {
//        int lastDot = fileName.lastIndexOf("."); //$NON-NLS-1$
//        if (lastDot < 0)
//            lastDot = fileName.length();
//        String prefix = fileName.substring(0, lastDot);
//        String suffix = fileName.substring(lastDot);
//        int lastLeftBracket = prefix.lastIndexOf("("); //$NON-NLS-1$
//        if (lastLeftBracket > 0 && prefix.endsWith(")")) { //$NON-NLS-1$
//            String t = prefix.substring(lastLeftBracket + 1, prefix
//                    .lastIndexOf(")")); //$NON-NLS-1$
//            try {
//                int i = Integer.parseInt(t);
//                prefix = prefix.substring(0, lastLeftBracket + 1) + (++i) + ")"; //$NON-NLS-1$
//                return prefix + suffix;
//            } catch (Exception e) {
//            }
//        }
//        return prefix + " (2)" + suffix; //$NON-NLS-1$
//    }
//
//    public static String filenameToUnixShell(String fileName) {
//        StringBuffer sb = new StringBuffer();
//        for (char c : fileName.toCharArray()) {
//            if (c <= '\u007f')
//                sb.append(c);
//            else
//                sb.append(charToUnixShell(c));
//        }
//        //System.out.println(sb.toString());
//        return sb.toString();
//    }
//
//    private static String charToUnixShell(char c) {
//        StringBuffer bin = new StringBuffer(Integer.toBinaryString(c));
//        while (bin.length() < 16) {
//            bin.insert(0, '0');
//        }
//        // char[] bs = new char[3];
//        // bs[0] = (char)(short)Short.valueOf("1110"+bin.substring(0, 4), 2);
//        // bs[1] = (char)(short)Short.valueOf("10"+bin.substring(4, 10), 2);
//        // bs[2] = (char)(short)Short.valueOf("10"+bin.substring(10, 16), 2);
//
//        bin.insert(0, "01110"); //$NON-NLS-1$
//        bin.insert(9, "010"); //$NON-NLS-1$
//        bin.insert(18, "010"); //$NON-NLS-1$
//        StringBuffer oct = new StringBuffer(Integer.toOctalString(Integer
//                .valueOf(bin.toString(), 2)));
//        oct.insert(0, '\\');
//        oct.insert(4, '\\');
//        oct.insert(8, '\\');
//        return oct.toString();
//
//        // int ret = Integer.valueOf( bin.toString(), 2 );
//        // byte[] bs = BigInteger.valueOf(ret).toByteArray();
//        // for ( char b : bs ) {
//        // System.out.print(Integer.toOctalString(b));
//        // System.out.println();
//        // }
//        // return new String(bs);
//    }
//
////    /**
////     * @param f
////     */
////    public static void umount(de.schlichtherle.io.File f) {
////        try {
////            de.schlichtherle.io.File.umount(f, true, true, true, true);
////        } catch (ArchiveWarningException e) {
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//
//    public static URL fileToURL(String filePath) {
//        return buildURL(filePath, true);
//    }
//
//    public static String urlToFile(URL url) {
//        String spec = url.toExternalForm();
//        if (spec.startsWith("file:")) { //$NON-NLS-1$
//            spec = spec.substring(5);
//            while (spec.startsWith("/")) //$NON-NLS-1$
//                spec = spec.substring(1);
//            try {
//                spec = URLDecoder.decode(spec, "ascii"); //$NON-NLS-1$ 
//            } catch (UnsupportedEncodingException e) {
//            }
//            // if ( BrainyUI.isCarbon() )
//            // return "/" + spec;
//            // if (BrainyUI.isWin32())
//            if (Platform.WS_WIN32.equals(Platform.getWS()))
//                return spec.replaceAll("/", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
//            else
//                return "/" + spec; //$NON-NLS-1$
//        }
//        return spec;
//    }
//
//    /**
//     * @param spec
//     * @param trailingSlash
//     * @return
//     */
//    private static URL buildURL(String spec, boolean trailingSlash) {
//        if (spec == null)
//            return null;
//        boolean isFile = spec.startsWith("file:"); //$NON-NLS-1$
//        try {
//            if (isFile)
//                return adjustTrailingSlash(new File(spec.substring(5)).toURI()
//                        .toURL(), trailingSlash);
//            else
//                return new URL(spec);
//        } catch (MalformedURLException e) {
//            // if we failed and it is a file spec, there is nothing more we can
//            // do
//            // otherwise, try to make the spec into a file URL.
//            if (isFile)
//                return null;
//            try {
//                return adjustTrailingSlash(new File(spec).toURI().toURL(),
//                        trailingSlash);
//            } catch (MalformedURLException e1) {
//                return null;
//            }
//        }
//    }
//
//    private static URL adjustTrailingSlash(URL url, boolean trailingSlash)
//            throws MalformedURLException {
//        String file = url.getFile();
//        if (trailingSlash == (file.endsWith("/"))) //$NON-NLS-1$
//            return url;
//        file = trailingSlash ? file + "/" : file.substring(0, file.length() - 1); //$NON-NLS-1$
//        return new URL(url.getProtocol(), url.getHost(), file);
//    }
//
//    // public static Image getFileIconByExtension( String extension ) {
//    // String key = "programIcon_" + extension;
//    // Image image = ImageUtils.getImage( key );
//    // if ( image != null )
//    // return image;
//    //
//    // Program p = Program.findProgram( extension );
//    // if ( p != null ) {
//    // ImageData imageData = p.getImageData();
//    // if ( imageData != null ) {
//    // return ImageUtils.getImage( key, imageData );
//    // }
//    // }
//    // return null;
//    // }
//
////    public static Image getExtensionIcon(String... exts) {
////        for (String extension : exts) {
////            if (extension != null && extension.length() > 0) {
////                if (extension.charAt(0) != '.')
////                    extension = "." + extension; //$NON-NLS-1$
////                String key = "ext_icon" + extension; //$NON-NLS-1$
////                Image image = ImageUtils.getImage(key);
////                if (image != null)
////                    return image;
////                Program p = Program.findProgram(extension);
////                if (p != null) {
////                    ImageData imageData = p.getImageData();
////                    if (imageData != null)
////                        return ImageUtils.getImage(key, imageData);
////                }
////            }
////        }
////        return null;
////    }
////
////    public static ImageDescriptor getExtensionIconDescriptor(String... exts) {
////        for (String ext : exts) {
////            if (ext != null && ext.length() > 0) {
////                if (ext.charAt(0) != '.')
////                    ext = "." + ext; //$NON-NLS-1$
////                String key = "ext_icon" + ext; //$NON-NLS-1$
////                ImageDescriptor desc = ImageUtils.getDescriptor(key);
////                if (desc != null)
////                    return desc;
////                Program p = Program.findProgram(ext);
////                if (p != null) {
////                    ImageData imgData = p.getImageData();
////                    if (imgData != null) {
////                        desc = IdentifiedImageDescriptor.createFromImageData(
////                                key, imgData);
////                        ImageUtils.putImageDescriptor(key, desc);
////                        return desc;
////                    }
////                }
////            }
////        }
////        return null;
////    }
////
////    public static String getExtension(String path) {
////        int lastIndex = path.lastIndexOf("."); //$NON-NLS-1$
////        if (lastIndex == -1) {
////            return null;
////        }
////        return path.substring(lastIndex + 1, path.length());
////    }
////
////    public static Image getFileIcon(String path) {
////        if (path == null)
////            return null;
////        String ext = getExtension(path);
////        if (ext != null) {
////            Image image = getExtensionIcon(ext);
////            if (image != null)
////                return image;
////        }
////        return null;
////    }
//
//    private static final long K = 1 << 10;
//    private static final long M = 1 << 20;
//
//    public static String fileLengthToString(long length) {
//        if (length < K) {
//            return Long.toString(length) + " B"; //$NON-NLS-1$
//        } else if (length >= K && length < M) {
//            double tmp = ((double) length) / (double) K;
//            return String.format("%,.2f KB", tmp); //$NON-NLS-1$
//        }
//        double tmp = ((double) length) / (double) (M);
//        return String.format("%,.2f MB", tmp); //$NON-NLS-1$
//    }
//
//    public static String[] list(File path) {
//        return list(path, null);
//    }
//
//    public static String[] list(File path, final FilenameFilter filter) {
//        return list(path, false, filter);
//    }
//
//    public static String[] list(File path, boolean extend,
//            final FilenameFilter filter) {
//        String pathName = path.getAbsolutePath();
//        if (path.isFile() && path.exists()) {
//            if (extend)
//                return list(
//                        new File(path.getParentFile(), path.getName() + "*"), false, filter); //$NON-NLS-1$
//            if (filter == null
//                    || filter.accept(path.getParentFile(), path.getName()))
//                return new String[] { pathName };
//        } else if (path.isDirectory() && path.exists()) {
//            File[] subFiles = path.listFiles(filter);
//            if (subFiles != null) {
//                List<String> list = new ArrayList<String>();
//                for (File subFile : subFiles) {
//                    list.add(subFile.getAbsolutePath());
//                }
//                return list.toArray(new String[0]);
//            }
//        } else {
//            int index = pathName.lastIndexOf(File.separatorChar);
//            if (index >= 0) {
//                String dirName = pathName.substring(0, index + 1);
//                String filePattern = pathName.substring(index + 1, pathName
//                        .length());
//                if (extend && !filePattern.endsWith("*")) //$NON-NLS-1$
//                    filePattern += "*"; //$NON-NLS-1$
//                File dir = new File(dirName);
//                if (dir.isDirectory()) {
//                    final StringMatcher matcher = new StringMatcher(
//                            filePattern, true, false);
//                    return list(dir, false, new FilenameFilter() {
//                        public boolean accept(File dir, String name) {
//                            return matcher.match(name)
//                                    && (filter == null || filter.accept(dir,
//                                            name));
//                        }
//                    });
//                }
//            }
//        }
//        return new String[0];
//    }
//
//    public static File ensureFileParent(File f) {
//        File p = f.getParentFile();
//        if (!p.exists())
//            p.mkdirs();
//        return f;
//    }
//
//    public static File ensureDirectory(File dir) {
//        if (!dir.exists())
//            dir.mkdirs();
//        return dir;
//    }
//
//    public static String findResourceDir(String mainDir) {
//        return findResourceDir(mainDir, DEFAULT_PROVIDER, false, false);
//    }
//
//    public static String findResourceDir(String mainDir, boolean withNL) {
//        return findResourceDir(mainDir, DEFAULT_PROVIDER, withNL, false);
//    }
//
//    public static String findResourceDir(String mainDir, String provider) {
//        return findResourceDir(mainDir, provider, false, false);
//        // URL r = FileLocator.toFileURL( Platform.getInstallLocation().getURL()
//        // );
//        // File f = new File( new File( FileUtils.urlToFile( r ), mainDir ),
//        // provider );
//        // if ( !f.exists() ) {
//        // r = FileLocator.toFileURL( FileLocator.find(
//        // Activator.getDefault().getBundle(), new Path( mainDir
//        // + "/" + provider ), null ) ); //$NON-NLS-1$
//        // f = new File( FileUtils.urlToFile( r ) );
//        // }
//        // String s = f.getAbsolutePath();
//        // return s;
//    }
//
//    public static String findResourceDir(String mainDir, boolean withNL,
//            boolean checkNLSubDir) {
//        return findResourceDir(mainDir, DEFAULT_PROVIDER, withNL, checkNLSubDir);
//    }
//
//    public static String findResourceDir(String mainDir, String provider,
//            boolean withNL, boolean checkNLSubDir) {
//        return findResourceDir(mainDir, provider, withNL, checkNLSubDir, true);
//    }
//
//    public static String findResourceDir(String mainDir, String provider,
//            boolean withNL, boolean checkNLSubDir, boolean checkInstallLoc) {
//        String nl = Platform.getNL();
//        if (withNL) {
//            if (nl == null || "".equals(nl) || DEFAULT_NL.equals(nl)) //$NON-NLS-1$ 
//                withNL = false;
//        }
//
//        File f = null;
//
//        if (checkInstallLoc) {
//            URL url = Platform.getInstallLocation().getURL();
//            if (url != null) {
//                try {
//                    url = FileLocator.toFileURL(url);
//                } catch (IOException e) {
//                    url = null;
//                }
//                if (url != null) {
//                    File dir = new File(urlToFile(url), mainDir);
//                    if (provider != null)
//                        f = new File(dir, provider);
//                    if (checkNLSubDir)
//                        f = checkNLSubPath(f, withNL, nl);
//                }
//            }
//        }
//
//        if (DEFAULT_BUNDLE != null && (f == null || !f.exists())) {
//            String path = provider == null ? mainDir : mainDir + "/" + provider; //$NON-NLS-1$
//            URL url = FileLocator.find(DEFAULT_BUNDLE, new Path(path), null);
//            if (url != null) {
//                try {
//                    url = FileLocator.toFileURL(url);
//                } catch (IOException e) {
//                    url = null;
//                }
//                if (url != null) {
//                    f = new File(urlToFile(url));
//                    if (withNL) {
//                        String nlPath = f.getAbsolutePath().replaceAll(
//                                "cn.brainy.standard", //$NON-NLS-1$
//                                "cn.brainy.standard.nl1"); //$NON-NLS-2$
//                        File nlFile = new File(nlPath);
//                        if (nlFile.exists()) {
//                            if (checkNLSubDir) {
//                                File subNLFile = checkNLSubPath(nlFile, withNL,
//                                        nl);
//                                if (subNLFile.exists()
//                                        && !subNLFile.equals(nlFile))
//                                    f = subNLFile;
//                            } else {
//                                f = nlFile;
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//
//        if (f != null && !f.exists()) {
//            f = checkNLSubPath(f, withNL, nl);
//        }
//
//        return f == null ? null : f.getAbsolutePath();
//    }
//
//    public static File checkNLSubPath(File dir, boolean withNL, String nl) {
//        if (dir.exists() && dir.isDirectory() && withNL) {
//            String[] nlParts = getNLParts(nl);
//            File sub = dir;
//            for (int i = 0; i < nlParts.length; i++) {
//                String s = nlParts[i];
//                sub = new File(sub, s);
//                if (!sub.exists())
//                    break;
//            }
//            if (sub != dir && sub.exists() && sub.isDirectory())
//                dir = sub;
//        }
//        return dir;
//    }
//
//    public static File getNLSubDir(File dir, String nl) {
//        if (dir != null) {
//            if (nl != null && !"".equals(nl) && !DEFAULT_NL.equals(nl)) { //$NON-NLS-1$
//                String[] nlParts = getNLParts(nl);
//                for (String sub : nlParts) {
//                    dir = new File(dir, sub);
//                }
//            }
//        }
//        return dir;
//    }
//
//    public static String getLocaleFileName(String fileName) {
//        Object resource = findLocaleResource(fileName, FileFinder);
//        if (resource instanceof File)
//            return ((File) resource).getAbsolutePath();
//        if (resource instanceof String)
//            return (String) resource;
//        return fileName;
//        // String nl = Platform.getNL();
//        // if ( nl == null || "".equals( nl ) || DEFAULT_NL.equals( nl ) )
//        // //$NON-NLS-1$ //$NON-NLS-2$
//        // return filename;
//        // String nlFileName = generateNLPath( filename, nl );
//        // if ( fileExists( nlFileName ) )
//        // return nlFileName;
//        // String partNL = getPartNL( nl );
//        // if ( !partNL.equals( nl ) ) {
//        // nlFileName = generateNLPath( filename, partNL );
//        // if ( fileExists( nlFileName ) )
//        // return nlFileName;
//        // }
//        // return filename;
//        // String fn = filename.substring( 0, filename.lastIndexOf( '.' ) );
//        // String ext = filename.substring( filename.lastIndexOf( '.' ) );
//        // String nl = Platform.getNL();
//        // if ( nl == null || "".equals( nl ) || DEFAULT_NL.equals( nl ) ) {
//        // //$NON-NLS-1$ //$NON-NLS-2$
//        // return fn + ext;
//        // }else {
//        // return fn + "_" + nl + ext; //$NON-NLS-1$
//        // }
//        // return filename;
//    }
//
//    public static final IResourceFinder FileFinder = new IResourceFinder() {
//        public Object findResource(String path) {
//            File file = new File(path);
//            return file.exists() ? file : null;
//        }
//    };
//
//    public static Properties loadProperties(Class clazz, String mainPath,
//            String fileName, boolean checkNLSubDir) {
//        Properties prop = null;
//        InputStream is = FileUtils.findLocaleResourceAsStream(clazz, mainPath,
//                fileName, checkNLSubDir);
//        if (is != null) {
//            prop = new Properties();
//            try {
//                prop.load(is);
//            } catch (IOException e) {
//                Logger.log(e, //$NON-NLS-1$
//                        "Error while loading properties file: class=" + clazz //$NON-NLS-1$ 
//                                + ", mainPath=" + mainPath //$NON-NLS-1$
//                                + ", fileName=" + fileName);
//            } finally {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    Logger.log(e, //$NON-NLS-1$
//                            "Error while closing input stream: class=" + clazz //$NON-NLS-1$ 
//                                    + ", mainPath=" + mainPath //$NON-NLS-1$
//                                    + ", fileName=" + fileName);
//                }
//            }
//        }
//        return prop;
//    }
//
//    public static InputStream findLocaleResourceAsStream(final Class clazz,
//            String mainPath, String fileName, boolean checkNLSubDir) {
//        InputStream is = null;
//        String dir = findResourceDir(mainPath, DEFAULT_PROVIDER, true,
//                checkNLSubDir);
//        if (dir != null)
//            is = findLocaleFileInputStream(dir, fileName);
//        if (is == null) {
//            if (checkNLSubDir) {
//                dir = findResourceDir(mainPath, DEFAULT_PROVIDER, true, false);
//                if (dir != null)
//                    is = findLocaleFileInputStream(dir, fileName);
//            }
//            if (is == null) {
//                dir = findResourceDir(mainPath, DEFAULT_PROVIDER, false, false);
//                if (dir != null)
//                    is = findLocaleFileInputStream(dir, fileName);
//                if (is == null) {
//                    String path = joinPath(mainPath, fileName);
//                    is = findLocaleResourceAsStream(clazz, path);
//                    if (is == null) {
//                        path = joinPath(joinPath(mainPath, DEFAULT_PROVIDER),
//                                fileName);
//                        is = findLocaleResourceAsStream(clazz, path);
//                    }
//                }
//            }
//        }
//        return is;
//    }
//
//    /**
//     * @param dir
//     * @param fileName
//     * @return
//     */
//    private static InputStream findLocaleFileInputStream(String dir,
//            String fileName) {
//        InputStream is = null;
//        File f = new File(dir, fileName);
//        String path = getLocaleFileName(f.getAbsolutePath());
//        File nlFile = new File(path);
//        if (nlFile.exists()) {
//            try {
//                is = new FileInputStream(nlFile);
//            } catch (FileNotFoundException e) {
//                Logger.log(e, "Error while looking for resource file: dir=" //$NON-NLS-1$ 
//                        + dir + ", fileName=" //$NON-NLS-1$
//                        + fileName);
//            }
//        }
//        if (is == null && f.exists()) {
//            try {
//                is = new FileInputStream(f);
//            } catch (FileNotFoundException e) {
//                Logger.log(e, "Error while looking for resource file: dir=" //$NON-NLS-1$ 
//                        + dir + ", fileName=" //$NON-NLS-1$
//                        + fileName);
//            }
//        }
//        return is;
//    }
//
//    public static String joinPath(String mainPath, String subPath) {
//        String sep = null;
//        if (mainPath.endsWith("/") || mainPath.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
//            int length = mainPath.length();
//            sep = mainPath.substring(length - 1, length);
//            mainPath = mainPath.substring(0, length - 1);
//        }
//        if (sep == null) {
//            if (subPath.startsWith("/") || subPath.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
//                sep = subPath.substring(0, 1);
//                subPath = subPath.substring(1, subPath.length());
//            }
//        }
//        if (sep == null)
//            sep = "/"; //$NON-NLS-1$ 
//        return mainPath + sep + subPath;
//    }
//
//    public static InputStream findLocaleResourceAsStream(final Class clazz,
//            String path) {
//        final Class realClass;
//        final String realPath;
//        if (clazz == null) {
//            realClass = FileUtils.class;
//            realPath = path.startsWith("/") ? path //$NON-NLS-1$ 
//                    : ("/" + path); //$NON-NLS-1$
//        } else {
//            realClass = clazz;
//            realPath = path;
//        }
//        return (InputStream) findLocaleResource(realPath,
//                new IResourceFinder() {
//                    public Object findResource(String path) {
//                        return realClass.getResourceAsStream(path);
//                    }
//                });
//    }
//
//    public static Object findLocaleResource(String path, IResourceFinder finder) {
//        if (finder == null)
//            finder = FileFinder;
//        Object resource = null;
//        String nl = Platform.getNL();
//        if (nl != null && !"".equals(nl) && !DEFAULT_NL.equals(nl)) { //$NON-NLS-1$ 
//            resource = finder.findResource(getNLFilePath(path, nl));
//        }
//        if (resource == null) {
//            String partNL = getPartNL(nl);
//            if (!partNL.equals(nl)) {
//                resource = finder.findResource(getNLFilePath(path, partNL));
//            }
//        }
//        if (resource == null) {
//            resource = finder.findResource(path);
//        }
//        return resource;
//    }
//
//    public static String getNLFilePath(String fileName, String nl) {
//        if (nl == null || "".equals(nl) || DEFAULT_NL.equals(nl)) //$NON-NLS-1$
//            return fileName;
//        int dotIndex = fileName.lastIndexOf('.');
//        if (dotIndex < 0) {
//            return fileName + "_" + nl; //$NON-NLS-1$ 
//        }
//        String fn = fileName.substring(0, dotIndex);
//        String ex = fileName.substring(dotIndex);
//        return fn + "_" + nl + ex; //$NON-NLS-1$
//    }
//
//    // public static boolean fileExists( String nlFileName ) {
//    // if ( new File( nlFileName ).exists() )
//    // return true;
//    // try {
//    // InputStream resource = BrainyPlatform.class.getResourceAsStream(
//    // nlFileName );
//    // return resource != null;
//    // }catch ( Exception e ) {
//    // }
//    // return false;
//    // }
//
//    public static String getPartNL(String nl) {
//        int underlineIndex = nl.indexOf('_');
//        if (underlineIndex < 0)
//            return nl;
//        return nl.substring(0, underlineIndex);
//    }
//
//    public static String[] getNLParts(String nl) {
//        return nl.split("_"); //$NON-NLS-1$
//    }
//
//    public static boolean isThisNL(String fileName, String nl) {
//        if ("".equals(nl) || DEFAULT_NL.equals(nl)) //$NON-NLS-1$ 
//            nl = null;
//        String name;
//        int dotIndex = fileName.lastIndexOf('.');
//        if (dotIndex < 0) {
//            name = fileName;
//        } else {
//            name = fileName.substring(0, dotIndex);
//        }
//        return nl == null ? name.charAt(name.length() - 3) != '_' : name
//                .endsWith(nl);
//    }

}