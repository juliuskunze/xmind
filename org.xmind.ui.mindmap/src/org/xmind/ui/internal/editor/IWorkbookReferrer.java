package org.xmind.ui.internal.editor;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;

public interface IWorkbookReferrer {

//    void setSelection(ISelection selection, boolean reveal, boolean forceFocus);

    void savePreivew(IWorkbook workbook, IProgressMonitor monitor)
            throws IOException, CoreException;

    void postSave(IProgressMonitor monitor);

    void postSaveAs(Object newKey, IProgressMonitor monitor);

}
