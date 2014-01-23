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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.xmind.ui.internal.WorkbookFactory;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public class ChooseLocationInWorkspaceWizardPage extends
        AbstractChooseLocationWizardPage {

    private static class ContainerContentProvider implements
            ITreeContentProvider {

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IWorkspace) {
                // check if closed projects should be shown
                IProject[] allProjects = ((IWorkspace) parentElement).getRoot()
                        .getProjects();
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

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private IContainer selectedContainer;

    public ChooseLocationInWorkspaceWizardPage() {
        super();
        setDescription(WizardMessages.NewPage_description);
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

        createContainerViewer(composite);
        createFileNameControl(composite);
        createSaveLaterButton(composite);
        updateAll();
    }

    private void createContainerViewer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(WizardMessages.NewPage_SelectParent_label);

        final TreeViewer containerViewer = new TreeViewer(parent, SWT.BORDER
                | SWT.SINGLE);
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

        addInputControl(containerViewer.getTree());

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
        updateAll();
    }

    protected boolean isSavePathAvailable() {
        return super.isSavePathAvailable() && selectedContainer != null;
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

    public IEditorInput createEditorInput(InputStream templateStream)
            throws CoreException {
        if (!isSavePathAvailable())
            return null;
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

    private IFile createFile(IPath fullPath) {
        IFile file = selectedContainer.getWorkspace().getRoot()
                .getFile(fullPath);

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