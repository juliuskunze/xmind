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
package org.xmind.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmind.core.ICloneData;

public class CloneData implements ICloneData {

    private Collection<Object> sources;

    private ICloneData parent;

    private Map<Object, Object> clonedElements;

    private Map<Object, List<ICloneDataListener>> listeners;

    private Map<Object, Object> caches;

    public CloneData(Collection<? extends Object> sources, ICloneData parent) {
        this.sources = new ArrayList<Object>(sources);
        this.parent = parent;
    }

    public Object get(Object source) {
        Object cloned = clonedElements == null ? null : clonedElements
                .get(source);
        if (cloned == null && parent != null)
            cloned = parent.get(source);
        return cloned;
    }

    public boolean hasCloned() {
        if (clonedElements != null) {
            for (Object cloned : clonedElements.values())
                if (cloned != null)
                    return true;
        }
        if (parent != null)
            return parent.hasCloned();
        return false;
    }

    public Collection<Object> getCloneds() {
        ArrayList<Object> list = new ArrayList<Object>(sources.size());
        for (Object source : sources) {
            Object cloned = get(source);
            if (cloned != null)
                list.add(cloned);
        }
        return list;
    }

    public Collection<Object> getSources() {
        return sources;
    }

    public ICloneData getParent() {
        return parent;
    }

    public void put(Object source, Object cloned) {
        if (clonedElements == null)
            clonedElements = new HashMap<Object, Object>();
        clonedElements.put(source, cloned);
        if (parent != null) {
            parent.put(source, cloned);
        }
        fireObjectCloned(source, cloned);
    }

    public void cache(Object key, Object value) {
        if (caches == null)
            caches = new HashMap<Object, Object>();
        caches.put(key, value);
    }

    public Object getCache(Object key) {
        return caches == null ? null : caches.get(key);
    }

    private void fireObjectCloned(Object source, Object cloned) {
        if (listeners == null || listeners.isEmpty())
            return;
        List<ICloneDataListener> list = listeners.get(source);
        if (list == null || list.isEmpty()) {
            listeners.remove(source);
            return;
        }
        for (Object o : list.toArray()) {
            ((ICloneDataListener) o).objectCloned(source, cloned);
        }
    }

    public void addCloneDataListener(Object source, ICloneDataListener listener) {
        if (listeners == null)
            listeners = new HashMap<Object, List<ICloneDataListener>>();
        List<ICloneDataListener> list = listeners.get(source);
        if (list == null) {
            list = new ArrayList<ICloneDataListener>();
            listeners.put(source, list);
        }
        list.add(listener);
    }

    public void removeCloneDataListener(Object source,
            ICloneDataListener listener) {
        if (listeners == null)
            return;
        List<ICloneDataListener> list = listeners.get(source);
        if (list == null)
            return;
        list.remove(listener);
        if (list.isEmpty())
            listeners.remove(source);
        if (listeners.isEmpty())
            listeners = null;
    }

    public boolean isCloned(Object source) {
        if (clonedElements != null && clonedElements.containsKey(source))
            return true;
        return parent != null && parent.isCloned(source);
    }

    public String getString(String sourceString) {
        Object cloned = clonedElements == null ? null : clonedElements
                .get(sourceString);
        if ((cloned == null || !(cloned instanceof String)) && parent != null) {
            cloned = parent.get(sourceString);
        }
        return cloned instanceof String ? (String) cloned : null;
    }

}