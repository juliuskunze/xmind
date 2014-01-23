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

import java.util.HashMap;
import java.util.Map;

import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.IDomainServiceFactory;

/**
 * @author Frank Shaka
 */
public class LANDiscoveryServiceFactory implements IDomainServiceFactory {

    private Map<ICommandServiceDomain, LANDiscoveryServiceAdapter> services = new HashMap<ICommandServiceDomain, LANDiscoveryServiceAdapter>(
            2);

    public LANDiscoveryServiceFactory() {
    }

    public synchronized Object createDomainService(ICommandServiceDomain domain) {
        LANDiscoveryServiceAdapter service = services.get(domain);
        if (service == null) {
            service = new LANDiscoveryServiceAdapter();
            services.put(domain, service);
        }
        return service;
    }

}
