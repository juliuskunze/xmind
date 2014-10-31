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
package org.xmind.ui.internal.sharing;

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.sharing.ISharingListener;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.sharing.SharingEvent;
import org.xmind.ui.resources.FontUtils;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class LocalNetworkSharingPrefPage extends PreferencePage implements
        IWorkbenchPreferencePage, ISharingListener, IPropertyChangeListener,
        PropertyChangeListener {

    private ISharingService sharingService;

    private Text libraryNameEditor = null;

    private Label statusLabel = null;

    private Control noBonjourWidget = null;

    private Button changeStatusButton = null;

    public LocalNetworkSharingPrefPage() {
    }

    public void init(IWorkbench workbench) {
        this.sharingService = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (sharingService != null) {
            sharingService.addSharingListener(this);
        }
        setPreferenceStore(LocalNetworkSharingUI.getDefault()
                .getPreferenceStore());
        getPreferenceStore().addPropertyChangeListener(this);
        LocalNetworkSharingUI
                .getDefault()
                .getServiceStatusSupport()
                .addPropertyChangeListener(
                        LocalNetworkSharingUI.PREF_FEATURE_ENABLED, this);
    }

    protected Control createContents(Composite parent) {
        if (sharingService != null) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout compositeLayout = new GridLayout(1, false);
            compositeLayout.marginWidth = 0;
            compositeLayout.marginHeight = 0;
            compositeLayout.verticalSpacing = 15;
            compositeLayout.horizontalSpacing = 5;
            composite.setLayout(compositeLayout);

            Label descriptionLabel = new Label(composite, SWT.WRAP);
            descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP,
                    true, false));
            ((GridData) descriptionLabel.getLayoutData()).widthHint = 240;
            descriptionLabel
                    .setText(SharingMessages.PreferencePage_FeatureDescription);

            Composite form = new Composite(composite, SWT.NONE);
            form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            GridLayout formLayout = new GridLayout(2, false);
            formLayout.marginWidth = 5;
            formLayout.marginHeight = 5;
            formLayout.verticalSpacing = 15;
            formLayout.horizontalSpacing = 5;
            form.setLayout(formLayout);

            fillLibraryNameSection(form);
            fillStatusSection(form);

            updateLibraryNameEditor(false);
            updateStatusSection(false);

            return composite;
        } else {
            return new Composite(parent, SWT.NONE);
        }
    }

    private void fillLibraryNameSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
        label.setFont(JFaceResources.getDefaultFont());
        label.setText(SharingMessages.PreferencePage_Form_Name_label);

        libraryNameEditor = createLibraryNameEditor(parent);
        libraryNameEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
    }

    private Text createLibraryNameEditor(Composite parent) {
        final Text editor = new Text(parent, SWT.BORDER | SWT.SINGLE);
        editor.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                validateLibraryName(editor);
            }

            public void focusGained(FocusEvent e) {
                e.display.asyncExec(new Runnable() {
                    public void run() {
                        if (editor.isDisposed())
                            return;
                        editor.setSelection(0, editor.getCharCount());
                    }
                });
            }
        });
        editor.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateLibraryName(editor);
            }
        });
        return editor;
    }

    private void fillStatusSection(Composite parent) {
        Label titleLabel = new Label(parent, SWT.NONE);
        titleLabel.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false,
                false));
        titleLabel.setFont(JFaceResources.getDefaultFont());
        titleLabel.setText(SharingMessages.PreferencePage_Form_Status_label);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        Composite statusComposite = new Composite(composite, SWT.NONE);
        statusComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        GridLayout statusLayout = new GridLayout(2, false);
        statusLayout.marginWidth = 0;
        statusLayout.marginHeight = 0;
        statusLayout.verticalSpacing = 5;
        statusLayout.horizontalSpacing = 5;
        statusComposite.setLayout(statusLayout);

        statusLabel = new Label(statusComposite, SWT.LEFT);
        statusLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
                false, true));
        statusLabel.setFont(FontUtils.getBold(JFaceResources.DEFAULT_FONT));
        statusLabel.setText(""); //$NON-NLS-1$

        Composite noBonjourWidget = new Composite(statusComposite, SWT.NONE);
        this.noBonjourWidget = noBonjourWidget;
        noBonjourWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        GridLayout noBonjourLayout = new GridLayout(2, false);
        noBonjourLayout.marginWidth = 0;
        noBonjourLayout.marginHeight = 0;
        noBonjourLayout.verticalSpacing = 3;
        noBonjourLayout.horizontalSpacing = 3;
        noBonjourWidget.setLayout(noBonjourLayout);

        Label noBonjourWarningImage = new Label(noBonjourWidget, SWT.NONE);
        noBonjourWarningImage.setLayoutData(new GridData(SWT.BEGINNING,
                SWT.CENTER, false, true));
        noBonjourWarningImage.setImage(PlatformUI.getWorkbench()
                .getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));

        Label noBonjourWarningLabel = new Label(noBonjourWidget, SWT.WRAP);
        noBonjourWarningLabel.setLayoutData(new GridData(SWT.BEGINNING,
                SWT.CENTER, false, true));
        noBonjourWarningLabel
                .setText(SharingMessages.PreferencePage_Form_Status_DisplayArea_NoBonjour_warningText);

        changeStatusButton = new Button(composite, SWT.PUSH | SWT.CENTER);
        changeStatusButton.setLayoutData(new GridData(SWT.BEGINNING,
                SWT.BEGINNING, false, false));
        changeStatusButton.setText(""); //$NON-NLS-1$
        changeStatusButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                changeServiceStatus();
            }
        });
    }

    private boolean validateLibraryName(Text editor) {
        if ("".equals(editor.getText())) { //$NON-NLS-1$
            setErrorMessage(SharingMessages.PreferencePage_EmptyName_errorMessage);
            return false;
        } else {
            setErrorMessage(null);
            return true;
        }
    }

    private void changeServiceStatus() {
        final Display display = Display.getCurrent();
        changeStatusButton.setEnabled(false);
        Thread t = new Thread(new Runnable() {
            public void run() {
                final boolean featureEnabled = getPreferenceStore().getBoolean(
                        LocalNetworkSharingUI.PREF_FEATURE_ENABLED);
                final boolean bonjourInstalled = LocalNetworkSharingUI
                        .getDefault().isBonjourInstalled();
                Job job;
                if (!bonjourInstalled) {
                    job = LocalNetworkSharingUI.getDefault()
                            .getBonjourInstaller().installBonjour(false);
                } else {
                    job = ToggleSharingServiceStatusJob.startToggle(
                            sharingService, !featureEnabled, new Runnable() {
                                public void run() {
                                    getPreferenceStore()
                                            .setValue(
                                                    LocalNetworkSharingUI.PREF_FEATURE_ENABLED,
                                                    !featureEnabled);
                                }
                            }, false);
                }
                if (job == null)
                    return;

                try {
                    job.join();
                } catch (InterruptedException e) {
                }
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (changeStatusButton != null
                                && !changeStatusButton.isDisposed()) {
                            changeStatusButton.setEnabled(true);
                        }
                    }
                });
            }
        }, "ChangeLNSServiceStatus"); //$NON-NLS-1$
        t.setDaemon(true);
        t.start();
    }

    public void dispose() {
        if (sharingService != null) {
            sharingService.removeSharingListener(this);
        }
        getPreferenceStore().removePropertyChangeListener(this);
        super.dispose();
    }

    public void handleSharingEvent(final SharingEvent event) {
        if ((event.getType() == SharingEvent.Type.LIBRARY_NAME_CHANGED && event
                .isLocal())) {
            updateLibraryNameEditor(true);
        } else if (event.getType() == SharingEvent.Type.SERVICE_STATUS_CHANGED) {
            updateStatusSection(true);
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        updateStatusSection(true);
    }

    private void updateLibraryNameEditor(boolean async) {
        if (libraryNameEditor == null || libraryNameEditor.isDisposed())
            return;

        if (async) {
            libraryNameEditor.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateLibraryNameEditor(false);
                }
            });
        } else {
            libraryNameEditor.setText(sharingService.getLocalLibrary()
                    .getName());
        }
    }

    private void updateStatusSection(boolean async) {
        if (statusLabel == null || statusLabel.isDisposed()
                || changeStatusButton == null
                || changeStatusButton.isDisposed() || noBonjourWidget == null
                || noBonjourWidget.isDisposed())
            return;

        if (async) {
            statusLabel.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateStatusSection(false);
                }
            });
        } else {
            boolean featureEnabled = getPreferenceStore().getBoolean(
                    LocalNetworkSharingUI.PREF_FEATURE_ENABLED);
            boolean bonjourInstalled = LocalNetworkSharingUI.getDefault()
                    .isBonjourInstalled();
            if (!bonjourInstalled) {
                statusLabel
                        .setText(SharingMessages.PreferencePage_Form_Status_DisplayArea_Disabled_text);
                changeStatusButton
                        .setText(SharingMessages.PreferencePage_Form_Status_ControlArea_InstallBonjourAndEnable_buttonText);
            } else if (featureEnabled) {
                statusLabel
                        .setText(SharingMessages.PreferencePage_Form_Status_DisplayArea_Enabled_text);
                changeStatusButton
                        .setText(SharingMessages.PreferencePage_Form_Status_ControlArea_Disable_buttonText);
            } else {
                statusLabel
                        .setText(SharingMessages.PreferencePage_Form_Status_DisplayArea_Disabled_text);
                changeStatusButton
                        .setText(SharingMessages.PreferencePage_Form_Status_ControlArea_Enable_buttonText);
            }
            boolean showNoBonjourWarning = !bonjourInstalled;
            noBonjourWidget.setVisible(showNoBonjourWarning);
            ((GridData) noBonjourWidget.getLayoutData()).exclude = !showNoBonjourWarning;
            changeStatusButton.getParent().layout();
        }
    }

    @Override
    public boolean performOk() {
        if (!saveLibraryName())
            return false;
        return true;
    }

    protected void performDefaults() {
        if (libraryNameEditor != null && !libraryNameEditor.isDisposed()) {
            libraryNameEditor.setText(SharingUtils.getComputerName());
        }
        super.performDefaults();
    }

    private boolean saveLibraryName() {
        if (libraryNameEditor == null || libraryNameEditor.isDisposed())
            return true;
        if (!validateLibraryName(libraryNameEditor))
            return false;

        String name = libraryNameEditor.getText();
        sharingService.getLocalLibrary().setName(name);
        return true;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        updateStatusSection(true);
    }

}
