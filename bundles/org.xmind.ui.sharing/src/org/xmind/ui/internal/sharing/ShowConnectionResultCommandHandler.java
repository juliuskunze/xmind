package org.xmind.ui.internal.sharing;

import static org.xmind.core.sharing.SharingConstants.PROP_NAME;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;
import org.xmind.core.sharing.IContactManager;
import org.xmind.core.sharing.ISharingService;
import org.xmind.ui.dialogs.Notification;

public class ShowConnectionResultCommandHandler implements ICommandHandler {

    public IStatus execute(IProgressMonitor monitor, ICommand command,
            String[] matches) {
        String contactID = command.getArgument(PROP_REMOTE_ID);
        if (contactID == null || "".equals(contactID)) //$NON-NLS-1$
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "Missing remote library id."); //$NON-NLS-1$

        final String remoteName = command.getArgument(PROP_NAME);
        if (remoteName == null || "".equals(remoteName)) //$NON-NLS-1$
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "Missing remote name."); //$NON-NLS-1$

        final String verificationCode = command
                .getArgument(PROP_VERIFICATION_CODE);
        if (verificationCode == null || "".equals(verificationCode)) //$NON-NLS-1$
            return new Status(IStatus.WARNING, "org.xmind.core.sharing", //$NON-NLS-1$
                    "Missing verification code."); //$NON-NLS-1$

        ISharingService service = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (service == null)
            return new Status(IStatus.ERROR, "org.xmind.core.sharing", //$NON-NLS-1$
                    "No sharing service available."); //$NON-NLS-1$

        String localVerificationCode = service.getContactManager()
                .getVerificationCode();
        if (!localVerificationCode.equals(verificationCode))
            return null;

        service.getContactManager().addContact(contactID, remoteName,
                IContactManager.ACCESS_WRITE_READ);

        if (!PlatformUI.isWorkbenchRunning())
            return null;

        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return null;

        Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return null;

        display.asyncExec(new Runnable() {
            public void run() {
                showNotification(remoteName);
            }
        });
        return Status.OK_STATUS;
    }

    private void showNotification(String remoteName) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        Shell parentShell = window == null ? Display.getCurrent()
                .getActiveShell() : window.getShell();

        final Notification[] notification = new Notification[1];
        IAction okAction = new Action() {
            @Override
            public void run() {
                notification[0].close();
            }
        };
        okAction.setText(SharingMessages.ShowConnectionResultCommandHandler_okAction_name);
        okAction.setImageDescriptor(LocalNetworkSharingUI
                .imageDescriptorFromPlugin(LocalNetworkSharingUI.PLUGIN_ID,
                        "icons/localnetwork32.png")); //$NON-NLS-1$

        notification[0] = new Notification(
                parentShell,
                null,
                NLS.bind(
                        SharingMessages.ShowConnectionResultHandler_ResultDialog_dialogMessage_withRemoteName,
                        remoteName), okAction, null);
        notification[0].setGroupId(LocalNetworkSharingUI.PLUGIN_ID);
        notification[0].setCenterPopUp(true);
        notification[0].setDuration(-1);
        notification[0].popUp();
    }

}
