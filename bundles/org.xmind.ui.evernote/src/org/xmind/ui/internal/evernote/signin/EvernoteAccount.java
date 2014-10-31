package org.xmind.ui.internal.evernote.signin;

import org.xmind.ui.evernote.signin.IEvernoteAccount;

/**
 * @author Jason Wong
 */
public class EvernoteAccount implements IEvernoteAccount {

    private String authToken;

    private String username;

    private String serviceType;

    public EvernoteAccount(String authToken, String username, String serviceType) {
        this.authToken = authToken;
        this.username = username;
        this.serviceType = serviceType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }

    public String getServiceType() {
        return serviceType;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("authToken = "); //$NON-NLS-1$
        buffer.append(authToken);
        buffer.append(", username = "); //$NON-NLS-1$
        buffer.append(username);
        buffer.append(", serviceType = "); //$NON-NLS-1$
        buffer.append(serviceType);
        return buffer.toString();
    }

}
