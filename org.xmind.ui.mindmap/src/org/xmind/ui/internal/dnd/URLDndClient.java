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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.CreateTopicCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyTopicHyperlinkCommand;
import org.xmind.ui.internal.mindmap.ImageDownloadCenter;
import org.xmind.ui.mindmap.IMindMapDndClient;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.MindMapUtils;

public class URLDndClient implements IDndClient, IMindMapDndClient {

    private URLTransfer transfer = URLTransfer.getInstance();

    public Object getData(Transfer transfer, TransferData data) {
        if (transfer == this.transfer)
            return this.transfer.nativeToJava(data);
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
        String url = (String) transferData;
        IWorkbook workbook = (IWorkbook) viewer.getAdapter(IWorkbook.class);
        if (workbook != null) {
            ITopic topic = workbook.createTopic();
            topic.setTitleText(url);
            topic.setHyperlink(url);
            return new Object[] { topic };
        }
        return null;
    }

    public boolean handleRequest(Request request, DndData dndData) {
        String url = (String) dndData.parsedData;
        IPart target = request.getPrimaryTarget();
        IPart parent = (IPart) request.getParameter(GEF.PARAM_PARENT);
        ITopicPart targetTopic = MindMapUtils.findTopicPart(target);
        ITopicPart parentTopic = MindMapUtils.findTopicPart(parent);
        if (targetTopic != null && targetTopic.equals(parentTopic)) {
            if (MindMapUI.getProtocolManager().isHyperlinkModifiable(
                    targetTopic.getTopic(), url))
                return handleAddURLToTopic(request.getTargetCommandStack(),
                        targetTopic.getTopic(), url);
        } else if (isImageURL(url)) {
            return handleAddImageTopic(request, parentTopic, url);
        }
        return false;
    }

    /**
     * @param request
     * @param topic
     * @param url
     * @return
     */
    private boolean handleAddImageTopic(Request request, ITopicPart parent,
            String url) {
        ISheet sheet = (ISheet) request.getTargetViewer().getAdapter(
                ISheet.class);
        if (sheet == null)
            return false;

        CreateTopicCommand create = new CreateTopicCommand(sheet
                .getOwnedWorkbook());
        List<Command> commands = new ArrayList<Command>();
        commands.add(create);
        ITopic parentTopic;
        boolean detached = parent == null;
        if (detached) {
            parentTopic = sheet.getRootTopic();
        } else {
            parentTopic = parent.getTopic();
        }
        int index = request.getIntParameter(GEF.PARAM_INDEX, -1);
        AddTopicCommand add = new AddTopicCommand(create, parentTopic, index,
                detached ? ITopic.DETACHED : ITopic.ATTACHED);
        commands.add(add);
        if (detached) {
            Point pos = (Point) request.getParameter(GEF.PARAM_POSITION);
            commands.add(new ModifyPositionCommand(create, MindMapUtils
                    .toModelPosition(pos)));
        }

        Command cmd = new CompoundCommand(CommandMessages.Command_CreateTopic,
                commands);
        request.getTargetCommandStack().execute(cmd);
        ITopic targetTopic = (ITopic) create.getSource();
        request.getTargetViewer().setSelection(
                new StructuredSelection(targetTopic), true);
        addImageToTopic(targetTopic, url);
        return true;
    }

    private boolean handleAddURLToTopic(ICommandStack commandStack,
            ITopic topic, String url) {
        if (isImageURL(url)) {
            addImageToTopic(topic, url);
        } else {
            changeTopicHyperlink(commandStack, topic, url);
        }
        return true;
    }

    private void changeTopicHyperlink(ICommandStack commandStack, ITopic topic,
            String url) {
        Command command = new ModifyTopicHyperlinkCommand(topic, url);
        command.setLabel(CommandMessages.Command_ModifyTopicHyperlink);
        commandStack.execute(command);
    }

    private boolean isImageURL(String url) {
        return url != null
                && ImageFormat.findByExtension(FileUtils.getExtension(url),
                        null) != null;
    }

    private void addImageToTopic(ITopic topic, String url) {
        ImageDownloadCenter.getInstance().startDownload(topic, url);
//        ModifyImageSourceCommand command = new ModifyImageSourceCommand(topic,
//                url);
//        command.setLabel(CommandMessages.Command_InsertImage);
//        commandStack.execute(command);
    }

}