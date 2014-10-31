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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.sharing.ILocalSharedMap;

public class CopySharedMapsHandler extends AbstractHandler {

    public CopySharedMapsHandler() {
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        Display display = Display.getCurrent();
        if (display == null || display.isDisposed())
            return null;

        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return null;

        List<String> files = new ArrayList<String>();
        for (Object map : ((IStructuredSelection) selection).toList()) {
            if (map instanceof ILocalSharedMap) {
                String path = ((ILocalSharedMap) map).getResourcePath();
                if (path != null && !"".equals(path)) //$NON-NLS-1$
                    files.add(path);
            }
        }
        if (files.isEmpty())
            return null;

        Clipboard clipboard = new Clipboard(display);
        try {
            clipboard.setContents(
                    new Object[] { files.toArray(new String[files.size()]) },
                    new Transfer[] { FileTransfer.getInstance() });
        } finally {
            clipboard.dispose();
        }
        return null;
    }

}
