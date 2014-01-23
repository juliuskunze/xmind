/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.xmind.share.dialog.UploaderDialog;
import net.xmind.share.jobs.UploadJob;
import net.xmind.share.jobs.UploadSession;
import net.xmind.signin.IAccountInfo;
import net.xmind.signin.IAuthenticationListener;
import net.xmind.signin.XMindNet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;
import org.xmind.core.Core;
import org.xmind.core.IMeta;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.ui.dialogs.NotificationWindow;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapExtractor;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

public class Uploader extends JobChangeAdapter {

    private Shell parentShell;

    private IMindMapViewer sourceViewer;

    private IWorkbook workbook;

    private MindMapExtractor extractor;

    private Info info;

    private Point origin;

    private Image fullImage;

    private File file;

    private boolean trimmed = false;

    public Uploader(Shell parentShell, IMindMapViewer sourceViewer) {
        this.parentShell = parentShell;
        this.sourceViewer = sourceViewer;
    }

    public void upload() {
        info = new Info();

        boolean prepared = false;
        try {
            prepared = prepare();
        } catch (OutOfMemoryError e) {
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID,
                            Messages.ErrorDialog_OutOfMemory_message, e),
                    StatusManager.SHOW);
//            XmindSharePlugin.log(e, "Failed to prepare XMind file to upload."); //$NON-NLS-1$
//            new ErrorDetailsDialog(parentShell, Messages.ErrorDialog_title,
//                    Messages.ErrorDialog_OutOfMemory_message, e,
//                    "Failed to prepare XMind file to upload.", //$NON-NLS-1$
//                    System.currentTimeMillis()).open();
        } catch (Throwable e) {
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID,
                            Messages.ErrorDialog_UnexpectedError_message, e),
                    StatusManager.SHOW);
//            XmindSharePlugin.log(e, "Failed to prepare XMind file to upload."); //$NON-NLS-1$
//            new ErrorDetailsDialog(parentShell, Messages.ErrorDialog_title,
//                    Messages.ErrorDialog_UnexpectedError_message, e,
//                    "Failed to prepare XMind file to upload.", //$NON-NLS-1$
//                    System.currentTimeMillis()).open();
        }

        if (!prepared) {
            cancel();
            return;
        }

        UploadJob uploadJob = new UploadJob(info);
        uploadJob.setSystem(false);
        uploadJob.setUser(true);
        uploadJob.addJobChangeListener(this);
        uploadJob.schedule();
    }

    private boolean prepare() throws Exception {
        if (!signIn())
            return false;

        final Display display = parentShell.getDisplay();

        info.setProperty(Info.MULTISHEETS, multiSheets(sourceViewer));

        extractor = new MindMapExtractor(sourceViewer);
        workbook = extractor.extract();
        trimWorkbook();
        generatePreview(display);

        if (fullImage == null || origin == null)
            throw new RuntimeException(Messages.failedToGenerateThumbnail);

        final String mapTitle = getDefaultMapTitle();
        info.setProperty(Info.TITLE, mapTitle);
        info.setProperty(Info.DESCRIPTION, getDefaultMapDescription());
        info.setProperty(Info.FULL_IMAGE, fullImage);
        info.setInt(IMeta.ORIGIN_X, origin.x);
        info.setInt(IMeta.ORIGIN_Y, origin.y);
        info.setProperty(IMeta.BACKGROUND_COLOR, getBackgroundColor());
        info.setProperty(Info.TRIMMED, trimmed);

        UploaderDialog dialog = createUploadDialog();
        int ret = dialog.open();
        if (ret != UploaderDialog.OK)
            return false;

        int x = info.getInt(Info.X, 0);
        int y = info.getInt(Info.Y, 0);
        double scale = info.getDouble(Info.SCALE, 1.0d);
        // Legacy API Change: 300 * 180 -> 150 * 90
        scale /= 2;

        IMeta meta = workbook.getMeta();
        meta.setValue(Info.DESCRIPTION, info.getString(Info.DESCRIPTION));
        meta.setValue(Info.X, String.valueOf(x));
        meta.setValue(Info.Y, String.valueOf(y));
        meta.setValue(Info.SCALE, String.valueOf(scale));
        meta.setValue(IMeta.ORIGIN_X, String.valueOf(origin.x));
        meta.setValue(IMeta.ORIGIN_Y, String.valueOf(origin.y));
        meta.setValue(IMeta.BACKGROUND_COLOR,
                info.getString(IMeta.BACKGROUND_COLOR));
        meta.setValue(Info.PRIVACY,
                info.getString(Info.PRIVACY, Info.PRIVACY_PUBLIC));
        meta.setValue(Info.DOWNLOADABLE,
                info.getString(Info.DOWNLOADABLE, Info.DOWNLOADABLE_YES));
        meta.setValue(Info.LANGUAGE_CHANNEL,
                info.getString(Info.LANGUAGE_CHANNEL));

        if (file == null) {
            String tempFile = Core.getWorkspace()
                    .getTempFile(
                            "upload/" //$NON-NLS-1$
                                    + Core.getIdFactory().createId()
                                    + MindMapUI.FILE_EXT_XMIND);
            file = new File(tempFile);
        }

        String path = file.getAbsolutePath();
        workbook.saveTemp();
        workbook.save(path);
        Uploader.validateUploadFile(path);

        if (!file.exists() || !file.canRead())
            throw new FileNotFoundException(Messages.failedToGenerateUploadFile);

        info.setProperty(Info.FILE, file);
        info.setProperty(Info.WORKBOOK, workbook);
        return true;
    }

    private Boolean multiSheets(IMindMapViewer sourceViewer) {
        IWorkbook workbook = (IWorkbook) sourceViewer
                .getAdapter(IWorkbook.class);
        if (workbook.getSheets().size() > 1)
            return true;
        return false;
    }

    private String getBackgroundColor() {
        Layer layer = sourceViewer.getLayer(GEF.LAYER_BACKGROUND);
        if (layer != null) {
            Color color = layer.getBackgroundColor();
            if (color != null)
                return ColorUtils.toString(color);
        }
        return "#ffffff"; //$NON-NLS-1$
    }

    private boolean signIn() {
        IAccountInfo accountInfo = XMindNet.signIn();
        if (accountInfo != null) {
            info.setProperty(Info.USER_ID, accountInfo.getUser());
            info.setProperty(Info.TOKEN, accountInfo.getAuthToken());
            return true;
        }
        return false;
    }

    private UploaderDialog createUploadDialog() {
        UploaderDialog dialog = new UploaderDialog(parentShell);
        dialog.setInfo(info);
        return dialog;
    }

    private void generatePreview(Display display) {
        MindMapImageExporter exporter = new MindMapImageExporter(display);
//        exporter.setSourceViewer(sourceViewer);
        exporter.setSource(new MindMap(workbook.getPrimarySheet()), null, null);
        exporter.setTargetWorkbook(workbook);
        fullImage = exporter.createImage();
        exporter.export(fullImage);
        origin = exporter.calcRelativeOrigin();
    }

    private void cancel() {
        clearTemp();
    }

    private void clearTemp() {
        if (fullImage != null) {
            fullImage.dispose();
            fullImage = null;
        }

        if (extractor != null) {
            extractor.delete();
            extractor = null;
        }

        if (file != null) {
            file.delete();
            file = null;
        }

        workbook = null;
    }

    private String getDefaultMapTitle() {
        if (workbook != null) {
            return workbook.getPrimarySheet().getRootTopic().getTitleText();
        }
        return null;
    }

    private String getDefaultMapDescription() {
        if (workbook != null) {
            String desc = workbook.getMeta().getValue(IMeta.DESCRIPTION);
            if (desc == null || "".equals(desc)) { //$NON-NLS-1$
                ITopic rootTopic = workbook.getPrimarySheet().getRootTopic();
                List<ITopic> mainTopics = rootTopic
                        .getChildren(ITopic.ATTACHED);
                if (!mainTopics.isEmpty()) {
                    StringBuffer sb = new StringBuffer(mainTopics.size() * 15);
                    for (int i = 0; i < mainTopics.size(); i++) {
                        sb.append(mainTopics.get(i).getTitleText());
                        if (i < mainTopics.size() - 1) {
                            sb.append(" / "); //$NON-NLS-1$
                        }
                    }
                    desc = sb.toString();
                }
            }
            return desc;
        }
        return null;
    }

    @Override
    public void done(final IJobChangeEvent event) {
        clearTemp();

        final IStatus result = event.getResult();
        final UploadSession session = ((UploadJob) event.getJob()).getSession();
        runInUI(new Runnable() {
            public void run() {
                if (result.isOK()) {
                    promptCompletion(session);
                } else if (result.matches(IStatus.ERROR | IStatus.WARNING)) {
                    promptError(session.getStatus(), result);
                }
            }
        });
    }

    private void runInUI(final Runnable runnable) {
        Display display = parentShell.getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(runnable);
    }

    private void trimWorkbook() {
        trim();
    }

    private void trim() {
        for (ISheet sheet : workbook.getSheets()) {
            trim(sheet.getRootTopic());
        }
    }

    private void trim(ITopic topic) {
        String hyperlink = topic.getHyperlink();
        if (hyperlink != null) {
            if (HyperlinkUtils.isAttachmentURL(hyperlink)) {
                topic.setHyperlink(null);
                trimmed = true;
            }

            if (HyperlinkUtils.getProtocolName(hyperlink) != null
                    && HyperlinkUtils.getProtocolName(hyperlink).equals("file")) { //$NON-NLS-1$
                topic.setHyperlink(null);
                trimmed = true;
            }

            if (HyperlinkUtils.isInternalURL(hyperlink)) {
                if (workbook.findTopic(hyperlink.substring(hyperlink
                        .indexOf('#') + 1)) == null) {
                    topic.setHyperlink(null);
                    trimmed = true;
                }
            }
        }
        if (topic.getExtension("org.xmind.ui.audionotes") != null) { //$NON-NLS-1$
            topic.deleteExtension("org.xmind.ui.audionotes"); //$NON-NLS-1$
            trimmed = true;
        }
        for (ITopic c : topic.getAllChildren()) {
            trim(c);
        }
    }

    private void promptCompletion(UploadSession session) {
        final String permalink = session.getViewLink();
        IAction viewAction = new Action() {
            public void run() {
                showUploadedMap(permalink);
            }
        };
        viewAction.setText(Messages.UploadJob_OpenMap_message + " " //$NON-NLS-1$
                + Messages.UploadJob_View_text);
        new NotificationWindow(parentShell, Messages.UploadJob_OpenMap_title,
                viewAction, null, 0).open();

//        SimpleInfoPopupDialog dialog = new SimpleInfoPopupDialog(null, null,
//                Messages.UploadJob_OpenMap_message, 0, null, viewAction);
//        dialog.setDuration(30000);
//        dialog.setGroupId("org.xmind.notifications"); //$NON-NLS-1$
//        dialog.setCloseOnAction(true);
//        dialog.popUp();
    }

    private void showUploadedMap(String url) {
        if (url != null) {
            XMindNet.gotoURL(true, url);
            return;
        }

        IAccountInfo accountInfo = XMindNet.getAccountInfo();
        if (accountInfo == null)
            return;

        String userId = accountInfo.getUser();
        String token = accountInfo.getAuthToken();
        XMindNet.gotoURL(true, "http://www.xmind.net/xmind/account/%s/%s/", //$NON-NLS-1$
                userId, token);
    }

    private void promptError(int uploadStatus, IStatus error) {
        int httpStatus = error.getCode();
        String message = null;
        boolean tryAgainAllowed = true;
        if (uploadStatus == UploadSession.PREPARING) {
            if (httpStatus == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resignin();
                return;
            }
        } else if (uploadStatus == UploadSession.UPLOADING) {
            if (httpStatus == HttpURLConnection.HTTP_NOT_FOUND) {
                return;
            } else if (httpStatus == UploadSession.CODE_VERIFICATION_FAILURE) {
                message = Messages.ErrorDialog_Unauthorized_message;
                tryAgainAllowed = false;
            }
        }

        if (message == null)
            message = Messages.ErrorDialog_message;

        if (tryAgainAllowed) {
            if (MessageDialog.openQuestion(null, Messages.ErrorDialog_title,
                    message)) {
                retry();
            }
        } else {
            MessageDialog.openError(null, Messages.ErrorDialog_title, message);
        }
    }

    private void resignin() {
        XMindNet.signOut();
        XMindNet.signIn(new IAuthenticationListener() {
            public void postSignIn(IAccountInfo accountInfo) {
                retry();
            }

            public void postSignOut(IAccountInfo oldAccountInfo) {
            }
        }, false);
    }

    private void retry() {
        runInUI(new Runnable() {
            public void run() {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        new Uploader(parentShell, sourceViewer).upload();
                    }
                });
            }
        });
    }

    public static void validateUploadFile(String path)
            throws FileValidationException {
        Set<String> entries = new HashSet<String>();
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(path));
            try {
                ZipEntry e;
                String name;
                while ((e = zin.getNextEntry()) != null) {
                    name = e.getName();
                    entries.add(name);
                }
            } finally {
                zin.close();
            }
        } catch (Throwable e) {
            throw new FileValidationException("File Validation Failed: " //$NON-NLS-1$
                    + e.getLocalizedMessage(), e);
        }
        if (!entries.contains("Thumbnails/thumbnail.png")) //$NON-NLS-1$
            throw new FileValidationException(
                    "File Validation Failed: missing entry 'Thumbnails/thumbnail.png'"); //$NON-NLS-1$
    }

}