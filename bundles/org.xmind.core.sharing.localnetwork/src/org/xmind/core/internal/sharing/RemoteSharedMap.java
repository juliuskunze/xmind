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
package org.xmind.core.internal.sharing;

import static org.xmind.core.sharing.SharingConstants.COMMAND_SOURCE;
import static org.xmind.core.sharing.SharingConstants.PROP_CONTENT;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.Command;
import org.xmind.core.command.IReturnValueConsumer;
import org.xmind.core.command.ReturnValue;
import org.xmind.core.command.binary.IBinaryEntry;
import org.xmind.core.command.binary.IBinaryStore;
import org.xmind.core.command.binary.INamedEntry;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.command.remote.Options;
import org.xmind.core.sharing.ISharedLibrary;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class RemoteSharedMap extends AbstractSharedMap {

    private IRemoteCommandService remoteServer;

    private INamedEntry resourceCache = null;

    public RemoteSharedMap(IRemoteCommandService remoteServer,
            ISharedLibrary library, String id, String name,
            byte[] thumbnailData, boolean missing) {
        super(library, id, name, thumbnailData);
        setMissing(missing);
        this.remoteServer = remoteServer;
    }

    public InputStream getResourceAsStream(IProgressMonitor loadingProgress) {
        loadingProgress.beginTask(null, 100);
        IBinaryEntry cache = getResourceCache(loadingProgress);
        try {
            return cache == null ? null : cache.openInputStream();
        } catch (IOException e) {
            LocalNetworkSharing.log("Failed to load resource from cache.", e); //$NON-NLS-1$
            return null;
        } finally {
            loadingProgress.done();
        }
    }

    private synchronized IBinaryEntry getResourceCache(
            IProgressMonitor loadingProgress) {
        if (this.resourceCache == null) {
            this.resourceCache = loadResourceIntoCache(loadingProgress);
        }
        return this.resourceCache;
    }

    private INamedEntry loadResourceIntoCache(IProgressMonitor loadingProgress) {
        final INamedEntry[] cache = new INamedEntry[1];
        cache[0] = null;
        remoteServer.execute(loadingProgress, new Command(COMMAND_SOURCE,
                "sharing/file/" + getID(), null, null, null), //$NON-NLS-1$
                new IReturnValueConsumer() {
                    public IStatus consumeReturnValue(IProgressMonitor monitor,
                            IStatus returnValue) {
                        if (returnValue.isOK()
                                && returnValue instanceof ReturnValue) {
                            IBinaryStore caches = ((ReturnValue) returnValue)
                                    .getBinaryEntries();
                            if (caches != null) {
                                IBinaryEntry entry = caches
                                        .getEntry(PROP_CONTENT);
                                if (entry != null) {
                                    try {
                                        cache[0] = saveResourceCache(monitor,
                                                entry);
                                    } catch (IOException e) {
                                        LocalNetworkSharing
                                                .log("Failed to cache shared filed.", //$NON-NLS-1$
                                                        e);
                                    } catch (InterruptedException e) {
                                        if (monitor.isCanceled())
                                            return Status.CANCEL_STATUS;
                                    }
                                }
                            }
                        }
                        if (monitor.isCanceled())
                            return Status.CANCEL_STATUS;
                        monitor.done();
                        return Status.OK_STATUS;
                    }
                }, Options.DEFAULT);
        return cache[0];
    }

    private INamedEntry saveResourceCache(IProgressMonitor monitor,
            IBinaryEntry entry) throws IOException, InterruptedException {
        InputStream input = entry.openInputStream();
        try {
            return getRemoteCaches().addEntry(monitor, input);
        } finally {
            input.close();
        }
    }

    protected synchronized void invalidateResourceCache() {
        if (this.resourceCache != null) {
            getRemoteCaches().removeEntry(this.resourceCache.getName());
            this.resourceCache = null;
        }
    }

    private IBinaryStore getRemoteCaches() {
        return LocalNetworkSharing.getDefault().getRemoteCaches();
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RemoteSharedMap))
            return false;
        RemoteSharedMap that = (RemoteSharedMap) obj;
        return this.getID().equals(that.getID())
                && this.remoteServer.getInfo().getId()
                        .equals(that.remoteServer.getInfo().getId());
    }

    @Override
    public String toString() {
        return "RemoteSharedMap(" + getResourceName() //$NON-NLS-1$
                + "@" + getID() //$NON-NLS-1$
                + ")"; //$NON-NLS-1$
    }

}
