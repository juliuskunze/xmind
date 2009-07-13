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
package org.xmind.ui.internal.notes;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.graphics.Point;
import org.xmind.ui.internal.findreplace.AbstractFindReplaceOperationProvider;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.IRichTextEditViewer;

/**
 * 
 * @author Karelun huang
 */
public class NotesFindReplaceOperationProvider extends
        AbstractFindReplaceOperationProvider {

    private static int parameter = PARAM_FORWARD | PARAM_CURRENT_MAP;

    private NotesViewer viewer = null;

    private TextViewer textViewer = null;

    private IRichDocument document;

    private String text = null;

    private int offset = 0;

    public NotesFindReplaceOperationProvider(NotesViewer viewer) {
        this.viewer = viewer;

    }

    @Override
    protected boolean findAll(String toFind) {
        refreshText(viewer);
        if (!isCaseSensitive()) {
            text = text.toLowerCase();
            toFind.toLowerCase();
        }
        int lastIndex = text.lastIndexOf(toFind);
        if (lastIndex == -1)
            return false;
        selectText(lastIndex, toFind.length());
        return true;
    }

    @Override
    protected boolean findNext(String toFind) {
        refreshText(viewer);
        offset = getCurrentOffset();
        if (offset < 0)
            return false;
        findNextSubText(toFind);
        if (offset < 0)
            return false;
        selectText(offset, toFind.length());
        return true;
    }

    private void findNextSubText(String toFind) {
        int offset1 = isForward() ? 0 : text.length();
        int index = indexOf(text, toFind, offset);
        if (index == -1) {
            int firstIndex = indexOf(text, toFind, offset1);
            this.offset = firstIndex;
        } else
            this.offset = index;
    }

    private int getCurrentOffset() {
        Point range = textViewer.getSelectedRange();
        if (range.y > 0)
            return isForward() ? range.x + range.y : range.x;
        return text == null ? -1 : getNewOffset(text);
    }

    @Override
    protected boolean replaceAll(String toFind, String toReplaceWith) {
        if (toReplaceWith == null || "".equals(toReplaceWith)) //$NON-NLS-1$
            return false;
        refreshText(viewer);
        int index = getOffsetInText(toFind);
        boolean found = index != -1 ? true : false;
        while (found) {
            index = getOffsetInText(toFind);
            if (index == -1)
                break;
            try {
                document.replace(index, toFind.length(), toReplaceWith);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        int lastIndex = text.lastIndexOf(toReplaceWith);
        selectText(lastIndex, toReplaceWith.length());
        return found;
    }

    private int getOffsetInText(String toFind) {
        String str = document.get();
        if (!isCaseSensitive()) {
            str = str.toLowerCase();
            toFind = toFind.toLowerCase();
        }
        return str.indexOf(toFind);
    }

    @Override
    protected boolean replaceNext(String toFind, String toReplaceWith) {
        Point range = textViewer.getSelectedRange();
        int start = range.x;
        int length = range.y;
        if (length == 0)
            return false;
        try {
            String str = document.get(start, length);
            if (!toFind.equals(str))
                return false;
            document.replace(start, length, toReplaceWith);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        textViewer.setSelectedRange(start, toReplaceWith.length());
        return findNext(toFind);
    }

    public boolean canFind(String toFind) {
//        return toFind != null && text != null && !"".equals(text.trim());
        return toFind != null;
    }

    public boolean canReplace(String toFind, String toReplaceWith) {
        // TODO Auto-generated method stub
        return canFind(toFind);
    }

    public int getParameter() {
        return parameter;
    }

    public void setParameter(int op, boolean value) {
        parameter &= ~op;
        if (value)
            parameter |= op;
    }

    protected void selectText(int offset, int length) {
        if (textViewer != null)
            textViewer.setSelectedRange(offset, length);
    }

    private void refreshText(NotesViewer viewer) {
        IRichTextEditViewer imple = viewer.getImplementation();
        this.textViewer = imple.getTextViewer();
        this.document = imple.getDocument();
        this.text = document.get();
    }
}
