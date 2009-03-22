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
package org.xmind.gef.tool;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IGraphicalEditPart;

/**
 * @author Brian Sun
 * @version 2005
 */
public abstract class EditTool extends GraphicalTool implements ISourceTool {

    private IGraphicalEditPart source;

    public IGraphicalEditPart getSource() {
        return source;
    }

    public void setSource(IGraphicalEditPart source) {
        this.source = source;
    }

    /**
     * @see org.xmind.gef.tool.AbstractTool#onActivated(ITool)
     */
    @Override
    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        refreshStatus();
        if (getSource() != null) {
            if (!startEditing(getSource())) {
                changeActiveTool(GEF.TOOL_DEFAULT);
                return;
            }
        }
    }

    /**
     * 
     */
    protected void refreshStatus() {
        getStatus().setStatus(GEF.ST_CONTROL_PRESSED, false);
        getStatus().setStatus(GEF.ST_SHIFT_PRESSED, false);
        getStatus().setStatus(GEF.ST_ALT_PRESSED, false);
    }

    /**
     * @param source
     * @return
     * 
     */
    protected boolean startEditing(IGraphicalEditPart source) {
        return false;
    }

    protected void cancelEditing() {
        changeActiveTool(GEF.TOOL_DEFAULT);
    }

    protected void finishEditing() {
        changeActiveTool(GEF.TOOL_DEFAULT);
    }

    protected boolean isViewRequest(String reqType) {
        String role = getDomain().getPartRoles().getRole(reqType);
        return isViewRole(role);
    }

    protected boolean isViewRole(String role) {
        return GEF.ROLE_SELECTABLE.equals(role)
                || GEF.ROLE_SCALABLE.equals(role);
    }

    protected void handleSingleRequest(Request request) {
        if (request.getTargetViewer() == null
                || request.getTargetViewer() != getTargetViewer()) {
            super.handleSingleRequest(request);
            return;
        }
        String requestType = request.getType();
        if (GEF.REQ_EDIT.equals(requestType)) {
            return;
        } else if (GEF.REQ_SELECT_ALL.equals(requestType)) {
            selectAll();
        } else if (GEF.REQ_COPY.equals(requestType)) {
            copy();
        } else if (GEF.REQ_CUT.equals(requestType)) {
            cut();
        } else if (GEF.REQ_PASTE.equals(requestType)) {
            paste();
        } else if (GEF.REQ_DELETE.equals(requestType)) {
            delete();
        } else if (GEF.REQ_CANCEL.equals(requestType)) {
            cancelEditing();
        } else if (GEF.REQ_FINISH.equals(requestType)) {
            finishEditing();
        } else if (GEF.REQ_REDO.equals(requestType)) {
            if (canRedo()) {
                redo();
            }
        } else if (GEF.REQ_UNDO.equals(requestType)) {
            if (canUndo()) {
                undo();
            }
        } else if (isViewRequest(requestType)) {
            ITool defaultTool = getDomain().getDefaultTool();
            if (defaultTool != null) {
                defaultTool.handleRequest(request);
            }
        } else {
            finishEditing();
            ITool activeTool = getDomain().getActiveTool();
            if (activeTool != this)
                activeTool.handleRequest(request);
        }
    }

    protected void internalHandleRequest(Request request) {
        super.handleSingleRequest(request);
    }

    protected void selectAll() {
    }

    protected void copy() {
    }

    protected void cut() {
    }

    protected void paste() {
    }

    protected void delete() {
    }

    protected void undo() {
    }

    protected void redo() {
    }

    public boolean canUndo() {
        return false;
    }

    public boolean canRedo() {
        return false;
    }

    protected boolean handleMouseDown(MouseEvent me) {
        if (shouldFinishOnMouseDown(me)) {
            finishEditing();
            ITool activeTool = getDomain().getActiveTool();
            if (activeTool != this)
                activeTool.mouseDown(me, getTargetViewer());
            return true;
        } else {
            return super.handleMouseDown(me);
        }
    }

    protected boolean shouldFinishOnMouseDown(MouseEvent me) {
        return me.target != getSource();
    }

    protected boolean handleKeyUp(KeyEvent ke) {
        if (shouldFinish(ke)) {
            finishEditing();
            return true;
        } else if (shouldCancel(ke)) {
            cancelEditing();
            return true;
        }
        return super.handleKeyUp(ke);
    }

    protected abstract boolean shouldCancel(KeyEvent ke);

    protected abstract boolean shouldFinish(KeyEvent ke);

    protected boolean handleMouseEntered(MouseEvent me) {
        if (me.target.hasRole(GEF.ROLE_SELECTABLE)) {
            getTargetViewer().setPreselected(me.target);
        }
        return super.handleMouseEntered(me);
    }

}