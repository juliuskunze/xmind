/*
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.xmind.signin.ISignInListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class UserInfoManager {

    private static class SignInCallback implements ISignInListener {

        private ISignInListener realCallback;

        private Display display;

        private boolean block;

        /**
         * 
         */
        public SignInCallback(ISignInListener realCallback, boolean block) {
            this.realCallback = realCallback;
            this.block = block;
            this.display = Display.getCurrent();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * net.xmind.signin.ISignInListener#postSignIn(java.util.Properties)
         */
        public void postSignIn(final Properties userInfo) {
            if (display != null) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        SafeRunner.run(new ISafeRunnable() {
                            public void run() throws Exception {
                                realCallback.postSignIn(userInfo);
                            }

                            public void handleException(Throwable exception) {
                                // do nothing
                            }
                        });
                    }
                };
                if (block) {
                    display.syncExec(runnable);
                } else {
                    display.asyncExec(runnable);
                }
            } else {
                realCallback.postSignIn(userInfo);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.xmind.signin.ISignInListener#postSignOut()
         */
        public void postSignOut() {
            if (display != null) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        SafeRunner.run(new ISafeRunnable() {
                            public void run() throws Exception {
                                realCallback.postSignOut();
                            }

                            public void handleException(Throwable exception) {
                                // do nothing
                            }
                        });
                    }
                };
                if (block) {
                    display.syncExec(runnable);
                } else {
                    display.asyncExec(runnable);
                }
            } else {
                realCallback.postSignOut();
            }
        }

    }

    public static final String USER_ID = "USER_ID"; //$NON-NLS-1$

    public static final String TOKEN = "TOKEN"; //$NON-NLS-1$

    private static final String SIGN_OUT_URL = "http://www.xmind.net/_res/token/%s/%s"; //$NON-NLS-1$

    private static UserInfoManager instance = null;

    private IPreferenceStore prefStore;

    private String userID;

    private String token;

    private List<ISignInListener> listeners;

    private List<ISignInListener> callbacks;

    private Job asyncSignInJob = null;

    private UserInfoManager(IPreferenceStore prefStore) {
        this.prefStore = prefStore;
        this.userID = prefStore.getString(USER_ID);
        if ("".equals(this.userID)) //$NON-NLS-1$
            this.userID = null;
        this.token = prefStore.getString(TOKEN);
        if ("".equals(this.token)) //$NON-NLS-1$
            this.token = null;
    }

    public Properties signIn() {
        final Properties[] result = new Properties[1];
        result[0] = null;
        signIn(new ISignInListener() {
            public void postSignOut() {
            }

            public void postSignIn(Properties userInfo) {
                result[0] = userInfo;
            }
        }, true);
        return result[0];
    }

    public void signIn(ISignInListener callback) {
        signIn(callback, true);
    }

    public void signIn(final ISignInListener callback, boolean block) {
        if (callback == null)
            return;

        if (hasSignedIn()) {
            callback.postSignIn(getUserInfo());
            return;
        }

        if (callbacks != null && callbacks.contains(callback))
            return;

        if (callbacks == null)
            callbacks = new ArrayList<ISignInListener>();
        callbacks.add(new SignInCallback(callback, block));

        Display display = block ? Display.getCurrent() : null;

        startAsyncSignInJob();

        if (block) {
            while (asyncSignInJob != null) {
                if (display == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignore) {
                        break;
                    }
                } else {
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
    private void startAsyncSignInJob() {
        if (asyncSignInJob == null) {
            asyncSignInJob = createAsyncSignInJob();
            asyncSignInJob.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    asyncSignInJob = null;
                }
            });
            asyncSignInJob.schedule();
        }
    }

    /**
     * @return
     */
    private Job createAsyncSignInJob() {
        Job job = new Job("Sign In To XMind.net") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                doSignInJob();
                return Status.OK_STATUS;
            }

        };
        job.setSystem(true);
        return job;
    }

    /**
     * 
     */
    private void doSignInJob() {
        IDisplayProvider displayProvider = getDisplayProvider();
        try {
            displayProvider.getDisplay().syncExec(new Runnable() {
                public void run() {
                    SignInDialog dialog = new SignInDialog(null);
                    int retCode = dialog.open();
                    if (retCode == SignInDialog.OK) {
                        saveUserInfo(dialog.getUserID(), dialog.getToken(),
                                dialog.shouldRemember());
                    } else {
                        clearUserInfo();
                    }
                }
            });
        } catch (Throwable e) {
            Activator.log(e);
        } finally {
            displayProvider.dispose();
        }

        Properties userInfo = getUserInfo();
        notifyCallbacks(userInfo);
        if (userInfo != null) {
            firePostSignIn(userInfo);
        }
    }

    private IDisplayProvider getDisplayProvider() {
        if (PlatformUI.isWorkbenchRunning())
            return new DisplayProvider(PlatformUI.getWorkbench().getDisplay());

        Display display = Display.getCurrent();
        if (display != null)
            return new DisplayProvider(display);

        display = Display.getDefault();
        return new DisplayProvider(display, display.getThread() == Thread
                .currentThread());
    }

    private void notifyCallbacks(Properties userInfo) {
        int i = 0;
        while (callbacks != null && i < callbacks.size()) {
            ISignInListener callback = callbacks.get(i);
            if (callback != null) {
                if (userInfo != null) {
                    callback.postSignIn(userInfo);
                } else {
                    callback.postSignOut();
                }
            }
            i++;
        }
        callbacks = null;
    }

    public Properties getUserInfo() {
        if (userID == null || token == null)
            return null;

        Properties info = new Properties();
        info.setProperty(USER_ID, userID);
        info.setProperty(TOKEN, token);
        return info;
    }

    private void saveUserInfo(String userID, String token, boolean remember) {
        if (userID == null || token == null)
            throw new IllegalArgumentException();
        this.userID = userID;
        this.token = token;
        if (remember) {
            prefStore.setValue(USER_ID, userID);
            prefStore.setValue(TOKEN, token);
        }
    }

    public boolean hasSignedIn() {
        return userID != null && token != null;
    }

    public String getUserID() {
        return userID;
    }

    public String getToken() {
        return token;
    }

    public void signOut() {
        signOut(true);
    }

    private void signOut(boolean notifyServer) {
        final String oldUserID = this.userID;
        final String oldToken = this.token;
        clearUserInfo();

        if (notifyServer && oldUserID != null && oldToken != null) {
            Job signOutJob = new Job(NLS.bind(Messages.SignOut_jobName,
                    oldUserID)) {
                protected IStatus run(IProgressMonitor monitor) {
                    String url = String.format(SIGN_OUT_URL, oldUserID,
                            oldToken);
                    HttpMethod method = new PostMethod(url);
                    method.setRequestHeader("Content-Type", //$NON-NLS-1$
                            "application/x-www-form-urlencoded; charset=UTF-8"); //$NON-NLS-1$
                    method.setRequestHeader("AuthToken", token); //$NON-NLS-1$
                    method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$

                    HttpClient client = new HttpClient();
                    try {
                        client.executeMethod(method);
                    } catch (Exception e) {
                        Activator.log(e);
                    }
                    return new Status(IStatus.OK, Activator.PLUGIN_ID,
                            "Sign out finished"); //$NON-NLS-1$
                }
            };
            signOutJob.setSystem(true);
            signOutJob.schedule();
        }

        firePostSignOut();
    }

    private void clearUserInfo() {
        this.userID = null;
        this.token = null;
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

    protected void firePostSignIn(Properties userInfo) {
        if (listeners == null)
            return;
        for (Object listener : listeners.toArray()) {
            ((ISignInListener) listener).postSignIn(userInfo);
        }
    }

    protected void firePostSignOut() {
        if (listeners == null)
            return;
        for (Object listener : listeners.toArray()) {
            ((ISignInListener) listener).postSignOut();
        }
    }

}