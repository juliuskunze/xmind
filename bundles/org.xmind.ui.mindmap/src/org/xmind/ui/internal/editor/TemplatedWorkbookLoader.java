package org.xmind.ui.internal.editor;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.Core;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.IStorage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.WorkbookFactory;

public class TemplatedWorkbookLoader implements IWorkbookLoader {

    private InputStream templateStream;

    /**
     * @param templateStream
     */
    public TemplatedWorkbookLoader(InputStream templateStream) {
        super();
        this.templateStream = templateStream;
    }

    public IWorkbook loadWorkbook(IStorage storage,
            IEncryptionHandler encryptionHandler, IProgressMonitor monitor)
            throws CoreException {
        if (templateStream == null) {
            return WorkbookFactory.createEmptyWorkbook();
        }

        try {
            return Core.getWorkbookBuilder().loadFromStream(templateStream,
                    storage, encryptionHandler);
        } catch (Throwable e) {
            throw new CoreException(new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID, null, e));
        } finally {
            templateStream = null;
        }
    }
}
