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
import java.util.Iterator;
import java.util.List;

public interface IManifest extends IWorkbookComponent, IAdaptable {

    List<IFileEntry> getFileEntries();

    Iterator<IFileEntry> iterFileEntries();

    Iterator<IFileEntry> iterFileEntries(IFileEntryFilter filter);

    IFileEntry getFileEntry(String path);

    IFileEntry createFileEntry(String path);

    IFileEntry createFileEntry(String path, String mediaType);

    String makeAttachmentPath(String sourcePath);

    String makeAttachmentPath(String source, boolean directory);

    IFileEntry createAttachmentFromStream(InputStream stream, String sourceName)
            throws IOException;

    IFileEntry createAttachmentFromStream(InputStream stream,
            String sourceName, String mediaType) throws IOException;

    IFileEntry createAttachmentFromFilePath(String sourcePath)
            throws IOException;

    IFileEntry createAttachmentFromFilePath(String sourcePath, String mediaType)
            throws IOException;

    IFileEntry cloneEntry(IFileEntry sourceEntry, String targetPath)
            throws IOException;

    IFileEntry cloneEntryAsAttachment(IFileEntry sourceEntry)
            throws IOException;

    IEncryptionData getEncryptionData(String entryPath);

}