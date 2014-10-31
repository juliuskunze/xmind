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
package org.xmind.core.internal.sharing;

import static org.xmind.core.sharing.SharingConstants.CODE_MISSING_ARGUMENT;
import static org.xmind.core.sharing.SharingConstants.CODE_NOT_FOUND;
import static org.xmind.core.sharing.SharingConstants.PLUGIN_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_CONTENT;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;
import org.xmind.core.command.ReturnValue;
import org.xmind.core.command.binary.BinaryStore;
import org.xmind.core.command.binary.IBinaryEntry;
import org.xmind.core.command.binary.IBinaryStore;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.SharingConstants;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class FileContentCommandHandler implements ICommandHandler {

    public FileContentCommandHandler() {
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        if (command.getArgument(SharingConstants.PROP_CONTACT_ID) == null)
            return new Status(IStatus.WARNING, PLUGIN_ID,
                    CODE_MISSING_ARGUMENT, "Missing contact ID.", null); //$NON-NLS-1$

        if (matches.length < 1)
            return new Status(IStatus.WARNING, PLUGIN_ID,
                    CODE_MISSING_ARGUMENT, "Missing map ID.", null); //$NON-NLS-1$

        String resourceID = matches[0];
        final ISharedMap map = LocalNetworkSharing.getDefault()
                .getSharingService().getLocalLibrary().findMapByID(resourceID);
        if (map == null)
            return new Status(IStatus.CANCEL, PLUGIN_ID, CODE_NOT_FOUND,
                    "Shared map is not found.", null); //$NON-NLS-1$

        final InputStream stream = map.getResourceAsStream(monitor);
        if (stream == null)
            return new Status(IStatus.CANCEL, PLUGIN_ID, CODE_NOT_FOUND,
                    "Shared map is missing.", null); //$NON-NLS-1$

        IBinaryStore files = new BinaryStore();
        files.addEntry(PROP_CONTENT, new IBinaryEntry() {
            public InputStream openInputStream() throws IOException {
                return stream;
            }

            public void dispose() {
            }

            @Override
            public String toString() {
                return "MapContent@" + map.getResourceName(); //$NON-NLS-1$
            }
        });
        monitor.done();
        return new ReturnValue(PLUGIN_ID, files);
    }

}
