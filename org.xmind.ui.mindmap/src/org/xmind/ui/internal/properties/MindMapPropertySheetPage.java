/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.properties.GraphicalPropertySheetPage;
import org.xmind.ui.mindmap.ICategoryManager;
import org.xmind.ui.mindmap.ICategoryAnalyzation;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapPropertySheetPage extends GraphicalPropertySheetPage {

    private static PropertySectionContributorManager manager = PropertySectionContributorManager
            .getInstance();

    public MindMapPropertySheetPage(IGraphicalEditor editor) {
        super(editor);
    }

    protected void selectionChanged(ISelection selection) {
        if (getControl() != null && !getControl().isDisposed())
            getControl().setRedraw(false);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            List<String> newVisibleSectionIds = manager
                    .getApplicableSectionIds(ss.toArray());
            List<String> oldVisibleSectionIds = getVisibleSectionIds();
            if (!equalsList(oldVisibleSectionIds, newVisibleSectionIds)) {
                List<String> oldSectionIds = getSectionIds();
                List<String> toAdd = new ArrayList<String>(newVisibleSectionIds);
                toAdd.removeAll(oldSectionIds);
                for (String id : toAdd) {
                    addSection(id, newVisibleSectionIds, oldSectionIds);
                    oldSectionIds = getSectionIds();
                }

                List<String> toHide = new ArrayList<String>(oldSectionIds);
                toHide.removeAll(newVisibleSectionIds);
                for (String id : oldSectionIds) {
                    setSectionVisible(id, !toHide.contains(id));
                }

                reflow();
            }
            setTitle(calcTitle(ss.toArray()));
        } else {
            setTitle(null);
        }
        if (getControl() != null && !getControl().isDisposed())
            getControl().setRedraw(true);
    }

    private void addSection(String id, List<String> newVisibleSectionIds,
            List<String> oldSectionIds) {
        addSection(id, manager.createSection(id));
        String aboveId = findAboveId(id, oldSectionIds,
                newVisibleSectionIds);
        moveSectionAfter(id, aboveId);
    }

    private String findAboveId(String id, List<String> oldSectionIds,
            List<String> newSectionIds) {
        int index = newSectionIds.indexOf(id);
        for (int i = index - 1; i >= 0; i--) {
            String aboveId = newSectionIds.get(i);
            if (oldSectionIds.contains(aboveId))
                return aboveId;
        }
        return null;
    }

    private static boolean equalsList(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size())
            return false;
        for (int i = 0; i < list1.size(); i++) {
            String s1 = list1.get(i);
            String s2 = list2.get(i);
            if (!s1.equals(s2))
                return false;
        }
        return true;
    }

    private String calcTitle(Object[] objects) {
        if (objects == null || objects.length == 0)
            return null;
        ICategoryManager typeManager = MindMapUI.getCategoryManager();
        ICategoryAnalyzation result = typeManager.analyze(objects);
        return typeManager.getCategoryName(result.getMainCategory());
    }

}