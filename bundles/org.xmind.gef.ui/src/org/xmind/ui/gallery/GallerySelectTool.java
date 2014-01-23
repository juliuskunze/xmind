/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.ui.gallery;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.tool.SelectTool;

public class GallerySelectTool extends SelectTool {

    private FramePart sourceFrame = null;

    protected boolean isFrameAsButton() {
        return !getTargetViewer().getProperties().getBoolean(
                GalleryViewer.SolidFrames, false);
    }

    protected boolean isSingleClickToOpen() {
        return getTargetViewer().getProperties().getBoolean(
                GalleryViewer.SingleClickToOpen, false);
    }

    protected boolean isCursorInTitle(IPart p) {
        return p instanceof FramePart
                && ((FramePart) p).getFigure().getTitle()
                        .containsPoint(getCursorPosition());
    }

    protected boolean isTitleEditable(IPart p) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.SelectTool#handleMouseDown(org.xmind.gef.event.MouseEvent
     * )
     */
    protected boolean handleMouseDown(MouseEvent me) {
        if (isFrameAsButton()) {
            if (me.target instanceof FramePart) {
                FramePart frame = (FramePart) me.target;
                if (frame.getContentPane().containsPoint(me.cursorLocation)) {
                    sourceFrame = frame;
                    sourceFrame.getFigure().press();
                }
            }
        }
        boolean ret = handleSelectionOnMouseDown(me);
        if (isCursorInTitle(me.target) && isTitleEditable(me.target)) {
            Request request = new Request(GEF.REQ_EDIT);
            request.setDomain(getDomain());
            request.setViewer(getTargetViewer());
            request.setPrimaryTarget(me.target);
            startEditing(me.target, request);
            ITool et = getTool(GEF.TOOL_EDIT);
            if (et != null && et == getDomain().getActiveTool()) {
                me.consume();
            }
        } else if (isSingleClickToOpen()) {
            performOpen();
            me.consume();
        }
        return ret;
    }

    protected boolean handleSelectionOnMouseDown(MouseEvent me) {
        return super.handleMouseDown(me);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.SelectTool#handleMouseUp(org.xmind.gef.event.MouseEvent
     * )
     */
    public boolean handleMouseUp(MouseEvent me) {
        if (sourceFrame != null) {
            sourceFrame.getFigure().unpress();
            sourceFrame = null;
        }
        return super.handleMouseUp(me);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.SelectTool#handleMouseDoubleClick(org.xmind.gef.event
     * .MouseEvent)
     */
    protected boolean handleMouseDoubleClick(MouseEvent me) {
        if (me.target instanceof FramePart) {
            if (!isSingleClickToOpen()
                    && (!isCursorInTitle(me.target) || !isTitleEditable(me.target))) {
                performOpen();
                me.consume();
                return true;
            }
        }
        return super.handleMouseDoubleClick(me);
    }

    protected void performOpen() {
        GalleryViewer viewer = (GalleryViewer) getTargetViewer();
        viewer.fireOpen();
    }

}