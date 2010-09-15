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

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.internal.TopicExtensionElement;
import org.xmind.core.util.DOMUtils;

public class TopicExtensionElementImpl extends TopicExtensionElement {

    private Element implementation;

    private TopicImpl topic;

    private TopicExtensionImpl extension;

    public TopicExtensionElementImpl(Element implementation, TopicImpl topic,
            TopicExtensionImpl extension) {
        super();
        this.implementation = implementation;
        this.topic = topic;
        this.extension = extension;
    }

    public Element getImplementation() {
        return implementation;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof TopicExtensionElementImpl))
            return false;
        TopicExtensionElementImpl that = (TopicExtensionElementImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "{element:" + getName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getName() {
        return implementation.getTagName();
    }

    public ITopicExtensionElement createChild(String elementName) {
        Element childImpl = DOMUtils.createElement(implementation, elementName);
        TopicExtensionElementImpl child = new TopicExtensionElementImpl(
                childImpl, topic, extension);
        registerChild(child);
        topic.updateModifiedTime();
        return child;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.ITopicExtensionElement#getCreatedChild(java.lang.String)
     */
    public ITopicExtensionElement getCreatedChild(String elementName) {
        List<ITopicExtensionElement> children = getChildren(elementName);
        if (!children.isEmpty())
            return children.get(0);
        return createChild(elementName);
    }

    private void registerChild(TopicExtensionElementImpl child) {
        extension.registerElement(child);
    }

    private void unregisterChild(TopicExtensionElementImpl child) {
        extension.unregisterElement(child);
    }

    public void deleteChild(ITopicExtensionElement child) {
        TopicExtensionElementImpl c = (TopicExtensionElementImpl) child;
        Element childImpl = c.getImplementation();
        if (childImpl.getParentNode() == implementation) {
            unregisterChild(c);
            implementation.removeChild(childImpl);
            topic.updateModifiedTime();
        }
    }

    public void deleteChildren(String name) {
        Element[] children;
        if (name == null)
            children = DOMUtils.getChildElements(implementation);
        else
            children = DOMUtils.getChildElementsByTag(implementation, name);
        for (int i = 0; i < children.length; i++) {
            implementation.removeChild(children[i]);
        }
        if (children.length > 0)
            topic.updateModifiedTime();
    }

    public void deleteChildren() {
        deleteChildren(null);
    }

    public String getAttribute(String attrName) {
        return DOMUtils.getAttribute(implementation, attrName);
    }

    public List<ITopicExtensionElement> getChildren() {
        return DOMUtils.getChildList(implementation, null, extension);
    }

    public List<ITopicExtensionElement> getChildren(String elementName) {
        return DOMUtils.getChildList(implementation, elementName, extension);
    }

    public ITopicExtension getExtension() {
        return extension;
    }

    public ITopicExtensionElement getParent() {
        Node p = implementation.getParentNode();
        if (p == null || !(p instanceof Element))
            return null;
        return extension.getElement((Element) p);
    }

    public String getTextContent() {
        Node c = implementation.getFirstChild();
        if (c != null && c.getNodeType() == Node.TEXT_NODE)
            return c.getTextContent();
        return null;
    }

    public ITopic getTopic() {
        return topic;
    }

    public void setAttribute(String attrName, String attrValue) {
        DOMUtils.setAttribute(implementation, attrName, attrValue);
        topic.updateModifiedTime();
    }

    public void setTextContent(String text) {
        Node c = implementation.getFirstChild();
        if (text == null) {
            if (c != null) {
                implementation.removeChild(c);
                topic.updateModifiedTime();
            }
        } else {
            if (c != null && c.getNodeType() == Node.TEXT_NODE) {
                c.setTextContent(text);
            } else {
                Node t = implementation.getOwnerDocument().createTextNode(text);
                implementation.insertBefore(t, c);
            }
            topic.updateModifiedTime();
        }
    }

}