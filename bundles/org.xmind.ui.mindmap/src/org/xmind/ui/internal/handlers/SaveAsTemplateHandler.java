package org.xmind.ui.internal.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.MindMapTemplateManager;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.editor.MindMapEditor;
import org.xmind.ui.mindmap.MindMapUI;

public class SaveAsTemplateHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor instanceof MindMapEditor) {
            saveAsTemplate((MindMapEditor) editor);
        }
        return null;
    }

    private void saveAsTemplate(final MindMapEditor targetEditor) {
        if (targetEditor == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                targetEditor.doSaveAs(new NullProgressMonitor(),
                        MindMapUI.FILE_EXT_TEMPLATE,
                        DialogMessages.TemplateFilterName);
                IEditorInput input = targetEditor.getEditorInput();
                if (input != null) {
                    File file = MME.getFile(input);
                    if (file != null) {
                        saveTemplateFromFile(file);
                    }
                }
            }
        });

    }

    private void saveTemplateFromFile(File file) {
        MindMapTemplateManager.getInstance().importCustomTemplate(
                file.getAbsolutePath());
    }

}
