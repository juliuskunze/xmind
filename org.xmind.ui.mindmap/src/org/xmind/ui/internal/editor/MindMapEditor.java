/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.osgi.framework.Bundle;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.actions.RedoAction;
import org.xmind.gef.ui.actions.UndoAction;
import org.xmind.gef.ui.editor.GraphicalEditor;
import org.xmind.gef.ui.editor.GraphicalEditorPagePopupPreviewHelper;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.editor.MultiGraphicalPageSelectionProvider;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.commands.MoveSheetCommand;
import org.xmind.ui.dialogs.SimpleInfoPopupDialog;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.actions.CreateSheetAction;
import org.xmind.ui.internal.actions.DeleteOtherSheetsAction;
import org.xmind.ui.internal.actions.DeleteSheetAction;
import org.xmind.ui.internal.actions.ShowPropertiesAction;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider;
import org.xmind.ui.internal.mindmap.MindMapEditDomain;
import org.xmind.ui.internal.outline.MindMapOutlinePage;
import org.xmind.ui.internal.properties.MindMapPropertySheetPage;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapPreviewBuilder;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.tabfolder.IPageMoveListener;
import org.xmind.ui.tabfolder.IPageTitleChangedListener;
import org.xmind.ui.tabfolder.PageMoveHelper;
import org.xmind.ui.tabfolder.PageTitleEditor;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.MindMapUtils;

public class MindMapEditor extends GraphicalEditor implements ISaveablePart2,
        ICoreEventListener, IPageMoveListener, IPageTitleChangedListener,
        IWorkbookReferrer {

    private static boolean PROMPT_COMPATIBILITY_WARNING = false;

    protected class MindMapEditorSelectionProvider extends
            MultiGraphicalPageSelectionProvider {

        @Override
        public void setSelection(ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                ISheet sheet = findSheet(((IStructuredSelection) selection)
                        .toArray());
                if (sheet != null) {
                    ensurePageVisible(sheet);
                }
            }
            super.setSelection(selection);
        }

        /**
         * @param array
         * @return
         */
        private ISheet findSheet(Object[] array) {
            for (Object o : array) {
                ISheet sheet = MindMapUtils.findSheet(o);
                if (sheet != null)
                    return sheet;
            }
            return null;
        }

        public void fireSelectionChanged() {
            fireSelectionChangedEvent(new SelectionChangedEvent(this,
                    getSelection()));
        }

    }

    private static class MindMapEditorPagePopupPreviewHelper extends
            GraphicalEditorPagePopupPreviewHelper {

        private static final int MIN_PREVIEW_WIDTH = 600;

        private static final int MIN_PREVIEW_HEIGHT = 600;

        public MindMapEditorPagePopupPreviewHelper(IGraphicalEditor editor,
                CTabFolder tabFolder) {
            super(editor, tabFolder);
        }

        protected Rectangle calcContentsBounds(IFigure contents,
                IGraphicalViewer viewer) {
            Rectangle bounds = super.calcContentsBounds(contents, viewer);
            int max = Math.max(bounds.width, bounds.height) + 50;

            int newWidth = bounds.width;
            if (newWidth < MIN_PREVIEW_WIDTH) {
                newWidth = MIN_PREVIEW_WIDTH;
            }
            if (newWidth < max) {
                newWidth = max;
            }

            if (newWidth != bounds.width) {
                int ex = (newWidth - bounds.width) / 2;
                Rectangle b = contents.getBounds();
                int right = bounds.x + bounds.width;
                bounds.x = Math.max(b.x, bounds.x - ex);
                bounds.width = Math.min(b.x + b.width, right + ex) - bounds.x;
            }

            int newHeight = bounds.height;
            if (newHeight < MIN_PREVIEW_HEIGHT) {
                newHeight = MIN_PREVIEW_HEIGHT;
            }
            if (newHeight < max) {
                newHeight = max;
            }
            if (newHeight != bounds.height) {
                int ex = (newHeight - bounds.height) / 2;
                Rectangle b = contents.getBounds();
                int bottom = bounds.y + bounds.height;
                bounds.y = Math.max(b.y, bounds.y - ex);
                bounds.height = Math.min(b.y + b.height, bottom + ex)
                        - bounds.y;
            }
            return bounds;
        }

    }

    private class EncryptionDailogPane extends DialogPane {

        private Text oldPasswordInputBox;

        private Text newPasswordInputBox;

        private Text verifyNewPasswordInputBox;

        private Label oldPasswordVerificationLabel;

        private Label newPasswordVerificationLabel;

        private Image doneIcon;

        private Image undoneIcon;

        private Image blankIcon;

        private Image getDoneIcon() {
            if (getContainer() == null || getContainer().isDisposed())
                return null;
            if (doneIcon == null || doneIcon.isDisposed()) {
                ImageDescriptor img = MindMapUI.getImages().get(
                        IMindMapImages.DONE, true);
                if (img != null) {
                    doneIcon = img.createImage(getContainer().getDisplay());
                }
            }
            return doneIcon;
        }

        private Image getUndoneIcon() {
            if (getContainer() == null || getContainer().isDisposed())
                return null;
            if (undoneIcon == null || undoneIcon.isDisposed()) {
                ImageDescriptor img = MindMapUI.getImages().get(
                        IMindMapImages.DONE, false);
                if (img != null) {
                    undoneIcon = img.createImage(getContainer().getDisplay());
                }
            }
            return undoneIcon;
        }

        private Image getBlankIcon() {
            if (getContainer() == null || getContainer().isDisposed())
                return null;
            if (blankIcon == null || blankIcon.isDisposed()) {
                ImageDescriptor img = MindMapUI.getImages().get(
                        IMindMapImages.BLANK);
                if (img != null) {
                    blankIcon = img.createImage(getContainer().getDisplay());
                }
            }
            return blankIcon;
        }

        @Override
        public void dispose() {
            if (doneIcon != null) {
                doneIcon.dispose();
                doneIcon = null;
            }
            if (blankIcon != null) {
                blankIcon.dispose();
                blankIcon = null;
            }
            super.dispose();
        }

        @Override
        protected Control createDialogContents(Composite parent) {
            Composite composite = (Composite) super
                    .createDialogContents(parent);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 20;
            gridLayout.horizontalSpacing = 0;
            composite.setLayout(gridLayout);

            createMessageArea(composite);
            createPasswordArea(composite);

            verify();

            return composite;
        }

        private void createMessageArea(Composite parent) {
            Composite area = new Composite(parent, SWT.NONE);
            area.setBackground(parent.getBackground());

            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            area.setLayoutData(gridData);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 10;
            area.setLayout(gridLayout);

            createMessageIcon(area);
            createMessageBoard(area);
        }

        private void createMessageIcon(Composite parent) {
            Label iconLabel = new Label(parent, SWT.NONE);
            iconLabel.setBackground(parent.getBackground());
            iconLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
                    false, false));
            Image image = getMessageIcon(iconLabel);
            iconLabel.setImage(image);
        }

        private Image getMessageIcon(Control control) {
            if (control == null)
                return null;
            ImageDescriptor image = MindMapUI.getImages().get(
                    IMindMapImages.LOCK, true);
            if (image != null)
                return image.createImage(control.getDisplay());
            return null;
        }

        private void createMessageBoard(Composite parent) {
            Text messageBoard = new Text(parent, SWT.READ_ONLY | SWT.MULTI
                    | SWT.WRAP);
            messageBoard.setBackground(parent.getBackground());
            applyFont(messageBoard);

            GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            messageBoard.setLayoutData(gridData);
            messageBoard
                    .setText(MindMapMessages.EncryptDialogPane_board_message);
        }

        private void createPasswordArea(Composite parent) {
            Composite area = new Composite(parent, SWT.NONE);
            area.setBackground(parent.getBackground());

            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            area.setLayoutData(gridData);

            GridLayout gridLayout = new GridLayout(3, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 5;
            gridLayout.horizontalSpacing = 3;
            area.setLayout(gridLayout);

            IWorkbook workbook = getWorkbook();
            if (workbook != null) {
                String oldPassword = workbook.getPassword();
                if (oldPassword != null && !"".equals(oldPassword)) { //$NON-NLS-1$
                    createOldPasswordInputBox(area);
                }
            }

            createNewPasswordInputBox(area);
            createVerifyPasswordInputBox(area);

            Listener verifyListener = new Listener() {
                public void handleEvent(Event event) {
                    verify();
                }
            };
            if (oldPasswordInputBox != null) {
                oldPasswordInputBox.addListener(SWT.Modify, verifyListener);
            }
            newPasswordInputBox.addListener(SWT.Modify, verifyListener);
            verifyNewPasswordInputBox.addListener(SWT.Modify, verifyListener);

        }

        private void createOldPasswordInputBox(Composite parent) {
            Label assistMessageBox = new Label(parent, SWT.WRAP);
            assistMessageBox.setBackground(parent.getBackground());
            assistMessageBox.setLayoutData(new GridData(SWT.FILL,
                    SWT.BEGINNING, true, false));
            ((GridData) assistMessageBox.getLayoutData()).horizontalSpan = 3;
            assistMessageBox
                    .setText(MindMapMessages.EncryptDialogPane_assist_message);

            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            label.setText(MindMapMessages.EncryptDialogPane_oldpassword_text);
            label.setBackground(parent.getBackground());
            applyFont(label);

            oldPasswordInputBox = new Text(parent, SWT.BORDER | SWT.PASSWORD
                    | SWT.SINGLE);
            applyFont(oldPasswordInputBox);

            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            oldPasswordInputBox.setLayoutData(gridData);

            hookText(oldPasswordInputBox);
            addRefreshDefaultButtonListener(oldPasswordInputBox);
            addTriggerDefaultButtonListener(oldPasswordInputBox,
                    SWT.DefaultSelection);

            oldPasswordVerificationLabel = new Label(parent, SWT.NONE);
            oldPasswordVerificationLabel.setBackground(parent.getBackground());
            oldPasswordVerificationLabel.setLayoutData(new GridData(SWT.END,
                    SWT.CENTER, false, false));
            oldPasswordVerificationLabel.setImage(getDoneIcon());

            Label sep = new Label(parent, SWT.NONE);
            sep.setBackground(parent.getBackground());
            sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            ((GridData) sep.getLayoutData()).horizontalSpan = 3;
        }

        private void createNewPasswordInputBox(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            String text;
            if (oldPasswordInputBox == null) {
                text = MindMapMessages.EncryptDialogPane_password_text;
            } else {
                text = MindMapMessages.EncryptDialogPane_newpassword_text;
            }
            label.setText(text);
            label.setBackground(parent.getBackground());
            applyFont(label);

            newPasswordInputBox = new Text(parent, SWT.BORDER | SWT.PASSWORD
                    | SWT.SINGLE);
            applyFont(newPasswordInputBox);

            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            newPasswordInputBox.setLayoutData(gridData);

            hookText(newPasswordInputBox);
            addRefreshDefaultButtonListener(newPasswordInputBox);
            addTriggerDefaultButtonListener(newPasswordInputBox,
                    SWT.DefaultSelection);

            Label blankIcon = new Label(parent, SWT.NONE);
            blankIcon.setBackground(parent.getBackground());
            blankIcon.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                    false));
            blankIcon.setImage(getBlankIcon());
        }

        private void createVerifyPasswordInputBox(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            label.setText(MindMapMessages.EncryptDialogPane_confirm_text);
            label.setBackground(parent.getBackground());
            applyFont(label);

            verifyNewPasswordInputBox = new Text(parent, SWT.BORDER
                    | SWT.PASSWORD | SWT.SINGLE);
            applyFont(verifyNewPasswordInputBox);

            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            verifyNewPasswordInputBox.setLayoutData(gridData);

            hookText(verifyNewPasswordInputBox);
            addRefreshDefaultButtonListener(verifyNewPasswordInputBox);
            addTriggerDefaultButtonListener(verifyNewPasswordInputBox,
                    SWT.DefaultSelection);

            newPasswordVerificationLabel = new Label(parent, SWT.NONE);
            newPasswordVerificationLabel.setBackground(parent.getBackground());
            newPasswordVerificationLabel.setLayoutData(new GridData(SWT.END,
                    SWT.CENTER, false, false));
            newPasswordVerificationLabel.setImage(getDoneIcon());
        }

        @Override
        protected void createButtonsForButtonBar(Composite buttonBar) {
            createButton(buttonBar, IDialogConstants.OK_ID,
                    IDialogConstants.OK_LABEL, true);
            createButton(buttonBar, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
            setOKButtonEnabled(false);
        }

        private void setOKButtonEnabled(boolean enabled) {
            Button button = getButton(IDialogConstants.OK_ID);
            if (button != null && !button.isDisposed()) {
                button.setEnabled(enabled);
            }
        }

        private void verify() {
            boolean oldPasswordVerified = false;
            IWorkbook workbook = getWorkbook();
            if (workbook != null) {
                String oldPassword = workbook.getPassword();
                if (oldPassword == null || "".equals(oldPassword)) { //$NON-NLS-1$
                    oldPasswordVerified = !"".equals(newPasswordInputBox.getText()); //$NON-NLS-1$
                } else if (oldPasswordInputBox != null) {
                    oldPasswordVerified = oldPassword != null
                            && oldPassword
                                    .equals(oldPasswordInputBox.getText());
                    oldPasswordVerificationLabel
                            .setImage(oldPasswordVerified ? getDoneIcon()
                                    : getUndoneIcon());
                }
            }
            boolean newPasswordVerified = ((oldPasswordInputBox != null //
                    || !"".equals(newPasswordInputBox.getText()))) //$NON-NLS-1$
                    && newPasswordInputBox.getText().equals(
                            verifyNewPasswordInputBox.getText());
            newPasswordVerificationLabel
                    .setImage(newPasswordVerified ? getDoneIcon()
                            : getUndoneIcon());
            setOKButtonEnabled(oldPasswordVerified && newPasswordVerified);
        }

        @Override
        protected boolean okPressed() {
            setPassword(newPasswordInputBox.getText());
            close();
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    if (parent == null || parent.isDisposed())
                        return;

                    doSave(new NullProgressMonitor());
                }
            });
            return true;
        }

        private void setPassword(String password) {
            if ("".equals(password)) { //$NON-NLS-1$
                password = null;
            }
            IWorkbook workbook = getWorkbook();
            if (workbook != null) {
                workbook.setPassword(password);
            }
        }

        @Override
        protected boolean cancelPressed() {
            close();
            return true;
        }

        private void close() {
            backCover.hideEncryptionDialog();
            hideBackCover();
        }

        @Override
        public void setFocus() {
            if (oldPasswordInputBox != null
                    && !oldPasswordInputBox.isDisposed()) {
                oldPasswordInputBox.setFocus();
            } else if (newPasswordInputBox != null
                    && !newPasswordInputBox.isDisposed()) {
                newPasswordInputBox.setFocus();
            }
        }

    }

    private class DecryptionDialogPane extends DialogPane {

        private Text messageBoard;

        private Text passwordInputBox;

        private Label iconLabel;

        @Override
        protected Control createDialogContents(Composite parent) {
            Composite composite = (Composite) super
                    .createDialogContents(parent);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 20;
            gridLayout.horizontalSpacing = 0;
            composite.setLayout(gridLayout);

            createMessageArea(composite);
            createPasswordInputBox(composite);
            return composite;
        }

        protected void createButtonsForButtonBar(Composite buttonBar) {
            createOkButton(buttonBar);
            createCloseButton(buttonBar);
        }

        private void createOkButton(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID,
                    IDialogConstants.OK_LABEL, true);
        }

        private void createCloseButton(Composite parent) {
            createButton(parent, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
        }

        private void createPasswordInputBox(Composite parent) {
            passwordInputBox = new Text(parent, SWT.BORDER | SWT.PASSWORD
                    | SWT.SINGLE);
            applyFont(passwordInputBox);

            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            passwordInputBox.setLayoutData(gridData);

            hookText(passwordInputBox);
            addRefreshDefaultButtonListener(passwordInputBox);
            addTriggerDefaultButtonListener(passwordInputBox,
                    SWT.DefaultSelection);
        }

        private void createMessageArea(Composite parent) {
            Composite area = new Composite(parent, SWT.NONE);
            area.setBackground(parent.getBackground());

            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            area.setLayoutData(gridData);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 10;
            area.setLayout(gridLayout);

            createIcon(area);
            createMessageBoard(area);
        }

        private void createIcon(Composite parent) {
            iconLabel = new Label(parent, SWT.NONE);
            iconLabel.setBackground(parent.getBackground());
            iconLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
                    false, false));
            Image image = getImage(iconLabel, false);
            iconLabel.setImage(image);
        }

        private Image getImage(Control control, boolean errorOrWarning) {
            if (errorOrWarning)
                return control.getDisplay().getSystemImage(SWT.ICON_ERROR);

            ImageDescriptor image = MindMapUI.getImages().get(
                    IMindMapImages.UNLOCK, true);
            if (image != null)
                return image.createImage(control.getDisplay());
            return null;
        }

        private void createMessageBoard(Composite parent) {
            messageBoard = new Text(parent, SWT.READ_ONLY | SWT.MULTI
                    | SWT.WRAP);
            messageBoard.setBackground(parent.getBackground());
            applyFont(messageBoard);

            GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            messageBoard.setLayoutData(gridData);
        }

        @Override
        protected boolean cancelPressed() {
            IWorkbenchPage page = getSite().getPage();
            page.closeEditor(MindMapEditor.this, false);
            return true;
        }

        protected boolean okPressed() {
            if (loadWorkbookJob != null) {
                loadWorkbookJob.notifyPassword(passwordInputBox.getText());
            }
            return true;
        }

        public void dispose() {
            super.dispose();
            passwordInputBox = null;
            messageBoard = null;
        }

        public void setFocus() {
            if (passwordInputBox != null && !passwordInputBox.isDisposed()) {
                passwordInputBox.setFocus();
            }
        }

        public void setContent(String message, boolean errorOrWarning) {
            if (messageBoard != null && !messageBoard.isDisposed()) {
                messageBoard.setText(message);
            }
            if (iconLabel != null) {
                iconLabel.setImage(getImage(iconLabel, errorOrWarning));
            }
            relayout();
        }

        @Override
        protected void escapeKeyPressed() {
            triggerButton(IDialogConstants.CLOSE_ID);
        }

    }

    private class ErrorDialogPane extends DialogPane {

        private Text summaryBoard;

        private Throwable error;

        private long time;

        private String title;

        @Override
        protected Control createDialogContents(Composite parent) {
            Composite composite = (Composite) super
                    .createDialogContents(parent);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 20;
            gridLayout.horizontalSpacing = 0;
            composite.setLayout(gridLayout);

            createSummaryBoard(composite);
            return composite;
        }

        @Override
        protected int getPreferredWidth() {
            return 500;
        }

        private void createSummaryBoard(Composite parent) {
            Composite box = new Composite(parent, SWT.NONE);
            box.setBackground(parent.getBackground());
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            box.setLayoutData(gridData);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 10;
            box.setLayout(gridLayout);

            createIcon(box);
            createSummaryBox(box);
        }

        private void createIcon(Composite parent) {
            Label iconLabel = new Label(parent, SWT.NONE);
            iconLabel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false,
                    true));
            iconLabel.setBackground(parent.getBackground());
            iconLabel.setImage(parent.getDisplay().getSystemImage(
                    SWT.ICON_ERROR));
        }

        private void createSummaryBox(Composite parent) {
            summaryBoard = new Text(parent, SWT.READ_ONLY | SWT.MULTI
                    | SWT.WRAP);
            summaryBoard.setBackground(parent.getBackground());
            applyFont(summaryBoard);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            summaryBoard.setLayoutData(gridData);
        }

        public void dispose() {
            super.dispose();
            summaryBoard = null;
        }

        public void setFocus() {
            if (summaryBoard != null && !summaryBoard.isDisposed()) {
                summaryBoard.setFocus();
            }
        }

        protected void createButtonsForButtonBar(Composite buttonBar) {
            createButton(buttonBar, IDialogConstants.OK_ID,
                    MindMapMessages.EncryptDialogPane_detailsButton_label,
                    false);
            createButton(buttonBar, IDialogConstants.CLOSE_ID,
                    IDialogConstants.CLOSE_LABEL, false);
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        }

        @Override
        protected boolean closePressed() {
            getSite().getPage().closeEditor(MindMapEditor.this, false);
            return true;
        }

        @Override
        protected boolean okPressed() {
            showDetails();
            return true;
        }

        private void showDetails() {
            if (error == null)
                return;

            new ErrorDetailsDialog(error, title, time).open();
        }

        public void setContent(Throwable error, String title, String message,
                long time) {
            this.error = error;
            this.time = time;
            this.title = title;
            Button detailsButton = getButton(IDialogConstants.OK_ID);
            if (detailsButton != null) {
                detailsButton.setEnabled(error != null);
            }
            if (summaryBoard != null) {
                summaryBoard.setText(NLS.bind(
                        MindMapMessages.ErrorDialogPane_summaryBoard_text,
                        new Object[] { message, error.getClass().getName(),
                                error.getLocalizedMessage() }));
            }
            relayout();
        }

        @Override
        protected void escapeKeyPressed() {
            triggerButton(IDialogConstants.CLOSE_ID);
        }

    }

    protected class MindMapEditorBackCover extends DialogPaneContainer {

        private Font bigFont;

        @Override
        public void createControl(Composite parent) {
            super.createControl(parent);
            createBigFont(parent.getDisplay());
        }

        private void createBigFont(Display display) {
            Font base = display.getSystemFont();
            FontData[] fontData = base.getFontData();
            int increment;
            if ((Util.isMac())
                    && System
                            .getProperty("org.eclipse.swt.internal.carbon.smallFonts") != null) { //$NON-NLS-1$
                increment = 3;
            } else {
                increment = 1;
            }
            for (FontData fd : fontData) {
                fd.setHeight(fd.getHeight() + increment);
            }
            this.bigFont = new Font(display, fontData);
        }

        @Override
        protected void handleDispose() {
            if (bigFont != null) {
                bigFont.dispose();
                bigFont = null;
            }
            super.handleDispose();
        }

        @Override
        protected void showDialog(DialogPane dialog) {
            if (dialog != null) {
                dialog.setDefaultFont(bigFont);
            }
            super.showDialog(dialog);
        }

        public void showPasswordDialog(String message, boolean errorOrWarning) {
            DecryptionDialogPane dialog = new DecryptionDialogPane();
            showDialog(dialog);
            dialog.setContent(message, errorOrWarning);
        }

        public void hidePasswordDialog() {
            if (getCurrentDialog() instanceof DecryptionDialogPane) {
                hideCurrentDialog();
            }
        }

        public void showErrorDialog(Throwable e, String title, String message,
                long time) {
            ErrorDialogPane dialog = new ErrorDialogPane();
            showDialog(dialog);
            dialog.setContent(e, title, message, time);
        }

        public void hideErrorDialog() {
            if (getCurrentDialog() instanceof ErrorDialogPane) {
                hideCurrentDialog();
            }
        }

        public void showEncryptionDialog() {
            EncryptionDailogPane dialog = new EncryptionDailogPane();
            showDialog(dialog);
        }

        public void hideEncryptionDialog() {
            if (getCurrentDialog() instanceof EncryptionDailogPane) {
                hideCurrentDialog();
            }
        }

    }

    private class LoadWorkbookJob extends Job implements IEncryptionHandler {

        private String password = null;

        private Object passwordLock = null;

        private boolean firstTry = true;

        private boolean closed = false;

        private IProgressMonitor progress;

        public LoadWorkbookJob() {
            super(NLS.bind(MindMapMessages.LoadWorkbookJob_text,
                    getEditorInput().getName()));
            //setSystem(true);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            this.progress = monitor;
            monitor.beginTask(null, 100);

            // Test password dialog
//            try {
//                retrievePassword();
//            } catch (CoreException e1) {
//                if (progress.isCanceled())
//                    return Status.CANCEL_STATUS;
//                e1.printStackTrace();
//                return Status.CANCEL_STATUS;
//            }

            if (workbookRef == null)
                return Status.CANCEL_STATUS;

            IStorage storage = workbookRef.createStorage();
            boolean wrongPassword;
            Throwable error = null;
            long errorTime = -1;
            do {
                wrongPassword = false;
                password = null;
                storage.clear();

                if (workbookRef == null)
                    return Status.CANCEL_STATUS;

                try {
                    workbookRef.loadWorkbook(storage, this, monitor);
                } catch (Throwable e) {
                    errorTime = System.currentTimeMillis();
                    if (e instanceof CoreException) {
                        CoreException coreEx = (CoreException) e;
                        int errType = coreEx.getType();
                        if (errType == Core.ERROR_CANCELLATION) {
                            return Status.CANCEL_STATUS;
                        }
                        if (errType == Core.ERROR_WRONG_PASSWORD) {
                            wrongPassword = true;
                        }
                    }
                    if (!wrongPassword) {
                        error = e;
                        Logger.log(e);
                    }
                    //e.printStackTrace();
                }
                firstTry = false;
            } while (wrongPassword);

            if (getWorkbook() == null) {
                if (error == null) {
                    error = new FileNotFoundException(getEditorInput()
                            .getName());
                }
                showErrorMessage(error, errorTime);
                return Status.CANCEL_STATUS;
            }

            //Test error dialog
//            try {
//                if ("".equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
//                    throw new IllegalArgumentException("Illegal arguments."); //$NON-NLS-1$
//                }
//            } catch (Throwable e) {
//                showErrorMessage(e, System.currentTimeMillis());
//                return Status.CANCEL_STATUS;
//            }

            hideBackCover();
            showWorkbook();

            return Status.OK_STATUS;
        }

        private void showErrorMessage(final Throwable error, final long time) {
            parent.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (parent == null || parent.isDisposed())
                        return;

                    ensureBackCover();
                    if (backCover == null || backCover.getControl() == null
                            || backCover.getControl().isDisposed())
                        return;

                    backCover
                            .showErrorDialog(
                                    error,
                                    MindMapMessages.LoadWorkbookJob_errorDialog_title,
                                    MindMapMessages.LoadWorkbookJob_errorDialog_message,
                                    time);
                }
            });
        }

        private void hideBackCover() {
            if (backCover != null) {
                parent.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        MindMapEditor.this.hideBackCover();
                    }
                });
            }
        }

        public String retrievePassword() throws CoreException {
            if (password == null) {
                try {
                    showPasswordPage();
                } catch (Throwable e) {
                    throw new CoreException(Core.ERROR_CANCELLATION, e);
                }

                passwordLock = new Object();
                synchronized (passwordLock) {
                    while (password == null) {
                        try {
                            passwordLock.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                        if (progress != null && progress.isCanceled()) {
                            throw new CoreException(Core.ERROR_CANCELLATION);
                        }
                    }
                }
                passwordLock = null;
                hidePasswordPage();
            }
            return password;
        }

        @Override
        protected void canceling() {
            super.canceling();
            if (passwordLock != null) {
                synchronized (passwordLock) {
                    passwordLock.notifyAll();
                }
            }
            if (!closed && !parent.isDisposed()) {
                parent.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if (parent == null || parent.isDisposed())
                            return;

                        getSite().getPage().closeEditor(MindMapEditor.this,
                                false);
                    }
                });
            }
        }

        private void showPasswordPage() {//
            parent.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (parent == null || parent.isDisposed())
                        return;

                    ensureBackCover();
                    if (backCover == null || backCover.getControl() == null
                            || backCover.getControl().isDisposed())
                        return;

                    if (progress != null) {
                        progress.worked(10);
                        progress
                                .subTask(MindMapMessages.LoadWorkbookJob_retrive_password_message);
//                        progress.subTask("Retrieving password");
                    }

                    String message;
                    if (firstTry) {
                        message = MindMapMessages.LoadWorkbookJob_firstTry_message;
//                        message = "This file seems to be protected. Enter the correct password to proceed:";
                    } else {
                        message = MindMapMessages.LoadWorkbookJob_moreTry_message;
//                        message = "Sorry, password is wrong or the file may be damaged. Try enter password again:";
                    }
                    backCover.showPasswordDialog(message, !firstTry);
                }
            });
        }

        private void hidePasswordPage() {
            if (backCover != null) {
                parent.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if (backCover == null)
                            return;

                        backCover.hidePasswordDialog();
                    }
                });
            }
        }

        public void notifyPassword(String password) {
            this.password = password;
            if (passwordLock != null) {
                synchronized (passwordLock) {
                    passwordLock.notifyAll();
                }
            }
        }

        public void notifyEditorClose() {
            closed = true;
            cancel();
        }

    }

    private WorkbookRef workbookRef = null;

    private ICoreEventRegister eventRegister = null;

    private PageTitleEditor pageTitleEditor = null;

    private PageMoveHelper pageMoveHelper = null;

    private IContentOutlinePage outlinePage = null;

    private IPropertySheetPage propertyPage = null;

//    private List<IPreSaveListener> preSavingListeners = null;

//    private MindMapPreviewBuilder previewBuilder = null;

    private MindMapFindReplaceOperationProvider findReplaceOperationProvider = null;

    private EditorInputMonitor inputMonitor = null;

    private Composite parent = null;

    private StackLayout paneSwitcher = null;

    private Composite pageContainer = null;

    private LoadWorkbookJob loadWorkbookJob = null;

    private MindMapEditorBackCover backCover = null;

    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        if (this.workbookRef == null) {
            try {
                this.workbookRef = WorkbookRefManager.getInstance()
                        .addReferrer(input, this);
            } catch (org.eclipse.core.runtime.CoreException e) {
                throw new PartInitException(
                        NLS
                                .bind(
                                        MindMapMessages.MindMapEditor_partInitException_message,
                                        input), e);
            }
        }
        setSite(site);
        doSetInput(input);
    }

    protected void doSetInput(IEditorInput input) {
        super.setInput(input);
    }

    protected void initEditorActions(IActionRegistry actionRegistry) {
        super.initEditorActions(actionRegistry);

        UndoAction undoAction = new UndoAction(this);
        actionRegistry.addAction(undoAction);
        addCommandStackAction(undoAction);

        RedoAction redoAction = new RedoAction(this);
        actionRegistry.addAction(redoAction);
        addCommandStackAction(redoAction);

        CreateSheetAction createSheetAction = new CreateSheetAction(this);
        actionRegistry.addAction(createSheetAction);

        DeleteSheetAction deleteSheetAction = new DeleteSheetAction(this);
        actionRegistry.addAction(deleteSheetAction);

        DeleteOtherSheetsAction deleteOtherSheetAction = new DeleteOtherSheetsAction(
                this);
        actionRegistry.addAction(deleteOtherSheetAction);

        ShowPropertiesAction showPropertiesAction = new ShowPropertiesAction(
                getSite().getWorkbenchWindow());
        actionRegistry.addAction(showPropertiesAction);
    }

    protected ICommandStack createCommandStack() {
        return workbookRef.getCommandStack();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.ui.editor.GraphicalEditor#createSelectionProvider()
     */
    @Override
    protected ISelectionProvider createSelectionProvider() {
        return new MindMapEditorSelectionProvider();
    }

    public void dispose() {
        WorkbookRefManager.getInstance().removeReferrer(getEditorInput(), this);
        if (inputMonitor != null) {
            inputMonitor.dispose();
            inputMonitor = null;
        }
        if (propertyPage != null) {
            propertyPage.dispose();
            propertyPage = null;
        }
        if (outlinePage != null) {
            outlinePage.dispose();
            outlinePage = null;
        }
        if (loadWorkbookJob != null) {
            loadWorkbookJob.notifyEditorClose();
            loadWorkbookJob = null;
        }
        if (backCover != null) {
            backCover.dispose();
            backCover = null;
        }
        super.dispose();
        eventRegister = null;
        pageTitleEditor = null;
        pageMoveHelper = null;
        findReplaceOperationProvider = null;
        workbookRef = null;
        parent = null;
        paneSwitcher = null;
        pageContainer = null;
    }

    protected void disposeCommandStack(ICommandStack commandStack) {
        // No need to dispose command stack here, for the workbook reference
        // manager will dispose unused command stacks automatically.
    }

    protected Composite createPageContainer(Composite parent) {
        this.parent = parent;
        paneSwitcher = new StackLayout();
        parent.setLayout(paneSwitcher);

        pageContainer = new Composite(parent, SWT.NONE);
        paneSwitcher.topControl = pageContainer;
        return pageContainer;
    }

    protected void showPane(Control pane) {
        paneSwitcher.topControl = pane;
        parent.layout(true);
    }

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        if (getContainer() instanceof CTabFolder) {
            final CTabFolder tabFolder = (CTabFolder) getContainer();
            pageMoveHelper = new PageMoveHelper(tabFolder);
            pageMoveHelper.addListener(this);
            pageTitleEditor = new PageTitleEditor(tabFolder);
            pageTitleEditor.addPageTitleChangedListener(this);
            pageTitleEditor.setContextId(getSite(),
                    "org.xmind.ui.context.mindmap.textEdit"); //$NON-NLS-1$
            new MindMapEditorPagePopupPreviewHelper(this, tabFolder);
            tabFolder.addListener(SWT.MouseDoubleClick, new Listener() {
                public void handleEvent(Event event) {
                    CTabItem item = tabFolder.getItem(new Point(event.x,
                            event.y));
                    if (item == null)
                        createSheet();
                }
            });
        }
        MindMapEditorConfigurerManager.getInstance().configureEditor(this);

        inputMonitor = new EditorInputMonitor(this);
        if (getWorkbook() != null) {
            showWorkbook();
        } else if (loadWorkbookJob == null) {
            final LoadWorkbookJob theLoadJob = new LoadWorkbookJob();
            loadWorkbookJob = theLoadJob;
            loadWorkbookJob.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    loadWorkbookJob = null;
                }
            });
            loadWorkbookJob.schedule();
        }
        fireDirty();
    }

    @Override
    protected void createEditorContents() {
        setMiniBarContributor(new MindMapMiniBarContributor());
        super.createEditorContents();
    }

    private void showWorkbook() {
        installModelListener();
        parent.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (parent.isDisposed())
                    return;
                showPane(pageContainer);
                createInitialPages();
                if (getSite().getPage().getActiveEditor() == MindMapEditor.this) {
                    setFocus();
                }
                firePropertyChange(PROP_INPUT);
                fireDirty();
            }
        });
    }

    protected void createInitialPages() {
        updateNames();
        if (getWorkbook() == null)
            return;

        super.createInitialPages();
        for (IGraphicalEditorPage page : getPages()) {
            configurePage(page);
        }
    }

    private void configurePage(IGraphicalEditorPage page) {
        MindMapEditorConfigurerManager.getInstance().configurePage(page);
    }

    protected void createPages() {
        for (ISheet sheet : getWorkbook().getSheets()) {
            createSheetPage(sheet, -1);
        }
        if (getPageCount() > 0)
            setActivePage(0);

        checkWorkbookVersion();
    }

    private void checkWorkbookVersion() {
        if (!PROMPT_COMPATIBILITY_WARNING)
            return;

        String version = getWorkbook().getVersion();
        if (Core.getCurrentVersion().equals(version))
            return;

        final SimpleInfoPopupDialog popup = new SimpleInfoPopupDialog(getSite()
                .getShell(), DialogMessages.CompatibilityWarning_title,
                DialogMessages.CompatibilityWarning_message,
                SWT.ICON_INFORMATION);
        popup.popUp(getContainer());
        getContainer().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                popup.close();
            }
        });
    }

    protected IGraphicalEditorPage createSheetPage(ISheet sheet, int index) {
        IGraphicalEditorPage page = new MindMapEditorPage();
        page.init(this, sheet);
        addPage(page);
        page.updatePageTitle();
        if (index >= 0 && index < getPageCount()) {
            movePageTo(findPage(page), index);
        }
        if (getActivePage() != index) {
            setActivePage(index);
        }
        return page;
    }

    protected EditDomain createEditDomain(IGraphicalEditorPage page) {
        MindMapEditDomain domain = new MindMapEditDomain();
        domain.setCommandStack(getCommandStack());
        return domain;
    }

    protected void updateNames() {
        setPartName(getEditorInput().getName());
        setTitleToolTip(getEditorInput().getToolTipText());
    }

    public int promptToSaveOnClose() {
        return DEFAULT;
    }

    /**
     * 
     * @param fileName
     * @return 0 for Save As, 1 for Cancel, -1 for Save
     */
    private int promptWorkbookVersion(String fileName) {
        IWorkbook workbook = getWorkbook();
        if (Core.getCurrentVersion().equals(workbook.getVersion()))
            return -1;

        String messages = NLS.bind(
                DialogMessages.ConfirmWorkbookVersion_message, fileName);

        MessageDialog dialog = new MessageDialog(getSite().getShell(),
                DialogMessages.ConfirmWorkbookVersion_title, null, messages,
                MessageDialog.QUESTION, new String[] {
                        DialogMessages.ConfirmWorkbookVersion_SaveAs,
                        IDialogConstants.CANCEL_LABEL }, 0);
        return dialog.open();
    }

    public void doSave(final IProgressMonitor monitor) {
        if (!getEditorInput().exists() || !workbookRef.isSaveable()) {
            doSaveAs(monitor);
        } else {
            int ret = promptWorkbookVersion(getPartName());
            if (ret == 1)
                return;

            if (ret == 0) {
                doSaveAs(monitor);
            } else {
                BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
                    public void run() {
                        SafeRunner.run(new SafeRunnable() {
                            public void run() throws Exception {
                                workbookRef.saveWorkbook(monitor);
                            }
                        });
                    }
                });
            }
        }
    }

    public void doSaveAs(final IProgressMonitor monitor,
            String filterExtension, String filterName) {
        if (getWorkbook() == null) {
            monitor.setCanceled(true);
            return;
        }
        Bundle ide = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$
        if (ide != null) {
            saveAsResource(ide, monitor, filterExtension, filterName);
        } else {
            saveAsFile(monitor, filterExtension, filterName);
        }
    }

    private void saveAsFile(final IProgressMonitor monitor,
            String filterExtension, String filterName) {
        String path;
        String extension = filterExtension;
        String proposalName;
        File oldFile = MME.getFile(getEditorInput());
        if (oldFile != null) {
            proposalName = FileUtils.getNoExtensionFileName(oldFile.getName());
            path = oldFile.getParent();
        } else {
            String name = getWorkbook().getPrimarySheet().getRootTopic()
                    .getTitleText();
            proposalName = MindMapUtils.trimFileName(name);
            path = null;
        }

        final String result = DialogUtils.save(getSite().getShell(),
                proposalName, new String[] { "*" + extension }, //$NON-NLS-1$
                new String[] { filterName }, 0, path);
        if (result == null) {
            monitor.setCanceled(true);
            return;
        }

        BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
            public void run() {
                final String errorMessage = NLS.bind(
                        DialogMessages.FailedToSaveWorkbook_message, result);
                SafeRunner.run(new SafeRunnable(errorMessage) {
                    public void run() throws Exception {
                        workbookRef.saveWorkbookAs(MME
                                .createFileEditorInput(result), monitor);
                    }
                });
            }
        });
    }

    private void saveAsResource(Bundle ide, IProgressMonitor monitor,
            String filterExtension, String filterName) {
        // TODO 

    }

    protected void doSaveAs(final IProgressMonitor monitor) {
        doSaveAs(monitor, MindMapUI.FILE_EXT_XMIND,
                DialogMessages.WorkbookFilterName);
    }

    public void doSaveAs() {
        doSaveAs(new NullProgressMonitor());
    }

    public boolean isSaveAsAllowed() {
        return getWorkbook() != null;
    }

    public IWorkbookRef getWorkbookRef() {
        return workbookRef;
    }

    public IWorkbook getWorkbook() {
        if (workbookRef == null)
            return null;
        return workbookRef.getWorkbook();
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IContentOutlinePage.class) {
            if (outlinePage == null) {
                outlinePage = new MindMapOutlinePage(this, SWT.MULTI
                        | SWT.H_SCROLL | SWT.V_SCROLL);
            }
            return outlinePage;
        } else if (adapter == IPropertySheetPage.class) {
            if (propertyPage == null) {
                propertyPage = new MindMapPropertySheetPage(this);
            }
            return propertyPage;
        } else if (adapter == IWorkbookRef.class) {
            return getWorkbookRef();
        } else if (adapter == IWorkbook.class) {
            return getWorkbook();
        } else if (adapter == PageTitleEditor.class) {
            return pageTitleEditor;
        } else if (adapter == PageMoveHelper.class) {
            return pageMoveHelper;
        } else if (adapter == IFindReplaceOperationProvider.class) {
            if (findReplaceOperationProvider == null) {
                findReplaceOperationProvider = new MindMapFindReplaceOperationProvider(
                        this);
            }
            return findReplaceOperationProvider;
        }
        return super.getAdapter(adapter);
    }

    protected void installModelListener() {
        super.installModelListener();
        IWorkbook workbook = getWorkbook();
        if (workbook instanceof ICoreEventSource) {
            eventRegister = new CoreEventRegister((ICoreEventSource) workbook,
                    this);
            eventRegister.register(Core.SheetAdd);
            eventRegister.register(Core.SheetRemove);
            eventRegister.register(Core.SheetMove);
            eventRegister.register(Core.PasswordChange);
            eventRegister.register(Core.WorkbookPreSaveOnce);
        }
    }

    protected void uninstallModelListener() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
        }
        super.uninstallModelListener();
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.WorkbookPreSaveOnce.equals(type)) {
            fireDirty();
        } else if (Core.SheetAdd.equals(type)) {
            ISheet sheet = (ISheet) event.getTarget();
            int index = event.getIndex();
            IGraphicalEditorPage page = createSheetPage(sheet, index);
            configurePage(page);
        } else if (Core.SheetRemove.equals(type)) {
            ISheet sheet = (ISheet) event.getTarget();
            IGraphicalEditorPage page = findPage(sheet);
            if (page != null) {
                removePage(page);
            }
        } else if (Core.SheetMove.equals(type)) {
            int oldIndex = event.getIndex();
            int newIndex = ((ISheet) event.getTarget()).getIndex();
            movePageTo(oldIndex, newIndex);
        } else if (Core.PasswordChange.equals(type)) {
            IWorkbook workbook = getWorkbook();
            if (workbook instanceof ICoreEventSource2) {
                ((ICoreEventSource2) workbook).registerOnceCoreEventListener(
                        Core.WorkbookPreSaveOnce, ICoreEventListener.NULL);
            }
        }
    }

    public boolean isDirty() {
        if (workbookRef == null)
            return false;
        IWorkbook workbook = getWorkbook();
        if (workbook instanceof ICoreEventSource2
                && ((ICoreEventSource2) workbook)
                        .hasOnceListeners(Core.WorkbookPreSaveOnce)) {
            return true;
        }
        return super.isDirty();
    }

    protected void saveAndRun(Command command) {
        ICommandStack cs = getCommandStack();
        if (cs != null)
            cs.execute(command);
    }

    public void pageMoved(int fromIndex, int toIndex) {
        IWorkbook workbook = getWorkbook();
        MoveSheetCommand command = new MoveSheetCommand(workbook, fromIndex,
                toIndex);
        command.setLabel(CommandMessages.Command_MoveSheet);
        saveAndRun(command);
    }

    public void pageTitleChanged(int pageIndex, String newValue) {
        IGraphicalEditorPage page = getPage(pageIndex);
        if (page != null) {
            Object pageInput = page.getInput();
            if (pageInput instanceof ISheet) {
                ModifyTitleTextCommand command = new ModifyTitleTextCommand(
                        (ISheet) pageInput, newValue);
                command.setLabel(CommandMessages.Command_ModifySheetTitle);
                saveAndRun(command);
            }
        }
    }

    protected void createSheet() {
        IAction action = getActionRegistry().getAction(
                MindMapActionFactory.NEW_SHEET.getId());
        if (action != null && action.isEnabled()) {
            action.run();
        }
    }

    @Override
    public void setFocus() {
        if (workbookRef != null) {
            workbookRef.setPrimaryReferrer(this);
        }
        if (isShowingBackCover()) {
            backCover.setFocus();
        } else {
            super.setFocus();
        }
    }

    public boolean isShowingBackCover() {
        return backCover != null && backCover.getControl() != null
                && !backCover.getControl().isDisposed();
    }

    private void ensureBackCover() {
        if (backCover == null) {
            backCover = new MindMapEditorBackCover();
        }
        if (backCover.getControl() == null
                || backCover.getControl().isDisposed()) {
            backCover.createControl(parent);
        }
        showPane(backCover.getControl());
        setFocus();
    }

    private void hideBackCover() {
        if (backCover != null) {
            if (backCover.getControl() != null)
                backCover.getControl().dispose();
            backCover = null;
        }
        showPane(pageContainer);
        setFocus();
    }

    public void openEncryptionDialog() {
        if (parent == null || parent.isDisposed())
            return;
        ensureBackCover();
        if (!isShowingBackCover())
            return;
        backCover.showEncryptionDialog();
    }

    public ISelectionProvider getSelectionProvider() {
        return getSite().getSelectionProvider();
    }

    public void reveal() {
        getSite().getPage().activate(this);
        setFocus();
    }

    public void savePreivew(final IWorkbook workbook,
            final IProgressMonitor monitor) throws IOException, CoreException {
        if (workbook == null)
            throw new IllegalArgumentException();

        if (workbook.getPassword() != null) {
            URL url = BundleUtility.find(MindMapUI.PLUGIN_ID,
                    IMindMapImages.ENCRYPTED_THUMBNAIL);
            if (url != null) {
                new MindMapPreviewBuilder(workbook).saveFrom(url.openStream());
            }
        } else if (MindMapUIPlugin.getDefault().getPreferenceStore()
                .getBoolean(PrefConstants.PREVIEW_SKIPPED)) {
            URL url = BundleUtility.find(MindMapUI.PLUGIN_ID,
                    IMindMapImages.DEFAULT_THUMBNAIL);
            if (url != null) {
                new MindMapPreviewBuilder(workbook).saveFrom(url.openStream());
            }
        } else {
            final Composite parent = this.parent;
            if (parent != null && !parent.isDisposed()
                    && parent.getDisplay() != null
                    && !parent.getDisplay().isDisposed()) {
                final IOException[] ioe = new IOException[1];
                parent.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        Composite previewCanvas = new Composite(parent,
                                SWT.NONE);
                        try {
                            parent.layout(true);
                            new MindMapPreviewBuilder(workbook)
                                    .save(previewCanvas);
                        } catch (Throwable e) {
                            if (e instanceof IOException)
                                ioe[0] = (IOException) e;
                            Logger.log(e, "Failed to save preview image."); //$NON-NLS-1$
                        } finally {
                            previewCanvas.dispose();
                            parent.layout(true);
                        }
                    }
                });
                if (ioe[0] != null)
                    throw ioe[0];
            }
        }
    }

    public void postSave(IProgressMonitor monitor) {
        super.doSave(monitor);
    }

    public void postSaveAs(Object newKey, IProgressMonitor monitor) {
        if (newKey instanceof IEditorInput) {
            doSetInput((IEditorInput) newKey);
            firePropertyChange(PROP_INPUT);
        }
        super.doSave(monitor);
        updateNames();
    }

    public void setSelection(ISelection selection, boolean reveal,
            boolean forceFocus) {
        ISelectionProvider selectionProvider = getSite().getSelectionProvider();
        if (selectionProvider != null) {
            selectionProvider.setSelection(selection);
        }
        if (forceFocus) {
            getSite().getPage().activate(this);
            Shell shell = getSite().getShell();
            if (shell != null && !shell.isDisposed()) {
                shell.setActive();
            }
        } else if (reveal) {
            getSite().getPage().bringToTop(this);
        }
    }

    public IGraphicalEditorPage findPage(Object input) {
        if (input instanceof IMindMap) {
            input = ((IMindMap) input).getSheet();
        }
        return super.findPage(input);
    }

}