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
package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;

public class ThemesView extends ViewPart implements IContributedContentsView,
        IPartListener, IPageChangedListener, ICoreEventListener {

    private static final String KEY_LINK_TO_EDITOR = "LINK_TO_EDITOR"; //$NON-NLS-1$

    private class ToggleLinkEditorAction extends Action {
        public ToggleLinkEditorAction() {
            super(MindMapMessages.ThemesView_LinkWithEditor_text, AS_CHECK_BOX);
            setToolTipText(MindMapMessages.ThemesView_LinkWithEditor_toolTip);
            setImageDescriptor(MindMapUI.getImages().get(IMindMapImages.SYNCED,
                    true));
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.SYNCED, false));
            setChecked(isLinkingToEditor());
        }

        public void run() {
            setLinkingToEditor(isChecked());
        }
    }

    private class ChangeThemeListener implements IOpenListener {
        public void open(OpenEvent event) {
            if (updatingSelection)
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o != null && o instanceof IStyle) {
                changeTheme((IStyle) o);
            }
        }
    }

    private IGraphicalEditor activeEditor;

    private ThemesViewer viewer;

    private IDialogSettings dialogSettings;

    private boolean linkingToEditor;

    private boolean updatingSelection = false;

    private ICoreEventRegister register = null;

    public void init(IViewSite site) throws PartInitException {
        super.init(site);
    }

    public ThemesViewer getViewer() {
        return viewer;
    }

    public void createPartControl(Composite parent) {
        dialogSettings = MindMapUIPlugin.getDefault().getDialogSettings(
                getClass().getName());
        if (dialogSettings.get(KEY_LINK_TO_EDITOR) == null) {
            dialogSettings.put(KEY_LINK_TO_EDITOR, true);
        }
        linkingToEditor = dialogSettings != null
                && dialogSettings.getBoolean(KEY_LINK_TO_EDITOR);

        viewer = new ThemesViewer(parent);
        viewer.setInput(getViewerInput());
        viewer.addOpenListener(new ChangeThemeListener());

        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor instanceof IGraphicalEditor) {
            setActiveEditor((IGraphicalEditor) editor);
        }

        ToggleLinkEditorAction toggleLinkingAction = new ToggleLinkEditorAction();
        IToolBarManager toolBar = getViewSite().getActionBars()
                .getToolBarManager();
        toolBar.add(toggleLinkingAction);
        toolBar.add(new Separator());
        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        menu.add(toggleLinkingAction);
        menu.add(new Separator());
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        getSite().setSelectionProvider(viewer);
        getSite().getPage().addPartListener(this);

        ICoreEventSupport ces = (ICoreEventSupport) MindMapUI
                .getResourceManager().getUserThemeSheet().getAdapter(
                        ICoreEventSupport.class);
        if (ces != null) {
            register = new CoreEventRegister(this);
            register.setNextSupport(ces);
            register.register(Core.StyleAdd);
            register.register(Core.StyleRemove);
            register.register(Core.Name);
        }
    }

    public void dispose() {
        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        getSite().getPage().removePartListener(this);
        getSite().setSelectionProvider(null);

        setActiveEditor(null);

        super.dispose();
        viewer = null;
        dialogSettings = null;
    }

    public void setFocus() {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    public IWorkbenchPart getContributingPart() {
        return getSite().getPage().getActiveEditor();
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        }
        return super.getAdapter(adapter);
    }

    private boolean isLinkingToEditor() {
        return linkingToEditor;
    }

    private void setLinkingToEditor(boolean linking) {
        if (linking == this.linkingToEditor)
            return;

        this.linkingToEditor = linking;
        if (dialogSettings != null) {
            dialogSettings.put(KEY_LINK_TO_EDITOR, linking);
        }
        if (linking)
            updateSelection();
    }

    private Object getViewerInput() {
        Set<IStyle> systemThemes = MindMapUI.getResourceManager()
                .getSystemThemeSheet().getStyles(IStyleSheet.MASTER_STYLES);
        Set<IStyle> userThemes = MindMapUI.getResourceManager()
                .getUserThemeSheet().getStyles(IStyleSheet.MASTER_STYLES);
        List<IStyle> list = new ArrayList<IStyle>(systemThemes.size()
                + userThemes.size() + 1);
        list.add(MindMapUI.getResourceManager().getDefaultTheme());
        list.addAll(systemThemes);
        list.addAll(userThemes);
        return list;
    }

    private void changeTheme(IStyle theme) {
        if (activeEditor == null)
            return;

        IGraphicalEditorPage page = activeEditor.getActivePageInstance();
        if (page == null)
            return;

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null)
            return;

        ISheetPart sheetPart = (ISheetPart) viewer.getAdapter(ISheetPart.class);
        if (sheetPart == null)
            return;

        EditDomain domain = page.getEditDomain();
        if (domain == null)
            return;

        domain.handleRequest(new Request(MindMapUI.REQ_MODIFY_THEME)
                .setPrimaryTarget(sheetPart).setParameter(
                        MindMapUI.PARAM_RESOURCE, theme));
        updateSelection();
    }

    private void updateSelection() {
        if (!isLinkingToEditor())
            return;

        if (viewer == null || viewer.getControl().isDisposed())
            return;
        String themeId = getCurrentThemeId();
        IStyle theme;
        if (themeId == null) {
            theme = MindMapUI.getResourceManager().getDefaultTheme();
        } else {
            theme = MindMapUI.getResourceManager().getSystemThemeSheet()
                    .findStyle(themeId);
            if (theme == null) {
                theme = MindMapUI.getResourceManager().getUserThemeSheet()
                        .findStyle(themeId);
            }
        }
        updatingSelection = true;
        viewer.setSelection(theme == null ? StructuredSelection.EMPTY
                : new StructuredSelection(theme));
        updatingSelection = false;
    }

    private String getCurrentThemeId() {
        if (activeEditor != null) {
            IGraphicalEditorPage page = activeEditor.getActivePageInstance();
            if (page != null) {
                ISheet sheet = (ISheet) page.getAdapter(ISheet.class);
                if (sheet != null) {
                    return sheet.getThemeId();
                }
            }
        }
        return null;
    }

    public void partActivated(IWorkbenchPart part) {
        if (!(part instanceof IGraphicalEditor))
            return;

        setActiveEditor((IGraphicalEditor) part);
    }

    private void setActiveEditor(IGraphicalEditor editor) {
        if (editor == this.activeEditor)
            return;

        if (this.activeEditor != null) {
            unhook(this.activeEditor);
        }
        this.activeEditor = editor;
        if (editor != null) {
            hook(editor);
        }
        updateSelection();
    }

    private void hook(IGraphicalEditor editor) {
        editor.addPageChangedListener(this);
    }

    private void unhook(IGraphicalEditor editor) {
        editor.removePageChangedListener(this);
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (part == this.activeEditor) {
            setActiveEditor(null);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void pageChanged(PageChangedEvent event) {
        updateSelection();
    }

    public void handleCoreEvent(CoreEvent event) {
        if (Core.Name.equals(event.getType())) {
            viewer.update(new Object[] { event.getSource() });
        } else {
            viewer.setInput(getViewerInput());
            viewer.setSelection(new StructuredSelection(event.getSource()),
                    true);
        }
    }

}