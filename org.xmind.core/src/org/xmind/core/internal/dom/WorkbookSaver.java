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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.CONTENT_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_MARKER_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_REVISIONS;
import static org.xmind.core.internal.zip.ArchiveConstants.REVISIONS_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.IChecksumStream;
import org.xmind.core.IEncryptionData;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.internal.security.Crypto;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.internal.zip.ZipStreamOutputTarget;
import org.xmind.core.io.DirectoryOutputTarget;
import org.xmind.core.io.ICloseableOutputTarget;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;

/**
 * @author frankshaka
 * 
 */
public class WorkbookSaver {

    /**
     * The workbook to save
     */
    private WorkbookImpl workbook;

    /**
     * The target to save to
     */
    private IOutputTarget target;

    /**
     * (Optional) The absolute path representing a ZIP file target
     */
    private String file;

    /**
     * Saved entry paths for one 'save' process
     */
    private Set<String> savedEntries;

    /**
     * Whether to skip revisions when saving.
     */
    private boolean skipRevisions = false;

    /**
     * @param workbook
     * @param file
     */
    public WorkbookSaver(WorkbookImpl workbook, String file) {
        super();
        this.workbook = workbook;
        this.file = file;
    }

    /**
     * @return the file path
     */
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @param skipRevisions
     *            the skipRevisions to set
     */
    public void setSkipRevisions(boolean skipRevisions) {
        this.skipRevisions = skipRevisions;
    }

    public boolean isSkipRevisions() {
        return skipRevisions;
    }

    /**
     * 
     * @throws IOException
     * @throws CoreException
     */
    public void save() throws IOException, CoreException {
        save(this.target, this.file);
    }

    /**
     * 
     * @param output
     * @throws IOException
     * @throws CoreException
     */
    public void save(OutputStream output) throws IOException, CoreException {
        save(new ZipStreamOutputTarget(new ZipOutputStream(output)), null);

        // the target can't be reused
        this.target = null;
    }

    public void save(String file) throws IOException, CoreException {
        save(null, file);

        // the target can't be reused
        this.target = null;
    }

    public void save(IOutputTarget target) throws IOException, CoreException {
        save(target, null);
    }

    /**
     * 
     * @param target
     * @param file
     * @throws IOException
     * @throws CoreException
     */
    private void save(IOutputTarget target, String file) throws IOException,
            CoreException {
        if (target == null) {
            if (file != null) {
                if (new File(file).isDirectory()) {
                    target = new DirectoryOutputTarget(file);
                } else {
                    target = new ZipStreamOutputTarget(new ZipOutputStream(
                            new FileOutputStream(file)));
                }
            }
        }
        this.file = file;

        if (target == null)
            throw new FileNotFoundException("No target to save."); //$NON-NLS-1$

        try {
            doSave(target);
        } finally {
            if (target instanceof ICloseableOutputTarget) {
                ((ICloseableOutputTarget) target).close();
            }
        }
    }

    private void doSave(IOutputTarget target) throws FileNotFoundException,
            IOException, CoreException {
        this.target = target;
        this.savedEntries = null;
        try {
            doSave();
        } finally {
            try {
                clearEncryptionData();
            } catch (Throwable ignore) {
            }
            this.savedEntries = null;
        }
    }

    /**
     * The main saving process.
     * 
     * @throws IOException
     * @throws CoreException
     */
    private void doSave() throws IOException, CoreException {
        saveMeta();
        saveContent();
        saveMarkerSheet();
        saveStyleSheet();
        if (!skipRevisions) {
            saveRevisions();
        }

        copyOtherStaff();

        saveManifest();
    }

    private void saveManifest() throws IOException, CoreException {
        saveDOM(workbook.getManifest(), target, MANIFEST_XML);
    }

    private void saveStyleSheet() throws IOException, CoreException {
        IStyleSheet styleSheet = workbook.getStyleSheet();
        if (!styleSheet.isEmpty()) {
            saveDOM(styleSheet, target, STYLES_XML);
        }
    }

    private void saveMarkerSheet() throws IOException, CoreException {
        IMarkerSheet markerSheet = workbook.getMarkerSheet();
        if (!markerSheet.isEmpty()) {
            saveDOM(markerSheet, target, PATH_MARKER_SHEET);
        }
    }

    private void saveRevisions() throws IOException, CoreException {
        Iterator<Element> it = DOMUtils.childElementIterByTag(
                workbook.getWorkbookElement(), TAG_SHEET);
        while (it.hasNext()) {
            Element sheetEle = it.next();
            String sheetId = sheetEle.getAttribute(ATTR_ID);
            IRevisionManager manager = workbook.getRevisionRepository()
                    .getRevisionManager(sheetId, IRevision.SHEET);
            String path = PATH_REVISIONS + sheetId + "/" + REVISIONS_XML; //$NON-NLS-1$
            saveDOM(manager, target, path);
        }
    }

    private void saveContent() throws IOException, CoreException {
        saveDOM(workbook, target, CONTENT_XML);
    }

    private void saveMeta() throws IOException, CoreException {
        saveDOM(workbook.getMeta(), target, META_XML);
    }

    private void copyOtherStaff() throws IOException, CoreException {
        IInputSource source = workbook.getTempStorage().getInputSource();
        copyAll(source, target);
    }

    private void copyAll(IInputSource source, IOutputTarget target) {
        IManifest manifest = workbook.getManifest();
        for (IFileEntry entry : manifest.getFileEntries()) {
            if (!entry.isDirectory()) {
                String entryPath = entry.getPath();
                if (shouldSaveEntry(entryPath)) {
                    copyEntry(source, target, entryPath);
                    markSaved(entryPath);
                }
            }
        }
    }

    private boolean shouldSaveEntry(String entryPath) {
        return entryPath != null
                && !"".equals(entryPath) //$NON-NLS-1$
                && !ArchiveConstants.MANIFEST_XML.equals(entryPath)
                && !hasBeenSaved(entryPath)
                && (!skipRevisions || !entryPath
                        .startsWith(ArchiveConstants.PATH_REVISIONS));
    }

    private void copyEntry(IInputSource source, IOutputTarget target,
            String entryPath) {
        try {
            InputStream in = getInputStream(source, entryPath);
            if (in != null) {
                OutputStream out = getOutputStream(target, entryPath);
                if (out != null) {
                    try {
                        FileUtils.transfer(in, out, true);
                    } finally {
                        long time = source.getEntryTime(entryPath);
                        if (time >= 0) {
                            target.setEntryTime(entryPath, time);
                        }
                        recordChecksum(entryPath, out);
                    }
                }
            }
        } catch (IOException e) {
            Core.getLogger().log(e);
        } catch (CoreException e) {
            Core.getLogger().log(e);
        }
    }

    private InputStream getInputStream(IInputSource source, String entryPath) {
        if (source.hasEntry(entryPath)) {
            return source.getEntryStream(entryPath);
        }
        return null;
    }

    /**
     * @param manifest
     */
    private void clearEncryptionData() {
        for (IFileEntry entry : workbook.getManifest().getFileEntries()) {
            entry.deleteEncryptionData();
        }
    }

    private boolean hasBeenSaved(String entryPath) {
        return savedEntries != null && savedEntries.contains(entryPath);
    }

    /**
     * @param entryPath
     */
    private void markSaved(String entryPath) {
        if (savedEntries == null)
            savedEntries = new HashSet<String>();
        savedEntries.add(entryPath);
    }

    /**
     * @param domAdapter
     * @param target
     * @param entryPath
     */
    private void saveDOM(IAdaptable domAdapter, IOutputTarget target,
            String entryPath) throws IOException, CoreException {
        OutputStream out = getOutputStream(target, entryPath);
        if (out != null) {
            try {
                DOMUtils.save(domAdapter, out, true);
            } finally {
                recordChecksum(entryPath, out);
                markSaved(entryPath);
            }
        }
    }

    /**
     * @param entryPath
     * @param out
     * @throws IOException
     */
    private void recordChecksum(String entryPath, Object checksumProvider)
            throws IOException {
        if (checksumProvider instanceof IChecksumStream) {
            IEncryptionData encData = workbook.getManifest().getEncryptionData(
                    entryPath);
            if (encData != null && encData.getChecksumType() != null) {
                String checksum = ((IChecksumStream) checksumProvider)
                        .getChecksum();
                if (checksum != null) {
                    encData.setAttribute(checksum, DOMConstants.ATTR_CHECKSUM);
                }
            }
        }
    }

    private OutputStream getOutputStream(IOutputTarget target, String entryPath)
            throws CoreException {
        if (!target.isEntryAvaialble(entryPath))
            return null;

        OutputStream out = target.getEntryStream(entryPath);
        if (out == null)
            return null;

        String password = workbook.getPassword();
        if (password == null)
            return out;

        IFileEntry entry = workbook.getManifest().getFileEntry(entryPath);
        if (entry == null)
            return out;

        if (ignoresEncryption(entry, entryPath))
            return out;

        IEncryptionData encData = entry.createEncryptionData();
        return Crypto.creatOutputStream(out, true, encData, password);
    }

    private boolean ignoresEncryption(IFileEntry entry, String entryPath) {
        return ArchiveConstants.MANIFEST_XML.equals(entryPath)
                || ((FileEntryImpl) entry).isIgnoreEncryption();
    }

}
