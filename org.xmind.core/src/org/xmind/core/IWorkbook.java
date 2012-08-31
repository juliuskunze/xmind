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
public interface IWorkbook extends IAdaptable, IModifiable {

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

    /**
     * 
     * @return
     */
    IBoundary createBoundary();

    /**
     * 
     * @return
     */
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
     * Finds an element with the given identifier string requested starting from
     * the source object.
     * 
     * @param id
     * @param source
     * @return
     */
    Object findElement(String id, IAdaptable source);

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
     * Finds the topic element with the given ID starting from the source
     * object.
     * 
     * @param id
     * @param source
     * @return
     */
    ITopic findTopic(String id, IAdaptable source);

    /**
     * 
     * @return
     */
    IStyleSheet getStyleSheet();

    /**
     * 
     * @return
     */
    IManifest getManifest();

    /**
     * 
     * @return
     */
    IMeta getMeta();

    /**
     * 
     * @return
     */
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

    /**
     * 
     * @return
     */
    String getVersion();

    /**
     * Clones the specified elements into this workbook and returns a session
     * object mapping source resources to their clones.
     * 
     * <p>
     * Note that the elements cloned will have newly assigned identifying
     * attributes, so that it's safe to keep the cloned elements together with
     * their sources within one workbook.
     * </p>
     * 
     * @param sources
     * @return
     */
    ICloneData clone(Collection<? extends Object> sources);

    /**
     * 
     * @param topic
     * @return
     */
    ITopic cloneTopic(ITopic topic);

    /**
     * Imports the specified element into this workbook. The imported element
     * will have the same identifying attributes with the source element, so
     * this method is primarily used to replace the existing element that has
     * the same identifying attributes.
     * 
     * <p>
     * Note that adding imported elements into this workbook without first
     * removing the source element may cause unknown behavior when invocating
     * <code>getElementById()</code> or <code>findElement()</code>.
     * </p>
     * 
     * @param source
     *            the element to import
     * @return the imported element
     */
    IAdaptable importElement(IAdaptable source);

    /**
     * 
     * @param resourceType
     * @param resourceId
     * @return
     */
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

    /**
     * 
     * @return
     */
    IRevisionRepository getRevisionRepository();

}