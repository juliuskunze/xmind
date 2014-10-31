package org.xmind.ui.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.gef.command.CreateCommand;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.internal.editor.TemplatedWorkbookLoader;
import org.xmind.ui.internal.editor.WorkbookBackupManager;
import org.xmind.ui.internal.editor.WorkbookRef;
import org.xmind.ui.internal.editor.WorkbookRefManager;
import org.xmind.ui.mindmap.MindMapUI;

public class NewSheetFromTemplateCommand extends CreateCommand {
    private static final String SUBDIR_TEMPLATE = "temp_templates"; //$NON-NLS-1$

    private ITemplateDescriptor template;

    private IWorkbook workbook;

    private List<ISheet> sheets;

    public NewSheetFromTemplateCommand(ITemplateDescriptor template,
            IWorkbook workbook) {
        super();
        this.template = template;
        this.workbook = workbook;
    }

    protected boolean canCreate() {
        return true;
    }

    protected Object create() {
        TemplatedWorkbookLoader templatedWorkbookLoader = new TemplatedWorkbookLoader(
                template.newStream());
        IWorkbook newWorkbook = null;
        IStorage storage = null;
        try {
            storage = createStorage();
            newWorkbook = templatedWorkbookLoader.loadWorkbook(storage, null,
                    null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        List<ISheet> oldSheets = newWorkbook.getSheets();
        Collection collection = workbook.clone(oldSheets).getCloneds();
        sheets = new ArrayList<ISheet>();
        if (collection != null && !collection.isEmpty()) {
            for (Object obj : collection) {
                if (obj instanceof ISheet) {
                    sheets.add((ISheet) obj);
                }
            }
        }

        storage.clear();
        WorkbookBackupManager.getInstance().removeWorkbook(
                (WorkbookRef) WorkbookRefManager.getInstance().findRef(
                        newWorkbook));
        newWorkbook = null;

        return sheets;
    }

    private IStorage createStorage() {
        String tempFile = Core.getIdFactory().createId()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        String tempLocation = Core.getWorkspace().getTempDir(
                SUBDIR_TEMPLATE + "/" + tempFile); //$NON-NLS-1$
        File tempDir = new File(tempLocation);
        IStorage storage = new DirectoryStorage(tempDir);
        return storage;
    }

    public void redo() {
        addSheets();
        super.redo();
    }

    private void addSheets() {
        for (ISheet sheet : sheets) {
            workbook.addSheet(sheet);
        }
    }

    public void undo() {
        removeSheets();
        super.undo();
    }

    private void removeSheets() {
        for (ISheet sheet : sheets) {
            workbook.removeSheet(sheet);
        }
    }

}
