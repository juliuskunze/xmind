package org.xmind.ui.internal.evernote.signin;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xmind.ui.evernote.EvernotePlugin;
import org.xmind.ui.evernote.signin.IEvernoteAccount;

/**
 * @author Jason Wong
 */
public class EvernoteAccountStore {

    public static final String TOKEN = "token"; //$NON-NLS-1$

    public static final String USERNAME = "username"; //$NON-NLS-1$

    public static final String SERVICE_TYPE = "serviceType"; //$NON-NLS-1$

    public static final String EVERNOTE_SIGNEDIN = "signedin"; //$NON-NLS-1$

    private IPreferenceStore localStore;

    private EvernoteAccount accountInfo;

    public EvernoteAccountStore(IPreferenceStore prefStore) {
        this.localStore = prefStore;
        readFromStore();
    }

    private void readFromStore() {
        String authToken = localStore.getString(TOKEN);
        String username = localStore.getString(USERNAME);
        String serviceType = localStore.getString(SERVICE_TYPE);

        if (authToken != null && !"".equals(authToken) //$NON-NLS-1$
                && username != null && !"".equals(username) //$NON-NLS-1$ 
                && serviceType != null && !"".equals(serviceType)) {//$NON-NLS-1$ 
            this.accountInfo = new EvernoteAccount(authToken, username,
                    serviceType);
        }
    }

    public void signedIn(String authToken, String username, String serviceType) {
        this.accountInfo = new EvernoteAccount(authToken, username, serviceType);
        saveToStore(authToken, username, serviceType);
    }

    private void saveToStore(String authToken, String username,
            String serviceType) {
        localStore.setValue(TOKEN, authToken);
        localStore.setValue(USERNAME, username);
        localStore.setValue(SERVICE_TYPE, serviceType);
        localStore.setValue(EVERNOTE_SIGNEDIN, true);
        flushStore();
    }

    public void signedOut() {
        this.accountInfo = null;
        clearStore();
    }

    private void clearStore() {
        localStore.setValue(TOKEN, ""); //$NON-NLS-1$
        localStore.setValue(USERNAME, ""); //$NON-NLS-1$
        localStore.setValue(SERVICE_TYPE, ""); //$NON-NLS-1$
        localStore.setValue(EVERNOTE_SIGNEDIN, false);
        flushStore();
    }

    private void flushStore() {
        IEclipsePreferences node = InstanceScope.INSTANCE
                .getNode(EvernotePlugin.PLUGIN_ID);
        if (node != null) {
            try {
                node.flush();
            } catch (Throwable e) {
            }
        }
    }

    public IEvernoteAccount getAccountInfo() {
        return accountInfo;
    }
}
