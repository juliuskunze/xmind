/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

public interface ISubscriptionInfo {

    /**
     * Status for retrieving subscription info
     */
    int RETRIEVING_SUBSCRIPTION_INFO = 0;

    /**
     * Status for not having subscribed
     */
    int UNSUBSCRIBED = 1;

    /**
     * Status for having subscribed and not having expired
     */
    int SUBSCRIPTION_VALID = 2;

    /**
     * Status for having subscribed but already expired
     */
    int SUBSCRIPTION_EXPIRED = 3;

    /**
     * Status for unavailable to retrieve subscription info
     */
    int UNKNOWN = 4;

    /**
     * Returns the subscription status of this account.
     * 
     * @return the subscription status
     */
    int getSubscriptionStatus();

    /**
     * Returns whether this account has valid subscription.
     * 
     * @return <code>true</code> if this account has valid subscription
     */
    boolean hasValidSubscription();

}
