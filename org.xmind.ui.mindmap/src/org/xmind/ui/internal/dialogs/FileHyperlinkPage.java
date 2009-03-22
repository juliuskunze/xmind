package org.xmind.ui.internal.dialogs;

import java.io.File;
import java.net.URI;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.dialogs.HyperlinkPage;
import org.xmind.ui.internal.protocols.FileProtocol;

public class FileHyperlinkPage extends HyperlinkPage implements Listener {

    private Composite composite;

    private Text pathInput;

    private Button fileChooser;

    private Button folderChooser;

    private boolean isModifyingValue = false;

    public FileHyperlinkPage() {
    }

    public void init(IStructuredSelection selection) {
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        createLabel(composite);
        createPathInput(composite);
        createPathChoosers(composite);

    }

    /**
     * @param parent
     */
    private void createLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) label.getLayoutData()).widthHint = 380;
        label
                .setText(DialogMessages.FileHyperlinkPage_label);
    }

    /**
     * @param parent
     */
    private void createPathInput(Composite parent) {
        pathInput = new Text(composite, SWT.SINGLE | SWT.BORDER);
        pathInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        pathInput.addListener(SWT.Modify, this);
    }

    /**
     * @param parent
     */
    private void createPathChoosers(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createFileChooser(composite);
        createFolderChooser(composite);

        int maxWidth = 98;
        for (Control c : composite.getChildren()) {
            if (c instanceof Button) {
                maxWidth = Math.max(maxWidth, c.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT).x);
            }
        }
        for (Control c : composite.getChildren()) {
            if (c instanceof Button && c.getLayoutData() instanceof GridData) {
                ((GridData) c.getLayoutData()).widthHint = maxWidth;
            }
        }
    }

    /**
     * @param parent
     */
    private void createFileChooser(Composite parent) {
        fileChooser = new Button(parent, SWT.PUSH);
        fileChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        fileChooser.setText(DialogMessages.FileHyperlinkPage_ChooseFile_text);
        fileChooser.addListener(SWT.Selection, this);
    }

    /**
     * @param parent
     */
    private void createFolderChooser(Composite parent) {
        folderChooser = new Button(parent, SWT.PUSH);
        folderChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        folderChooser.setText(DialogMessages.FileHyperlinkPage_ChooseFolder_text);
        folderChooser.addListener(SWT.Selection, this);
    }

    private String toFileURL() {
        File file = new File(pathInput.getText());
        URI uri = file.toURI();
        return uri.toString();
    }

    public void setValue(String value) {
        super.setValue(value);
        if (!isModifyingValue) {
            if (pathInput != null && !pathInput.isDisposed()) {
                if (value == null) {
                    pathInput.setText(""); //$NON-NLS-1$
                } else {
                    pathInput.setText(toFilePath(value));
                }
            }
        }
    }

    private String toFilePath(String url) {
        return FileProtocol.toFilePath(url);
    }

    public void dispose() {
    }

    public Control getControl() {
        return composite;
    }

    public void setFocus() {
        if (pathInput != null && !pathInput.isDisposed()) {
            pathInput.setFocus();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(Event event) {
        if (event.widget == pathInput) {
            isModifyingValue = true;
            setValue(toFileURL());
            isModifyingValue = false;
            boolean exists = new File(pathInput.getText()).exists();
            setCanFinish(exists);
            setErrorMessage(exists ? null : DialogMessages.FileHyperlinkPage_FileNotExists_message);
        } else if (event.widget == fileChooser) {
            FileDialog fd = new FileDialog(composite.getShell(), SWT.OPEN
                    | SWT.SINGLE);
            fd.setText(DialogMessages.FileHyperlinkPage_OpenFileDialog_windowTitle);
            String path = fd.open();
            if (path != null) {
                pathInput.setText(path);
                pathInput.setFocus();
            }
        } else if (event.widget == folderChooser) {
            DirectoryDialog dd = new DirectoryDialog(composite.getShell(),
                    SWT.SINGLE | SWT.OPEN);
            dd.setText(DialogMessages.FileHyperlinkPage_OpenDirectoryDialog_windowTitle);
            String path = dd.open();
            if (path != null) {
                pathInput.setText(path);
                pathInput.setFocus();
            }
        }
    }

}
