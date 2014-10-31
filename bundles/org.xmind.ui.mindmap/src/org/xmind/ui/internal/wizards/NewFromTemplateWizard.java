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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.core.IFileEntryFilter;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.internal.zip.ZipStreamOutputTarget;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public class NewFromTemplateWizard extends Wizard implements INewWizard {

    private static final String SECTION_NAME = "org.xmind.ui.newWizard"; //$NON-NLS-1$

    private IWorkbench workbench;

    private IStructuredSelection selection;

    private ChooseTemplateWizardPage templatePage;

    public NewFromTemplateWizard() {
        setWindowTitle(WizardMessages.NewWizard_windowTitle);
        setDialogSettings(MindMapUIPlugin.getDefault().getDialogSettings(
                SECTION_NAME));
        setNeedsProgressMonitor(false);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }

    protected IWorkbench getWorkbench() {
        return workbench;
    }

    protected IStructuredSelection getSelection() {
        return selection;
    }

    public void addPages() {
        addPage(templatePage = new ChooseTemplateWizardPage());
    }

//    public boolean performFinish() {
//        if (workbench == null)
//            return false;
//
//        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
//        new NewFromMoreTemplateAction(window).run();
//        return true;
//    }

    public boolean performFinish() {
        if (getWorkbench() == null)
            return false;

        IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return false;

        final IWorkbenchPage workbenchPage = window.getActivePage();
        if (workbenchPage == null)
            return false;

        final ITemplateDescriptor template = templatePage.getTemplate();

        final IEditorPart[] editor = new IEditorPart[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final IEditorInput editorInput = createEditorInput(template);
                if (editorInput != null) {
                    editor[0] = workbenchPage.openEditor(editorInput,
                            MindMapUI.MINDMAP_EDITOR_ID);
                }
            }
        });
        return editor[0] != null;
    }

    private IEditorInput createEditorInput(final ITemplateDescriptor template)
            throws CoreException {
        InputStream templateStream = null;
        if (template != null) {
            templateStream = template.newStream();
            templateStream = removeRevisions(templateStream);
        }
        return createEditorInput(templateStream);
    }

    protected IEditorInput createEditorInput(InputStream templateStream)
            throws CoreException {
        if (templateStream != null) {
            return MME.createTemplatedEditorInput(null, templateStream);
        }
        return MME.createNonExistingEditorInput();
    }

    private InputStream removeRevisions(InputStream templateStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            FileUtils.extractZipStream(templateStream,
                    new ZipStreamOutputTarget(new ZipOutputStream(buffer)),
                    new IFileEntryFilter() {
                        public boolean select(String path, String mediaType,
                                boolean isDirectory) {
                            return !path
                                    .startsWith(ArchiveConstants.PATH_REVISIONS);
                        }
                    });
        } catch (Exception e) {
            return templateStream;
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }

}