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
package org.xmind.ui.internal.editpolicies;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.ICloneData;
import org.xmind.core.IImage;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddMarkerCommand;
import org.xmind.ui.commands.AddRelationshipCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.commands.ModifyImageAlignmentCommand;
import org.xmind.ui.commands.ModifyImageSizeCommand;
import org.xmind.ui.commands.ModifyImageSourceCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.mindmap.IMindMapDndClient;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class DropTargetPolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_DROP.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_DROP.equals(type)) {
            drop(request);
        }
    }

    private void drop(Request request) {
        DndData dndData = (DndData) request
                .getParameter(MindMapUI.PARAM_DND_DATA);
        if (dndData == null)
            return;

        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        IDndSupport dndSupport = viewer.getDndSupport();
        if (dndSupport == null)
            return;

        ISheet targetSheet = (ISheet) viewer.getAdapter(ISheet.class);
        IWorkbook targetWorkbook = targetSheet == null ? null : targetSheet
                .getParent();
        IPart targetParent = (IPart) request.getParameter(GEF.PARAM_PARENT);
        Object targetModel = targetParent == null ? viewer
                .getAdapter(ITopic.class) : MindMapUtils
                .getRealModel(targetParent);

        IDndClient client = dndSupport.getDndClient(dndData.clientId);
        if (client != null) {
            if (client instanceof IMindMapDndClient) {
                if (((IMindMapDndClient) client)
                        .handleRequest(request, dndData))
                    return;
            }

            if (targetWorkbook != null) {
                Object[] elements = client.toViewerElements(dndData.parsedData,
                        viewer, targetModel);
                if (elements != null && elements.length > 0) {
                    copy(request, elements, viewer, targetModel, targetWorkbook);
                    return;
                }
            }
        }
    }

    protected void copy(Request request, Object[] elements, IViewer viewer,
            Object target, IWorkbook targetWorkbook) {
        ICloneData result = targetWorkbook.clone(Arrays.asList(elements));
        if (!result.hasCloned())
            return;

        Collection<Object> cloneds = result.getCloneds();
        ITopic parentTopic;
        if (target instanceof ITopic) {
            parentTopic = (ITopic) target;
        } else {
            parentTopic = (ITopic) viewer.getAdapter(ITopic.class);
        }
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        int index = request.getIntParameter(GEF.PARAM_INDEX, -1);
        boolean floating = request.getParameter(GEF.PARAM_PARENT) == null;
        Point position = (Point) request.getParameter(GEF.PARAM_POSITION);

        PropertyCommandBuilder builder = new PropertyCommandBuilder(request);
        if (!builder.canStart())
            return;

        builder.start();
        builder.setLabel(CommandMessages.Command_AddResources);

        for (Object cloned : cloneds) {
            if (cloned instanceof ITopic) {
                if (parentTopic != null) {
                    ITopic clonedTopic = (ITopic) cloned;
                    if (floating && position != null) {
                        builder.add(new AddTopicCommand(clonedTopic,
                                parentTopic, -1, ITopic.DETACHED), true);
                        builder.add(new ModifyPositionCommand(clonedTopic,
                                new org.xmind.core.util.Point(position.x,
                                        position.y)), true);
                    } else {
                        builder.add(new AddTopicCommand(clonedTopic,
                                parentTopic, index, ITopic.ATTACHED), true);
                        builder.add(
                                new ModifyPositionCommand(clonedTopic, null),
                                true);
                        if (index >= 0)
                            index++;
                    }
                }
            } else if (cloned instanceof IRelationship) {
                if (sheet != null) {
                    IRelationship clonedRelationship = (IRelationship) cloned;
                    builder.add(new AddRelationshipCommand(clonedRelationship,
                            sheet), true);
                }
            } else if (cloned instanceof IMarkerRef
                    || cloned instanceof IMarker) {
                if (parentTopic != null) {
                    IMarker marker = (cloned instanceof IMarker) ? (IMarker) cloned
                            : ((IMarkerRef) cloned).getMarker();
                    if (marker != null) {
                        IMarkerGroup group = marker.getParent();
                        if (group.isSingleton()) {
                            for (IMarker m : group.getMarkers()) {
                                if (parentTopic.hasMarker(m.getId())) {
                                    builder.add(new DeleteMarkerCommand(
                                            parentTopic, m.getId()), false);
                                }
                            }
                        }
                        String markerId = (cloned instanceof IMarker) ? ((IMarker) cloned)
                                .getId() : ((IMarkerRef) cloned).getMarkerId();
                        AddMarkerCommand addMarker = new AddMarkerCommand(
                                parentTopic, markerId);
                        builder.add(addMarker, true);
                    }
                }
            } else if (cloned instanceof IImage) {
                IImage image = (IImage) cloned;
                if (parentTopic != null) {
                    builder.add(
                            new ModifyImageSourceCommand(parentTopic, image
                                    .getSource()), true);
                    builder.add(
                            new ModifyImageSizeCommand(parentTopic, image
                                    .getWidth(), image.getHeight()), true);
                    builder.add(new ModifyImageAlignmentCommand(parentTopic,
                            image.getAlignment()), true);
                }
            }
            builder.addSource(cloned, true);
        }

        builder.end();
        select(builder.getCommand().getSources(), viewer);
    }

}