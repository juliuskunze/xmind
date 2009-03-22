package org.xmind.cathy.internal.actions;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchWindow;
import org.osgi.framework.Bundle;
import org.xmind.cathy.internal.WorkbenchMessages;

/**
 * 
 * @author Frank Shaka
 * @deprecated Help action should be available without Cathy running, so is now
 *             moved to an action set in net.xmind.signin.
 */
public class HelpAction extends XMindNetAction {

    private static final String ONLINE_HELP_URL = "http://www.xmind.net/"; //$NON-NLS-1$

    public HelpAction(IWorkbenchWindow window) {
        super("org.xmind.ui.help", window, ONLINE_HELP_URL, //$NON-NLS-1$
                WorkbenchMessages.Help_text, WorkbenchMessages.Help_toolTip);
    }

    public void run() {
        setURL(findHelpURL());
        super.run();
    }

    private String findHelpURL() {
        Bundle helpBundle = Platform.getBundle("org.xmind.ui.help"); //$NON-NLS-1$
        if (helpBundle != null) {
            URL url = FileLocator.find(helpBundle, new Path(
                    "$nl$/contents/index.html"), null); //$NON-NLS-1$
            if (url != null) {
                try {
                    url = FileLocator.toFileURL(url);
                    return url.toExternalForm();
                } catch (IOException e) {
                }
            }
        }
        return ONLINE_HELP_URL;
    }

}
