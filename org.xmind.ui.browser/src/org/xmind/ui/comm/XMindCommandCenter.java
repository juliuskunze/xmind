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
package org.xmind.ui.comm;

import org.xmind.ui.internal.comm.XMindCommand;
import org.xmind.ui.internal.comm.XMindCommandSupport;

public class XMindCommandCenter {

    private XMindCommandCenter() {
    }

    public static boolean dispatch(IXMindCommand command) {
        return XMindCommandSupport.getInstance().handleCommand(command);
    }

    public static boolean dispatch(String uri) {
        return XMindCommandSupport.getInstance().handleCommand(uri);
    }

    public static boolean canHandle(String uri) {
        return uri != null && uri.startsWith(XMindCommand.XMIND_PROTOCOL);
    }
}
