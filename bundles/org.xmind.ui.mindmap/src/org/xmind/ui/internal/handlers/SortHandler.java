package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.actions.ActionConstants;

public class SortHandler extends AbstractHandler {

    private static final String MODIFIED = "modified"; //$NON-NLS-1$
    private static final String PRIORITY = "priority"; //$NON-NLS-1$
    private static final String TITLE = "title"; //$NON-NLS-1$
    private static final String PARAMETER_ID = "org.xmind.ui.sortParameter"; //$NON-NLS-1$

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor instanceof IGraphicalEditor) {
            IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                    .getActivePageInstance();
            if (page != null) {
                EditDomain domain = page.getEditDomain();
                if (domain != null) {
                    String value = event.getParameter(PARAMETER_ID);
                    domain.handleRequest(new Request(GEF.REQ_SORT).setViewer(
                            page.getViewer()).setParameter(GEF.PARAM_COMPARAND,
                            getSortId(value)));
                }
            }
        }
        return null;
    }

    private String getSortId(String value) {
        if (TITLE.equals(value)) {
            return ActionConstants.SORT_TITLE_ID;
        } else if (PRIORITY.equals(value)) {
            return ActionConstants.SORT_PRIORITY_ID;
        } else if (MODIFIED.equals(value)) {
            return ActionConstants.SORT_MODIFIED_ID;
        }
        return null;
    }

}
