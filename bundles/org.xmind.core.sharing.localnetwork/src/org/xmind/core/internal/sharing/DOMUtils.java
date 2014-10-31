package org.xmind.core.internal.sharing;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtils {

    private DOMUtils() {
    }

    public static Element getFirstChildElementByTag(Node parent, String tag) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (isElementByTag(child, tag))
                return (Element) child;
        }
        return null;
    }

    public static Element[] childElementArrayByTag(Node parent, String tagName) {
        NodeList children = parent.getChildNodes();
        Element[] elements = new Element[children.getLength()];

        int index = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (isElementByTag(child, tagName))
                elements[index++] = (Element) child;
        }

        return elements;
    }

    public static boolean isElementByTag(Node node, String tagName) {
        if (!(node instanceof Element))
            return false;
        if (tagName == null)
            return true;
        Element element = (Element) node;
        String tag = element.getTagName();
        return tag.equals(tagName)
                || getLocalName(tag).equals(getLocalName(tagName));
    }

    public static String getLocalName(String qualifiedName) {
        int index = qualifiedName.indexOf(':');
        if (index >= 0)
            return qualifiedName.substring(index + 1);
        return qualifiedName;
    }

    public static Element createElement(Node parent, String tag) {
        Document doc = parent.getNodeType() == Node.DOCUMENT_NODE ? (Document) parent
                : parent.getOwnerDocument();
        Element e = doc.createElement(tag);
        parent.appendChild(e);
        return e;
    }

    public static void save(Node dom, OutputStream out, boolean closeOnFinish)
            throws IOException {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(dom), new StreamResult(out));
        } catch (TransformerException e) {
            throw new IOException(e.getMessage());
        } finally {
            if (closeOnFinish)
                out.close();
        }
    }

}
