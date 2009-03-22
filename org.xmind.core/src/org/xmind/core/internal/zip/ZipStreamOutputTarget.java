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
package org.xmind.core.internal.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xmind.core.Core;
import org.xmind.core.io.IOutputTarget;

/**
 * @author frankshaka
 * 
 */
public class ZipStreamOutputTarget implements IOutputTarget {

    private static class NonCloseableOutputStream extends FilterOutputStream {

        /**
         * @param out
         */
        public NonCloseableOutputStream(OutputStream out) {
            super(out);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.FilterOutputStream#close()
         */
        @Override
        public void close() throws IOException {
            try {
                flush();
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
            return new NonCloseableOutputStream(zip);
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
