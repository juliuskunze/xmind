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
package org.xmind.ui.internal.dnd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyImageSizeCommand;
import org.xmind.ui.commands.ModifyImageSourceCommand;
import org.xmind.ui.mindmap.IMindMapDndClient;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.Logger;

public class FileDndClient implements IDndClient, IMindMapDndClient {

    private FileTransfer transfer = FileTransfer.getInstance();

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
        return null;
    }

    public Object[] toViewerElements(Object transferData, IViewer viewer,
            Object target) {
        if (transferData instanceof String[]) {
            String[] paths = (String[]) transferData;
            IWorkbook workbook = (IWorkbook) viewer.getAdapter(IWorkbook.class);
            if (workbook != null) {
                return buildTopics(workbook, paths);
            }
        }
        return null;
    }

    private Object[] buildTopics(IWorkbook wb, String[] paths) {
        IManifest manifest = wb.getManifest();
        List<ITopic> topics = new ArrayList<ITopic>(paths.length);
        for (String path : paths) {
            ITopic topic = buildTopic(wb, manifest, path);
            if (topic != null) {
                topics.add(topic);
            }
        }
        return topics.toArray();
    }

    private ITopic buildTopic(IWorkbook wb, IManifest manifest, String path) {
        IFileEntry entry;
        try {
            entry = manifest.createAttachmentFromFilePath(path);
        } catch (IOException e) {
            Logger.log(e, "Error occurred when transfering file: " + path); //$NON-NLS-1$
            return null;
        }
        ITopic topic = wb.createTopic();

        if (isImagePath(path)) {
            Dimension size = getImageSize(path);
            if (size != null) {
                topic.getImage().setSource(
                        HyperlinkUtils.toAttachmentURL(entry.getPath()));
                topic.getImage().setSize(size.width, size.height);
            } else {
                topic.setTitleText(new File(path).getName());
                topic.setHyperlink(HyperlinkUtils.toAttachmentURL(entry
                        .getPath()));
            }
        } else {
            topic.setTitleText(new File(path).getName());
            topic.setHyperlink(HyperlinkUtils.toAttachmentURL(entry.getPath()));
        }
        return topic;
    }

    public boolean handleRequest(Request request, DndData dndData) {
        if (dndData.parsedData instanceof String[]) {
            String[] paths = (String[]) dndData.parsedData;
            if (isSingleImage(paths)) {
                String path = paths[0];
                IPart target = request.getPrimaryTarget();
                if (target != null
                        && target
                                .equals(request.getParameter(GEF.PARAM_PARENT))) {
                    if (target instanceof ITopicPart) {
                        ITopic topic = ((ITopicPart) target).getTopic();
                        IFileEntry entry = createAttachmentEntry(topic, path);
                        if (entry != null) {
                            addImage(request, topic, entry, path);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void addImage(Request request, ITopic topic, IFileEntry entry,
            String path) {
        Command command = new ModifyImageSourceCommand(topic,
                HyperlinkUtils.toAttachmentURL(entry.getPath()));
        Dimension size = getImageSize(path);
        if (size != null) {
            ModifyImageSizeCommand modifySize = new ModifyImageSizeCommand(
                    topic, size.width, size.height);
            command = new CompoundCommand(command, modifySize);
        }
        command.setLabel(CommandMessages.Command_InsertImage);
        request.getTargetCommandStack().execute(command);
    }

    private IFileEntry createAttachmentEntry(ITopic topic, String path) {
        try {
            return topic.getOwnedWorkbook().getManifest()
                    .createAttachmentFromFilePath(path);
        } catch (IOException e) {
            Logger.log(e, "Error occurred when transfering file: " + path); //$NON-NLS-1$
        }
        return null;
    }

    private boolean isSingleImage(String[] paths) {
        return paths.length == 1 && isImagePath(paths[0]);
    }

    protected boolean isImagePath(String path) {
        String ext = FileUtils.getExtension(path);
        return ImageFormat.findByExtension(ext, null) != null;
    }

    private Dimension getImageSize(String path) {
        try {
            Image tempImage = new Image(Display.getCurrent(), path);
            Rectangle imageBounds = tempImage.getBounds();
            tempImage.dispose();
            return Geometry.getScaledConstrainedSize(imageBounds.width,
                    imageBounds.height, MindMapUI.IMAGE_INIT_WIDTH,
                    MindMapUI.IMAGE_INIT_HEIGHT);
        } catch (Throwable e) {
        }
        return null;
    }

}