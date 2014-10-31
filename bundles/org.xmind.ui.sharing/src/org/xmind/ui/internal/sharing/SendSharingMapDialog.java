package org.xmind.ui.internal.sharing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.core.sharing.ILocalSharedMap;
import org.xmind.core.sharing.IRemoteSharedLibrary;
import org.xmind.core.sharing.ISharedContact;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharingService;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;

/**
 * @author Jason Wong
 */
public class SendSharingMapDialog extends Dialog {

    private ISharingService sharingService;

    private static final String ICON_CONTACT = "icons/localnetwork32.png"; //$NON-NLS-1$

    public static final String SHARE_ALL = "shareAll"; //$NON-NLS-1$

    public static final String SHARE_SELECT = "shareSelect"; //$NON-NLS-1$

    private static final String REMOTE_LIBRARY = "remoteLibrary"; //$NON-NLS-1$

    private static final String CONTACT_ID = "contactID"; //$NON-NLS-1$

    private Button selectAllButton;

    private Button shareAll;

    private Button shareSelect;

    private ScrolledComposite selectScrolled;

    private ScrolledComposite alreadySharedScrolled;

    private Text messageText;

    private String message;

    private File[] files;

    private ILocalSharedMap map;

    private int receiverSize = 0;

    private String selectShareMode;

    private List<Button> contactButtons = new ArrayList<Button>();

    private List<Button> alreadyButtons = new ArrayList<Button>();

    private List<Button> selectedButtons = new ArrayList<Button>();

    private List<String> receivers = new ArrayList<String>();

    public SendSharingMapDialog(Shell parentShell,
            ISharingService sharingService, File[] files) {
        super(parentShell);
        this.sharingService = sharingService;
        this.files = files;
    }

    public SendSharingMapDialog(Shell parentShell,
            ISharingService sharingService, ILocalSharedMap map) {
        this(parentShell, sharingService, new File[0]);
        this.map = map;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(SharingMessages.CommonDialogTitle_LocalNetworkSharing);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        createMessageArea(parent);

        Composite composite = (Composite) super.createDialogArea(parent);
        createTipArea(composite);
        createShareArea(composite);
        createOptionalMessageArea(composite);

        updateButtonStatus(getShareMode());
        return composite;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite buttonBar = (Composite) super.createButtonBar(parent);
        Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText(SharingMessages.SendSharingMapDialog_ShareButton_text);

        if (SHARE_SELECT.equals(getShareMode()))
            okButton.setEnabled(false);

        return buttonBar;
    }

    private void createMessageArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 20;
        gridLayout.marginHeight = 15;
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = 400;
        gridData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(gridData);
        composite.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));

        Label message = new Label(composite, SWT.WRAP);
        GridData messageData = new GridData(SWT.BEGINNING, SWT.CENTER, true,
                true);
        messageData.widthHint = SWT.DEFAULT;
        messageData.heightHint = SWT.DEFAULT;
        message.setLayoutData(messageData);
        message.setBackground(composite.getBackground());
        message.setText(SharingMessages.SendSharingMapDialog_dialogMessage);

        Label icon = new Label(composite, SWT.NONE);
        GridData iconData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        icon.setLayoutData(iconData);
        icon.setBackground(composite.getBackground());
        setContactImage(icon);

        createSeparator(parent);
    }

    private void setContactImage(Label label) {
        URL url = Platform.getBundle(LocalNetworkSharingUI.PLUGIN_ID).getEntry(
                ICON_CONTACT);

        if (url != null) {
            try {
                label.setImage(new Image(null, url.openStream()));
                return;
            } catch (IOException e) {
            }
        }
        label.setImage(label.getDisplay().getSystemImage(SWT.ICON_INFORMATION));
    }

    private void createSeparator(Composite parent) {
        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        sep.setLayoutData(gridData);
    }

    private void createTipArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label label = new Label(composite, SWT.WRAP);
        GridData labelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        labelData.widthHint = 350;
        label.setAlignment(SWT.CENTER);
        label.setLayoutData(labelData);

        label.setText(getTipText());
        fixLabelFontSize(label, labelData.widthHint);
        return;
    }

    private String getTipText() {
        if (map != null) {
            return getFileName(new File(map.getResourceName()));
        }

        if (files.length == 1) {
            return getFileName(files[0]);
        } else if (files.length > 1) {
            return NLS.bind(
                    SharingMessages.SendSharingMapDialog_tipText_withMapsCount,
                    files.length);
        }
        return ""; //$NON-NLS-1$
    }

    private String getFileName(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(MindMapUI.FILE_EXT_XMIND)) {
            int index = fileName.indexOf(MindMapUI.FILE_EXT_XMIND);
            fileName = fileName.substring(0, index);
        }
        return fileName.replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void fixLabelFontSize(Label label, int parentWidthHint) {
        int widthMargin = 100;
        int defaultWidth = getTextWidth(label.getText(), 0);
        int widthDifference = parentWidthHint - defaultWidth - widthMargin;
        int commonDifference = getCommonDifference(label.getText());

        int times = 1;
        if (widthDifference > 0) {
            times = widthDifference / commonDifference;
        }

        label.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                times > 20 ? 20 : times));
    }

    private int getCommonDifference(String text) {
        if (text == null || "".equals(text)) //$NON-NLS-1$
            return 1;

        int width1 = getTextWidth(text, 1);
        int width0 = getTextWidth(text, 0);
        int difference = width1 - width0;
        return difference > 0 ? difference : 1;
    }

    private int getTextWidth(String text, int fontHeight) {
        Font font = FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                fontHeight);
        return GraphicsUtils.getAdvanced().getTextSize(text, font).width();
    }

    private void createShareArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 15;
        composite.setLayout(gridLayout);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createShareAllArea(composite);
        createShareSelectArea(composite);
    }

    private void createShareAllArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        shareAll = new Button(composite, SWT.RADIO);
        shareAll.setText(SharingMessages.SendSharingMapDialog_ShareAllRadioButton_text);
        shareAll.setSelection(SHARE_ALL.equals(getShareMode()));
        shareAll.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                updateButtonStatus(SHARE_ALL);
            }
        });
    }

    private void createShareSelectArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        shareSelect = new Button(composite, SWT.RADIO);
        shareSelect
                .setText(SharingMessages.SendSharingMapDialog_ShareSelectRadioButton_text);
        shareSelect.setSelection(SHARE_SELECT.equals(getShareMode()));
        shareSelect.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                updateButtonStatus(SHARE_SELECT);
            }
        });

        Composite scrolledParent = new Composite(composite, SWT.NONE);
        GridLayout scrolledParentLayout = new GridLayout(1, false);
        scrolledParentLayout.marginWidth = 20;
        scrolledParent.setLayout(scrolledParentLayout);
        scrolledParent.setLayoutData(composite.getLayoutData());

        createScrolledSelectArea(scrolledParent);
        createScrolledAlreadySharedArea(scrolledParent);
    }

    private ScrolledComposite createScrolledComposite(Composite parent,
            int height) {
        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.BORDER
                | SWT.V_SCROLL);

        GridData newPageData = new GridData(GridData.FILL_HORIZONTAL);
        scrolled.setLayoutData(newPageData);

        Composite composite = new Composite(scrolled, SWT.NONE);

        scrolled.setContent(composite);
        scrolled.setExpandVertical(true);
        scrolled.setExpandHorizontal(true);
        scrolled.setAlwaysShowScrollBars(true);
        scrolled.setMinHeight(height);

        composite.setLayout(new GridLayout(3, false));
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(data);
        composite.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));

        return scrolled;
    }

    private void createScrolledSelectArea(Composite parent) {
        selectScrolled = createScrolledComposite(parent, 100);
        Composite composite = (Composite) selectScrolled.getContent();

        selectAllButton = new Button(composite, SWT.CHECK);
        selectAllButton.setBackground(composite.getBackground());
        selectAllButton
                .setText(SharingMessages.SendSharingMapDialog_SelectAllButton_text);
        GridData selectAllData = new GridData();
        selectAllData.horizontalSpan = 3;
        selectAllButton.setLayoutData(selectAllData);
        selectAllButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                boolean selection = selectAllButton.getSelection();
                for (Button b : contactButtons) {
                    b.setSelection(selection);
                    if (selection && !selectedButtons.contains(b)) {
                        selectedButtons.add(b);
                    }
                }

                if (selection) {
                    receiverSize = contactButtons.size();
                } else {
                    receiverSize = 0;
                    selectedButtons.clear();
                }

                updateSelectAllButton();
                updateOKButton();
            }
        });

        Map<String, IRemoteSharedLibrary> remoteLibrarys = new HashMap<String, IRemoteSharedLibrary>();
        for (IRemoteSharedLibrary remote : sharingService.getRemoteLibraries()) {
            String id = remote.getContactID();
            if (id != null && !"".equals(id)) //$NON-NLS-1$
                remoteLibrarys.put(id, remote);
        }

        List<String> alreadyReceivers = null;
        if (map != null)
            alreadyReceivers = map.getReceiverIDs();

        for (ISharedContact contact : sharingService.getContactManager()
                .getContacts()) {
            if (alreadyReceivers != null && !alreadyReceivers.isEmpty()) {
                String contactdID = contact.getID();
                if (alreadyReceivers.contains(contactdID))
                    continue;
            }

            IRemoteSharedLibrary remote = remoteLibrarys.get(contact.getID());
            if (remote == null)
                continue;

            String remoteName = remote.getName();
            if (remoteName == null || "".equals(remoteName)) //$NON-NLS-1$
                continue;

            final Button button = new Button(composite, SWT.CHECK);
            button.setBackground(composite.getBackground());
            button.setText(remoteName);
            button.setData(REMOTE_LIBRARY, remote);
            button.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    boolean selection = button.getSelection();
                    if (selection) {
                        if (!selectedButtons.contains(button)) {
                            selectedButtons.add(button);
                            receiverSize++;
                        }
                    } else {
                        if (selectedButtons.contains(button)) {
                            selectedButtons.remove(button);
                            receiverSize--;
                        }
                    }

                    updateSelectAllButton();
                    updateOKButton();
                }
            });
            contactButtons.add(button);
        }
    }

    private void createScrolledAlreadySharedArea(Composite parent) {
        if (map == null)
            return;

        List<String> receiverIDs = map.getReceiverIDs();
        if (receiverIDs.isEmpty())
            return;

        Label label = new Label(parent, SWT.NONE);
        label.setText(SharingMessages.SendSharingMapDialog_alreadySharedLabel);
        alreadySharedScrolled = createScrolledComposite(parent, 50);
        Composite composite = (Composite) alreadySharedScrolled.getContent();

        for (String id : receiverIDs) {
            ISharedContact c = sharingService.getContactManager()
                    .findContactByID(id);

            if (c == null || c.getName() == null || "".equals(c.getName())) //$NON-NLS-1$
                continue;

            final Button button = new Button(composite, SWT.CHECK);
            button.setData(CONTACT_ID, id);
            button.setText(c.getName());
            button.setSelection(true);
            button.setBackground(composite.getBackground());
            button.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    boolean selection = button.getSelection();
                    if (selection && !selectedButtons.contains(button)) {
                        selectedButtons.add(button);
                    }

                    if (!selection && selectedButtons.contains(button)) {
                        selectedButtons.remove(button);
                    }

                    updateOKButton();
                }
            });
            alreadyButtons.add(button);
        }

        selectedButtons.addAll(alreadyButtons);
    }

    private void createOptionalMessageArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 20;
        layout.marginBottom = 0;
        composite.setLayout(layout);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label label = new Label(composite, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(SharingMessages.SendSharingMapDialog_SendMessageLabel_text);

        messageText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        messageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        messageText.setFocus();
    }

    private void updateButtonStatus(String type) {
        if (type == null)
            type = SHARE_ALL;

        if (SHARE_ALL.equals(type)) {
            shareAll.setSelection(true);
            shareSelect.setSelection(false);
            updateEnablement(selectScrolled, false);
            updateEnablement(alreadySharedScrolled, false);
        } else if (SHARE_SELECT.equals(type)) {
            shareSelect.setSelection(true);
            shareAll.setSelection(false);
            updateEnablement(selectScrolled, true);
            updateEnablement(alreadySharedScrolled, true);
        }
        selectShareMode = type;
        updateOKButton();
    }

    private void updateEnablement(Control control, boolean enabled) {
        if (control == null)
            return;

        control.setEnabled(enabled);
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (int i = 0; i < children.length; i++) {
                updateEnablement(children[i], enabled);
            }
        }
    }

    private void updateSelectAllButton() {
        if (receiverSize == 0)
            selectAllButton.setSelection(false);

        if (receiverSize == contactButtons.size() && contactButtons.size() > 0) {
            selectAllButton.setGrayed(false);
            selectAllButton.setSelection(true);
        }

        if (receiverSize < contactButtons.size() && receiverSize > 0) {
            selectAllButton.setGrayed(true);
            selectAllButton.setSelection(true);
        }
    }

    private void updateOKButton() {
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null) {
            button.setEnabled(false);
            if (SHARE_ALL.equals(selectShareMode)) {
                button.setEnabled(true);
                return;
            }

            if (SHARE_SELECT.equals(selectShareMode)) {
                if (selectAllButton.getSelection() && contactButtons.size() > 0) {
                    button.setEnabled(true);
                    return;
                }

                if (!(alreadyButtons.size() > 1))
                    return;

                for (Button b : alreadyButtons) {
                    if (!b.getSelection()) {
                        button.setEnabled(true);
                        return;
                    }
                }
            }
        }
    }

    public List<String> getReceivers() {
        return this.receivers;
    }

    @Override
    protected void okPressed() {
        message = messageText != null && !messageText.isDisposed() ? messageText
                .getText() : ""; //$NON-NLS-1$
        if (SHARE_SELECT.equals(selectShareMode)) {
            for (Button b : selectedButtons) {
                ISharedLibrary remote = (ISharedLibrary) b
                        .getData(REMOTE_LIBRARY);
                String contactID;
                if (remote != null) {
                    contactID = remote.getContactID();
                } else {
                    contactID = (String) b.getData(CONTACT_ID);
                }

                if (!receivers.contains(contactID) && contactID != null
                        && !"".equals(contactID)) { //$NON-NLS-1$
                    receivers.add(contactID);
                }
            }
        }

        super.okPressed();
    }

    public String getMessage() {
        return message == null ? "" : message.trim(); //$NON-NLS-1$
    }

    private String getShareMode() {
        if (map != null) {
            if (!map.getReceiverIDs().isEmpty()) {
                return SHARE_SELECT;
            }
        }
        return SHARE_ALL;
    }

}