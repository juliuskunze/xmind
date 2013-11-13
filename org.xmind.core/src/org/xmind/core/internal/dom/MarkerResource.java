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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.marker.AbstractMarkerResource;
import org.xmind.core.marker.IMarker;

public class MarkerResource extends AbstractMarkerResource {

//    private class InnerInputStream extends InputStream {
//
//        private InputStream is;
//
//        private void ensureOpen() {
//            if (is == null) {
//                is = source.getEntryStream(getFullPath());
//            }
//        }
//
//        public int available() throws IOException {
//            ensureOpen();
//            return is.available();
//        }
//
//        public void close() throws IOException {
//            if (is != null) {
//                is.close();
//                is = null;
//            }
//        }
//
//        public void mark(int readlimit) {
//            ensureOpen();
//            is.mark(readlimit);
//        }
//
//        public boolean markSupported() {
//            ensureOpen();
//            return is.markSupported();
//        }
//
//        public int read(byte[] b, int off, int len) throws IOException {
//            ensureOpen();
//            return is.read(b, off, len);
//        }
//
//        public int read(byte[] b) throws IOException {
//            ensureOpen();
//            return is.read(b);
//        }
//
//        public void reset() throws IOException {
//            ensureOpen();
//            is.reset();
//        }
//
//        public long skip(long n) throws IOException {
//            ensureOpen();
//            return is.skip(n);
//        }
//
//        public int read() throws IOException {
//            return is.read();
//        }
//
//    }
//
//    private class InnerOutputStream extends OutputStream {
//
//        private OutputStream os;
//
//        private void ensureOpen() {
//            if (os == null) {
//                os = target.getEntryStream(getFullPath());
//            }
//        }
//
//        public void close() throws IOException {
//            if (os != null) {
//                os.close();
//                os = null;
//            }
//        }
//
//        public void flush() throws IOException {
//            ensureOpen();
//            os.flush();
//        }
//
//        public void write(byte[] b, int off, int len) throws IOException {
//            ensureOpen();
//            os.write(b, off, len);
//        }
//
//        public void write(byte[] b) throws IOException {
//            ensureOpen();
//            os.write(b);
//        }
//
//        public void write(int b) throws IOException {
//            ensureOpen();
//            os.write(b);
//        }
//
//    }

    private IInputSource source;

    private IOutputTarget target;

    public MarkerResource(IMarker marker, IInputSource source,
            IOutputTarget target) {
        super(marker);
        this.source = source;
        this.target = target;
    }

    public InputStream getInputStream() {
        return source == null ? null : source.getEntryStream(getFullPath());
//        if (source != null && source.hasEntry(getFullPath())) {
//            return new InnerInputStream();
//        }
//        return null;
    }

    public OutputStream getOutputStream() {
        return target == null ? null : target.getEntryStream(getFullPath());
//        if (target != null && target.isEntryAvaialble(getFullPath())) {
//            return new InnerOutputStream();
//        }
//        return null;
    }

    public InputStream openInputStream() throws IOException {
        if (source == null)
            throw new FileNotFoundException(getFullPath());
        return source.openEntryStream(getFullPath());
    }

    public OutputStream openOutputStream() throws IOException {
        if (target == null)
            throw new FileNotFoundException(getFullPath());
        return target.openEntryStream(getFullPath());
    }

}