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
package org.xmind.ui.internal.editor;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IZoomListener;
import org.xmind.gef.Request;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.ZoomObject;
import org.xmind.gef.ui.actions.EditorAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.editor.IMiniBar;
import org.xmind.gef.ui.editor.MiniBarContributor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.internal.actions.VisibleSeparator;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.viewers.ISliderContentProvider;
import org.xmind.ui.viewers.SliderViewer;

public class MindMapMiniBarContributor extends MiniBarContributor implements
        IZoomListener {

    public static final String VALUE = "value"; //$NON-NLS-1$

    public static final String ACTIVE_PAGE = "activePage"; //$NON-NLS-1$

    protected static final int SLIDER_WIDTH = 88;

    private class ZoomValueItem extends ContributionItem {

        private CLabel label;

        public void fill(ToolBar parent, int index) {
            if (label == null || label.isDisposed()) {
                ToolItem placeHolder;
                if (index >= 0) {
                    placeHolder = new ToolItem(parent, SWT.SEPARATOR, index);
                } else {
                    placeHolder = new ToolItem(parent, SWT.SEPARATOR);
                }
                label = new CLabel(parent, SWT.NONE);
                label.setText("000%"); // set initial text for computing size //$NON-NLS-1$
                placeHolder.setControl(label);
                placeHolder.setWidth(label.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT, true).x + 10);
                label.setText(""); //$NON-NLS-1$
                update();
            }
        }

        public void update() {
            super.update();
            update(null);
        }

        public void update(String id) {
            super.update(id);

            if (label != null && !label.isDisposed()) {

                boolean valueChanged = id == null || VALUE.equals(id);

                if (valueChanged) {
                    if (zoomManager != null) {
                        double scale = zoomManager.getScale();
                        int s = (int) Math.round(scale * 100);
                        label.setText(String.format("%d%%", s)); //$NON-NLS-1$
                    } else {
                        label.setText(""); //$NON-NLS-1$
                    }
                }
            }
        }

    }

    private class ZoomSliderItem extends ContributionItem implements
            ISelectionChangedListener, IOpenListener {

        private class ZoomSliderContentProvider implements
                ISliderContentProvider {

            private final double center = 1.0;

            private double min = 0.5;

            private double max = 2.0;

            public double getRatio(Object input, Object value) {
                if (value instanceof Double) {
                    return calcPortion(((Double) value).doubleValue());
                }
                return center;
            }

            private double calcPortion(double v) {
                if (v == center)
                    return 0.5;
                if (v < min)
                    return 0;
                if (v > max)
                    return 1.0;
                if (v < center) {
                    double d = center - min;
                    if (d == 0)
                        return min;
                    return (v - min) * 0.5 / d;
                }
                double d = max - center;
                if (d == 0)
                    return max;
                return (v - center) * 0.5 / d + 0.5;
            }

            public Object getValue(Object input, double ratio) {
                return Double.valueOf(calcValue(ratio));
            }

            private double calcValue(double ratio) {
                if (ratio > 0.45 && ratio < 0.52)
                    return center;
                if (ratio < 0.5)
                    return ((center - min) * ratio / 0.5 + min);
                return ((max - center) * (ratio - 0.5) / 0.5 + center);
            }

//            public Object[] getValues(Object input) {
//                return new Double[] { min, center, max };
//            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
                if (newInput != null && newInput instanceof ZoomManager) {
                    ZoomManager zm = (ZoomManager) newInput;
                    min = zm.getMin();
                    max = zm.getMax();
                } else {
                    min = 0.5;
                    max = 2.0;
                }
            }

            public void dispose() {
            }

        }

        private class ZoomSliderLabelProvider extends LabelProvider {

            public String getText(Object element) {
                if (element instanceof Double) {
                    double scale = ((Double) element).doubleValue();
                    return String.format("%.0f%%", Double.valueOf(scale * 100)); //$NON-NLS-1$
                }
                return super.getText(element);
            }

        }

        private SliderViewer slider;

        private boolean updating = false;

        private boolean sendingRequest = false;

        private IContentProvider contentProvider = new ZoomSliderContentProvider();

        private IBaseLabelProvider labelProvider = new ZoomSliderLabelProvider();

        public void fill(ToolBar parent, int index) {
            if (slider == null || slider.getControl().isDisposed()) {
                ToolItem placeHolder;
                if (index >= 0) {
                    placeHolder = new ToolItem(parent, SWT.SEPARATOR, index);
                } else {
                    placeHolder = new ToolItem(parent, SWT.SEPARATOR);
                }
                slider = new SliderViewer(parent, SWT.HORIZONTAL);
                slider.setContentProvider(contentProvider);
                slider.setLabelProvider(labelProvider);
                slider.addSelectionChangedListener(this);
                slider.addOpenListener(this);
                placeHolder.setControl(slider.getControl());
                placeHolder.setWidth(SLIDER_WIDTH);
                update();
            }
        }

        public void update() {
            update(null);
        }

        public void update(String id) {
            super.update(id);

            if (slider != null && !slider.getControl().isDisposed()
                    && !sendingRequest) {

                boolean zoomObjectChanged = id == null
                        || ACTIVE_PAGE.equals(id);
                boolean valueChanged = id == null || VALUE.equals(id);

                if (zoomObjectChanged) {
                    slider.setInput(zoomManager);
                    slider.getControl().setEnabled(zoomManager != null);
                }

                if (valueChanged) {
                    double value;
                    if (zoomManager != null) {
                        value = zoomManager.getScale();
                    } else {
                        value = 1.0d;
                    }
                    updating = true;
                    slider.setSelection(new StructuredSelection(Double
                            .valueOf(value)));
                    updating = false;
                }
            }
        }

        public void selectionChanged(SelectionChangedEvent event) {
            if (updating)
                return;

            ISelection selection = event.getSelection();
            if (!selection.isEmpty()
                    && selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) selection;
                Object element = ss.getFirstElement();
                if (element != null && element instanceof Double) {
                    double value = ((Double) element).doubleValue();
                    IGraphicalEditorPage page = getActivePage();
                    if (page != null) {
                        EditDomain domain = page.getEditDomain();
                        if (domain != null) {
                            sendingRequest = true;
                            sendZoomRequest(domain, value);
                            sendingRequest = false;
                        }
                    }
                }
            }
        }

        private void sendZoomRequest(EditDomain domain, double value) {
            Request request = new Request(GEF.REQ_ZOOM);
            request.setViewer(getActivePage().getViewer());
            request.setParameter(GEF.PARAM_ZOOM_SCALE, Double.valueOf(value));
            domain.handleRequest(request);
        }

        public void open(OpenEvent event) {
            IGraphicalEditorPage page = getActivePage();
            if (page != null) {
                EditDomain domain = page.getEditDomain();
                if (domain != null) {
                    sendZoomRequest(domain, 1.0d);
                }
            }
        }

    }

    private static class MiniZoomInAction extends EditorAction {
        public MiniZoomInAction(IGraphicalEditor editor) {
            super(editor);
            setToolTipText(MindMapMessages.ZoomIn_text);
            setImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.ZOOMIN_SMALL, true));
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.ZOOMIN_SMALL, false));
        }

        public void run() {
            if (isDisposed())
                return;
            performRequest(GEF.REQ_ZOOMIN);
        }
    }

    private static class MiniZoomOutAction extends EditorAction {
        public MiniZoomOutAction(IGraphicalEditor editor) {
            super(editor);
            setToolTipText(MindMapMessages.ZoomOut_text);
            setImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.ZOOMOUT_SMALL, true));
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.ZOOMOUT_SMALL, false));
        }

        public void run() {
            if (isDisposed())
                return;
            performRequest(GEF.REQ_ZOOMOUT);
        }
    }

    private static class MiniActualSizeAction extends EditorAction {
        public MiniActualSizeAction(IGraphicalEditor editor) {
            super(editor);
            setToolTipText(MindMapMessages.ActualSize_text);
            setImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.ACTUAL_SIZE_SMALL, true));
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.ACTUAL_SIZE_SMALL, false));
        }

        public void run() {
            if (isDisposed())
                return;
            performRequest(GEF.REQ_ACTUALSIZE);
        }
    }

    public static final int PREF_HEIGHT = 12;

    private ZoomManager zoomManager = null;

    private ZoomValueItem zoomValueItem;

    private ZoomSliderItem zoomSliderItem;

    private MiniZoomInAction zoomInAction;

    private MiniZoomOutAction zoomOutAction;

    private MiniActualSizeAction actualSizeAction;

    public void init(IMiniBar bar) {
        zoomValueItem = new ZoomValueItem();
        zoomSliderItem = new ZoomSliderItem();
        zoomInAction = new MiniZoomInAction(getEditor());
        zoomOutAction = new MiniZoomOutAction(getEditor());
        actualSizeAction = new MiniActualSizeAction(getEditor());
        super.init(bar);
    }

    public void contributeToToolBar(IToolBarManager toolBar) {
        super.contributeToToolBar(toolBar);
        toolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBar.add(new GroupMarker(ActionConstants.GROUP_ZOOM));
        toolBar.add(new VisibleSeparator());
        add(toolBar, zoomValueItem);
        toolBar.add(new Separator());
        add(toolBar, zoomOutAction);
        toolBar.add(zoomSliderItem);
        add(toolBar, zoomInAction);
        add(toolBar, actualSizeAction);
    }

    protected void add(IContributionManager manager, IAction action) {
        if (manager == null || action == null)
            return;
        manager.add(action);
    }

    protected void add(IContributionManager manager, IContributionItem item) {
        if (manager == null || item == null)
            return;
        manager.add(item);
    }

    public void dispose() {
        super.dispose();
        zoomValueItem = null;
        zoomSliderItem = null;
        if (zoomInAction != null) {
            zoomInAction.dispose();
            zoomInAction = null;
        }
        if (zoomOutAction != null) {
            zoomOutAction.dispose();
            zoomOutAction = null;
        }
        if (actualSizeAction != null) {
            actualSizeAction.dispose();
            actualSizeAction = null;
        }
    }

    protected void updateZoomContributionItems() {
        update(zoomValueItem);
        update(zoomSliderItem);
    }

    protected void update(IContributionItem item) {
        if (item != null)
            item.update();
    }

    protected void pageChanged(IGraphicalEditorPage page) {
        super.pageChanged(page);
        setZoomManager(getZoomManager(page));
    }

    private ZoomManager getZoomManager(IGraphicalEditorPage page) {
        if (page == null)
            return null;

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null)
            return null;

        return viewer.getZoomManager();
    }

    protected void setZoomManager(ZoomManager zoomManager) {
        if (zoomManager == this.zoomManager)
            return;

        if (this.zoomManager != null) {
            unhookZoomManager(this.zoomManager);
        }
        this.zoomManager = zoomManager;
        if (zoomManager != null) {
            hookZoomManager(zoomManager);
        }
        updateZoomContributionItems();
    }

    protected void hookZoomManager(ZoomManager zoomManager) {
        zoomManager.addZoomListener(this);
    }

    protected void unhookZoomManager(ZoomManager zoomManager) {
        zoomManager.removeZoomListener(this);
    }

    public void scaleChanged(ZoomObject source, double oldValue, double newValue) {
        updateZoomContributionItems();
    }

}