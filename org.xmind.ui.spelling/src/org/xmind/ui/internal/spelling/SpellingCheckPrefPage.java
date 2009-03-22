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
package org.xmind.ui.internal.spelling;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.swabunga.spell.engine.Configuration;

public class SpellingCheckPrefPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    private List<FieldEditor> settingFields = new ArrayList<FieldEditor>();

    private Composite settingsParent;

    public SpellingCheckPrefPage() {
        super(Messages.SpellingPrefPage_title, FLAT);
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(SpellingPlugin.getDefault().getPreferenceStore());
    }

    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(SpellingPlugin.SPELLING_CHECK_ENABLED,
                Messages.enableSpellCheck, getFieldEditorParent()));
        addSpellingSettings();
        updateOptions(SpellingPlugin.isSpellingCheckEnabled());
    }

    private void addSpellingSettings() {
        settingsParent = createSettingsParent();
        addSettingField(Configuration.SPELL_IGNOREUPPERCASE,
                Messages.ignoreAllCapital);
        addSettingField(Configuration.SPELL_IGNOREMIXEDCASE,
                Messages.ignoreMultiCapital);
        addSettingField(Configuration.SPELL_IGNOREINTERNETADDRESSES,
                Messages.ignoreWebAddress);
        addSettingField(Configuration.SPELL_IGNOREDIGITWORDS,
                Messages.ignoreNumberousAppendix);
        addSettingField(Configuration.SPELL_IGNORESENTENCECAPITALIZATION,
                Messages.ignoreFirstLowercaseSentences);
    }

    private void addSettingField(String name, String label) {
        FieldEditor field = new BooleanFieldEditor(name, label, settingsParent);
        addField(field);
        settingFields.add(field);
    }

    private Composite createSettingsParent() {
        Composite parent = getFieldEditorParent();
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        parent.setLayout(gridLayout);

        Label blank = new Label(parent, SWT.NONE);
        blank.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));

        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.options);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout2 = new GridLayout(1, false);
        gridLayout2.marginWidth = 5;
        gridLayout2.marginHeight = 5;
        gridLayout2.verticalSpacing = 0;
        gridLayout2.horizontalSpacing = 0;
        group.setLayout(gridLayout2);

        Composite composite = new Composite(group, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        return composite;
    }

    private void updateOptions(boolean enabled) {
        settingsParent.setEnabled(enabled);
        for (FieldEditor field : settingFields) {
            field.setEnabled(enabled, settingsParent);
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        FieldEditor field = (FieldEditor) event.getSource();
        if (SpellingPlugin.SPELLING_CHECK_ENABLED.equals(field
                .getPreferenceName())) {
            updateOptions(((BooleanFieldEditor) field).getBooleanValue());
        }
        super.propertyChange(event);
    }

    public boolean performOk() {
        boolean ok = super.performOk();
        if (ok) {
            SpellCheckerAgent.setConfigurations(getPreferenceStore());
        }
        return ok;
    }
}