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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.statushandlers.StatusAdapter;

public class RuntimeErrorDialog extends Dialog {

    public static final QualifiedName SHOW_DETAILS_ON_CREATE = new QualifiedName(
            CathyStatusHandler.PROPERTY_PREFIX, "show_details_on_create"); //$NON-NLS-1$

    private StatusDetails details;

    private Control detailsArea = null;

    private Button detailsToggler = null;

    protected RuntimeErrorDialog(StatusAdapter statusAdapter) {
        super(ProgressManagerUtil.getDefaultParent());
        if (statusAdapter.getProperty(SHOW_DETAILS_ON_CREATE) == null) {
            statusAdapter.setProperty(SHOW_DETAILS_ON_CREATE, Boolean.FALSE);
        }
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
        ((GridData) titleArea.getLayoutData()).widthHint = 480;

        Control detailsToggler = createDetailsToggler(composite);
        detailsToggler.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        detailsArea = createDetailsArea(composite);
        detailsArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData) detailsArea.getLayoutData()).widthHint = 620;
        ((GridData) detailsArea.getLayoutData()).heightHint = 420;

        toggleDetailsArea(
                ((Boolean) details.getStatusAdapter().getProperty(
                        SHOW_DETAILS_ON_CREATE)), false);

        return composite;
    }

    private Control createTitleArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        area.setLayout(layout);

        Label titleImageLabel = new Label(area, SWT.NONE);
        titleImageLabel.setImage(details.getImage());
        titleImageLabel.setLayoutData(new GridData(SWT.BEGINNING,
                SWT.BEGINNING, false, false));

        Label messageLabel = new Label(area, SWT.WRAP);
        messageLabel.setText(details.getMessage());
        messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                true));

        return area;
    }

    private Control createDetailsToggler(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        composite.setLayout(layout);

        detailsToggler = new Button(composite, SWT.PUSH);
        detailsToggler
                .setText(StatusHandlerMessages.RuntimeErrorDialog_ShowDetailsButton_text);
        detailsToggler.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                toggleDetailsArea(null, true);
            }
        });
        detailsToggler.setLayoutData(new GridData(SWT.END, SWT.CENTER, true,
                true));

        return composite;
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

    private void toggleDetailsArea(Boolean newVisible, boolean updateShell) {
        if (detailsArea == null || detailsArea.isDisposed())
            return;

        Rectangle oldBounds = getShell().getBounds();

        boolean visible = newVisible == null ? !detailsArea.getVisible()
                : newVisible.booleanValue();
        detailsArea.setVisible(visible);
        ((GridData) detailsArea.getLayoutData()).exclude = !visible;

        if (detailsToggler != null && !detailsToggler.isDisposed()) {
            if (visible) {
                detailsToggler
                        .setText(StatusHandlerMessages.RuntimeErrorDialog_HideDetailsButton_text);
            } else {
                detailsToggler
                        .setText(StatusHandlerMessages.RuntimeErrorDialog_ShowDetailsButton_text);
            }
        }

        if (updateShell) {
            Point newBounds = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    true);
            int newWidth = newBounds.x, newHeight = newBounds.y;
            getShell().setBounds(
                    getConstrainedShellBounds(new Rectangle(oldBounds.x
                            - (newWidth - oldBounds.width) / 2, oldBounds.y
                            - (newHeight - oldBounds.height) / 2, newWidth,
                            newHeight)));
        }
    }

    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.HELP_ID,
                StatusHandlerMessages.RuntimeErrorDialog_ReportButton_text,
                true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                StatusHandlerMessages.RuntimeErrorDialog_IgnoreButton_text,
                false);
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.HELP_ID) {
            reportPressed();
        } else {
            super.buttonPressed(buttonId);
        }
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