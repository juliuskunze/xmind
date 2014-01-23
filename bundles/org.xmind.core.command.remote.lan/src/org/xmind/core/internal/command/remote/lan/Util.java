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
/**
 * 
 */
package org.xmind.core.internal.command.remote.lan;

import java.io.UnsupportedEncodingException;

/**
 * @author Frank Shaka
 */
public class Util {

    private Util() {
        throw new AssertionError();
    }

    public static byte[] encode(String str) {
        try {
            return str.getBytes("UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }

    public static String decode(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

}
