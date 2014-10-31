/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.ui.internal.statushandlers;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.statushandlers.StatusAdapter;

public class RuntimeErrorDialog extends Dialog {

    public static final QualifiedName SHOW_DETAILS_ON_CREATE = new QualifiedName(
            CathyStatusHandler.PROPERTY_PREFIX, "show_details_on_create"); //$NON-NLS-1$
    public static final QualifiedName DIALOG_EXTENSION = new QualifiedName(
            CathyStatusHandler.PROPERTY_PREFIX, "dialog_extension"); //$NON-NLS-1$

    private StatusDetails details;

    private Control detailsArea = null;

    protected RuntimeErrorDialog(StatusAdapter statusAdapter) {
        super(ProgressManagerUtil.getDefaultParent());
        this.details = new StatusDetails(statusAdapter);
        setBlockOnOpen(false);
        if (!((Boolean) statusAdapter.getProperty(CathyStatusHandler.BLOCK))
                .booleanValue()) {
            setShellStyle(~SWT.APPLICATION_MODAL & getShellStyle());
        }
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(StatusHandlerMessages.RuntimeErrorDialog_windowTitle);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Control titleArea = createTitleArea(composite);
        titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData) titleArea.getLayoutData()).widthHint = 280;

        detailsArea = createDetailsArea(composite);
        detailsArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData) detailsArea.getLayoutData()).widthHint = 300;
        ((GridData) detailsArea.getLayoutData()).heightHint = 100;

        return composite;
    }

    private Control createTitleArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        area.setLayout(layout);

        Label titleImageLabel = new Label(area, SWT.NONE);
        titleImageLabel.setImage(details.getImage());
        titleImageLabel.setLayoutData(new GridData(SWT.BEGINNING,
                SWT.BEGINNING, false, false));

        Composite messageParent = new Composite(area, SWT.NONE);
        messageParent.setLayout(new GridLayout());

        Label messageLabel = new Label(messageParent, SWT.WRAP);
        messageLabel.setText(details.getMessage());
        messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                true));
        IRuntimeErrorDialogExtension extension = (IRuntimeErrorDialogExtension) details
                .getStatusAdapter().getProperty(DIALOG_EXTENSION);
        if (extension != null) {
            extension.createDialogExtension(messageParent);
        }

        return area;
    }

    private Control createDetailsArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        area.setLayout(layout);

        Text detailsText = new Text(area, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL);
        detailsText.setEditable(false);
        detailsText.setText(details.getFullText());
        detailsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return area;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;
        ((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
        ((GridLayout) parent.getLayout()).horizontalSpacing = 250;
        Hyperlink report = new Hyperlink(parent, SWT.LEFT);
        report.setText(StatusHandlerMessages.RuntimeErrorDialog_ReportHyperlink_Text);
        report.setUnderlined(true);
        report.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                reportPressed();
            }
        });
        createButton(parent, IDialogConstants.CANCEL_ID,
                StatusHandlerMessages.RuntimeErrorDialog_CloseButton_Text,
                false);

    }

    private void reportPressed() {
        try {
            IErrorReporter.Default.getInstance().report(details);
        } catch (InterruptedException e) {
            return;
        }
        close();
    }

}