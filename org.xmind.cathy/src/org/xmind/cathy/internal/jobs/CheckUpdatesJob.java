package org.xmind.cathy.internal.jobs;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
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
import org.json.JSONObject;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.FileUtils;

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
            label
                    .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                            false));
            label
                    .setText(WorkbenchMessages.CheckUpdatesJob_NewUpdate_moreDownloads_text);
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
            Program.launch(allDownloadsUrl);
        }

        private void openDownloadUrl() {
            Program.launch(downloadUrl);
        }
    }

    private final IWorkbench workbench;

    private boolean showFailResult;

    public CheckUpdatesJob(IWorkbench workbench, boolean showFailResult) {
        super(WorkbenchMessages.CheckUpdatesJob_jobName);
        this.workbench = workbench;
        this.showFailResult = showFailResult;
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 1);
        if (showFailResult) {
            SafeRunner.run(new SafeRunnable(
                    WorkbenchMessages.CheckUpdatesJob_Fail_message) {
                public void run() throws Exception {
                    doCheck();
                }
            });
        } else {
            try {
                doCheck();
            } catch (Exception e) {
                return new Status(IStatus.WARNING, CathyPlugin.PLUGIN_ID,
                        WorkbenchMessages.CheckUpdatesJob_Fail_message, e);
            }
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    protected void doCheck() throws Exception {
        String url = "http://www.xmind.net/_api/checkVersion/3.1.0/" //$NON-NLS-1$
                + Platform.getOS() + "/" + Platform.getOSArch(); //$NON-NLS-1$
        HttpMethod method = new GetMethod(url);
        HttpClient client = new HttpClient();

        int code = client.executeMethod(method);
        if (code == HttpStatus.SC_OK) {
            String resp = method.getResponseBodyAsString();
            JSONObject json = new JSONObject(resp);
            code = json.getInt("_code"); //$NON-NLS-1$
            if (code == HttpStatus.SC_OK) {
                String downloadUrl = json.getString("download"); //$NON-NLS-1$
                String allDownloadsUrl = json.getString("allDownloads"); //$NON-NLS-1$
                int size = json.getInt("size"); //$NON-NLS-1$
                String version = json.getString("version"); //$NON-NLS-1$
                if (downloadUrl != null) {
                    if (allDownloadsUrl == null)
                        allDownloadsUrl = "http://www.xmind.net/downloads/"; //$NON-NLS-1$
                    showNewUpdateDailog(downloadUrl, version, size,
                            allDownloadsUrl);
                }
            }
        }
    }

    private void showNewUpdateDailog(final String downloadUrl,
            final String version, final int size, final String allDownloadsUrl) {
        workbench.getDisplay().asyncExec(new Runnable() {
            public void run() {
                new NewUpdateDialog(downloadUrl, version, size, allDownloadsUrl)
                        .open();
            }
        });
    }

}
