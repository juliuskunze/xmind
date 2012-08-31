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
package org.xmind.core.io;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public interface IInputSource {

    Iterator<String> NO_ENTRIES = new ArrayList<String>(0).iterator();

    /**
     * 
     * @param entryName
     * @return
     */
    boolean hasEntry(String entryName);

    /**
     * 
     * @return
     */
    Iterator<String> getEntries();

    /**
     * 
     * @param entryName
     * @return
     */
    InputStream getEntryStream(String entryName);

    /**
     * Returns the file size of the specific entry.
     * 
     * @param entryName
     *            The name of the entry
     * @return The file size of the specific entry; or <code>-1</code> if the
     *         entry does not exist
     */
    long getEntrySize(String entryName);

    /**
     * Returns the last modification time of the specific entry.
     * 
     * @param entryName
     *            The name of the entry
     * @return The last modification time of the specific entry; or
     *         <code>-1</code> if the entry does not exist
     */
    long getEntryTime(String entryName);

}