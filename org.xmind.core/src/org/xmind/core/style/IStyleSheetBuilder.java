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
package org.xmind.core.style;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmind.core.CoreException;

public interface IStyleSheetBuilder {

    IStyleSheet createStyleSheet();

    IStyleSheet loadFromStream(InputStream stream) throws IOException,
            CoreException;

    IStyleSheet loadFromFile(File file) throws IOException, CoreException;

    IStyleSheet loadFromPath(String absolutePath) throws IOException,
            CoreException;

    IStyleSheet loadFromUrl(URL url) throws IOException, CoreException;

    void loadProperties(InputStream stream, IStyleSheet styleSheet)
            throws IOException, CoreException;

}