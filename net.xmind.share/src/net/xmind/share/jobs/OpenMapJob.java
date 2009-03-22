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
package net.xmind.share.jobs;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowserSupport;

public class OpenMapJob implements Runnable {

    private Thread thread;

    private String url;

    public OpenMapJob(String url) {
        this.url = url;
        thread = new Thread(this);
        thread.setName("Open Map: " + url); //$NON-NLS-1$
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void run() {
        HttpClient client = new HttpClient();

        int result;
        do {

            try {
                Thread.sleep(50);
            } catch (InterruptedException e1) {
            }

            GetMethod method = new GetMethod(url);
            try {
                result = client.executeMethod(method);
            } catch (Exception e) {
                result = -1;
            }

            if (result == 404)
                // some error occurred
                return;

        } while (result != HttpStatus.SC_OK);

        openURL();
    }

    private void openURL() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                try {
                    BrowserSupport.getInstance().createBrowser(
                            IBrowserSupport.AS_EDITOR).openURL(url);
                } catch (Throwable ignore) {
                    Program.launch(url);
                }
            }
        });
    }

}