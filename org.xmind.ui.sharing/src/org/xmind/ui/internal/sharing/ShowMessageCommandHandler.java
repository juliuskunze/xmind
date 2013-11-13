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

import static org.xmind.core.sharing.SharingConstants.PROP_CONTENT;
import static org.xmind.core.sharing.SharingConstants.PROP_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_MAPS;
import static org.xmind.core.sharing.SharingConstants.PROP_REMOTE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;
import org.xmind.core.command.arguments.ArrayMapper;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.ISharingService;
import org.xmind.ui.dialogs.NotificationWindow;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class ShowMessageCommandHandler implements ICommandHandler {

    public ShowMessageCommandHandler() {
    }

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        String remoteSymbolicName = command.getArgument(PROP_REMOTE);
        if (remoteSymbolicName == null || "".equals(remoteSymbolicName)) //$NON-NLS-1$
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "Missing remote symbolic name."); //$NON-NLS-1$

        ISharingService sharingService = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (sharingService == null)
            return new Status(IStatus.ERROR, "org.xmind.core.sharing", //$NON-NLS-1$
                    "No sharing service available."); //$NON-NLS-1$

        IRemoteSharedLibrary sourceLibrary = sharingService
                .findRemoteLibrary(remoteSymbolicName);
        if (sourceLibrary == null)
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "Remote library not found."); //$NON-NLS-1$

        ArrayMapper mapsReader = new ArrayMapper(command.getArguments()
                .getRawMap(), PROP_MAPS);
        List<ISharedMap> maps = new ArrayList<ISharedMap>(mapsReader.getSize());
        while (mapsReader.hasNext()) {
            mapsReader.next();
            ISharedMap map = sourceLibrary.findMapByID((String) mapsReader
                    .get(PROP_ID));
            if (map != null) {
                maps.add(map);
            }
        }

        String message = command.getArgument(PROP_CONTENT);
        if ((message == null || "".equals(message)) && maps.isEmpty()) { //$NON-NLS-1$
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "No message or maps to show."); //$NON-NLS-1$
        }

        showMessage(sourceLibrary, message, maps);
        return Status.OK_STATUS;
    }

    private void showMessage(final ISharedLibrary sourceLibrary,
            final String message, final List<ISharedMap> maps) {
        if (!PlatformUI.isWorkbenchRunning())
            return;
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) {
            Display display = workbench.getDisplay();
            if (display != null && !display.isDisposed()) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        doShowMessage(sourceLibrary.getName(), message, maps);
                    }
                });
            }
        }
    }

    private void doShowMessage(final String sourceName, final String message,
            final List<ISharedMap> maps) {
        String text;
        if (maps.size() == 0) {
            text = NLS
                    .bind(SharingMessages.SharingMessageNotification_label_withRemoteUserName_and_ZeroSharedMaps,
                            sourceName);
        } else if (maps.size() == 1) {
            text = NLS
                    .bind(SharingMessages.SharingMessageNotification_label_withRemoteUserName_and_OneSharedMap,
                            sourceName, maps.get(0).getResourceName());
        } else {
            StringBuilder mapNames = new StringBuilder(maps.size() * 15);
            for (int i = 0; i < maps.size(); i++) {
                ISharedMap map = maps.get(i);
                if (i > 0) {
                    if (i == maps.size() - 1) {
                        mapNames.append(SharingMessages.SharingMessageNotification_LastSharedMapNameConjunction);
                    } else {
                        mapNames.append(SharingMessages.SharingMessageNotification_DefaultSharedMapNamesConjunction);
                    }
                }
                mapNames.append('\u2018');
                mapNames.append(map.getResourceName());
                mapNames.append('\u2019');
            }
            text = NLS
                    .bind(SharingMessages.SharingMessageNotification_label_withRemoteUserName_and_NumberOfSharedMaps_and_ConjunctSharedMapNames,
                            new String[] { sourceName,
                                    String.valueOf(maps.size()),
                                    mapNames.toString() });
        }
        if (message != null && !"".equals(message)) { //$NON-NLS-1$
            text = text + "\n\n\u201C" + message + "\u201D"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        Shell parentShell = window == null ? Display.getCurrent()
                .getActiveShell() : window.getShell();

        final NotificationWindow[] notification = new NotificationWindow[1];

        IAction action = new Action() {

            public void run() {
                openMaps(maps);
            }
        };
        action.setText(text);

        IAction viewAction = new Action() {

            public void run() {
                openMaps(maps);
                notification[0].close();
            }
        };
        viewAction
                .setText(SharingMessages.SharingMessageNotification_ViewAction_text);

        notification[0] = new NotificationWindow(parentShell, null, action,
                viewAction, 0);
        notification[0].open();
    }

    private void openMaps(final List<ISharedMap> maps) {
        SafeRunner.run(new SafeRunnable() {

            public void run() throws Exception {
                IWorkbenchWindow window = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();
                if (window == null) {
                    window = PlatformUI.getWorkbench()
                            .openWorkbenchWindow(null);
                }
                SharingUtils.openSharedMaps(window.getActivePage(), maps);
            }
        });
    }

}
