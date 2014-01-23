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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.IReturnValueConsumer;
import org.xmind.core.command.remote.ICommandServiceInfo;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.command.remote.Identifier;
import org.xmind.core.command.remote.Options;
import org.xmind.core.command.remote.socket.OutgoingSocketCommandHandler;
import org.xmind.core.command.remote.socket.SocketAddress;
import org.xmind.core.command.remote.socket.SocketCommandServiceInfo;
import org.xmind.core.command.remote.socket.SocketPool;
import org.xmind.core.internal.command.remote.lan.LANRemoteCommandPlugin;
import org.xmind.core.internal.command.remote.lan.Messages;
import org.xmind.core.internal.command.remote.lan.Util;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.TXTRecord;

/**
 * @author Frank Shaka
 */
public class DNSSDRemoteCommandService extends ResolveAdapter implements
        IRemoteCommandService {

    private final String domainId;

    private final SocketCommandServiceInfo info;

    private boolean resolving = false;

    private SocketPool socketPool = null;

    /**
     * @param flags
     * @param ifIndex
     * @param serviceName
     * @param regType
     * @param domain
     */
    public DNSSDRemoteCommandService(int flags, int ifIndex,
            String serviceName, String regType, String domain, String hostName,
            int port, TXTRecord txtRecord, String domainId) {
        super(flags, ifIndex, serviceName, regType, domain);
        this.domainId = domainId;
        this.info = new SocketCommandServiceInfo();
        fillCommandServiceInfo(hostName, port, txtRecord);
    }

    public void setSocketPool(SocketPool socketPool) {
        this.socketPool = socketPool;
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            IReturnValueConsumer returnValueConsumer, Options options) {
        monitor.beginTask(null, 100);

        monitor.subTask(Messages.DNSSDRemoteCommandService_ResolveRemoteCommandService);

        boolean doResolve = false;
        synchronized (info) {
            if (!resolving) {
                resolving = true;
                doResolve = true;
            }
        }

        DNSSDService resolver = null;
        if (doResolve) {
            try {
                resolver = DNSSD.resolve(flags, ifIndex, serviceName, regType,
                        domain, this);
            } catch (DNSSDException e) {
                return new Status(
                        IStatus.ERROR,
                        LANRemoteCommandPlugin.ID,
                        Messages.DNSSDRemoteCommandService_FailedResolveRemoteCommand,
                        e);
            }

        }
        try {
            while (resolving) {
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;
                Thread.sleep(0);
            }
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        } finally {
            if (resolver != null) {
                resolver.stop();
            }
        }

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.worked(10);

        monitor.subTask(Messages.DNSSDRemoteCommandService_SendCommand);
        IProgressMonitor handleMonitor = new SubProgressMonitor(monitor, 90);
        OutgoingSocketCommandHandler handler = new OutgoingSocketCommandHandler(
                info.getAddress());
        handler.setPluginId(LANRemoteCommandPlugin.ID);
        handler.setSocketPool(socketPool);
        IStatus handled = handler.handleOutgoingCommand(handleMonitor, command,
                returnValueConsumer, options.timeout());
        if (!handleMonitor.isCanceled())
            handleMonitor.done();
        return handled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IRemoteCommandService#getInfo()
     */
    public ICommandServiceInfo getInfo() {
        return info;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.apple.dnssd.ResolveListener#serviceResolved(com.apple.dnssd.DNSSDService
     * , int, int, java.lang.String, java.lang.String, int,
     * com.apple.dnssd.TXTRecord)
     */
    public void serviceResolved(DNSSDService resolver, int flags, int ifIndex,
            String fullName, String hostName, int port, TXTRecord txtRecord) {
        fillCommandServiceInfo(hostName, port, txtRecord);
        synchronized (info) {
            resolving = false;
        }
    }

    private void fillCommandServiceInfo(String hostName, int port,
            TXTRecord txtRecord) {
        info.setId(new Identifier(domainId, serviceName));
        info.setAddress(new SocketAddress(hostName, port));
        byte[] nameBytes = txtRecord
                .getValue(DNSSDDiscoveryServiceAdapter.NAME);
        if (nameBytes != null)
            info.setName(Util.decode(nameBytes));
        restoreMetadata(info, txtRecord, ICommandServiceInfo.VERSION);
        restoreMetadata(info, txtRecord, ICommandServiceInfo.CLIENT_NAME);
        restoreMetadata(info, txtRecord,
                ICommandServiceInfo.CLIENT_SYMBOLIC_NAME);
        restoreMetadata(info, txtRecord, ICommandServiceInfo.CLIENT_VERSION);
        restoreMetadata(info, txtRecord, ICommandServiceInfo.CLIENT_BUILD_ID);
    }

    private static void restoreMetadata(SocketCommandServiceInfo info,
            TXTRecord txtRecord, String key) {
        byte[] bytes = txtRecord.getValue(key);
        info.setMetadata(key, bytes == null ? null : Util.decode(bytes));
    }

}
