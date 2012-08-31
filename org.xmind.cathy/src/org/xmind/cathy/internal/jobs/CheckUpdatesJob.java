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
package org.xmind.cathy.internal.jobs;

import net.xmind.signin.IDataStore;
import net.xmind.signin.XMindNet;
import net.xmind.signin.internal.XMindNetRequest;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.FileUtils;

/**
 * 
 * @author Frank Shaka
 */
public class CheckUpdatesJob extends Job {

    private static class NewUpdateDialog extends Dialog {

        private String downloadUrl;

        private String version;

        private int size;

        private String allDownloadsUrl;

        public NewUpdateDialog(String downloadUrl, String version, int size,
                String allDownloadsUrl) {
            super((Shell) null);
            this.downloadUrl = downloadUrl;
            this.version = version;
            this.size = size;
            this.allDownloadsUrl = allDownloadsUrl;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            createImageAndText(composite);
            return composite;
        }

        private void createImageAndText(Composite parent) {
            Composite imageAndText = new Composite(parent, SWT.NONE);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            imageAndText.setLayoutData(gridData);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 20;
            imageAndText.setLayout(gridLayout);

            createImage(imageAndText);
            createText(imageAndText);
        }

        private void createImage(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false,
                    false));
            ImageDescriptor imgDesc = AbstractUIPlugin
                    .imageDescriptorFromPlugin("org.xmind.cathy", //$NON-NLS-1$
                            "icons/xmind.48.png"); //$NON-NLS-1$
            if (imgDesc != null) {
                final Image img = imgDesc.createImage();
                label.setImage(img);
                label.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        img.dispose();
                    }
                });
            } else {
                label.setImage(parent.getDisplay().getSystemImage(
                        SWT.ICON_INFORMATION));
            }
        }

        private void createText(Composite parent) {
            Composite textArea = new Composite(parent, SWT.NONE);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.widthHint = 420;
            gridData.heightHint = SWT.DEFAULT;
            textArea.setLayoutData(gridData);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 10;
            gridLayout.horizontalSpacing = 0;
            textArea.setLayout(gridLayout);

            createMessageArea(textArea);
            createInfoArea(textArea);
            createMoreDownloadsArea(textArea);
        }

        private void createMessageArea(Composite parent) {
            Label label = new Label(parent, SWT.WRAP);
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            label.setText(WorkbenchMessages.CheckUpdatesJob_NewUpdate_message);
            label.setFont(FontUtils.getBoldRelative(
                    JFaceResources.DEFAULT_FONT, 1));
        }

        private void createInfoArea(Composite parent) {
            Label label = new Label(parent, SWT.WRAP);
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            label.setText(NLS.bind(
                    WorkbenchMessages.CheckUpdatesJob_NewUpdate_info_message,
                    version, FileUtils.fileLengthToString(size)));
        }

        private void createMoreDownloadsArea(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            label.setText(WorkbenchMessages.CheckUpdatesJob_NewUpdate_moreDownloads_text);
            label.setForeground(parent.getDisplay().getSystemColor(
                    SWT.COLOR_BLUE));
            label.addListener(SWT.MouseUp, new Listener() {
                public void handleEvent(Event event) {
                    openAllDownloadsUrl();
                }
            });
            label.setCursor(parent.getDisplay()
                    .getSystemCursor(SWT.CURSOR_HAND));
        }

        @Override
        protected void okPressed() {
            super.okPressed();
            openDownloadUrl();
        }

        private void openAllDownloadsUrl() {
            XMindNet.gotoURL(true, allDownloadsUrl);
        }

        private void openDownloadUrl() {
            Program.launch(downloadUrl);
        }
    }

    private final IWorkbench workbench;

    private boolean showFailureResult;

    private XMindNetRequest checkVersionRequest = null;

    public CheckUpdatesJob(IWorkbench workbench, boolean showFailureResult) {
        super(WorkbenchMessages.CheckUpdatesJob_jobName);
        this.workbench = workbench;
        this.showFailureResult = showFailureResult;
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 1);
        if (showFailureResult) {
            try {
                doCheck();
            } catch (Throwable e) {
                return new Status(IStatus.ERROR, CathyPlugin.PLUGIN_ID,
                        WorkbenchMessages.CheckUpdatesJob_Fail_message, e);
            }
        } else {
            try {
                doCheck();
            } catch (Throwable e) {
                return new Status(IStatus.WARNING, CathyPlugin.PLUGIN_ID,
                        WorkbenchMessages.CheckUpdatesJob_Fail_message, e);
            }
        }
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.done();
        return Status.OK_STATUS;
    }

    protected void doCheck() throws Throwable {
        XMindNetRequest request = new XMindNetRequest();
        this.checkVersionRequest = request;
        request.uri("/_api/checkVersion/3.3.0"); //$NON-NLS-1$
        request.addParameter("distrib", CathyPlugin.getDistributionId()); //$NON-NLS-1$
        request.get();

        if (request.isAborted())
            return;

        int code = request.getCode();
        IDataStore data = request.getData();
        if (code == HttpStatus.SC_OK && data != null) {
            String downloadUrl = data.getString("download"); //$NON-NLS-1$
            if (downloadUrl != null) {
                String allDownloadsUrl = data.getString("allDownloads"); //$NON-NLS-1$
                int size = data.getInt("size"); //$NON-NLS-1$
                String version = data.getString("version"); //$NON-NLS-1$
                if (allDownloadsUrl == null) {
                    allDownloadsUrl = "http://www.xmind.net/xmind/downloads/"; //$NON-NLS-1$
                }
                showNewUpdateDialog(downloadUrl, version, size, allDownloadsUrl);
            } else {
                showNoUpdateDialog();
            }
        } else {
            if (request.getException() != null)
                throw request.getException();
            showNoUpdateDialog();
        }

//        String url = "http://www.xmind.net/_api/checkVersion/3.3.0"; //$NON-NLS-1$
//        String distribId = CathyPlugin.getDistributionId();
//        url = url + "?distrib=" + distribId; //$NON-NLS-1$
//        HttpMethod method = new GetMethod(url);
//        int code = new HttpClient().executeMethod(method);
//        if (code == HttpStatus.SC_OK) {
//            String resp = method.getResponseBodyAsString();
//            JSONObject json = new JSONObject(resp);
//            code = json.getInt("_code"); //$NON-NLS-1$
//            if (code == HttpStatus.SC_OK) {
//                String downloadUrl = json.getString("download"); //$NON-NLS-1$
//                String allDownloadsUrl = json.getString("allDownloads"); //$NON-NLS-1$
//                int size = json.getInt("size"); //$NON-NLS-1$
//                String version = json.getString("version"); //$NON-NLS-1$
//                if (downloadUrl != null) {
//                    if (allDownloadsUrl == null)
//                        allDownloadsUrl = "http://www.xmind.net/downloads/"; //$NON-NLS-1$
//                    showNewUpdateDialog(downloadUrl, version, size,
//                            allDownloadsUrl);
//                } else {
//                    showNoUpdateDialog();
//                }
//            } else {
//                showNoUpdateDialog();
//            }
//        } else {
//            showNoUpdateDialog();
//        }
    }

    @Override
    protected void canceling() {
        if (checkVersionRequest != null) {
            checkVersionRequest.abort();
        }
        super.canceling();
    }

    private void showNoUpdateDialog() {
        if (showFailureResult) {
            workbench.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openInformation(null,
                            WorkbenchMessages.AppWindowTitle,
                            WorkbenchMessages.CheckUpdatesJob_NoUpdate_message);
                }
            });
        }
    }

    private void showNewUpdateDialog(final String downloadUrl,
            final String version, final int size, final String allDownloadsUrl) {
        workbench.getDisplay().asyncExec(new Runnable() {
            public void run() {
                new NewUpdateDialog(downloadUrl, version, size, allDownloadsUrl)
                        .open();
            }
        });
    }

}
