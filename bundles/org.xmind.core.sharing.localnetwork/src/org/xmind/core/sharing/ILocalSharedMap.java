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

/**
 * 
 * @author Frank Shaka
 * 
 */
public interface ILocalSharedMap extends ISharedMap {

    /**
     * Returns the most recent time (in milliseconds) when this map was
     * modified.
     * 
     * @return the modified time in milliseconds
     */
    long getResourceModifiedTime();

    /**
     * Returns the time (in milliseconds) when this map was added into the
     * shared library.
     * 
     * @return the added time in milliseconds
     */
    long getAddedTime();

    /**
     * Returns the absolute path of this map.
     * 
     * @return the absolute path
     */
    String getResourcePath();

}
