/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package org.xmind.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Frank Shaka
 */
public class HyperlinkDialog extends InputDialog {

    public static final int REMOVE = 3;

    private static final int REMOVE_ID = IDialogConstants.CLIENT_ID + 1;

    private static final String EMPTY = ""; //$NON-NLS-1$

    private boolean multi;

    private boolean internalChangeValue = false;

    public HyperlinkDialog(Shell parentShell, String initValue) {
        super(
                parentShell,
                DialogMessages.HyperlinkDialog_title,
                DialogMessages.HyperlinkDialog_MultipleTopics_message,
                initValue == null ? DialogMessages.HyperlinkDialog_MultipleTopics_value
                        : initValue, null);
        this.multi = true;
    }

    public HyperlinkDialog(Shell parentShell, String topicTitle,
            String initValue) {
        super(parentShell, DialogMessages.HyperlinkDialog_title, NLS.bind(
                DialogMessages.HyperlinkDialog_description, topicTitle), initValue,
                null);
        this.multi = false;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, REMOVE_ID, DialogMessages.HyperlinkDialog_Remove,
                false);
        super.createButtonsForButtonBar(parent);
    }

    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        if (multi) {
            hookText(getText());
        }
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null) {
            button.setEnabled(false);
        }
        return control;
    }

    private void hookText(final Control textControl) {
        final Listener hook = new Listener() {
            public void handleEvent(Event event) {
                if (internalChangeValue)
                    return;
                internalChangeValue = true;
                getText().setText(EMPTY);
                final Listener hook = this;
                event.display.asyncExec(new Runnable() {
                    public void run() {
                        try {
                            textControl.removeListener(SWT.KeyDown, hook);
                            textControl.removeListener(SWT.MouseDown, hook);
                        } catch (Throwable t) {
                            // do nothing
                        }
                    }
                });
                internalChangeValue = false;
            }
        };
        textControl.addListener(SWT.KeyDown, hook);
        textControl.addListener(SWT.MouseDown, hook);
    }

    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (buttonId == REMOVE_ID) {
            removePressed();
        }
    }

    protected void removePressed() {
        setReturnCode(REMOVE);
        close();
    }

}