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

import org.xmind.core.internal.command.remote.lan.LANRemoteCommandPlugin;

import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.ResolveListener;

/**
 * @author Frank Shaka
 */
public abstract class ResolveAdapter implements ResolveListener {

    protected final int flags;

    protected final int ifIndex;

    protected final String serviceName;

    protected final String regType;

    protected final String domain;

    /**
     * @param flags
     * @param ifIndex
     * @param serviceName
     * @param regType
     * @param domain
     */
    public ResolveAdapter(int flags, int ifIndex, String serviceName,
            String regType, String domain) {
        super();
        this.flags = flags;
        this.ifIndex = ifIndex;
        this.serviceName = serviceName;
        this.regType = regType;
        this.domain = domain;
    }

    public int getFlags() {
        return flags;
    }

    public int getIfIndex() {
        return ifIndex;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getRegType() {
        return regType;
    }

    public String getDomainName() {
        return domain;
    }

    public void operationFailed(DNSSDService service, int errorCode) {
        LANRemoteCommandPlugin.log("DNSSD resolver failed: ErrorCode=" //$NON-NLS-1$
                + errorCode, null);
    }

}
