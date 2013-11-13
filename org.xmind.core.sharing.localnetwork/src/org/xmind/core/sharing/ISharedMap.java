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
package org.xmind.core.sharing;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A shared map represents a map shared by the local user or a remote user.
 * 
 * @author Frank Shaka
 */
public interface ISharedMap {

    /**
     * The unique identifier of this map. This should at least be unique amongst
     * all maps shared by the same user.
     * 
     * @return the unqieu identifier of this map
     */
    String getID();

    /**
     * Returns the library where this map is shared from.
     * 
     * @return the library where this map is shared from
     */
    ISharedLibrary getSharedLibrary();

    /**
     * Returns the name of this map.
     * 
     * @return the name of this map
     */
    String getResourceName();

    /**
     * Returns the thumbnail data of this map.
     * 
     * @return the thumbnail data of this map
     */
    byte[] getThumbnailData();

    /**
     * Opens an input stream for the contents of this map.
     * 
     * @param loadingProgress
     * @return
     */
    InputStream getResourceAsStream(IProgressMonitor loadingProgress);

    /**
     * Determines whether this map is missing or not.
     * 
     * @return <code>true</code> if this map is missing, or <code>false</code>
     *         if the map exists
     */
    boolean isMissing();

}
