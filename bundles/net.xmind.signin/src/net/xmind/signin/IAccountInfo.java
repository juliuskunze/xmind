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

@SuppressWarnings("deprecation")
public interface IAccountInfo extends ISubscriptionInfo {

    /**
     * Returns the identified name of this account.
     * 
     * @return the user name
     */
    String getUser();

    /**
     * Returns the authentication token retrieved from XMind.net.
     * 
     * @return the authentication token
     */
    String getAuthToken();

    /**
     * Returns the date in milliseconds (since the Unix Epoch) when the
     * auth-token is going to expire.
     * 
     * @return the expire date in milliseconds
     */
    long getExpireDate();
}
