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
package org.xmind.ui.internal.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.ui.wizards.MindMapImporter;

public class XMind2008Importer extends MindMapImporter {

    /**
     * 
     * @param sourcePath
     * @param targetWorkbook
     */
    public XMind2008Importer(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.wizards.MindMapImporter#build()
     */
    public void build() throws InvocationTargetException, InterruptedException {
        try {
            IWorkbook sourceWorkbook = Core.getWorkbookBuilder().loadFromPath(
                    getSourcePath());
            List<ISheet> sourceSheets = sourceWorkbook.getSheets();
            if (!sourceSheets.isEmpty()) {
                IWorkbook targetWorkbook = getTargetWorkbook();
                ICloneData data = targetWorkbook.clone(sourceSheets);
                if (data != null) {
                    for (ISheet sourceSheet : sourceSheets) {
                        Object targetSheet = data.get(sourceSheet);
                        if (targetSheet instanceof ISheet) {
                            addTargetSheet((ISheet) targetSheet);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

}
