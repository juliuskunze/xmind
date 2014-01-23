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

import static org.xmind.core.internal.zip.ArchiveConstants.PATH_MARKER_SHEET;

import org.xmind.core.IFileEntry;
import org.xmind.core.internal.AbstractRefCounter;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.IMarkerRefCounter;

public class WorkbookMarkerRefCounter extends AbstractRefCounter implements
        IMarkerRefCounter {

    private MarkerSheetImpl sheet;

    private ManifestImpl manifest;

    WorkbookMarkerRefCounter(MarkerSheetImpl sheet, ManifestImpl manifest) {
        this.sheet = sheet;
        this.manifest = manifest;
    }

    protected Object findResource(String resourceId) {
        return sheet.findMarker(resourceId);
    }

    protected void postIncreaseRef(String resourceId, Object resource) {
        IMarker marker = (IMarker) resource;
        if (sheet.equals(marker.getOwnedSheet())) {
            IMarkerGroup group = marker.getParent();
            if (group != null) {
                if (group.getParent() == null) {
                    sheet.addMarkerGroup(group);
                    for (IMarker m : group.getMarkers()) {
                        IMarkerResource res = m.getResource();
                        if (res instanceof WorkbookMarkerResource) {
                            String fullPath = ((WorkbookMarkerResource) res)
                                    .getFullPath();
                            IFileEntry e = manifest.getFileEntry(fullPath);
                            if (e == null)
                                e = manifest.createFileEntry(fullPath,
                                        FileUtils.getMediaType(fullPath));
                            e.increaseReference();
                        }
                    }
                }

                IFileEntry sheetEntry = manifest.getFileEntry(PATH_MARKER_SHEET);
                if (sheetEntry == null) {
                    sheetEntry = manifest.createFileEntry(PATH_MARKER_SHEET);
                }
                sheetEntry.increaseReference();
            }
        }
    }

    protected void postDecreaseRef(String resourceId, Object resource) {
        IMarker marker = (IMarker) resource;
        if (sheet.equals(marker.getOwnedSheet())) {
            IMarkerGroup group = marker.getParent();
            if (group != null) {
                IMarkerSheet parent = group.getParent();
                if (sheet.equals(parent)) {
                    if (!isGroupReferenced(group)) {
                        parent.removeMarkerGroup(group);
                        for (IMarker m : group.getMarkers()) {
                            IMarkerResource res = m.getResource();
                            if (res instanceof WorkbookMarkerResource) {
                                String fullPath = ((WorkbookMarkerResource) res)
                                        .getFullPath();
                                IFileEntry e = manifest.getFileEntry(fullPath);
                                if (e != null) {
                                    e.decreaseReference();
                                }
                            }
                        }
                    }
                }
            }

            if (sheet.isEmpty()) {
                IFileEntry entry = manifest.getFileEntry(PATH_MARKER_SHEET);
                if (entry != null) {
                    entry.decreaseReference();
                }
            }
        }
    }

    private boolean isGroupReferenced(IMarkerGroup group) {
        for (IMarker marker : group.getMarkers()) {
            int c = getRefCount(marker.getId());
            if (c > 0)
                return true;
        }
        return false;
    }

}