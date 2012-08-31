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

import java.util.ArrayList;
import java.util.List;

import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.share.XmindSharePlugin;
import net.xmind.signin.IAccountInfo;
import net.xmind.signin.XMindNet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class UploaderDialog extends TitleAreaDialog implements
        IUploaderPageContainer {

    private static final String SECTION_NAME = "net.xmind.share.UploadDialog"; //$NON-NLS-1$

    private Info info;

    private TabFolder tabFolder;

    private List<IUploaderPage> pages;

    private List<String> pageIds;

    public UploaderDialog(Shell parentShell) {
        super(parentShell);
        setBlockOnOpen(true);
        setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
    }

    public IDialogSettings getDialogSettings() {
        IDialogSettings ds = XmindSharePlugin.getDefault().getDialogSettings();
        IDialogSettings section = ds.getSection(SECTION_NAME);
        if (section == null) {
            section = ds.addNewSection(SECTION_NAME);
        }
        return section;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        if (id == OK)
            label = Messages.UploaderDialog_Upload_text;
        return super.createButton(parent, id, label, defaultButton);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.UploaderDialog_windowTitle);
        newShell.addShellListener(new ShellAdapter() {
            public void shellActivated(ShellEvent e) {
                super.shellActivated(e);
                checkUserInfoValidity();
            }
        });
    }

    private void checkUserInfoValidity() {
        String userID = info.getString(Info.USER_ID);
        String token = info.getString(Info.TOKEN);
        IAccountInfo accountInfo = XMindNet.getAccountInfo();
        if (isInvalidToken(userID, token, accountInfo)) {
            setReturnCode(CANCEL);
            close();
        }
    }

    private boolean isInvalidToken(String userID, String token,
            IAccountInfo currentInfo) {
        if (userID == null || token == null)
            return true;
        if (currentInfo == null)
            return true;
        return !userID.equals(currentInfo.getUser())
                || !token.equals(currentInfo.getAuthToken());
    }

    private void ensurePages() {
        if (pages != null)
            return;

        pages = new ArrayList<IUploaderPage>();
        pageIds = new ArrayList<String>();
        addPages();
    }

    protected void addPage(String pageId, IUploaderPage page) {
        if (pages == null)
            return;

        pages.add(page);
        pageIds.add(pageId);
        page.setContainer(this);
    }

    protected void addPages() {
        addPage("org.xmind.ui.uploader.general", new GeneralUploaderPage()); //$NON-NLS-1$
        addPage("org.xmind.ui.uploader.privacy", new PrivacyUploaderPage()); //$NON-NLS-1$
        addPage("org.xmind.ui.uploader.thumbnail", new ThumbnailUploaderPage()); //$NON-NLS-1$
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 5;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        tabFolder = new TabFolder(container, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        ensurePages();

        for (int i = 0; i < pages.size(); i++) {
            IUploaderPage page = pages.get(i);
            page.createControl(tabFolder);
            Control control = page.getControl();
            Assert.isNotNull(control);
            TabItem item = new TabItem(tabFolder, SWT.NONE);
            item.setText(page.getTitle());
            item.setToolTipText(page.getDescription());
            item.setImage(page.getImage());
            item.setControl(control);
        }

        setFocus();

        tabFolder.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                setFocus();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        setTitle(Messages.UploaderDialog_title);
        setMessage(Messages.UploaderDialog_message);
        return composite;
    }

    private void setFocus() {
        IUploaderPage page = getActivePage();
        if (page != null) {
            page.setFocus();
        }
    }

    private IUploaderPage getActivePage() {
        if (tabFolder != null && !tabFolder.isDisposed()) {
            int index = tabFolder.getSelectionIndex();
            if (index >= 0) {
                return pages.get(index);
            }
        }
        return null;
    }

    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings global = XmindSharePlugin.getDefault()
                .getDialogSettings();
        String sectionId = getClass().getName();
        IDialogSettings ds = global.getSection(sectionId);
        if (ds == null) {
            ds = global.addNewSection(sectionId);
        }
        return ds;
    }

    public void updateMessage() {
        IUploaderPage page = getActivePage();
        if (page != null) {
            String errorMessage = page.getErrorMessage();
            if (errorMessage != null) {
                setErrorMessage(errorMessage);
            } else {
                setMessage(page.getMessage());
            }
        }
    }

    public void showPage(String pageId) {
        if (pageId == null || pages == null || pageIds == null)
            return;
        if (tabFolder == null || tabFolder.isDisposed())
            return;
        int index = pageIds.indexOf(pageId);
        if (index < 0)
            return;
        tabFolder.setSelection(index);
    }

    protected void okPressed() {
        getDialogSettings().put(Info.PRIVACY,
                getInfo().getString(Info.PRIVACY, Info.PRIVACY_PUBLIC));
        getDialogSettings().put(Info.DOWNLOADABLE,
                getInfo().getString(Info.DOWNLOADABLE, Info.DOWNLOADABLE_YES));
        super.okPressed();
    }

}