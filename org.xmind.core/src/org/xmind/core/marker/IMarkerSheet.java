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
package org.xmind.core.marker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.io.IInputSource;

public interface IMarkerSheet extends IAdaptable {

    List<IMarkerGroup> getMarkerGroups();

    IMarkerGroup createMarkerGroup(boolean singleton);

    IMarker createMarker(String resourcePath);

    void addMarkerGroup(IMarkerGroup group);

    void removeMarkerGroup(IMarkerGroup group);

    void save(OutputStream out) throws IOException, CoreException;

    void setParentSheet(IMarkerSheet parent);

    IMarkerSheet getParentSheet();

    IMarkerGroup findMarkerGroup(String groupId);

    IMarker findMarker(String markerId);

    boolean isEmpty();

    boolean isPermanent();

    void importFrom(IInputSource source) throws IOException, CoreException;

    void importFrom(IInputSource source, String groupName) throws IOException,
            CoreException;

    void importFrom(String sourcePath) throws IOException, CoreException;

    void importFrom(IMarkerSheet sheet);

    IMarkerGroup importGroup(IMarkerGroup group);

}