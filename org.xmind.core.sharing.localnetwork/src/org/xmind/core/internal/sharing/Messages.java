package org.xmind.core.internal.sharing;

import org.eclipse.osgi.util.NLS;

/**
 * @auther Jason Wong
 */

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.core.internal.sharing.messages"; //$NON-NLS-1$
    public static String LocalNetworkSharingService_BroadcastShareEvent;
    public static String LocalNetworkSharingService_ConnectionLocalNetworkSharing;
    public static String LocalNetworkSharingService_ContractRemoteLibrary;
    public static String LocalNetworkSharingService_DisconnectionLocalNetworkSharing;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
