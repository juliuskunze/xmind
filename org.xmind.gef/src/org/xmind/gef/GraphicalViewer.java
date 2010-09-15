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
package org.xmind.gef;

import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.event.PartsEventDispatcher;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IGraphicalRootPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.tool.SelectTool;

/**
 * @author Brian Sun
 */
public class GraphicalViewer extends AbstractViewer implements IGraphicalViewer {

    private static final Point POINT = new Point();

    protected class GraphicalSelectionSupport extends SelectionSupport {

        public boolean isSelectable(IPart p) {
            boolean selectable = super.isSelectable(p);
            if (selectable) {
                if (p instanceof IGraphicalPart) {
                    IFigure fig = ((IGraphicalPart) p).getFigure();
                    if (fig == null || !fig.isShowing())
                        return false;
                }
            }
            return selectable;
        }

        protected void partSelectionChanged(List<? extends IPart> parts,
                boolean reveal) {
            super.partSelectionChanged(parts, reveal);
            if (getEditDomain() != null) {
                ITool tool = getEditDomain().getTool(GEF.TOOL_SELECT);
                if (tool != null && tool instanceof SelectTool) {
                    ((SelectTool) tool).resetSeqSelectStart();
                }
                IPart focused = findSelectablePart(getFocused());
                if (focused == null || !focused.getStatus().isActive()
                        || !focused.getStatus().isSelected()) {
                    setFocused(findSelectedPart(parts));
                }
            }
        }

        protected IPart findSelectedPart(List<? extends IPart> parts) {
            for (IPart p : parts) {
                if (p.getStatus().isActive() && p.getStatus().isSelected())
                    return p;
            }
            return null;
        }
    }

    private final class UserMotionDetector implements Listener {

        boolean mousePressing = false;

        boolean keyPressing = false;

        boolean resizedDuringMousePress = false;

        boolean resizedDuringKeyPress = false;

        public void handleEvent(Event event) {
            if (event.type == SWT.MouseDown) {
                mousePressing = true;
            } else if (event.type == SWT.KeyDown) {
                keyPressing = true;
            } else if (event.type == SWT.MouseUp) {
                mousePressing = false;
                if (resizedDuringMousePress) {
                    resizedDuringMousePress = false;
                    performCenterJob();
                }
            } else if (event.type == SWT.KeyUp) {
                keyPressing = false;
                if (resizedDuringKeyPress) {
                    resizedDuringKeyPress = false;
                    performCenterJob();
                }
            }
        }

        // If resizing occurs while mouse or key is pressed,
        // don't center until mouse or key is released
        public boolean needsCenterWhenResizing() {
            if (!mousePressing && !keyPressing)
                return true;

            if (mousePressing)
                resizedDuringMousePress = true;
            if (keyPressing)
                resizedDuringKeyPress = true;
            return false;
        }
    }

    private final class ControlEventListener implements Listener {
        public void handleEvent(Event event) {
            if (event.type == SWT.Resize) {
                if (userMotionDetector == null
                        || userMotionDetector.needsCenterWhenResizing()) {
                    performCenterJob();
                } else {
                    event.display.asyncExec(new Runnable() {
                        public void run() {
                            performCenterJob();
                        }
                    });
                }
                revalidateContents();
            } else if (event.type == SWT.Selection) {
                onScrolling();
            } else if (event.type == SWT.FocusOut) {
                hideToolTip();
            } else if (event.type == SWT.FocusIn || event.type == SWT.Paint) {
                // Fix bug:
                FigureCanvas fc = getCanvas();
                org.eclipse.swt.graphics.Rectangle clientArea = fc
                        .getClientArea();
                if (clientArea.width > 0 || clientArea.height > 0) {
                    performCenterJob();
                    fc.removeListener(event.type, this);
                }
            }
        }
    }

    private ILayerManager layerManager = null;

    private Point centerPoint = new Point();

    private Display display = null;

    private UserMotionDetector userMotionDetector = null;

    private PartsEventDispatcher eventDispatcher = null;

    private ZoomManager zoomManager = null;

    public GraphicalViewer() {
        super();
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ILayerManager.class)
            return getLayerManager();
        if (adapter == FigureCanvas.class)
            return getCanvas();
        if (adapter == ZoomManager.class)
            return getZoomManager();
        if (adapter == IGraphicalPart.class)
            return getRootPart() instanceof IGraphicalPart ? (IGraphicalPart) getRootPart()
                    : null;
        if (adapter == IFigure.class)
            return getRootFigure();
        return super.getAdapter(adapter);
    }

    public IGraphicalPart findGraphicalPart(Object model) {
        IPart part = findPart(model);
        if (part instanceof IGraphicalPart)
            return (IGraphicalPart) part;
        return null;
    }

    public IPart findPart(int x, int y) {
        Point p = convertPoint(x, y);
        return findPart(p);
    }

    protected Point convertPoint(int controlX, int controlY) {
        Point p = new Point(controlX, controlY);
        if (getControl() != null && !getControl().isDisposed()) {
            p = computeToLayer(p, true);
        }
        return p;
    }

    protected IPart findPart(Point position) {
        return findPart(getRootPart(), position);
    }

    protected IPart findPart(IPart parent, Point position) {
        if (parent == null)
            return null;
        if (parent instanceof IGraphicalEditPart) {
            return (((IGraphicalEditPart) parent).findAt(position));
        }
        for (IPart p : parent.getChildren()) {
            IPart ret = findPart(p, position);
            if (ret != null)
                return ret;
        }
        return null;
    }

    public Control createControl(Composite parent) {
        return createControl(parent, SWT.DOUBLE_BUFFERED);
    }

    @Override
    protected Control internalCreateControl(Composite parent, int style) {
        FigureCanvas fc = new FigureCanvas(parent, style);
        eventDispatcher = createEventDispatcher();
        eventDispatcher.setDndSupport(getDndSupport());
        fc.getLightweightSystem().setEventDispatcher(eventDispatcher);
        if (getEditDomain() != null) {
            eventDispatcher.activate();
        }
        IFigure rootFigure = getRootFigure();
        if (rootFigure instanceof Viewport) {
            fc.setViewport((Viewport) rootFigure);
        } else {
            fc.setContents(rootFigure);
        }
        return fc;
    }

    protected void hookControl(Control control) {
        super.hookControl(control);

        Listener eventHandler = new ControlEventListener();
        control.addListener(SWT.Resize, eventHandler);
        control.addListener(SWT.FocusIn, eventHandler);
        control.addListener(SWT.FocusOut, eventHandler);
        control.addListener(SWT.Paint, eventHandler);

        ScrollBar hBar = ((FigureCanvas) control).getHorizontalBar();
        if (hBar != null) {
            hBar.addListener(SWT.Selection, eventHandler);
        }
        ScrollBar vBar = ((FigureCanvas) control).getVerticalBar();
        if (vBar != null) {
            vBar.addListener(SWT.Selection, eventHandler);
        }

        display = control.getDisplay();
        if (display != null) {
            userMotionDetector = new UserMotionDetector();
            display.addFilter(SWT.MouseDown, userMotionDetector);
            display.addFilter(SWT.MouseUp, userMotionDetector);
            display.addFilter(SWT.KeyDown, userMotionDetector);
            display.addFilter(SWT.KeyUp, userMotionDetector);
        }
    }

    public void setRootPart(IRootPart rootPart) {
        if (getCanvas() != null && !getCanvas().isDisposed()) {
            IFigure oldRootFigure = getRootFigure();
            if (oldRootFigure instanceof Viewport) {
                getCanvas().setViewport(null);
            } else {
                getCanvas().setContents(null);
            }
        }
        super.setRootPart(rootPart);
        if (getCanvas() != null && !getCanvas().isDisposed()) {
            IFigure newRootFigure = getRootFigure();
            if (newRootFigure instanceof Viewport) {
                getCanvas().setViewport((Viewport) newRootFigure);
            } else {
                getCanvas().setContents(newRootFigure);
            }
        }
    }

    protected IFigure getRootFigure() {
        if (getRootPart() instanceof IGraphicalPart) {
            return ((IGraphicalPart) getRootPart()).getFigure();
        }
        return null;
    }

    protected void revalidateContents() {
        IPart contents = getRootPart().getContents();
        if (contents != null && contents instanceof IGraphicalPart
                && contents.getStatus().isActive()) {
            ((IGraphicalPart) contents).getFigure().revalidate();
        }
        getCanvas().layout(true);
    }

    protected PartsEventDispatcher createEventDispatcher() {
        return new PartsEventDispatcher(this);
    }

    public PartsEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void setEditDomain(EditDomain domain) {
        if (eventDispatcher != null) {
            eventDispatcher.deactivate();
        }
        super.setEditDomain(domain);
        if (getEditDomain() != null && eventDispatcher != null) {
            eventDispatcher.activate();
        }
    }

    public Layer getLayer(Object key) {
        return layerManager == null ? null : layerManager.getLayer(key);
    }

    public ILayerManager getLayerManager() {
        return layerManager;
    }

    public void setLayerManager(ILayerManager layerManager) {
        this.layerManager = layerManager;
    }

    public void updateToolTip() {
        if (eventDispatcher != null)
            eventDispatcher.updateToolTip();
    }

    public void hideToolTip() {
        if (getControl() != null)
            getControl().setToolTipText(null);
    }

    public FigureCanvas getCanvas() {
        return (FigureCanvas) super.getControl();
    }

    public Dimension getSize() {
        return new Dimension(getCanvas().getSize());
    }

    public Rectangle getClientArea() {
        Rectangle area = new Rectangle(getCanvas().getClientArea());
        area.setLocation(getScrollPosition());
        return area;
    }

    public Point getCenterPoint() {
        return centerPoint;
    }

    public void center(Rectangle area) {
        center(area.getCenter());
    }

    public void center(Point center) {
        if (center != null && !center.equals(centerPoint)) {
            centerPoint.setLocation(center);
        }
        performCenterJob();
    }

    private void performCenterJob() {
        if (!getCanvas().isDisposed()) {
            org.eclipse.swt.graphics.Rectangle clientArea = getCanvas()
                    .getClientArea();
            int x = centerPoint.x - clientArea.width / 2;
            int y = centerPoint.y - clientArea.height / 2;
            if (usesSmoothScroll()) {
                getCanvas().getViewport().setViewLocation(x, y);
            } else {
                getCanvas().scrollTo(x, y);
            }
        }
    }

    public void scrollToX(int x) {
        if (usesSmoothScroll()) {
            getCanvas().getViewport().setHorizontalLocation(x);
//            getCanvas().scrollSmoothTo(x,
//                    getCanvas().getViewport().getViewLocation().y);
        } else {
            getCanvas().scrollToX(x);
        }
        updateCenterPoint();
    }

    public void scrollToY(int y) {
        if (usesSmoothScroll()) {
            getCanvas().getViewport().setVerticalLocation(y);
//            getCanvas().scrollSmoothTo(
//                    getCanvas().getViewport().getViewLocation().x, y);
        } else {
            getCanvas().scrollToY(y);
        }
        updateCenterPoint();
    }

    public void scrollTo(Point p) {
        scrollTo(p.x, p.y);
    }

    public void scrollTo(int x, int y) {
        if (usesSmoothScroll()) {
            getCanvas().getViewport().setViewLocation(x, y);
//            getCanvas().scrollSmoothTo(x, y);
        } else {
            getCanvas().scrollTo(x, y);
        }
        updateCenterPoint();
    }

    protected boolean usesSmoothScroll() {
        return Boolean.TRUE.equals(getProperties().get(VIEWER_SCROLL_SMOOTH));
    }

    /**
     * @see org.xmind.gef.IViewer#scrollDelta(org.eclipse.draw2d.geometry.Dimension)
     */
    public void scrollDelta(Dimension d) {
        scrollTo(getScrollPosition().translate(d));
    }

    /**
     * @see org.xmind.gef.IGraphicalViewer#scrollDelta(int, int)
     */
    public void scrollDelta(int dx, int dy) {
        scrollTo(getScrollPosition().translate(dx, dy));
    }

    /**
     * @see org.xmind.gef.IViewer#getScrollPosition()
     */
    public Point getScrollPosition() {
        return getCanvas().getViewport().getViewLocation();
    }

    protected void updateCenterPoint() {
        Point p = getScrollPosition();
        Dimension s = getClientArea().getSize();
        centerPoint.setLocation(p.x + s.width / 2, p.y + s.height / 2);
    }

    /**
     * @see org.xmind.gef.IGraphicalViewer#computeToLayer(org.eclipse.draw2d.geometry.Point,
     *      boolean)
     */
    public Point computeToLayer(Point controlPoint, boolean zoomed) {
        Point p = getScrollPosition();
        p.translate(controlPoint);
        if (zoomed) {
            return p.scale(1 / getZoomManager().getScale());
        }
        return p;
    }

    /**
     * @see org.xmind.gef.IGraphicalViewer#computeToControl(org.eclipse.draw2d.geometry.Point,
     *      boolean)
     */
    public Point computeToControl(Point layerPoint, boolean zoomed) {
        POINT.setLocation(layerPoint);
        if (zoomed) {
            POINT.scale(getZoomManager().getScale());
        }
        return getScrollPosition().negate().translate(POINT);
    }

    public Point computeToDisplay(Point layerPoint, boolean zoomed) {
        Point p = computeToControl(layerPoint, zoomed);
        org.eclipse.swt.graphics.Point loc = getControl().toDisplay(p.x, p.y);
        return p.setLocation(loc.x, loc.y);
    }

    public void ensureVisible(Rectangle box) {
        box = getZoomManager().getScaled(box);
        Rectangle clientArea = getClientArea();
        if (clientArea.contains(box) || box.contains(clientArea))
            return;
        ensureVisible(box, clientArea, 0);
    }

    public void ensureControlVisible(Rectangle box) {
        Rectangle clientArea = new Rectangle(getCanvas().getClientArea());
        if (clientArea.contains(box) || box.contains(clientArea))
            return;
        ensureVisible(box, clientArea, 0);
    }

    /**
     * @param box
     * @param clientArea
     * @param margin
     */
    protected void ensureVisible(Rectangle box, Rectangle clientArea, int margin) {
        int dx = 0;
        int dy = 0;
        if (box.width > clientArea.width)
            dx = box.getCenter().x - clientArea.getCenter().x;
        else if (box.x < clientArea.x)
            dx = box.x - clientArea.x - margin;
        else if (box.right() > clientArea.right())
            dx = box.right() - clientArea.right() + margin;
        if (box.height > clientArea.height)
            dy = box.getCenter().y - clientArea.getCenter().y;
        else if (box.y < clientArea.y)
            dy = box.y - clientArea.y - margin;
        else if (box.bottom() > clientArea.bottom())
            dy = box.bottom() - clientArea.bottom() + margin;
        smoothScrollDelta(dx, dy);
    }

    /**
     * @param dx
     * @param dy
     */
    protected void smoothScrollDelta(int dx, int dy) {
        scrollDelta(dx, dy);
    }

    protected void handleDispose(DisposeEvent e) {
        if (display != null) {
            if (userMotionDetector != null) {
                display.removeFilter(SWT.MouseDown, userMotionDetector);
                display.removeFilter(SWT.MouseUp, userMotionDetector);
                display.removeFilter(SWT.KeyDown, userMotionDetector);
                display.removeFilter(SWT.KeyUp, userMotionDetector);
                userMotionDetector = null;
            }
            display = null;
        }
//        centerJob = null;
        super.handleDispose(e);
    }

    protected IGraphicalRootPart getGraphicalRootEditPart() {
        IRootPart rootPart = getRootPart();
        if (rootPart instanceof IGraphicalRootPart) {
            return (IGraphicalRootPart) rootPart;
        }
        return null;
    }

    protected ISelectionSupport createSelectionSupport() {
        return new GraphicalSelectionSupport();
    }

    protected void inputChanged(Object input, Object oldInput) {
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().setRedraw(false);
        }

        ISelection oldSelection = getSelection();
        setSelectionOnInputChanged(StructuredSelection.EMPTY);
        double oldScale = getZoomManager().getScale();

        if (eventDispatcher != null) {
            eventDispatcher.deactivate();
        }

        internalInputChanged(input, oldInput);

        if (eventDispatcher != null) {
            eventDispatcher.activate();
        }

        setSelectionOnInputChanged(oldSelection);

        if (oldScale >= 0) {
            getZoomManager().setScale(oldScale);
        }

        if (getControl() != null && !getControl().isDisposed()) {
            getControl().setRedraw(true);
        }
    }

    protected void setSelectionOnInputChanged(ISelection selection) {
        setSelection(selection);
    }

    protected void internalInputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
    }

    /**
     * @param x
     * @param y
     */
    protected void alternativeScrollTo(int x, int y) {
        int hOffset = verifyScrollBarOffset(getCanvas().getViewport()
                .getHorizontalRangeModel(), x);
        int vOffset = verifyScrollBarOffset(getCanvas().getViewport()
                .getVerticalRangeModel(), y);

        int hOffsetOld = getCanvas().getViewport().getViewLocation().x;
        if (hOffset == hOffsetOld) {
            scrollToY(y);
            return;
        }
        int dx = -hOffset + hOffsetOld;

        int vOffsetOld = getCanvas().getViewport().getViewLocation().y;
        if (vOffset == vOffsetOld) {
            scrollToX(x);
            return;
        }
        int dy = -vOffset + vOffsetOld;

        Rectangle clientArea = getCanvas().getViewport().getBounds()
                .getCropped(getCanvas().getViewport().getInsets());
        Rectangle blit = clientArea.getResized(-Math.abs(dx), -Math.abs(dy));
        Rectangle expose = clientArea.getCopy();
        Rectangle expose2 = clientArea.getCopy();
        Point dest = clientArea.getTopLeft();
        expose.width = Math.abs(dx);
        if (dx < 0) { //Moving left?
            blit.translate(-dx, 0); //Move blit area to the right
            expose.x = dest.x + blit.width;
        } else
            //Moving right
            dest.x += dx; //Move expose area to the right

        expose2.height = Math.abs(dy);
        if (dy < 0) { //Moving up?
            blit.translate(0, -dy); //Move blit area down
            expose2.y = dest.y + blit.height; //Move expose area down
        } else
            //Moving down
            dest.y += dy;

        // fix for bug 41111
        Control[] children = getCanvas().getChildren();
        boolean[] manualMove = new boolean[children.length];
        for (int i = 0; i < children.length; i++) {
            org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
            manualMove[i] = blit.width <= 0 || blit.height < 0
                    || bounds.x > blit.x + blit.width
                    || bounds.y > blit.y + blit.height
                    || bounds.x + bounds.width < blit.x
                    || bounds.y + bounds.height < blit.y;
        }
        getCanvas().scroll(dest.x, dest.y, blit.x, blit.y, blit.width,
                blit.height, true);

        for (int i = 0; i < children.length; i++) {
            org.eclipse.swt.graphics.Rectangle bounds = children[i].getBounds();
            if (manualMove[i])
                children[i].setBounds(bounds.x + dx, bounds.y, bounds.width,
                        bounds.height);
        }

        getCanvas().getViewport().setIgnoreScroll(true);
        getCanvas().getViewport().setHorizontalLocation(hOffset);
        getCanvas().getViewport().setVerticalLocation(vOffset);
        getCanvas().getViewport().setIgnoreScroll(false);
        getCanvas().redraw(expose.x, expose.y, expose.width, expose.height,
                true);
        getCanvas().redraw(expose2.x, expose2.y, expose2.width, expose2.height,
                true);
    }

    private int verifyScrollBarOffset(RangeModel model, int value) {
        value = Math.max(model.getMinimum(), value);
        return Math.min(model.getMaximum() - model.getExtent(), value);
    }

    protected void onScrolling() {
        updateCenterPoint();
    }

    public ZoomManager getZoomManager() {
        if (zoomManager == null)
            zoomManager = new ZoomManager();
        return zoomManager;
    }

    public void setZoomManager(ZoomManager zoomManager) {
        this.zoomManager = zoomManager;
    }

    public void setDndSupport(IDndSupport dndSupport) {
        super.setDndSupport(dndSupport);
        if (eventDispatcher != null) {
            eventDispatcher.setDndSupport(dndSupport);
        }
    }

}