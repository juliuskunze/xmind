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

import java.io.File;

/**
 * 
 * @author Frank Shaka
 * 
 */
public interface ILocalSharedLibrary extends ISharedLibrary {

    /**
     * Changes the display name of this library.
     * 
     * @param name
     *            new name to set
     */
    void setName(String name);

    /**
     * Adds a new shared map from local file.
     * 
     * @param file
     *            a local file to be added into this library
     * @return added shared map
     */
    ISharedMap addSharedMap(File file);

    /**
     * Removes the specified shared map.
     * 
     * @param map
     *            the shared map to be removed
     * @return <code>true</code> if map removed successfully, or
     *         <code>false</code> otherwise
     */
    boolean removeSharedMap(ISharedMap map);

}
