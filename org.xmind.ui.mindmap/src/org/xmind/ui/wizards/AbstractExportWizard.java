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
package org.xmind.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.xmind.core.Core;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.wizards.WizardMessages;

public abstract class AbstractExportWizard extends Wizard implements
        IExportWizard {

    protected static final String KEY_PATH_HISTORY = "PATH_HISTORY"; //$NON-NLS-1$

    protected static final String FILTER_ALL_FILES = "*.*"; //$NON-NLS-1$

    private String targetPath;

    private boolean overwriteWithoutPrompt = false;

    private List<String> pathHistory = new ArrayList<String>();

    private List<String> temporaryPaths = null;

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        loadDialogSettings();
    }

    protected void loadDialogSettings() {
        if (getDialogSettings() != null) {
            loadDialogSettings(getDialogSettings());
        }
    }

    protected void loadDialogSettings(IDialogSettings settings) {
        String history = settings.get(KEY_PATH_HISTORY);
        if (history != null && !"".equals(history)) { //$NON-NLS-1$
            String[] paths = history.split("\\|"); //$NON-NLS-1$
            for (String path : paths) {
                if (!"".equals(path)) //$NON-NLS-1$
                    pathHistory.add(path);
            }
        }
    }

    public void dispose() {
        saveDialogSettings();
        super.dispose();
        deleteTemporaryPaths();
    }

    protected void saveDialogSettings() {
        if (getDialogSettings() != null) {
            saveDialogSettings(getDialogSettings());
        }
    }

    protected void saveDialogSettings(IDialogSettings settings) {
        if (targetPath != null) {
            if (!pathHistory.contains(targetPath)) {
                pathHistory.add(targetPath);
            }
            StringBuilder sb = new StringBuilder(pathHistory.size() * 20);
            for (String path : pathHistory) {
                if (sb.length() > 0)
                    sb.append('|');
                sb.append(path);
            }
            settings.put(KEY_PATH_HISTORY, sb.toString());
            targetPath = null;
        }
    }

    public boolean canFinish() {
        return super.canFinish() && hasTargetPath();
    }

    protected void launchTargetFile(boolean fileOrDirectory,
            IProgressMonitor monitor, Display display, Shell parentShell) {
        if (new File(getTargetPath()).exists()) {
            monitor.subTask(WizardMessages.ExportPage_Launching);
            Program.launch(getTargetPath());
        }
    }

    protected String requestTemporaryPath(String applicationName,
            String fileNameExtension, boolean fileOrDirectory) {
        String tempDir = Core.getWorkspace().getTempDir();
        File mainDir = FileUtils.ensureDirectory(new File(tempDir, "export")); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder(26
                + (applicationName == null ? 0 : applicationName.length() + 1)
                + (fileNameExtension == null ? 4
                        : fileNameExtension.length() + 1));
        if (applicationName != null) {
            sb.append(applicationName);
            sb.append('_');
        }
        sb.append(Core.getIdFactory().createId());
        if (fileNameExtension == null) {
            sb.append(".tmp"); //$NON-NLS-1$
        } else {
            if (fileNameExtension.charAt(0) != '.')
                sb.append('.');
            sb.append(fileNameExtension);
        }
        String name = sb.toString();
        File file = new File(mainDir, name);
        if (!fileOrDirectory)
            FileUtils.ensureDirectory(file);
        String result = file.getAbsolutePath();
        if (temporaryPaths == null)
            temporaryPaths = new ArrayList<String>(3);
        temporaryPaths.add(result);
        return result;
    }

    protected void deleteTemporaryPath(String path) {
        if (FileUtils.delete(new File(path))) {
            if (temporaryPaths != null) {
                temporaryPaths.remove(path);
                if (temporaryPaths.isEmpty())
                    temporaryPaths = null;
            }
        }
    }

    protected void deleteTemporaryPaths() {
        if (temporaryPaths != null) {
            for (Object o : temporaryPaths.toArray()) {
                deleteTemporaryPath((String) o);
            }
        }
    }

    protected String cleanFileName(String name) {
        if (name == null)
            return ""; //$NON-NLS-1$
        return name.replaceAll("\\r\\n|\\r|\\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setTargetPath(String path) {
        this.targetPath = path;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean hasTargetPath() {
        return this.targetPath != null && !"".equals(this.targetPath); //$NON-NLS-1$
    }

    public List<String> getPathHistory() {
        return pathHistory;
    }

    public void setOverwriteWithoutPrompt(boolean overwriteWithoutPrompt) {
        this.overwriteWithoutPrompt = overwriteWithoutPrompt;
    }

    public boolean isOverwriteWithoutPrompt() {
        return overwriteWithoutPrompt;
    }

}