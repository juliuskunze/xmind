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
package org.xmind.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookBuilder;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;

public abstract class AbstractWorkbookBuilder implements IWorkbookBuilder {

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromPath(java.lang.String)
     */
    public IWorkbook loadFromPath(String path) throws IOException,
            CoreException {
        return loadFromPath(path, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromPath(java.lang.String,
     * org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromPath(String path,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        return loadFromPath(path, null, encryptionHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromPath(java.lang.String,
     * org.xmind.core.io.IStorage, org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromPath(String path, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        if (path == null)
            throw new IllegalArgumentException("Path is null"); //$NON-NLS-1$
        return doLoadFromPath(path, storage, encryptionHandler);
    }

    protected IWorkbook doLoadFromPath(String path, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        return loadFromFile(new File(path), storage, encryptionHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromFile(java.io.File)
     */
    public IWorkbook loadFromFile(File file) throws IOException, CoreException {
        return loadFromFile(file, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromFile(java.io.File,
     * org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromFile(File file,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        return loadFromFile(file, null, encryptionHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromFile(java.io.File,
     * org.xmind.core.io.IStorage, org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromFile(File file, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        if (file == null)
            throw new IllegalArgumentException("File is null"); //$NON-NLS-1$
        if (!file.exists())
            throw new FileNotFoundException("File not exists: " + file); //$NON-NLS-1$

        if (file.isDirectory()) {
            return doLoadFromDirectory(file, storage, encryptionHandler);
        }

        if (!file.canRead())
            throw new IOException("File can't be read: " + file); //$NON-NLS-1$

        return doLoadFromFile(file, storage, encryptionHandler);
    }

    /**
     * 
     * @param file
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    protected IWorkbook doLoadFromDirectory(File dir, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        return loadFromInputSource(new DirectoryInputSource(dir), storage,
                encryptionHandler);
    }

    /**
     * 
     * @param file
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     * @throws FileNotFoundException
     */
    protected IWorkbook doLoadFromFile(File file, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException, FileNotFoundException {
        return loadFromStream(new FileInputStream(file), storage,
                encryptionHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromStream(java.io.InputStream,
     * java.lang.String)
     */
    public IWorkbook loadFromStream(InputStream in, String tempLocation)
            throws IOException, CoreException {
        return loadFromStream(in, tempLocation, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromStream(java.io.InputStream,
     * java.lang.String, org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromStream(InputStream in, String tempLocation,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        if (tempLocation == null)
            throw new IllegalArgumentException("Temp location is null"); //$NON-NLS-1$
        File dir = new File(tempLocation);
        if (!dir.exists())
            throw new FileNotFoundException(
                    "Temp location not found: " + tempLocation); //$NON-NLS-1$
        if (!dir.isDirectory())
            throw new FileNotFoundException(
                    "Temp location is not directory: " + tempLocation); //$NON-NLS-1$
        return loadFromStream(in, new DirectoryStorage(dir), encryptionHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromStream(java.io.InputStream,
     * org.xmind.core.io.IOutputTarget)
     */
    public IWorkbook loadFromStream(InputStream in, IStorage storage)
            throws IOException, CoreException {
        return loadFromStream(in, storage, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbookBuilder#loadFromStream(java.io.InputStream,
     * org.xmind.core.io.IStorage, org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromStream(InputStream in, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        if (in == null)
            throw new IllegalArgumentException("Input stream is null"); //$NON-NLS-1$
        if (storage == null)
            throw new IllegalArgumentException("Storage is null"); //$NON-NLS-1$
        return doLoadFromSteam(in, storage, encryptionHandler);
    }

    /**
     * 
     * @param in
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    protected abstract IWorkbook doLoadFromSteam(InputStream in,
            IStorage storage, IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromTempLocation(java.lang.String)
     */
    public IWorkbook loadFromTempLocation(String tempLocation)
            throws IOException, CoreException {
        if (tempLocation == null)
            throw new IllegalArgumentException("Temp location is null"); //$NON-NLS-1$
        File dir = new File(tempLocation);
        if (!dir.exists())
            throw new FileNotFoundException(
                    "Temp location not found: " + tempLocation); //$NON-NLS-1$
        if (!dir.isDirectory())
            throw new FileNotFoundException(
                    "Temp location is not directory: " + tempLocation); //$NON-NLS-1$
        return doLoadFromDirectory(dir, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromStorage(org.xmind.core.io.IStorage
     * )
     */
    public IWorkbook loadFromStorage(IStorage storage) throws IOException,
            CoreException {
        return loadFromInputSource(null, storage, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromInputSource(org.xmind.core.io
     * .IInputSource)
     */
    public IWorkbook loadFromInputSource(IInputSource source)
            throws IOException, CoreException {
        return loadFromInputSource(source, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.IWorkbookBuilder#loadFromInputSource(org.xmind.core.io
     * .IInputSource, org.xmind.core.IEncryptionHandler)
     */
    public IWorkbook loadFromInputSource(IInputSource source,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException {
        return loadFromInputSource(source, null, encryptionHandler);
    }

    /**
     * 
     * @param source
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    public abstract IWorkbook loadFromInputSource(IInputSource source,
            IStorage storage, IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

//    public IWorkbook loadFromUri(String uri) throws IOException, CoreException {
//        return loadFromUrl(new URL(uri));
//    }
//
//    public IWorkbook loadFromUrl(URL url) throws IOException, CoreException {
//        // TODO load workbook from url
//        InputStream in = url.openStream();
//        if (in != null) {
//            return loadFromStream(in);
//        }
//        return createWorkbook();
//    }
//
//    protected IWorkbook loadFromStream(InputStream input) throws IOException,
//            CoreException {
//        return loadFromStream(input, (IStorage) null, null);
//    }
}
