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

@Deprecated
public interface ISubscriptionInfo {

    /**
     * Status for retrieving subscription info
     */
    @Deprecated
    int RETRIEVING_SUBSCRIPTION_INFO = 0;

    /**
     * Status for not having subscribed
     */
    @Deprecated
    int UNSUBSCRIBED = 1;

    /**
     * Status for having subscribed and not having expired
     */
    @Deprecated
    int SUBSCRIPTION_VALID = 2;

    /**
     * Status for having subscribed but already expired
     */
    @Deprecated
    int SUBSCRIPTION_EXPIRED = 3;

    /**
     * Status for unavailable to retrieve subscription info
     */
    @Deprecated
    int UNKNOWN = 4;

    /**
     * Returns the subscription status of this account.
     * 
     * @return the subscription status
     */
    @Deprecated
    int getSubscriptionStatus();

    /**
     * Returns whether this account has valid subscription.
     * 
     * @return <code>true</code> if this account has valid subscription
     */
    @Deprecated
    boolean hasValidSubscription();

}
