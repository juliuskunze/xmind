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
package org.xmind.ui.internal.sharing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.sharing.ISharedMap;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharedMapSelection {

    private static final List<ISharedMap> NO_MAPS = Collections.emptyList();

    private boolean localOnly;

    private boolean excludeMissing;

    private List<ISharedMap> selectedMaps = null;

    public SharedMapSelection(boolean localOnly, boolean excludeMissing) {
        this.localOnly = localOnly;
        this.excludeMissing = excludeMissing;
    }

    public ISharedMap[] toArray() {
        return selectedMaps == null ? new ISharedMap[0] : selectedMaps
                .toArray(new ISharedMap[selectedMaps.size()]);
    }

    public List<ISharedMap> toList() {
        return selectedMaps == null ? NO_MAPS : Collections
                .unmodifiableList(selectedMaps);
    }

    public boolean hasSelectedMaps() {
        return selectedMaps != null && !selectedMaps.isEmpty();
    }

    public void setSelection(ISelection selection) {
        selectedMaps = collectLocalSharedMapsFromSelection(selection);
    }

    public int size() {
        return selectedMaps == null ? 0 : selectedMaps.size();
    }

    private List<ISharedMap> collectLocalSharedMapsFromSelection(
            ISelection selection) {
        if (selection != null && selection instanceof IStructuredSelection) {
            return collectLocalSharedMaps(((IStructuredSelection) selection)
                    .toArray());
        }
        return null;
    }

    private List<ISharedMap> collectLocalSharedMaps(Object[] elements) {
        List<ISharedMap> maps = new ArrayList<ISharedMap>(elements.length);
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] instanceof ISharedMap) {
                ISharedMap map = (ISharedMap) elements[i];
                if ((!localOnly || map.getSharedLibrary().isLocal())
                        && (!excludeMissing || !map.isMissing())) {
                    maps.add(map);
                }
            }
        }
        return maps;
    }

}
