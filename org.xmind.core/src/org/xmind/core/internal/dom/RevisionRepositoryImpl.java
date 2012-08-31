package org.xmind.core.internal.dom;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IRevisionManager;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.RevisionRepository;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.util.DOMUtils;

public class RevisionRepositoryImpl extends RevisionRepository {

    private WorkbookImpl ownedWorkbook;

    private Map<String, RevisionManagerImpl> managers = new HashMap<String, RevisionManagerImpl>();

    public RevisionRepositoryImpl(WorkbookImpl ownedWorkbook) {
        this.ownedWorkbook = ownedWorkbook;
    }

    public IRevisionManager getRevisionManager(String resourceId,
            String contentType) {
        RevisionManagerImpl manager = managers.get(resourceId);
        if (manager == null) {
            manager = loadRevisionManager(resourceId, contentType);
            manager.addNotify(ownedWorkbook);
            managers.put(resourceId, manager);
        }
        return manager;
    }

    private RevisionManagerImpl loadRevisionManager(String resourceId,
            String contentType) {
        String dirPath = ArchiveConstants.PATH_REVISIONS + resourceId + "/"; //$NON-NLS-1$
        String metaPath = dirPath + ArchiveConstants.REVISIONS_XML;
        IManifest manifest = ownedWorkbook.getManifest();
        IFileEntry metaEntry = manifest.getFileEntry(metaPath);
        if (metaEntry != null) {
            RevisionManagerImpl manager = loadRevisionManager(resourceId,
                    metaEntry, dirPath);
            if (manager != null)
                return manager;
        }
        Document doc = DOMUtils.createDocument();
        Element ele = DOMUtils.createElement(doc, DOMConstants.TAG_REVISIONS);
        ele.setAttribute(DOMConstants.ATTR_RESOURCE_ID, resourceId);
        ele.setAttribute(DOMConstants.ATTR_MEDIA_TYPE, contentType);
        ele.setAttribute(DOMConstants.ATTR_NEXT_REVISION_NUMBER, "1"); //$NON-NLS-1$
        manifest.createFileEntry(metaPath);
        return new RevisionManagerImpl(doc, ownedWorkbook, dirPath);
    }

    private RevisionManagerImpl loadRevisionManager(String resourceId,
            IFileEntry metaEntry, String path) {
        InputStream stream = metaEntry.getInputStream();
        if (stream == null)
            return null;
        try {
            Document doc = DOMUtils.loadDocument(stream);
            return new RevisionManagerImpl(doc, ownedWorkbook, path);
        } catch (IOException e) {
            Core.getLogger().log(e,
                    "Failed to load document at " + metaEntry.getPath()); //$NON-NLS-1$
        }
        return null;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        return false;
    }

}
