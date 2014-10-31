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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryViewer;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SendSharingMessageDialog extends Dialog {

    private static final String SETTINGS_ID = "org.xmind.ui.sharing.SendSharingMessageDialog"; //$NON-NLS-1$

    private static final String SETTING_SELECTED_USER = "selectedUser"; //$NON-NLS-1$

    private final ISharedMap[] maps;

    private ISharedLibrary[] remoteLibraries = null;

    private Combo userSelector = null;

    private Text messageText = null;

    public SendSharingMessageDialog(Shell parentShell, ISharedMap[] maps) {
        super(parentShell);
        Assert.isNotNull(maps);
        Assert.isLegal(maps.length > 0);
        this.maps = maps;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(NLS
                .bind(SharingMessages.SendSharingMessageDialog_dialogTitle_withCommonDialogTitle,
                        SharingMessages.CommonDialogTitle_LocalNetworkSharing));
    }

    @Override
    public void create() {
        super.create();
        messageText.setFocus();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Control userSelectionControl = createUserSelectionControl(composite);
        userSelectionControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));

        Control mapGalleryControl = createMapGalleryControl(composite);
        mapGalleryControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        Control messageInputControl = createMessageInputControl(composite);
        messageInputControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));

        return composite;
    }

    private Control createUserSelectionControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(SharingMessages.SendSharingMessageDialog_UserSelection_SendTo_label);

        Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Collection<IRemoteSharedLibrary> libraries = LocalNetworkSharingUI
                .getDefault().getSharingService().getRemoteLibraries();
        this.remoteLibraries = libraries.toArray(new ISharedLibrary[libraries
                .size()]);
        List<String> users = new ArrayList<String>();
        users.add(SharingMessages.SendSharingMessageDialog_UserSelection_AllAvailableUsers_text);
        for (ISharedLibrary library : libraries) {
            users.add(library.getName());
        }
        combo.setItems(users.toArray(new String[users.size()]));
        combo.select(findLastSelectedUser(loadLastSelectedUser()));
        this.userSelector = combo;

        return composite;
    }

    private Control createMapGalleryControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        GalleryViewer gallery = new GalleryViewer();
        gallery.setContentProvider(new ArrayContentProvider());
        gallery.setLabelProvider(new SharedMapLabelProvider());

        Properties properties = gallery.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout(
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_FILL, 1, 1,
                new Insets(5)));
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.FrameContentSize, new Dimension(200, 120));
        properties.set(GalleryViewer.PackFrameContent, Boolean.TRUE);
        properties
                .set(GalleryViewer.TitlePlacement, GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);

        gallery.createControl(composite);
        gallery.getCanvas().setScrollBarVisibility(FigureCanvas.NEVER);

        gallery.setInput(maps);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 400;
        gridData.heightHint = gallery.getControl()
                .computeSize(400, SWT.DEFAULT).y;
        gallery.getControl().setLayoutData(gridData);

        return composite;
    }

    private Control createMessageInputControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(SharingMessages.SendSharingMessageDialog_Message_label);

        Text text = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
                | SWT.BORDER);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textLayoutData.heightHint = 80;
        text.setLayoutData(textLayoutData);
        text.setText(""); //$NON-NLS-1$

        this.messageText = text;

        return composite;
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return LocalNetworkSharingUI.getDialogSettingsSection(SETTINGS_ID);
    }

    @Override
    public boolean close() {
        if (userSelector != null && !userSelector.isDisposed()) {
            saveLastSelectedUser(userSelector.getSelectionIndex() == 0 ? "" //$NON-NLS-1$
                    : userSelector.getText());
        }
        return super.close();
    }

    @Override
    protected void okPressed() {
        if (sendMessage()) {
            super.okPressed();
        }
    }

    private String getMessage() {
        return messageText != null && !messageText.isDisposed() ? messageText
                .getText() : ""; //$NON-NLS-1$
    }

    private boolean sendMessage() {
        if (remoteLibraries == null || remoteLibraries.length <= 0)
            return false;

        String message = getMessage();
        String[] mapIDs = new String[maps.length];
        for (int i = 0; i < maps.length; i++) {
            mapIDs[i] = maps[i].getID();
        }
        ISharedLibrary selectedLibrary = getSelectedLibrary();
        if (selectedLibrary == null) {
            SharingUtils.sendMessage(remoteLibraries, message, mapIDs);
        } else {
            SharingUtils.sendMessage(new ISharedLibrary[] { selectedLibrary },
                    message, mapIDs);
        }

        return true;
    }

    private ISharedLibrary getSelectedLibrary() {
        if (userSelector != null && !userSelector.isDisposed()) {
            int index = userSelector.getSelectionIndex();
            if (index == 0 || remoteLibraries == null)
                return null;
            return remoteLibraries[index - 1];
        }
        return null;
    }

    private int findLastSelectedUser(String userName) {
        if (userName != null && !"".equals(userName) && remoteLibraries != null) { //$NON-NLS-1$
            for (int i = 0; i < remoteLibraries.length; i++) {
                if (userName.equals(remoteLibraries[i].getName())) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    private String loadLastSelectedUser() {
        return getDialogBoundsSettings().get(SETTING_SELECTED_USER);
    }

    private void saveLastSelectedUser(String userName) {
        getDialogBoundsSettings().put(SETTING_SELECTED_USER, userName);
    }

}
