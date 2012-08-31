package net.xmind.workbench.internal.actions;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.XMindNet;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class WelcomeActionDelegate implements IWorkbenchWindowActionDelegate {

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

        IAccountInfo accountInfo = XMindNet.getAccountInfo();
        if (accountInfo != null) {
            XMindNet.gotoURL("http://www.xmind.net/xmind/welcome/%s/%s", //$NON-NLS-1$
                    accountInfo.getUser(), accountInfo.getAuthToken());
        } else {
            XMindNet.gotoURL(DEFAULT_URL);
        }
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
