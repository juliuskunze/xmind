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
package org.xmind.ui.internal.actions;

import java.io.File;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class OpenWorkbookAction extends Action implements IWorkbenchAction {

    private static final String OPEN_DIALOG_SETTINGS_ID = "org.xmind.ui.openDialog"; //$NON-NLS-1$

    private static final String OLD_FILE_EXT = ".xmap"; //$NON-NLS-1$

    private static final String FILTER_INDEX = "filterIndex"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    private IDialogSettings ds;

    public OpenWorkbookAction(IWorkbenchWindow window) {
        super(MindMapMessages.OpenWorkbook_text);
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
        setId("org.xmind.ui.open"); //$NON-NLS-1$
        setImageDescriptor(MindMapUI.getImages().get(IMindMapImages.OPEN, true));
        setDisabledImageDescriptor(MindMapUI.getImages().get(
                IMindMapImages.OPEN, false));
        setToolTipText(MindMapMessages.OpenWorkbook_toolTip);
        setActionDefinitionId("org.xmind.ui.command.openWorkbook"); //$NON-NLS-1$
    }

    public void run() {
        if (window == null)
            return;

        FileDialog fd = new FileDialog(window.getShell(), SWT.OPEN | SWT.MULTI);
        String xmindExt = "*" + MindMapUI.FILE_EXT_XMIND; //$NON-NLS-1$
        String oldExt = "*" + OLD_FILE_EXT; //$NON-NLS-1$
        String allSupportedFileExt = String.format("%s;%s", //$NON-NLS-1$
                xmindExt, oldExt);
        String allExt = "*.*"; //$NON-NLS-1$
        fd.setFilterExtensions(new String[] { xmindExt, oldExt,
                allSupportedFileExt, allExt });
        fd.setFilterNames(new String[] {
                NLS.bind("{0} ({1})", DialogMessages.WorkbookFilterName, //$NON-NLS-1$
                        xmindExt),
                NLS.bind("{0} ({1})", DialogMessages.OldWorkbookFilterName, //$NON-NLS-1$
                        oldExt),
                NLS.bind("{0} ({1}, {2})", //$NON-NLS-1$
                        new Object[] {
                                DialogMessages.AllSupportedFilesFilterName,
                                xmindExt, oldExt }),
                NLS.bind("{0} ({1})", DialogMessages.AllFilesFilterName, //$NON-NLS-1$
                        allExt) });
        fd.setFilterIndex(getFilterIndex());
        if (fd.open() == null)
            return;

        String path = fd.getFilterPath();
        String[] fileNames = fd.getFileNames();
        setFilterIndex(fd.getFilterIndex());
        for (String f : fileNames) {
            File file = new File(path, f);
            if (file.exists()) {
                open(file);
            }
        }
    }

    private void setFilterIndex(int index) {
        getDialogSettings().put(FILTER_INDEX, index);
    }

    private int getFilterIndex() {
        try {
            int filterIndex = getDialogSettings().getInt(FILTER_INDEX);
            if (filterIndex >= 0 && filterIndex <= 2)
                return filterIndex;
        } catch (NumberFormatException ignore) {
        }
        return 2;
    }

    private IDialogSettings getDialogSettings() {
        if (ds == null) {
            IDialogSettings global = MindMapUIPlugin.getDefault()
                    .getDialogSettings();
            ds = global.getSection(OPEN_DIALOG_SETTINGS_ID);
            if (ds == null) {
                ds = global.addNewSection(OPEN_DIALOG_SETTINGS_ID);
            }
        }
        return ds;
    }

    protected void open(final File file) {
        String errMessage = NLS.bind(
                DialogMessages.FailedToLoadWorkbook_message, file
                        .getAbsolutePath());
        SafeRunner.run(new SafeRunnable(errMessage) {
            public void run() throws Exception {
                IEditorInput input = MME.createFileEditorInput(file);
                window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }

    public void dispose() {
        window = null;
    }

}