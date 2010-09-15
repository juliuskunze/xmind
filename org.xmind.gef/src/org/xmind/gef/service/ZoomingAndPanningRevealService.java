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
package org.xmind.gef.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.part.IGraphicalPart;

public abstract class ZoomingAndPanningRevealService extends
        GraphicalViewerService implements IRevealService {

    private class ViewerSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            handleViewerSelectionChanged(event.getSelection());
        }

    }

    private class RevealJob implements Runnable {

        private Display display;

        private List<IGraphicalPart> toReveal;

        private long startTime = -1;

        private boolean canceled = false;

        private int elapsedSteps = -1;

        public RevealJob(Display display, List<IGraphicalPart> toReveal) {
            this.display = display;
            this.toReveal = toReveal;
        }

        public void cancel() {
            boolean oldCanceled = this.canceled;
            setCanceled();
            if (!oldCanceled)
                revealJobCanceled(toReveal);
        }

        private void setCanceled() {
            canceled = true;
        }

        public void run() {
            if (canceled)
                return;

            if (getViewer().getControl() == null
                    || getViewer().getControl().isDisposed()) {
                setCanceled();
                return;
            }

            if (!isAnimationEnabled()) {
                finish();
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (startTime < 0)
                startTime = currentTime;

            int elapsedTime = (int) (currentTime - startTime);
            int remainingTime = getDuration() - elapsedTime;
            if (remainingTime <= 0) {
                finish();
                return;
            }

            double intervals = INTERVALS;
            if (elapsedSteps < 0) {
                elapsedSteps = 0;
                intervals += 20;
            } else {
                intervals += (((double) (currentTime - startTime - INTERVALS
                        * elapsedSteps)) / elapsedSteps);
            }

            int remainingSteps = (int) ((remainingTime + intervals - 1) / intervals);
            if (remainingSteps <= 0) {
                finish();
                return;
            }

            Rectangle revealBounds = getRevealBounds(toReveal);
            if (revealBounds == null) {
                cancel();
                return;
            }

            doStep(toReveal, revealBounds, remainingSteps);

            display.timerExec(INTERVALS, this);
            elapsedSteps++;
        }

        public void finish() {
            setCanceled();
            revealJobFinished(toReveal);
        }

    }

    private static final int INTERVALS = 30;

    private boolean autoRevealSelection;

    private ISelectionChangedListener selectionChangedListener;

    private int duration = 200;

    private int delay = 100;

    private RevealJob job = null;

    protected ZoomingAndPanningRevealService(IGraphicalViewer viewer,
            boolean autoRevealSelection) {
        super(viewer);
        this.autoRevealSelection = autoRevealSelection;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    protected boolean isAnimationEnabled() {
        return true;
    }

    protected void activate() {
        if (autoRevealSelection) {
            if (selectionChangedListener == null)
                selectionChangedListener = new ViewerSelectionChangedListener();
            getViewer().addSelectionChangedListener(selectionChangedListener);
        }
    }

    protected void deactivate() {
        if (selectionChangedListener != null) {
            getViewer()
                    .removeSelectionChangedListener(selectionChangedListener);
        }
    }

    public void reveal(ISelection selection) {
        if (!isActive())
            return;

        startReveal(selection);
    }

    protected void startReveal(ISelection selection) {
        if (job != null) {
            job.cancel();
            job = null;
        }

        List<IGraphicalPart> toReveal = collectPartsToReveal(selection);
        if (toReveal != null && !toReveal.isEmpty()) {
            Display display = Display.getCurrent();
            job = new RevealJob(display, toReveal);
            display.timerExec(delay, job);
            revealJobStarted(toReveal);
        }
    }

    protected abstract double calcTargetScale(List<IGraphicalPart> toReveal,
            Rectangle revealBounds);

    protected abstract PrecisionPoint calcTargetCenter(
            List<IGraphicalPart> toReveal, Rectangle revealBounds,
            double targetScale);

    protected Rectangle getRevealBounds(List<IGraphicalPart> parts) {
        Rectangle r = null;
        for (IGraphicalPart p : parts) {
            r = Geometry.union(r, getRevealBounds(p));
        }
        return r;
    }

    protected Rectangle getRevealBounds(IGraphicalPart p) {
        return p.getFigure().getBounds();
    }

    protected double getViewerScale() {
        return getViewer().getZoomManager().getScale();
    }

    protected PrecisionPoint getViewerCenterPoint(double scale) {
        return new PrecisionPoint(getViewer().getCenterPoint())
                .scale(1 / scale);
    }

    private List<IGraphicalPart> collectPartsToReveal(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            List<IGraphicalPart> list = new ArrayList<IGraphicalPart>(ss.size());
            for (Object o : ss.toList()) {
                IGraphicalPart p = getViewer().findGraphicalPart(o);
                if (p != null && !exclude(p)) {
                    list.add(p);
                }
            }
            return list;
        }
        return null;
    }

    protected boolean exclude(IGraphicalPart part) {
        return false;
    }

    private void handleViewerSelectionChanged(ISelection selection) {
        reveal(selection);
    }

    protected void finishCurrentJob() {
        if (job != null) {
            job.finish();
            job = null;
        }
    }

    protected void cancelCurrentJob() {
        if (job != null) {
            job.cancel();
            job = null;
        }
    }

    protected void revealJobStarted(List<IGraphicalPart> toReveal) {
    }

    protected void revealJobCanceled(List<IGraphicalPart> toReveal) {
    }

    protected void revealJobFinished(List<IGraphicalPart> toReveal) {
        Rectangle revealBounds = getRevealBounds(toReveal);
        if (revealBounds == null)
            return;

        double targetScale = calcTargetScale(toReveal, revealBounds);
        PrecisionPoint targetCenter = calcTargetCenter(toReveal, revealBounds,
                targetScale);
        if (targetScale > 0) {
            getViewer().getZoomManager().setScale(targetScale);
        }
        if (targetCenter != null) {
            getViewer().center(
                    targetCenter.getScaled(getViewerScale())
                            .toRoundedDraw2DPoint());
        }
    }

    protected void doStep(List<IGraphicalPart> toReveal,
            Rectangle revealBounds, int remainingSteps) {
        double scale = getViewerScale();
        PrecisionPoint center = getViewerCenterPoint(scale);
        double targetScale = calcTargetScale(toReveal, revealBounds);
        PrecisionPoint targetCenter = calcTargetCenter(toReveal, revealBounds,
                targetScale);

        if (targetScale > 0) {
            double remainingScale = targetScale - scale;
            double stepScale = remainingScale / remainingSteps;
            scale += stepScale;
        }

        if (targetCenter != null) {
            double horizontalOffset = targetCenter.x - center.x;
            double verticalOffset = targetCenter.y - center.y;
            double stepX = horizontalOffset / remainingSteps;
            double stepY = verticalOffset / remainingSteps;
            center.x += stepX;
            center.y += stepY;
        }

        if (targetScale > 0) {
            getViewer().getZoomManager().setScale(scale);
        }
        if (targetCenter != null) {
            getViewer().center(
                    center.getScaled(getViewerScale()).toRoundedDraw2DPoint());
        }
    }

}