/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package net.xmind.signin.internal.actions;

import java.util.Properties;

import net.xmind.signin.ISignInListener;
import net.xmind.signin.XMindNetEntry;
import net.xmind.signin.internal.Messages;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class SignInActionDelegate extends XMindNetActionDelegate implements
        IWorkbenchWindowActionDelegate, IActionDelegate2, ISignInListener {

    private IWorkbenchWindow window;

    private IAction action;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        window = null;
        action = null;
        XMindNetEntry.removeSignInListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.
     * IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
        XMindNetEntry.addSignInListener(this);
        update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action) {
        this.action = action;
        update();
    }

    private void update() {
        if (action == null)
            return;

        boolean signedIn = XMindNetEntry.hasSignedIn();
        action.setText(signedIn ? Messages.SignOut_text : Messages.SignIn_text);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action
     * .IAction, org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (window == null)
            return;

        boolean signedIn = XMindNetEntry.hasSignedIn();
        if (signedIn) {
            XMindNetEntry.signOut();
            signOutInBrowser();
        } else {
            final Display display = Display.getCurrent();
            XMindNetEntry.signIn(new ISignInListener() {
                public void postSignOut() {
                }

                public void postSignIn(final Properties userInfo) {
                    if (userInfo != null)
                        display.asyncExec(new Runnable() {
                            public void run() {
                                showAccount(userInfo);
                            }
                        });
                }
            }, false);
        }
    }

    private void signOutInBrowser() {
        setURL("http://www.xmind.net/xmind/go?r=http%3A%2F%2Fwww.xmind.net%2Fxmind%2Fsignout2%2F"); //$NON-NLS-1$
        gotoURL();
    }

    private void showAccount(Properties userInfo) {
        String userID = userInfo.getProperty(XMindNetEntry.USER_ID);
        String token = userInfo.getProperty(XMindNetEntry.TOKEN);
        final String url = String.format(
                "http://www.xmind.net/xmind/account/%s/%s", //$NON-NLS-1$ 
                userID, token);
        setURL(url);
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xmind.signin.ISignInListener#postSignIn(java.util.Properties)
     */
    public void postSignIn(Properties userInfo) {
        update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.xmind.signin.ISignInListener#postSignOut()
     */
    public void postSignOut() {
        update();
    }

}
