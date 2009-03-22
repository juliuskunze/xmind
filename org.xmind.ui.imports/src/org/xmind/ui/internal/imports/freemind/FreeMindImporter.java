/*
 * Copyright (c) 2006-2008 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.imports.freemind;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.INotes;
import org.xmind.core.IParagraph;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ITextSpan;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.ResourceMappingManager;
import org.xmind.core.io.freemind.FreeMindConstants;
import org.xmind.core.io.freemind.FreeMindResourceMappingManager;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.ui.internal.imports.ImportMessages;
import org.xmind.ui.internal.imports.ImporterUtils;
import org.xmind.ui.io.MonitoredInputStream;
import org.xmind.ui.style.Styles;
import org.xmind.ui.wizards.MindMapImporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FreeMindImporter extends MindMapImporter implements
        FreeMindConstants, ErrorHandler {

    private class ArrowLink {
        String fromId;
        String toId;

        public ArrowLink(String fromId, String toId) {
            super();
            this.fromId = fromId;
            this.toId = toId;
        }
    }

    private class NotesImporter {

        IHtmlNotesContent content;

        IParagraph currentParagraph;

        public NotesImporter(IHtmlNotesContent content) {
            this.content = content;
        }

        public void importFrom(Element htmlEle) throws InterruptedException {
            checkInterrupted();
            String tagName = DOMUtils.getLocalName(htmlEle.getTagName());
            if ("p".equalsIgnoreCase(tagName)) { //$NON-NLS-1$
                addParagraph();
            } else if ("li".equalsIgnoreCase(tagName)) { //$NON-NLS-1$
                addParagraph();
            } else if ("br".equalsIgnoreCase(tagName)) { //$NON-NLS-1$
                addParagraph();
            }
            NodeList nl = htmlEle.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node e = nl.item(i);
                if (e.getNodeType() == Node.TEXT_NODE) {
                    addText(e.getTextContent());
                } else if (e.getNodeType() == Node.ELEMENT_NODE) {
                    importFrom((Element) e);
                }
            }
        }

        private void addText(String textContent) {
            if (currentParagraph == null)
                addParagraph();
            ITextSpan span = content.createTextSpan(textContent);
            currentParagraph.addSpan(span);
        }

        private void addParagraph() {
            currentParagraph = content.createParagraph();
            content.addParagraph(currentParagraph);
        }
    }

    private static DocumentBuilder documentBuilder = null;

    private static ResourceMappingManager mappings = null;

    private Map<String, String> idMap = new HashMap<String, String>();

    private IStyleSheet tempStyleSheet = null;

    private List<ArrowLink> links = null;

    public FreeMindImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public FreeMindImporter(String sourcePath) {
        super(sourcePath);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        try {
            DocumentBuilder builder = getDocumentBuilder();
            builder.setErrorHandler(this);
            Document doc;
            InputStream in = new FileInputStream(getSourcePath());
            try {
                in = new MonitoredInputStream(in, getMonitor());
                doc = builder.parse(in);
            } finally {
                builder.setErrorHandler(null);
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            loadSheet(doc.getDocumentElement());

        } catch (Throwable e) {
            throw new InvocationTargetException(e);
        }
    }

    private void checkInterrupted() throws InterruptedException {
        if (getMonitor().isCanceled())
            throw new InterruptedException();
    }

    private void loadSheet(Element docEle) throws InterruptedException {
        checkInterrupted();
        ISheet sheet = getTargetWorkbook().createSheet();
        Element nodeEle = child(docEle, "node"); //$NON-NLS-1$
        if (nodeEle != null) {
            loadTopic(sheet.getRootTopic(), nodeEle);
        } else {
            sheet.getRootTopic().setTitleText(
                    ImportMessages.Importer_CentralTopic);
        }

        arrangeRelationships();

        String bgColor = att(docEle, "backgound_color"); //$NON-NLS-1$
        if (bgColor != null) {
            IStyle style = getTempStyleSheet().createStyle(IStyle.MAP);
            style.setProperty(Styles.FillColor, bgColor);
            style = getTargetWorkbook().getStyleSheet().importStyle(style);
            if (style != null) {
                sheet.setStyleId(style.getId());
            }
        }
        addTargetSheet(sheet);
    }

    private void arrangeRelationships() {
    }

    private void loadTopic(ITopic topic, Element topicEle)
            throws InterruptedException {
        checkInterrupted();
        String id = att(topicEle, "ID"); //$NON-NLS-1$
        if (id != null)
            idMap.put(id, topic.getId());

        checkInterrupted();
        String text = att(topicEle, "TEXT"); //$NON-NLS-1$
        if (text == null)
            text = ImporterUtils.getDefaultTopicTitle(topic);
        topic.setTitleText(text);

        checkInterrupted();
        String folded = att(topicEle, "FOLDED"); //$NON-NLS-1$
        if (folded != null && "true".equals(folded)) //$NON-NLS-1$
            topic.setFolded(true);

        checkInterrupted();
        String link = att(topicEle, "LINK"); //$NON-NLS-1$
        if (link != null) {
            topic.setHyperlink(link);
        }

        checkInterrupted();
        Element ele = null;
        if (!topic.isRoot()) {
            ele = child(topicEle, "cloud"); //$NON-NLS-1$
            if (ele != null) {
                IBoundary boundary = getTargetWorkbook().createBoundary();
                int index = topic.getIndex();
                boundary.setStartIndex(index);
                boundary.setEndIndex(index);
                topic.getParent().addBoundary(boundary);
            }
        }

        checkInterrupted();
        ele = child(topicEle, "arrowlink"); //$NON-NLS-1$
        if (ele != null) {
            String toId = att(ele, "DESTINATION"); //$NON-NLS-1$
            if (toId != null) {
                ArrowLink arrowLink = new ArrowLink(id, toId);
                if (links == null)
                    links = new ArrayList<ArrowLink>();
                links.add(arrowLink);
            }
        }

        Iterator<Element> it = children(topicEle, "icon"); //$NON-NLS-1$
        while (it.hasNext()) {
            checkInterrupted();
            Element iconEle = it.next();
            String builtIn = att(iconEle, "BUILTIN"); //$NON-NLS-1$
            if (builtIn != null) {
                String markerId = getTransferred("marker", builtIn, null); //$NON-NLS-1$
                if (markerId != null) {
                    topic.addMarker(markerId);
                }
            }
        }

        it = children(topicEle, "hook"); //$NON-NLS-1$
        while (it.hasNext()) {
            checkInterrupted();
            Element hookEle = it.next();
            String type = att(hookEle, "NAME"); //$NON-NLS-1$
            if (type != null
                    && "accessories/plugins/NodeNote.properties".equals(type)) { //$NON-NLS-1$
                Element textEle = child(hookEle, "text"); //$NON-NLS-1$
                if (textEle != null) {
                    IPlainNotesContent notesContent = (IPlainNotesContent) getTargetWorkbook()
                            .createNotesContent(INotes.PLAIN);
                    notesContent.setTextContent(textEle.getTextContent());
                    topic.getNotes().setContent(INotes.PLAIN, notesContent);
                    break;
                }
            }
        }

        it = children(topicEle, "richcontent"); //$NON-NLS-1$
        while (it.hasNext()) {
            checkInterrupted();
            Element richEle = it.next();
            String type = att(richEle, "TYPE"); //$NON-NLS-1$
            if (type != null && "NOTE".equals(type)) { //$NON-NLS-1$
                Element htmlEle = child(richEle, "html"); //$NON-NLS-1$
                if (htmlEle != null) {
                    IHtmlNotesContent notesContent = (IHtmlNotesContent) getTargetWorkbook()
                            .createNotesContent(INotes.HTML);
                    new NotesImporter(notesContent).importFrom(htmlEle);
                    topic.getNotes().setContent(INotes.HTML, notesContent);
                    break;
                }
            }
        }

        it = children(topicEle, "node"); //$NON-NLS-1$
        while (it.hasNext()) {
            checkInterrupted();
            Element subTopicEle = it.next();
            ITopic subTopic = getTargetWorkbook().createTopic();
            topic.add(subTopic);
            loadTopic(subTopic, subTopicEle);
        }
    }

    private IStyleSheet getTempStyleSheet() {
        if (tempStyleSheet == null)
            tempStyleSheet = Core.getStyleSheetBuilder().createStyleSheet();
        return tempStyleSheet;
    }

    private static Element child(Element parentEle, String childTag) {
        return children(parentEle, childTag).next();
    }

    private static Iterator<Element> children(final Element parentEle,
            final String childTag) {
        return new Iterator<Element>() {

            String tag = DOMUtils.getLocalName(childTag);

            Iterator<Element> it = DOMUtils.childElementIter(parentEle);

            Element next = findNext();

            public void remove() {
            }

            private Element findNext() {
                while (it.hasNext()) {
                    Element ele = it.next();
                    if (DOMUtils.getLocalName(ele.getTagName())
                            .equalsIgnoreCase(tag)) {
                        return ele;
                    }
                }
                return null;
            }

            public Element next() {
                Element result = next;
                next = findNext();
                return result;
            }

            public boolean hasNext() {
                return next != null;
            }
        };
    }

    private static String att(Element ele, String attName) {
        if (ele.hasAttribute(attName))
            return ele.getAttribute(attName);

        attName = DOMUtils.getLocalName(attName);
        NamedNodeMap atts = ele.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node att = atts.item(i);
            if (attName.equalsIgnoreCase(DOMUtils.getLocalName(att
                    .getNodeName()))) {
                return att.getNodeValue();
            }
        }
        return null;
    }

    private static String getTransferred(String type, String sourceId,
            String defaultId) {
        if (sourceId != null) {
            ResourceMappingManager mappings = getMappings();
            if (mappings != null) {
                String destination = mappings.getDestination(type, sourceId);
                if (destination != null)
                    return destination;
            }
        }
        return defaultId;
    }

    private static ResourceMappingManager getMappings() {
        if (mappings == null) {
            mappings = createMappings();
        }
        return mappings;
    }

    private static ResourceMappingManager createMappings() {
        return FreeMindResourceMappingManager.getInstance();
    }

    private DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {
        if (documentBuilder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory
                    .setAttribute(
                            "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
                            true);
            documentBuilder = factory.newDocumentBuilder();
        }
        return documentBuilder;
    }

    public void error(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void warning(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

}