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

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.cathy.internal.WorkbenchMessages;

/**
 * 
 * @author Frank Shaka
 * @deprecated ShowAccount action should be available without Cathy running, so
 *             is now moved to an action set in net.xmind.signin.
 */
public class ShowAccountAction extends XMindNetAction implements
        ISignInListener {

    public ShowAccountAction(IWorkbenchWindow window) {
        super("net.xmind.ui.showAccount", window, null, //$NON-NLS-1$
                WorkbenchMessages.ShowAccount_text, null);
        XMindNetEntry.addSignInListener(this);
        update();
    }

    private void update() {
        String userID = getUserID();
        if (userID == null) {
            setText(WorkbenchMessages.ShowAccount_text);
            setToolTipText(WorkbenchMessages.ShowAccount_toolTip);
        } else {
            setText(NLS.bind(WorkbenchMessages.ShowAccount_pattern, userID));
            setToolTipText(NLS.bind(
                    WorkbenchMessages.ShowAccount_toolTip_pattern, userID));
        }
        setEnabled(XMindNetEntry.hasSignedIn());
    }

    private String getUserID() {
        Properties userInfo = XMindNetEntry.getCurrentUserInfo();
        return userInfo == null ? null : userInfo
                .getProperty(XMindNetEntry.USER_ID);
    }

    private String getToken() {
        Properties userInfo = XMindNetEntry.getCurrentUserInfo();
        return userInfo == null ? null : userInfo
                .getProperty(XMindNetEntry.TOKEN);
    }

    public void dispose() {
        XMindNetEntry.removeSignInListener(this);
        super.dispose();
    }

    public void run() {
        if (getWindow() == null || !XMindNetEntry.hasSignedIn())
            return;

        setURL(String.format("http://www.xmind.net/xmind/account/%s/%s/", //$NON-NLS-1$ 
                getUserID(), getToken()));
        super.run();
    }

    public void postSignIn(Properties userInfo) {
        update();
    }

    public void postSignOut() {
        update();
    }

}