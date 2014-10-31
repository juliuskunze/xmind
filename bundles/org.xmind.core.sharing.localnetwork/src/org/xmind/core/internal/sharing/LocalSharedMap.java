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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.sharing.ILocalSharedMap;
import org.xmind.core.sharing.ISharedLibrary;

/**
 * 
 * @author Frank Shaka
 * @author Jason Wong
 * 
 */
public class LocalSharedMap extends AbstractSharedMap implements
        ILocalSharedMap {

    private String path;

    private String encodedThumbnailData;

    private long addedTime = 0;

    private List<String> receiverIDs = new ArrayList<String>();

    public LocalSharedMap(ISharedLibrary library, String id) {
        super(library, id);
    }

    public void setResourcePath(String path) {
        this.path = path;
        File file = new File(path);

        // Trim extension name:
        String name = file.getName();
        int extIndex = name.lastIndexOf('.');
        if (extIndex >= 0) {
            name = name.substring(0, extIndex);
        }
        setResourceName(name);

        setMissing(!file.exists());
    }

    public String getResourcePath() {
        return path;
    }

    public void setEncodedThumbnailData(String data) {
        if (data == null || "".equals(data)) { //$NON-NLS-1$
            this.encodedThumbnailData = null;
            super.setThumbnailData(null);
        } else {
            this.encodedThumbnailData = data;
            super.setThumbnailData(Base64.base64ToByteArray(data));
        }
    }

    @Override
    public void setThumbnailData(byte[] data) {
        if (data != null && data.length == 0) {
            data = null;
        }
        super.setThumbnailData(data);
        this.encodedThumbnailData = data == null ? null : Base64
                .byteArrayToBase64(data);
    }

    public String getEncodedThumbnailData() {
        return encodedThumbnailData == null ? "" : encodedThumbnailData; //$NON-NLS-1$
    }

    public long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(long time) {
        this.addedTime = time;
    }

    public InputStream getResourceAsStream(IProgressMonitor progress) {
        String path = getResourcePath();
        if (path == null)
            return null;
        progress.beginTask(null, 1);
        try {
            return new FileInputStream(path);
        } catch (IOException e) {
            LocalNetworkSharing.log(
                    "Failed to load shared map content from local file: " //$NON-NLS-1$
                            + path, e);
            return null;
        } finally {
            progress.done();
        }
    }

    public void addReceiver(String receiverID) {
        receiverIDs.add(receiverID);
    }

    public void addReceivers(List<String> receiverIDs) {
        this.receiverIDs.addAll(receiverIDs);
    }

    public List<String> getReceiverIDs() {
        return this.receiverIDs;
    }

    public boolean hasAccessRight(String remoteID) {
        boolean isContact = LocalNetworkSharing.getDefault()
                .getSharingService().getContactManager().isContact(remoteID);
        if (remoteID == null)
            return true;

        if (!isContact)
            return false;

        if (receiverIDs.isEmpty() || receiverIDs.contains(remoteID))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LocalSharedMap))
            return false;
        LocalSharedMap that = (LocalSharedMap) obj;
        return this.getID().equals(that.getID());
    }

    @Override
    public String toString() {
        return "LocalSharedMap(" + getResourcePath() //$NON-NLS-1$
                + "@" + getID() //$NON-NLS-1$
                + ")"; //$NON-NLS-1$
    }
}
