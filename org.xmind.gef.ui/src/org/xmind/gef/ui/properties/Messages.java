/**
 * 
 */
package org.xmind.gef.ui.properties;

import org.eclipse.osgi.util.NLS;

/**
 * @author frankshaka
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.gef.ui.properties.messages"; //$NON-NLS-1$
    public static String propertiesNotAvailable;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
