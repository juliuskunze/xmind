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

import java.io.InputStream;
import java.io.OutputStream;

import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.AbstractMarkerResource;
import org.xmind.core.marker.IMarker;

public class WorkbookMarkerResource extends AbstractMarkerResource {

    private WorkbookImpl workbook;

    public WorkbookMarkerResource(WorkbookImpl workbook, IMarker marker) {
        super(marker, ArchiveConstants.PATH_MARKERS);
        this.workbook = workbook;
    }

    public InputStream getInputStream() {
//        IArchivedWorkbook aw = getArchivedWorkbook();
//        if (aw != null)
//            return aw.getEntryInputStream(getFullPath());
        try {
            IStorage storage = getStorage();
            IInputSource inputSource = storage.getInputSource();
            String fullPath = getFullPath();
            InputStream entryStream = inputSource.getEntryStream(fullPath);
            return entryStream;
        } catch (CoreException e) {
            Core.getLogger().log(e);
        }
        return null;
    }

    public OutputStream getOutputStream() {
//        IArchivedWorkbook aw = getArchivedWorkbook();
//        if (aw != null)
//            return aw.getEntryOutputStream(getFullPath());
        try {
            return getStorage().getOutputTarget().getEntryStream(getFullPath());
        } catch (CoreException e) {
            Core.getLogger().log(e);
        }
        return null;
    }

    private IStorage getStorage() {
        return workbook.getTempStorage();
    }

//    private IArchivedWorkbook getArchivedWorkbook() {
//        IArchivedWorkbook aw = workbook.getTempArchivedWorkbook();
//        if (aw == null)
//            aw = workbook.getArchivedWorkbook();
//        return aw;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof WorkbookMarkerResource))
            return false;
        WorkbookMarkerResource that = (WorkbookMarkerResource) obj;
        return this.workbook.equals(that.workbook) && super.equals(obj);
    }

}