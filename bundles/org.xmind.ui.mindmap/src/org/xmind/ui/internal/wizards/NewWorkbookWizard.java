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
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;

public class NewWorkbookWizard extends NewFromTemplateWizard {

    private static final String LAST_FOLDER = "lastSelectedFolder"; //$NON-NLS-1$

    private AbstractChooseLocationWizardPage locationPage;

    public void dispose() {
        String path = locationPage.getParentPath();
        if (path != null)
            getDialogSettings().put(LAST_FOLDER, path);
        super.dispose();
    }

    public void addPages() {
        super.addPages();
        IStructuredSelection initSelection = getSelection();
        IProduct product = Platform.getProduct();
        if (product == null
                || "org.xmind.cathy.application".equals(product.getApplication())) { //$NON-NLS-1$
            addPage(locationPage = new ChooseLocationInFileSystemWizardPage());
            String lastFolder = getDialogSettings().get(LAST_FOLDER);
            if (lastFolder != null && !"".equals(lastFolder)) { //$NON-NLS-1$
                initSelection = new StructuredSelection(new File(lastFolder));
            }
        } else {
            addPage(locationPage = new ChooseLocationInWorkspaceWizardPage());
        }
        locationPage.setWorkbenchSelection(initSelection);
    }

    @Override
    protected IEditorInput createEditorInput(InputStream templateStream)
            throws CoreException {
        if (locationPage.isSaveLater()) {
            return super.createEditorInput(templateStream);
        }
        return locationPage.createEditorInput(templateStream);
    }

}