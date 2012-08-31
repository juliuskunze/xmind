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

package net.xmind.workbench.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author Frank Shaka
 * 
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "net.xmind.workbench.internal.messages"; //$NON-NLS-1$

    public static String SiteEventNotificationService_View_text;
    public static String SiteEventNotificationService_More_text;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
