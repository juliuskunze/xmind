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
package org.xmind.ui.internal.views;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.internal.handlers.IMindMapCommandConstants;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * View tool bar, dropdown menu, popup menu groups:
 * <ol>
 * <li><code>group.file</code></li>
 * <li><code>group.open</code></li>
 * <li><code>group.showIn</code></li>
 * <li><code>group.edit</code></li>
 * <li><code>group.reorganize</code></li>
 * <li><code>group.generate</code></li>
 * <li><code>additions</code></li>
 * <li><code>group.properties</code></li>
 * </ol>
 * 
 * @author Frank Shaka
 */
public class StylesView extends ViewPart implements IContributedContentsView {

    private static final String GROUP_FILE = IWorkbenchActionConstants.GROUP_FILE;

    private static final String GROUP_OPEN = "group.open"; //$NON-NLS-1$

    private static final String GROUP_SHOW_IN = IWorkbenchActionConstants.GROUP_SHOW_IN;

    private static final String GROUP_EDIT = "group.edit"; //$NON-NLS-1$

    private static final String GROUP_REORGANIZE = IWorkbenchActionConstants.GROUP_REORGANIZE;

    private static final String GROUP_GENERATE = "group.generate"; //$NON-NLS-1$

    private static final String GROUP_PROPERTIES = "group.properties"; //$NON-NLS-1$

    private StylesViewer viewer;

    private MenuManager contextMenu;

    public void createPartControl(Composite parent) {
        viewer = new StylesViewer();
        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event
                        .getSelection();
                Object element = selection.getFirstElement();
                if (element instanceof IStyle) {
                    applyStyle((IStyle) element);
                }
            }
        });
        viewer.createControl(parent, SWT.NONE);

        contextMenu = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        configurePopupMenu(contextMenu);
        viewer.getControl().setMenu(
                contextMenu.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(contextMenu, viewer);

        IActionBars actionBars = getViewSite().getActionBars();
        configureToolBar(actionBars.getToolBarManager());
        configureMenu(actionBars.getMenuManager());
        actionBars.updateActionBars();

        getSite().setSelectionProvider(viewer);

        viewer.setInput(new IStyleSheet[] {
                MindMapUI.getResourceManager().getSystemStyleSheet(),
                MindMapUI.getResourceManager().getUserStyleSheet() });
    }

    protected void configureToolBar(IToolBarManager toolBar) {
        toolBar.add(new Separator(GROUP_FILE));
        toolBar.add(new Separator(GROUP_OPEN));
        toolBar.add(new GroupMarker(GROUP_SHOW_IN));
        toolBar.add(new Separator(GROUP_EDIT));
        toolBar.add(new Separator(GROUP_REORGANIZE));
        toolBar.add(new Separator(GROUP_GENERATE));
        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBar.add(new Separator(GROUP_PROPERTIES));
    }

    protected void configureMenu(IMenuManager menu) {
        menu.add(new Separator(GROUP_FILE));
        menu.add(new Separator(GROUP_OPEN));
        menu.add(new GroupMarker(GROUP_SHOW_IN));
        menu.add(new Separator(GROUP_EDIT));
        menu.add(new Separator(GROUP_REORGANIZE));
        menu.add(new Separator(GROUP_GENERATE));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator(GROUP_PROPERTIES));
    }

    protected void configurePopupMenu(MenuManager menu) {
        menu.add(new Separator(GROUP_FILE));
        menu.add(new Separator(GROUP_OPEN));
        menu.add(new GroupMarker(GROUP_SHOW_IN));
        menu.add(new Separator(GROUP_EDIT));
        menu.add(new Separator(GROUP_REORGANIZE));
        menu.add(new Separator(GROUP_GENERATE));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator(GROUP_PROPERTIES));
    }

    public StylesViewer getViewer() {
        return viewer;
    }

    public void setFocus() {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    public void dispose() {
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }
        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null) {
                control.dispose();
            }
            viewer = null;
        }
        super.dispose();
    }

    public IWorkbenchPart getContributingPart() {
        return getSite().getPage().getActiveEditor();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        } else if (adapter.isAssignableFrom(StylesViewer.class)) {
            return viewer;
        }
        return super.getAdapter(adapter);
    }

    private void applyStyle(IStyle style) {
        if (style == null)
            return;

        final ICommandService cs = (ICommandService) getSite().getService(
                ICommandService.class);
        final IHandlerService hs = (IHandlerService) getSite().getService(
                IHandlerService.class);
        if (cs == null || hs == null)
            return;

        final Command command = cs
                .getCommand(IMindMapCommandConstants.STYLE_APPLY);
        if (command == null || !command.isDefined() || !command.isEnabled()
                || !command.isHandled())
            return;

        final String resourceURI = MindMapUI.getResourceManager()
                .toResourceURI(style);
        if (resourceURI == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IParameter resourceURIParam = command
                        .getParameter(IMindMapCommandConstants.RESOURCE_URI);
                if (resourceURIParam == null)
                    return;

                hs.executeCommand(new ParameterizedCommand(command,
                        new Parameterization[] { new Parameterization(
                                resourceURIParam, resourceURI) }), null);
            }
        });
    }

}