package org.xmind.ui.evernote.signin;

import org.xmind.ui.internal.evernote.signin.InternalEvernote;

/**
 * @author Jason Wong
 */
public class Evernote {

    private Evernote() {
    }

    public static IEvernoteAccount signIn() {
        return InternalEvernote.getInstance().getAuthenticator().signIn();
    }

    public static IEvernoteAccount getAccountInfo() {
        return InternalEvernote.getInstance().getAccountStore()
                .getAccountInfo();
    }

    public static void signOut() {
        InternalEvernote.getInstance().getAuthenticator().signOut();
    }

}
