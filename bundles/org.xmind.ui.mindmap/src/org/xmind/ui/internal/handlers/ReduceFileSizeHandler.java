package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.dialogs.ReduceFileSizeDialog;

public class ReduceFileSizeHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor == null)
            return null;

        reduceFileSize(editor);
        return null;
    }

    private void reduceFileSize(final IEditorPart editor) {
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                ReduceFileSizeDialog dialog = new ReduceFileSizeDialog(editor);
                dialog.open();
            }
        });
    }

}
