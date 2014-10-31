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
package org.xmind.ui.internal.sharing;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharedLibrariesView extends ViewPart {

    private SharedLibrariesViewer viewer;

    private MenuManager contextMenu;

    public SharedLibrariesView() {
    }

    public void createPartControl(Composite parent) {
        contextMenu = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        parent.setMenu(contextMenu.createContextMenu(parent));

        viewer = new SharedLibrariesViewer();
        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                handleOpenSelectedMap(event.getSelection());
            }
        });
        viewer.createControl(parent, SWT.NONE);
        viewer.setInput(LocalNetworkSharingUI.getDefault().getSharingService());

        getSite().registerContextMenu(contextMenu, viewer);

        getSite().setSelectionProvider(viewer);

    }

    public void setFocus() {
        viewer.setFocus();
    }

    private void handleOpenSelectedMap(ISelection selection) {
        if (!(selection instanceof IStructuredSelection))
            return;

        final Object[] elements = ((IStructuredSelection) selection).toArray();
        SharingUtils.openSharedMaps(getSite().getPage(), elements);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(SharedLibrariesViewer.class))
            return viewer;
        return super.getAdapter(adapter);
    }

    public void dispose() {
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }
        if (viewer != null) {
            if (viewer.getControl() != null) {
                viewer.getControl().dispose();
            }
            viewer = null;
        }
        super.dispose();
    }

}
