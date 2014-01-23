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

import org.eclipse.core.runtime.IStatus;

@Deprecated
public interface IAuthorizationListener {

    @Deprecated
    int UNAUTHENTICATED = 1;

    @Deprecated
    int ERROR_OCCURRED = 2;

    /**
     * Called when user retrieved authorization from XMind.net. This method is
     * always called after
     * {@link IAuthenticationListener#postSignIn(IAccountInfo)} is called.
     * 
     * @param accountInfo
     *            the account info
     */
    @Deprecated
    void authorized(IAccountInfo accountInfo);

    /**
     * Called when authorization failed. Use <code>result.getCode()</code> to
     * get specific reason of this failure.
     * <p>
     * Possible reasons:<br>
     * <ul>
     * <li>UNAUTHENTICATED: user signed out or has not signed in</li>
     * <li>ERROR_OCCURRED: network or client error occurred when retrieving
     * authorization info</li>
     * </ul>
     * </p>
     * 
     * @param result
     *            the authorization result
     * @param accountInfo
     *            the account info of the current user, or <code>null</code> if
     *            no user has signed in or user has signed out
     * @see #UNAUTHENTICATED
     * @see #ERROR_OCCURRED
     */
    @Deprecated
    void unauthorized(IStatus result, IAccountInfo accountInfo);

}
