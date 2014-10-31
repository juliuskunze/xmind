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

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.sharing.ISharingService;

public class PasteAsSharedMapsHandler extends AbstractHandler {

    public PasteAsSharedMapsHandler() {
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final ISharingService sharingService = LocalNetworkSharingUI
                .getDefault().getSharingService();
        if (sharingService == null)
            return null;

        Display display = Display.getCurrent();
        if (display == null || display.isDisposed())
            return null;

        Clipboard clipboard = new Clipboard(display);
        try {
            String[] filePaths = (String[]) clipboard.getContents(FileTransfer
                    .getInstance());
            if (filePaths == null || filePaths.length == 0)
                return null;

//            BusyIndicator.showWhile(display, new Runnable() {
//                public void run() {
//                    ILocalSharedLibrary library = sharingService
//                            .getLocalLibrary();
//                    for (int i = 0; i < filePaths.length; i++) {
//                        if (filePaths[i].endsWith(MindMapUI.FILE_EXT_XMIND)) {
//                            library.addSharedMap(new File(filePaths[i]));
//                        }
//                    }
//                }
//            });

            final File[] files = new File[filePaths.length];
            for (int i = 0; i < filePaths.length; i++) {
                files[i] = new File(filePaths[i]);
            }

            SharingUtils.addSharedMaps(Display.getCurrent().getActiveShell(),
                    sharingService, files);
        } finally {
            clipboard.dispose();
        }
        return null;
    }
}
