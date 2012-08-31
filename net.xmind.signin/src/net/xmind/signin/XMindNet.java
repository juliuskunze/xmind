/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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

import net.xmind.signin.internal.InternalXMindNet;

import org.xmind.ui.internal.browser.BrowserUtil;

public class XMindNet {

    public static IAccountInfo signIn() {
        return InternalXMindNet.getInstance().getAuthenticator()
                .signIn(null, null);
    }

    public static IAccountInfo signIn(String message,
            ISignInDialogExtension extension) {
        return InternalXMindNet.getInstance().getAuthenticator()
                .signIn(message, extension);
    }

    public static void signIn(IAuthenticationListener callback, boolean block) {
        InternalXMindNet.getInstance().getAuthenticator()
                .signIn(callback, block, null, null);
    }

    public static void signIn(IAuthenticationListener callback, boolean block,
            String message, ISignInDialogExtension extension) {
        InternalXMindNet.getInstance().getAuthenticator()
                .signIn(callback, block, message, extension);
    }

    public static void signOut() {
        InternalXMindNet.getInstance().getAuthenticator().signOut();
    }

    public static String makeURL(String sourceURL) {
        return BrowserUtil.makeRedirectURL(sourceURL);
    }

    public static void gotoURL(String url, Object... values) {
        InternalXMindNet.getInstance().getNavigator()
                .gotoURL(false, url, values);
    }

    public static void gotoURL(boolean external, String urlPattern,
            Object... values) {
        InternalXMindNet.getInstance().getNavigator()
                .gotoURL(external, urlPattern, values);
    }

    public static void addAuthenticationListener(
            IAuthenticationListener listener) {
        InternalXMindNet.getInstance().getAccount()
                .addAuthenticationListener(listener);
    }

    public static void removeAuthenticationListener(
            IAuthenticationListener listener) {
        InternalXMindNet.getInstance().getAccount()
                .removeAuthenticationListener(listener);
    }

    @Deprecated
    public static void addAuthorizationListener(IAuthorizationListener listener) {
        InternalXMindNet.getInstance().getAccount()
                .addAuthorizationListener(listener);
    }

    @Deprecated
    public static void removeAuthorizationListener(
            IAuthorizationListener listener) {
        InternalXMindNet.getInstance().getAccount()
                .removeAuthorizationListener(listener);
    }

    @Deprecated
    public static void addPreauthorizationListener(
            IPreauthorizationListener listener) {
        InternalXMindNet.getInstance().getAccount()
                .addPreauthorizationListener(listener);
    }

    @Deprecated
    public static void removePreauthorizationListener(
            IPreauthorizationListener listener) {
        InternalXMindNet.getInstance().getAccount()
                .removePreauthorizationListener(listener);
    }

    public static void addXMindNetCommandHandler(String commandName,
            IXMindNetCommandHandler handler) {
        InternalXMindNet.getInstance().getCommandSupport()
                .addXMindNetCommandHandler(commandName, handler);
    }

    public static void removeXMindNetCommandHandler(String commandName,
            IXMindNetCommandHandler handler) {
        InternalXMindNet.getInstance().getCommandSupport()
                .removeXMindNetCommandHandler(commandName, handler);
    }

    /**
     * Returns the account info of the current authenticated XMind.net user.
     * Returns <code>null</code> if the user has not signed in or has already
     * signed out.
     * 
     * @return the account info of the current authenticated XMind.net user, or
     *         <code>null</code> if the user has not signed in or has already
     *         signed out
     */
    public static IAccountInfo getAccountInfo() {
        return InternalXMindNet.getInstance().getAccount().getAccountInfo();
    }

    public static ILicenseInfo getLicenseInfo() {
        return InternalXMindNet.getInstance().getLicenseAgent()
                .getLicenseInfo();
    }

    public static void addLicenseListener(ILicenseListener listener) {
        InternalXMindNet.getInstance().getLicenseAgent()
                .addLicenseListener(listener);
    }

    public static void removeLicenseListener(ILicenseListener listener) {
        InternalXMindNet.getInstance().getLicenseAgent()
                .removeLicenseListener(listener);
    }

}
