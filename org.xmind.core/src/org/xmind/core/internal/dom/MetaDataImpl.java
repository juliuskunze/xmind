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
package org.xmind.core.internal.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.IMeta;
import org.xmind.core.IMetaData;
import org.xmind.core.internal.MetaData;
import org.xmind.core.util.DOMUtils;

public class MetaDataImpl extends MetaData {

    private Element implementation;

    private MetaImpl ownedMeta;

    public MetaDataImpl(Element implementation, MetaImpl ownedMeta) {
        this.implementation = implementation;
        this.ownedMeta = ownedMeta;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MetaDataImpl))
            return false;
        MetaDataImpl that = (MetaDataImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(implementation);
    }

    public Object getAdapter(Class adapter) {
        if (adapter == Node.class || adapter == Element.class)
            return getImplementation();
        return super.getAdapter(adapter);
    }

    public Element getImplementation() {
        return implementation;
    }

    public void addMetaData(IMetaData data) {
        Element mdEle = ((MetaDataImpl) data).getImplementation();
        implementation.appendChild(mdEle);
    }

    public void removeMetaData(IMetaData data) {
        Element mdEle = ((MetaDataImpl) data).getImplementation();
        implementation.removeChild(mdEle);
    }

    public String getAttribute(String key) {
        return DOMUtils.getAttribute(implementation, key);
    }

    public String getKey() {
        return implementation.getTagName();
    }

    public IMetaData[] getMetaData(String key) {
        List<IMetaData> list = new ArrayList<IMetaData>();
        Iterator<Element> it = DOMUtils.childElementIterByTag(implementation,
                key);
        while (it.hasNext()) {
            Element mdEle = it.next();
            list.add(ownedMeta.getMetaData(mdEle));
        }
        return list.toArray(new IMetaData[list.size()]);
    }

    public IMeta getOwnedMeta() {
        return ownedMeta;
    }

    public String getValue() {
        return implementation.getTextContent();
    }

    public void setAttribute(String key, String value) {
        DOMUtils.setAttribute(implementation, key, value);
    }

    public void setValue(String value) {
        implementation.setTextContent(value);
    }

}