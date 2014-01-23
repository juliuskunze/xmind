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
package org.xmind.core.internal.sharing;

import static org.xmind.core.sharing.SharingConstants.COMMAND_VERSION;
import static org.xmind.core.sharing.SharingConstants.PLUGIN_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_MAPS;
import static org.xmind.core.sharing.SharingConstants.PROP_NAME;
import static org.xmind.core.sharing.SharingConstants.PROP_THUMBNAIL;
import static org.xmind.core.sharing.SharingConstants.PROP_VERSION;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;
import org.xmind.core.command.ReturnValue;
import org.xmind.core.command.arguments.ArrayMapper;
import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.SharingConstants;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class HandshakeCommandHandler implements ICommandHandler {

    public HandshakeCommandHandler() {
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        ISharedLibrary localLibrary = LocalNetworkSharing.getDefault()
                .getSharingService().getLocalLibrary();
        Attributes attrs = new Attributes();
        attrs.with(PROP_VERSION, COMMAND_VERSION);
        attrs.with(PROP_NAME, localLibrary.getName());
        ArrayMapper mapsWriter = new ArrayMapper(attrs.getRawMap(), PROP_MAPS);
        for (ISharedMap map : localLibrary.getMaps()) {
            mapsWriter.next();
            mapsWriter.set(PROP_ID, map.getID());
            mapsWriter.set(PROP_NAME, map.getResourceName());
            String thumbnail = ((LocalSharedMap) map).getEncodedThumbnailData();
            mapsWriter.set(PROP_THUMBNAIL, thumbnail);
            mapsWriter.set(SharingConstants.PROP_MISSING,
                    map.isMissing() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        mapsWriter.setSize();
        monitor.done();
        return new ReturnValue(PLUGIN_ID, attrs);
    }

}
