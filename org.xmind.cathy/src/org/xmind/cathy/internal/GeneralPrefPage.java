/* ******************************************************************************
 * Copyright (c) 2006-2009 XMind Ltd. and others.
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
package org.xmind.cathy.internal;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class GeneralPrefPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    private IntegerFieldEditor autoSaveIntervalsField;

    private Composite autoSaveIntervalsParent;

    private IntegerFieldEditor recentFilesField;

    public GeneralPrefPage() {
        super(WorkbenchMessages.GeneralPrefPage_title, FLAT);
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return CathyPlugin.getDefault().getPreferenceStore();
    }

    protected Control createContents(Composite parent) {
        Composite composite = (Composite) super.createContents(parent);
        ((GridLayout) composite.getLayout()).verticalSpacing = 15;
        return composite;
    }

    protected void createFieldEditors() {
        addRecentFileCountField();
        addAutoSaveGroup();
        addRememberLastSessionField();
        addCheckUpdatesField();
    }

    private void addRecentFileCountField() {
        addField(recentFilesField = new IntegerFieldEditor(
                IPreferenceConstants.RECENT_FILES,
                WorkbenchMessages.RecentFiles_label, getFieldEditorParent()));
    }

    private void addRememberLastSessionField() {
        addField(new BooleanFieldEditor(CathyPlugin.RESTORE_LAST_SESSION,
                WorkbenchMessages.RestoreLastSession_label,
                getFieldEditorParent()));
    }

    private void addCheckUpdatesField() {
        addField(new BooleanFieldEditor(CathyPlugin.CHECK_UPDATES_ON_STARTUP,
                WorkbenchMessages.CheckUpdates_label, getFieldEditorParent()));
    }

    private void addAutoSaveGroup() {
        Composite parent = getFieldEditorParent();
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        parent.setLayout(gridLayout);

        addField(new BooleanFieldEditor(CathyPlugin.AUTO_SAVE_ENABLED,
                WorkbenchMessages.AutoSave_label, createFieldContainer(parent,
                        false)));

        autoSaveIntervalsParent = createFieldContainer(parent, true);

        addField(autoSaveIntervalsField = new IntegerFieldEditor(
                CathyPlugin.AUTO_SAVE_INTERVALS, "", //$NON-NLS-1$
                autoSaveIntervalsParent));

        autoSaveIntervalsField.setEnabled(getPreferenceStore().getBoolean(
                CathyPlugin.AUTO_SAVE_ENABLED), autoSaveIntervalsParent);

        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(WorkbenchMessages.AutoSave_Minutes);

    }

    private Composite createFieldContainer(Composite parent,
            boolean grabHorizontal) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                grabHorizontal, true));
        composite.setLayout(new GridLayout(1, false));
        return composite;
    }

    protected void initialize() {
        super.initialize();
        IPreferenceStore uiPrefStore = WorkbenchPlugin.getDefault()
                .getPreferenceStore();
        recentFilesField.setPreferenceStore(uiPrefStore);
        recentFilesField.load();
    }

    public void init(IWorkbench workbench) {
    }

    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getSource() instanceof FieldEditor) {
            FieldEditor fe = (FieldEditor) event.getSource();
            if (event.getProperty().equals(FieldEditor.VALUE)) {
                String prefName = fe.getPreferenceName();
                if (CathyPlugin.AUTO_SAVE_ENABLED.equals(prefName)) {
                    autoSaveIntervalsField.setEnabled((Boolean) event
                            .getNewValue(), autoSaveIntervalsParent);
                }
            }
        }
    }
}