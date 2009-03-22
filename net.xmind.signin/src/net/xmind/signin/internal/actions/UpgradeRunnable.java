/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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

package net.xmind.signin.internal.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.xmind.signin.internal.Activator;
import net.xmind.signin.internal.Messages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.wizards.InstallWizard2;
import org.eclipse.update.internal.ui.wizards.ResizableInstallWizardDialog;
import org.eclipse.update.operations.IInstallFeatureOperation;
import org.eclipse.update.operations.OperationsManager;
import org.eclipse.update.search.BackLevelFilter;
import org.eclipse.update.search.EnvironmentFilter;
import org.eclipse.update.search.IUpdateSearchResultCollector;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;

@SuppressWarnings("restriction")
public class UpgradeRunnable implements Runnable {

//    private static class SpecificFeatureFilter implements IUpdateSearchFilter {
//
//        private String featureId;
//
//        /**
//         * @param featureId
//         */
//        public SpecificFeatureFilter(String featureId) {
//            this.featureId = featureId;
//        }
//
//        /**
//         * @see org.eclipse.update.search.IUpdateSearchFilter#accept(org.eclipse.update.core.IFeature)
//         */
//        public boolean accept(IFeature match) {
//            return featureId.equals(match.getVersionedIdentifier()
//                    .getIdentifier());
//        }
//
//        /**
//         * @see org.eclipse.update.search.IUpdateSearchFilter#accept(org.eclipse.update.core.IFeatureReference)
//         */
//        public boolean accept(IFeatureReference match) {
//            try {
//                return featureId.equals(match.getVersionedIdentifier()
//                        .getIdentifier());
//            } catch (CoreException e) {
//                return false;
//            }
//        }
//
//    }

    private static class UpgradeSourceDialog extends Dialog {

        private String url;

        private Button onlineButton;

        private Button localFileButton;

        public UpgradeSourceDialog(Shell parentShell) {
            super(parentShell);
            setBlockOnOpen(true);
            setShellStyle(SWT.TITLE | SWT.CLOSE);
        }

        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.UpgradeSourceDialog_title);
        }

        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);

            Label label = new Label(composite, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            label.setText(Messages.UpgradeSourceDialog_label);

            onlineButton = new Button(composite, SWT.RADIO);
            onlineButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                    false));
            onlineButton.setText(Messages.UpgradeSourceDialog_Online);

            localFileButton = new Button(composite, SWT.RADIO);
            localFileButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                    true, false));
            localFileButton.setText(Messages.UpgradeSourceDialog_LocalFile);

            onlineButton.setSelection(true);
            localFileButton.setSelection(false);

            return composite;
        }

        protected void okPressed() {
            boolean openFileDialog = localFileButton.getSelection();
            if (openFileDialog) {
                FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
                fileDialog.setFilterExtensions(new String[] { "*.*" }); //$NON-NLS-1$
                fileDialog.setFilterNames(new String[] { "(*.*)" }); //$NON-NLS-1$
                String path = fileDialog.open();
                if (path == null) {
                    return;
                }
                this.url = "jar:file:" + path + "!/"; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                this.url = "http://www.xmind.net/xmind/updates/xmindpro3/"; //$NON-NLS-1$
            }
            super.okPressed();
        }

        public String getURL() {
            return url;
        }
    }

    private class SearchResultCollector implements IUpdateSearchResultCollector {
        public void accept(IFeature feature) {
            IInstallFeatureOperation operation = OperationsManager
                    .getOperationFactory().createInstallOperation(null,
                            feature, null, null, null);
            updates.add(operation);
        }
    }

    private class UpgradeJob extends Job {

        /**
         * @param name
         */
        public UpgradeJob() {
            super(Messages.Upgrade_jobName);
        }

        /**
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                searchRequest.performSearch(new SearchResultCollector(),
                        monitor);
                return Status.OK_STATUS;
            } catch (OperationCanceledException e) {
                return Status.CANCEL_STATUS;
            } catch (CoreException e) {
                return e.getStatus();
            }
        }

    }

    private static UpgradeRunnable runningInstance = null;

    private Shell parentShell;

    private UpdateSearchRequest searchRequest;

    private List<IInstallFeatureOperation> updates;

    private UpgradeJob job;

    /**
     * 
     */
    public UpgradeRunnable() {
        this(null);
    }

    /**
     * 
     */
    public UpgradeRunnable(Shell parentShell) {
        this.parentShell = parentShell;
        if (runningInstance == null)
            runningInstance = this;
    }

    public void run() {
        UpgradeSourceDialog dialog = new UpgradeSourceDialog(parentShell);
        int retCode = dialog.open();
        if (retCode != UpgradeSourceDialog.OK) {
            notifyStopped();
            return;
        }

        String url = dialog.getURL();
        UpdateSearchScope scope = new UpdateSearchScope();
        try {
            scope.addSearchSite("XMind Pro", new URL(url), null); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            Activator.log(e);
        }
        scope.setFeatureProvidedSitesEnabled(false);
        searchRequest = new UpdateSearchRequest(UpdateSearchRequest
                .createDefaultSiteSearchCategory(), scope);
        searchRequest.addFilter(new EnvironmentFilter());
        searchRequest.addFilter(new BackLevelFilter());

//        searchRequest.addFilter(new SpecificFeatureFilter(
//                "org.xmind.meggy.feature"));
//        searchRequest.addFilter(new SpecificFeatureFilter(
//                "org.xmind.meggy.feature.nl_de"));
//        searchRequest.addFilter(new SpecificFeatureFilter(
//                "org.xmind.meggy.feature.nl_ja"));
//        searchRequest.addFilter(new SpecificFeatureFilter(
//                "org.xmind.meggy.feature.nl_zh_CN"));
//        searchRequest.addFilter(new SpecificFeatureFilter(
//                "org.xmind.meggy.feature.nl_zh_TW"));

        searchFeature(Display.getCurrent());
    }

    /**
     * 
     */
    private void searchFeature(final Display display) {
        updates = new ArrayList<IInstallFeatureOperation>();
        job = new UpgradeJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            /**
             * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
             */
            @Override
            public void done(IJobChangeEvent event) {
                afterSearch(display, event.getResult());
            }

        });
        job.schedule();
    }

    /**
     * @param result
     */
    private void afterSearch(final Display display, final IStatus result) {
        if (result == Status.CANCEL_STATUS)
            return;
        if (result != Status.OK_STATUS)
            display.syncExec(new Runnable() {
                public void run() {
                    UpdateUI.log(result, true);
                }
            });
        display.asyncExec(new Runnable() {
            public void run() {
                BusyIndicator.showWhile(display, new Runnable() {
                    public void run() {
                        openInstallWizard();
                    }
                });
            }
        });
    }

    /**
     * 
     */
    private void openInstallWizard() {
        if (InstallWizard2.isRunning()) {
            MessageDialog.openInformation(parentShell,
                    UpdateUIMessages.InstallWizard_isRunningTitle,
                    UpdateUIMessages.InstallWizard_isRunningInfo);
            return;
        }
        if (updates == null || updates.isEmpty()) {
            MessageDialog.openInformation(parentShell,
                    Messages.Upgrade_ErrorDialog_title,
                    Messages.Upgrade_ErrorDialog_message);
            return;
        }
        InstallWizard2 wizard = new InstallWizard2(searchRequest, updates
                .toArray(new IInstallFeatureOperation[0]), true);
        final WizardDialog dialog = new ResizableInstallWizardDialog(
                parentShell, wizard, Messages.Upgrade_InstallDialog_title);
        dialog.create();
        dialog.open();
        notifyStopped();
    }

    private void notifyStopped() {
        if (runningInstance == this) {
            runningInstance = null;
        }
    }

    public static boolean isRunning() {
        return runningInstance != null;
    }

}