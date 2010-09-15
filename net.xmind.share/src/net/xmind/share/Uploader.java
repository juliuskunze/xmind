/*
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
import java.io.IOException;
import java.io.InputStream;

import net.xmind.share.dialog.UploaderDialog;
import net.xmind.share.jobs.UploadJob;
import net.xmind.signin.IAccountInfo;
import net.xmind.signin.XMindNet;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.IMeta;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapExtractor;
import org.xmind.ui.mindmap.MindMapPreviewBuilder;
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

    public Uploader(Shell parentShell, IMindMapViewer sourceViewer) {
        this.parentShell = parentShell;
        this.sourceViewer = sourceViewer;
    }

    public void upload() {
        info = new Info();
        if (!signIn()) {
            cancel();
            return;
        }

        extractor = new MindMapExtractor(sourceViewer);
        workbook = extractor.extract();

        trimAttachments();
        generatePreview();

        if (fullImage == null || origin == null) {
            cancel();
            MessageDialog.openError(parentShell,
                    Messages.UploaderDialog_windowTitle,
                    Messages.failedToGenerateThumbnail);
            return;
        }

        final String mapTitle = getDefaultMapTitle();
        info.setProperty(Info.TITLE, mapTitle);
        info.setProperty(Info.DESCRIPTION, getDefaultMapDescription());
        info.setProperty(Info.FULL_IMAGE, fullImage);
        info.setInt(Info.ORIGIN_X, origin.x);
        info.setInt(Info.ORIGIN_Y, origin.y);
        info.setProperty(Info.BACKGROUND_COLOR, getBackgroundColor());

        UploaderDialog dialog = createUploadDialog();
        int ret = dialog.open();
        if (ret != UploaderDialog.OK) {
            cancel();
            return;
        }

        int x = info.getInt(Info.X, 0);
        int y = info.getInt(Info.Y, 0);
        double scale = info.getDouble(Info.SCALE, 1.0d);
        // 300 * 180 -> 150 * 90
        scale /= 2;

        IMeta meta = workbook.getMeta();
        meta.setValue(Info.DESCRIPTION, info.getString(Info.DESCRIPTION));
        meta.setValue(Info.X, String.valueOf(x));
        meta.setValue(Info.Y, String.valueOf(y));
        meta.setValue(Info.SCALE, String.valueOf(scale));
        meta.setValue(Info.ORIGIN_X, String.valueOf(origin.x));
        meta.setValue(Info.ORIGIN_Y, String.valueOf(origin.y));
        meta.setValue(Info.BACKGROUND_COLOR, info
                .getString(Info.BACKGROUND_COLOR));
//        if (info.hasProperty(Info.ALLOW_DOWNLOAD)) {
//            meta.setValue(Info.ALLOW_DOWNLOAD, info.getString(
//                    Info.ALLOW_DOWNLOAD, Info.Public));
//        } else {
//            meta.setValue(Info.ALLOW_DOWNLOAD, Info.Public);
//        }
        meta.setValue(Info.PRIVACY, info.getString(Info.PRIVACY,
                Info.PRIVACY_PUBLIC));
        meta.setValue(Info.DOWNLOADABLE, info.getString(Info.DOWNLOADABLE,
                Info.DOWNLOADABLE_YES));

        if (file == null) {
            String tempFile = Core.getWorkspace()
                    .getTempFile(
                            "upload/" //$NON-NLS-1$
                                    + Core.getIdFactory().createId()
                                    + MindMapUI.FILE_EXT_XMIND);

            file = new File(tempFile);
        }

        SafeRunner.run(new SafeRunnable(Messages.failedToGenerateUploadFile) {
            public void run() throws Exception {
                String path = file.getAbsolutePath();
                workbook.save(path);
            }
        });

        if (!file.exists() || !file.canRead()) {
            // some error may have occurred and been catched
            // by the above SafeRunner, so we simply return here
            //cancel();
            return;
        }

        if (file != null) {
            info.setProperty(Info.FILE, file);
            info.setProperty(Info.WORKBOOK, workbook);
            UploadJob uploadJob = new UploadJob(info);
            uploadJob.addJobChangeListener(this);
            uploadJob.schedule();
        }

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

    private void generatePreview() {
        final MindMapPreviewBuilder previewBuilder = new MindMapPreviewBuilder(
                workbook);
        final Display display = parentShell.getDisplay();
        BusyIndicator.showWhile(display, new Runnable() {
            public void run() {
                SafeRunner.run(new SafeRunnable(
                        Messages.failedToGenerateThumbnail) {
                    public void run() throws Exception {
                        previewBuilder.save(parentShell);
                        origin = previewBuilder.getOrigin();
                        fullImage = getPreviewImage(workbook, display);
                    }
                });
            }
        });
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
        if (workbook != null)
            return workbook.getMeta().getValue(IMeta.DESCRIPTION);
        return null;
    }

    private Image getPreviewImage(IWorkbook workbook, Display display)
            throws Exception {
        IFileEntry entry = workbook.getManifest().getFileEntry(
                MindMapPreviewBuilder.PATH_THUMBNAIL);
        if (entry != null) {
            InputStream in = entry.getInputStream();
            if (in != null) {
                try {
                    return new Image(display, in);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void done(final IJobChangeEvent event) {
        clearTemp();
    }

    private void trimAttachments() {
        for (ISheet sheet : workbook.getSheets()) {
            trimAttachments(sheet.getRootTopic());
        }
    }

    private void trimAttachments(ITopic topic) {
        String hyperlink = topic.getHyperlink();
        if (hyperlink != null) {
            if (HyperlinkUtils.isAttachmentURL(hyperlink)) {
                topic.setHyperlink(null);
            }
        }
    }

//    public void onError() {
//        clearTemp();
//    }
//
//    public void onSuccess() {
//        openMyMaps();
//        clearTemp();
//    }
//
//    public void onCancle() {
//        clearTemp();
//    }

//    private void openMyMaps() {
//        // TODO open 'My Maps' page
//    }

}