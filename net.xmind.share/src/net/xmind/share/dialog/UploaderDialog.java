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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.xmind.share.Info;
import net.xmind.share.Messages;
import net.xmind.share.XmindSharePlugin;
import net.xmind.signin.XMindNetEntry;

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
        Properties currentInfo = XMindNetEntry.getCurrentUserInfo();
        if (isInvalidToken(userID, token, currentInfo)) {
            setReturnCode(CANCEL);
            close();
        }
    }

    private boolean isInvalidToken(String userID, String token,
            Properties currentInfo) {
        if (userID == null || token == null)
            return true;
        if (currentInfo == null)
            return true;
        return !userID.equals(currentInfo.getProperty(XMindNetEntry.USER_ID))
                || !token.equals(currentInfo.getProperty(XMindNetEntry.TOKEN));
    }

    private void ensurePages() {
        if (pages != null)
            return;

        pages = new ArrayList<IUploaderPage>();
        addPages();
    }

    protected void addPage(IUploaderPage page) {
        if (pages == null)
            return;

        pages.add(page);
        page.setContainer(this);
    }

    protected void addPages() {
        addPage(new GeneralUploaderPage());
        addPage(new ThumbnailUploaderPage());
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

    protected void okPressed() {
        getDialogSettings().put(Info.ALLOW_DOWNLOAD,
                getInfo().getString(Info.ALLOW_DOWNLOAD, Info.Public));
        super.okPressed();
    }

}