package org.xmind.ui.internal.evernote.signin;

import org.xmind.ui.evernote.EvernotePlugin;

/**
 * @author Jason Wong
 */
public class InternalEvernote {

    private static InternalEvernote instance = new InternalEvernote();

    private EvernoteAccountStore accountStore = null;

    private EvernoteAuthenticator authenticator = null;

    private InternalEvernote() {
    }

    public static InternalEvernote getInstance() {
        return instance;
    }

    public EvernoteAccountStore getAccountStore() {
        if (accountStore == null)
            accountStore = new EvernoteAccountStore(EvernotePlugin.getDefault()
                    .getPreferenceStore());
        return accountStore;
    }

    public EvernoteAuthenticator getAuthenticator() {
        if (authenticator == null)
            authenticator = new EvernoteAuthenticator();
        return authenticator;
    }

}
