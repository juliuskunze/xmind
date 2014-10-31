package org.xmind.ui.internal.sharing;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ConnectionDialog extends Dialog {

    private static final String LOCAL_NETWORK_SHARING_ICON = "icons/localnetwork32.png"; //$NON-NLS-1$

    private String message;

    public ConnectionDialog(Shell parentShell, String message) {
        super(parentShell);
        Assert.isNotNull(message);
        this.message = message;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(SharingMessages.CommonDialogTitle_LocalNetworkSharing);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite buttonBar = (Composite) super.createButtonBar(parent);
        Button okButton = getButton(Dialog.OK);
        if (okButton != null && !okButton.isDisposed()) {
            okButton.setFocus();
        }
        return buttonBar;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        createContent(composite);
        return composite;
    }

    private void createContent(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 20;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label imageLabel = new Label(composite, SWT.NONE);
        URL url = Platform.getBundle(LocalNetworkSharingUI.PLUGIN_ID).getEntry(
                LOCAL_NETWORK_SHARING_ICON);
        if (url != null) {
            try {
                imageLabel.setImage(new Image(null, url.openStream()));
            } catch (IOException e) {
            }
        }
        imageLabel.setBackground(composite.getBackground());

        Label label = new Label(composite, SWT.WRAP);
        label.setText(message);
    }

}
