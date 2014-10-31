package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.style.IStyled;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyStyleCommand;

public class ResetStyleHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (activeEditor instanceof IGraphicalEditor) {
            IGraphicalEditor editor = (IGraphicalEditor) activeEditor;
            resetStyle(editor);
        }
        return null;
    }

    private void resetStyle(IGraphicalEditor editor) {
        IGraphicalEditorPage activePageInstance = editor
                .getActivePageInstance();
        if (activePageInstance == null)
            return;

        ISelectionProvider selectionProvider = activePageInstance
                .getSelectionProvider();
        if (selectionProvider == null)
            return;

        ISelection selection = selectionProvider.getSelection();
        if (!(selection instanceof StructuredSelection))
            return;

        Object[] resetedStyleds = ((StructuredSelection) selection).toArray();
        if (resetedStyleds != null) {
            for (Object styled : resetedStyleds) {
                if (styled instanceof IStyled) {
                    IStyled resetedStyled = (IStyled) styled;
                    ModifyStyleCommand modifyStyleCommand = new ModifyStyleCommand(
                            resetedStyled, (String) null);
                    modifyStyleCommand
                            .setLabel(CommandMessages.Command_ModifyStyle);
                    editor.getCommandStack().execute(modifyStyleCommand);
                }
            }
        }
    }

}
