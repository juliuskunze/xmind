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
package org.xmind.ui.internal.editor;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.IStorage;

/**
 * 
 * @author Karelun huang
 */
public class TempWorkbookLoader implements IWorkbookLoader {

    private WorkbookRef ref;

    private IWorkbookLoader oldLoader;

    private String tempLocation;

    private String filePath;

    private boolean skipRevisions;

    public TempWorkbookLoader(WorkbookRef ref, String tempLocation,
            String filePath, boolean skipRevisions) {
        super();
        this.ref = ref;
        this.oldLoader = ref.getWorkbookLoader();
        this.tempLocation = tempLocation;
        this.filePath = filePath;
        this.skipRevisions = skipRevisions;
    }

    public IWorkbook loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
            throws IOException, CoreException,
            org.eclipse.core.runtime.CoreException {
        try {
            IWorkbook workbook = Core.getWorkbookBuilder()
                    .loadFromTempLocation(tempLocation);
            workbook.setTempLocation(tempLocation);
            workbook.setFile(filePath);
            ((WorkbookImpl) workbook).setSkipRevisionsWhenSaving(skipRevisions);
            if (workbook instanceof ICoreEventSource2) {
                ((ICoreEventSource2) workbook).registerOnceCoreEventListener(
                        Core.WorkbookPreSaveOnce, ICoreEventListener.NULL);
            }
            return workbook;
        } finally {
            ref.setWorkbookLoader(oldLoader);
        }
    }

}
