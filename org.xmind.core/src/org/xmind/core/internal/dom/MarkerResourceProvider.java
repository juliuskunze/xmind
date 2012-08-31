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
package org.xmind.core.internal.dom;

import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerResourceProvider;

public class MarkerResourceProvider implements
        IMarkerResourceProvider {

    private IInputSource source;

    private IOutputTarget target;

    public MarkerResourceProvider(IInputSource source, IOutputTarget target) {
        this.source = source;
        this.target = target;
    }

    public IMarkerResource getMarkerResource(IMarker marker) {
        return new MarkerResource(marker, source, target);
    }

    public boolean isPermanent() {
        return false;
    }

}