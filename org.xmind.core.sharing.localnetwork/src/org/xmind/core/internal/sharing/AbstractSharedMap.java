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
package org.xmind.core.internal.sharing;

import java.util.Comparator;

import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;

/**
 * 
 * @author Frank Shaka
 * 
 */
public abstract class AbstractSharedMap implements ISharedMap {

    public static final Comparator<ISharedMap> MAP_COMPARATOR = new Comparator<ISharedMap>() {
        public int compare(ISharedMap o1, ISharedMap o2) {
            int d = o1.getResourceName().compareTo(o2.getResourceName());
            if (d == 0) {
                d = o1.getID().compareTo(o2.getID());
            }
            return d;
        }
    };

    private ISharedLibrary library;

    private String id;

    private String name;

    private byte[] thumbnailData;

    private boolean missing;

    public AbstractSharedMap(ISharedLibrary library, String id) {
        this(library, id, null, null);
    }

    public AbstractSharedMap(ISharedLibrary library, String id, String name,
            byte[] thumbnailData) {
        this.library = library;
        this.id = id;
        this.name = name;
        this.thumbnailData = thumbnailData;
        this.missing = false;
    }

    public ISharedLibrary getSharedLibrary() {
        return library;
    }

    public String getID() {
        return id;
    }

    public String getResourceName() {
        return name == null ? "" : name; //$NON-NLS-1$
    }

    public byte[] getThumbnailData() {
        return thumbnailData;
    }

    public void setResourceName(String name) {
        this.name = name;
    }

    public void setThumbnailData(byte[] thumbnailData) {
        this.thumbnailData = thumbnailData;
    }

    public boolean isMissing() {
        return this.missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }

    @Override
    public int hashCode() {
        return (getClass().getName() + getID()).hashCode();
    }

}
