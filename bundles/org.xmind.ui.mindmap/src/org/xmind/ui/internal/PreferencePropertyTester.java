package org.xmind.ui.internal;

import org.eclipse.core.expressions.PropertyTester;

public class PreferencePropertyTester extends PropertyTester {

    private static final String P_KEY = "key"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (P_KEY.equals(property)) {
            if (expectedValue instanceof String)
                return MindMapUIPlugin.getDefault().getPreferenceStore()
                        .getBoolean((String) expectedValue);
        }
        return false;
    }

}
