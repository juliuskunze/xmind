/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

import java.net.URI;
import java.net.URLEncoder;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.XMindNet;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;

public class XMindNetNavigator {

    private static final String BROWSER_ID = "net.xmind.browser.commmon"; //$NON-NLS-1$

    public void gotoURL(final String url) {
        if (url.startsWith("xmind:")) //$NON-NLS-1$
            return;
        final IBrowser browser = BrowserSupport.getInstance().createBrowser(
                IBrowserSupport.AS_EDITOR, BROWSER_ID);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                browser.openURL(makeURL(url));
            }
        });
    }

    public String makeURL(String url) {
        if (!url.startsWith("file:")) { //$NON-NLS-1$
            try {
                StringBuffer buffer = new StringBuffer(100);
                buffer.append("http://www.xmind.net/xmind/go?r="); //$NON-NLS-1$
                buffer.append(URLEncoder.encode(new URI(url).toString(),
                        "UTF-8")); //$NON-NLS-1$
                buffer.append("&u="); //$NON-NLS-1$
                IAccountInfo info = XMindNet.getAccountInfo();
                if (info != null) {
                    buffer.append(info.getUser());
                }
                buffer.append("&t="); //$NON-NLS-1$
                if (info != null) {
                    buffer.append(info.getAuthToken());
                    buffer.append("&exp="); //$NON-NLS-1$
                    buffer.append(info.getExpireDate());
                }
                String distributionId = System
                        .getProperty("org.xmind.product.distribution.id"); //$NON-NLS-1$
                if (distributionId != null) {
                    buffer.append("&distrib="); //$NON-NLS-1$
                    buffer.append(distributionId);
                }
                return buffer.toString();
            } catch (Exception ignore) {
            }
        }
        return url;
    }

}
