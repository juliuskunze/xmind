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
package org.xmind.cathy.internal.actions;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;

/**
 * @author briansun
 */
public class XMindNetAction extends Action implements IWorkbenchAction {

    public static final String BROWSER_ID = "net.xmind.webbrowseraction"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    private String url;

    /**
     * @param url
     */
    public XMindNetAction(String id, IWorkbenchWindow window, String url,
            String text, String toolTipText) {
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
        setId(id);
        setText(text);
        setToolTipText(toolTipText);
        this.url = url;
    }

    public IWorkbenchWindow getWindow() {
        return window;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getURL() {
        return url;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (window == null || getURL() == null)
            return;

        final IBrowser browser = BrowserSupport.getInstance().createBrowser(
                IBrowserSupport.AS_EDITOR, BROWSER_ID);
        if (browser == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                browser.openURL(getURL());
            }
        });
    }

    /**
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        window = null;
    }

}