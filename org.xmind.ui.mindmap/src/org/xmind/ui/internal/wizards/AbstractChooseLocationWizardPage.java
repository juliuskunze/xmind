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
package org.xmind.ui.internal.wizards;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.xmind.ui.mindmap.MindMapUI;

public abstract class AbstractChooseLocationWizardPage extends WizardPage {

    private static final String PAGE_NAME = "org.xmind.ui.wizard.newWorkbookWizard.chooseLocationPage"; //$NON-NLS-1$

    private static final String SAVE_LATER = "decideSaveLocationLater"; //$NON-NLS-1$

    private String fileName;

    private boolean saveLater;

    private List<Control> inputControls = new ArrayList<Control>();

    protected AbstractChooseLocationWizardPage() {
        super(PAGE_NAME, WizardMessages.ChooseLocationWizardPage_title, null);
    }

    public abstract void setWorkbenchSelection(IStructuredSelection selection);

    public abstract IEditorInput createEditorInput(InputStream templateStream)
            throws CoreException;

    public abstract String getParentPath();

    protected void createSaveLaterButton(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 20;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        final Button button = new Button(composite, SWT.CHECK);
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        button.setText(WizardMessages.ChooseLocationWizardPage_DecideLocationLater_text);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                saveLater = button.getSelection();
                updateAll();
            }
        });
        saveLater = getDialogSettings().getBoolean(SAVE_LATER);
        button.setSelection(saveLater);
        updateSaveLaterState();
        button.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event event) {
                getDialogSettings().put(SAVE_LATER, saveLater);
            }
        });
    }

    protected void updateSaveLaterState() {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                for (Control inputControl : inputControls) {
                    if (!inputControl.isDisposed())
                        inputControl.setEnabled(!saveLater);
                }
            }
        });
    }

    protected void addInputControl(Control control) {
        inputControls.add(control);
    }

    protected void createFileNameControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(WizardMessages.NewPage_FileName_label);

        final Text fileNameText = new Text(composite, SWT.BORDER | SWT.SINGLE);
        addInputControl(fileNameText);
        fileNameText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fileNameText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                fileName = fileNameText.getText();
                updateAll();
            }
        });
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (!fileNameText.isDisposed() && fileNameText.isEnabled())
                    fileNameText.setFocus();
            }
        });
    }

    private void validateFileName() {
        if (!isSaveLater()
                && (fileName == null || !fileName
                        .endsWith(MindMapUI.FILE_EXT_XMIND))) {
            setMessage(WizardMessages.NewPage_InvalidExtension_message, WARNING);
        } else {
            setMessage(null, WARNING);
        }
    }

    protected void updateAll() {
        updateSaveLaterState();
        updateWarning();
        updateButtonStates();
    }

    protected void updateWarning() {
        validateFileName();
    }

    protected void updateButtonStates() {
        setPageComplete(isPageCompletable());
    }

    protected boolean isPageCompletable() {
        return saveLater || isSavePathAvailable();
    }

    protected boolean isSavePathAvailable() {
        return fileName != null && !"".equals(fileName); //$NON-NLS-1$
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        updateAll();
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isSaveLater() {
        return saveLater;
    }

}