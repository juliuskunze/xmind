package org.xmind.ui.internal.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.IImage;
import org.xmind.core.IManifest;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.IViewer;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.ui.internal.AttachmentImageDescriptor;

public class ImageDndClient implements IDndClient {

    private ImageTransfer transfer = ImageTransfer.getInstance();

    public Object getData(Transfer transfer, TransferData data) {
        if (transfer == this.transfer) {
            return this.transfer.nativeToJava(data);
        }
        return null;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public Object toTransferData(Object[] viewerElements, IViewer viewer) {
        if (viewerElements != null && viewerElements.length > 0) {
            for (Object element : viewerElements) {
                if (element instanceof IImage) {
                    IImage image = (IImage) element;
                    String source = image.getSource();
                    if (source != null) {
                        if (HyperlinkUtils.isAttachmentURL(source)) {
                            String path = HyperlinkUtils
                                    .toAttachmentPath(source);
                            ImageDescriptor imageDescriptor = AttachmentImageDescriptor
                                    .createFromEntryPath(image
                                            .getOwnedWorkbook(), path);
                            ImageData imageData = imageDescriptor
                                    .getImageData();
                            return imageData;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Object[] toViewerElements(Object transferData, IViewer viewer,
            Object target) {
        if (transferData instanceof ImageData) {
            IWorkbook workbook = (IWorkbook) viewer.getAdapter(IWorkbook.class);
            if (workbook != null) {
                ImageData imageData = (ImageData) transferData;
                ImageLoader saver = new ImageLoader();
                saver.data = new ImageData[] { imageData };
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                saver.save(os, SWT.IMAGE_PNG);
                IManifest manifest = workbook.getManifest();
                try {
                    IFileEntry entry = manifest.createAttachmentFromStream(
                            new ByteArrayInputStream(os.toByteArray()),
                            "temp.png", Core.MEDIA_TYPE_IMAGE_PNG); //$NON-NLS-1$
                    ITopic topic = workbook.createTopic();
                    topic.getImage().setSource(
                            HyperlinkUtils.toAttachmentURL(entry.getPath()));
                    return new Object[] { topic.getImage() };
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
