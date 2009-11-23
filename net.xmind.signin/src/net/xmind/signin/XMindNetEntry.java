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
package net.xmind.signin;

import java.util.Properties;

import net.xmind.signin.internal.UserInfoManager;

public class XMindNetEntry {

    /**
     * Key for retrieving the XMind ID from the user information.
     * (value="USER_ID")
     */
    public static final String USER_ID = UserInfoManager.USER_ID;

    /**
     * Key for retrieving the current token from the user information.
     * (value="TOKEN")
     */
    public static final String TOKEN = UserInfoManager.TOKEN;

    /**
     * Let user sign in to XMind.net and returns the user information.
     * <p>
     * The current thread will be blocked until the sign-in process is over.
     * </p>
     * <p>
     * If sign-in succeeded, at least USER_ID and TOKEN will be returned.
     * Clients may retrieve them by calling
     * <code>Properties.getProperty(String)</code>.
     * </p>
     * <p>
     * <b>NOTE:</b> This method is OK to be called outside the UI thread.
     * </p>
     * 
     * @return the user information, or <code>null</code> if sign-in failed or
     *         canceled.
     */
    public static Properties signIn() {
        return UserInfoManager.getDefault().signIn();
    }

    public static Properties signIn(String message) {
        return UserInfoManager.getDefault().signIn(message);
    }

    public static void signIn(ISignInListener callback, boolean block) {
        UserInfoManager.getDefault().signIn(callback, block);
    }

    public static void signIn(ISignInListener callback, boolean block,
            String message) {
        UserInfoManager.getDefault().signIn(callback, block, message);
    }

    public static void signOut() {
        UserInfoManager.getDefault().signOut();
    }

    public static Properties getCurrentUserInfo() {
        return UserInfoManager.getDefault().getUserInfo();
    }

    public static boolean hasSignedIn() {
        return UserInfoManager.getDefault().hasSignedIn();
    }

    public static void addSignInListener(ISignInListener listener) {
        UserInfoManager.getDefault().addSignInListener(listener);
    }

    public static void removeSignInListener(ISignInListener listener) {
        UserInfoManager.getDefault().removeSignInListener(listener);
    }

}