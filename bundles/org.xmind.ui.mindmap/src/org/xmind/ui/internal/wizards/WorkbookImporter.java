package org.xmind.ui.internal.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.MindMapImporter;

public class WorkbookImporter extends MindMapImporter {

    private static final String FILE_PROTOCOL = "file:"; //$NON-NLS-1$

    private IWorkbook targetWorkbook;
    private IStorage storage;
    private IWorkbook sourceWorkbook;
    private List<ISheet> sheets;

    public WorkbookImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        try {
            storage = createStorage();
            sourceWorkbook = Core.getWorkbookBuilder().loadFromPath(
                    getSourcePath(), storage, null);
            targetWorkbook = getTargetWorkbook();

            sheets = sourceWorkbook.getSheets();
            ICloneData cloned = targetWorkbook.clone(sheets);
            for (ISheet sheet : sheets) {

                ISheet clonedSheet = (ISheet) cloned.get(sheet);
                fixRelativeFileHyperlinks(clonedSheet.getRootTopic(),
                        getSourcePath(), targetWorkbook.getFile());
                addTargetSheet(clonedSheet);
            }
        } catch (Throwable e) {
            throw new InvocationTargetException(e);
        }
    }

    private IStorage createStorage() {
        String tempFile = Core.getIdFactory().createId()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        String tempLocation = Core.getWorkspace().getTempDir(
                "workbooks" + "/" + tempFile); //$NON-NLS-1$ //$NON-NLS-2$
        File tempDir = new File(tempLocation);
        return new DirectoryStorage(tempDir);
    }

    private void fixRelativeFileHyperlinks(ITopic topic, String sourcePath,
            String targetPath) {
        String hyperlink = topic.getHyperlink();
        if (hyperlink == null || !hyperlink.startsWith(FILE_PROTOCOL)) {
            List<ITopic> topics = topic.getAllChildren();
            if (topics != null) {
                for (ITopic temptopic : topics) {
                    fixRelativeFileHyperlinks(temptopic, sourcePath, targetPath);
                }
            }
            return;
        }
        String path = FilePathParser.toPath(hyperlink);
        if (!FilePathParser.isPathRelative(path))
            return;

        File sourceBase = new File(sourcePath).getParentFile();
        File targetBase = targetPath == null ? new File(
                FilePathParser.ABSTRACT_FILE_BASE) : new File(targetPath)
                .getParentFile();
        String absolutePath = FilePathParser.toAbsolutePath(
                sourceBase.getAbsolutePath(), path);
        hyperlink = FilePathParser.toRelativePath(targetBase.getAbsolutePath(),
                absolutePath);
        topic.setHyperlink(FilePathParser.toURI(hyperlink, true));
        return;
    }

}
