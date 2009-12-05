/*
 * Copyright (c) 2006-2009 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.signin.internal;

import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.xmind.signin.ISignInDialogExtension;
import net.xmind.signin.ISignInListener;
import net.xmind.signin.IXMindCommandHandler;
import net.xmind.signin.util.IDataStore;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.browser.IPropertyChangingListener;
import org.xmind.ui.browser.PropertyChangingEvent;
import org.xmind.ui.internal.browser.BrowserViewer;

public class UserInfoManager implements IXMindCommandHandler {

    private class XMindCommandListener implements PropertyChangeListener,
            IPropertyChangingListener {

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
        }

        public void propertyChanging(PropertyChangingEvent event) {
            if (event.getSource() instanceof BrowserViewer) {
                String url = ((BrowserViewer) event.getSource()).getURL();
                if (isXMindUrl(url)) {
                    XMindCommand command = new XMindCommand((String) event
                            .getNewValue());
                    if (!command.parse())
                        return;

                    event.doit = false;
                    handleXMindCommand(command);
                }
            }
        }

        private boolean isXMindUrl(String url) {
            if (url != null) {
                try {
                    String host = new URI(url).getHost();
                    return host != null && host.endsWith(".xmind.net"); //$NON-NLS-1$
                } catch (Exception e) {

                }
            }
            return false;
        }

    }

    private class SignInJob extends Job {

        private String message;

        private ISignInDialogExtension extension;

        public SignInJob(String message, ISignInDialogExtension extension) {
            super("Sign in to xmind.net"); //$NON-NLS-1$
            this.message = message;
            this.extension = extension;
            setSystem(true);
        }

        protected IStatus run(IProgressMonitor monitor) {
            return doSignIn(monitor, message, extension);
        }
    }

//    private class SignOutJob extends Job {
//
//        private final String oldUserID;
//
//        private final String oldToken;
//
//        private SignOutJob(String oldUserID, String oldToken) {
//            super(NLS.bind(Messages.SignOut_jobName, oldUserID));
//            this.oldUserID = oldUserID;
//            this.oldToken = oldToken;
//            setSystem(true);
//        }
//
//        protected IStatus run(IProgressMonitor monitor) {
//            return doSignOut(oldUserID, oldToken, monitor);
//        }
//    }

    public static final String USER_ID = "user"; //$NON-NLS-1$

    public static final String TOKEN = "token"; //$NON-NLS-1$

    public static final String REMEMBER = "remember"; //$NON-NLS-1$

    //private static final String SIGN_OUT_URL = "http://www.xmind.net/_res/token/%s/%s"; //$NON-NLS-1$

    private static UserInfoManager instance = null;

    private IPreferenceStore prefStore;

    private Properties data;

    private List<ISignInListener> listeners;

    private List<ISignInListener> callbacks;

    private Job signInJob = null;

    private XMindCommandListener xmindCommandListener = null;

    private Map<String, List<IXMindCommandHandler>> xmindCommandHandlers = new HashMap<String, List<IXMindCommandHandler>>(
            1);

    private UserInfoManager(IPreferenceStore prefStore) {
        this.prefStore = prefStore;

        // Clear previously stored info
        prefStore.setToDefault("USER_ID"); //$NON-NLS-1$
        prefStore.setToDefault("TOKEN"); //$NON-NLS-1$

        this.data = new Properties();
        String userID = prefStore.getString(USER_ID);
        if ("".equals(userID)) //$NON-NLS-1$
            userID = null;
        String token = prefStore.getString(TOKEN);
        if ("".equals(token)) //$NON-NLS-1$
            token = null;
        if (userID != null)
            data.setProperty(USER_ID, userID);
        if (token != null)
            data.setProperty(TOKEN, token);
    }

    public Properties signIn() {
        return signIn((String) null, null);
    }

    public Properties signIn(String message, ISignInDialogExtension extension) {
        signIn(null, true, message, extension);
        return getUserInfo();
    }

//    public void signIn(ISignInListener callback) {
//        signIn(callback, false, null);
//    }

    public void signIn(ISignInListener calllback, boolean block) {
        signIn(calllback, block, null, null);
    }

    public void signIn(final ISignInListener callback, boolean block,
            String message) {
        signIn(callback, block, message, null);
    }

    public void signIn(final ISignInListener callback, boolean block,
            String message, ISignInDialogExtension extension) {
        if (hasSignedIn()) {
            if (callback != null)
                callback.postSignIn(getUserInfo());
            return;
        }

        if (callback != null) {
            if (callbacks == null)
                callbacks = new ArrayList<ISignInListener>();
            callbacks.add(callback);
        }

        Display display = block ? Display.getCurrent() : null;

        startSignInJob(message, extension);

        if (block) {
            while (signInJob != null) {
                if (display == null) {
                    // not in UI thread
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignore) {
                        break;
                    }
                } else {
                    // in UI thread
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            }
        }
    }

    /**
     * 
     */
    private void startSignInJob(String message, ISignInDialogExtension extension) {
        if (signInJob == null) {
            signInJob = new SignInJob(message, extension);
            signInJob.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    signInJob = null;
                }
            });
            signInJob.schedule();
        }
    }

    /**
     * 
     */
    private IStatus doSignIn(final IProgressMonitor monitor,
            final String message, final ISignInDialogExtension extension) {
        if (!PlatformUI.isWorkbenchRunning())
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "No workbench is running."); //$NON-NLS-1$

        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "No workbench is available."); //$NON-NLS-1$

        final Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "No display is available."); //$NON-NLS-1$

        final SignInDialog[] dialogs = new SignInDialog[1];
        final boolean[] done = new boolean[1];
        done[0] = false;
        display.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                final Shell shell = window == null ? null : window.getShell();
                if (shell != null)
                    shell.setActive();

                SignInDialog dialog = new SignInDialog(shell, message,
                        extension);
                dialogs[0] = dialog;
                int code = dialog.open();
                dialogs[0] = null;

                if (monitor.isCanceled())
                    return;

                if (code == SignInDialog.OK) {
                    saveUserInfo(dialog.getData());
                } else {
                    clearUserInfo();
                }
                done[0] = true;
            }
        });

        // block job thread
        while (!done[0] && !monitor.isCanceled()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }

        if (monitor.isCanceled()) {
            if (dialogs[0] != null) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        dialogs[0].close();
                    }
                });
            }
            return Status.CANCEL_STATUS;
        }

        notifyCallbacks(data.isEmpty() ? null : data);
        if (data != null && data.containsKey(USER_ID)
                && data.containsKey(TOKEN)) {
            firePostSignIn(data);
        }

        return Status.OK_STATUS;
    }

    private void notifyCallbacks(Properties userInfo) {
        int i = 0;
        while (callbacks != null && i < callbacks.size()) {
            ISignInListener callback = callbacks.get(i);
            if (callback != null) {
                if (userInfo != null) {
                    callback.postSignIn(new Properties(userInfo));
                } else {
                    callback.postSignOut();
                }
            }
            i++;
        }
        callbacks = null;
    }

    public Properties getUserInfo() {
        return data.isEmpty() ? null : new Properties(data);
    }

    private void saveUserInfo(IDataStore store) {
        String userID = store.getString(USER_ID);
        String token = store.getString(TOKEN);
        if (userID == null || token == null)
            throw new IllegalArgumentException();
        this.data.clear();
        this.data.putAll(store.toMap());
        if (Boolean.parseBoolean(store.getString(REMEMBER))) {
            prefStore.setValue(USER_ID, userID);
            prefStore.setValue(TOKEN, token);
        } else {
            prefStore.setToDefault(USER_ID);
            prefStore.setToDefault(TOKEN);
        }
    }

    public boolean hasSignedIn() {
        return getUserID() != null && getToken() != null;
    }

    public String getUserID() {
        return data.getProperty(USER_ID);
    }

    public String getToken() {
        return data.getProperty(TOKEN);
    }

    public void signOut() {
        signOut(true);
    }

    private void signOut(boolean notifyServer) {
//        final String oldUserID = getUserID();
//        final String oldToken = getToken();

        clearUserInfo();

//        if (notifyServer && oldUserID != null && oldToken != null) {
//            new SignOutJob(oldUserID, oldToken).schedule();
//        }

        firePostSignOut();
    }

//    private IStatus doSignOut(String oldUserID, String oldToken,
//            IProgressMonitor monitor) {
//        IWorkbench workbench = PlatformUI.getWorkbench();
//        if (workbench != null) {
//            final Display display = workbench.getDisplay();
//            if (display != null) {
//                display.asyncExec(new Runnable() {
//                    public void run() {
//                        SignOutDialog.getInstance().open();
//                    }
//                });
//            }
//        }
//        String url = String.format(SIGN_OUT_URL, oldUserID, oldToken);
//        HttpMethod method = new PostMethod(url);
//        method.setRequestHeader("Content-Type", //$NON-NLS-1$
//                "application/x-www-form-urlencoded; charset=UTF-8"); //$NON-NLS-1$
//        method.setRequestHeader("AuthToken", oldToken); //$NON-NLS-1$
//        method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
//
//        HttpClient client = new HttpClient();
//        try {
//            client.executeMethod(method);
//        } catch (Exception e) {
//            Activator.log(e);
//        }
//        return Status.OK_STATUS;
//    }

    private void clearUserInfo() {
        this.data.clear();
        prefStore.setToDefault(USER_ID);
        prefStore.setToDefault(TOKEN);
    }

    public static UserInfoManager getDefault() {
        if (instance == null)
            instance = new UserInfoManager(Activator.getDefault()
                    .getPreferenceStore());
        return instance;
    }

    public void addSignInListener(ISignInListener listener) {
        if (listeners == null || listener == null)
            listeners = new ArrayList<ISignInListener>();
        listeners.add(listener);
    }

    public void removeSignInListener(ISignInListener listener) {
        if (listeners == null || listener == null)
            return;
        listeners.remove(listener);
    }

    protected void firePostSignIn(final Properties userInfo) {
        if (listeners == null)
            return;
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((ISignInListener) listener).postSignIn(new Properties(
                            userInfo));
                }
            });
        }
    }

    protected void firePostSignOut() {
        if (listeners == null)
            return;
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((ISignInListener) listener).postSignOut();
                }
            });
        }
    }

    PropertyChangeListener getXMindCommandListener() {
        if (xmindCommandListener == null) {
            xmindCommandListener = new XMindCommandListener();
        }
        return xmindCommandListener;
    }

    public boolean handleXMindCommand(XMindCommand command) {
        if ("signout".equals(command.getCommand())) { //$NON-NLS-1$
            signOut(false);
            return true;
        } else if ("200".equals(command.getCode())) { //$NON-NLS-1$
            saveUserInfo(command.getJSON());
            firePostSignIn(data);
            return true;
        }
        return fireXMindCommand(command);
    }

    private boolean fireXMindCommand(final XMindCommand command) {
        String cmd = command.getCommand();
        if (cmd != null) {
            List<IXMindCommandHandler> handlers = xmindCommandHandlers.get(cmd);
            if (handlers != null && !handlers.isEmpty()) {
                return fireXMindCommand(command, handlers.toArray());
            }
        }
        String code = command.getCode();
        if (code != null) {
            List<IXMindCommandHandler> handlers = xmindCommandHandlers
                    .get(code);
            if (handlers != null && !handlers.isEmpty()) {
                return fireXMindCommand(command, handlers.toArray());
            }
        }
        return false;
    }

    private boolean fireXMindCommand(final XMindCommand command,
            Object[] handlers) {
        final boolean[] handled = new boolean[1];
        handled[0] = false;
        for (final Object handler : handlers) {
            if (handled[0])
                return true;
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    handled[0] = ((IXMindCommandHandler) handler)
                            .handleXMindCommand(command);
                }
            });
            if (handled[0])
                return true;
        }
        return handled[0];
    }

    public void addXMindCommandHandler(String commandName,
            IXMindCommandHandler handler) {
        List<IXMindCommandHandler> handlers = xmindCommandHandlers
                .get(commandName);
        if (handlers == null) {
            handlers = new ArrayList<IXMindCommandHandler>();
            xmindCommandHandlers.put(commandName, handlers);
        }
        handlers.add(handler);
    }

    public void removeXMindCommandHandler(String commandName,
            IXMindCommandHandler handler) {
        List<IXMindCommandHandler> handlers = xmindCommandHandlers
                .get(commandName);
        if (handlers != null && !handlers.isEmpty()) {
            handlers.remove(handler);
            if (handlers.isEmpty()) {
                xmindCommandHandlers.remove(commandName);
            }
        }
    }

}