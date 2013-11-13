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
package org.xmind.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public interface IFileEntry extends IAdaptable, IWorkbookComponent {

    String getPath();

    String getMediaType();

    boolean hasBeenReferred();

    int getReferenceCount();

    void increaseReference();

    void decreaseReference();

    /**
     * Opens a new input stream for reading data from this file entry.
     * 
     * <p>
     * <strong>For dianostic purpose, this method is not recommended any more.
     * Use {@link #openInputStream()} instead to let potential I/O errors be
     * thrown.</strong>
     * </p>
     * 
     * @return an input stream for this file entry, or <code>null</code> if the
     *         input stream is not available
     */
    InputStream getInputStream();

    /**
     * Opens a new output stream for writing data to this file entry.
     * 
     * <p>
     * <strong>For dianostic purpose, this method is not recommended any more.
     * Use {@link #openOutputStream()} instead to let potential I/O errors be
     * thrown.</strong>
     * </p>
     * 
     * @return an output stream for this file entry, or <code>null</code> if the
     *         output stream is not available
     */
    OutputStream getOutputStream();

    /**
     * Opens a new input stream for reading data from this file entry.
     * 
     * @return an input stream for this file entry
     * @throws IOException
     *             if I/O error occurs, this entry is not found in storage, or
     *             this entry is a directory
     */
    InputStream openInputStream() throws IOException;

    /**
     * Opens a new output stream for writing data to this file entry.
     * 
     * @return an output stream for this file entry
     * @throws IOException
     *             if I/O error occurs, or this entry is not available for
     *             writing
     */
    OutputStream openOutputStream() throws IOException;

    boolean isDirectory();

    List<IFileEntry> getSubEntries();

    Iterator<IFileEntry> iterSubEntries();

    /**
     * Returns the time this entry last modified.
     * 
     * @return The time this entry last modified; or -1 if this entry is not
     *         accessable or some error occurred while getting the time.
     */
    long getTime();

    void setTime(long time);

    long getSize();

    /**
     * @return
     */
    IEncryptionData getEncryptionData();

    /**
     * 
     * @return
     */
    IEncryptionData createEncryptionData();

    /**
     * 
     */
    void deleteEncryptionData();

}