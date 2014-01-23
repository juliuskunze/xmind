package org.xmind.core.internal.command.remote.lan;

import org.eclipse.osgi.util.NLS;

/**
 * @auther Jason Wong
 */

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.core.internal.command.remote.lan.messages"; //$NON-NLS-1$
    public static String DNSSDDiscoveryServiceAdapter_ActivateRemoteCommand;
    public static String DNSSDDiscoveryServiceAdapter_DeactivateRemoteCommand;
    public static String DNSSDDiscoveryServiceAdapter_OperationLock;
    public static String DNSSDDiscoveryServiceAdapter_RefreshRemoteCommand;
    public static String DNSSDDiscoveryServiceAdapter_RegisterLocalCommandServer;
    public static String DNSSDDiscoveryServiceAdapter_RegisterSocketFailed;
    public static String DNSSDDiscoveryServiceAdapter_RemoteCommandServiceDiscoverer;
    public static String DNSSDDiscoveryServiceAdapter_UnregisteredLocalCommandServer;
    public static String DNSSDRemoteCommandService_FailedResolveRemoteCommand;
    public static String DNSSDRemoteCommandService_ResolveRemoteCommandService;
    public static String DNSSDRemoteCommandService_SendCommand;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
