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
package org.xmind.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;

/**
 * 
 * @author frankshaka
 * 
 */
public interface IWorkbookBuilder {

    /**
     * 
     * @return
     */
    IWorkbook createWorkbook();

    /**
     * 
     * @param targetPath
     * @return
     */
    IWorkbook createWorkbook(String targetPath);

    /**
     * 
     * @param tempLocation
     * @return
     */
    IWorkbook createWorkbookOnTemp(String tempLocation);

    /**
     * 
     * @param storage
     * @return
     */
    IWorkbook createWorkbook(IStorage storage);

    /**
     * 
     * @param file
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromFile(File file) throws IOException, CoreException;

    /**
     * 
     * @param file
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromFile(File file, IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * 
     * @param file
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromFile(File file, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException;

    /**
     * 
     * @param path
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromPath(String path) throws IOException, CoreException;

    /**
     * 
     * @param path
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromPath(String path, IEncryptionHandler encryptionHandler)
            throws IOException, CoreException;

    /**
     * 
     * @param path
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromPath(String path, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException;

    /**
     * 
     * @param tempLocation
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromTempLocation(String tempLocation) throws IOException,
            CoreException;

    /**
     * 
     * @param storage
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromStorage(IStorage storage) throws IOException,
            CoreException;

    /**
     * NOTE: The input stream will be consumed after loading.
     * 
     * @param in
     * @param tempLocation
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromStream(InputStream in, String tempLocation)
            throws IOException, CoreException;

    /**
     * NOTE: The input stream will be consumed after loading.
     * 
     * @param in
     * @param storage
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromStream(InputStream in, IStorage storage)
            throws IOException, CoreException;

    /**
     * NOTE: The input stream will be closed after loading.
     * 
     * @param in
     * @param tempLocation
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromStream(InputStream in, String tempLocation,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException;

    /**
     * NOTE: The input stream will be consumed after loading.
     * 
     * @param in
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromStream(InputStream in, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException;

    /**
     * 
     * @param source
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromInputSource(IInputSource source) throws IOException,
            CoreException;

    /**
     * 
     * @param source
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromInputSource(IInputSource source,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException;

    /**
     * 
     * @param source
     * @param storage
     * @param encryptionHandler
     * @return
     * @throws IOException
     * @throws CoreException
     */
    IWorkbook loadFromInputSource(IInputSource source, IStorage storage,
            IEncryptionHandler encryptionHandler) throws IOException,
            CoreException;

    /**
     * 
     * @param encryptionHandler
     */
    void setDefaultEncryptionHandler(IEncryptionHandler encryptionHandler);

}