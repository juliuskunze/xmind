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
package org.xmind.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xmind.core.CoreException;

/**
 * @author frankshaka
 * 
 */
public class ByteArrayStorage implements IStorage {

    protected class ByteArrayInputSource implements IInputSource {

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntries()
         */
        public Iterator<String> getEntries() {
            return dataTable == null ? NO_ENTRIES : dataTable.keySet()
                    .iterator();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntryStream(java.lang.String)
         */
        public InputStream getEntryStream(String entryName) {
            if (dataTable != null && entryName != null) {
                byte[] bs = dataTable.get(entryName);
                if (bs != null) {
                    return new ByteArrayInputStream(bs);
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntrySize(java.lang.String)
         */
        public long getEntrySize(String entryName) {
            if (dataTable != null && entryName != null) {
                byte[] bs = dataTable.get(entryName);
                if (bs != null) {
                    return bs.length;
                }
            }
            return -1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntryTime(java.lang.String)
         */
        public long getEntryTime(String entryName) {
            if (timeTable != null && entryName != null) {
                Long time = timeTable.get(entryName);
                if (time != null)
                    return time.longValue();
            }
            return 0;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getName()
         */
        public String getName() {
            return ByteArrayStorage.this.getName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#hasEntry(java.lang.String)
         */
        public boolean hasEntry(String entryName) {
            return dataTable != null && dataTable.containsKey(entryName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#open()
         */
        public boolean open() {
            return dataTable != null && !dataTable.isEmpty();
        }

    }

    protected class ByteArrayOutputTarget implements IOutputTarget {

        private class ByteArrayOutputStream2 extends ByteArrayOutputStream {

            private String entryName;

            /**
             * 
             */
            public ByteArrayOutputStream2(String entryName) {
                this.entryName = entryName;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.OutputStream#flush()
             */
            @Override
            public void flush() throws IOException {
                super.flush();
                pushBytes();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.ByteArrayOutputStream#close()
             */
            @Override
            public void close() throws IOException {
                super.close();
                pushBytes();
                setEntryTime(entryName, System.currentTimeMillis());
            }

            /**
             * 
             */
            private void pushBytes() {
                if (dataTable == null) {
                    dataTable = new HashMap<String, byte[]>();
                }
                dataTable.put(entryName, toByteArray());
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IOutputTarget#getEntryStream(java.lang.String)
         */
        public OutputStream getEntryStream(String entryName) {
            if (entryName != null)
                return new ByteArrayOutputStream2(entryName);
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.core.io.IOutputTarget#isEntryAvaialble(java.lang.String)
         */
        public boolean isEntryAvaialble(String entryName) {
            return entryName != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IOutputTarget#open()
         */
        public boolean open() {
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IOutputTarget#setEntryTime(long)
         */
        public void setEntryTime(String entryName, long time) {
            if (timeTable == null) {
                timeTable = new HashMap<String, Long>();
            }
            timeTable.put(entryName, time);
        }

    }

    private Map<String, byte[]> dataTable;

    private Map<String, Long> timeTable;

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getFullPath()
     */
    public String getFullPath() {
        return getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getInputSource()
     */
    public IInputSource getInputSource() throws CoreException {
        return new ByteArrayInputSource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getName()
     */
    public String getName() {
        return toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getOutputTarget()
     */
    public IOutputTarget getOutputTarget() throws CoreException {
        return new ByteArrayOutputTarget();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IStorage#clear()
     */
    public void clear() {
        dataTable = null;
        timeTable = null;
    }
}
