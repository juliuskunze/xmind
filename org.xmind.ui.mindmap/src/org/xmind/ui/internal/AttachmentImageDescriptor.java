/* ******************************************************************************
 * Copyright (c) 2006-2009 XMind Ltd. and others.
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
package org.xmind.ui.internal;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.xmind.core.IFileEntry;
import org.xmind.core.IWorkbook;

public class AttachmentImageDescriptor extends ImageDescriptor {

    private IWorkbook workbook;

    private String path;

    protected AttachmentImageDescriptor(IWorkbook workbook, String path) {
        if (workbook == null)
            throw new IllegalArgumentException("Workbook is null!"); //$NON-NLS-1$
        if (path == null)
            throw new IllegalArgumentException("Path is null!"); //$NON-NLS-1$
        this.workbook = workbook;
        this.path = path;
    }

    public ImageData getImageData() {
        ImageData imageData = null;
        InputStream in = getStream();
        if (in != null) {
            try {
                imageData = new ImageData(in);
            } catch (Exception e) {
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
        return imageData;
    }

    private InputStream getStream() {
        IFileEntry entry = workbook.getManifest().getFileEntry(path);
        if (entry == null)
            return null;
        InputStream stream = entry.getInputStream();
        if (stream == null)
            return null;
        return new BufferedInputStream(stream);
    }

    public String toString() {
        return "AttachmentImageDescriptor(workbook=" + workbook.toString()//$NON-NLS-1$ 
                + ",path=" + path + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof AttachmentImageDescriptor))
            return false;
        AttachmentImageDescriptor that = (AttachmentImageDescriptor) obj;
        return this.workbook.equals(that.workbook)
                && this.path.equals(that.path);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public static ImageDescriptor createFromEntryPath(IWorkbook workbook,
            String path) {
        return new AttachmentImageDescriptor(workbook, path);
    }

    public static ImageDescriptor createFromEntry(IWorkbook workbook,
            IFileEntry entry) {
        return new AttachmentImageDescriptor(workbook, entry.getPath());
    }

}