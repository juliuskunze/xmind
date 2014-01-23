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
package org.xmind.core.util;

import org.xmind.core.IIdentifiable;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;

public class HyperlinkUtils {

    private HyperlinkUtils() {
    }

    public static String getProtocolName(String uri) {
        int i = uri.indexOf(':');
        return i < 0 ? null : uri.substring(0, i);
    }

    public static String trimURLContent(String uri) {
        int i = uri.indexOf(':');
        if (i >= 0) {
            uri = uri.substring(i + 1);
            while (uri.startsWith("/")) { //$NON-NLS-1$
                uri = uri.substring(1);
            }
        }
        return uri;
    }

    public static String getAttachmentProtocolName() {
        return "xap"; //$NON-NLS-1$
    }

    public static boolean isAttachmentURL(String url) {
        if (url == null || "".equals(url)) //$NON-NLS-1$
            return false;
        return getAttachmentProtocolName().equals(getProtocolName(url));
    }

    public static String toAttachmentURL(String path) {
        return getAttachmentProtocolName() + ":" + path; //$NON-NLS-1$
    }

    public static String toAttachmentPath(String url) {
        return trimURLContent(url);
    }

    public static String getInternalProtocolName() {
        return "xmind"; //$NON-NLS-1$
    }

    public static boolean isInternalURL(String url) {
        if (url == null || "".equals(url)) //$NON-NLS-1$
            return false;
        return getInternalProtocolName().equals(getProtocolName(url));
    }

    public static String toInternalURL(String elementId) {
        return getInternalProtocolName() + ":#" + elementId; //$NON-NLS-1$
    }

    public static String toInternalURL(Object element) {
        return toInternalURL(element, null);
    }

    public static String toInternalURL(Object element, IWorkbook workbook) {
        if (element instanceof IIdentifiable) {
            String id = ((IIdentifiable) element).getId();
            //String mainPath = getMainPath(element, workbook);
            return getInternalProtocolName() + ":#" + id; //$NON-NLS-1$
        }
        return null;
    }

    public static String toElementID(String uri) {
        if (isInternalURL(uri)) {
            int index = uri.indexOf("#"); //$NON-NLS-1$
            if (index >= 0) {
                return uri.substring(index + 1);
            }
        }
        return null;
    }

//    /**
//     * @param element
//     * @param workbook
//     * @return
//     */
//    private static String getMainPath(Object element, IWorkbook workbook) {
//        if (workbook != null) {
//            String file = workbook.getFile();
//            if (file != null) {
//                return file;
//            }
//        }
//        return ""; //$NON-NLS-1$
//    }

    public static Object findElement(String uri, IWorkbook workbook) {
        String id = toElementID(uri);
        if (id != null) {
            Object element = workbook.getElementById(id);
            if (element instanceof ITopic) {
                if (!isAttach((ITopic) element)) {
                    element = null;
                }
            }
            return element;
        }
        return null;
    }

    private static boolean isAttach(ITopic topic) {
        return topic.getPath().getWorkbook() == topic.getOwnedWorkbook();
    }

}