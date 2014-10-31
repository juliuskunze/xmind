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
package org.xmind.ui.internal.wizards;

import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.xmind.core.IFileEntryFilter;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.internal.zip.ZipStreamOutputTarget;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.internal.editor.MME;

public abstract class AbstractTemplateDescriptor implements ITemplateDescriptor {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private ImageDescriptor image = null;

    public ImageDescriptor getImage() {
        return image;
    }

    public void setImage(ImageDescriptor image) {
        ImageDescriptor oldImage = this.image;
        this.image = image;
        pcs.firePropertyChange(PROP_IMAGE, oldImage, image);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    public IEditorInput createEditorInput() {
        InputStream stream = newStream();
        if (stream != null) {
            return MME.createTemplatedEditorInput(removeRevisions(stream));
        }
        return MME.createNonExistingEditorInput();
    }

    private InputStream removeRevisions(InputStream stream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            ZipOutputStream zip = new ZipOutputStream(buffer);
            try {
                FileUtils.extractZipStream(stream, new ZipStreamOutputTarget(
                        zip), new IFileEntryFilter() {
                    public boolean select(String path, String mediaType,
                            boolean isDirectory) {
                        return !path
                                .startsWith(ArchiveConstants.PATH_REVISIONS);
                    }
                });
            } finally {
                zip.close();
            }
        } catch (Exception e) {
            return newStream();
        } finally {
            try {
                buffer.close();
            } catch (IOException e) {
            }
        }
        return new ByteArrayInputStream(buffer.toByteArray());
    }
}