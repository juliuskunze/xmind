package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

public class AlignmentHandler extends AbstractHandler {

    private static final String BOTTOM = "bottom"; //$NON-NLS-1$
    private static final String MIDDLE = "middle"; //$NON-NLS-1$
    private static final String TOP = "top"; //$NON-NLS-1$
    private static final String CENTER = "center"; //$NON-NLS-1$
    private static final String RIGHT = "right"; //$NON-NLS-1$
    private static final String LEFT = "left"; //$NON-NLS-1$
    private static final String PARAMETER_ID = "org.xmind.ui.alignmentParameter"; //$NON-NLS-1$

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor instanceof IGraphicalEditor) {
            IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                    .getActivePageInstance();
            if (page != null) {
                EditDomain domain = page.getEditDomain();
                if (domain != null) {
                    String alignment = event.getParameter(PARAMETER_ID);
                    domain.handleRequest(new Request(GEF.REQ_ALIGN).setViewer(
                            page.getViewer()).setParameter(GEF.PARAM_ALIGNMENT,
                            getAlignment(alignment)));
                }
            }
        }

        return null;
    }

    private int getAlignment(String alignment) {
        if (LEFT.equals(alignment)) {
            return PositionConstants.LEFT;
        } else if (RIGHT.equals(alignment)) {
            return PositionConstants.RIGHT;
        } else if (CENTER.equals(alignment)) {
            return PositionConstants.CENTER;
        } else if (TOP.equals(alignment)) {
            return PositionConstants.TOP;
        } else if (MIDDLE.equals(alignment)) {
            return PositionConstants.MIDDLE;
        } else if (BOTTOM.equals(alignment)) {
            return PositionConstants.BOTTOM;
        }
        return PositionConstants.NONE;
    }

}
