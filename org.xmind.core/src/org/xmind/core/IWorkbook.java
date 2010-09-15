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
package org.xmind.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;

/**
 * @author briansun
 * 
 */
public interface IWorkbook extends IAdaptable {

    /**
     * @return
     */
    ITopic createTopic();

    /**
     * @return
     */

    ISheet createSheet();

    /**
     * @param end1
     * @param end2
     * @return
     */
    IRelationship createRelationship(IRelationshipEnd end1,
            IRelationshipEnd end2);

    /**
     * @return
     */
    IRelationship createRelationship();

    IBoundary createBoundary();

    ISummary createSummary();

    /**
     * 
     */
    List<ISheet> getSheets();

    /**
     * 
     * @return
     */
    ISheet getPrimarySheet();

    /**
     * 
     * @param sheet
     */
    void addSheet(ISheet sheet);

    /**
     * 
     * @param sheet
     * @param index
     */
    void addSheet(ISheet sheet, int index);

    /**
     * 
     * @param sheet
     */
    void removeSheet(ISheet sheet);

    /**
     * 
     * @param sourceIndex
     * @param targetIndex
     */
    void moveSheet(int sourceIndex, int targetIndex);

    /**
     * Gets an element with the given identifier string.
     * 
     * @param id
     *            The identifier string of the desired element
     * @return The element with the given identifier string.
     */
    Object getElementById(String id);

    /**
     * Gets a Topic element with the given id. The topic returned is the same
     * as:
     * 
     * <pre>
     * Object element = getElementById(id);
     * return element instanceof ITopic ? (ITopic) element : null;
     * </pre>
     * 
     * @see #getElementById(String)
     * @param id
     * @return
     */
    ITopic findTopic(String id);

    /**
     * 
     * @return
     */
    IStyleSheet getStyleSheet();

    IManifest getManifest();

    IMeta getMeta();

    ITopic cloneTopic(ITopic topic);

    IMarkerSheet getMarkerSheet();

    /**
     * @see org.xmind.core.INotes#PLAIN
     * @see org.xmind.core.INotes#HTML
     * @see org.xmind.core.IPlainNotesContent
     * @see org.xmind.core.IHtmlNotesContent
     * @param format
     * @return
     */
    INotesContent createNotesContent(String format);

    String getVersion();

    ICloneData clone(Collection<? extends Object> sources);

    IResourceRef createResourceRef(String resourceType, String resourceId);

    /**
     * 
     * @throws IOException
     * @throws CoreException
     */
    void save() throws IOException, CoreException;

    /**
     * 
     * @param file
     * @throws IOException
     * @throws CoreException
     */
    void save(String file) throws IOException, CoreException;

    /**
     * 
     * @param output
     * @throws IOException
     * @throws CoreException
     */
    void save(OutputStream output) throws IOException, CoreException;

    /**
     * 
     * @param target
     * @throws IOException
     * @throws CoreException
     */
    void save(IOutputTarget target) throws IOException, CoreException;

    /**
     * 
     * @return
     */
    String getFile();

    /**
     * 
     * @param file
     */
    void setFile(String file);

    /**
     * 
     * @param storage
     */
    void setTempStorage(IStorage storage);

    /**
     * 
     * @return
     */
    IStorage getTempStorage();

    /**
     * 
     * @param tempLocation
     */
    void setTempLocation(String tempLocation);

    /**
     * 
     * @return
     */
    String getTempLocation();

    /**
     * 
     * @throws IOException
     * @throws CoreException
     */
    void saveTemp() throws IOException, CoreException;

    /**
     * @param password
     */
    void setPassword(String password);

    /**
     * @return
     */
    String getPassword();

}