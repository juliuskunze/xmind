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
package org.xmind.core.internal.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xmind.core.Core;
import org.xmind.core.io.ICloseableOutputTarget;

/**
 * @author frankshaka
 * 
 */
public class ZipStreamOutputTarget implements ICloseableOutputTarget {

    private static class ZipEntryOutputStream extends FilterOutputStream {

        /**
         * @param out
         */
        public ZipEntryOutputStream(ZipOutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.FilterOutputStream#close()
         */
        @Override
        public void close() throws IOException {
            try {
                ((ZipOutputStream) out).closeEntry();
            } catch (IOException ignored) {
            }
        }

    }

    private ZipOutputStream zip;

    private ZipEntry currentEntry;

    /**
     * 
     */
    public ZipStreamOutputTarget(ZipOutputStream zip) {
        this.zip = zip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IOutputTarget#close()
     */
    public void close() {
        try {
            zip.flush();
            zip.close();
            return;
        } catch (IOException e) {
            Core.getLogger().log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IOutputTarget#getEntryStream(java.lang.String)
     */
    public OutputStream getEntryStream(String entryName) {
        try {
            this.currentEntry = new ZipEntry(entryName);
            zip.putNextEntry(currentEntry);
            return new ZipEntryOutputStream(zip);
        } catch (IOException e) {
            Core.getLogger().log(e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IOutputTarget#isEntryAvaialble(java.lang.String)
     */
    public boolean isEntryAvaialble(String entryName) {
        return zip != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IOutputTarget#setEntryTime(java.lang.String, long)
     */
    public void setEntryTime(String entryName, long time) {
        if (entryName != null) {
            if (currentEntry != null
                    && currentEntry.getName().equals(entryName)) {
                currentEntry.setTime(time);
            }
        }
    }

}
