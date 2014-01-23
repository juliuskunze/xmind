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
package org.xmind.core.internal.compatibility;

import java.io.IOException;

import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.dom.FileFormat_0_1;
import org.xmind.core.internal.dom.FileFormat_1;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.IXMLLoader;

public class Compatibility {

    public static IWorkbook loadCompatibleWorkbook(IInputSource source,
            IXMLLoader loader, IStorage storage) throws CoreException,
            IOException {

        WorkbookImpl workbook = null;

        FileFormat format = new FileFormat_0_1(source, loader, storage);

        if (format.identifies())
            workbook = format.load();

        if (workbook == null) {
            format = new FileFormat_1(source, loader, storage);
            if (format.identifies())
                workbook = format.load();
        }

        return workbook;
    }

}