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
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.TAG_NOTES;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.INotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.Notes;
import org.xmind.core.util.DOMUtils;

/**
 * @author briansun
 * 
 */
public class NotesImpl extends Notes {

    private Element topicElement;

    private TopicImpl ownedTopic;

    /**
     * @param implementation
     */
    public NotesImpl(Element topicElement, TopicImpl ownedTopic) {
        super();
        this.topicElement = topicElement;
        this.ownedTopic = ownedTopic;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof NotesImpl))
            return false;
        NotesImpl that = (NotesImpl) obj;
        return this.topicElement == that.topicElement;
    }

    public int hashCode() {
        return topicElement.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(getNotesElement());
    }

    public Object getAdapter(Class adapter) {
        if (adapter == Node.class || adapter == Element.class)
            return getNotesElement();
        return super.getAdapter(adapter);
    }

    /**
     * @see org.xmind.core.INotes#getParent()
     */
    public ITopic getParent() {
        return ownedTopic;
    }

    private Element getNotesElement() {
        return DOMUtils.getFirstChildElementByTag(topicElement, TAG_NOTES);
    }

    public INotesContent getContent(String format) {
        Element n = getNotesElement();
        if (n != null) {
            Element c = DOMUtils.getFirstChildElementByTag(n, format);
            if (c != null) {
                return getNotesContent(c);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        Element n = getNotesElement();
        if (n != null) {
            return !n.hasChildNodes();
        }
        return true;
    }

    public void setContent(String format, INotesContent content) {
        if (format == null)
            return;

        Element notesEle = getNotesElement();
        Element oldContentEle = notesEle == null ? null : DOMUtils
                .getFirstChildElementByTag(notesEle, format);
        INotesContent oldContent = oldContentEle == null ? null
                : getNotesContent(oldContentEle);

        WorkbookImpl workbook = null;
        if (oldContent instanceof NotesContentImplBase) {
            if (workbook == null)
                workbook = getRealizedWorkbook();
            ((NotesContentImplBase) oldContent).removeNotify(workbook);
        }

        if (oldContentEle != null && notesEle != null) {
            notesEle.removeChild(oldContentEle);
        }

        if (content != null) {
            Element newContentEle = (Element) content.getAdapter(Element.class);
            if (newContentEle != null) {
                if (notesEle == null)
                    notesEle = DOMUtils.ensureChildElement(topicElement,
                            TAG_NOTES);
                notesEle.appendChild(newContentEle);
            }
        }

        if (notesEle != null && !notesEle.hasChildNodes()) {
            topicElement.removeChild(notesEle);
        }

        if (content instanceof NotesContentImplBase) {
            if (workbook == null)
                workbook = getRealizedWorkbook();
            ((NotesContentImplBase) content).addNotify(workbook);
        }

        fireTargetValueChange(format, oldContent, content);
    }

    private INotesContent getNotesContent(Element oldContentEle) {
        return (INotesContent) ((WorkbookImpl) getOwnedWorkbook())
                .getAdaptable(oldContentEle);
    }

    protected WorkbookImpl getRealizedWorkbook() {
        ITopic parent = getParent();
        if (parent instanceof TopicImpl)
            return ((TopicImpl) parent).getRealizedWorkbook();
        return null;
    }

    protected void addNotify(WorkbookImpl workbook) {
        Element n = getNotesElement();
        if (n != null) {
            Iterator<Element> it = DOMUtils.childElementIter(n);
            while (it.hasNext()) {
                Element c = it.next();
                INotesContent content = getNotesContent(c);
                if (content instanceof NotesContentImplBase) {
                    ((NotesContentImplBase) content).addNotify(workbook);
                }
            }
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        Element n = getNotesElement();
        if (n != null) {
            Iterator<Element> it = DOMUtils.childElementIter(n);
            while (it.hasNext()) {
                Element c = it.next();
                INotesContent content = getNotesContent(c);
                if (content instanceof NotesContentImplBase) {
                    ((NotesContentImplBase) content).removeNotify(workbook);
                }
            }
        }
    }

//    /**
//     * @see org.xmind.core.INotes#getContent(java.lang.String)
//     */
//    public String getContent(String format) {
//        Element notesElement = getNotesElement();
//        if (notesElement != null) {
//            return getTextContentByTag(notesElement, format);
//        }
//        return null;
//    }
//
//    /**
//     * @see org.xmind.core.INotes#isEmpty()
//     */
//    public boolean isEmpty() {
//        Element notesElement = getNotesElement();
//        if (notesElement != null) {
//            return getTextContentByTag(topicElement, INotes.PLAIN) != null
//                    || getTextContentByTag(topicElement, INotes.HTML) != null;
//        }
//        return true;
//    }
//
//    /**
//     * @see org.xmind.core.INotes#setContent(java.lang.String, java.lang.String)
//     */
//    public void setContent(String format, String content) {
//        String oldValue = getContent(format);
//        if (HTML.equals(format)) {
//            decreaseRefs(oldValue);
//        }
//        if (content != null) {
//            Element notesElement = ensureChildElement(topicElement, TAG_NOTES);
//            setText(notesElement, format, content);
//        } else {
//            Element notesElement = getNotesElement();
//            Element dataElement = getFirstChildElementByTag(notesElement,
//                    format);
//            if (dataElement != null) {
//                notesElement.removeChild(dataElement);
//                if (!notesElement.hasChildNodes())
//                    topicElement.removeChild(notesElement);
//            }
//        }
//        String newValue = getContent(format);
//        if (HTML.equals(format)) {
//            increaseRefs(newValue);
//        }
//        fireTargetValueChange(format, oldValue, newValue);
//    }

//    private void decreaseRefs(String content) {
//        if (content == null || "".equals(content)) //$NON-NLS-1$
//            return;
//
//        Element richEle = getRichElement(content);
//        if (richEle != null) {
//            decreaseRefs(richEle);
//        }
//    }
//
//    private void increaseRefs(String content) {
//        if (content == null || "".equals(content)) //$NON-NLS-1$
//            return;
//
//        Element richEle = getRichElement(content);
//        if (richEle != null) {
//            increaseRefs(richEle);
//        }
//    }
//
//    private void decreaseRefs(Element ele) {
//        String styleId = getAttribute(ele, DOMConstants.ATTR_STYLE_ID);
//        if (styleId != null) {
//            IWorkbook wb = ownedTopic.getPath().getWorkbook();
//            if (wb != null && wb == ownedTopic.getOwnedWorkbook()) {
//                ((WorkbookImpl) wb).getStyleRefCounter().decreaseRef(styleId);
//            }
//        }
//
//        String url = getAttribute(ele, DOMConstants.ATTR_SRC);
//        if (url != null) {
//            InternalHyperlinkUtils.deactivateHyperlink(ownedTopic
//                    .getOwnedWorkbook(), url);
//        }
//
//        Iterator<Element> it = childElementIter(ele);
//        while (it.hasNext()) {
//            decreaseRefs(it.next());
//        }
//    }
//
//    private void increaseRefs(Element ele) {
//        String styleId = getAttribute(ele, DOMConstants.ATTR_STYLE_ID);
//        if (styleId != null) {
//            IWorkbook wb = ownedTopic.getPath().getWorkbook();
//            if (wb != null && wb == ownedTopic.getOwnedWorkbook()) {
//                ((WorkbookImpl) wb).getStyleRefCounter().increaseRef(styleId);
//            }
//        }
//
//        String url = getAttribute(ele, DOMConstants.ATTR_SRC);
//        if (url != null) {
//            InternalHyperlinkUtils.activateHyperlink(ownedTopic
//                    .getOwnedWorkbook(), url);
//        }
//
//        Iterator<Element> it = childElementIter(ele);
//        while (it.hasNext()) {
//            increaseRefs(it.next());
//        }
//    }
//
//    private Element getRichElement(String content) {
//        String text = DOMUtils.makeElementText(content, NS.XMAP,
//                WorkbookUtils.TAG_RICH_CONTENT, NS.Xhtml, NS.Xlink, NS.SVG);
//        try {
//            Document doc = DOMUtils.loadDocument(text.getBytes());
//            return doc.getDocumentElement();
//        } catch (IOException e) {
//            return null;
//        }
//    }

    private void fireTargetValueChange(Object target, Object oldValue,
            Object newValue) {
        ownedTopic.getCoreEventSupport().dispatchTargetValueChange(ownedTopic,
                Core.TopicNotes, target, oldValue, newValue);
    }

    public ISheet getOwnedSheet() {
        return ownedTopic.getOwnedSheet();
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedTopic.getOwnedWorkbook();
    }

}