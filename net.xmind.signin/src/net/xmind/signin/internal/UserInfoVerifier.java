/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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

package net.xmind.signin.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import net.xmind.signin.ISignInListener;
import net.xmind.signin.IVerifyListener;
import net.xmind.signin.XMindNetEntry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frank Shaka
 * 
 */
public class UserInfoVerifier implements ISignInListener {

    private static class Callback implements IVerifyListener {

        private IVerifyListener callback;

        private Display display;

        /**
         * 
         */
        public Callback(IVerifyListener callback) {
            this.callback = callback;
            this.display = Display.getCurrent();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * net.xmind.signin.IVerifyListener#notifyValidity(org.eclipse.core.
         * runtime.IStatus)
         */
        public void notifyValidity(final IStatus validity) {
            if (display == null || !display.isDisposed()) {
                callback.notifyValidity(validity);
            } else {
                display.asyncExec(new Runnable() {
                    public void run() {
                        callback.notifyValidity(validity);
                    }
                });
            }
        }

    }

    /**
     * @author Frank Shaka
     * 
     */
    private final class VerifyJob extends Job {

        private VerifyJob() {
            super("Verify user information"); //$NON-NLS-1$
            setSystem(true);
        }

        protected IStatus run(IProgressMonitor monitor) {
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            Properties userInfo = XMindNetEntry.getCurrentUserInfo();
            if (userInfo == null)
                return Status.CANCEL_STATUS;
            return doVerify(userInfo);
        }
    }

    private static final boolean DEBUG = false;

    private static UserInfoVerifier instance = null;

    private IStatus validity = null;

    private List<IVerifyListener> listeners;

    private List<IVerifyListener> callbacks;

    private boolean firingValidity = false;

    /**
     * 
     */
    public UserInfoVerifier() {
        XMindNetEntry.addSignInListener(this);
    }

    public void postSignOut() {
        this.validity = createValidity(IVerifyListener.NOT_SIGNED_IN);
        notifyValidity(validity, null);
    }

    public void postSignIn(Properties userInfo) {
        verify(null);
    }

    public void verify() {
        verify(null);
    }

    public void verify(IVerifyListener callback) {
        if (validity != null) {
            int code = validity.getCode();
            if (code == IVerifyListener.VALID
                    || (code & IVerifyListener.INVALID) != 0) {
                notifyValidity(validity, callback);
                return;
            }
        }

        Properties userInfo = XMindNetEntry.getCurrentUserInfo();
        if (userInfo == null) {
            this.validity = createValidity(IVerifyListener.NOT_SIGNED_IN);
            notifyValidity(validity, callback);
            return;
        }

        if (callback != null) {
            if (callbacks == null)
                callbacks = new ArrayList<IVerifyListener>();
            callbacks.add(new Callback(callback));
        }

        new VerifyJob().schedule();
    }

//    private void verify(Properties userInfo) {
//        if (userInfo != null) {
//            new VerifyJob(userInfo).schedule();
//        }
//    }
//
//    public void verify(Properties userInfo, IVerifyListener callback) {
//        if (userInfo != null) {
//            new VerifyJob(userInfo, callback).schedule();
//        }
//    }

    /**
     * @return
     */
    private IStatus doVerify(Properties userInfo) {
        String userID = userInfo.getProperty(XMindNetEntry.USER_ID);
        String token = userInfo.getProperty(XMindNetEntry.TOKEN);
        debug("user info -----------"); //$NON-NLS-1$
        debug("user id: " + userID); //$NON-NLS-1$
        debug("token: " + token); //$NON-NLS-1$

        debug("generate http method"); //$NON-NLS-1$
        String url = "http://www.xmind.net/xmind/verify/?user=" + userID //$NON-NLS-1$
                + "&token=" + token; //$NON-NLS-1$
        debug("url: " + url); //$NON-NLS-1$
        GetMethod method = new GetMethod(url);
        method.setRequestHeader("AuthToken", token); //$NON-NLS-1$
        method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$

        IStatus validity;
        HttpClient client = new HttpClient();
        try {
            debug("start execute method"); //$NON-NLS-1$
            int result = client.executeMethod(method);
            debug("result: " + result); //$NON-NLS-1$
            if (HttpStatus.SC_OK == result) {
                int state = getValidityState(client, method);
                validity = createValidity(state);
            } else {
                validity = createValidity(IVerifyListener.ERROR);
            }
        } catch (Throwable e) {
            debug(e);
            validity = createValidity(IVerifyListener.ERROR, e);
        }

        this.validity = validity;
        debug("validity: " + validity == null ? "null" : validity.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        notifyValidity(validity, null);

        return validity;
    }

    public IStatus getValidity() {
        return validity;
    }

    private int getValidityState(HttpClient client, GetMethod method)
            throws JSONException, IOException {
        String resp = method.getResponseBodyAsString();
        debug("response: " + resp); //$NON-NLS-1$

        JSONObject json = new JSONObject(resp);

        long expireDate = json.getLong("expireDate"); //$NON-NLS-1$
        if (expireDate < 0) {
            debug("not subscribed"); //$NON-NLS-1$
            return IVerifyListener.NOT_SUBSCRIBED;
        }

        boolean expired = json.getBoolean("expired"); //$NON-NLS-1$
        if (expired) {
            debug("expired"); //$NON-NLS-1$
            return IVerifyListener.EXPIRED;
        }

        Calendar expDate = Calendar.getInstance();
        expDate.setTimeInMillis(expireDate);
        Calendar now = Calendar.getInstance();
        if (expDate.after(now)) {
            debug(String.format("valid: %1$tF, %2$tF", expDate, now)); //$NON-NLS-1$
            return IVerifyListener.VALID;
        }
        debug(String.format("expired: %1$tF, %2$tF", expDate, now)); //$NON-NLS-1$
        return IVerifyListener.EXPIRED;
    }

    /**
     * @param valid
     */
    private IStatus createValidity(int state) {
        return createValidity(state, null);
    }

    /**
     * @param state
     * @param object
     */
    private IStatus createValidity(int state, Throwable error) {
        int severity;
        if (state == IVerifyListener.ERROR) {
            severity = IStatus.WARNING;
        } else {
            severity = IStatus.OK;
        }
        return new Status(severity, Activator.PLUGIN_ID, state, null, error);
    }

    /**
     * @param validity
     */
    private void notifyValidity(IStatus validity, IVerifyListener callback) {
        Assert.isNotNull(validity);
        if (callback != null) {
            callback.notifyValidity(validity);
        } else {
            notifyCallbacks(validity);
        }
        fireValidify(validity);
    }

    /**
     * @param validity2
     */
    private synchronized void notifyCallbacks(final IStatus validity) {
        if (callbacks == null)
            return;
        for (final Object callback : callbacks.toArray()) {
            try {
                ((IVerifyListener) callback).notifyValidity(validity);
            } catch (Throwable e) {
                Activator.log(e);
            }
        }
        callbacks.clear();
        callbacks = null;
    }

    /**
     * @param result
     */
    private void fireValidify(final IStatus result) {
        if (listeners == null || firingValidity)
            return;
        firingValidity = true;
        for (final Object listener : listeners.toArray()) {
            if (listener != null) {
                try {
                    ((IVerifyListener) listener).notifyValidity(result);
                } catch (Throwable e) {
                    Activator.log(e);
                }
            }
        }
        firingValidity = false;
    }

    public void addVerifyListener(IVerifyListener listener) {
        if (listeners == null)
            listeners = new ArrayList<IVerifyListener>();
        listeners.add(listener);
    }

    public void removeVerifyListener(IVerifyListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private static void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    private static void debug(Throwable e) {
        if (DEBUG) {
            e.printStackTrace();
        }
    }

//    private void verify(final Properties userInfo) {
//        if (isSigningIn || isCheckingPro)
//            return;
//
//        final IWorkbench workbench = PlatformUI.getWorkbench();
//        if (workbench != null) {
//            final Display display = workbench.getDisplay();
//            display.asyncExec(new Runnable() {
//                public void run() {
//                    IWorkbenchWindow window = workbench
//                            .getActiveWorkbenchWindow();
//                    if (window != null) {
//                        checkPro(window.getShell(), userInfo, false);
//                    }
//                }
//            });
//        }
//    }
//
//    public void checkPro(Shell parentShell) {
//        if (isCheckingPro)
//            return;
//
//        checkPro(parentShell, null, true);
//    }
//
//    /**
//     * 
//     * @param parentShell
//     * @param userInfo
//     * @param handleInvalidation
//     */
//    private void checkPro(Shell parentShell, Properties userInfo,
//            boolean handleInvalidation) {
//        isCheckingPro = true;
//
//        if (userInfo == null) {
//            userInfo = getUserInfo(parentShell);
//        }
//
//        if (userInfo != null) {
//            doCheckPro(parentShell, userInfo, handleInvalidation);
//        }
//
//        isCheckingPro = false;
//    }
//
//    /**
//     * @param parentShell
//     * @param userInfo
//     * @param handleInvalidation
//     */
//    private void doCheckPro(Shell parentShell, Properties userInfo,
//            boolean handleInvalidation) {
//        
//
//    }
//
//    /**
//     * @return
//     */
//    private Properties getUserInfo(Shell parentShell) {
//        return XMindNetEntry.signIn();
//    }
//
    /**
     * @return the instance
     */
    public static UserInfoVerifier getInstance() {
        if (instance == null) {
            instance = new UserInfoVerifier();
        }
        return instance;
    }

}
