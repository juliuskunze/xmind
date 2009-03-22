package net.xmind.signin.internal.actions;

import java.util.Properties;

import net.xmind.signin.XMindNetEntry;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class WelcomeActionDelegate extends XMindNetActionDelegate implements
        IWorkbenchWindowActionDelegate {

    private static final String DEFAULT_URL = "http://www.xmind.net/xmind/welcome/"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        this.window = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.
     * IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (window == null)
            return;

        if (XMindNetEntry.hasSignedIn()) {
            Properties userInfo = XMindNetEntry.getCurrentUserInfo();
            setURL(String.format("http://www.xmind.net/xmind/welcome/%s/%s", //$NON-NLS-1$
                    userInfo.getProperty(XMindNetEntry.USER_ID), userInfo
                            .getProperty(XMindNetEntry.TOKEN)));
        } else {
            setURL(DEFAULT_URL);
        }
        gotoURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

}
