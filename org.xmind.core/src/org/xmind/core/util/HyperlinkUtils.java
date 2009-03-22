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
package org.xmind.core.util;

public class HyperlinkUtils {

    private HyperlinkUtils() {
    }

    public static String getProtocolName(String uri) {
        int i = uri.indexOf(':');
        if (i < 0)
            return null;
        return uri.substring(0, i);
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

}