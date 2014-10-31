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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.sharing.ILocalSharedLibrary;
import org.xmind.core.sharing.ISharedMap;

public class DeleteSharedMapsHandler extends AbstractHandler implements IFilter {

    public DeleteSharedMapsHandler() {
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return null;

        final List<ISharedMap> maps = SharingUtils.getSharedMapsFrom(
                (IStructuredSelection) selection, this);
        if (maps.isEmpty())
            return null;

        if (maps.size() == 1) {
            if (!MessageDialog
                    .openConfirm(
                            Display.getCurrent().getActiveShell(),
                            SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                            NLS.bind(
                                    SharingMessages.ConfirmDeleteSingleSharedMap_dialogMessage,
                                    ((ISharedMap) maps.get(0))
                                            .getResourceName())))
                return null;
        } else {
            StringBuilder sb = new StringBuilder(maps.size() * 20);
            for (Object map : maps) {
                if (sb.length() > 0) {
                    sb.append(',');
                    sb.append(' ');
                }
                sb.append('\'');
                sb.append(((ISharedMap) map).getResourceName());
                sb.append('\'');
            }
            if (!MessageDialog
                    .openConfirm(
                            Display.getCurrent().getActiveShell(),
                            SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                            NLS.bind(
                                    SharingMessages.ConfirmDeleteMultipleSharedMaps_dialogMessage,
                                    sb.toString())))
                return null;
        }
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
                ILocalSharedLibrary library = LocalNetworkSharingUI
                        .getDefault().getSharingService().getLocalLibrary();
                for (ISharedMap map : maps) {
                    library.removeSharedMap(map);
                }
            }
        });

        return null;
    }

    public boolean select(Object toTest) {
        ISharedMap map = (ISharedMap) toTest;
        return map.getSharedLibrary().isLocal();
    }

}
