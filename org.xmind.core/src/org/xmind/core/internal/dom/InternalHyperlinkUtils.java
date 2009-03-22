/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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

import java.io.IOException;

import org.xmind.core.IFileEntry;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;

public class InternalHyperlinkUtils {

    public static void activateHyperlink(IWorkbook workbook, String url) {
        if (workbook != null && HyperlinkUtils.isAttachmentURL(url)) {
            String attPath = HyperlinkUtils.toAttachmentPath(url);
            increaseFileEntryRef(workbook, attPath);
        }
    }

    public static void increaseFileEntryRef(IWorkbook workbook, String entryPath) {
        if (workbook != null && entryPath != null) {
            IFileEntry e = workbook.getManifest().getFileEntry(entryPath);
            if (e != null) {
                e.increaseReference();
                if (e.isDirectory()) {
                    for (IFileEntry sub : e.getSubEntries()) {
                        sub.increaseReference();
                    }
                }
            }
        }
    }

    public static void deactivateHyperlink(IWorkbook workbook, String url) {
        if (workbook != null && HyperlinkUtils.isAttachmentURL(url)) {
            String attPath = HyperlinkUtils.toAttachmentPath(url);
            decreaseFileEntryRef(workbook, attPath);
        }
    }

    public static void decreaseFileEntryRef(IWorkbook workbook, String entryPath) {
        if (workbook != null && entryPath != null) {
            IFileEntry e = workbook.getManifest().getFileEntry(entryPath);
            if (e != null) {
                e.decreaseReference();
                if (e.isDirectory()) {
                    for (IFileEntry sub : e.getSubEntries()) {
                        sub.decreaseReference();
                    }
                }
            }
        }
    }

    public static String importAttachmentURL(String sourceHyperlink,
            IWorkbook sourceWorkbook, IWorkbook targetWorkbook)
            throws IOException {
        String sourcePath = HyperlinkUtils.toAttachmentPath(sourceHyperlink);
        String targetPath = importAttachment(sourcePath, sourceWorkbook,
                targetWorkbook);
        return HyperlinkUtils.toAttachmentURL(targetPath);
    }

    public static String importAttachment(String sourcePath,
            IWorkbook sourceWorkbook, IWorkbook targetWorkbook)
            throws IOException {
        IFileEntry sourceEntry = sourceWorkbook.getManifest().getFileEntry(
                sourcePath);
        if (sourceEntry != null) {
            IFileEntry targetEntry = targetWorkbook.getManifest()
                    .cloneEntryAsAttachment(sourceEntry);
            if (targetEntry != null) {
                return targetEntry.getPath();
            }
        }
        return sourcePath;
    }

}