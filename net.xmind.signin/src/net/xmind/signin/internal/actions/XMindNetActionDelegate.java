/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package net.xmind.signin.internal.actions;

import java.net.URI;
import java.net.URLEncoder;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;

/**
 * @author Frank Shaka
 * 
 */
public class XMindNetActionDelegate {

    protected static final String BROWSER_ID = "net.xmind.browser.commmon"; //$NON-NLS-1$

    private String url;

    /**
     * @return the url
     */
    public String getURL() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setURL(String url) {
        this.url = url;
    }

    public void gotoURL() {
        gotoURL(getURL());
    }

    /**
     * @param url2
     */
    protected void gotoURL(final String url) {
        final IBrowser browser = BrowserSupport.getInstance().createBrowser(
                IBrowserSupport.AS_EDITOR, BROWSER_ID);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                browser.openURL(makeURL(url));
            }
        });
    }

    private static final String makeURL(String url) {
        if (!url.startsWith("file:")) {
            try {
                return "http://www.xmind.net/xmind/go?r=" //$NON-NLS-1$
                        + URLEncoder.encode(new URI(url).toString(), "UTF-8"); //$NON-NLS-1$
            } catch (Exception ignore) {
            }
        }
        return url;
    }

}
