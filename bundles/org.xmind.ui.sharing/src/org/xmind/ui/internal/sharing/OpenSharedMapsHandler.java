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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.sharing.ISharedMap;

public class OpenSharedMapsHandler extends AbstractHandler {

    public OpenSharedMapsHandler() {
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        Display display = Display.getCurrent();
        if (display == null || display.isDisposed())
            return null;

        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return null;

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return null;

        List<ISharedMap> maps = SharingUtils
                .getSharedMapsFrom((IStructuredSelection) selection);
        if (maps.isEmpty())
            return null;

        SharingUtils.openSharedMaps(page, maps);

        return null;
    }

}
