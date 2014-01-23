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
import static org.xmind.core.sharing.SharingConstants.PLUGIN_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_MAP;
import static org.xmind.core.sharing.SharingConstants.PROP_MAPS;
import static org.xmind.core.sharing.SharingConstants.PROP_MISSING;
import static org.xmind.core.sharing.SharingConstants.PROP_NAME;
import static org.xmind.core.sharing.SharingConstants.PROP_REMOTE;
import static org.xmind.core.sharing.SharingConstants.PROP_THUMBNAIL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.xmind.core.command.Command;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ReturnValue;
import org.xmind.core.command.arguments.ArrayMapper;
import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceInfo;
import org.xmind.core.command.remote.IIdentifier;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.command.remote.IRemoteCommandServiceListener;
import org.xmind.core.command.remote.RemoteCommandJob;
import org.xmind.core.sharing.ILocalSharedLibrary;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingListener;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.sharing.SharingEvent;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class LocalNetworkSharingService implements ISharingService,
        IRemoteCommandServiceListener {

    private static final String PROP_STATUS = "org.xmind.core.sharing.localnetwork.service.status"; //$NON-NLS-1$

    private static final String[] STATUS_VALUES = new String[] { //
    "INACTIVE", //$NON-NLS-1$
            "ACTIVATING", //$NON-NLS-1$
            "ACTIVE", //$NON-NLS-1$
            "DEACTIVATING" //$NON-NLS-1$
    };

    private static final Comparator<IRemoteSharedLibrary> RemoteLibraryComparator = new Comparator<IRemoteSharedLibrary>() {
        public int compare(IRemoteSharedLibrary r1, IRemoteSharedLibrary r2) {
            return r1.getName().compareTo(r2.getName());
        }
    };

    private class RemoteLibraryAdder extends RemoteCommandJob {

        private IRemoteCommandService remoteService;

        /**
         * @param name
         * @param pluginId
         * @param remoteService
         */
        public RemoteLibraryAdder(IRemoteCommandService remoteService) {
            super(Messages.LocalNetworkSharingService_ContractRemoteLibrary,
                    PLUGIN_ID, remoteService);
            this.remoteService = remoteService;
        }

        protected ICommand createCommand(IProgressMonitor monitor)
                throws CoreException {
            return new Command(COMMAND_SOURCE, "sharing/handshake"); //$NON-NLS-1$
        }

        protected IStatus executeCommand(IProgressMonitor sendCommandMonitor,
                ICommand command) {
            IStatus status = super.executeCommand(sendCommandMonitor, command);
            if (status.getSeverity() == IStatus.ERROR) {
                LocalNetworkSharing.log(status);
                status = Status.CANCEL_STATUS;
            }
            return status;
        }

        public IStatus consumeReturnValue(IProgressMonitor monitor,
                IStatus returnValue) {
            if (returnValue == null || !returnValue.isOK()
                    || !(returnValue instanceof ReturnValue))
                return returnValue;
            Attributes p = ((ReturnValue) returnValue).getAttributes();
            String libraryName = p.get(PROP_NAME);
            RemoteSharedLibrary remoteLibrary = new RemoteSharedLibrary(
                    remoteService, libraryName);
            ArrayMapper mapsReader = new ArrayMapper(p.getRawMap(), PROP_MAPS);
            List<ISharedMap> maps = new ArrayList<ISharedMap>(
                    mapsReader.getSize());
            while (mapsReader.hasNext()) {
                mapsReader.next();
                String mapID = (String) mapsReader.get(PROP_ID);
                String mapName = (String) mapsReader.get(PROP_NAME);
                String thumbnailData = (String) mapsReader.get(PROP_THUMBNAIL);
                String missing = (String) mapsReader.get(PROP_MISSING);
                maps.add(new RemoteSharedMap(remoteService, remoteLibrary,
                        mapID, mapName,
                        Base64.base64ToByteArray(thumbnailData), Boolean
                                .parseBoolean(missing)));
            }
            remoteLibrary.addMaps(maps);
            addRemoteLibrary(remoteLibrary);
            return returnValue;
        }

    }

    private class BroadcastLocalEventJob extends RemoteCommandJob {

        private SharingEvent event;

        /**
         * @param name
         * @param pluginId
         * @param client
         */
        public BroadcastLocalEventJob(IRemoteCommandService remoteService,
                SharingEvent event) {
            super(Messages.LocalNetworkSharingService_BroadcastShareEvent,
                    PLUGIN_ID, remoteService);
            this.event = event;
        }

        protected IStatus executeCommand(IProgressMonitor sendCommandMonitor,
                ICommand command) {
            IStatus status = super.executeCommand(sendCommandMonitor, command);
            if (status.getSeverity() == IStatus.ERROR) {
                LocalNetworkSharing.log(status);
                status = Status.CANCEL_STATUS;
            }
            return status;
        }

        public IStatus consumeReturnValue(IProgressMonitor monitor,
                IStatus returnValue) {
            return returnValue;
        }

        protected ICommand createCommand(IProgressMonitor monitor)
                throws CoreException {
            ICommandServiceDomain theDomain;
            synchronized (lock) {
                theDomain = domain;
            }
            if (theDomain == null)
                return null;

            String commandName = "sharing/event/" + event.getType().name(); //$NON-NLS-1$
            Attributes data = new Attributes();
            ICommandServiceInfo info = theDomain.getCommandServerAdvertiser()
                    .getRegisteredInfo();
            if (info == null) {
                LocalNetworkSharing
                        .log("Failed to broadcast local library change: Local command server is not registered or has been unregistered.", null); //$NON-NLS-1$
                return null;
            }
            IIdentifier serverId = info.getId();
            if (serverId == null) {
                LocalNetworkSharing
                        .log("Failed to broadcast local library change: Local command server is not identified.", null); //$NON-NLS-1$
                return null;
            }
            data.with(PROP_REMOTE, serverId.getName());
            ISharedMap map = event.getMap();
            if (map != null) {
                data.with(PROP_MAP, map.getID());
                data.with(PROP_NAME, map.getResourceName());
                data.with(PROP_THUMBNAIL,
                        ((LocalSharedMap) map).getEncodedThumbnailData());
                data.with(PROP_MISSING, map.isMissing() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                data.with(PROP_NAME, getLocalLibrary().getName());
            }
            return new Command(COMMAND_SOURCE, commandName, data, null, null);
        }
    }

    private int status = INACTIVE;

    private ICommandServiceDomain domain;

    private LocalSharedLibrary localLibrary = null;

    private Map<IRemoteCommandService, IRemoteSharedLibrary> remoteLibraries = new HashMap<IRemoteCommandService, IRemoteSharedLibrary>();

    private ListenerList listeners = new ListenerList();

    private boolean disposed = false;

    private Object lock = new Object();

    private List<Job> jobs = new ArrayList<Job>();

    LocalNetworkSharingService() {
        System.setProperty(PROP_STATUS, STATUS_VALUES[INACTIVE]);
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        if (adapter == ICommandServiceDomain.class)
            return domain;
        if (adapter == ISchedulingRule.class)
            return this;
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    public IStatus activate(IProgressMonitor monitor) {
        synchronized (lock) {
            if (this.status == INACTIVE) {
                changeStatus(ACTIVATING);
            } else {
                return Status.CANCEL_STATUS;
            }
        }

        monitor.beginTask(null, 100);
        monitor.subTask(Messages.LocalNetworkSharingService_ConnectionLocalNetworkSharing);

        ICommandServiceDomain theDomain;
        synchronized (lock) {
            if (domain == null) {
                domain = LocalNetworkSharing
                        .getDefault()
                        .getCommandServiceDomainManager()
                        .getCommandServiceDomain(
                                "org.xmind.core.command.remote.domains.lan"); //$NON-NLS-1$
            }
            theDomain = domain;
        }

        if (theDomain == null) {
            synchronized (lock) {
                changeStatus(INACTIVE);
            }
            return new Status(IStatus.ERROR, LocalNetworkSharing.ID, 23330,
                    "No LAN remote domain service available.", null); //$NON-NLS-1$
        }
        theDomain.getRemoteCommandServiceDiscoverer()
                .addRemoteCommandServiceListener(this);
        IStatus connected = theDomain.getDirector().connect(
                new SubProgressMonitor(monitor, 100));
        if (connected != null && !connected.isOK()) {
            theDomain.getRemoteCommandServiceDiscoverer()
                    .removeRemoteCommandServiceListener(this);
            synchronized (lock) {
                changeStatus(DEACTIVATING);
            }
            theDomain.getDirector().disconnect(new NullProgressMonitor());
            synchronized (lock) {
                changeStatus(INACTIVE);
            }
            return connected;
        }

        synchronized (lock) {
            changeStatus(ACTIVE);
        }

        monitor.done();
        return Status.OK_STATUS;
    }

    public IStatus deactivate(IProgressMonitor monitor) {
        synchronized (lock) {
            if (this.status == ACTIVE) {
                changeStatus(DEACTIVATING);
            } else {
                return Status.CANCEL_STATUS;
            }
        }
        monitor.beginTask(null, 100);
        monitor.subTask(Messages.LocalNetworkSharingService_DisconnectionLocalNetworkSharing);

        ICommandServiceDomain theDomain;
        synchronized (lock) {
            theDomain = domain;
            domain = null;
        }

        IStatus result = null;
        if (theDomain != null) {
            clearJobs();
            IStatus disconnected = theDomain.getDirector().disconnect(
                    new SubProgressMonitor(monitor, 100));
            if (disconnected != null && !disconnected.isOK())
                result = disconnected;
            theDomain.getRemoteCommandServiceDiscoverer()
                    .removeRemoteCommandServiceListener(this);
        }

        synchronized (lock) {
            changeStatus(INACTIVE);
        }

        monitor.done();
        if (result != null)
            return result;
        return Status.OK_STATUS;
    }

    private void changeStatus(int newStatus) {
        int oldStatus = this.status;
        this.status = newStatus;
        System.setProperty(PROP_STATUS, STATUS_VALUES[newStatus]);
        fireSharingEvent(new SharingEvent(oldStatus, newStatus));
    }

    public int getStatus() {
        return this.status;
    }

    public synchronized ILocalSharedLibrary getLocalLibrary() {
        if (this.localLibrary == null && !disposed) {
            this.localLibrary = new LocalSharedLibrary(this);
        }
        return this.localLibrary;
    }

    public synchronized Collection<IRemoteSharedLibrary> getRemoteLibraries() {
        ICommandServiceDomain theDomain = domain;
        SortedSet<IRemoteSharedLibrary> result = new TreeSet<IRemoteSharedLibrary>(
                RemoteLibraryComparator);
        if (theDomain != null) {
            IRemoteCommandService[] remoteCommandServices = theDomain
                    .getRemoteCommandServiceDiscoverer()
                    .getRemoteCommandServices();
            for (int i = 0; i < remoteCommandServices.length; i++) {
                IRemoteCommandService remoteService = remoteCommandServices[i];
                IRemoteSharedLibrary library = remoteLibraries
                        .get(remoteService);
                if (library != null) {
                    result.add(library);
                }
            }
        }
        return result;
    }

    public synchronized IRemoteSharedLibrary findRemoteLibrary(
            String symbolicName) {
        if (symbolicName == null)
            return null;
        ICommandServiceDomain theDomain = domain;
        if (theDomain == null)
            return null;
        IRemoteCommandService service = theDomain
                .getRemoteCommandServiceDiscoverer().findRemoteCommandService(
                        symbolicName);
        if (service == null)
            return null;
        return remoteLibraries.get(service);
    }

    public IStatus refresh(IProgressMonitor monitor) {
        if (this.status != ACTIVE)
            return Status.CANCEL_STATUS;

        this.remoteLibraries.clear();
        ICommandServiceDomain theDomain = domain;
        if (theDomain == null)
            return Status.CANCEL_STATUS;
        return theDomain.getRemoteCommandServiceDiscoverer().refresh(monitor);
    }

    public void addSharingListener(ISharingListener listener) {
        listeners.add(listener);
    }

    public void removeSharingListener(ISharingListener listener) {
        listeners.remove(listener);
    }

    public void registerJob(Job job) {
        if (job == null)
            return;
        jobs.add(job);
        job.addJobChangeListener(new JobChangeAdapter() {
            public void done(IJobChangeEvent event) {
                jobs.remove(event.getJob());
            }
        });
    }

    private void clearJobs() {
        Object[] theJobs;
        synchronized (jobs) {
            theJobs = jobs.toArray();
            jobs.clear();
        }
        for (int i = 0; i < theJobs.length; i++) {
            Job job = (Job) theJobs[i];
            if (job != null) {
                job.cancel();
            }
        }
    }

    public synchronized void dispose() {
        this.disposed = true;
        if (this.localLibrary != null) {
            this.localLibrary.dispose();
            this.localLibrary = null;
        }
        this.remoteLibraries.clear();
    }

    public void remoteCommandServiceDiscovered(IRemoteCommandService service) {
        RemoteLibraryAdder adder = new RemoteLibraryAdder(service);
        adder.setUser(false);
        adder.setSystem(true);
        adder.setRule(this);
        registerJob(adder);
        adder.schedule();
    }

    public void remoteCommandServiceDropped(IRemoteCommandService service) {
        removeRemoteLibraryByCommandService(service);
    }

    private synchronized void addRemoteLibrary(RemoteSharedLibrary remoteLibrary) {
        removeRemoteLibraryByCommandService(remoteLibrary
                .getRemoteCommandService());
        remoteLibraries.put(remoteLibrary.getRemoteCommandService(),
                remoteLibrary);
        fireSharingEvent(new SharingEvent(SharingEvent.Type.LIBRARY_ADDED,
                remoteLibrary));
    }

    private synchronized void removeRemoteLibraryByCommandService(
            IRemoteCommandService service) {
        ISharedLibrary remoteLibrary;
        while ((remoteLibrary = remoteLibraries.get(service)) != null) {
            removeRemoteLibrary(service, remoteLibrary);
        }
    }

    private synchronized void removeRemoteLibrary(
            IRemoteCommandService service, ISharedLibrary remoteLibrary) {
        remoteLibraries.remove(service);
        fireSharingEvent(new SharingEvent(SharingEvent.Type.LIBRARY_REMOVED,
                remoteLibrary));
    }

    /**
     * <b>Note: For internal use only.</b>
     * 
     * @param event
     */
    protected void fireSharingEvent(final SharingEvent event) {
        for (Object listener : listeners.getListeners()) {
            ((ISharingListener) listener).handleSharingEvent(event);
        }
        broadcastLocalChanges(event);
    }

    private void broadcastLocalChanges(final SharingEvent event) {
        if (!event.isLocal())
            return;

        for (IRemoteSharedLibrary remoteLibrary : remoteLibraries.values()) {
            BroadcastLocalEventJob job = new BroadcastLocalEventJob(
                    (IRemoteCommandService) remoteLibrary
                            .getAdapter(IRemoteCommandService.class),
                    event);
            job.setRule(this);
            job.setUser(false);
            job.setSystem(true);
            registerJob(job);
            job.schedule();
        }
    }

    public boolean contains(ISchedulingRule rule) {
        return rule == this;
    }

    public boolean isConflicting(ISchedulingRule rule) {
        return rule == this;
    }

}
