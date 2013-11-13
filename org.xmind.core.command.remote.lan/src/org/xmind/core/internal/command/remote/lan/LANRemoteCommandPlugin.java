package org.xmind.core.internal.command.remote.lan;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.command.remote.lan.ILANDomainPreferences;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class LANRemoteCommandPlugin implements BundleActivator {

    public static final String ID = "org.xmind.core.command.remote.lan"; //$NON-NLS-1$

    private static BundleContext context;

    private static LANRemoteCommandPlugin singleton;

    private ILANDomainPreferences preferences;

    private ServiceRegistration<ILANDomainPreferences> preferencesRegistration;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext bundleContext) throws Exception {
        LANRemoteCommandPlugin.singleton = this;
        LANRemoteCommandPlugin.context = bundleContext;

        preferences = new LANDomainPreferences(ID);
        preferencesRegistration = bundleContext.registerService(
                ILANDomainPreferences.class, preferences, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        if (preferencesRegistration != null) {
            preferencesRegistration.unregister();
            preferencesRegistration = null;
        }
        if (debugTracker != null) {
            debugTracker.close();
            debugTracker = null;
        }
        LANRemoteCommandPlugin.context = null;
        LANRemoteCommandPlugin.singleton = null;
    }

    private DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    context, DebugOptions.class.getName(), null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public static boolean isDebugging(String option) {
        DebugOptions debugOptions = getDefault().getDebugOptions();
        return debugOptions != null
                && debugOptions.getBooleanOption(option == null ? ID : ID
                        + option, false);
    }

    public static ILANDomainPreferences getPreferences() {
        return getDefault().preferences;
    }

    public static void log(String message, Throwable e) {
        log(new Status(e == null ? IStatus.WARNING : IStatus.ERROR, ID,
                message, e));
    }

    public static void log(IStatus status) {
        ILog log = Platform.getLog(context.getBundle());
        if (log != null)
            log.log(status);
    }

    public static LANRemoteCommandPlugin getDefault() {
        return LANRemoteCommandPlugin.singleton;
    }

}
