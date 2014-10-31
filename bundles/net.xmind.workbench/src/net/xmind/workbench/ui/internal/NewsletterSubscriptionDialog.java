package net.xmind.workbench.ui.internal;

import net.xmind.signin.XMindNet;
import net.xmind.workbench.internal.Messages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewsletterSubscriptionDialog extends TitleAreaDialog {

    private String value = ""; //$NON-NLS-1$

    private IInputValidator validator;

    private Button okButton;

    private Text text;

    private Image titleImage;

    public NewsletterSubscriptionDialog(Shell parentShell,
            IInputValidator validator) {
        super(parentShell);
        this.validator = validator;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            value = text.getText();
        } else {
            value = null;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.NewsletterSubscriptionReminder_DialogTitle);
    }

    @Override
    public void create() {
        super.create();
        setTitle(Messages.NewsletterSubscriptionReminder_DialogTitle);
        //setMessage(Messages.LicenseInputDialog_Description);
        if (titleImage == null)
            titleImage = XMindNet.createBannerLogo();
        if (titleImage != null)
            setTitleImage(titleImage);
        setMessage(Messages.NewsletterSubscriptionReminder_TitleMessage);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        text.setFocus();
        if (value != null) {
            text.setText(value);
            text.selectAll();
        }
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite area = new Composite(composite, SWT.NONE);

        GridLayout layout = new GridLayout();
//        layout.marginHeight = 15;
        layout.marginTop = 15;
        layout.marginWidth = 9;
        area.setLayout(layout);
        area.setLayoutData(new GridData(GridData.FILL_BOTH));

        createDetailArea(area);

//        createMessageLabel(composite);
//
//        createTextArea(composite);

        applyDialogFont(composite);
        return composite;
    }

    private Composite createDetailArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.CENTER);

        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createMessageLabel(composite);

        createTextArea(composite);

        return composite;
    }

    private void createMessageLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);

        label.setText(Messages.NewsletterSubscriptionReminder_DialogMessage);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
        label.setLayoutData(data);

        label.setFont(parent.getFont());
    }

    private void createTextArea(Composite composite) {
        text = new Text(composite, getInputTextStyle());
        text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
    }

    protected Label getErrorMessageLabel() {
        return null;
    }

    protected Button getOkButton() {
        return okButton;
    }

    protected Text getText() {
        return text;
    }

    protected IInputValidator getValidator() {
        return validator;
    }

    public String getValue() {
        return value;
    }

    protected void validateInput() {
        String message = null;
        if (validator != null) {
            message = validator.isValid(text.getText());
        }
        Control button = getButton(IDialogConstants.OK_ID);
        if (button != null) {
            button.setEnabled(message == null);
        }
    }

    protected int getInputTextStyle() {
        return SWT.SINGLE | SWT.BORDER;
    }

    @Override
    public boolean close() {
        if (titleImage != null && !titleImage.isDisposed())
            titleImage.dispose();
        return super.close();
    }
}
