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
package org.xmind.ui.internal.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.WorkbookFactory;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class NewWorkbookWizard extends Wizard implements INewWizard {

    private static final String PAGE_NAME = "org.xmind.ui.newWizardPage"; //$NON-NLS-1$

    private static abstract class NewWizardPage extends WizardPage {

        private Text fileNameText;

        private String fileName;

        protected NewWizardPage(String pageName, String title,
                ImageDescriptor titleImage) {
            super(pageName, title, titleImage);
        }

        protected NewWizardPage(String pageName) {
            super(pageName);
        }

        public abstract void setWorkbenchSelection(
                IStructuredSelection selection);

        public abstract IEditorInput createEditorInput() throws CoreException;

        public abstract String getParentPath();

        protected void createFileNameControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));
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

            fileNameText = new Text(composite, SWT.BORDER | SWT.SINGLE);
            fileNameText.addListener(SWT.Modify, new Listener() {
                public void handleEvent(Event event) {
                    fileName = fileNameText.getText();
                    validFileName();
                    updateButtonStates();
                }
            });
            fileNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    if (!fileNameText.isDisposed())
                        fileNameText.setFocus();
                }
            });
        }

        private void validFileName() {
            String fileName = fileNameText.getText();
            if (!fileName.endsWith(MindMapUI.FILE_EXT_XMIND)) {
                setMessage(WizardMessages.NewPage_InvalidExtension_message,
                        WARNING);
            } else {
                setMessage(null, WARNING);
            }
        }

        protected void updateButtonStates() {
            setPageComplete(isPageCompletable());
        }

        protected boolean isPageCompletable() {
            return fileName != null && !"".equals(fileName); //$NON-NLS-1$
        }

        public String getFileName() {
            return fileName;
        }
    }

    private static class NewWorkbookInWorkspacePage extends NewWizardPage {

        private static class ContainerContentProvider implements
                ITreeContentProvider {

            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof IWorkspace) {
                    // check if closed projects should be shown
                    IProject[] allProjects = ((IWorkspace) parentElement)
                            .getRoot().getProjects();
                    return allProjects;
                } else if (parentElement instanceof IContainer) {
                    IContainer container = (IContainer) parentElement;
                    if (container.isAccessible()) {
                        try {
                            List<Object> children = new ArrayList<Object>();
                            IResource[] members = container.members();
                            for (int i = 0; i < members.length; i++) {
                                if (members[i].getType() != IResource.FILE) {
                                    children.add(members[i]);
                                }
                            }
                            return children.toArray();
                        } catch (CoreException ignore) {
                        }
                    }
                }
                return new Object[0];
            }

            public Object getParent(Object element) {
                if (element instanceof IResource) {
                    return ((IResource) element).getParent();
                }
                return null;
            }

            public boolean hasChildren(Object element) {
                return getChildren(element).length > 0;
            }

            public Object[] getElements(Object inputElement) {
                return getChildren(inputElement);
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

        }

        private TreeViewer containerViewer;

        private IContainer selectedContainer;

        public NewWorkbookInWorkspacePage() {
            super(PAGE_NAME, WizardMessages.NewPage_title, null);
            setDescription(WizardMessages.NewPage_description);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite
                    .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 5;
            gridLayout.horizontalSpacing = 5;
            composite.setLayout(gridLayout);
            setControl(composite);

            createContainerViewer(composite);
            createFileNameControl(composite);
            updateButtonStates();
        }

        private void createContainerViewer(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            label.setText(WizardMessages.NewPage_SelectParent_label);

            containerViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE);
            containerViewer.getControl().setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));
            containerViewer.setContentProvider(new ContainerContentProvider());
            containerViewer.setLabelProvider(WorkbenchLabelProvider
                    .getDecoratingWorkbenchLabelProvider());
            containerViewer.setComparator(new ViewerComparator());
            containerViewer.setUseHashlookup(true);
            containerViewer
                    .addSelectionChangedListener(new ISelectionChangedListener() {
                        public void selectionChanged(SelectionChangedEvent event) {
                            IStructuredSelection selection = (IStructuredSelection) event
                                    .getSelection();
                            containerSelectionChanged((IContainer) selection
                                    .getFirstElement()); // allow null
                        }
                    });
            containerViewer.addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event) {
                    ISelection selection = event.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        Object item = ((IStructuredSelection) selection)
                                .getFirstElement();
                        if (item == null) {
                            return;
                        }
                        if (containerViewer.getExpandedState(item)) {
                            containerViewer.collapseToLevel(item, 1);
                        } else {
                            containerViewer.expandToLevel(item, 1);
                        }
                    }
                }
            });

            parent.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    containerViewer.setInput(ResourcesPlugin.getWorkspace());
                    if (selectedContainer != null) {
                        containerViewer.setSelection(new StructuredSelection(
                                selectedContainer), true);
                    }
                    updateButtonStates();
                }
            });
        }

        public void containerSelectionChanged(IContainer container) {
            selectedContainer = container;
            updateButtonStates();
        }

        protected boolean isPageCompletable() {
            return super.isPageCompletable() && selectedContainer != null;
        }

        public void setWorkbenchSelection(IStructuredSelection selection) {
            Object element = selection.getFirstElement();
            if (element instanceof IContainer) {
                containerSelectionChanged((IContainer) element);
            } else if (element instanceof IAdaptable) {
                IResource resource = (IResource) ((IAdaptable) element)
                        .getAdapter(IResource.class);
                if (resource instanceof IContainer) {
                    containerSelectionChanged((IContainer) resource);
                } else if (resource != null) {
                    containerSelectionChanged(resource.getParent());
                }
            }
        }

        public IEditorInput createEditorInput() throws CoreException {
            if (selectedContainer != null) {
                String name = getFileName();
                if (!name.endsWith(MindMapUI.FILE_EXT_XMIND)) {
                    name += MindMapUI.FILE_EXT_XMIND;
                }
                IPath fullPath = selectedContainer.getFullPath().append(name);
                IFile file = createFile(fullPath);
                return MME.createFileEditorInput(file);
//                IWorkbook workbook = WorkbookFactory.createEmptyWorkbook(file
//                        .getLocation().toOSString());
//                return new WorkbookEditorInput(workbook, file);
            }
            return null;
        }

        private IFile createFile(IPath fullPath) {
            IFile file = selectedContainer.getWorkspace().getRoot().getFile(
                    fullPath);

            try {
                file.create(WorkbookFactory.createEmptyWorkbookStream(), false,
                        new NullProgressMonitor());
            } catch (CoreException e) {
            }
            return file;
        }

        public String getParentPath() {
            return null;
        }
    }

    private static class NewWorkbookInFileSystemPage extends NewWizardPage {

        private static class FolderContentProvider implements
                ITreeContentProvider {

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

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
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
                    folderImage = MindMapUI.getImages().get(
                            IMindMapImages.OPEN, true).createImage(false);
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

        private TreeViewer folderViewer;

        public NewWorkbookInFileSystemPage() {
            super(PAGE_NAME, WizardMessages.NewPage_title, null);
            setDescription(WizardMessages.NewPage2_description);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite
                    .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 5;
            gridLayout.horizontalSpacing = 5;
            composite.setLayout(gridLayout);
            setControl(composite);

            createFolderViewer(composite);
            createFileNameControl(composite);
            updateButtonStates();
        }

        private void createFolderViewer(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            label.setText(WizardMessages.NewPage_SelectParent_label);

            folderViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE);
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

            parent.getDisplay().asyncExec(new Runnable() {
                public void run() {
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
            updateButtonStates();
        }

        protected boolean isPageCompletable() {
            return super.isPageCompletable() && selectedFolder != null;
        }

        public IEditorInput createEditorInput() throws CoreException {
            if (selectedFolder == null)
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
            return selectedFolder == null ? null : selectedFolder
                    .getAbsolutePath();
        }
    }

    private static final String SECTION_NAME = "org.xmind.ui.newWizard"; //$NON-NLS-1$

    private static final String LAST_FOLDER = "lastSelectedFolder"; //$NON-NLS-1$

    private NewWizardPage page;

    private IWorkbench workbench;

    private IStructuredSelection selection;

    public NewWorkbookWizard() {
        setWindowTitle(WizardMessages.NewWizard_windowTitle);
        setDialogSettings(MindMapUIPlugin.getDefault().getDialogSettings(
                SECTION_NAME));
    }

    public void dispose() {
        String path = page.getParentPath();
        if (path != null)
            getDialogSettings().put(LAST_FOLDER, path);
        super.dispose();
    }

    public void addPages() {
        IProduct product = Platform.getProduct();
        IStructuredSelection initSelection = this.selection;
        if (product == null
                || "org.xmind.cathy.application".equals(product.getApplication())) { //$NON-NLS-1$
            addPage(page = new NewWorkbookInFileSystemPage());
            String lastFolder = getDialogSettings().get(LAST_FOLDER);
            if (lastFolder != null && !"".equals(lastFolder)) { //$NON-NLS-1$
                initSelection = new StructuredSelection(new File(lastFolder));
            }
        } else {
            addPage(page = new NewWorkbookInWorkspacePage());
        }
        page.setWorkbenchSelection(initSelection);
    }

    public boolean performFinish() {
        if (workbench == null)
            return false;

        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null)
            return false;

        final IWorkbenchPage workbenchPage = window.getActivePage();
        if (workbenchPage == null)
            return false;

        final IEditorPart[] editor = new IEditorPart[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final IEditorInput editorInput = page.createEditorInput();
                if (editorInput == null)
                    return;

                editor[0] = workbenchPage.openEditor(editorInput,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
        return editor[0] != null;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }

}