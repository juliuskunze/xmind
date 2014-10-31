package org.xmind.ui.evernote.signin;

/**
 * @author Jason Wong
 */
public interface IEvernoteAccount {

    String getAuthToken();

    String getUsername();

    String getServiceType();

}
