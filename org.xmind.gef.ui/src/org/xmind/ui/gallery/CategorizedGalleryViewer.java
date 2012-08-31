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
package org.xmind.ui.gallery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.viewers.CategorizedViewer;

public class CategorizedGalleryViewer extends CategorizedViewer {

    private Map<Object, GalleryViewer> viewers = new HashMap<Object, GalleryViewer>();

    private Properties properties = new Properties();

    private ISelectionChangedListener viewerSelectionChangedListener = new ISelectionChangedListener() {

        public void selectionChanged(SelectionChangedEvent event) {
            if (settingViewerSelections)
                return;

            fireSelectionChanged(new SelectionChangedEvent(
                    CategorizedGalleryViewer.this, getSelection()));
        }

    };

    private IOpenListener viewerOpenListener = new IOpenListener() {

        public void open(OpenEvent event) {
            fireOpen(new OpenEvent(CategorizedGalleryViewer.this,
                    getSelection()));
        }

    };

    private boolean settingViewerSelections = false;

    private Listener viewerControlListener = new Listener() {

        public void handleEvent(Event event) {
            if (event.type == SWT.FocusIn || event.type == SWT.FocusOut)
                updateFocus(event.display);
        }

    };

    public Properties getProperties() {
        return properties;
    }

    protected void refreshControls() {
        viewers.clear();
        super.refreshControls();
    }

    protected Control createSectionContent(Composite parent, Object category,
            List<Object> elements) {
        GalleryViewer viewer = createNestedViewer();
        configureNestedViewer(viewer);
        viewer.createControl(parent);
        hookViewerControl(viewer);

        viewer.setInput(elements);
        viewer.setSelection(StructuredSelection.EMPTY);

        viewers.put(category, viewer);
        return viewer.getControl();
    }

    protected GalleryViewer createNestedViewer() {
        return new GalleryViewer();
    }

    protected void configureNestedViewer(GalleryViewer viewer) {
        viewer.setProperties(properties);
        viewer.setLabelProvider(getLabelProvider());
        viewer.setFilters(getFilters());
        viewer.setSorter(getSorter());
        viewer.addSelectionChangedListener(viewerSelectionChangedListener);
        viewer.addOpenListener(viewerOpenListener);
    }

    protected void hookViewerControl(GalleryViewer viewer) {
        viewer.getCanvas().setScrollBarVisibility(FigureCanvas.NEVER);
    }

    protected void reveal(Object category, Object element) {
        super.reveal(category, element);
        GalleryViewer viewer = viewers.get(category);
        if (viewer != null) {
            viewer.reveal(new Object[] { element });
            IPart part = viewer.findPart(element);
            if (part != null && part instanceof IGraphicalPart) {
                IFigure fig = ((IGraphicalPart) part).getFigure();
                Point loc = viewer.computeToDisplay(fig.getBounds()
                        .getLocation(), true);
                reveal(loc.x, loc.y);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void fillSelection(Object category, List selection) {
        GalleryViewer viewer = viewers.get(category);
        if (viewer != null) {
            ISelection sel = viewer.getSelection();
            if (sel instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) sel;
                selection.addAll(ss.toList());
            }
        }
    }

    protected void setSelectionToCategory(Object category,
            ISelection selection, boolean reveal) {
        GalleryViewer viewer = viewers.get(category);
        if (viewer != null) {
            setSelectionToNestedViewer(viewer, selection, reveal);
        }
    }

    private void setSelectionToNestedViewer(GalleryViewer viewer,
            ISelection selection, boolean reveal) {
        settingViewerSelections = true;
        viewer.setSelection(selection, reveal);
        settingViewerSelections = false;
    }

    protected void hookSectionContent(Control content) {
        super.hookSectionContent(content);
        content.addListener(SWT.FocusIn, viewerControlListener);
        content.addListener(SWT.FocusOut, viewerControlListener);
    }

    private void updateFocus(final Display display) {
        if (display.isDisposed())
            return;
        display.asyncExec(new Runnable() {

            public void run() {
                if (display.isDisposed() || getControl().isDisposed())
                    return;

                Control focusControl = display.getFocusControl();
                for (GalleryViewer viewer : viewers.values()) {
                    if (viewer.getControl() != focusControl) {
                        setSelectionToNestedViewer(viewer,
                                StructuredSelection.EMPTY, false);
                    }
                }
            }

        });
    }

    protected void handleDispose(DisposeEvent event) {
        viewers.clear();
        super.handleDispose(event);
    }

    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        super.setLabelProvider(labelProvider);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.setLabelProvider(labelProvider);
        }
    }

    public void setSorter(ViewerSorter sorter) {
        super.setSorter(sorter);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.setSorter(sorter);
        }
    }

    public void addFilter(ViewerFilter filter) {
        super.addFilter(filter);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.addFilter(filter);
        }
    }

    public void removeFilter(ViewerFilter filter) {
        super.removeFilter(filter);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.removeFilter(filter);
        }
    }

    public void setFilters(ViewerFilter[] filters) {
        super.setFilters(filters);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.setFilters(filters);
        }
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        super.doUpdateItem(item, element, fullMap);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.update(new Object[] { element });
        }
    }
}