package net.xmind.workbench.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class XMindNetWorkbench extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "net.xmind.workbench"; //$NON-NLS-1$

    // XMind.net URLs
    /**
     * Feedback page.
     */
    public static final String URL_FEEDBACK = "http://www.xmind.net/xmind/feedback/"; //$NON-NLS-1$

    /**
     * Online help page.
     */
    public static final String URL_HELP = "http://www.xmind.net/xmind/help/"; //$NON-NLS-1$

    /**
     * Invite others to try XMind.
     */
    public static final String URL_INVITE = "http://www.xmind.net/xmind/invite/"; //$NON-NLS-1$

    /**
     * Subscribe to XMind newsletter.
     */
    public static final String URL_SUBSCRIBE_NEWSLETTER = "https://www.xmind.net/xmind/newsletter/subscribe/"; //$NON-NLS-1$

    /**
     * Sign out XMind.net account.
     */
    public static final String URL_SIGNOUT = "https://www.xmind.net/xmind/signout2/"; //$NON-NLS-1$

    /**
     * Purchase XMind Pro/Plus/Subscription.
     */
    public static final String URL_PURCHASE = "https://www.xmind.net/xmind/buy/"; //$NON-NLS-1$

    /**
     * Common welcome page for anonymous users.
     */
    public static final String URL_WELCOME = "http://www.xmind.net/xmind/welcome/"; //$NON-NLS-1$

    /**
     * Welcome page for authenticated users.
     * 
     * <pre>
     * http://www.xmind.net/xmind/welcome/USER_NAME/TOKEN/
     * </pre>
     */
    public static final String URL_WELCOME_USER = "http://www.xmind.net/xmind/welcome/%s/%s"; //$NON-NLS-1$

    /**
     * The home page for an authenticated user.
     * 
     * <pre>
     * http://www.xmind.net/xmind/account/USER_NAME/TOKEN/
     * </pre>
     */
    public static final String URL_ACCOUNT = "https://www.xmind.net/xmind/account/%s/%s/"; //$NON-NLS-1$

    // The shared instance
    private static XMindNetWorkbench plugin;

    /**
     * The constructor
     */
    public XMindNetWorkbench() {
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
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static XMindNetWorkbench getDefault() {
        return plugin;
    }

    public static void log(Throwable e, String message) {
        getDefault().getLog().log(
                new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    public IDialogSettings getDialogSettings(String sectionName) {
        IDialogSettings settings = getDialogSettings();
        IDialogSettings section = settings.getSection(sectionName);
        if (section == null) {
            section = settings.addNewSection(sectionName);
        }
        return section;
    }
}
