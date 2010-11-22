package net.xmind.signin.internal;

import java.util.ArrayList;
import java.util.List;

import net.xmind.signin.IButtonCreator;
import net.xmind.signin.IDataStore;
import net.xmind.signin.ISignInDialogExtension;
import net.xmind.signin.ISignInDialogExtension2;
import net.xmind.signin.XMindNet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmind.ui.resources.FontUtils;

public class SignInDialog2 extends Dialog implements IJobChangeListener,
        IButtonCreator {

    private class InternalSignInJob extends Job {

        private String user;

        private String password;

        private boolean remember;

        public InternalSignInJob(String user, String passwrod, boolean remember) {
            super("Sign in to XMind.net"); //$NON-NLS-1$
            setSystem(true);
            this.user = user;
            this.password = passwrod;
            this.remember = remember;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            String url = "https://www.xmind.net/_res/token/" + user; //$NON-NLS-1$
            PostMethod method = new PostMethod(url);
            method.addParameter("user", user); //$NON-NLS-1$
            method.addParameter("password", password); //$NON-NLS-1$
            method.addParameter("remember", Boolean.toString(remember)); //$NON-NLS-1$

            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            HttpClient client = new HttpClient();
            int code;
            try {
                code = client.executeMethod(method);
            } catch (Exception e) {
                return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                        Messages.SignInDialog_NetworkError_message, e);
            }

            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            if (code == HttpStatus.SC_OK) {
                JSONObject json;
                try {
                    String resp = method.getResponseBodyAsString();
                    json = new JSONObject(resp);
                } catch (Exception e) {
                    return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                            Messages.SignInDialog_ApplicationError_message, e);
                }

                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                code = json.optInt("_code", -1); //$NON-NLS-1$
                if (code == HttpStatus.SC_OK) {
                    try {
                        json
                                .put(SignInJob.REMEMBER, Boolean
                                        .toString(remember));
                    } catch (JSONException e) {
                        //never happens
                    }
                    data = new JSONStore(json);
                    return Status.OK_STATUS;
                } else {
                    return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                            code, Messages.SignInDialog_RequestError_message,
                            null);
                }
            }

            return new Status(IStatus.WARNING, Activator.PLUGIN_ID, code,
                    Messages.SignInDialog_ServerError_message, null);
        }

    }

    private String message;

    private ISignInDialogExtension extension;

    private Text nameField;

    private Text passwordField;

    private Button rememberCheck;

    private Label messageLabel;

    private Label messageIcon;

    private boolean showingErrorMessage = false;

    private IDataStore data = null;

    private Job signInJob;

    private List<Integer> buttonIds = new ArrayList<Integer>();

    public SignInDialog2(Shell parentShell) {
        this(parentShell, null, null);
    }

    public SignInDialog2(Shell parentShell, String message,
            ISignInDialogExtension extension) {
        super(parentShell);
        setBlockOnOpen(true);
        setShellStyle(SWT.TITLE | SWT.CLOSE | SWT.APPLICATION_MODAL);
        this.message = message == null ? Messages.SignInDialog_message
                : message;
        this.extension = extension;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        if (message != null) {
            createMessageArea(composite);
            createSeparator(composite);
        }
        createFormArea(composite);

        return composite;
    }

    private void createMessageArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = 540;
        gridData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 15;
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;
        composite.setLayout(gridLayout);
        composite.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));
        createMessageIcon(composite);
        createMessageLabel(composite);
    }

    private void createMessageIcon(Composite parent) {
        messageIcon = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        messageIcon.setLayoutData(gridData);
        messageIcon.setBackground(parent.getBackground());
        messageIcon.setImage(parent.getDisplay().getSystemImage(
                SWT.ICON_INFORMATION));
    }

    private void createMessageLabel(Composite parent) {
        messageLabel = new Label(parent, SWT.WRAP);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        messageLabel.setLayoutData(gridData);
        messageLabel.setBackground(parent.getBackground());
        messageLabel.setForeground(parent.getDisplay().getSystemColor(
                SWT.COLOR_LIST_FOREGROUND));
        messageLabel.setText(message);
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
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = 35;
        gridLayout.marginHeight = 30;
        gridLayout.marginBottom = 15;
        gridLayout.verticalSpacing = 7;
        gridLayout.horizontalSpacing = 7;
        form.setLayout(gridLayout);

        // Row 1:
        createNameLabel(form);
        createNameField(form);
        createSignUpButton(form);

        // Row 2:
        createPasswordLabel(form);
        createPasswordField(form);
        createForgotPasswordButton(form);
    }

    private void createNameLabel(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        label.setText(Messages.SignInDialog_NameField_text);
    }

    private void createNameField(Composite parent) {
        nameField = new Text(parent, SWT.BORDER | SWT.SINGLE);
        nameField
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) nameField.getLayoutData()).widthHint = 160;
        nameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                restoreMessageArea();
            }
        });
        nameField.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                nameField.selectAll();
            }
        });
    }

    private void createSignUpButton(Composite parent) {
        Control link = createLink(parent, Messages.SignInDialog_NotMember_text,
                new Runnable() {
                    public void run() {
                        XMindNet.gotoURL("http://www.xmind.net/signup/"); //$NON-NLS-1$
                        close();
                    }
                });
        link
                .setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                        false));
    }

    private void createPasswordLabel(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        label.setText(Messages.SignInDialog_PasswordField_text);
    }

    private void createPasswordField(Composite parent) {
        passwordField = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
        passwordField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        ((GridData) passwordField.getLayoutData()).widthHint = 160;
        passwordField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                restoreMessageArea();
            }
        });
        passwordField.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                passwordField.selectAll();
            }
        });
    }

    private void createForgotPasswordButton(Composite parent) {
        Control link = createLink(parent,
                Messages.SignInDialog_ForgotPassword_text, new Runnable() {
                    public void run() {
                        XMindNet
                                .gotoURL("http://www.xmind.net/signin/forgotpassword/"); //$NON-NLS-1$
                        close();
                    }
                });
        link
                .setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                        false));
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
                    if (pressed && inside) {
                        openHandler.run();
                    }
                    pressed = false;
                    inside = false;
                    link.setForeground(normalColor);
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

        createRememberCheck(composite);
        if (extension != null) {
            createExtensionControls(composite);
        }
    }

    private boolean hasButtonBarContributor() {
        return extension instanceof ISignInDialogExtension2;
    }

    private void createRememberCheck(Composite parent) {
        rememberCheck = new Button(parent, SWT.CHECK);
        rememberCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        rememberCheck.setText(Messages.SignInDialog_Remember_text);
    }

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
        if (buttonId != IDialogConstants.OK_ID) {
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
        signInJob = new InternalSignInJob(nameField.getText(), passwordField
                .getText(), rememberCheck.getSelection());
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
        return data == null ? null : data.getString(XMindNetAccount.USER);
    }

    public String getToken() {
        return data == null ? null : data.getString(XMindNetAccount.TOKEN);
    }

    public boolean shouldRemember() {
        return data == null ? false : data.getBoolean(SignInJob.REMEMBER);
    }

    public IDataStore getData() {
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

    private void restoreMessageArea() {
        if (!showingErrorMessage)
            return;
        if (messageLabel == null || messageLabel.isDisposed())
            return;
        messageLabel.setText(message);
        messageIcon.setImage(messageIcon.getDisplay().getSystemImage(
                SWT.ICON_INFORMATION));
        showingErrorMessage = false;
    }

    private void setErrorMessage(String erorMessage) {
        if (messageLabel == null || messageLabel.isDisposed())
            return;
        messageLabel.setText(erorMessage);
        messageIcon.setImage(Display.getCurrent()
                .getSystemImage(SWT.ICON_ERROR));
        showingErrorMessage = true;
    }

}
