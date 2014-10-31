package org.xmind.ui.internal.evernote.signin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.xmind.ui.evernote.EvernotePlugin;
import org.xmind.ui.internal.evernote.EvernoteMessages;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.edam.type.User;

/**
 * @author Jason Wong
 */
public class SignInJob extends Job {

    private static final String PREF_BROWSER_CREATE_TIMESTAMP = "CreateBrowserTimestamp"; //$NON-NLS-1$

    private Properties data;

    private SignInDialog dialog;

    public SignInJob() {
        super("Sign In to Evernote.com"); //$NON-NLS-1$
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        if (!PlatformUI.isWorkbenchRunning())
            return new Status(IStatus.ERROR, EvernotePlugin.PLUGIN_ID,
                    "No workbench is running."); //$NON-NLS-1$

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return new Status(IStatus.ERROR, EvernotePlugin.PLUGIN_ID,
                    "No workbench is available."); //$NON-NLS-1$

        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return new Status(IStatus.ERROR, EvernotePlugin.PLUGIN_ID,
                    "No display is available."); //$NON-NLS-1$

        dialog = null;

        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                final Shell shell = window == null ? display.getActiveShell()
                        : window.getShell();
                if (shell != null)
                    shell.setActive();

                Properties prop = loadLocalData();
                if (prop != null) {
                    if (!MessageDialog
                            .openConfirm(
                                    shell,
                                    EvernoteMessages.EvernoteExportDialog_title,
                                    EvernoteMessages.EvernoteSignInDialog_BrowserCrash_label))
                        return;
                }

                writeBrowserCreateTimestamp();
                dialog = new SignInDialog(shell);
                int code = dialog.open();

                if (monitor.isCanceled())
                    return;

                if (code == SignInDialog.OK) {
                    data = dialog.getData();
                } else {
                    int returnCode = dialog.getCode();
                    if (returnCode == SignInDialog.OK) {
                        data = dialog.getData();
                    } else {
                        data = new Properties();
                    }
                }
            }
        });

        while (data == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }

            if (dialog != null && dialog.getReturnCode() == SignInDialog.CANCEL) {
                clearBrowserCreateTimestamp();
                return Status.CANCEL_STATUS;
            }
        }

        loadUsername();
        clearBrowserCreateTimestamp();
        return Status.OK_STATUS;
    }

    public Properties getData() {
        return this.data;
    }

    private void loadUsername() {
        ClientFactory factory = createEvernoteFactory();
        if (factory == null)
            return;

        User user = getEvernoteUser(factory);
        if (user == null)
            return;

        data.put(EvernoteAccountStore.USERNAME, user.getUsername());
    }

    private ClientFactory createEvernoteFactory() {
        String token = data.getProperty(EvernoteAccountStore.TOKEN);
        EvernoteService evernoteService = getEvernoteService();
        if (token == null || evernoteService == null)
            return null;

        return new ClientFactory(new EvernoteAuth(getEvernoteService(), token));
    }

    private EvernoteService getEvernoteService() {
        String serviceType = data
                .getProperty(EvernoteAccountStore.SERVICE_TYPE);
        if (EvernoteService.YINXIANG.name().equals(serviceType))
            return EvernoteService.YINXIANG;
        if (EvernoteService.PRODUCTION.name().equals(serviceType))
            return EvernoteService.PRODUCTION;
        return null;
    }

    private User getEvernoteUser(ClientFactory factory) {
        try {
            return factory.createUserStoreClient().getUser();
        } catch (Exception e) {
            EvernotePlugin.log(
                    "Failed to get the evernote user when sign in.", e); //$NON-NLS-1$
        }
        return null;
    }

    private void clearBrowserCreateTimestamp() {
        File stateFile = getBrowserFatalFile();
        if (stateFile == null)
            return;

        stateFile.delete();
    }

    private void writeBrowserCreateTimestamp() {
        File stateFile = getBrowserFatalFile();
        if (stateFile == null)
            return;

        Properties store = new Properties();
        saveState(store, PREF_BROWSER_CREATE_TIMESTAMP,
                String.valueOf(System.currentTimeMillis()));
        try {
            FileOutputStream stream = new FileOutputStream(stateFile);
            try {
                store.store(stream, "Awakening Gatha, Deena Metzger"); //$NON-NLS-1$
            } finally {
                stream.close();
            }
        } catch (IOException e) {
        }
    }

    private Properties loadLocalData() {
        File stateFile = getBrowserFatalFile();
        if (stateFile != null && stateFile.exists() && stateFile.isFile()) {
            Properties store = new Properties();
            try {
                InputStream stream = new BufferedInputStream(
                        new FileInputStream(stateFile), 1024);
                try {
                    store.load(stream);
                } finally {
                    stream.close();
                }
                return store;
            } catch (IOException e) {
            }
        }
        return null;
    }

    private static void saveState(Properties store, String name, String value) {
        if (value != null && !"".equals(value)) { //$NON-NLS-1$
            store.setProperty(name, value);
        } else {
            store.remove(name);
        }
    }

    private File getBrowserFatalFile() {
        Bundle bundle = Platform.getBundle(EvernotePlugin.PLUGIN_ID);
        if (bundle != null) {
            File root = Platform.getStateLocation(bundle).toFile();
            return new File(root, ".BrowserFatal"); //$NON-NLS-1$
        }
        return null;
    }
}
