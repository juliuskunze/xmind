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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.WorkbookFactory;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class ChooseLocationInFileSystemWizardPage extends
        AbstractChooseLocationWizardPage {

    private static class FolderContentProvider implements ITreeContentProvider {

        private Object input;

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof File) {
                File parent = (File) parentElement;
                if (parent.isDirectory()) {
                    File[] subFiles = parent.listFiles();
                    if (subFiles != null) {
                        List<Object> list = new ArrayList<Object>(
                                subFiles.length);
                        for (File child : subFiles) {
                            if (child.isDirectory() && !child.isHidden()
                                    && child.canRead()) {
                                list.add(child);
                            }
                        }
                        return list.toArray();
                    }
                }
            } else if (parentElement instanceof File[]) {
                return (File[]) parentElement;
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof File) {
                File parent = ((File) element).getParentFile();
                if (parent == null)
                    return input;
                return parent;
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            return true;//getChildren(element).length > 0;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            this.input = newInput;
        }

    }

    private static class FolderLabelProvider extends LabelProvider {
        private Image folderImage;

        public String getText(Object element) {
            if (element instanceof File) {
                File file = (File) element;
                if (file.getParentFile() == null)
                    return file.getAbsolutePath();
                return file.getName();
            }
            return super.getText(element);
        }

        public Image getImage(Object element) {
            if (folderImage == null) {
                folderImage = MindMapUI.getImages()
                        .get(IMindMapImages.OPEN, true).createImage(false);
            }
            return folderImage;
        }

        public void dispose() {
            if (folderImage != null) {
                folderImage.dispose();
                folderImage = null;
            }
            super.dispose();
        }
    }

    private File selectedFolder;

    public ChooseLocationInFileSystemWizardPage() {
        super();
        setDescription(WizardMessages.NewPage2_description);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 5;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);
        setControl(composite);

        createFolderViewer(composite);
        createFileNameControl(composite);
        createSaveLaterButton(composite);
        updateAll();
    }

    private void createFolderViewer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(WizardMessages.NewPage_SelectParent_label);

        final TreeViewer folderViewer = new TreeViewer(parent, SWT.BORDER
                | SWT.SINGLE);
        folderViewer.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        folderViewer.setContentProvider(new FolderContentProvider());
        folderViewer.setLabelProvider(new FolderLabelProvider());
        folderViewer.setComparator(new ViewerComparator());
        folderViewer.setUseHashlookup(true);
        folderViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        IStructuredSelection selection = (IStructuredSelection) event
                                .getSelection();
                        folderSelectionChanged((File) selection
                                .getFirstElement());
                    }
                });
        folderViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object item = ((IStructuredSelection) selection)
                            .getFirstElement();
                    if (item == null) {
                        return;
                    }
                    if (folderViewer.getExpandedState(item)) {
                        folderViewer.collapseToLevel(item, 1);
                    } else {
                        folderViewer.expandToLevel(item, 1);
                    }
                }
            }
        });
        addInputControl(folderViewer.getTree());

        parent.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (folderViewer.getControl().isDisposed())
                    return;

                folderViewer.setInput(File.listRoots());
                File homeDir = new File(System.getProperty("user.home")); //$NON-NLS-1$
                folderViewer.setSelection(new StructuredSelection(homeDir),
                        true);
                folderViewer.setExpandedState(homeDir, true);
                updateButtonStates();
            }
        });

    }

    private void folderSelectionChanged(File folder) {
        this.selectedFolder = folder;
        updateAll();
    }

    @Override
    protected boolean isSavePathAvailable() {
        return super.isSavePathAvailable() && selectedFolder != null;
    }

    public IEditorInput createEditorInput(InputStream templateStream)
            throws CoreException {
        if (!isSavePathAvailable())
            return null;
        String name = getFileName();
        if (!name.endsWith(MindMapUI.FILE_EXT_XMIND))
            name += MindMapUI.FILE_EXT_XMIND;
        File file = new File(selectedFolder, name);
        FileUtils.ensureFileParent(file);
        IWorkbook workbook = WorkbookFactory.createEmptyWorkbook();
        try {
            workbook.save(new FileOutputStream(file));
        } catch (Exception e) {
        }
        return MME.createFileEditorInput(file);
        //return new WorkbookEditorInput(workbook, path);
    }

    public void setWorkbenchSelection(IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if (element instanceof File) {
            folderSelectionChanged((File) element);
        }
    }

    public String getParentPath() {
        return selectedFolder == null ? null : selectedFolder.getAbsolutePath();
    }
}