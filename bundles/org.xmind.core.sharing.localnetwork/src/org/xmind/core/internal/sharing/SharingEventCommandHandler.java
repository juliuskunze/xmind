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

import static org.xmind.core.sharing.SharingConstants.CODE_MISSING_ARGUMENT;
import static org.xmind.core.sharing.SharingConstants.PLUGIN_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_MAP;
import static org.xmind.core.sharing.SharingConstants.PROP_MAPS;
import static org.xmind.core.sharing.SharingConstants.PROP_MISSING;
import static org.xmind.core.sharing.SharingConstants.PROP_MODIFIED_TIME;
import static org.xmind.core.sharing.SharingConstants.PROP_NAME;
import static org.xmind.core.sharing.SharingConstants.PROP_REMOTE;
import static org.xmind.core.sharing.SharingConstants.PROP_THUMBNAIL;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;
import org.xmind.core.command.arguments.ArrayMapper;
import org.xmind.core.command.arguments.Attributes;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.SharingEvent;
import org.xmind.core.sharing.SharingEvent.Type;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharingEventCommandHandler implements ICommandHandler {

    public SharingEventCommandHandler() {
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        if (matches.length < 1) {
            return new Status(IStatus.WARNING, PLUGIN_ID,
                    CODE_MISSING_ARGUMENT, "Missing event type.", null); //$NON-NLS-1$
        }
        String type = matches[0];
        handleRemoteEvent(type, command);
        return Status.OK_STATUS;
    }

    private void handleRemoteEvent(String type, ICommand command) {
        LocalNetworkSharingService service = (LocalNetworkSharingService) LocalNetworkSharing
                .getDefault().getSharingService();
        String remoteSymbolicName = command.getArgument(PROP_REMOTE);
        if (remoteSymbolicName == null)
            return;

        RemoteSharedLibrary remoteLibrary = (RemoteSharedLibrary) service
                .findRemoteLibrary(remoteSymbolicName);
        if (remoteLibrary == null)
            return;

        Type eventType = SharingEvent.Type.valueOf(type);
        if (eventType == null)
            return;

        RemoteSharedMap map = null;
        if (eventType == SharingEvent.Type.SHARED_MAP_ADDED
                || eventType == SharingEvent.Type.SHARED_MAP_UPDATED) {
            String mapID = command.getArgument(PROP_MAP);
            if (mapID != null) {
                String mapName = command.getArgument(PROP_NAME);
                byte[] thumbnailData = Base64.base64ToByteArray(command
                        .getArgument(PROP_THUMBNAIL));
                boolean missing = Boolean.parseBoolean(command
                        .getArgument(PROP_MISSING));
                String time = command.getArgument(PROP_MODIFIED_TIME);
                long modifiedTime = 0;
                if (time != null)
                    modifiedTime = Long.parseLong(time);

                map = (RemoteSharedMap) remoteLibrary.findMapByID(mapID);
                if (map == null) {
                    map = new RemoteSharedMap(
                            remoteLibrary.getRemoteCommandService(),
                            remoteLibrary, mapID, mapName, thumbnailData,
                            missing);
                    map.setResourceModifiedTime(modifiedTime);
                    remoteLibrary.addMap(map);
                } else {
                    map.setResourceName(mapName);
                    map.setMissing(missing);
                    map.setThumbnailData(thumbnailData);
                    map.setResourceModifiedTime(modifiedTime);
                    map.invalidateResourceCache();
                }
            }
        } else if (eventType == SharingEvent.Type.SHARED_MAP_REMOVED) {
            String mapID = command.getArgument(PROP_MAP);
            if (mapID != null) {
                ISharedMap mapToRemove = remoteLibrary.findMapByID(mapID);
                if (mapToRemove != null) {
                    remoteLibrary.removeMap(mapToRemove);
                }
            }
        } else if (eventType == SharingEvent.Type.LIBRARY_NAME_CHANGED) {
            String name = command.getArgument(PROP_NAME);
            remoteLibrary.setName(name == null ? "" : name); //$NON-NLS-1$
            service.getContactManager().updateContactName(
                    remoteLibrary.getContactID(), name);
        } else if (eventType == SharingEvent.Type.CONTACT_ADDED) {
            Attributes attrs = command.getArguments();
            if (!attrs.isEmpty()) {
                ArrayMapper mapsReader = new ArrayMapper(attrs.getRawMap(),
                        PROP_MAPS);
                List<ISharedMap> maps = new ArrayList<ISharedMap>(
                        mapsReader.getSize());
                while (mapsReader.hasNext()) {
                    mapsReader.next();
                    String mapID = (String) mapsReader.get(PROP_MAP);
                    String mapName = (String) mapsReader.get(PROP_NAME);
                    String thumbnailData = (String) mapsReader
                            .get(PROP_THUMBNAIL);
                    String missing = (String) mapsReader.get(PROP_MISSING);

                    long modifiedTime = 0;
                    String time = (String) mapsReader.get(PROP_MODIFIED_TIME);
                    if (time != null) {
                        modifiedTime = Long.parseLong(time);
                    }
                    map = new RemoteSharedMap(
                            remoteLibrary.getRemoteCommandService(),
                            remoteLibrary, mapID, mapName,
                            Base64.base64ToByteArray(thumbnailData),
                            Boolean.parseBoolean(missing));
                    map.setResourceModifiedTime(modifiedTime);
                    maps.add(map);
                }
                remoteLibrary.addMaps(maps);
                map = null;
            }
        }

        service.fireSharingEvent(new SharingEvent(eventType, remoteLibrary, map));
    }
}
