package org.xmind.cathy.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class AutoBackupIndicator extends ContributionItem implements
        IPropertyChangeListener, Listener {

    private static final int DISABLED = 1;

    private static final int ENABLED = 2;

//    private static final int BACKUP_ENABLED = 2;
//
//    private static final int ALL_ENABLED = 3;

    private class ChangeAutoSavePrefAction extends Action {
        private IPreferenceStore ps;
        private int value;

        /**
         * 
         */
        public ChangeAutoSavePrefAction(IPreferenceStore ps, String text,
                int value) {
            super(text);
            this.ps = ps;
            this.value = value;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            if (ps == null)
                return;

            changeStatus(value);
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    private static class OpenPreferencePageAction extends Action {

        /**
         * 
         */
        public OpenPreferencePageAction() {
            super(WorkbenchMessages.AutoBackupIndicator_OpenPreferenceAction_text);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            PreferencesUtil.createPreferenceDialogOn(null,
                    "org.xmind.ui.GeneralPrefPage", null, null).open(); //$NON-NLS-1$
        }
    }

    private Label label;

    private MenuManager menu;

    private IPreferenceStore ps;

//    private int lastEnabledValue = 0;

    public AutoBackupIndicator() {
        super("org.xmind.ui.AutoSaveIndicator"); //$NON-NLS-1$
    }

    protected Control createControl(Composite parent) {
        ps = CathyPlugin.getDefault().getPreferenceStore();

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 3;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 4;
        composite.setLayout(gridLayout);

        Label sep = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
        sep.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));

        label = new Label(composite, SWT.NONE);
        label.setText(WorkbenchMessages.AutoBackupIndicator_AutoSaveDisabled_label);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        label.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        label.addListener(SWT.MouseDown, this);

        menu = new MenuManager();
        menu.add(new ChangeAutoSavePrefAction(ps, WorkbenchMessages.AutoBackupIndicator_DisableAutoSaveAction_text, DISABLED));
        menu.add(new ChangeAutoSavePrefAction(ps, WorkbenchMessages.AutoBackupIndicator_EnableAutoSaveAction_text, ENABLED));
//        menu.add(new ChangeBackupPrefAction(ps, "Enable backup", BACKUP_ENABLED));
//        menu.add(new ChangeBackupPrefAction(ps,
//                "Enable backup and local file saving", ALL_ENABLED));
        menu.add(new Separator());
        menu.add(new OpenPreferencePageAction());
        menu.createContextMenu(label);
        label.setMenu(menu.getMenu());

        Point size = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point size2 = sep.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        StatusLineLayoutData data = new StatusLineLayoutData();
        data.widthHint = size.x + size2.x + 10;
        data.heightHint = Math.max(size.y, size2.y);
        composite.setLayoutData(data);

        update();
        ps.addPropertyChangeListener(this);

        return composite;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.ContributionItem#update()
     */
    @Override
    public void update() {
        update(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
     */
    @Override
    public void update(String id) {
        super.update(id);

        if (label == null || label.isDisposed() || ps == null)
            return;

        if (id == null || CathyPlugin.AUTO_SAVE_ENABLED.equals(id)) {
            int value = getValue();
            if (value == ENABLED) {
                label.setText(WorkbenchMessages.AutoBackupIndicator_AutoSaveEnabled_label);
                int intervals = ps.getInt(CathyPlugin.AUTO_SAVE_INTERVALS);
                label.setToolTipText(NLS.bind(
                        WorkbenchMessages.AutoSave_label2, intervals));
            } else {
                label.setText(WorkbenchMessages.AutoBackupIndicator_AutoSaveDisabled_label);
                label.setToolTipText(WorkbenchMessages.AutoBackupIndicator_AutoSaveDisabled_description);
            }

            for (IContributionItem item : menu.getItems()) {
                if (item instanceof ActionContributionItem) {
                    IAction action = ((ActionContributionItem) item)
                            .getAction();
                    if (action instanceof ChangeAutoSavePrefAction) {
                        action.setChecked(((ChangeAutoSavePrefAction) action)
                                .getValue() == value);
                    }
                }
            }
        }
    }

    private int getValue() {
        return ps.getBoolean(CathyPlugin.AUTO_SAVE_ENABLED) ? ENABLED
                : DISABLED;
//        boolean backupEnabled = !ps
//                .getBoolean(CathyPlugin.AUTO_SAVE_REVISIONS_DISABLED);
//        int value;
//        if (backupEnabled && ps.getBoolean(CathyPlugin.AUTO_SAVE_ENABLED)) {
//            value = ALL_ENABLED;
//        } else if (backupEnabled) {
//            value = BACKUP_ENABLED;
//        } else {
//            value = DISABLED;
//        }
//        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.ContributionItem#dispose()
     */
    @Override
    public void dispose() {
        if (ps != null) {
            ps.removePropertyChangeListener(this);
            ps = null;
        }
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    public void fill(Composite parent) {
        createControl(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
     * .jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        update(event.getProperty());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(final Event event) {
        if (event.widget == label) {
            if (event.button == 1) {
                event.display.timerExec(10, new Runnable() {
                    public void run() {
                        Point loc = label.getParent().toDisplay(event.x,
                                event.y);
                        menu.getMenu().setLocation(loc);
                        menu.getMenu().setVisible(true);
//                        changeStatus();
                    }
                });
            }
        }
    }

//    /**
//     * 
//     */
//    private void changeStatus() {
//        int value = getValue();
//        int newValue;
//        if (value == DISABLED) {
//            if (lastEnabledValue == 0) {
//                newValue = BACKUP_ENABLED;
//            } else {
//                newValue = lastEnabledValue;
//            }
//        } else {
//            newValue = DISABLED;
//        }
//        changeStatus(newValue);
//    }

    private void changeStatus(int value) {
        ps.setValue(CathyPlugin.AUTO_SAVE_ENABLED, value == ENABLED);
//        if (value == DISABLED) {
//            ps.setValue(CathyPlugin.AUTO_SAVE_REVISIONS_DISABLED, true);
//            ps.setValue(CathyPlugin.AUTO_SAVE_ENABLED, false);
//        } else if (value == BACKUP_ENABLED) {
//            ps.setValue(CathyPlugin.AUTO_SAVE_REVISIONS_DISABLED, false);
//            ps.setValue(CathyPlugin.AUTO_SAVE_ENABLED, false);
//        } else {
//            ps.setValue(CathyPlugin.AUTO_SAVE_REVISIONS_DISABLED, false);
//            ps.setValue(CathyPlugin.AUTO_SAVE_ENABLED, true);
//        }
    }
}
