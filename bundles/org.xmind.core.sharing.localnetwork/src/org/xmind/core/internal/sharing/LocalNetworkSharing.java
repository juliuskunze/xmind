package org.xmind.core.internal.sharing;

import java.io.File;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.command.binary.BinaryStore;
import org.xmind.core.command.binary.IBinaryStore;
import org.xmind.core.command.remote.ICommandServiceDomainManager;
import org.xmind.core.sharing.ISharingService;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class LocalNetworkSharing implements BundleActivator {

    public static final String ID = "org.xmind.core.sharing.localnetwork"; //$NON-NLS-1$

    public static final String DEBUG_OPTION = "/debug"; //$NON-NLS-1$

    private static LocalNetworkSharing singleton;

    private Bundle bundle;

    private ServiceTracker<ICommandServiceDomainManager, ICommandServiceDomainManager> domanManagerTracker;

    private ISharingService sharingService;

    private ServiceRegistration<ISharingService> sharingServiceRegistration;

    private IBinaryStore remoteCache = null;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    public static LocalNetworkSharing getDefault() {
        return LocalNetworkSharing.singleton;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext bundleContext) throws Exception {
        LocalNetworkSharing.singleton = this;
        this.bundle = bundleContext.getBundle();

        this.domanManagerTracker = new ServiceTracker<ICommandServiceDomainManager, ICommandServiceDomainManager>(
                bundleContext, ICommandServiceDomainManager.class.getName(),
                null);
        this.domanManagerTracker.open();

        this.sharingService = new LocalNetworkSharingService();
        this.sharingServiceRegistration = bundleContext.registerService(
                ISharingService.class, this.sharingService, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (this.remoteCache != null) {
            this.remoteCache.clear();
            this.remoteCache = null;
        }
        if (this.sharingServiceRegistration != null) {
            this.sharingServiceRegistration.unregister();
            this.sharingServiceRegistration = null;
        }
        if (this.sharingService != null) {
            ((LocalNetworkSharingService) this.sharingService).dispose();
            this.sharingService = null;
        }
        if (this.domanManagerTracker != null) {
            this.domanManagerTracker.close();
            this.domanManagerTracker = null;
        }
        if (debugTracker != null) {
            debugTracker.close();
            debugTracker = null;
        }
        this.bundle = null;
        LocalNetworkSharing.singleton = null;
    }

    public ICommandServiceDomainManager getCommandServiceDomainManager() {
        return domanManagerTracker == null ? null : domanManagerTracker
                .getService();
    }

    public ISharingService getSharingService() {
        return this.sharingService;
    }

    public IBinaryStore getRemoteCaches() {
        if (this.remoteCache == null && this.bundle != null) {
            File dataDir = getDataDirectory();
            this.remoteCache = new BinaryStore(new File(dataDir,
                    "remote-caches")); //$NON-NLS-1$
            remoteCache.clear();
        }
        return remoteCache;
    }

    public File getDataDirectory() {
        return Platform.getStateLocation(this.bundle).toFile();
    }

    private ILog getLog() {
        return Platform.getLog(this.bundle);
    }

    public static void log(String message, Throwable e) {
        log(new Status(e == null ? IStatus.WARNING : IStatus.ERROR, ID,
                message, e));
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    private DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    bundle.getBundleContext(), DebugOptions.class.getName(),
                    null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public static boolean isDebugging(String option) {
        DebugOptions debugOptions = getDefault().getDebugOptions();
        return debugOptions != null
                && debugOptions.getBooleanOption(ID + option, false);
    }

}
