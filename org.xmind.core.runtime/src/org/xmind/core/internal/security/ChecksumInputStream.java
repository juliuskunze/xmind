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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.xmind.core.CoreException;
import org.xmind.core.IChecksumStream;

/**
 * @author MANGOSOFT
 * 
 */
public class ChecksumInputStream extends FilterInputStream implements
        IChecksumStream {

//    private MessageDigest digest;
    private Digest digest;

    public ChecksumInputStream(InputStream in) throws CoreException {
        super(in);
//        this.digest = createDigest();
        this.digest = new MD5Digest();
    }

    /**
     * @return
     * @throws CoreException
     * 
     */
//    private MD5Digest createDigest() throws CoreException {
////        return CryptedStream.createChecksumDigest();
//        return new MD5Digest();
//    }
    @Override
    public int read() throws IOException {
        int b = super.read();
        digest.update((byte) b);
//        by = new byte[digest.getDigestSize()];
        return b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int num = super.read(b, off, len);
        digest.update(b, off, num);
        return num;
    }

    /*
     * @see org.xmind.core.IChecksumStream#getChecksum()
     */
    public String getChecksum() {
        byte[] by = new byte[digest.getDigestSize()];
        digest.doFinal(by, 0);
        return Base64.byteArrayToBase64(by);
//        return Base65.byteArrayToBase64(digest.digest());
    }
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.io.FilterInputStream#read(byte[])
//     */
//    @Override
//    public int read(byte[] b) throws IOException {
//        int num = super.read(b);
//        digest.update(b, 0, num);
//        return num;
//    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.lang.Object#finalize()
//     */
//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.io.FilterInputStream#close()
//     */
//    @Override
//    public void close() throws IOException {
//        super.close();
//    }
}
