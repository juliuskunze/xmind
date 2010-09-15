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
package net.xmind.workbench.internal.actions;

import net.xmind.signin.XMindNet;

/**
 * @author Frank Shaka
 * 
 */
public class XMindNetActionDelegate {

    private String url;

    public XMindNetActionDelegate() {
    }

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
    public XMindNetActionDelegate setURL(String url) {
        this.url = url;
        return this;
    }

    public XMindNetActionDelegate gotoURL() {
        gotoURL(getURL());
        return this;
    }

    /**
     * @param url
     */
    protected void gotoURL(final String url) {
        XMindNet.gotoURL(url);
    }

}
