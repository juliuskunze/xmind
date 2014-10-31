package org.xmind.ui.internal.sharing;

import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.sharing.ISharingService;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Frank Shaka
 */
public class LocalNetworkSharingUI extends AbstractUIPlugin {

    // The plug-in ID:
    public static final String PLUGIN_ID = "org.xmind.ui.sharing"; //$NON-NLS-1$

    // The Local Network Sharing view ID:
    public static final String VIEW_ID = "org.xmind.ui.LocalNetworkSharingView"; //$NON-NLS-1$

    // The Local Network Sharing preference page ID:
    public static final String PREF_PAGE_ID = "org.xmind.ui.LocalNetworkSharingPrefPage"; //$NON-NLS-1$

    // Preference key for enabling/disabling Local Network Sharing UI features:
    // Values: true, false (default)
    public static final String PREF_FEATURE_ENABLED = "LNS_FEATURE_ENABLED"; //$NON-NLS-1$

    public static final String PREF_SERVICE_ACTIVATED = "LNS_SERVICE_ACTIVATED"; //$NON-NLS-1$

    public static final String PREF_SKIP_AUTO_ENABLE = "SKIP_AUTO_ENABLE"; //$NON-NLS-1$

//    public static final String PREF_NO_BONJOUR = "NO_BONJOUR"; //$NON-NLS-1$

//    public static final String PREF_SKIP_LAUNCHING_ON_STARTUP = "SKIP_LAUNCHING_ON_STARTUP"; //$NON-NLS-1$
//
//    public static final String PREF_SKIP_WELCOME_DIALOG_ON_STARTUP = "SKIP_WELCOME_DIALOG_ON_STARTUP"; //$NON-NLS-1$
//
//    public static final String PREF_SHOW_SHARING_SERVICE_STATUS = "SHOW_SHARING_SERVICE_STATUS"; //$NON-NLS-1$

    // The shared instance
    private static LocalNetworkSharingUI plugin;

    private ServiceTracker<ISharingService, ISharingService> sharingServiceTracker;

    private BonjourInstaller bonjourInstaller = null;

    private PropertyChangeSupport serviceStateSupport = new PropertyChangeSupport(
            this);

    // We assume Bonjour is installed each time the plugin is activated,
    // so that we won't miss a manual installation of a higher version of Bonjour.
    private boolean bonjourInstalled = true;

    private IPropertyChangeListener prefListener = null;

    /**
     * The constructor
     */
    public LocalNetworkSharingUI() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        LocalNetworkSharingUI.plugin = this;
        this.prefListener = new IPropertyChangeListener() {

            boolean lastAvailable = isLNSServiceAvailable();

            public void propertyChange(PropertyChangeEvent event) {
                if (PREF_FEATURE_ENABLED.equals(event.getProperty())) {
                    boolean oldAvailable = lastAvailable;
                    boolean newAvailable = isLNSServiceAvailable();
                    this.lastAvailable = newAvailable;
                    if (oldAvailable != newAvailable) {
                        serviceStateSupport.firePropertyChange(
                                PREF_FEATURE_ENABLED, oldAvailable,
                                newAvailable);
                    }
                }
            }
        };
        getPreferenceStore().addPropertyChangeListener(prefListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
        if (prefListener != null) {
            getPreferenceStore().removePropertyChangeListener(prefListener);
            prefListener = null;
        }
        if (bonjourInstaller != null) {
            bonjourInstaller.dispose();
            bonjourInstaller = null;
        }
        if (sharingServiceTracker != null) {
            sharingServiceTracker.close();
            sharingServiceTracker = null;
        }
        LocalNetworkSharingUI.plugin = null;
        super.stop(context);
    }

    public synchronized ISharingService getSharingService() {
        if (sharingServiceTracker == null) {
            sharingServiceTracker = new ServiceTracker<ISharingService, ISharingService>(
                    getBundle().getBundleContext(), ISharingService.class, null);
            sharingServiceTracker.open();
        }
        return sharingServiceTracker.getService();
    }

    public synchronized BonjourInstaller getBonjourInstaller() {
        if (bonjourInstaller == null) {
            bonjourInstaller = new BonjourInstaller();
        }
        return bonjourInstaller;
    }

    public PropertyChangeSupport getServiceStatusSupport() {
        return serviceStateSupport;
    }

    public boolean isBonjourInstalled() {
        return bonjourInstalled;
    }

    public synchronized void setBonjourInstalled(boolean installed) {
        boolean oldAvailable = isLNSServiceAvailable();
        this.bonjourInstalled = installed;
        boolean newAvailable = isLNSServiceAvailable();
        if (oldAvailable != newAvailable) {
            serviceStateSupport.firePropertyChange(PREF_FEATURE_ENABLED,
                    oldAvailable, newAvailable);
        }
    }

    public boolean isLNSServiceAvailable() {
        return getPreferenceStore().getBoolean(PREF_FEATURE_ENABLED)
                && bonjourInstalled;
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static LocalNetworkSharingUI getDefault() {
        return plugin;
    }

    public static synchronized IDialogSettings getDialogSettingsSection(
            String sectionName) {
        IDialogSettings settings = getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(sectionName);
        if (section == null) {
            section = settings.addNewSection(sectionName);
        }
        return section;
    }

    public static void log(String message, Throwable e) {
        log(new Status(e == null ? IStatus.WARNING : IStatus.ERROR, PLUGIN_ID,
                message, e));
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

}
