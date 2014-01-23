/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.xmind.signin.XMindNet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.core.util.FileUtils;

import com.swabunga.spell.engine.Configuration;

public class SpellingCheckPrefPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    private static final Object DEFAULT_PLACEHOLDER = Messages.defaultDictionary;

    private class DictionaryContentProvider implements
            IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            Object[] descriptors = SpellCheckerRegistry.getInstance()
                    .getDescriptors().toArray();
            if (getPreferenceStore().getBoolean(
                    SpellingPlugin.DEFAULT_SPELLING_CHECKER_DISABLED))
                return descriptors;

            Object[] elements = new Object[descriptors.length + 1];
            elements[0] = DEFAULT_PLACEHOLDER;
            System.arraycopy(descriptors, 0, elements, 1, descriptors.length);
            return elements;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private static class DictionaryLabelProvider extends LabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            if (element instanceof ISpellCheckerDescriptor)
                return ((ISpellCheckerDescriptor) element).getName();
            return super.getText(element);
        }
    }

    private static class DictionaryComparator extends ViewerComparator {

        /**
         * 
         */
        public DictionaryComparator() {
            super(new Comparator<String>() {
                public int compare(String n1, String n2) {
                    n1 = FileUtils.getNoExtensionFileName(n1);
                    n2 = FileUtils.getNoExtensionFileName(n2);
                    return n1.compareToIgnoreCase(n2);
                }
            });
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
         */
        @Override
        public int category(Object element) {
            if (element == DEFAULT_PLACEHOLDER)
                return 0;
            return 1;
        }
    }

    private class DictionarySelectionListener implements
            ISelectionChangedListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged
         * (org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event) {
            updateDictionaryControls();
        }

    }

    private List<FieldEditor> settingFields = new ArrayList<FieldEditor>();

    private Composite settingsParent;

    private ListViewer dictionaryViewer;

    private Button addButton;

    private Button removeButton;

    public SpellingCheckPrefPage() {
        super(Messages.SpellingPrefPage_title, FLAT);
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(SpellingPlugin.getDefault().getPreferenceStore());
    }

    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(SpellingPlugin.SPELLING_CHECK_ENABLED,
                Messages.enableSpellCheck, getFieldEditorParent()));
        addSpellingSettings(getFieldEditorParent());
        addDictionariesPanel(getFieldEditorParent());

        updateOptions(SpellingPlugin.isSpellingCheckEnabled());
        updateDictionaryControls();
    }

    private void addSpellingSettings(Composite composite) {
        settingsParent = createSettingsParent(composite);
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

    private Composite createSettingsParent(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 7;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        parent.setLayout(layout);

        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.options);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout groupLayout = new GridLayout(1, false);
        groupLayout.marginWidth = 5;
        groupLayout.marginHeight = 5;
        groupLayout.verticalSpacing = 5;
        groupLayout.horizontalSpacing = 0;
        group.setLayout(groupLayout);

        return group;
    }

    private void updateOptions(boolean enabled) {
        settingsParent.setEnabled(enabled);
        for (FieldEditor field : settingFields) {
            field.setEnabled(enabled, settingsParent);
        }
    }

    private void addDictionariesPanel(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 7;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.dictionaries);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout groupLayout = new GridLayout(2, false);
        groupLayout.marginWidth = 5;
        groupLayout.marginHeight = 5;
        groupLayout.verticalSpacing = 5;
        groupLayout.horizontalSpacing = 5;
        group.setLayout(groupLayout);

        createDictionaryViewer(group);
        createDictionaryControls(group);
        createDetailsLink(group);
    }

    private void createDictionaryViewer(Composite parent) {
        dictionaryViewer = new ListViewer(parent, SWT.SINGLE | SWT.BORDER);
        dictionaryViewer.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        dictionaryViewer.setContentProvider(new DictionaryContentProvider());
        dictionaryViewer.setLabelProvider(new DictionaryLabelProvider());
        dictionaryViewer.setComparator(new DictionaryComparator());
        dictionaryViewer
                .addSelectionChangedListener(new DictionarySelectionListener());
        dictionaryViewer.setInput(SpellCheckerRegistry.getInstance());
    }

    private void createDictionaryControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        Composite buttonBar = new Composite(composite, SWT.NONE);
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        GridLayout buttonBarLayout = new GridLayout(1, true);
        buttonBarLayout.marginWidth = 0;
        buttonBarLayout.marginHeight = 0;
        buttonBarLayout.verticalSpacing = 10;
        buttonBarLayout.horizontalSpacing = 0;
        buttonBar.setLayout(buttonBarLayout);

        createAddDictionaryButton(buttonBar);
        createRemoveDictionaryButton(buttonBar);
        //createDictionaryInfoPanel(composite);
    }

    private void createDetailsLink(Composite parent) {
        Hyperlink hyperlink = new Hyperlink(parent, SWT.SINGLE);
        hyperlink
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) hyperlink.getLayoutData()).horizontalSpan = 2;
        hyperlink.setText(Messages.detailsLink_text);
        hyperlink.setForeground(parent.getDisplay().getSystemColor(
                SWT.COLOR_BLUE));
        hyperlink.addHyperlinkListener(new IHyperlinkListener() {
            public void linkExited(HyperlinkEvent e) {
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                XMindNet.gotoURL(true,
                        "http://www.xmind.net/xmind/help/language-dic.html"); //$NON-NLS-1$
            }
        });
    }

    private void createAddDictionaryButton(Composite parent) {
        addButton = new Button(parent, SWT.PUSH);
        addButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        addButton.setText(Messages.dictionaries_add);
        addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                addDictionary();
            }
        });
    }

    private void createRemoveDictionaryButton(Composite parent) {
        removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        removeButton.setText(Messages.dictionaries_remove);
        removeButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                removeSelectedDictionary();
            }
        });
    }

    private void addDictionary() {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.dic;*.dict;*.txt;*.*" }); //$NON-NLS-1$
        final String path = dialog.open();
        if (path == null)
            return;

        final Display display = Display.getCurrent();
        try {
            ProgressMonitorDialog progress = new ProgressMonitorDialog(
                    getShell());
            progress.setOpenOnRun(false);
            progress.run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.addingDictionary, 1);
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            SpellCheckerRegistry.getInstance().importDictFile(
                                    new File(path));
                        }
                    });
                    display.asyncExec(new Runnable() {
                        public void run() {
                            dictionaryViewer.refresh();
                        }
                    });
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }

    }

    private void removeSelectedDictionary() {
        Object selection = ((IStructuredSelection) dictionaryViewer
                .getSelection()).getFirstElement();
        if (selection == null)
            return;

        // Confirm remove
        String name = ((ILabelProvider) dictionaryViewer.getLabelProvider())
                .getText(selection);
        if (!MessageDialog.openConfirm(getShell(),
                Messages.dictionaries_remove_confirm_title,
                NLS.bind(Messages.dictionaries_remove_confirm_message, name)))
            return;

        // Default dictionary?
        if (selection == DEFAULT_PLACEHOLDER) {
            getPreferenceStore().setValue(
                    SpellingPlugin.DEFAULT_SPELLING_CHECKER_DISABLED, true);
            dictionaryViewer.refresh();
            return;
        }

        // Remove dictionary descriptor and local file
        final ISpellCheckerDescriptor descriptor = (ISpellCheckerDescriptor) selection;
        final Display display = Display.getCurrent();
        try {
            ProgressMonitorDialog progress = new ProgressMonitorDialog(
                    getShell());
            progress.setOpenOnRun(false);
            progress.run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.removingDictionary, 1);
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            SpellCheckerRegistry.getInstance()
                                    .removeDictionary(descriptor);
                        }
                    });
                    display.asyncExec(new Runnable() {
                        public void run() {
                            dictionaryViewer.refresh();
                        }
                    });
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
    }

    private void updateDictionaryControls() {
        removeButton.setEnabled(!dictionaryViewer.getSelection().isEmpty());
    }

//    /**
//     * @param composite
//     */
//    private void createDictionaryInfoPanel(Composite composite) {
//
//    }

    public void propertyChange(PropertyChangeEvent event) {
        FieldEditor field = (FieldEditor) event.getSource();
        if (SpellingPlugin.SPELLING_CHECK_ENABLED.equals(field
                .getPreferenceName())) {
            updateOptions(((BooleanFieldEditor) field).getBooleanValue());
        }
        super.propertyChange(event);
    }

}