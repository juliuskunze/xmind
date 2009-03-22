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
package org.xmind.ui.internal.actions;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.ui.actions.EditorAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.CreateSheetCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.WorkbookEditorInput;

public class CreateSheetAction extends EditorAction {

    public CreateSheetAction(IGraphicalEditor editor) {

        super(MindMapActionFactory.NEW_SHEET.getId(), editor);

    }

    public void run() {

        if (isDisposed())
            return;

        IGraphicalEditor ed = getEditor();
        if (ed != null) {
            IWorkbook workbook = (IWorkbook) ed.getAdapter(IWorkbook.class);

            if (workbook == null) {
                IEditorInput input = ed.getEditorInput();
                if (input instanceof WorkbookEditorInput) {
                    workbook = ((WorkbookEditorInput) input).getContents();
                }
            }
            if (workbook == null)
                return;
            saveAndRunCreateSheetCommand(workbook);
        }
    }

    protected void saveAndRunCreateSheetCommand(IWorkbook workbook) {

        CreateSheetCommand command = new CreateSheetCommand(workbook, null);
        command.setLabel(CommandMessages.Command_CreateSheet);
        saveAndRun(command);
        ISheet sheet = (ISheet) command.getSource();
        if (sheet != null) {
            decorateCreatedSheet(sheet);
        }
    }

    protected void decorateCreatedSheet(ISheet sheet) {

        sheet.setTitleText(NLS.bind(MindMapMessages.TitleText_Sheet, sheet
                .getParent().getSheets().size()));

        sheet.getRootTopic().setTitleText(
                MindMapMessages.TitleText_CentralTopic);
    }
}