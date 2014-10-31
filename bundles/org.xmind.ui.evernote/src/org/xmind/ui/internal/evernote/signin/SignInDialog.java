package org.xmind.ui.internal.evernote.signin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.EvernoteApi;
import org.scribe.exceptions.OAuthConnectionException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.xmind.ui.evernote.EvernotePlugin;
import org.xmind.ui.internal.evernote.ErrorStatusDialog;
import org.xmind.ui.internal.evernote.EvernoteMessages;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;

/**
 * @author Jason Wong
 */
public class SignInDialog extends Dialog implements IJobChangeListener {

    private boolean DEBUGGING = EvernotePlugin.getDefault().isDebugging(
            EvernotePlugin.DEBUG_OPTION);

    private class OAuthJob extends Job implements LocationListener {

        private static final String OAUTH_VERIFIER_NAME = "oauth_verifier="; //$NON-NLS-1$

        private static final int OAUTH_VERIFIER_LENGTH = 32;

        private OAuthService oauthService;

        private EvernoteService evernoteService;

        private Token requestToken;

        public OAuthJob(SignInDialog dialog, EvernoteService evernoteService) {
            super(EvernoteMessages.EvernoteOAuthJob_title);
            this.evernoteService = evernoteService;
            if (DEBUGGING) {
                System.out.println("EvernoteService:" + evernoteService.name()); //$NON-NLS-1$
            }
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            oauthService = createOAuthService(getEvernoteAPI());

            try {
                requestToken = oauthService.getRequestToken();
            } catch (OAuthConnectionException e) {
                return new Status(
                        IStatus.WARNING,
                        EvernotePlugin.PLUGIN_ID,
                        NLS.bind(
                                EvernoteMessages.EvernoteExportJob_OtherException_message_withErrorMessage,
                                e.getMessage()), e);
            }

            String url = oauthService.getAuthorizationUrl(requestToken);

            if (DEBUGGING) {
                System.out.println("OAuthUrl:" + url); //$NON-NLS-1$
            }

            gotoURL(this, url);
            return Status.OK_STATUS;
        }

        private Class<? extends EvernoteApi> getEvernoteAPI() {
            return evernoteService == EvernoteService.PRODUCTION ? EvernoteApi.class
                    : EvernoteApi.Yinxiang.class;
        }

        private OAuthService createOAuthService(
                Class<? extends EvernoteApi> provider) {
            return new ServiceBuilder().provider(provider).apiKey(CONSUMER_KEY)
                    .apiSecret(CONSUMER_SECRET).callback(CALLBACK_URL).build();
        }

        public void changed(LocationEvent event) {
            String query = getQuery(event);
            if (query == null)
                return;

            String verifier = getOAuthVerifier(query);
            if (verifier != null && !"".equals(verifier)) { //$NON-NLS-1$
                if (DEBUGGING) {
                    System.out.println("verifier:" + verifier); //$NON-NLS-1$
                }

                removeBrowserListener();
                loadUserInfo(evernoteService, getAccessToken(verifier));
            }
        }

        private String getOAuthVerifier(String url) {
            if (!url.contains(OAUTH_VERIFIER_NAME))
                return null;

            int beginIndex = url.indexOf(OAUTH_VERIFIER_NAME)
                    + OAUTH_VERIFIER_NAME.length();
            int endIndex = beginIndex + OAUTH_VERIFIER_LENGTH;
            if (url.length() > endIndex)
                return url.substring(beginIndex, endIndex);
            return url.substring(beginIndex);
        }

        private String getQuery(LocationEvent event) {
            String query = null;
            try {
                URL url = new URL(event.location);
                query = url.getQuery();
            } catch (MalformedURLException e) {
            }
            return query;
        }

        private void removeBrowserListener() {
            Browser browser = getCurrentBrowser();
            if (browser != null && !browser.isDisposed()) {
                browser.stop();
                browser.removeLocationListener(this);
            }
        }

        private Token getAccessToken(String verifier) {
            return oauthService.getAccessToken(requestToken, new Verifier(
                    verifier));
        }

        private void loadUserInfo(EvernoteService evernoteService, Token token) {
            setUserInfo(token);
            setReturnCode(OK);
            returnCode = OK;
            doClose();
        }

        private void setUserInfo(Token token) {
            EvernoteAuth evernoteAuth = EvernoteAuth.parseOAuthResponse(
                    evernoteService, token.getRawResponse());

            data = new Properties();
            data.setProperty(EvernoteAccountStore.TOKEN,
                    evernoteAuth.getToken());
            data.setProperty(EvernoteAccountStore.SERVICE_TYPE,
                    evernoteService.name());

            if (DEBUGGING) {
                System.out.println("Token:" + evernoteAuth.getToken()); //$NON-NLS-1$
            }
        }

        public void changing(LocationEvent event) {
        }

    }

    private static final String CONSUMER_KEY = "xmind"; //$NON-NLS-1$

    private static final String CONSUMER_SECRET = "ee87a7c738511e29"; //$NON-NLS-1$

    private static final String CALLBACK_URL = "Home.action"; //$NON-NLS-1$

    private static final String LANGUAGE = Platform.getNL();

    private static final String NL_CN = "zh_CN"; //$NON-NLS-1$

    private static final String KEY_CONNECTING_AREA = "org.xmind.ui.evernote.SignInDialog.connectingArea"; //$NON-NLS-1$

    private static final String KEY_BROWSER = "org,xmind.ui.evernote.SignInDialog.browser"; //$NON-NLS-1$

    private Shell currentShell;

    private Job signInJob;

    private Properties data;

    private TabFolder tabFolder;

    private int returnCode;

    public SignInDialog(Shell parentShell) {
        super(parentShell);
        this.setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        this.currentShell = newShell;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(780, 550);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        return null;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new FillLayout());

        createTabFolder(composite);
        return composite;
    }

    private void createTabFolder(final Composite parent) {
        tabFolder = new TabFolder(parent, SWT.NONE);

        final TabItem evernoteItem = new TabItem(tabFolder, SWT.NONE);
        evernoteItem
                .setText(EvernoteMessages.EvernoteSignInDialog_EvernoteTab_text);
        createContentsForTabItem(evernoteItem);

        final TabItem yinxiangItem = new TabItem(tabFolder, SWT.NONE);
        yinxiangItem
                .setText(EvernoteMessages.EvernoteSignInDialog_YinxiangTab_text);
        createContentsForTabItem(yinxiangItem);

        EvernoteService service = getEvernoteService();
        updateContents(getEvernoteService());
        tabFolder
                .setSelection(service == EvernoteService.PRODUCTION ? evernoteItem
                        : yinxiangItem);

        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = tabFolder.getSelectionIndex();
                if (index < 0)
                    return;

                TabItem item = tabFolder.getItem(index);
                if (item == evernoteItem) {
                    updateContents(EvernoteService.PRODUCTION);
                } else if (item == yinxiangItem) {
                    updateContents(EvernoteService.YINXIANG);
                }
            };
        });
    }

    private void createContentsForTabItem(TabItem item) {
        Composite parent = new Composite(item.getParent(), SWT.NONE);
        StackLayout layout = new StackLayout();
        parent.setLayout(layout);

        Control connectingArea = createConnectingArea(parent);
        Control browser = new Browser(parent, SWT.NONE);
        layout.topControl = connectingArea;

        item.setControl(parent);
        item.setData(KEY_CONNECTING_AREA, connectingArea);
        item.setData(KEY_BROWSER, browser);
    }

    private Browser getCurrentBrowser() {
        if (tabFolder == null || tabFolder.isDisposed())
            return null;

        int index = tabFolder.getSelectionIndex();
        if (index < 0)
            return null;
        return (Browser) tabFolder.getItem(index).getData(KEY_BROWSER);
    }

    private Control createConnectingArea(Composite parent) {
        Composite connectingArea = new Composite(parent, SWT.NONE);
        connectingArea.setLayout(new GridLayout(1, false));

        Label messageLabel = new Label(connectingArea, SWT.NONE);
        messageLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
                true));
        messageLabel
                .setText(EvernoteMessages.EvernoteSignInDialog_Connecting_label);
        return connectingArea;
    }

    private void updateOAuthArea(boolean showBrowser) {
        int index = tabFolder.getSelectionIndex();
        if (index < 0)
            return;

        TabItem item = tabFolder.getItem(index);
        Control connectingArea = (Control) item.getData(KEY_CONNECTING_AREA);

        StackLayout stack = (StackLayout) connectingArea.getParent()
                .getLayout();
        if (showBrowser) {
            stack.topControl = (Control) item.getData(KEY_BROWSER);
        } else {
            stack.topControl = connectingArea;
        }
        connectingArea.getParent().layout(true, true);
    }

    private void updateContents(EvernoteService service) {
        if (service == EvernoteService.PRODUCTION) {
            currentShell
                    .setText(EvernoteMessages.EvernoteSignInDialog_Evernote_title);
        } else if (service == EvernoteService.YINXIANG) {
            currentShell
                    .setText(EvernoteMessages.EvernoteSignInDialog_Yinxiang_title);
        }

        startJob(service);
    }

    private void startJob(EvernoteService service) {
        stopJob();

        OAuthJob job = new OAuthJob(this, service);
        job.addJobChangeListener(this);
        job.setSystem(false);
        job.setUser(true);
        job.schedule();

        this.signInJob = job;
    }

    private void stopJob() {
        Job job = this.signInJob;
        if (job != null) {
            job.cancel();
            job = null;
        }
    }

    private void gotoURL(final LocationListener listener, final String url) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                Browser browser = getCurrentBrowser();
                gotoURL(browser, url);
                browser.addLocationListener(listener);
            }
        });
    }

    private void gotoURL(final Browser browser, final String url) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if (browser != null && !browser.isDisposed()) {
                    updateOAuthArea(true);

                    browser.setUrl(url);
                    if (Util.isMac())
                        browser.refresh();
                    browser.setFocus();
                }
            }
        });
    }

    private EvernoteService getEvernoteService() {
        if (NL_CN.equals(LANGUAGE))
            return EvernoteService.YINXIANG;
        return EvernoteService.PRODUCTION;
    }

    public void done(IJobChangeEvent event) {
        Job job = event.getJob();
        job.removeJobChangeListener(this);
        if (job != signInJob)
            return;

        signInJob = null;

        final IStatus result = event.getResult();
        if (result.getSeverity() != IStatus.OK) {
            if (result.getMessage() != null) {
                final Shell shell = this.getParentShell();
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        Dialog errorDialog = new ErrorStatusDialog(shell,
                                result.getMessage());
                        int code = errorDialog.open();
                        if (code == ErrorStatusDialog.OK
                                || code == ErrorStatusDialog.CANCEL) {
                            errorDialog.close();
                            doClose();
                        }
                    }
                });
            }
        }
    }

    public void aboutToRun(IJobChangeEvent event) {
    }

    public void awake(IJobChangeEvent event) {
    }

    public void running(IJobChangeEvent event) {
    }

    public void scheduled(IJobChangeEvent event) {
    }

    public void sleeping(IJobChangeEvent event) {
    }

    private boolean doClose() {
        boolean closed = super.close();
        if (closed) {
            stopJob();
        }
        return closed;
    }

    public Properties getData() {
        return this.data;
    }

    public int getCode() {
        return returnCode;
    }

}
