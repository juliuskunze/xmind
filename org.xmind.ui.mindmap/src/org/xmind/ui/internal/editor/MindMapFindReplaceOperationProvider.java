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
package org.xmind.ui.internal.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.xmind.core.ITitled;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.AbstractTool;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.findreplace.AbstractFindReplaceOperationProvider;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditTool;
import org.xmind.ui.texteditor.FloatingTextEditor;

/**
 * @author Frank Shaka
 */
/*
 * TODO Use the new request-policy mechanism
 */
public class MindMapFindReplaceOperationProvider extends
        AbstractFindReplaceOperationProvider {

    private class SearchData {
        IPart host;
        int offset;

        /**
         * @param host
         * @param offset
         * @param hostText
         */
        public SearchData(IPart host, int offset) {
            super();
            this.host = host;
            this.offset = offset;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof SearchData))
                return false;
            SearchData sd = (SearchData) obj;
            return sd.host == this.host && sd.offset == this.offset;
        }

    }

    /**
     * Parameter for global use (initial value is {@link #PARAM_FORWARD} and
     * {@link #PARAM_CURRENT_MAP})
     */
    private static int parameter = PARAM_FORWARD | PARAM_CURRENT_MAP;

    private IGraphicalEditor editor;

    private String cachedToFind = null;

    /**
     * 
     */
    public MindMapFindReplaceOperationProvider(IGraphicalEditor editor) {
        this.editor = editor;
    }

    protected IPart getCurrentPart() {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null) {
            IPart focusedPart = viewer.getFocusedPart();
            if (focusedPart != null && focusedPart instanceof ITopicPart)
                return focusedPart;
            List<IPart> selection = viewer.getSelectionSupport()
                    .getPartSelection();
            if (!selection.isEmpty()) {
                IPart part = selection.get(0);
                if (part instanceof ITopicPart)
                    return part;
            }
        }
        return getCurrentCentralTopicPart();
    }

    /**
     * @return
     */
    private IPart getCurrentCentralTopicPart() {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null)
            return (ITopicPart) viewer.getAdapter(ITopicPart.class);
        return null;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#canFind(java.lang.String)
     */
    public boolean canFind(String toFind) {
        return getCurrentPart() != null;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#canReplace(java.lang.String,
     *      java.lang.String)
     */
    public boolean canReplace(String toFind, String toReplaceWith) {
        return canFind(toFind);
    }

    /**
     * @param toFind
     */
    protected void saveCache(String toFind) {
        if ((cachedToFind == null && cachedToFind != toFind)
                || (cachedToFind != null && !cachedToFind.equals(toFind))) {
            cachedToFind = toFind;
        }
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#find(java.lang.String)
     */
    public boolean find(String toFind) {
        saveCache(toFind);
        if (cachedToFind == null)
            return false;
        return super.find(cachedToFind);
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#findAll(java.lang.String)
     */
    @Override
    protected boolean findAll(String toFind) {
        List<IPart> result = findAllParts(toFind);
        select(result);
        return !result.isEmpty();
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#findNext(java.lang.String)
     */
    @Override
    protected boolean findNext(String toFind) {
        int offset = getCurrentOffset();
        if (offset < 0)
            return false;
        SearchData current = new SearchData(getCurrentPart(), offset);
        SearchData next = findNext(toFind, current);
        if (next == null)
            return false;

        startEditing(next.host);
        selectText(next.offset, toFind.length());
        return true;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#replace(java.lang.String,
     *      java.lang.String)
     */
    public boolean replace(String toFind, String toReplaceWith) {
        saveCache(toFind);
        if (cachedToFind == null)
            return false;
        return super.replace(toFind, toReplaceWith);
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#replaceAll(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected boolean replaceAll(String toFind, String toReplaceWith) {
        boolean found = findAll(toFind);
        if (found) {
            EditDomain domain = getCurrentDomain();
            if (domain == null) {
                found = false;
            } else {
                Boolean ignoreCase = Boolean
                        .valueOf((getParameter() & PARAM_CASE_SENSITIVE) == 0);
                domain.handleRequest(new Request(MindMapUI.REQ_REPLACE_ALL)
                        .setParameter(GEF.PARAM_TEXT, toFind).setParameter(
                                MindMapUI.PARAM_REPLACEMENT, toReplaceWith)
                        .setParameter(MindMapUI.PARAM_IGNORE_CASE, ignoreCase)
                        .setDomain(domain).setViewer(getActiveViewer()));
            }
        }
        return found;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#replaceNext(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected boolean replaceNext(String toFind, String toReplaceWith) {
        FloatingTextEditor textEditor = getTextEditor();
        if (textEditor != null) {
            if (canReplace(toFind, textEditor)) {
                StyledText textWidget = textEditor.getTextViewer()
                        .getTextWidget();
                Point selection = textWidget.getSelection();
                int caretOffset = isForward() ? selection.y : selection.x;
                textWidget.insert(toReplaceWith);
                textWidget.setCaretOffset(caretOffset);
            }
        }
        return findNext(toFind);
    }

    /**
     * @return
     */
    private int getCurrentOffset() {
        if (isEditing()) {
            FloatingTextEditor textEditor = getTextEditor();
            if (textEditor == null)
                return -1;
            StyledText textWidget = textEditor.getTextViewer().getTextWidget();
            Point selection = textWidget.getSelection();
            int offset = isForward() ? selection.y : selection.x;
            getEditTool().handleRequest(GEF.REQ_FINISH, getActiveViewer());
            return offset;
        }
        if (isForward())
            return 0;
        String text = getText(getCurrentPart());
        return text == null ? -1 : getNewOffset(text);
    }

    protected void selectText(final int start, final int length) {
        FloatingTextEditor textEditor = getTextEditor();
        if (textEditor != null) {
            textEditor.getTextViewer().getTextWidget().setSelectionRange(start,
                    length);
        }
    }

    protected boolean startEditing(IPart p) {
        if (p == null)
            return false;
        ITool selectTool = getDefaultTool();
        ITool editTool = getEditTool();
        if (selectTool == null || editTool == null)
            return false;
        ((AbstractTool) editTool).getStatus().setStatus(GEF.ST_NO_FOCUS, true);
        selectTool.handleRequest(new Request(GEF.REQ_EDIT).setPrimaryTarget(p));
        return isEditing();
    }

    protected SearchData findNext(String toFind, SearchData current) {
        return findNext(toFind, current, null);
    }

    protected SearchData findNext(String toFind, SearchData current,
            SearchData start) {
        if (current == null || (current.equals(start)))
            return null;
        String text = getText(current.host);
        if (text == null)
            return null;
        int index = indexOf(text, toFind, current.offset);
        if (index >= 0) {
            return new SearchData(current.host, index);
        }
        if (start == null) {
            start = current;
        }
        IPart next = getNextPart(current);
        String nextText = getText(next);
        while (nextText == null) {
            next = getNextPart(next);
            if (next == start.host)
                break;
            nextText = getText(next);
        }
        int newOffset = getNewOffset(nextText);
        if (next != start.host) {
            return findNext(toFind, new SearchData(next, newOffset), start);
        }
        index = indexOf(nextText, toFind, newOffset);
        if (index >= 0 && isIndexPermitted(index, start.offset)) {
            return new SearchData(next, index);
        }
        return null;
    }

    /**
     * @param toFind
     * @return
     */
    protected List<IPart> findAllParts(String toFind) {
        return findAllParts(toFind, getCurrentPart(), null,
                new ArrayList<IPart>());
    }

    protected List<IPart> findAllParts(String toFind, IPart current,
            IPart start, List<IPart> result) {
        if (start == null)
            start = current;
        String text = getText(current);
        int index = indexOf(text, toFind, getNewOffset(text));
        if (index >= 0) {
            result.add(current);
        }
        IPart next = getNextPart(current);
        if (next != start) {
            result = findAllParts(toFind, next, start, result);
        }
        return result;
    }

    /**
     * @param result
     */
    protected boolean select(List<IPart> result) {
        if (isEditing()) {
            getEditTool().handleRequest(GEF.REQ_FINISH, getActiveViewer());
        }
        ITool selectTool = getDefaultTool();
        if (selectTool == null)
            return false;
        selectTool
                .handleRequest(new Request(GEF.REQ_SELECT).setTargets(result));
        return true;
    }

    protected boolean canReplace(String toFind, FloatingTextEditor textEditor) {
        ITextSelection selection = (ITextSelection) textEditor.getSelection();
        String selectionText = selection.getText();
        if (!equals(toFind, selectionText))
            return false;
        if (isWholeWord()) {
            String text = textEditor.getTextContents();
            return isWholeWord(text, selection.getOffset(), selection
                    .getLength());
        }
        return true;
    }

    protected String getText(IPart p) {
        if (p == null)
            return null;
        ITitled titled = (ITitled) p.getAdapter(ITitled.class);
        if (titled != null && titled.hasTitle()) {
            return titled.getTitleText();
        }
        return (String) p.getAdapter(String.class);
    }

    /**
     * @param current
     * @return
     */
    private IPart getNextPart(SearchData current) {
        return getNextPart(current.host);
    }

    private IPart getNextPart(IPart current) {
        return isForward() ? getForwardPart(current) : getBackwardPart(current);
    }

    /**
     * @param current
     * @return
     */
    private IPart getBackwardPart(IPart current) {
        IPart prev = findPrecedingPart(current);
        if (!isWorkbook())
            return prev;
//        if (prev != current && prev.getModel() != getCentralTopic())
//            return prev;
        int pageIndex = getActivePage().getIndex();
        int pageCount = editor.getPageCount();
        if (pageIndex == 0)
            pageIndex = pageCount - 1;
        else
            pageIndex--;
        editor.setActivePage(pageIndex);
        return getCurrentCentralTopicPart();
    }

    /**
     * @param current
     * @return
     */
    private IPart getForwardPart(IPart current) {
        IPart prev = findSucceedingPart(current);
        if (!isWorkbook())
            return prev;
        int pageIndex = getActivePage().getIndex();
        int pageCount = editor.getPageCount();
        if (pageIndex == pageCount - 1)
            pageIndex = 0;
        else
            pageIndex++;
        editor.setActivePage(pageIndex);
        return getCurrentCentralTopicPart();
    }

    private IPart findPrecedingPart(IPart current) {
        if (current.hasRole(GEF.ROLE_NAVIGABLE)) {
            Request navRequest = new Request(GEF.REQ_NAV_PREV)
                    .setPrimaryTarget(current).setViewer(
                            current.getSite().getViewer());
            current.handleRequest(navRequest, GEF.ROLE_NAVIGABLE);
            Object result = navRequest.getResult(GEF.RESULT_NAVIGATION);
            if (result instanceof IPart[]) {
                IPart[] parts = (IPart[]) result;
                if (parts.length > 0)
                    return parts[0];
            }
        }
        return current;
    }

    private IPart findSucceedingPart(IPart current) {
        if (current.hasRole(GEF.ROLE_NAVIGABLE)) {
            Request navRequest = new Request(GEF.REQ_NAV_NEXT)
                    .setPrimaryTarget(current).setViewer(
                            current.getSite().getViewer());
            current.handleRequest(navRequest, GEF.ROLE_NAVIGABLE);
            Object result = navRequest.getResult(GEF.RESULT_NAVIGATION);
            if (result instanceof IPart[]) {
                IPart[] parts = (IPart[]) result;
                if (parts.length > 0)
                    return parts[0];
            }
        }
        return current;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#getParameter()
     */
    public int getParameter() {
        return parameter;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#setParameter(int,
     *      boolean)
     */
    public void setParameter(int op, boolean value) {
        parameter &= ~op;
        if (value)
            parameter |= op;
    }

    /**
     * 
     * @return
     */
    public boolean canFind() {
        return cachedToFind != null;
    }

    /**
     * 
     * @return
     */
    public boolean findAll() {
        return findAll(cachedToFind);
    }

    /**
     * 
     * @return
     */
    public boolean findNext() {
        return findNext(cachedToFind);
    }

    /**
     * @param toReplaceWith
     * @return
     */
    public boolean replaceNext(String toReplaceWith) {
        return replaceNext(cachedToFind, toReplaceWith);
    }

    /**
     * @param toReplaceWith
     * @return
     */
    public boolean replaceAll(String toReplaceWith) {
        return replaceAll(cachedToFind, toReplaceWith);
    }

    protected FloatingTextEditor getTextEditor() {
        ITool editTool = getEditTool();
        if (editTool instanceof FloatingTextEditTool) {
            return ((FloatingTextEditTool) editTool).getEditor();
        }
        return null;
    }

//    protected ITopic getCentralTopic() {
//        IMindMap map = getCurrentMap();
//        return map == null ? null : map.getCentralTopic();
//    }
//
//    /**
//     * @return
//     */
//    protected IMindMap getCurrentMap() {
//        IGraphicalViewer viewer = getActiveViewer();
//        return viewer == null ? null : (IMindMap) viewer
//                .getAdapter(IMindMap.class);
//    }

    protected boolean isEditing() {
        ITool editTool = getEditTool();
        return editTool != null
                && ((AbstractTool) editTool).getStatus()
                        .isStatus(GEF.ST_ACTIVE);
    }

    /**
     * @return
     */
    protected ITool getEditTool() {
        EditDomain domain = getCurrentDomain();
        return domain == null ? null : domain
                .getTool(MindMapUI.TOOL_EDIT_TOPIC_TITLE);
    }

    /**
     * @return
     */
    protected ITool getDefaultTool() {
        EditDomain domain = getCurrentDomain();
        return domain == null ? null : domain.getDefaultTool();
    }

    /**
     * @return
     */
    protected EditDomain getCurrentDomain() {
        IGraphicalEditorPage page = getActivePage();
        return page == null ? null : page.getEditDomain();
    }

    /**
     * @return
     */
    protected IGraphicalViewer getActiveViewer() {
        IGraphicalEditorPage page = getActivePage();
        return page == null ? null : page.getViewer();
    }

    /**
     * @return
     */
    private IGraphicalEditorPage getActivePage() {
        return editor.getActivePageInstance();
    }

}