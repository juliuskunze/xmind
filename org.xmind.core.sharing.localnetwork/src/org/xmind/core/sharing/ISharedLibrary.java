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

import org.eclipse.core.runtime.IAdaptable;

/**
 * 
 * @author Frank Shaka
 * 
 */
public interface ISharedLibrary extends IAdaptable {

    /**
     * Gets the display name of this library. The name may be the user name of
     * the current operating system, or manually set. Never return
     * <code>null</code>.
     * 
     * @return the display name of this library
     */
    String getName();

    /**
     * Determines whether this library is local.
     * 
     * @return <code>true</code> if this library is local, otherwise
     *         <code>false</code>
     */
    boolean isLocal();

    /**
     * Retrieves all shared maps.
     * 
     * @return an array of shared maps
     */
    ISharedMap[] getMaps();

    /**
     * Determines whether this library has any shared maps. This is a convenient
     * method for checking map sharings without using {@link #getMaps()}.
     * 
     * @return <code>true</code> if this library contains shared maps, or
     *         <code>false</code> if none
     */
    boolean hasMaps();

    /**
     * Returns the number of shared maps in this library. This is a convenient
     * method for checking the map count without using {@link #getMaps()}.
     * 
     * @return an integer describing how many shared maps are in this library
     */
    int getMapCount();

    /**
     * Finds a specific shared map by searching for its ID.
     * 
     * @param mapID
     *            the unique identifier of the shared map to find
     * @return <code>null</code> if the shared map is not found
     */
    ISharedMap findMapByID(String mapID);

}
