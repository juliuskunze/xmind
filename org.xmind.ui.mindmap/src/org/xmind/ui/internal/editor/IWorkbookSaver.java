package org.xmind.ui.internal.editor;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;

public interface IWorkbookSaver {

    /**
     * Determines whether the save operation will overwrite existing target.
     * 
     * @return <code>true</code> if the save operationg will overwrite existing
     *         target, or <code>false</code> otherwise
     */
    boolean canSaveToTarget();

    /**
     * Save the workbook.
     * 
     * @param monitor
     * @param workbook
     * @throws IOException
     * @throws CoreException
     * @throws org.eclipse.core.runtime.CoreException
     */
    void save(IProgressMonitor monitor, IWorkbook workbook) throws IOException,
            CoreException, org.eclipse.core.runtime.CoreException;

}
