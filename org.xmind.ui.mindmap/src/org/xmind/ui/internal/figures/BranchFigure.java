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
package org.xmind.ui.internal.figures;

import java.util.Iterator;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.IDecoratedFigureListener;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.draw2d.IShadowedFigure;
import org.xmind.gef.draw2d.ITransparentableFigure;
import org.xmind.gef.draw2d.ReferencedFigure;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.graphics.AlphaGraphics;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.GrayedGraphics;
import org.xmind.ui.decorations.IBranchDecoration;

public class BranchFigure extends ReferencedFigure implements IDecoratedFigure,
        IMinimizable, IShadowedFigure, ITransparentableFigure {

    private static final int FLAG_FOLDED = MAX_FLAG << 1;

    private static final int FLAG_MINIMIZED = MAX_FLAG << 2;

    static {
        MAX_FLAG = FLAG_MINIMIZED;
    }

    private IDecoration connections = null;

    private IDecoration decoration = null;

    private int mainAlpha = 0xff;

    private int alpha = 0xff;

    public boolean isFolded() {
        return getFlag(FLAG_FOLDED);
    }

    public void setFolded(boolean folded) {
        if (folded == isFolded())
            return;
        setFlag(FLAG_FOLDED, folded);
        revalidate();
        repaint();
    }

    public boolean getMinimized() {
        return getFlag(FLAG_MINIMIZED);
    }

    public boolean isMinimized() {
        if (getMinimized())
            return true;
        if (getParent() instanceof BranchFigure) {
            if (((BranchFigure) getParent()).isFolded())
                return true;
        }
        if (getParent() != null && getParent() instanceof IMinimizable)
            return ((IMinimizable) getParent()).isMinimized();
        return false;
    }

    public void setMinimized(boolean minimized) {
        if (minimized == isMinimized())
            return;
        setFlag(FLAG_MINIMIZED, minimized);
        revalidate();
        repaint();
    }

    public IDecoration getConnections() {
        return connections;
    }

    public void setConnections(IDecoration connections) {
        if (connections == this.connections)
            return;
        this.connections = connections;
        revalidate();
        repaint();
    }

    public IDecoration getDecoration() {
        return decoration;
    }

    public void setDecoration(IDecoration decoration) {
        IDecoration oldDecoration = this.decoration;
        if (decoration == oldDecoration)
            return;
        this.decoration = decoration;
        fireDecorationChanged(oldDecoration, decoration);
        repaint();
    }

    public void addDecoratedFigureListener(IDecoratedFigureListener listener) {
        addListener(IDecoratedFigureListener.class, listener);
    }

    public void removeDecoratedFigureListener(IDecoratedFigureListener listener) {
        removeListener(IDecoratedFigureListener.class, listener);
    }

    protected void fireDecorationChanged(IDecoration oldDecoration,
            IDecoration newDecoration) {
        Iterator listeners = getListeners(IDecoratedFigureListener.class);
        while (listeners.hasNext()) {
            ((IDecoratedFigureListener) listeners.next()).decorationChanged(
                    this, oldDecoration, newDecoration);
        }
    }

    public boolean isShadowShowing() {
        return isShowing() && getMainAlpha() > 0;
    }

    public int getMainAlpha() {
        return mainAlpha;
    }

    public int getSubAlpha() {
        return alpha;
    }

    public void setMainAlpha(int alpha) {
        if (alpha == this.mainAlpha)
            return;
        this.mainAlpha = alpha;
        repaint();
    }

    public void setSubAlpha(int alpha) {
        if (alpha == this.alpha)
            return;
        this.alpha = alpha;
        repaint();
    }

    public void invalidate() {
        if (connections != null)
            connections.invalidate();
        if (decoration != null)
            decoration.invalidate();
        super.invalidate();
    }

    public void setEnabled(boolean value) {
        super.setEnabled(value);
        repaint();
    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        if (getMainAlpha() < 0xff) {
            AlphaGraphics ag = new AlphaGraphics(graphics);
            ag.setMainAlpha(getMainAlpha());
            ag.setAlpha(graphics.getAlpha());
            try {
                doPaint(ag);
            } finally {
                ag.dispose();
            }
        } else {
            doPaint(graphics);
        }
    }

    private void doPaint(Graphics graphics) {
        if (isEnabled()) {
            super.paint(graphics);
        } else {
            GrayedGraphics gg = new GrayedGraphics(graphics);
            try {
                super.paint(gg);
            } finally {
                gg.dispose();
            }
        }
    }

    protected void paintFigure(Graphics graphics) {
        if (getSubAlpha() < 0xff) {
            AlphaGraphics ag = new AlphaGraphics(graphics);
            ag.setMainAlpha(getSubAlpha());
            try {
                doPaintFigure(ag);
            } finally {
                ag.dispose();
            }
        } else {
            doPaintFigure(graphics);
        }
    }

    private void doPaintFigure(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        super.paintFigure(graphics);
        if (decoration != null) {
            decoration.paint(this, graphics);
        }
        if (connections != null) {
            connections.paint(this, graphics);
        }
//        graphics.setForegroundColor(ColorConstants.red);
//        graphics.drawRectangle(getBounds().getResized(-1, -1));
    }

    protected void paintBorder(Graphics graphics) {
        super.paintBorder(graphics);
        if (decoration != null && decoration instanceof IBranchDecoration) {
            ((IBranchDecoration) decoration).paintAboveChildren(this, graphics);
        }
    }

    public void paintShadow(Graphics graphics) {
        if (connections != null && connections instanceof IShadowedDecoration) {
            ((IShadowedDecoration) connections).paintShadow(this, graphics);
        }
        if (decoration != null && decoration instanceof IShadowedDecoration) {
            ((IShadowedDecoration) decoration).paintShadow(this, graphics);
        }
    }

    public String toString() {
        for (Object c : getChildren()) {
            if (c instanceof TopicFigure) {
                return c.toString();
            }
        }
        return super.toString();
    }

}