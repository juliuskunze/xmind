package org.xmind.ui.evernote;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Jason Wong
 */
public class EvernotePlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.xmind.ui.evernote"; //$NON-NLS-1$

    public static final String DEBUG_OPTION = "/debug"; //$NON-NLS-1$

    private static EvernotePlugin plugin;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    public EvernotePlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (debugTracker != null) {
            debugTracker.close();
            debugTracker = null;
        }
        plugin = null;
        super.stop(context);
    }

    public static EvernotePlugin getDefault() {
        return plugin;
    }

    public static IDialogSettings getDialogSettings(String sectionName) {
        IDialogSettings ds = getDefault().getDialogSettings();
        if (sectionName == null)
            return ds;

        IDialogSettings section = ds.getSection(sectionName);
        if (section == null) {
            section = ds.addNewSection(sectionName);
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

    private DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    getDefault().getBundle().getBundleContext(),
                    DebugOptions.class.getName(), null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public boolean isDebugging(String option) {
        DebugOptions debugOptions = getDefault().getDebugOptions();
        return debugOptions != null
                && debugOptions.getBooleanOption(PLUGIN_ID + option, false);
    }

}
