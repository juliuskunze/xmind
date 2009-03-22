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
package org.xmind.ui.commands;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.xmind.core.ICloneData;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.WorkbookEditorInput;
import org.xmind.ui.mindmap.ITopicPart;

/**
 * @author karelun huang
 * 
 */
public class CreateSheetCommandBuilder extends CommandBuilder {

    private IViewer viewer;

    private ITopic topic;

    private IWorkbook workbook;

    public CreateSheetCommandBuilder(IGraphicalEditor editor, IViewer viewer,
            ICommandStack commandStack) {
        super(viewer, commandStack);
        this.viewer = viewer;
        init(editor);
    }

    private void init(IGraphicalEditor editor) {
        IPart focusedPart = viewer.getFocusedPart();
        if (focusedPart instanceof ITopicPart) {
            ITopicPart part = (ITopicPart) focusedPart;
            topic = part.getTopic();
        }
        if (editor != null) {
            workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);
            if (workbook == null) {
                IEditorInput input = editor.getEditorInput();
                if (input instanceof WorkbookEditorInput)
                    workbook = ((WorkbookEditorInput) input).getContents();
            }
        }
    }

    public void addCommand() {
        CreateSheetCommand command = new CreateSheetCommand(workbook, topic);
        add(command, true);

        ISheet sheet = (ISheet) command.getSource();
        if (sheet == null)
            return;
        ICloneData cloneData = command.getCloneData();

        sheet.setTitleText(NLS.bind(MindMapMessages.TitleText_Sheet, sheet
                .getParent().getSheets().size()));
        String hyper = topic.getHyperlink();
        ITopic rootTopic = sheet.getRootTopic();
        if (hyper == null) {
            String rootTopicId = "xmind:#" + rootTopic.getId(); //$NON-NLS-1$
            ModifyTopicHyperlinkCommand command2 = new ModifyTopicHyperlinkCommand(
                    topic, rootTopicId);
            add(command2, false);

            String newRootTopicId = "xmind:#" + topic.getId(); //$NON-NLS-1$
            ModifyTopicHyperlinkCommand command3 = new ModifyTopicHyperlinkCommand(
                    rootTopic, newRootTopicId);
            add(command3, false);
        }
        String structure = newStructure(topic);
        if (structure != null)
            rootTopic.setStructureClass(structure);

        ISheet ownedSheet = topic.getOwnedSheet();
        Set<IRelationship> relationships = ownedSheet.getRelationships();
        if (relationships.size() > 0)
            addRelations(relationships, sheet, cloneData);
    }

    /**
     * @param relationships
     * @param sheet
     */
    private void addRelations(Set<IRelationship> relationships, ISheet sheet,
            ICloneData cloneData) {

        Iterator<IRelationship> itera = relationships.iterator();
        while (itera.hasNext()) {
            IRelationship relationship = itera.next();
            String end1Id = relationship.getEnd1Id();
            String end2Id = relationship.getEnd2Id();

            String id1 = (String) cloneData.get(end1Id);
            String id2 = (String) cloneData.get(end2Id);
            if (id1 == null || id2 == null)
                continue;
            ITopic topic1 = workbook.findTopic(id1);
            ITopic topic2 = workbook.findTopic(id2);

            createNewRelationship(sheet, relationship, topic1, topic2);
        }
    }

    /**
     * @param sheet
     * @param topic3
     * @param topic4
     */
    private void createNewRelationship(ISheet sheet,
            IRelationship relationship, ITopic topic1, ITopic topic2) {

        if (topic1 == null || topic2 == null)
            return;
        IRelationship newRelationship = workbook.createRelationship(topic1,
                topic2);
        decorateRelationship(relationship, newRelationship);
        sheet.addRelationship(newRelationship);
    }

    /**
     * @param relationship
     * @param newRelationship
     */
    private void decorateRelationship(IRelationship oldRelationship,
            IRelationship newRelationship) {
        IControlPoint oldPoint0 = oldRelationship.getControlPoint(0);
        IControlPoint newPoint0 = newRelationship.getControlPoint(0);
        newPoint0.setPolarAmount(oldPoint0.getPolarAmount());
        newPoint0.setPolarAngle(oldPoint0.getPolarAngle());
        newPoint0.setPosition(oldPoint0.getPosition());

        IControlPoint oldPoint1 = oldRelationship.getControlPoint(1);
        IControlPoint newPoint1 = newRelationship.getControlPoint(1);
        newPoint1.setPolarAmount(oldPoint1.getPolarAmount());
        newPoint1.setPolarAngle(oldPoint1.getPolarAngle());
        newPoint1.setPosition(oldPoint1.getPosition());

        newRelationship.setStyleId(oldRelationship.getStyleId());
        newRelationship.setTitleText(oldRelationship.getTitleText());
    }

    private String newStructure(ITopic topic) {
        String structure = topic.getStructureClass();
        if (structure != null) {
            if (structure.contains("floating.")) { //$NON-NLS-1$
                int index = structure.indexOf("floating."); //$NON-NLS-1$
                int lastIndex = structure.lastIndexOf('.');
                structure = structure.substring(0, index)
                        + structure.substring(lastIndex + 1);
            }
        }
        return structure;
    }
}
