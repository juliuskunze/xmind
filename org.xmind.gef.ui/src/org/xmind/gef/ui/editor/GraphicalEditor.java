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
package org.xmind.gef.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.command.CommandStack;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStackListener;
import org.xmind.gef.ui.actions.ActionRegistry;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.actions.ICommandStackAction;
import org.xmind.ui.tabfolder.IPageClosedListener;

/**
 * @author Brian Sun
 */
public abstract class GraphicalEditor extends EditorPart implements
        IGraphicalEditor, ICommandStackListener {

    protected class PageSelectionProvider implements ISelectionProvider {

        private int selectedPage = -1;

        private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

        public int getSelectedPage() {
            return selectedPage;
        }

        public IGraphicalEditorPage getSelectedPageInstance() {
            return getPage(selectedPage);
        }

        public void addSelectionChangedListener(
                ISelectionChangedListener listener) {
            listeners.add(listener);
        }

        public ISelection getSelection() {
            IGraphicalEditorPage page = getSelectedPageInstance();
            if (page != null) {
                Object pageInput = page.getInput();
                if (pageInput != null) {
                    return new StructuredSelection(pageInput);
                }
            }
            return StructuredSelection.EMPTY;
        }

        public void removeSelectionChangedListener(
                ISelectionChangedListener listener) {
            listeners.remove(listener);
        }

        public void setSelection(ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                Object pageInput = ((IStructuredSelection) selection)
                        .getFirstElement();
                if (pageInput != null) {
                    ensurePageVisible(pageInput);
                }
            }
        }

        public void setSelectedPage(int index) {
            if (index == this.selectedPage)
                return;

            this.selectedPage = index;
            firePageChanged();
        }

        protected void firePageChanged() {
            fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
        }

        private void fireSelectionChanged(final SelectionChangedEvent event) {
            for (final Object o : listeners.toArray()) {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        ((ISelectionChangedListener) o).selectionChanged(event);
                    }
                });
            }
        }

    }

    private IPageContainerPresentation containerPresentation = null;

    private IPageChangedListener presentationHooker = null;

    private Composite container = null;

    private List<IGraphicalEditorPage> pages = new ArrayList<IGraphicalEditorPage>();

    private List<IPageChangedListener> pageChangedListeners = null;

    private List<IPageClosedListener> pageClosedListeners = null;

    private ICommandStack commandStack = null;

    private IMiniBarContributor miniBarContributor = null;

    private IActionRegistry actionRegistry = null;

    private List<ICommandStackAction> csActions = null;

    private int activePageIndex = -1;

    private PageSelectionProvider pageSelectionProvider = null;

    private MenuManager pagePopupMenu = null;

    protected Composite getContainer() {
        return container;
    }

    protected IPageContainerPresentation getContainerPresentation() {
        return containerPresentation;
    }

    protected void hookContainerPresentation() {
        if (presentationHooker == null) {
            presentationHooker = new IPageChangedListener() {
                public void pageChanged(PageChangedEvent event) {
                    handlePageChange(getActivePage());
                }
            };
        }
        getContainerPresentation().addPageChangedListener(presentationHooker);
    }

    public void createPartControl(Composite parent) {
        init();
        if (containerPresentation == null) {
            containerPresentation = createContainerPresentation();
            hookContainerPresentation();
        }
        Composite pageContainer = createPageContainer(parent);
        this.container = containerPresentation.createContainer(pageContainer);
        createEditorContents();
        createInitialPages();
    }

    protected void createEditorContents() {
        createMiniBar();
        createPageContextMenu(getContainer());
    }

    protected void createInitialPages() {
        createPages();
        if (getActivePage() == -1 && getPageCount() > 0) {
            setActivePage(0);
        }
    }

    protected IPageContainerPresentation createContainerPresentation() {
        return new TabFolderContainerPresentation();
    }

    /**
     * Creates the parent control for the container returned by
     * {@link #getContainer() }.
     * 
     * <p>
     * Subclasses may extend and must call super implementation first.
     * </p>
     * 
     * @param parent
     *            the parent for all of the editors contents.
     * @return the parent for this editor's container. Must not be
     *         <code>null</code>.
     */
    protected Composite createPageContainer(Composite parent) {
        return parent;
    }

    public void addPage(IGraphicalEditorPage page) {
        page.setEditDomain(createEditDomain(page));
        createPageControl(page);
        pages.add(page);
    }

    protected EditDomain createEditDomain(IGraphicalEditorPage page) {
        return new EditDomain();
    }

    private void createPageControl(IGraphicalEditorPage page) {
        page.createPageControl(getContainer());
        Assert.isNotNull(page.getControl());
        Assert.isNotNull(page.getViewer());
        Assert.isNotNull(page.getViewer().getControl());
        addPage(page.getControl());
        //createContentPopupMenuForPage(page, page.getViewer().getControl());
    }

    protected void createPageContextMenu(final Composite container) {
        if (pagePopupMenu == null) {
            pagePopupMenu = createPagePopupMenu();
            String menuId = getSite().getId() + ".page"; //$NON-NLS-1$
            initPagePopupMenu(pagePopupMenu);
            registerPagePopupMenu(menuId, pagePopupMenu);
        }
        Menu menu = pagePopupMenu.getMenu();
        if (menu == null || menu.isDisposed()) {
            menu = pagePopupMenu.createContextMenu(container);
            container.addListener(SWT.MouseDown, new Listener() {
                public void handleEvent(Event event) {
                    handleMouseDownOnContainer(container, event.x, event.y);
                }
            });
        }
        container.setMenu(menu);
    }

    protected void handleMouseDownOnContainer(Composite container, int x, int y) {
        if (containerPresentation == null || pageSelectionProvider == null)
            return;

        int selectedPage = containerPresentation.findPage(container, x, y);
        if (pageSelectionProvider != null) {
            pageSelectionProvider.setSelectedPage(selectedPage);
        }
    }

    protected void registerPagePopupMenu(String menuId, MenuManager menu) {
        getSite().registerContextMenu(menuId, menu, getPageSelectionProvider());
    }

    protected void initPagePopupMenu(MenuManager menu) {
        if (isPagePopupMenuDynamic()) {
            menu.setRemoveAllWhenShown(true);
            menu.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {
                    contributeToPagePopupMenu(manager);
                }
            });
        } else {
            contributeToPagePopupMenu(menu);
        }
    }

    protected void contributeToPagePopupMenu(IMenuManager menu) {
        IEditorActionBarContributor contributor = getEditorSite()
                .getActionBarContributor();
        if (contributor instanceof GraphicalEditorActionBarContributor) {
            ((GraphicalEditorActionBarContributor) contributor)
                    .contributeToPagePopupMenu(menu);
        }
    }

    protected boolean isPagePopupMenuDynamic() {
        return true;
    }

    protected MenuManager createPagePopupMenu() {
        return new MenuManager();
    }

    protected void addPage(Control pageControl) {
        int index = containerPresentation.getPageCount(getContainer());
        containerPresentation.addPage(getContainer(), index, pageControl);
    }

    public void removePage(IGraphicalEditorPage page) {
        removePage(findPage(page));
    }

    protected void removePage(int pageIndex) {
        Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
        boolean wasActivePage = pageIndex == getActivePage();
        IGraphicalEditorPage page = getPage(pageIndex);
        containerPresentation.disposePage(getContainer(), pageIndex);
        pages.remove(page);
        if (page != null) {
            page.dispose();
        }
        if (wasActivePage) {
            if (pageIndex == getPageCount())
                pageIndex--;
            setActivePage(pageIndex);
        }
        firePageClosed(page);
    }

    public IGraphicalEditorPage getPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= getPageCount())
            return null;
        return pages.get(pageIndex);
    }

    public int findPage(IGraphicalEditorPage page) {
        return pages.indexOf(page);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (getCommandStack() != null) {
            getCommandStack().markSaved();
        }
        fireDirty();
    }

    public int getPageCount() {
        return pages.size();
    }

    public boolean isDirty() {
        return getCommandStack() != null && getCommandStack().isDirty();
    }

    protected void init() {
        getSite().setSelectionProvider(createSelectionProvider());
        initCommandStack();
        installModelListener();
        initEditorActions(getActionRegistry());
    }

    /**
     * @return
     */
    protected ISelectionProvider createSelectionProvider() {
        return new MultiGraphicalPageSelectionProvider();
    }

    protected void initCommandStack() {
        setCommandStack(createCommandStack());
    }

    protected void initEditorActions(IActionRegistry actionRegistry) {
    }

    protected abstract void createPages();

    /**
     * Creates the mini bar on the part control.
     * <p>
     * <b>IMPORTANT:</b> This mini bar contribution relies on the fact that the
     * page container is a CTabFolder, so it may do nothing if the
     * implementation of this editor changes.
     * </p>
     * 
     * @see #getContainer()
     */
    protected final void createMiniBar() {
        if (!(getContainer() instanceof CTabFolder))
            return;

        MiniBar miniBar = new MiniBar() {
            public void updateBar() {
                super.updateBar();
                ToolBar control = ((ToolBarManager) getToolBarManager())
                        .getControl();
                if (control != null && !control.isDisposed()) {
                    CTabFolder tabFolder = (CTabFolder) getContainer();
                    updateMiniBarControl(tabFolder, control);
                }

            }
        };
        initializeMiniBar(miniBar);
        if (!miniBar.isEmpty()) {
            createMiniBarControl(miniBar, (CTabFolder) getContainer());
        }
    }

    /**
     * 
     * @param miniBar
     */
    private void initializeMiniBar(IMiniBar miniBar) {
        if (getMiniBarContributor() != null) {
            getMiniBarContributor().init(miniBar, this);
        }
    }

    /**
     * Creates the mini bar's control on the specified tab folder.
     * 
     * @param miniBar
     * @param tabFolder
     */
    private void createMiniBarControl(IMiniBar miniBar, CTabFolder tabFolder) {
        ToolBar control = ((ToolBarManager) miniBar.getToolBarManager())
                .createControl(tabFolder);
        updateMiniBarControl(tabFolder, control);
    }

    private void updateMiniBarControl(CTabFolder tabFolder, Control barControl) {
        int tabHeight = barControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        tabHeight = Math.max(tabHeight, tabFolder.getTabHeight());
        tabFolder.setTabHeight(tabHeight);
        tabFolder.setTopRight(barControl, SWT.RIGHT);
    }

    public IMiniBarContributor getMiniBarContributor() {
        return miniBarContributor;
    }

    public void setMiniBarContributor(IMiniBarContributor miniBarContributor) {
        this.miniBarContributor = miniBarContributor;
    }

    protected ICommandStack createCommandStack() {
        return new CommandStack();
    }

    public void handleCommandStackEvent(CommandStackEvent event) {
        if ((event.getStatus() & GEF.CS_POST_MASK) != 0
                || event.getStatus() == GEF.CS_UPDATED) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    fireDirty();
                }
            });
        }
    }

    /**
     * @param commandStack
     *            the commandStack to set
     */
    public void setCommandStack(ICommandStack commandStack) {
        ICommandStack oldCS = this.commandStack;
        if (commandStack == oldCS)
            return;

        this.commandStack = commandStack;
        commandStackChanged(oldCS, commandStack);
    }

    protected void commandStackChanged(ICommandStack oldCS, ICommandStack newCS) {
        if (oldCS != null) {
            unhookCommandStack(oldCS);
        }
        if (newCS != null) {
            hookCommandStack(newCS);
        }
        for (IGraphicalEditorPage page : getPages()) {
            EditDomain domain = page.getEditDomain();
            if (domain != null) {
                domain.setCommandStack(newCS);
            }
        }
        if (csActions != null) {
            for (ICommandStackAction action : csActions) {
                action.setCommandStack(newCS);
            }
        }
        fireDirty();
    }

    protected void hookCommandStack(ICommandStack cs) {
        cs.addCSListener(this);
    }

    protected void unhookCommandStack(ICommandStack cs) {
        cs.removeCSListener(this);
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ICommandStack.class)
            return getCommandStack();
        if (adapter == IActionRegistry.class)
            return getActionRegistry();
        if (adapter == IMiniBarContributor.class)
            return getMiniBarContributor();
        return super.getAdapter(adapter);
    }

    /**
     * 
     */
    protected void installModelListener() {
    }

    /**
     * 
     */
    protected void uninstallModelListener() {
    }

    public Object getSelectedPage() {
        return getActivePageInstance();
    }

    public IGraphicalEditorPage getActivePageInstance() {
        return getPage(getActivePage());
    }

    public void addPageChangedListener(IPageChangedListener listener) {
        if (pageChangedListeners == null)
            pageChangedListeners = new ArrayList<IPageChangedListener>();
        pageChangedListeners.add(listener);
    }

    public void removePageChangedListener(IPageChangedListener listener) {
        if (pageChangedListeners == null)
            return;
        pageChangedListeners.remove(listener);
        if (pageChangedListeners.isEmpty())
            pageChangedListeners = null;
    }

    protected void firePageChanged(Object newPage) {
        if (pageChangedListeners == null)
            return;
        final PageChangedEvent event = new PageChangedEvent(this, newPage);
        for (final Object l : pageChangedListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IPageChangedListener) l).pageChanged(event);
                }
            });
        }
    }

    public void addPageClosedListener(IPageClosedListener listener) {
        if (pageClosedListeners == null)
            pageClosedListeners = new ArrayList<IPageClosedListener>();
        pageClosedListeners.add(listener);
    }

    public void removePageClosedListener(IPageClosedListener listener) {
        if (pageClosedListeners == null)
            return;
        pageClosedListeners.remove(listener);
        if (pageClosedListeners.isEmpty())
            pageClosedListeners = null;
    }

    protected void firePageClosed(final Object pageInstance) {
        if (pageClosedListeners == null)
            return;
        for (final Object listener : pageClosedListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IPageClosedListener) listener).pageClosed(pageInstance);
                }
            });
        }
    }

    protected void fireDirty() {
        firePropertyChange(PROP_DIRTY);
    }

    protected void handlePageChange(int newPageIndex) {
        boolean wasFocused = false;
        IGraphicalEditorPage oldActivePage = getPage(activePageIndex);
        if (oldActivePage != null && oldActivePage.isActive()) {
            wasFocused = oldActivePage.isFocused();
            oldActivePage.setActive(false);
        }

        this.activePageIndex = newPageIndex;
        IGraphicalEditorPage activePage = getPage(newPageIndex);
        if (activePage != null && !activePage.isActive()) {
            activePage.setActive(true);
        }
        if (wasFocused) {
            activePage.setFocus();
        }

        IEditorActionBarContributor contributor = getEditorSite()
                .getActionBarContributor();
        if (contributor != null
                && contributor instanceof GraphicalEditorActionBarContributor) {
            ((GraphicalEditorActionBarContributor) contributor)
                    .setActivePage(activePage);
        }

        ISelectionProvider selectionProvider = getSite().getSelectionProvider();
        if (selectionProvider instanceof MultiGraphicalPageSelectionProvider) {
            ((MultiGraphicalPageSelectionProvider) selectionProvider)
                    .setActivePage(activePage);
        }
        if (getMiniBarContributor() != null) {
            getMiniBarContributor().setActivePage(activePage);
        }
        firePageChanged(activePage);
    }

    public void setFocus() {
        setFocus(getActivePage());
    }

    protected void setFocus(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= getPageCount()) {
            container.setFocus();
        } else {
            IGraphicalEditorPage page = getPage(pageIndex);
            if (page != null) {
                page.setFocus();
            } else {
                Control control = containerPresentation.getPageControl(
                        getContainer(), pageIndex);
                if (control != null && !control.isDisposed()) {
                    control.setFocus();
                }
            }
        }
    }

    public void movePageTo(int oldIndex, int newIndex) {
        boolean wasActive = oldIndex == getActivePage();
        pages.add(newIndex, pages.remove(oldIndex));
        for (int i = 0; i < pages.size(); i++) {
            IGraphicalEditorPage page = pages.get(i);
            boolean wasFocused = page.isFocused();
            containerPresentation.setPageControl(getContainer(), i, page
                    .getControl());
            if (wasFocused)
                page.setFocus();
            page.updatePageTitle();
        }
        if (wasActive) {
            pages.get(newIndex).getControl().setVisible(true);
            setActivePage(newIndex);
        }
    }

    public String getPageText(int pageIndex) {
        return containerPresentation.getPageText(getContainer(), pageIndex);
    }

    public void setPageText(int pageIndex, String text) {
        if (text == null)
            text = ""; //$NON-NLS-1$
        containerPresentation.setPageText(getContainer(), pageIndex, text);
    }

    public int getActivePage() {
        return containerPresentation.getActivePage(getContainer());
    }

    public void setActivePage(int pageIndex) {
        Assert.isTrue(pageIndex < getPageCount());
        if (pageIndex >= 0) {
            containerPresentation.setActivePage(getContainer(), pageIndex);
        } else {
            containerPresentation.setActivePage(getContainer(),
                    containerPresentation.getPageCount(getContainer()) - 1);
        }
        handlePageChange(pageIndex);
    }

    public IGraphicalEditorPage[] getPages() {
        return pages.toArray(new IGraphicalEditorPage[pages.size()]);
    }

    protected ISelectionProvider getPageSelectionProvider() {
        if (pageSelectionProvider == null) {
            pageSelectionProvider = new PageSelectionProvider();
        }
        return pageSelectionProvider;
    }

    /**
     * @return commandStack
     */
    public ICommandStack getCommandStack() {
        return commandStack;
    }

    protected IActionRegistry getActionRegistry() {
        if (actionRegistry == null)
            actionRegistry = new ActionRegistry();
        return actionRegistry;
    }

    @Override
    public void dispose() {
        if (csActions != null) {
            for (ICommandStackAction action : csActions) {
                action.setCommandStack(null);
            }
            csActions = null;
        }
        if (actionRegistry != null) {
            actionRegistry.dispose();
            actionRegistry = null;
        }
        if (pagePopupMenu != null) {
            pagePopupMenu.dispose();
            pagePopupMenu = null;
        }

        uninstallModelListener();

        if (commandStack != null && !commandStack.isDisposed()) {
            disposeCommandStack(commandStack);
            commandStack = null;
        }
        disposePages();
        super.dispose();
    }

    protected void disposeCommandStack(ICommandStack commandStack) {
        commandStack.dispose();
    }

    private void disposePages() {
        if (pages.isEmpty())
            return;

        for (final Object page : pages.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IGraphicalEditorPage) page).dispose();
                }
            });
        }
        pages.clear();
    }

    /**
     * @param sourceEvent
     * @return
     */
    public IGraphicalEditorPage findPage(Object input) {
        for (IGraphicalEditorPage page : getPages()) {
            Object pageInput = page.getInput();
            if (pageInput == input
                    || (input != null && input.equals(pageInput)))
                return page;
        }
        return null;
    }

    public IGraphicalEditorPage ensurePageVisible(Object input) {
        IGraphicalEditorPage page = findPage(input);
        if (page != null) {
            if (page != getActivePageInstance()) {
                setActivePage(page.getIndex());
                page = getActivePageInstance();
            }
        }
        return page;
    }

    public boolean navigateTo(Object input, Object... elements) {
        IGraphicalEditorPage page = ensurePageVisible(input);
        if (page != null) {
            if (elements == null)
                return true;
            ISelectionProvider viewer = page.getSelectionProvider();
            if (viewer != null) {
                viewer.setSelection(new StructuredSelection(elements));
                return true;
            }
        }
        return false;
    }

    protected void addCommandStackAction(ICommandStackAction action) {
        if (csActions == null)
            csActions = new ArrayList<ICommandStackAction>();
        csActions.add(action);
    }
}