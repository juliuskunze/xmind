package net.xmind.signin.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;

public class VerificationDelegate {

    private static VerificationDelegate instance = null;

    private List<IVerificationListener> listeners = new ArrayList<IVerificationListener>();

    private boolean valid = false;

    private VerificationDelegate() {
    }

    public void addVerificationListener(IVerificationListener listener) {
        listeners.add(listener);
    }

    public void removeVerificationListener(IVerificationListener listener) {
        listeners.remove(listener);
    }

    public void fireVerified(final boolean valid) {
        this.valid = valid;
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IVerificationListener) listener).verified(valid);
                }
            });
        }
    }

    public boolean isValid() {
        return valid;
    }

    public static VerificationDelegate getDefault() {
        if (instance == null) {
            instance = new VerificationDelegate();
        }
        return instance;
    }
}
