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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.xmind.core.internal.sharing.AbstractSharedMap;
import org.xmind.core.internal.sharing.LocalNetworkSharing;
import org.xmind.core.internal.sharing.RemoteSharedLibrary;
import org.xmind.core.sharing.IContactManager;
import org.xmind.core.sharing.ILocalSharedLibrary;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingListener;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.sharing.SharingConstants;
import org.xmind.core.sharing.SharingEvent;
import org.xmind.core.sharing.SharingEvent.Type;

class SharedMapsContentProvider implements ITreeContentProvider,
        ISharingListener {

    private Viewer viewer = null;

    public SharedMapsContentProvider() {
    }

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ISharingService)
            return getCategories((ISharingService) inputElement);
        return new Object[0];
    }

    private Object[] getCategories(ISharingService sharingService) {
        IPreferenceStore prefStore = LocalNetworkSharingUI.getDefault()
                .getPreferenceStore();
        String arrangeMode = prefStore
                .getString(SharingConstants.PREF_ARRANGE_MODE);

        List<Object> categories = new ArrayList<Object>();
        ILocalSharedLibrary localLibrary = sharingService.getLocalLibrary();

        if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(arrangeMode)
                || SharingConstants.ARRANGE_MODE_PEOPLE.equals(arrangeMode)) {
            localLibrary.sortMaps(AbstractSharedMap.MAP_COMPARATOR);
            categories.add(localLibrary);
            categories.addAll(sharingService.getRemoteLibraries());
            Collections.sort(categories, LIBRARY_COMPARATOR);
        } else {
            RemoteSharedLibrary bigLibrary = new RemoteSharedLibrary(null,
                    SharingMessages.SharedMapsContentProvider_remoteLibraryName);
            List<ISharedMap> maps = new ArrayList<ISharedMap>();
            for (IRemoteSharedLibrary remote : sharingService
                    .getRemoteLibraries()) {
                if (!sharingService.getContactManager().isContact(
                        remote.getContactID()))
                    continue;

                for (ISharedMap map : remote.getMaps()) {
                    maps.add(map);
                }
            }
            bigLibrary.addMaps(maps);
            if (SharingConstants.ARRANGE_MODE_NAME.equals(arrangeMode)) {
                localLibrary.sortMaps(AbstractSharedMap.MAP_COMPARATOR);
            } else if (SharingConstants.ARRANGE_MODE_TIME.equals(arrangeMode)) {
                localLibrary
                        .sortMaps(AbstractSharedMap.MAP_COMPARATOR_BY_MODIFIED_TIME);
                bigLibrary
                        .sortMaps(AbstractSharedMap.MAP_COMPARATOR_BY_MODIFIED_TIME);
            }
            categories.add(localLibrary);
            categories.add(bigLibrary);
        }
        return categories.toArray();
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
                || type == SharingEvent.Type.SERVICE_STATUS_CHANGED
                || type == SharingEvent.Type.CONTACT_ADDED) {
            if (type == SharingEvent.Type.SERVICE_STATUS_CHANGED) {

            }
            viewer.refresh();
        } else if (type == SharingEvent.Type.LIBRARY_NAME_CHANGED) {
            ((StructuredViewer) viewer).update(event.getLibrary(), null);
        } else if (type == SharingEvent.Type.SHARED_MAP_UPDATED) {
            Object[] updatedElements = new Object[] { event.getMap() };
            ((SharedMapLabelProvider) ((StructuredViewer) viewer)
                    .getLabelProvider()).invalidateImageCache(updatedElements);
        }
    }

    private static final Comparator<Object> LIBRARY_COMPARATOR = new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof ILocalSharedLibrary)
                return -1;

            if (o2 instanceof ILocalSharedLibrary)
                return 1;

            if (o1 instanceof IRemoteSharedLibrary
                    && o2 instanceof IRemoteSharedLibrary) {
                IRemoteSharedLibrary r1 = (IRemoteSharedLibrary) o1;
                IRemoteSharedLibrary r2 = (IRemoteSharedLibrary) o2;

                int code;

                code = compareConnect(r1, r2);
                if (code != 0)
                    return code;

                code = compareContactID(r1, r2);
                if (code != 0)
                    return code;

                return compareName(r1.getName(), r2.getName());
            }
            return 0;
        }

        private int compareConnect(IRemoteSharedLibrary r1,
                IRemoteSharedLibrary r2) {
            IContactManager manager = LocalNetworkSharing.getDefault()
                    .getSharingService().getContactManager();

            boolean connect1 = manager.isContact(r1.getContactID());
            boolean connect2 = manager.isContact(r2.getContactID());

            if (connect1 && connect2)
                return compareName(r1.getName(), r2.getName());
            if (connect1)
                return -1;
            if (connect2)
                return 1;
            return 0;
        }

        private int compareContactID(IRemoteSharedLibrary r1,
                IRemoteSharedLibrary r2) {
            String id1 = r1.getContactID();
            String id2 = r2.getContactID();

            if (id1 != null && !"".equals(id1) //$NON-NLS-1$
                    && id2 != null && !"".equals(id2)) //$NON-NLS-1$
                return compareName(r1.getName(), r2.getName());
            if (id1 != null && !"".equals(id1)) //$NON-NLS-1$
                return -1;
            if (id2 != null && !"".equals(id2)) //$NON-NLS-1$
                return 1;
            return 0;
        }

        private int compareName(String name1, String name2) {
            if (name1 != null && name2 != null)
                return name1.compareTo(name2);
            if (name1 != null)
                return -1;
            if (name2 != null)
                return 1;
            return 0;
        }

    };
}