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

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.AdvancedToolbarLayout;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.graphics.GradientPattern;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.resources.ColorUtils;

/**
 * @author Frank Shaka
 */
public class FrameFigure extends Figure {

    private static final int FLAG_SELECTED = MAX_FLAG << 1;
    private static final int FLAG_PRESELECTED = MAX_FLAG << 2;
    private static final int FLAG_HIDE_TITLE = MAX_FLAG << 3;
    private static final int FLAG_FLAT = MAX_FLAG << 4;
    static {
        MAX_FLAG = FLAG_FLAT;
    }

    protected static final Color ColorSelected = ColorUtils.getColor("#0070d8"); //$NON-NLS-1$
    protected static final Color ColorSelectedPreselected = ColorUtils
            .getColor("#2088e0"); //$NON-NLS-1$
    protected static final Color ColorInactive = ColorUtils.gray(ColorSelected);

    private class FocusBorder extends AbstractBorder {

        private Insets insets = new Insets(6);

        public void paint(IFigure figure, Graphics graphics, Insets insets) {
            boolean selected = isSelected();
            boolean preselected = isPreselected();
            if (!preselected && !selected)
                return;

            boolean selectedPreselected = preselected && selected;
            boolean lighter = preselected && !selected;

            graphics.setAntialias(SWT.ON);
            graphics.setLineWidth(1);
            graphics.setLineStyle(SWT.LINE_SOLID);
            tempRect.setBounds(getPaintRectangle(figure, insets));
            tempRect.shrink(1, 1);

            graphics
                    .setForegroundColor(selectedPreselected ? ColorSelectedPreselected
                            : ColorSelected);

            graphics.setAlpha(lighter ? 0x80 : 0xf0);
            Path p = new Path(Display.getCurrent());
            p.addRoundedRectangle(tempRect.x, tempRect.y, tempRect.width,
                    tempRect.height, 3);
            graphics.drawPath(p);
            p.dispose();
        }

        public Insets getInsets(IFigure figure) {
            return insets;
        }
    }

    private RotatableWrapLabel title;

    private IFigure titleContainer;

    private ShadowedLayer contentLayer;

    private int titlePlacement = PositionConstants.TOP;

    /**
     * 
     */
    public FrameFigure() {
        setBorder(new FocusBorder());
        FrameBorderLayout layout = new FrameBorderLayout();
        layout.setVerticalSpacing(2);
        layout.setHorizontalSpacing(2);
        super.setLayoutManager(layout);

        titleContainer = new Layer();
        AdvancedToolbarLayout titleContainerLayout = new AdvancedToolbarLayout();
        titleContainerLayout.setStretchMinorAxis(true);
        titleContainer.setLayoutManager(titleContainerLayout);
        add(titleContainer, FrameBorderLayout.TOP);

        title = new RotatableWrapLabel(RotatableWrapLabel.NORMAL) {
            protected void paintFigure(Graphics graphics) {
                if (isSelected()) {
                    graphics.setForegroundColor(ColorConstants.white);
                }
                super.paintFigure(graphics);
            }
        };
        title.setTextAlignment(PositionConstants.CENTER);
        title.setAbbreviated(true);
        titleContainer.add(title, FrameBorderLayout.TOP);

        contentLayer = new ShadowedLayer();
        contentLayer.setBorderColor(ColorConstants.gray);
        add(contentLayer, FrameBorderLayout.CENTER);
    }

    public void setLayoutManager(LayoutManager manager) {
        // Prevent external layout manager to be set.
    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        super.paint(graphics);
    }

    @Override
    protected void paintFigure(Graphics graphics) {
        boolean preselected = isPreselected();
        boolean selected = isSelected();
        if (preselected && selected) {
            paintBackground(graphics, ColorSelectedPreselected, 0xf0);
        } else if (preselected) {
            paintBackground(graphics, ColorSelected, 0x80);
        } else if (selected) {
            paintBackground(graphics, ColorSelected, 0xf0);
        }
        super.paintFigure(graphics);
    }

    private void paintBackground(Graphics graphics, Color color, int alpha) {
        Rectangle b = new Rectangle(getBounds()).shrink(1, 1);
        graphics.setAntialias(SWT.ON);
        graphics.setAlpha(alpha);
        graphics.pushState();
        try {
            switch (getTitlePlacement()) {
            case PositionConstants.LEFT:
                paintHorizontalBackground(graphics, 0, b, color, alpha,
                        ColorUtils.gradientLighter(color), alpha);
                break;
            case PositionConstants.RIGHT:
                paintHorizontalBackground(graphics, 0, b, ColorUtils
                        .gradientLighter(color), alpha, color, alpha);
                break;
            case PositionConstants.BOTTOM:
                paintVerticalBackground(graphics, 0, b, ColorUtils
                        .gradientLighter(color), alpha, color, alpha);
                break;
            case PositionConstants.TOP:
                paintVerticalBackground(graphics, 0, b, color, alpha,
                        ColorUtils.gradientLighter(color), alpha);
                break;
            }
        } finally {
            graphics.popState();
        }
    }

    private void paintVerticalBackground(Graphics graphics, int start,
            Rectangle r, Color color1, int alpha1, Color color2, int alpha2) {
        int length = r.height;
        start += r.y;
        Pattern p = new GradientPattern(Display.getCurrent(), r.x, start, r.x,
                start + length, color1, alpha1, color2, alpha2);
        graphics.setBackgroundPattern(p);
        Path s = new Path(Display.getCurrent());
        s.addRoundedRectangle(r.x, start, r.width, length, 3);
        graphics.fillPath(s);
        s.dispose();
        p.dispose();
    }

    private void paintHorizontalBackground(Graphics graphics, int start,
            Rectangle r, Color color, int alpha1, Color color2, int alpha2) {
        int length = r.width;
        start += r.x;
        Pattern p = new GradientPattern(Display.getCurrent(), start, r.y, start
                + length, r.y, color, alpha1, color2, alpha2);
        graphics.setBackgroundPattern(p);
        Path s = new Path(Display.getCurrent());
        s.addRoundedRectangle(start, r.y, length, r.height, 3);
        graphics.fillPath(s);
        s.dispose();
        p.dispose();
    }

    /**
     * @return the slide
     */
    public ShadowedLayer getContentPane() {
        return contentLayer;
    }

    public ITextFigure getTitle() {
        return title;
    }

    /**
     * 
     * @return one of {@link PositionConstants#TOP},
     *         {@link PositionConstants#BOTTOM}, {@link PositionConstants#LEFT},
     *         {@link PositionConstants#RIGHT}
     */
    public int getTitlePlacement() {
        return titlePlacement;
    }

    /**
     * 
     * @param textPlacement
     *            one of {@link PositionConstants#TOP},
     *            {@link PositionConstants#BOTTOM},
     *            {@link PositionConstants#LEFT},
     *            {@link PositionConstants#RIGHT}
     */
    public void setTitlePlacement(int textPlacement) {
        if (textPlacement == getTitlePlacement())
            return;
        this.titlePlacement = textPlacement;
        updateTitlePlacement(textPlacement);
    }

    private void updateTitlePlacement(int textPlacement) {
        Object constraint = null;
        switch (textPlacement) {
        case PositionConstants.LEFT:
            constraint = FrameBorderLayout.LEFT;
            title.setTextAlignment(PositionConstants.RIGHT);
            break;
        case PositionConstants.RIGHT:
            constraint = FrameBorderLayout.RIGHT;
            title.setTextAlignment(PositionConstants.LEFT);
            break;
        case PositionConstants.TOP:
            constraint = FrameBorderLayout.TOP;
            title.setTextAlignment(PositionConstants.CENTER);
            break;
        case PositionConstants.BOTTOM:
            constraint = FrameBorderLayout.BOTTOM;
            title.setTextAlignment(PositionConstants.CENTER);
            break;
        }
        if (constraint != null && titleContainer.getParent() == this) {
            setConstraint(titleContainer, constraint);
        }
    }

    public boolean isSelected() {
        return getFlag(FLAG_SELECTED);
    }

    public void setSelected(boolean selected) {
        if (selected == isSelected())
            return;
        setFlag(FLAG_SELECTED, selected);
        repaint();
    }

    public void setPreselected(boolean preselected) {
        if (preselected == isPreselected())
            return;
        setFlag(FLAG_PRESELECTED, preselected);
        repaint();
    }

    public boolean isPreselected() {
        return getFlag(FLAG_PRESELECTED);
    }

    public boolean isPressed() {
        return contentLayer.isPressed();
    }

    public void setPressed(boolean pressed) {
        if (isFlat())
            return;
        contentLayer.setPressed(pressed);
    }

    public void press() {
        if (isFlat())
            return;
        contentLayer.press();
    }

    public void unpress() {
        if (isFlat())
            return;
        contentLayer.unpress();
    }

    public void togglePressed() {
        if (isFlat())
            return;
        contentLayer.togglePressed();
    }

    public boolean isHideTitle() {
        return getFlag(FLAG_HIDE_TITLE);
    }

    public boolean isFlat() {
        return getFlag(FLAG_FLAT);
    }

    public void setFlat(boolean flat) {
        if (flat == isFlat())
            return;
        setFlag(FLAG_FLAT, flat);
        if (flat) {
            contentLayer.setDepths(0);
        } else {
            contentLayer.setDepths(3);
        }
    }

    public void setHideTitle(boolean hideTitle) {
        boolean oldHideTitle = isHideTitle();
        if (hideTitle == oldHideTitle)
            return;
        setFlag(FLAG_HIDE_TITLE, hideTitle);
        if (hideTitle) {
            remove(titleContainer);
        } else {
            add(titleContainer);
            updateTitlePlacement(getTitlePlacement());
        }
    }

}