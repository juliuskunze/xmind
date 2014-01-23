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

import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.IDomainServiceFactory;
import org.xmind.core.command.remote.socket.SocketCommandServer;

/**
 * @author Frank Shaka
 */
public class LANCommandServerFactory implements IDomainServiceFactory {

    public LANCommandServerFactory() {
    }

    public Object createDomainService(ICommandServiceDomain domain) {
        return new SocketCommandServer(LANRemoteCommandPlugin.getPreferences()
                .getPortNumber(), LANRemoteCommandPlugin.getPreferences()
                .getBacklog(), true);
    }

}
