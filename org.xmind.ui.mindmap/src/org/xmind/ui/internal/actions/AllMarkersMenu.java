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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class AllMarkersMenu extends MenuManager {

    private class MarkerAction extends Action {

        private IMarker marker;

        public MarkerAction(IMarker marker) {
            this.marker = marker;
            setId("#" + marker.getId()); //$NON-NLS-1$
            setText(marker.getName());
            setImageDescriptor(MarkerImageDescriptor.createFromMarker(marker));
        }

        public void run() {
            if (page == null)
                return;

            EditDomain domain = page.getEditDomain();
            if (domain == null)
                return;

            domain.handleRequest(new Request(MindMapUI.REQ_ADD_MARKER)
                    .setViewer(page.getViewer()).setParameter(
                            MindMapUI.PARAM_MARKER_ID, marker.getId()));
        }
    }

    private IGraphicalEditorPage page;

    public AllMarkersMenu() {
        super(MindMapMessages.Markers_text, ActionConstants.ALL_MARKERS_MENU);
        setRemoveAllWhenShown(true);
        addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillMenu(manager);
            }
        });
    }

    public void setActivePage(IGraphicalEditorPage page) {
        this.page = page;
    }

    private void fillMenu(IMenuManager menu) {
        fillMarkerSheet(MindMapUI.getResourceManager().getSystemMarkerSheet(),
                menu);
        //fillMarkerSheet(MindMapUI.getResourceManager().getUserMarkerSheet());
    }

    private void fillMarkerSheet(IMarkerSheet sheet, IMenuManager menu) {
        for (final IMarkerGroup group : sheet.getMarkerGroups()) {
            MenuManager groupMenu = new MenuManager(group.getName(), "#" //$NON-NLS-1$
                    + group.getId());
            fillGroup(group, groupMenu);
            menu.add(groupMenu);
        }
    }

    private void fillGroup(IMarkerGroup group, IMenuManager menu) {
        for (IMarker marker : group.getMarkers()) {
            menu.add(new MarkerAction(marker));
        }
    }
}