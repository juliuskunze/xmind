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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingListener;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.sharing.SharingEvent;
import org.xmind.core.sharing.SharingEvent.Type;

class SharedMapsContentProvider implements
        ITreeContentProvider, ISharingListener {

    private Viewer viewer = null;

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ISharingService) {
            ISharingService sharingService = (ISharingService) inputElement;
            List<Object> categories = new ArrayList<Object>();
            categories.add(sharingService.getLocalLibrary());
            categories.addAll(sharingService.getRemoteLibraries());
            return categories.toArray();
        }
        return new Object[0];
    }

    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ISharedLibrary) {
            return ((ISharedLibrary) parentElement).getMaps();
        }
        return new Object[0];
    }

    public boolean hasChildren(Object element) {
        if (element instanceof ISharedLibrary)
            return ((ISharedLibrary) element).hasMaps();
        return false;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;
        if (oldInput instanceof ISharingService) {
            ((ISharingService) oldInput).removeSharingListener(this);
        }
        if (newInput instanceof ISharingService) {
            ((ISharingService) newInput).addSharingListener(this);
        }
    }

    public Object getParent(Object element) {
        if (element instanceof ISharedMap) {
            return ((ISharedMap) element).getSharedLibrary();
        }
        return null;
    }

    public void handleSharingEvent(final SharingEvent event) {
        if (viewer == null || viewer.getControl() == null
                || viewer.getControl().isDisposed())
            return;

        viewer.getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (viewer == null || viewer.getControl() == null
                        || viewer.getControl().isDisposed())
                    return;
                handleSharingEventInUI(event);
            }
        });
    }

    private void handleSharingEventInUI(SharingEvent event) {
        Type type = event.getType();
        if (type == SharingEvent.Type.LIBRARY_ADDED
                || type == SharingEvent.Type.LIBRARY_REMOVED
                || type == SharingEvent.Type.SHARED_MAP_ADDED
                || type == SharingEvent.Type.SHARED_MAP_REMOVED
                || type == SharingEvent.Type.SERVICE_STATUS_CHANGED) {
            if (type == SharingEvent.Type.SERVICE_STATUS_CHANGED) {

            }
            viewer.refresh();
        } else if (type == SharingEvent.Type.LIBRARY_NAME_CHANGED) {
            ((StructuredViewer) viewer).update(event.getLibrary(), null);
        } else if (type == SharingEvent.Type.SHARED_MAP_UPDATED) {
            Object[] updatedElements = new Object[] { event.getMap() };
            ((SharedMapLabelProvider) ((StructuredViewer) viewer)
                    .getLabelProvider())
                    .invalidateImageCache(updatedElements);
        }
    }
}