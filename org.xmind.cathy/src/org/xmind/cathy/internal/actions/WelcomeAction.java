package org.xmind.cathy.internal.actions;

import java.util.Properties;

import net.xmind.signin.XMindNetEntry;

import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.cathy.internal.WorkbenchMessages;

/**
 * 
 * @author Frank Shaka
 * @deprecated Welcome action should be available without Cathy running, so is
 *             now moved to an action set in net.xmind.signin.
 */
public class WelcomeAction extends XMindNetAction {

    private static final String DEFAULT_URL = "http://www.xmind.net/xmind/welcome/"; //$NON-NLS-1$

    public WelcomeAction(IWorkbenchWindow window) {
        super("net.xmind.ui.welcome", window, DEFAULT_URL, //$NON-NLS-1$
                WorkbenchMessages.Welcome_text,
                WorkbenchMessages.Welcome_toolTip);
    }

    public void run() {
        if (XMindNetEntry.hasSignedIn()) {
            Properties userInfo = XMindNetEntry.getCurrentUserInfo();
            setURL(String.format("http://www.xmind.net/xmind/welcome/%s/%s", //$NON-NLS-1$
                    userInfo.getProperty(XMindNetEntry.USER_ID), userInfo
                            .getProperty(XMindNetEntry.TOKEN)));
        } else {
            setURL(DEFAULT_URL);
        }
        super.run();
    }

}
