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
package org.xmind.core.internal.security;


/**
 * @author MANGOSOFT
 * @deprecated
 */
public class DecryptedInputSource {
//implements IInputSource {
//
//    private Map<String, EncryptionData> map = new HashMap<String, EncryptionData>();
//
//    private String password = null;
//
//    private IEncryptionHandler encryptionHandler;
//
//    private IInputSource source;
//
//    /**
//     * @param source
//     * @param encryptionHandler
//     */
//    public DecryptedInputSource(IInputSource source,
//            IEncryptionHandler encryptionHandler, Document mfDoc) {
//        super();
//        this.source = source;
//        this.encryptionHandler = encryptionHandler;
//        init(mfDoc);
//    }
//
//    private void init(Document mfDoc) {
//        //TODO check encryption data
//        map.clear();
//        Element docElement = mfDoc.getDocumentElement();
//        Element[] elements = DOMUtils.getChildElements(docElement);
//
//        for (Element fileEntryEle : elements) {
//            if (DOMConstants.TAG_FILE_ENTRY.equals(fileEntryEle.getTagName())) {
//
//                Element encryptionDataEle = DOMUtils.getFirstChildElementByTag(
//                        fileEntryEle, DOMConstants.TAG_ENCRYPTION_DATA);
//                if (encryptionDataEle == null)
//                    continue;
//                String fullPath = DOMUtils.getAttribute(fileEntryEle,
//                        DOMConstants.ATTR_FULL_PATH);
//                if (fullPath != null) {
//                    EncryptionData encData = loadEncryptionData(encryptionDataEle);
//                    if (encData != null) {
//                        map.put(fullPath, encData);
//                    }
//                }
//                fileEntryEle.removeChild(encryptionDataEle);
//            }
//        }
//    }
//
////    /**
////     * @param encryptionDataEle
////     * @return
////     */
////    private EncryptionData loadEncryptionData(Element encryptionDataEle) {
////        return new EncryptionData(encryptionDataEle);
////    }
//
//    /**
//     * 
//     * @see org.xmind.core.io.IInputSource#close()
//     */
//    public void close() {
//        source.close();
//    }
//
//    /**
//     * @return
//     * @see org.xmind.core.io.IInputSource#getEntries()
//     */
//    public Iterator<String> getEntries() {
//        return source.getEntries();
//    }
//
//    /**
//     * @param entryName
//     * @return
//     * @throws IOException
//     * @see org.xmind.core.io.IInputSource#getEntryStream(java.lang.String)
//     */
//    public InputStream getEntryStream(String entryName) {
//        InputStream stream = source.getEntryStream(entryName);
//        if (stream != null) {
//            EncryptionData encData = map.get(entryName);
//            if (encData != null) {
//                try {
//                    stream = createCipherStream(stream, encData);
//
//                } catch (CoreException e) {
//                    Core.getLogger().log(e);
//                }
//            }
//        }
//        return stream;
//    }
//
//    private InputStream createCipherStream(InputStream stream,
//            EncryptionData encData) throws CoreException {
//        String password = retrievePassword();
//        String checksum = encData.getChecksum();
//
//        return Crypto.createInputStream(stream, false, encData, password);
//
////        BufferedBlockCipher cipher = Crypto.createBlockCipher(false, password,
////                encData);
////        if (checksum != null)
////            return new ChecksumInputStream(new BlockCipherInputStream(stream,
////                    cipher));
////        return new BlockCipherInputStream(stream, cipher);
//    }
//
//    private String retrievePassword() {
//        if (password == null) {
//            if (encryptionHandler != null) {
//                password = encryptionHandler.retrievePassword();
//            }
//        }
//        return password;
//    }
//
//    /**
//     * @return the password
//     */
//    public String getPassword() {
//        return password;
//    }
//
//    /**
//     * @return the map
//     */
//    public Map<String, EncryptionData> getEncryptionDataMap() {
//        return map;
//    }
//
////    /**
////     * @return
////     * @see org.xmind.core.io.IInputSource#getName()
////     * @deprecated
////     */
////    public String getName() {
////        return source.getName();
////    }
//
//    /**
//     * @param entryName
//     * @return
//     * @see org.xmind.core.io.IInputSource#hasEntry(java.lang.String)
//     */
//    public boolean hasEntry(String entryName) {
//        return source.hasEntry(entryName);
//    }
//
//    /**
//     * @return
//     * @see org.xmind.core.io.IInputSource#open()
//     */
//    public boolean open() {
//        return source.open();
//    }
//
//    /**
//     * @param stream
//     * @return
//     * @see org.xmind.core.io.IInputSource#closeEntryStream(String,
//     *      java.io.InputStream)
//     */
//    public boolean closeEntryStream(String entryPath, InputStream stream) {
//        return source.closeEntryStream(entryPath, stream);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.xmind.core.io.IInputSource#getEntrySize(java.lang.String)
//     */
//    public long getEntrySize(String entryName) {
//        return source.getEntrySize(entryName);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.xmind.core.io.IInputSource#getEntryTime(java.lang.String)
//     */
//    public long getEntryTime(String entryName) {
//        return source.getEntryTime(entryName);
//    }
//
}
