package net.xmind.signin.internal;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.xmind.signin.IButtonCreator;
import net.xmind.signin.IDataStore;
import net.xmind.signin.ISignInDialogExtension;
import net.xmind.signin.ISignInDialogExtension2;
import net.xmind.signin.XMindNet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.resources.FontUtils;

public class SignInDialog2 extends Dialog implements IJobChangeListener,
        IButtonCreator {

    private static class InternalSignInJob extends Job {

        private String user;

        private String password;

//        private boolean remember;

        private XMindNetRequest request = null;

        private IDataStore data;

        public InternalSignInJob(String user, String passwrod) {
            super("Sign in to XMind.net"); //$NON-NLS-1$
            setSystem(true);
            this.user = user;
            this.password = passwrod;
//            this.remember = remember;
        }

        public IDataStore getData() {
            return data;
        }

        @Override
        protected void canceling() {
            if (request != null) {
                request.abort();
            }
            super.canceling();
        }

        private String hash(String password) {
            MessageDigest md5;
            try {
                try {
                    // Try BouncyCastle:
                    md5 = MessageDigest.getInstance("MD5", "BC"); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (NoSuchProviderException e) {
                    md5 = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
                }
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
            Base64 encoder = new Base64();
            try {
                return new String(encoder.encode(md5.digest(password
                        .getBytes("UTF-8"))), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            request = new XMindNetRequest(true);
            request.uri("/_res/token/%s", user); //$NON-NLS-1$
            request.addParameter("user", user); //$NON-NLS-1$
            String pwdhash = hash(password);
            if (pwdhash != null) {
                request.addParameter("pwd", pwdhash); //$NON-NLS-1$
            } else {
                request.addParameter("password", password); //$NON-NLS-1$
            }
//            request.addParameter("remember", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            request.post();

            if (monitor.isCanceled() || request.isAborted())
                return Status.CANCEL_STATUS;

            int code = request.getCode();
            IDataStore data = request.getData();
            if (code == HttpStatus.SC_OK) {
                if (data != null) {
                    this.data = data;
                    return Status.OK_STATUS;
                }
                return error(code,
                        Messages.SignInDialog_ApplicationError_message,
                        request.getException());
            } else if (code == XMindNetRequest.ERROR) {
                return error(code, Messages.SignInDialog_NetworkError_message,
                        request.getException());
            } else if (code >= 400 && code < 500) {
                return error(code, Messages.SignInDialog_RequestError_message,
                        null);
            } else {
                return error(code, Messages.SignInDialog_ServerError_message,
                        null);
            }
        }

        private static IStatus error(int code, String message,
                Throwable exception) {
            return new Status(IStatus.WARNING, Activator.PLUGIN_ID, code,
                    message, exception);
        }

    }

    private boolean sheet;

    private String message;

    private String errorMessage = null;

    private ISignInDialogExtension extension;

    private Text nameField;

    private Text passwordField;

    //private Button rememberCheck;

    private Composite messageArea;

    private Composite errorMessageArea;

    private Label errorLabel;

    private Properties data;

    private Job signInJob;

    private List<Integer> buttonIds = new ArrayList<Integer>();

    public SignInDialog2(Shell parentShell) {
        this(parentShell, null, null, false, null);
    }

    public SignInDialog2(Shell parentShell, String message,
            ISignInDialogExtension extension) {
        this(parentShell, message, extension, false, null);
    }

    public SignInDialog2(Shell parentShell, String message,
            ISignInDialogExtension extension, boolean sheet, Properties data) {
        super(parentShell);
        this.message = message == null ? Messages.SignInDialog_message
                : message;
        this.extension = extension;
        this.sheet = sheet;
        this.data = data == null ? new Properties() : data;
        if (sheet) {
            setShellStyle(getShellStyle() | SWT.SHEET);
        }
        setBlockOnOpen(true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        if (message != null) {
            createMessageArea(parent);
            createSeparator(parent);
        }
        Composite composite = (Composite) super.createDialogArea(parent);
        createFormArea(composite);
        return composite;
    }

    private void createMessageArea(Composite parent) {
        Composite messageStack = new Composite(parent, SWT.NONE);
        StackLayout layout = new StackLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        messageStack.setLayout(layout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = 480;
        gridData.heightHint = SWT.DEFAULT;
        messageStack.setLayoutData(gridData);

        messageArea = createMessagePage(messageStack);
        createMessageIcon(messageArea, SWT.ICON_INFORMATION);
        createMessageLabel(messageArea, message);

        errorMessageArea = createMessagePage(messageStack);
        createMessageIcon(errorMessageArea, SWT.ICON_WARNING);
        errorLabel = createMessageLabel(errorMessageArea, errorMessage);

        setErrorMessage(errorMessage);
    }

    private Composite createMessagePage(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 15;
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;
        composite.setLayout(gridLayout);
        composite.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));
        return composite;
    }

    private Label createMessageIcon(Composite parent, int image) {
        Label icon = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        icon.setLayoutData(gridData);
        icon.setBackground(parent.getBackground());
        icon.setImage(parent.getDisplay().getSystemImage(image));
        return icon;
    }

    private Label createMessageLabel(Composite parent, String message) {
        Label label = new Label(parent, SWT.WRAP);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        label.setLayoutData(gridData);
        label.setBackground(parent.getBackground());
        label.setForeground(parent.getDisplay().getSystemColor(
                SWT.COLOR_LIST_FOREGROUND));
        label.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                -1));
        if (message != null)
            label.setText(message);
        return label;
    }

    private void createSeparator(Composite parent) {
        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        sep.setLayoutData(gridData);
    }

    private void createFormArea(Composite parent) {
        Composite form = new Composite(parent, SWT.NONE);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 30;
        gridLayout.marginHeight = 25;
        gridLayout.marginBottom = 15;
        gridLayout.verticalSpacing = 12;
        gridLayout.horizontalSpacing = 7;
        form.setLayout(gridLayout);

        // Row 1:
        createNameLabel(form);
        createNameField(form);
//        createSignUpButton(form);

        // Row 2:
        createPasswordLabel(form);
        createPasswordField(form);
//        createForgotPasswordButton(form);

        // Row 3:
        Label emptyPlaceholder = new Label(form, SWT.NONE);
        emptyPlaceholder.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));

        Control forgotButton = createForgotPasswordButton(form);
        ((GridData) forgotButton.getLayoutData()).verticalIndent = -10;
    }

    private void createNameLabel(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        label.setText(Messages.SignInDialog_NameField_text);
        label.setFont(FontUtils.getBold(JFaceResources.DEFAULT_FONT));
    }

    private void createNameField(Composite parent) {
        nameField = new Text(parent, SWT.BORDER | SWT.SINGLE);
        nameField
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) nameField.getLayoutData()).widthHint = 160;
        nameField.setText(data.getProperty(XMindNetAccount.USER, "")); //$NON-NLS-1$
        nameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                data.setProperty(XMindNetAccount.USER, nameField.getText());
                setErrorMessage(null);
            }
        });
        nameField.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                nameField.selectAll();
            }
        });
    }

    private Control createSignUpButton(Composite parent) {
        Control link = createLink(parent, Messages.SignInDialog_NotMember_text,
                new Runnable() {
                    public void run() {
                        XMindNet.gotoURL(true,
                                "https://www.xmind.net/xmind/signup/"); //$NON-NLS-1$
//                        close();
                    }
                });
        link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        return link;
    }

    private void createPasswordLabel(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        label.setText(Messages.SignInDialog_PasswordField_text);
        label.setFont(FontUtils.getBold(JFaceResources.DEFAULT_FONT));
    }

    private void createPasswordField(Composite parent) {
        passwordField = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
        passwordField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        ((GridData) passwordField.getLayoutData()).widthHint = 160;
        passwordField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                setErrorMessage(null);
            }
        });
        passwordField.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                passwordField.selectAll();
            }
        });
    }

    private Control createForgotPasswordButton(Composite parent) {
        Control link = createLink(parent,
                Messages.SignInDialog_ForgotPassword_text, new Runnable() {
                    public void run() {
                        XMindNet.gotoURL(true,
                                "https://www.xmind.net/xmind/forgotpassword/"); //$NON-NLS-1$
//                        close();
                    }
                });
        link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        return link;
    }

    private Control createLink(final Composite parent, String text,
            final Runnable openHandler) {
        final Label link = new Label(parent, SWT.NONE);
        link.setText(text);
        link.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        final Color normalColor = parent.getDisplay().getSystemColor(
                SWT.COLOR_BLUE);
        link.setForeground(normalColor);
        link.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                -1));
        Listener listener = new Listener() {
            boolean pressed = false;
            boolean inside = false;
            Color pressedColor = parent.getDisplay().getSystemColor(
                    SWT.COLOR_DARK_MAGENTA);

            public void handleEvent(Event event) {
                if (event.type == SWT.MouseDown) {
                    pressed = true;
                    inside = true;
                    link.setForeground(pressedColor);
                } else if (event.type == SWT.MouseUp) {
                    boolean shouldRun = pressed && inside;
                    pressed = false;
                    inside = false;
                    link.setForeground(normalColor);
                    if (shouldRun) {
                        openHandler.run();
                    }
                } else if (event.type == SWT.MouseExit) {
                    if (pressed) {
                        inside = false;
                    }
                } else if (event.type == SWT.MouseEnter) {
                    if (pressed) {
                        inside = true;
                    }
                }
            }
        };
        link.addListener(SWT.MouseDown, listener);
        link.addListener(SWT.MouseUp, listener);
        link.addListener(SWT.MouseExit, listener);
        link.addListener(SWT.MouseEnter, listener);
        return link;
    }

    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(hasButtonBarContributor() ? 1
                : 2, false);
        gridLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        gridLayout.marginHeight = 0;
        gridLayout.marginBottom = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(gridLayout);

        createExpandedArea(composite);

        Composite buttonBar = new Composite(composite, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        buttonBar.setLayout(layout);
        buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));
        buttonBar.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(buttonBar);
        return buttonBar;
    }

    private void createExpandedArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(gridLayout);

        createSignUpButton(composite);
        //createRememberCheck(composite);
        if (extension != null) {
            createExtensionControls(composite);
        }
    }

    private boolean hasButtonBarContributor() {
        return extension instanceof ISignInDialogExtension2;
    }

//    private void createRememberCheck(Composite parent) {
//        rememberCheck = new Button(parent, SWT.CHECK);
//        rememberCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
//                false));
//        rememberCheck.setText(Messages.SignInDialog_Remember_text);
//    }

    private void createExtensionControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        extension.contributeToOptions(this, composite);
        if (composite.getChildren().length == 0) {
            composite.dispose();
        }
    }

    @Override
    public void create() {
        super.create();
        nameField.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.xmind.signin.IButtonCreator#doCreateButton(org.eclipse.swt.widgets
     * .Composite, int, java.lang.String)
     */
    public Button doCreateButton(Composite parent, int id, String label) {
        buttonIds.add(Integer.valueOf(id));
        return createButton(parent, id, label, false);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                Messages.SignInDialog_SignIn_text, true);
        if (sheet) {
            createButton(parent, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
        }
        if (extension instanceof ISignInDialogExtension2) {
            ((ISignInDialogExtension2) extension).contributeToButtonBar(this,
                    parent, this);
        }
    }

    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.SignInDialog_title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId != IDialogConstants.OK_ID
                && buttonId != IDialogConstants.CANCEL_ID) {
            if (extension instanceof ISignInDialogExtension2) {
                ((ISignInDialogExtension2) extension).handleButtonPressed(this,
                        buttonId);
                return;
            }
        }
        super.buttonPressed(buttonId);
    }

    private void changeButton(int buttonId, boolean enabled, String withLabel) {
        Button button = getButton(buttonId);
        if (button != null && !button.isDisposed()) {
            if (withLabel != null)
                button.setText(withLabel);
            button.setEnabled(enabled);
        }
    }

    @Override
    protected void okPressed() {
        changeButton(IDialogConstants.OK_ID, false,
                Messages.SignInDialog_SigningIn_text);
        for (Integer buttonId : buttonIds) {
            changeButton(buttonId.intValue(), false, null);
        }

        startJob();
    }

    @Override
    public boolean close() {
        setReturnCode(CANCEL);
        return doClose();
    }

    private boolean doClose() {
        boolean closed = super.close();
        if (closed) {
            stopJob();
        }
        return closed;
    }

    private void startJob() {
        stopJob();
        signInJob = new InternalSignInJob(nameField.getText(),
                passwordField.getText());
        signInJob.addJobChangeListener(this);
        signInJob.schedule();
    }

    private void stopJob() {
        if (signInJob != null) {
            signInJob.removeJobChangeListener(this);
            signInJob.cancel();
            signInJob = null;
        }
    }

    public String getUserID() {
        return data == null ? null : data.getProperty(XMindNetAccount.USER);
    }

    public String getToken() {
        return data == null ? null : data.getProperty(XMindNetAccount.TOKEN);
    }

//    public boolean shouldRemember() {
//        return data == null ? false : data.getBoolean(SignInJob.REMEMBER);
//    }

    public Properties getData() {
        return data;
    }

    public void aboutToRun(IJobChangeEvent event) {
    }

    public void awake(IJobChangeEvent event) {
    }

    public void done(IJobChangeEvent event) {
        Job job = event.getJob();
        job.removeJobChangeListener(this);
        if (job == signInJob) {
            signInJob = null;
            final IStatus result = event.getResult();
            if (result.getSeverity() == IStatus.OK) {
                IDataStore resultData = ((InternalSignInJob) job).getData();
                data.putAll(resultData.toMap());
                getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        setReturnCode(OK);
                        doClose();
                    }
                });
            } else {
                getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        setErrorMessage(result.getMessage());
                        changeButton(IDialogConstants.OK_ID, true,
                                Messages.SignInDialog_SignIn_text);
                        for (Integer buttonId : buttonIds) {
                            changeButton(buttonId.intValue(), true, null);
                        }
                    }
                });
            }
        }
    }

    public void running(IJobChangeEvent event) {
    }

    public void scheduled(IJobChangeEvent event) {
    }

    public void sleeping(IJobChangeEvent event) {
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorLabel == null || errorLabel.isDisposed())
            return;
        Composite stack = messageArea.getParent();
        StackLayout layout = (StackLayout) stack.getLayout();
        if (errorMessage == null) {
            layout.topControl = messageArea;
            messageArea.setVisible(true);
            errorMessageArea.setVisible(false);
        } else {
            errorLabel.setText(errorMessage);
            layout.topControl = errorMessageArea;
            messageArea.setVisible(false);
            errorMessageArea.setVisible(true);
        }
        stack.layout();
    }

}
