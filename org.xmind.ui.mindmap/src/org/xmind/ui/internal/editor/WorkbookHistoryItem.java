package org.xmind.ui.internal.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.xmind.ui.mindmap.MindMapUI;

public class WorkbookHistoryItem {

    static final String FILE_PROTOCOL = "file:"; //$NON-NLS-1$

    private IEditorInput input;

    private String uri;

    private long time;

    public WorkbookHistoryItem(IEditorInput input, String uri, long time) {
        this.input = input;
        this.uri = uri;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public String getURI() {
        return this.uri;
    }

    public String getPath() {
        if (uri.startsWith(FILE_PROTOCOL))
            return uri.substring(5);
        return uri;
    }

    void setInput(IEditorInput input) {
        this.input = input;
    }

    public IEditorInput getExistingEditorInput() {
        return this.input;
    }

    public IEditorInput createNewEditorInput() throws CoreException {
        return MME.createFileEditorInput(getPath());
    }

    public void reopen(IWorkbenchPage page) throws CoreException {
        page.openEditor(createNewEditorInput(), MindMapUI.MINDMAP_EDITOR_ID,
                true);
    }

    @Override
    public String toString() {
        return "{time=" + time + ",uri=" + uri + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    static String toURI(String filePath) {
        return FILE_PROTOCOL + filePath;
    }

}
