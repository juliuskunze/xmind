/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

package org.xmind.ui.dialogs;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Frank Shaka
 * 
 */
public class ErrorDetailsDialog extends IconAndMessageDialog {

    private String title;

    private ErrorDetails details;

    public ErrorDetailsDialog(Shell parentShell, String title, String message,
            Throwable error, String errorMessage, long time) {
        this(parentShell, title, message, new ErrorDetails(error, errorMessage,
                time));
    }

    /**
     * @param parentShell
     */
    public ErrorDetailsDialog(Shell parentShell, String title, String message,
            ErrorDetails details) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.details = details;
        setShellStyle(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE
                | getDefaultOrientation());
    }

    protected Image getImage() {
        return getErrorImage();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
        newShell.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    setReturnCode(CANCEL);
                    close();
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        createButton(parent, IDialogConstants.HELP_ID,
                IDialogConstants.HELP_LABEL, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.HELP_ID) {
            showHelp();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    private void showHelp() {
        try {
            PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
                    .openURL(new URL("http://www.xmind.net/xmind/help/")); //$NON-NLS-1$
        } catch (PartInitException e) {
        } catch (MalformedURLException e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        composite.setLayout(layout);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = SWT.DEFAULT;
        data.heightHint = SWT.DEFAULT;
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        createMessageArea(composite);
        createErrorText(composite);

        return composite;
    }

    private void createErrorText(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        Text textWidget = new Text(composite, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY);
        textWidget.setText(details.getFullText());

        final Label copiedLabel = new Label(composite, SWT.NONE);
        copiedLabel.setText(Messages.ErrorDetailsDialog_Copied_message);
        copiedLabel.setVisible(false);

        Button copyButton = new Button(composite, SWT.PUSH);
        copyButton.setText(Messages.ErrorDetailsDialog_CopyButton_text);
        copyButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                details.copyToClipboard();
                copiedLabel.setVisible(true);
            }
        });

        GridData mainData = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainData.widthHint = SWT.DEFAULT;
        mainData.heightHint = SWT.DEFAULT;
        mainData.horizontalSpan = 2;
        composite.setLayoutData(mainData);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 7;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.widthHint = SWT.DEFAULT;
        textData.heightHint = SWT.DEFAULT;
        textData.horizontalSpan = 2;
        textWidget.setLayoutData(textData);

        GridData copiedData = new GridData(SWT.END, SWT.CENTER, true, false);
        copiedData.widthHint = SWT.DEFAULT;
        copiedData.heightHint = SWT.DEFAULT;
        copiedLabel.setLayoutData(copiedData);

        GridData buttonData = new GridData(SWT.END, SWT.CENTER, false, false);
        buttonData.widthHint = SWT.DEFAULT;
        buttonData.heightHint = SWT.DEFAULT;
        copyButton.setLayoutData(buttonData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 380);
    }

}
