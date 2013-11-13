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
package org.xmind.core.internal.command.remote.lan.dnssd;

import org.xmind.core.command.remote.CommandServiceInfo;
import org.xmind.core.command.remote.ICommandServiceInfo;

import com.apple.dnssd.DNSSDRegistration;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class DNSSDCommandServiceInfo extends CommandServiceInfo {

    public final DNSSDRegistration registration;

    public final int flags;

    public final String serviceName;

    public final String regType;

    public final String domain;

    /**
     * Creates a DNSSD command service info instance for the registered local
     * command server.
     * 
     * @param source
     * @param registration
     * @param flags
     * @param serviceName
     * @param regType
     * @param domain
     */
    public DNSSDCommandServiceInfo(ICommandServiceInfo source,
            DNSSDRegistration registration, int flags, String serviceName,
            String regType, String domain) {
        super(source);
        this.registration = registration;
        this.flags = flags;
        this.serviceName = serviceName;
        this.regType = regType;
        this.domain = domain;
    }

    /**
     * Creates a DNSSD command service info instance for a remove command
     * service.
     * 
     * @param flags
     * @param serviceName
     * @param regType
     * @param domain
     */
    public DNSSDCommandServiceInfo(int flags, String serviceName,
            String regType, String domain) {
        super();
        this.registration = null;
        this.flags = flags;
        this.serviceName = serviceName;
        this.regType = regType;
        this.domain = domain;
    }

}