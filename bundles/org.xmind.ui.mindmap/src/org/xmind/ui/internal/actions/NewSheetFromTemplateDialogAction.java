package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.IWorkbook;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.dialogs.NewSheetFromTemplateDialog;
import org.xmind.ui.internal.editor.MindMapEditor;

public class NewSheetFromTemplateDialogAction extends PageAction implements
        ISelectionAction {
    IWorkbook currentWorkbook = null;
    IGraphicalEditor editor = null;

    public NewSheetFromTemplateDialogAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.NEW_SHEET_FROM_TEMPLATE.getId(), page);
        IGraphicalEditor editor = page.getParentEditor();
        this.editor = editor;
        currentWorkbook = ((MindMapEditor) editor).getWorkbook();
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return;

        new NewSheetFromTemplateDialog(currentWorkbook, editor).open();
    }

    public void setSelection(ISelection selection) {
        setEnabled(true);
    }

}
