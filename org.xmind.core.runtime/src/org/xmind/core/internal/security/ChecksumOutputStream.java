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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.xmind.core.CoreException;
import org.xmind.core.IChecksumStream;

/**
 * @author MANGOSOFT
 * 
 */
public class ChecksumOutputStream extends FilterOutputStream implements
        IChecksumStream {

//    private MessageDigest digest;
    private Digest digest;

    //private byte[] by;

    /**
     * @throws CoreException
     * 
     */
    public ChecksumOutputStream(OutputStream output) throws CoreException {
        super(output);
//        this.digest = createDigest();
        this.digest = new MD5Digest();
    }

    /**
     * @return
     * @throws CoreException
     */
//    private MessageDigest createDigest() throws CoreException {
//        return BouncyCastleProviderStream.createChecksumDigest();
//    }
    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        digest.update((byte) b);
//        System.out.print(new String(new byte[] { (byte) b }));
        super.write(b);
    }

    public String getChecksum() {
        byte[] by = new byte[digest.getDigestSize()];
        digest.doFinal(by, 0);
        return Base64.byteArrayToBase64(by);
//        return Base65.byteArrayToBase64(digest.digest());
    }
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.io.OutputStream#write(byte[])
//     */
//    @Override
//    public void write(byte[] b) throws IOException {
//        digest.update(b);
//        System.out.print(new String(b));
//        super.write(b);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.io.OutputStream#write(byte[], int, int)
//     */
//    @Override
//    public void write(byte[] b, int off, int len) throws IOException {
//        digest.update(b, off, len);
//        System.out.print(new String(b, off, len));
//        super.write(b, off, len);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.io.OutputStream#close()
//     */
//    @Override
//    public void close() throws IOException {
//        super.close();
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.io.OutputStream#flush()
//     */
//    @Override
//    public void flush() throws IOException {
//        super.flush();
//    }

}
