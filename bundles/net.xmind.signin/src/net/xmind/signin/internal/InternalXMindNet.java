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


public class InternalXMindNet {

    private static InternalXMindNet instance = new InternalXMindNet();

    private XMindNetAccount account = null;

    private XMindNetAuthenticator authenticator = null;

    private XMindNetNavigator navigator = null;

    private XMindNetCommandSupport commandSupport = null;

    private XMindLicenseAgent licenseAgent = null;

    private InternalXMindNet() {
    }

    public XMindNetAccount getAccount() {
        if (account == null)
            account = new XMindNetAccount(Activator.getDefault()
                    .getPreferenceStore());
        return account;
    }

    public XMindNetAuthenticator getAuthenticator() {
        if (authenticator == null)
            authenticator = new XMindNetAuthenticator();
        return authenticator;
    }

    public XMindNetNavigator getNavigator() {
        if (navigator == null)
            navigator = new XMindNetNavigator();
        return navigator;
    }

    public XMindNetCommandSupport getCommandSupport() {
        if (commandSupport == null)
            commandSupport = new XMindNetCommandSupport();
        return commandSupport;
    }

    public XMindLicenseAgent getLicenseAgent() {
        if (licenseAgent == null) {
            licenseAgent = new XMindLicenseAgent();
        }
        return licenseAgent;
    }

    public static InternalXMindNet getInstance() {
        return instance;
    }

}
