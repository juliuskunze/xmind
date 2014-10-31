package org.xmind.core.internal.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class BlockCipherInputStream extends FilterInputStream {

    private final BufferedBlockCipher cipher;

    private byte[] outBuffer;

    private int outOffset;

    private final boolean isStream;

    private boolean eof;

    private int lastRead = -1;

    public BlockCipherInputStream(InputStream in, BufferedBlockCipher cipher) {
        super(in);
        this.cipher = cipher;
        isStream = cipher.getBlockSize() == 1;
        eof = false;
    }

    public int available() throws IOException {
        if (isStream)
            return super.available();
        if (outBuffer == null || outOffset >= outBuffer.length)
            nextBlock();
        return outBuffer.length - outOffset;
    }

    public synchronized void close() throws IOException {
        super.close();
    }

    public synchronized int read() throws IOException {
        if (isStream) {
            byte[] buf = new byte[1];
            int in = super.read();
            if (in == -1)
                return -1;
            buf[0] = (byte) in;
            try {
                cipher.processBytes(buf, 0, 1, buf, 0);
            } catch (DataLengthException e) {
                throw new IOException(e.getMessage());
            }
            return buf[0] & 0xFF;
        }

        if (outBuffer == null || outOffset >= outBuffer.length) {
            if (eof)
                return -1;
            nextBlock();
        }
        if (outBuffer == null || outOffset >= outBuffer.length)
            return -1;
        return outBuffer[outOffset++] & 0xFF;
    }

    public synchronized int read(byte[] buf, int off, int len)
            throws IOException {
        if (buf == null)
            return (int) skip(len);

        if (isStream) {
            len = super.read(buf, off, len);
            if (len > 0) {
                try {
                    cipher.processBytes(buf, off, len, buf, off);
                } catch (DataLengthException shouldNotHappen) {
                    IOException ioe = new IOException(
                            "Short buffer for stream cipher -- this should not happen"); //$NON-NLS-1$
                    ioe.initCause(shouldNotHappen);
                    throw ioe;
                }
            }
            return len;
        }

        int count = 0;
        while (count < len) {
            if (outBuffer == null || outOffset >= outBuffer.length) {
                if (eof) {
                    if (count == 0)
                        count = -1;
                    break;
                }
                nextBlock();
            }
            if (outBuffer == null || outOffset >= outBuffer.length) {
                if (count == 0)
                    count = -1;
                break;
            }
            int l = Math.min(outBuffer.length - outOffset, len - count);
            System.arraycopy(outBuffer, outOffset, buf, count + off, l);
            count += l;
            outOffset += l;
        }
        return count;
    }

    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    public long skip(long bytes) throws IOException {
        if (isStream) {
            return super.skip(bytes);
        }
        long ret = 0;
        if (bytes > 0 && outBuffer != null && outOffset >= outBuffer.length) {
            ret = outBuffer.length - outOffset;
            outOffset = outBuffer.length;
        }
        return ret;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int mark) {
    }

    public void reset() throws IOException {
        throw new IOException("reset not supported"); //$NON-NLS-1$
    }

    private void nextBlock() throws IOException {
        byte[] buf = new byte[cipher.getBlockSize()];
        byte[] out = new byte[cipher.getOutputSize(buf.length)];

        try {
            outBuffer = null;
            outOffset = 0;
            while (outBuffer == null) {
                int len = in.read(buf);
                if (len == -1) {
                    if (lastRead > 0) {
                        int num = cipher.doFinal(out, 0);
                        if (num > 0) {
                            outBuffer = new byte[num];
                            System.arraycopy(out, 0, outBuffer, outOffset, num);
                        }
                    }
                    eof = true;
                    return;
                }

                lastRead = len;

                outOffset = 0;
                int num = cipher.processBytes(buf, 0, len, out, 0);
                if (num > 0) {
                    outBuffer = new byte[num];
                    System.arraycopy(out, 0, outBuffer, outOffset, num);
                }
            }
        } catch (DataLengthException bpe) {
            IOException ioe = new IOException("illegal block size"); //$NON-NLS-1$
            ioe.initCause(bpe);
            throw ioe;
        } catch (InvalidCipherTextException ibse) {
            IOException ioe = new IOException("bad padding"); //$NON-NLS-1$
            ioe.initCause(ibse);
            throw ioe;
        } finally {
        }
    }

}
