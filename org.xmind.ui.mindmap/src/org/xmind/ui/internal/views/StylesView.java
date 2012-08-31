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

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.MindMapUI;

public class StylesView extends ViewPart implements IContributedContentsView {

    private StylesViewer viewer;

    public void createPartControl(Composite parent) {
        StackLayout layout = new StackLayout();
        parent.setLayout(layout);
        viewer = new StylesViewer(parent);
        viewer.setInput(new IStyleSheet[] {
                MindMapUI.getResourceManager().getSystemStyleSheet(),
                MindMapUI.getResourceManager().getUserStyleSheet() });
        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event
                        .getSelection();
                Object element = selection.getFirstElement();
                if (element instanceof IStyle) {
                    changeStyle((IStyle) element);
                }
            }
        });
        layout.topControl = viewer.getControl();

        getSite().setSelectionProvider(viewer);
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
        super.dispose();
        viewer = null;
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

    private void changeStyle(IStyle style) {
        IEditorPart activeEditor = getSite().getPage().getActiveEditor();
        if (!(activeEditor instanceof IGraphicalEditor))
            return;

        IGraphicalEditor editor = (IGraphicalEditor) activeEditor;
        IGraphicalEditorPage page = editor.getActivePageInstance();
        if (page == null)
            return;

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null)
            return;

        EditDomain editDomain = page.getEditDomain();
        if (editDomain == null)
            return;

        editDomain.handleRequest(new Request(MindMapUI.REQ_MODIFY_STYLE)
                .setViewer(viewer)
                .setParameter(MindMapUI.PARAM_RESOURCE, style));
    }

}