/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.share.XmindSharePlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;

public class GeneralUploaderPage extends UploaderPage implements
        PropertyChangeListener {

    private static final String LANGUAGE_CHANNEL = "net.xmind.share.dialog.defaultLanguageChannel"; //$NON-NLS-1$

    private InfoField titleField;

    private InfoField descriptionField;

    private FormText privacyText;

    public GeneralUploaderPage() {
        setTitle(Messages.UploaderDialog_GeneralPage_title);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());

        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 10;
        composite.setLayout(layout);

        titleField = new InfoField(false, true, true);
        titleField.fill(composite);
        titleField.setName(Messages.UploaderDialog_Title_text);
        titleField.setText(getInfo().getString(Info.TITLE));
        titleField.getTextWidget().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getInfo().setProperty(Info.TITLE, titleField.getText());
            }
        });

        descriptionField = new InfoField(true, false, false);
        descriptionField.fill(composite);
        descriptionField.setName(Messages.UploaderDialog_Description_text);
        descriptionField.setText(getInfo().getString(Info.DESCRIPTION));
        descriptionField.getTextWidget().addModifyListener(
                new ModifyListener() {
                    public void modifyText(ModifyEvent e) {
                        getInfo().setProperty(Info.DESCRIPTION,
                                descriptionField.getText());
                    }
                });

        createLanguageSection(composite);
        createPrivacySection(composite);

        setControl(composite);

        getInfo().addPropertyChangeListener(Info.PRIVACY, this);
        getInfo().addPropertyChangeListener(Info.DOWNLOADABLE, this);
    }

    private void createPrivacySection(Composite parent) {
        privacyText = new FormText(parent, SWT.NO_FOCUS);
        privacyText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        privacyText.setBackground(parent.getBackground());
        privacyText.addHyperlinkListener(new IHyperlinkListener() {

            public void linkExited(HyperlinkEvent e) {
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                if ("privacy".equals(e.getHref())) { //$NON-NLS-1$
                    goToPrivacyPage();
                }
            }
        });
        updatePrivacyLabel();
    }

    private void createLanguageSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
        label.setText(Messages.UploaderDialog_LanguageChannel_label);

        final Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
        ((GridData) combo.getLayoutData()).widthHint = 280;
        combo.setItems(new String[] { //
        "English", //$NON-NLS-1$
                "Chinese - \u4e2d\u6587", //$NON-NLS-1$
                "French - fran\u00e7ais", //$NON-NLS-1$
                "German - Deutsch", //$NON-NLS-1$
                "Japanese - \u65e5\u672c\u8a9e", //$NON-NLS-1$
                "Spanish - espa\u00f1ol", //$NON-NLS-1$
                "Worldwide" //$NON-NLS-1$
        });
        final IPreferenceStore prefStore = XmindSharePlugin.getDefault()
                .getPreferenceStore();
        String lang = prefStore.getString(LANGUAGE_CHANNEL);
        if (lang == null || "".equals(lang)) { //$NON-NLS-1$
            lang = Info.getDefaultLanguageCode();
        }

        int index = Info.LANGUAGE_CODES.indexOf(lang);
        if (index < 0) {
            // Default is Worldwide:
            index = Info.LANGUAGE_CODES.size() - 1;
        }
        combo.select(index);
        combo.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                int index = Math.min(combo.getSelectionIndex(),
                        Info.LANGUAGE_CODES.size() - 1);
                String lang = Info.LANGUAGE_CODES.get(index);
                prefStore.setValue(LANGUAGE_CHANNEL, lang);
                getInfo().setProperty(Info.LANGUAGE_CHANNEL, lang);
            }
        });
    }

    public void setFocus() {
        if (descriptionField != null && !descriptionField.isDisposed()) {
            descriptionField.setFocus();
        }
    }

    @Override
    public void dispose() {
        getInfo().removePropertyChangeListener(Info.PRIVACY, this);
        getInfo().removePropertyChangeListener(Info.DOWNLOADABLE, this);
        super.dispose();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (Info.PRIVACY.equals(name) || Info.DOWNLOADABLE.equals(name)) {
            updatePrivacyLabel();
        }
    }

    private void updatePrivacyLabel() {
        if (privacyText == null || privacyText.isDisposed())
            return;
        privacyText.setText(
                NLS.bind(Messages.UploaderDialog_Privacy_prompt, new String[] {
                        getAccessibilityText(), getDownloadableText(),
                        "privacy" }), true, false); //$NON-NLS-1$
    }

    private String getAccessibilityText() {
        Object acc = getInfo().getString(Info.PRIVACY, Info.PRIVACY_PUBLIC);
        if (Info.PRIVACY_PUBLIC.equals(acc))
            return Messages.UploaderDialog_Privacy_Public_title;
        if (Info.PRIVACY_PRIVATE.equals(acc))
            return Messages.UploaderDialog_Privacy_Private_title;
        return Messages.UploaderDialog_Privacy_Unlisted_title;
    }

    private String getDownloadableText() {
        Object value = getInfo().getString(Info.DOWNLOADABLE,
                Info.DOWNLOADABLE_YES);
        if (Info.DOWNLOADABLE_YES.equals(value))
            return Messages.UploaderDialog_Privacy_DownloadAllowed;
        return Messages.UploaderDialog_Privacy_DownloadForbidden;
    }

    private void goToPrivacyPage() {
        getContainer().showPage("org.xmind.ui.uploader.privacy"); //$NON-NLS-1$
    }

}