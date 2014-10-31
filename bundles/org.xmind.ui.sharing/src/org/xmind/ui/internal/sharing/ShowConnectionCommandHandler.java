package org.xmind.ui.internal.sharing;

import static org.xmind.core.sharing.SharingConstants.PROP_REMOTE_ID;
import static org.xmind.core.sharing.SharingConstants.PROP_VERIFICATION_CODE;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;
import org.xmind.core.sharing.IContactManager;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedContact;
import org.xmind.core.sharing.ISharingService;
import org.xmind.ui.dialogs.Notification;

/**
 * @author Jason Wong
 */
public class ShowConnectionCommandHandler implements ICommandHandler {

    private ISharingService sharingService;

    private String verificationCode;

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        String contactID = command.getArgument(PROP_REMOTE_ID);
        if (contactID == null || "".equals(contactID)) //$NON-NLS-1$
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "Missing remote contact id."); //$NON-NLS-1$

        verificationCode = command.getArgument(PROP_VERIFICATION_CODE);
        if (verificationCode == null || "".equals(verificationCode)) //$NON-NLS-1$
            return new Status(IStatus.WARNING, "org.xmind.code.sharing", //$NON-NLS-1$
                    "Missing verification code."); //$NON-NLS-1$

        sharingService = LocalNetworkSharingUI.getDefault().getSharingService();
        if (sharingService == null)
            return new Status(IStatus.ERROR, "org.xmind.core.sharing", //$NON-NLS-1$
                    "No sharing service available."); //$NON-NLS-1$

        IRemoteSharedLibrary sourceLibrary = sharingService
                .findRemoteLibraryByID(contactID);
        if (sourceLibrary == null)
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "Remote library not found."); //$NON-NLS-1$

        showConnectionDialog(sourceLibrary);
        return Status.OK_STATUS;
    }

    private void showConnectionDialog(final IRemoteSharedLibrary sourceLibrary) {
        if (!PlatformUI.isWorkbenchRunning())
            return;

        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) {
            Display display = workbench.getDisplay();
            if (display != null && !display.isDisposed()) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        showNotification(sourceLibrary);
                    }
                });
            }
        }
    }

    private void showNotification(final IRemoteSharedLibrary sourceLibrary) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        Shell parentShell = window == null ? Display.getCurrent()
                .getActiveShell() : window.getShell();

        final Notification[] notification = new Notification[1];

        IAction ignoreAction = new Action() {
            @Override
            public void run() {
                connect(false, sourceLibrary);
            }
        };
        ignoreAction
                .setText(SharingMessages.ShowConnectionCommandHandler_ignoreAction_name);
        ignoreAction.setImageDescriptor(LocalNetworkSharingUI
                .imageDescriptorFromPlugin(LocalNetworkSharingUI.PLUGIN_ID,
                        "icons/localnetwork32.png")); //$NON-NLS-1$

        IAction acceptAction = new Action() {
            @Override
            public void run() {
                connect(true, sourceLibrary);
            }
        };
        acceptAction
                .setText(SharingMessages.ShowConnectionCommandHandler_acceptAction_name);
        acceptAction.setImageDescriptor(LocalNetworkSharingUI
                .imageDescriptorFromPlugin(LocalNetworkSharingUI.PLUGIN_ID,
                        "icons/localnetwork32.png")); //$NON-NLS-1$

        notification[0] = new Notification(parentShell, null, NLS.bind(
                SharingMessages.ConnectionDialog_ContentMessage_withRemoteName,
                sourceLibrary.getName()), ignoreAction, acceptAction);
        notification[0].setGroupId(LocalNetworkSharingUI.PLUGIN_ID);
        notification[0].setCenterPopUp(true);
        notification[0].setDuration(-1);
        notification[0].popUp();
    }

    private void connect(boolean isAccept, IRemoteSharedLibrary sourceLibrary) {
        if (isAccept) {
            String contactID = sourceLibrary.getContactID();
            String remoteName = sourceLibrary.getName();

            ISharedContact contact = sharingService.getContactManager()
                    .addContact(contactID, remoteName,
                            IContactManager.ACCESS_WRITE_READ);
            if (contact != null) {
                SharingUtils.returnConnectionInfo(sourceLibrary,
                        verificationCode);
                showLocalNetworkSharingView();
            }
        }
    }

    private void showLocalNetworkSharingView() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                try {
                    page.showView(LocalNetworkSharingUI.VIEW_ID);
                } catch (PartInitException e) {
                }
            }
        }
    }
}
