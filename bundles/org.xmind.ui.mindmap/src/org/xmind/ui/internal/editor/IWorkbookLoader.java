package org.xmind.ui.internal.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.IStorage;

public interface IWorkbookLoader {

    IWorkbook loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
            throws CoreException;

}
