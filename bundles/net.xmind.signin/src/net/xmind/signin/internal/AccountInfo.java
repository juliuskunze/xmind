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
package net.xmind.signin.internal;

import net.xmind.signin.IAccountInfo;

@SuppressWarnings("deprecation")
public class AccountInfo implements IAccountInfo {

    private String user;

    private String authToken;

    private long expireDate;

    private int subscriptionStatus = RETRIEVING_SUBSCRIPTION_INFO;

    public AccountInfo(String user, String authToken, long expireDate) {
        this.user = user;
        this.authToken = authToken;
        this.expireDate = expireDate;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUser() {
        return user;
    }

    public long getExpireDate() {
        return expireDate;
    }

    @Deprecated
    public int getSubscriptionStatus() {
        return subscriptionStatus;
    }

    @Deprecated
    public boolean hasValidSubscription() {
        return subscriptionStatus == SUBSCRIPTION_VALID;
    }

    @Deprecated
    void setSubscriptionStatus(int status) {
        this.subscriptionStatus = status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("user="); //$NON-NLS-1$
        buffer.append(user);
        buffer.append(",authToken="); //$NON-NLS-1$
        buffer.append(authToken);
        buffer.append(",expireDate="); //$NON-NLS-1$
        buffer.append(expireDate);
        return buffer.toString();
    }

}
