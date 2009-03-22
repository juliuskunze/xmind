/*
 * Copyright (c) 2006-2008 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.signin.internal;

import java.util.Properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.internal.browser.InternalBrowser;
import org.xmind.ui.internal.browser.InternalBrowserEditor;
import org.xmind.ui.internal.browser.InternalBrowserView;

/**
 * @author briansun
 */
@SuppressWarnings("restriction")
public class SignInDialog extends Dialog implements StatusTextListener,
        OpenWindowListener {

    private static final String SIGN_IN_URL = "http://www.xmind.net/xmind/signin/"; //$NON-NLS-1$

    private static final String USER = "user"; //$NON-NLS-1$

    private static final String TOKEN = "token"; //$NON-NLS-1$

    private static final String REMEMBER = "remember"; //$NON-NLS-1$

    private Browser browser;

    private String userID = null;

    private String token = null;

    private boolean shouldRemember = false;

    /**
     * @param parentWindow
     */
    public SignInDialog(Shell parent) {
        super(parent);
        setBlockOnOpen(true);
        setShellStyle(SWT.TITLE | SWT.CLOSE | SWT.RESIZE
                | SWT.APPLICATION_MODAL);
    }

    public String getUserID() {
        return userID;
    }

    public String getToken() {
        return token;
    }

    public boolean shouldRemember() {
        return shouldRemember;
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.SignInDialog_title);
    }

    protected Point getInitialLocation(Point initialSize) {
        Rectangle area = Display.getCurrent().getClientArea();
        return new Point(area.x + (area.width - initialSize.x) / 2, area.y
                + (area.height - initialSize.y) / 2);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        try {
            browser = new Browser(parent, SWT.MOZILLA);
        } catch (SWTError e) {
            browser = new Browser(parent, SWT.NONE);
        }
        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        browser.addStatusTextListener(this);
        browser.addOpenWindowListener(this);

        browser.setUrl(SIGN_IN_URL);

        if ("carbon".equals(SWT.getPlatform())) //$NON-NLS-1$
            browser.refresh();
        return browser;
    }

    protected Browser getBrowser() {
        return browser;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
     */
    @Override
    protected Point getInitialSize() {
        return new Point(540, 365);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // use html buttons
    }

    /**
     * @see org.eclipse.swt.browser.StatusTextListener#changed(org.eclipse.swt.browser.StatusTextEvent)
     */
    public void changed(StatusTextEvent event) {
        checkCommand(event.text);
    }

    private boolean checkCommand(String text) {
        String[] commandLines = text.split(";"); //$NON-NLS-1$
        Properties commands = new Properties();
        for (String commandLine : commandLines) {
            int index = commandLine.indexOf('=');
            if (index >= 0) {
                commands.setProperty(commandLine.substring(0, index),
                        commandLine.substring(index + 1));
            }
        }
        String code = commands.getProperty("xmind_status"); //$NON-NLS-1$
        if ("200".equals(code)) { //$NON-NLS-1$
            String json = commands.getProperty("xmind_json"); //$NON-NLS-1$
            if (json != null) {
                return executeJSON(json);
            }
        }
        return false;
    }

    /**
     * @param json2
     * @return
     */
    private boolean executeJSON(String json) {
        try {
            return setUserNameAndToken(json);
        } catch (JSONException e) {
        }
        return false;
    }

    private boolean setUserNameAndToken(String jsonSource) throws JSONException {
        JSONObject json = new JSONObject(jsonSource);
        String userID = json.getString(USER);
        String token = json.getString(TOKEN);
        if (userID == null || token == null || "".equals(userID) //$NON-NLS-1$
                || "".equals(token)) //$NON-NLS-1$
            return false;

        this.userID = userID;
        this.token = token;
        this.shouldRemember = json.getBoolean(REMEMBER);
        setReturnCode(OK);
        return close();
    }

    public void open(WindowEvent event) {
        IBrowser browser = BrowserSupport.getInstance().createBrowser(
                IBrowserSupport.AS_EDITOR);
        try {
            browser.openURL(""); //$NON-NLS-1$
            if (browser instanceof InternalBrowser) {
                IWorkbenchPart part = ((InternalBrowser) browser).getPart();
                if (part instanceof InternalBrowserEditor) {
                    event.browser = ((InternalBrowserEditor) part).getViewer()
                            .getBrowser();
                } else if (part instanceof InternalBrowserView) {
                    event.browser = ((InternalBrowserView) part).getViewer()
                            .getBrowser();
                }
            }
        } catch (PartInitException e) {
            Activator.log(e);
        }

        event.display.asyncExec(new Runnable() {
            public void run() {
                setReturnCode(CANCEL);
                close();
            }
        });
    }

}