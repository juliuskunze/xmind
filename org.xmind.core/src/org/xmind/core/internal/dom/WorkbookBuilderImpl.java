/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.AbstractWorkbookBuilder;
import org.xmind.core.internal.zip.ZipFileInputSource;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class WorkbookBuilderImpl extends AbstractWorkbookBuilder implements
        ErrorHandler {

//    private class WorkbookXMLLoader extends XMLLoader {
//
//        protected Document doLoadXMLFile(IInputSource source, String entryName)
//                throws CoreException, IOException {
//            InputStream stream = source.getEntryStream(entryName);
//
//            if (stream == null) {
//                throw new CoreException(Core.ERROR_NO_SUCH_ENTRY);
//            }
//            try {
//                DocumentBuilder loader = getDocumentLoader();
//                Document doc = loader.parse(stream);
//
//                if (stream instanceof IChecksumStream) {
//                    String checksum = ((IChecksumStream) stream).getChecksum();
//                    if (checksum != null) {
//                        Map<String, EncryptionData> map = ((DecryptedInputSource) source)
//                                .getEncryptionDataMap();
//                        EncryptionData encData = map.get(entryName);
//                        String check = encData.getChecksum();
//                        if (!check.equals(checksum))
//                            throw new CoreException(Core.ERROR_WRONG_PASSWORD);
//                    }
//                }
//
//                return doc;
//            } catch (Throwable e) {
//                CoreException coreException = new CoreException(
//                        Core.ERROR_FAIL_PARSING_XML, e);
//
//                if (stream instanceof IChecksumStream) {
////                        || stream instanceof BlockCipherInputStream) {
//                    String checksum = ((IChecksumStream) stream).getChecksum();
//                    if (checksum != null) {
//                        Map<String, EncryptionData> map = ((DecryptedInputSource) source)
//                                .getEncryptionDataMap();
//                        EncryptionData encData = map.get(entryName);
//                        String check = encData.getChecksum();
//                        if (!check.equals(checksum))
//                            coreException = new CoreException(
//                                    Core.ERROR_WRONG_PASSWORD, e);
//                    }
//                }
//                throw coreException;
//            } finally {
//                source.releaseStream(stream);
//            }
//        }
//
//        public Document createDocument() {
//            return WorkbookBuilderImpl.this.createDocument();
//        }
//
//    }

    private DocumentBuilder documentCreator = null;

    private DocumentBuilder documentLoader = null;

    private IEncryptionHandler defaultEncryptionHandler = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#setDefaultEncryptionHandler(org.xmind
     * .core.IEncryptionHandler)
     */
    public void setDefaultEncryptionHandler(IEncryptionHandler encryptionHandler) {
        if (this.defaultEncryptionHandler != null)
            return;

        this.defaultEncryptionHandler = encryptionHandler;
    }

    private DocumentBuilder getDocumentCreator() {
        if (documentCreator == null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setNamespaceAware(true);
                documentCreator = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException(e);
            }
        }
        return documentCreator;
    }

    public DocumentBuilder getDocumentLoader() throws CoreException {
        if (documentLoader == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory
                    .setAttribute(
                            "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
                            Boolean.TRUE);
            factory.setNamespaceAware(true);
            try {
                documentLoader = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_PARSER, e);
            }
            documentLoader.setErrorHandler(this);
        }
        return documentLoader;
    }

    public IWorkbook createWorkbook() {
        return newWorkbook(null);
    }

    public IWorkbook createWorkbook(String targetPath) {
        return newWorkbook(targetPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#createWorkbook(org.xmind.core.io.IStorage
     * )
     */
    public IWorkbook createWorkbook(IStorage storage) {
        WorkbookImpl wb = newWorkbook(null);
        wb.setTempStorage(storage);
        return wb;
    }

    private WorkbookImpl newWorkbook(String file) {
        Document impl = createDocument();
        WorkbookImpl workbook = new WorkbookImpl(impl, file);
        return workbook;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#createWorkbookOnTemp(java.lang.String)
     */
    public IWorkbook createWorkbookOnTemp(String tempLocation) {
        if (tempLocation == null)
            throw new IllegalArgumentException("Temp location is null"); //$NON-NLS-1$

        File dir = new File(tempLocation);
        if (!dir.exists())
            throw new IllegalArgumentException("Temp location not exists: " //$NON-NLS-1$
                    + tempLocation);

        return createWorkbook(new DirectoryStorage(dir));
    }

    public Document createDocument() {
        return getDocumentCreator().newDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.AbstractWorkbookBuilder#doLoadFromFile(java.io
     * .File, org.xmind.core.io.IStorage, org.xmind.core.IEncryptionHandler)
     */
    @Override
    protected IWorkbook doLoadFromFile(File file, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException, FileNotFoundException {
        ZipFile zipFile = new ZipFile(file);
        try {
            return loadFromInputSource(new ZipFileInputSource(zipFile),
                    storage, encryptionHandler);
        } finally {
            zipFile.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.AbstractWorkbookBuilder#doLoadFromSteam(java.
     * io.InputStream, org.xmind.core.io.IStorage,
     * org.xmind.core.IEncryptionHandler)
     */
    protected IWorkbook doLoadFromSteam(InputStream in, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        File tempDir = createTempDir();
        IStorage tempStorage = new DirectoryStorage(tempDir);
        try {
            IOutputTarget tempTarget = tempStorage.getOutputTarget();
            ZipInputStream zin = new ZipInputStream(in);
            try {
                copyAll(zin, tempTarget);
            } finally {
                zin.close();
            }

            return loadFromInputSource(tempStorage.getInputSource(), storage,
                    encryptionHandler);
        } finally {
            FileUtils.delete(tempDir);
        }
    }

    private File createTempDir() {
        File tempDir = new File(Core.getWorkspace().getTempDir(
                Core.getIdFactory().createId()));
        return tempDir;
    }

    private void copyAll(ZipInputStream zin, IOutputTarget target)
            throws IOException {
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            String entryPath = entry.getName();
            if (!entry.isDirectory()) {
                if (target.isEntryAvaialble(entryPath)) {
                    OutputStream out = target.getEntryStream(entryPath);
                    if (out != null) {
                        try {
                            FileUtils.transfer(zin, out, false);
                        } finally {
                            try {
                                out.close();
                            } catch (IOException ignore) {
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.AbstractWorkbookBuilder#doLoadFromInputSource
     * (org.xmind.core.io.IInputSource, org.xmind.core.io.IStorage,
     * org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromInputSource(IInputSource source, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        if (encryptionHandler == null) {
            encryptionHandler = this.defaultEncryptionHandler;
        }
        return new WorkbookLoader(this, source, storage, encryptionHandler)
                .load();
    }

//    public IWorkbook loadFromFile(File file,
//            IEncryptionHandler encryptionHandler) throws IOException,
//            CoreException {
//
//        if (file == null)
//            throw new IllegalArgumentException();
//        if (!file.exists())
//            throw new FileNotFoundException();
//
//        if (file.isDirectory()) {
//            // load from temp directory
//            return loadWorkbook(new DirectoryInputSource(file), null,
//                    encryptionHandler);
//        }
//
//        // load from zip file
//        return loadWorkbook(new ZipFileInputSource(file), null,
//                encryptionHandler);
//    }
//
//    public IWorkbook loadFromTempLocation(String tempLocation)
//            throws IOException, CoreException {
//        if (tempLocation == null)
//            throw new IllegalArgumentException("Temp location is null"); //$NON-NLS-1$
//
//        File file = new File(tempLocation);
//        if (file.isDirectory())
//            throw new IllegalArgumentException(
//                    "Temp location is not a directory"); //$NON-NLS-1$
//
//        return loadFromStorage(new DirectoryStorage(file));
//
////        IWorkbook workbook = loadFromInputSource(new DirectoryInputSource(
////                tempLocation), null, null);
////
////        workbook.setTempLocation(tempLocation);
////        return workbook;
//    }
//
//    public IWorkbook loadFromStream(InputStream in, String tempLocation,
//            IEncryptionHandler encryptionHandler) throws IOException,
//            CoreException {
//        if (in == null)
//            throw new IllegalArgumentException("Input stream is null"); //$NON-NLS-1$
//
//        if (tempLocation != null) {
//            File file = new File(tempLocation);
//            if (!file.isDirectory()) {
//                return loadFromStream(in, new DirectoryStorage(file),
//                        encryptionHandler);
//            }
//        }
//        return loadFromStream(in, (IStorage) null, encryptionHandler);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.xmind.core.IWorkbookBuilder#loadFromStream(java.io.InputStream,
//     * org.xmind.core.io.IArchive, org.xmind.core.IEncryptionHandler)
//     */
//    public IWorkbook loadFromStream(InputStream in, IStorage storage,
//            IEncryptionHandler encryptionHandler) throws IOException,
//            CoreException {
//        if (in == null)
//            throw new IllegalArgumentException("Input stream is null"); //$NON-NLS-1$
//
//        if (storage == null) {
//            storage = new ByteArrayStorage();
//        }
//
//        File tempDir = createTempDir();
//        IStorage tempStorage = new DirectoryStorage(tempDir);
//        try {
//            IOutputTarget tempTarget = tempStorage.getOutputTarget();
//            if (tempTarget.open()) {
//                ZipInputStream zin = new ZipInputStream(in);
//                try {
//                    copyAll(zin, tempTarget);
//                } finally {
//                    zin.close();
//                    tempTarget.close();
//                }
//            }
//
//            return loadWorkbook(tempStorage.getInputSource(), storage,
//                    encryptionHandler);
//        } finally {
//            FileUtils.delete(tempDir);
//        }
//
////        IWorkbook workbook = loadFromInputSource(new DirectoryInputSource(
////                tempLocation), null, encryptionHandler);
////
////        workbook.setTempLocation(tempLocation);
////        return workbook;
//    }
//
//
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.xmind.core.IWorkbookBuilder#loadFromStorage(org.xmind.core.io.IStorage
//     * )
//     */
//    public IWorkbook loadFromStorage(IStorage storage) throws IOException,
//            CoreException {
//        return loadFromStorage(storage, null);
//    }
//
//    public IWorkbook loadFromStorage(IStorage storage,
//            IEncryptionHandler encryptionHandler) throws IOException,
//            CoreException {
//        return loadWorkbook(null, storage, encryptionHandler);
//    }
//
//    private IWorkbook loadWorkbook(IInputSource source, IStorage storage,
//            IEncryptionHandler encryptionHandler) throws IOException,
//            CoreException {
//        return new WorkbookLoader(this, source, storage, encryptionHandler)
//                .load();
//    }
//
//    /**
//     * @param source
//     * @param encryptionHandler
//     * @return
//     */
//    private IWorkbook doLoad(IInputSource source, IStorage storage,
//            IEncryptionHandler encryptionHandler) throws IOException,
//            CoreException {
//        WorkbookXMLLoader xmlLoader = new WorkbookXMLLoader();
//
//        // load manifest
//        Document mfImpl = xmlLoader.loadXMLFile(source, MANIFEST_XML);
//
//        DecryptedInputSource decryptedSource = new DecryptedInputSource(source,
//                encryptionHandler == null ? this.encryptionHandler
//                        : encryptionHandler, mfImpl);
//        source = decryptedSource;
//
//        IWorkbook compatible = Compatibility.loadCompatibleWorkbook(source,
//                xmlLoader);
//        if (compatible != null) {
//            if (storage != null) {
//                compatible.setStorage(storage);
//            }
//            return compatible;
//        }
//        // load workbook contents
//
//        Document wbImpl = xmlLoader.loadXMLFile(source, CONTENT_XML);
//
//        Element wbEle = wbImpl.getDocumentElement();
//        if (wbEle == null)
//            throw new CoreException(Core.ERROR_NO_WORKBOOK_CONTENT, CONTENT_XML);
//
//        WorkbookImpl workbook = new WorkbookImpl(wbImpl);
//
//        ManifestImpl mf = new ManifestImpl(mfImpl);
//        mf.setWorkbook(workbook);
//        initManifest(mf);
//        workbook.setManifest(mf);
//
//        if (source.hasEntry(STYLES_XML)) {
//            Document styDoc = xmlLoader.loadXMLFile(source, STYLES_XML);
//            if (styDoc != null) {
//                StyleSheetImpl sheet = new StyleSheetImpl(styDoc);
//                initStyle(sheet);
//                workbook.setStyleSheet(sheet);
//                sheet.setManifest(workbook.getManifest());
//            }
//        }
//
//        InputStream msStream = source.getEntryStream(PATH_MARKER_SHEET);
//        if (msStream != null) {
//            try {
//                WorkbookMarkerResourceProvider resourceProvider = new WorkbookMarkerResourceProvider(
//                        workbook);
//                IMarkerSheet ms = Core.getMarkerSheetBuilder().loadFromStream(
//                        msStream, resourceProvider);
//                if (ms != null) {
//                    workbook.setMarkerSheet((MarkerSheetImpl) ms);
//                }
//            } catch (IOException e) {
//            } catch (CoreException e) {
//            } finally {
//                source.releaseStream(msStream);
//            }
//        }
//        try {
//            Document metaDocument = xmlLoader.loadXMLFile(source, META_XML);
//            if (metaDocument != null) {
//                MetaImpl meta = new MetaImpl(metaDocument);
//                workbook.setMeta(meta);
//            }
//        } catch (IOException e) {
//        } catch (CoreException e) {
//        }
//
//        // initialize workbook contents
//        initWorkbookContents(workbook);
//        workbook.setPassword(decryptedSource.getPassword());
//        IArchivedWorkbook archived = workbook.getArchivedWorkbook();
//        if (archived instanceof ArchivedWorkbook) {
//            ((ArchivedWorkbook) archived).setEncryptionDataMap(decryptedSource
//                    .getEncryptionDataMap());
//        }
//        return workbook;
//    }
//
//    /**
//     * @param sheet
//     */
//    private void initStyle(StyleSheetImpl sheet) {
//        for (IStyle style : sheet.getAllStyles()) {
//            init(style);
//        }
//    }
//
//    /**
//     * @param style
//     */
//    private void init(IStyle style) {
//    }
//
//    private void initManifest(ManifestImpl mf) {
//        mf.getFileEntries();
//    }
//
//    private void initWorkbookContents(IWorkbook workbook) {
//        for (ISheet s : workbook.getSheets()) {
//            initSheet(s, workbook);
//        }
//    }
//
//    private void initSheet(ISheet s, IWorkbook wb) {
//        ((SheetImpl) s).addNotify((WorkbookImpl) wb);
//    }

    public void error(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception, "Error while loading workbook"); //$NON-NLS-1$
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception, "Fatal error while loading workbook"); //$NON-NLS-1$
    }

    public void warning(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception, "Warning while loading workbook"); //$NON-NLS-1$
    }

}