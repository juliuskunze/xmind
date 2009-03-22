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
package org.xmind.cathy.internal.actions;

import java.util.Properties;

import net.xmind.signin.ISignInListener;
import net.xmind.signin.XMindNetEntry;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;

/**
 * 
 * @author Frank Shaka
 * @deprecated SignIn action should be available without Cathy running, so is
 *             now moved to an action set in net.xmind.signin.
 */
public class SignInAction extends Action implements IWorkbenchAction,
        ISignInListener {

    private IWorkbenchWindow window;

    public SignInAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setId("net.xmind.ui.signIn"); //$NON-NLS-1$
        XMindNetEntry.addSignInListener(this);
        update();
    }

    private void update() {
        boolean signedIn = XMindNetEntry.hasSignedIn();
        setText(signedIn ? WorkbenchMessages.SignOut_text
                : WorkbenchMessages.SignIn_text);
    }

    public void run() {
        if (window == null)
            return;

        boolean signedIn = XMindNetEntry.hasSignedIn();
        if (signedIn) {
            XMindNetEntry.signOut();
            showWelcome();
        } else {
            XMindNetEntry.signIn();
            showAccount();
        }
    }

    private void showWelcome() {
        final IBrowser browser = BrowserSupport.getInstance().createBrowser(
                IBrowserSupport.AS_EDITOR, XMindNetAction.BROWSER_ID);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                browser.openURL("http://www.xmind.net/xmind/welcome/"); //$NON-NLS-1$
            }
        });
    }

    private void showAccount() {
        if (!XMindNetEntry.hasSignedIn())
            return;

        final IBrowser browser = BrowserSupport.getInstance().createBrowser(
                IBrowserSupport.AS_EDITOR, XMindNetAction.BROWSER_ID);
        Properties userInfo = XMindNetEntry.getCurrentUserInfo();
        String userID = userInfo.getProperty(XMindNetEntry.USER_ID);
        String token = userInfo.getProperty(XMindNetEntry.TOKEN);
        final String url = String.format(
                "http://www.xmind.net/xmind/account/%s/%s", //$NON-NLS-1$ 
                userID, token);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                browser.openURL(url);
            }
        });
    }

    public void dispose() {
        this.window = null;
        XMindNetEntry.removeSignInListener(this);
    }

    public void postSignIn(Properties userInfo) {
        update();
    }

    public void postSignOut() {
        update();
    }

}