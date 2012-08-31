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
package org.xmind.ui.internal.dialogs;

import java.io.File;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.xmind.ui.dialogs.HyperlinkPage;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.protocols.FilePathParser;

/**
 * 
 * @author Frank Shaka
 */
public class FileHyperlinkPage extends HyperlinkPage implements Listener {

    private IEditorPart editor;

    private String basePath;

    private boolean relative;

    private File file;

    private Composite composite;

    private Text pathInput;

    private Button relativeButton;

    private Button absoluteButton;

    private Button fileChooser;

    private Button folderChooser;

    private boolean ignoreModify = false;

    private boolean warningFileNotExists = false;

    private boolean warningRelative = false;

    public FileHyperlinkPage() {
    }

    public void init(IEditorPart editor, IStructuredSelection selection) {
        this.editor = editor;
        File workbookFile = MME.getFile(editor.getEditorInput());
        if (workbookFile != null) {
            this.basePath = workbookFile.getParent();
        } else {
            this.basePath = null;
        }
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        createLabel(composite);
        createPathInput(composite);
        createOptionsArea(composite);
        createPathChoosers(composite);
    }

    private void createLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) label.getLayoutData()).widthHint = 380;
        label.setText(DialogMessages.FileHyperlinkPage_label);
    }

    private void createPathInput(Composite parent) {
        pathInput = new Text(parent, SWT.SINGLE | SWT.BORDER);
        pathInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        pathInput.addListener(SWT.Modify, this);
    }

    private void createOptionsArea(Composite parent) {
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(2, true);

        group.setLayout(layout);
        group.setText(DialogMessages.FileHyperlinkPage_HrefGroup_Text);

        absoluteButton = new Button(group, SWT.RADIO);
        absoluteButton.setLayoutData(layoutData);
        absoluteButton
                .setText(DialogMessages.FileHyperlinkPage_AbsoluteButton_Text);
        absoluteButton.setSelection(true);
        absoluteButton.addListener(SWT.Selection, this);

        relativeButton = new Button(group, SWT.RADIO);
        relativeButton.setLayoutData(layoutData);
        relativeButton
                .setText(DialogMessages.FileHyperlinkPage_RelativeButton_Text);
        relativeButton.addListener(SWT.Selection, this);
    }

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
                maxWidth = Math.max(maxWidth,
                        c.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
            }
        }
        for (Control c : composite.getChildren()) {
            if (c instanceof Button && c.getLayoutData() instanceof GridData) {
                ((GridData) c.getLayoutData()).widthHint = maxWidth;
            }
        }
    }

    private void createFileChooser(Composite parent) {
        fileChooser = new Button(parent, SWT.PUSH);
        fileChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        fileChooser.setText(DialogMessages.FileHyperlinkPage_ChooseFile_text);
        fileChooser.addListener(SWT.Selection, this);
    }

    private void createFolderChooser(Composite parent) {
        folderChooser = new Button(parent, SWT.PUSH);
        folderChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        folderChooser
                .setText(DialogMessages.FileHyperlinkPage_ChooseFolder_text);
        folderChooser.addListener(SWT.Selection, this);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        String path = value == null ? null : FilePathParser.toPath(value);
        relative = path == null ? false : FilePathParser.isPathRelative(path);
        file = getFile(path);
        if (pathInput != null && !pathInput.isDisposed()) {
            ignoreModify = true;
            pathInput
                    .setText(file == null ? (path == null ? "" : path) : file.getAbsolutePath()); //$NON-NLS-1$
            ignoreModify = false;
        }
        if (absoluteButton != null && !absoluteButton.isDisposed()) {
            absoluteButton.setSelection(!relative);
        }
        if (relativeButton != null && !relativeButton.isDisposed()) {
            relativeButton.setSelection(relative);
        }
    }

    private File getFile(String path) {
        if (path == null)
            return null;
        if (relative)
            return basePath == null ? new File(System.getProperty("user.home"), //$NON-NLS-1$
                    path) : new File(FilePathParser.toAbsolutePath(basePath,
                    path));
        return new File(path);
    }

    @Override
    public boolean tryFinish() {
        if (file != null && relative) {
            if (basePath == null) {
                editor.doSaveAs();
                File newFilePath = MME.getFile(editor.getEditorInput());
                if (newFilePath == null)
                    return false;
                basePath = newFilePath.getParent();
                String relativePath = FilePathParser.toRelativePath(basePath,
                        file.getAbsolutePath());
                if (relativePath != null) {
                    super.setValue(FilePathParser.toURI(relativePath, relative));
                }
//                String workbookPath = openSaveDialog();
//                if (workbookPath == null)
//                    return false;
//
//                basePath = new File(workbookPath).getParent();
//                if (basePath == null)
//                    return false;
//
//                String relativePath = FilePathParser.toRelativePath(basePath,
//                        file.getAbsolutePath());
//                if (relativePath != null) {
//                    saveWorkbook(workbookPath);
//                    super
//                            .setValue(FilePathParser.toURI(relativePath,
//                                    relative));
//                }
            }
        }
        return super.tryFinish();
    }

//    private String openSaveDialog() {
//        IWorkbook workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);
//        String name = workbook.getPrimarySheet().getRootTopic().getTitleText();
//        String proposalName = MindMapUtils.trimFileName(name);
//        return DialogUtils.save(composite.getShell(), proposalName,
//                new String[] { "*" + MindMapUI.FILE_EXT_XMIND }, //$NON-NLS-1$
//                new String[] { DialogMessages.WorkbookFilterName }, 0, null);
//    }
//
//    private void saveWorkbook(final String path) {
//        if (path != null) {
//            final IWorkbookRef workbookRef = (IWorkbookRef) editor
//                    .getAdapter(IWorkbookRef.class);
//            if (workbookRef != null && workbookRef instanceof WorkbookRef) {
//                BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
//                    public void run() {
//                        final String errorMessage = NLS.bind(
//                                DialogMessages.FailedToSaveWorkbook_message,
//                                path);
//                        SafeRunner.run(new SafeRunnable(errorMessage) {
//                            public void run() throws Exception {
//                                ((WorkbookRef) workbookRef).saveWorkbookAs(
//                                        MME.createFileEditorInput(path),
//                                        new NullProgressMonitor(), null);
//                            }
//                        });
//                    }
//                });
//            }
//        }
//    }

    public void handleEvent(Event event) {
        if (event.widget == pathInput) {
            if (event.type == SWT.Modify) {
                if (!ignoreModify) {
                    setFile(pathInput.getText(), false);
                }
            }
        } else if (event.widget == fileChooser) {
            FileDialog dialog = createFileDialog();
            String path = dialog.open();
            if (path != null) {
                setFile(path, true);
            }
        } else if (event.widget == folderChooser) {
            DirectoryDialog dialog = createFolderDialog();
            String path = dialog.open();
            if (path != null) {
                setFile(path, true);
            }
        } else if (event.widget == relativeButton) {
            setRelative(true);
        } else if (event.widget == absoluteButton) {
            setRelative(false);
        }
    }

    protected DirectoryDialog createFolderDialog() {
        DirectoryDialog dialog = new DirectoryDialog(composite.getShell(),
                SWT.OPEN | SWT.SINGLE);
        dialog.setText(DialogMessages.FileHyperlinkPage_OpenFileDialog_windowTitle);
        return dialog;
    }

    protected FileDialog createFileDialog() {
        FileDialog dialog = new FileDialog(composite.getShell(), SWT.OPEN
                | SWT.SINGLE);
        dialog.setText(DialogMessages.FileHyperlinkPage_OpenFileDialog_windowTitle);
        return dialog;
    }

    protected void setFile(String path, boolean updateWidget) {
        boolean invalidPath = path == null || "".equals(path); //$NON-NLS-1$
        file = invalidPath ? null : new File(path);
        super.setValue(getURI());
        setCanFinish(!invalidPath);
        warningFileNotExists = !invalidPath && file != null && !file.exists();
        updateWarningMessage();
        if (updateWidget && pathInput != null && !pathInput.isDisposed()) {
            ignoreModify = true;
            pathInput.setText(path);
            ignoreModify = false;
        }
    }

    protected void setRelative(boolean relative) {
        this.relative = relative;
        warningRelative = (relative && basePath == null);
        super.setValue(getURI());
        updateWarningMessage();
    }

    private void updateWarningMessage() {
        setMessage(
                warningFileNotExists ? DialogMessages.FileHyperlinkPage_FileNotExists_message
                        : (warningRelative ? DialogMessages.FileHyperlinkPage_RelativeWarning_message
                                : null), WARNING);
    }

    private String getURI() {
        if (file == null)
            return null;
        if (relative) {
            if (basePath != null)
                return FilePathParser.toURI(
                        FilePathParser.toRelativePath(basePath,
                                file.getAbsolutePath()), relative);
        }
        return FilePathParser.toURI(file.getAbsolutePath(), relative);
    }

    public void dispose() {
    }

    public Control getControl() {
        return composite;
    }

    public void setFocus() {
        if (pathInput != null && !pathInput.isDisposed())
            pathInput.setFocus();
    }
}
