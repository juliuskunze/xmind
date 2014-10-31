package org.xmind.ui.internal.evernote;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ErrorStatusDialog extends Dialog {

    private final String errorStr;

    public ErrorStatusDialog(Shell parentShell, String errorStr) {
        super(parentShell);
        this.errorStr = errorStr;
        setReturnCode(OK);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(EvernoteMessages.EvernoteExportDialog_title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        createMessageArea(composite);
        return composite;
    }

    protected void createMessageArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 10;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label icon = new Label(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        icon.setLayoutData(gridData);
        icon.setBackground(parent.getBackground());
        icon.setImage(parent.getDisplay().getSystemImage(SWT.ICON_INFORMATION));

        Label label = new Label(composite, SWT.WRAP);
        GridData labelData = new GridData(SWT.END, SWT.CENTER, true, true);
        labelData.widthHint = 300;
        labelData.heightHint = SWT.DEFAULT;
        label.setLayoutData(labelData);
        label.setText(errorStr);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite buttonBar = (Composite) super.createButtonBar(parent);
        GridLayout layout = (GridLayout) buttonBar.getLayout();
        layout.marginHeight = 10;
        getButton(IDialogConstants.CANCEL_ID).setVisible(false);
        return buttonBar;
    }

}
