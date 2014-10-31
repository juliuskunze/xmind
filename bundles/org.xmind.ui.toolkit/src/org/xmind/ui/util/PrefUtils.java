package org.xmind.ui.util;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class PrefUtils {

    public static final String GENERAL_PREF_PAGE_ID = "org.eclipse.ui.preferencePages.Workbench"; //$NON-NLS-1$

    public static void openPrefDialog(Shell shell, String prefPageId,
            Object data) {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                shell, prefPageId, null, data);
        IProduct product = Platform.getProduct();
        if (product != null
                && "org.xmind.cathy.application".equals(product.getApplication())) { //$NON-NLS-1$
            dialog.getTreeViewer().expandAll();
        }
        dialog.open();
    }

    public static void openPrefDialog(Shell shell, String prefPageId) {
        openPrefDialog(shell, prefPageId, null);
    }

}
