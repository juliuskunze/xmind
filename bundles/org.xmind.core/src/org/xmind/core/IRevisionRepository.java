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

/**
 * The repository of resource revisions.
 * 
 * @author Frank Shaka &lt;frank@xmind.net&gt;
 */
public interface IRevisionRepository extends IAdaptable, IWorkbookComponent {

    /**
     * Gets the revision manager with corresponding resource ID. If no revision
     * manager is related to the specified resource ID, a new one will be
     * created with the specified content type.
     * 
     * @param resourceId
     *            the ID of the resource
     * @param contentType
     *            the content type of the resource, see {@link IRevision} for
     *            all available content types
     * @return the corresponding revision manager with the specified resource ID
     * @see IRevision
     * @see IRevisionManager
     */
    IRevisionManager getRevisionManager(String resourceId, String contentType);

}
