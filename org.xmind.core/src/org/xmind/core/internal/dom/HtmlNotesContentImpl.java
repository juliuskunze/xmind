/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

import static org.xmind.core.internal.dom.DOMConstants.TAG_A;
import static org.xmind.core.internal.dom.DOMConstants.TAG_IMG;
import static org.xmind.core.internal.dom.DOMConstants.TAG_P;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmind.core.IAdaptable;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.IImageSpan;
import org.xmind.core.INotes;
import org.xmind.core.IParagraph;
import org.xmind.core.ITextSpan;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.util.DOMUtils;

public class HtmlNotesContentImpl extends BaseNotesContentImpl implements
        IHtmlNotesContent, INodeAdaptableProvider {

    public HtmlNotesContentImpl(Element implementation,
            WorkbookImpl ownedWorkbook) {
        super(implementation, ownedWorkbook);
    }

    public void addParagraph(IParagraph paragraph) {
        ParagraphImpl p = (ParagraphImpl) paragraph;
        getImplementation().appendChild(p.getImplementation());
        p.addNotify(getRealizedWorkbook());
    }

    public IImageSpan createImageSpan(String source) {
        Element e = ((WorkbookImpl) getOwnedWorkbook()).getImplementation()
                .createElement(TAG_IMG);
        ImageSpanImpl image = new ImageSpanImpl(e, this);
        image.setSource(source);
        register(e, image);
        return image;
    }

    public IHyperlinkSpan createHyperlinkSpan(String sourceHyper) {
        Element e = ((WorkbookImpl) getOwnedWorkbook()).getImplementation()
                .createElement(TAG_A);
        HyperlinkSpanImpl hyperlink = new HyperlinkSpanImpl(e, this);
        hyperlink.setHref(sourceHyper);
        register(e, hyperlink);
        return hyperlink;
    }

    public IParagraph createParagraph() {
        Element e = ((WorkbookImpl) getOwnedWorkbook()).getImplementation()
                .createElement(DOMConstants.TAG_P);
        ParagraphImpl paragraph = new ParagraphImpl(e, this);
        register(e, paragraph);
        return paragraph;
    }

    public ITextSpan createTextSpan(String textContent) {
        Text t = ((WorkbookImpl) getOwnedWorkbook()).getImplementation()
                .createTextNode(textContent);
        TextSpanImpl text = new TextSpanImpl(t, this);
        register(t, text);
        return text;
    }

    public List<IParagraph> getParagraphs() {
        return DOMUtils.getChildList(getImplementation(), TAG_P, this);
    }

    public void removeParagraph(IParagraph paragraph) {
        ParagraphImpl p = (ParagraphImpl) paragraph;
        if (p.getImplementation().getParentNode() == getImplementation()) {
            p.removeNotify(getRealizedWorkbook());
            getImplementation().removeChild(p.getImplementation());
        }
    }

    protected ElementRegistry getElementRegistry() {
        return ((WorkbookImpl) getOwnedWorkbook()).getElementRegistry();
    }

    public IAdaptable getAdaptable(Node node) {
        if (node == null)
            return null;

        IAdaptable element = ((WorkbookImpl) getOwnedWorkbook())
                .getAdaptable(node);
        if (element == null) {
            if (node instanceof Element) {
                Element e = (Element) node;
                String tagName = e.getTagName();
                if (TAG_P.equals(tagName)) {
                    element = new ParagraphImpl(e, this);
                } else if (DOMConstants.TAG_SPAN.equals(tagName)) {
                    element = new TextSpanImpl(e, this);
                } else if (DOMConstants.TAG_IMG.equals(tagName)) {
                    element = new ImageSpanImpl(e, this);
                } else if (DOMConstants.TAG_A.equals(tagName)) {
                    element = new HyperlinkSpanImpl(e, this);
                }
            } else {
                Node p = node.getParentNode();
                if (p instanceof Element) {
                    if (TAG_P.equals(p.getNodeName())
                            || TAG_A.equals(p.getNodeName()))
                        element = new TextSpanImpl(node, this);
                }
            }
            if (element != null) {
                register(node, element);
            }
        }
        return element;
    }

    protected void register(Object key, Object element) {
        getElementRegistry().registerByKey(key, element);
    }

    protected void unregister(Object key) {
        getElementRegistry().unregisterByKey(key);
    }

    protected void addNotify(WorkbookImpl workbook) {
        for (IParagraph p : getParagraphs()) {
            ((ParagraphImpl) p).addNotify(workbook);
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        for (IParagraph p : getParagraphs()) {
            ((ParagraphImpl) p).removeNotify(workbook);
        }
    }

    protected WorkbookImpl getRealizedWorkbook() {
        INotes parent = getParent();
        if (parent instanceof NotesImpl)
            return ((NotesImpl) parent).getRealizedWorkbook();
        return null;
    }

}