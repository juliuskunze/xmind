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

public abstract class AbstractMarkerResource implements IMarkerResource {

    private String mainPath;

    private IMarker marker;

    public AbstractMarkerResource(IMarker marker) {
        this(marker, null);
    }

    public AbstractMarkerResource(IMarker marker, String mainPath) {
        if (marker == null)
            throw new IllegalArgumentException();
        this.marker = marker;
        this.mainPath = mainPath == null ? "/" : mainPath; //$NON-NLS-1$
    }

    protected IMarker getMarker() {
        return marker;
    }

    public String getPath() {
        return marker.getResourcePath();
    }

    public String getFullPath() {
        String path = getPath();
        if (!path.startsWith("/")) { //$NON-NLS-1$
            path = getMainPath() + path;
        }
        return path;
    }

    protected String getMainPath() {
        return mainPath;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof AbstractMarkerResource))
            return false;
        AbstractMarkerResource that = (AbstractMarkerResource) obj;
        return this.marker.equals(that.marker);
    }
}