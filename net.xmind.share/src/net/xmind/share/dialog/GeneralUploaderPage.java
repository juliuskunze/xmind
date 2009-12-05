/*
 * Copyright (c) 2006-2009 XMind Ltd. and others.
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

import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.signin.internal.VerificationDelegate;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class GeneralUploaderPage extends UploaderPage {

    private InfoField titleField;

    private InfoField descriptionField;

    private RadioInfoFieldGroup privacyGroup;

    public GeneralUploaderPage() {
        setTitle(Messages.UploaderDialog_GeneralPage_title);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 15;
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

        privacyGroup = new RadioInfoFieldGroup(false);
        privacyGroup.fill(composite);
        privacyGroup.setName(Messages.UploaderDialog_Privacy_title);
        privacyGroup.addOption(Info.Public,
                Messages.UploaderDialog_Public_label);
        privacyGroup.addOption(Info.PublicView,
                Messages.UploaderDialog_PublicView_label);
        Button privateShareWidget = privacyGroup.addOption(Info.Private,
                Messages.UploaderDialog_Private_label);
        privateShareWidget.setEnabled(VerificationDelegate.getDefault()
                .isValid());

        Object value = getPrivacyValue();
        setPrivacyValue(value);
        privacyGroup.setSelectedValue(value);
        privacyGroup
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        setPrivacyValue(((IStructuredSelection) event
                                .getSelection()).getFirstElement());
                    }
                });

        setControl(composite);
    }

    private void setPrivacyValue(Object value) {
        getInfo().setProperty(Info.ALLOW_DOWNLOAD, value);
    }

    private Object getPrivacyValue() {
        Object value = getInfo().getProperty(Info.ALLOW_DOWNLOAD);
        if (value == null) {
            value = getContainer().getDialogSettings().get(Info.ALLOW_DOWNLOAD);
            if (value == null) {
                value = Info.Public;
            }
        }
        return value;
    }

    public void setFocus() {
        if (descriptionField != null && !descriptionField.isDisposed()) {
            descriptionField.setFocus();
        }
    }

}