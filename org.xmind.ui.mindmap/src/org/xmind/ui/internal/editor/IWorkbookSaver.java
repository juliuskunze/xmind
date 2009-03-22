package org.xmind.ui.internal.editor;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;

public interface IWorkbookSaver {

    void save(IProgressMonitor monitor, IWorkbook workbook) throws IOException,
            CoreException, org.eclipse.core.runtime.CoreException;

}
