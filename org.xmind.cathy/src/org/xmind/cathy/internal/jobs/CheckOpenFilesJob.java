package org.xmind.cathy.internal.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.Bundle;
import org.xmind.cathy.internal.Log;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.editor.WorkbookEditorInput;
import org.xmind.ui.internal.imports.freemind.FreeMindImporter;
import org.xmind.ui.internal.imports.mm.MindManagerImporter;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;
import org.xmind.ui.wizards.MindMapImporter;

public class CheckOpenFilesJob extends AbstractCheckFilesJob {

    private List<String> filesToOpen;

    private boolean startsPresentation;

    private boolean markersImported = false;

    public CheckOpenFilesJob(IWorkbench workbench) {
        super(workbench, WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name);
    }

    @Override
    protected void prepare() {
        filesToOpen = null;
        startsPresentation = false;
        markersImported = false;
        super.prepare();
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 4);
        doCheckAndOpenFiles(monitor);
        monitor.done();
        return Status.OK_STATUS;
    }

    /**
     * Check and open requested files logged in the .opening file. This method
     * consume 4 ticks from the given progress monitor.
     * 
     * @param monitor
     *            the progress monitor
     */
    protected void doCheckAndOpenFiles(IProgressMonitor monitor) {
        checkFilesToOpen(new SubProgressMonitor(monitor, 1));

        if (filesToOpen != null && !filesToOpen.isEmpty()) {
            readFilesToOpen(new SubProgressMonitor(monitor, 1));
            openEditors(monitor,
                    WorkbenchMessages.CheckOpenFilesJob_OpenFiles_name, 1, true);
        } else {
            monitor.worked(2);
        }

        if (startsPresentation) {
            startPresentation(new SubProgressMonitor(monitor, 1));
        } else {
            monitor.worked(1);
            if (markersImported)
                showMarkersPrefPage();
        }
    }

    public boolean startsPresentation() {
        return startsPresentation;
    }

    private void checkFilesToOpen(IProgressMonitor monitor) {
        monitor.beginTask(WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name,
                1);
        Log opening = Log.get(Log.OPENING);
        if (opening.exists()) {
            String[] contents = opening.getContents();
            for (String line : contents) {
                if ("-p".equals(line)) { //$NON-NLS-1$
                    startsPresentation = true;
                } else {
                    if (filesToOpen == null)
                        filesToOpen = new ArrayList<String>();
                    filesToOpen.add(line);
                }
            }
            opening.delete();
        }
        monitor.done();
    }

    private void readFilesToOpen(final IProgressMonitor monitor) {
        monitor.beginTask(WorkbenchMessages.CheckOpenFilesJob_OpenFiles_name,
                filesToOpen.size());
        for (final String fileName : filesToOpen) {
            SafeRunner.run(new SafeRunnable(NLS.bind(
                    WorkbenchMessages.CheckOpenFilesJob_FailsToOpen_message,
                    fileName)) {
                public void run() throws Exception {
                    monitor.subTask(fileName);
                    IEditorInput input = createEditorInput(fileName, monitor);
                    if (input != null) {
                        addEditorToOpen(input);
                    }
                    monitor.worked(1);
                }
            });
        }
        monitor.done();
    }

    private IEditorInput createEditorInput(String fileName,
            IProgressMonitor monitor) throws Exception {
        File file = new File(fileName);
        if (!file.exists() || !file.isFile())
            return null;

        final String path = fileName;
        String extension = FileUtils.getExtension(path);

        if (MindMapUI.FILE_EXT_TEMPLATE.equalsIgnoreCase(extension)) {
            return newFromTemplate(path);
        } else if (".mmap".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            return importMindManagerFile(path);
        } else if (".mm".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            return importFreeMindFile(path);
        } else if (MindMapUI.FILE_EXT_MARKER_PACKAGE
                .equalsIgnoreCase(extension)) {
            return importMarkers(path);
        } else {
            // assumes we're opening xmind files
            return MME.createFileEditorInput(path);
        }
    }

    private IEditorInput newFromTemplate(String path) throws Exception {
        return MME.createTemplatedEditorInput(new FileInputStream(path));
    }

    private IEditorInput importMindManagerFile(String path) throws Exception {
        MindMapImporter importer = new MindManagerImporter(path);
        importer.build();
        IWorkbook workbook = importer.getTargetWorkbook();
        return workbook == null ? null : new WorkbookEditorInput(workbook);
    }

    private IEditorInput importFreeMindFile(String path) throws Exception {
        FreeMindImporter importer = new FreeMindImporter(path);
        importer.build();
        IWorkbook workbook = importer.getTargetWorkbook();
        return workbook == null ? null : new WorkbookEditorInput(workbook);
    }

    private IEditorInput importMarkers(String path) throws Exception {
        MarkerImpExpUtils.importMarkerPackage(path);
        markersImported = true;
        return null;
    }

    private void startPresentation(IProgressMonitor monitor) {
        monitor.beginTask(
                WorkbenchMessages.CheckOpenFilesJob_ShowPresentation_name, 1);
        IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                final IEditorPart editor = page.getActiveEditor();
                if (editor != null) {
                    Display display = getWorkbench().getDisplay();
                    if (display != null && !display.isDisposed()) {
                        display.syncExec(new Runnable() {
                            public void run() {
                                SafeRunner.run(new SafeRunnable() {
                                    public void run() throws Exception {
                                        startPresentation(editor);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }
        monitor.done();
    }

    private void startPresentation(IEditorPart sourceEditor) {
        final IEditorActionDelegate delegate = createPresentationDelegate();
        if (delegate == null)
            return;

        final IAction action = new Action() {
        };
        delegate.setActiveEditor(action, sourceEditor);
        delegate.run(action);
    }

    private IEditorActionDelegate createPresentationDelegate() {
        String clazz = "org.xmind.ui.internal.presentation.ShowPresentationActionDelegate"; //$NON-NLS-1$
        Bundle bundle = Platform.getBundle("org.xmind.ui.presentation"); //$NON-NLS-1$
        if (bundle != null) {
            try {
                return (IEditorActionDelegate) bundle.loadClass(clazz)
                        .newInstance();
            } catch (Throwable e) {
                Logger.log(e);
            }
        }
        return null;
    }

    private void showMarkersPrefPage() {
        Display display = getWorkbench().getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.asyncExec(new Runnable() {
            public void run() {
                PreferencesUtil.createPreferenceDialogOn(null,
                        MarkerManagerPrefPage.ID, null, null).open();
            }
        });
    }

}
