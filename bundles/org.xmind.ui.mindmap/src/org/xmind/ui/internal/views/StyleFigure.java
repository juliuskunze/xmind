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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.xmind.core.style.IStyle;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

public class StyleFigure extends Figure {

    private static final Rectangle RECT = new Rectangle();

    private IStyle style;

    public StyleFigure() {
    }

    public IStyle getStyle() {
        return style;
    }

    public void setStyle(IStyle style) {
        if (style == this.style)
            return;

        this.style = style;
        repaint();
    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        super.paint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        drawStyle(graphics);
    }

    protected void drawStyle(Graphics graphics) {
        if (style == null)
            return;

        graphics.setAntialias(SWT.ON);
        graphics.setTextAntialias(SWT.ON);

        Rectangle r = getClientArea(RECT);
        drawStyle(graphics, style, r);
    }

    private void drawStyle(Graphics graphics, IStyle style, Rectangle r) {
        String type = style.getType();
        if (IStyle.TOPIC.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawTopic(graphics, topicBounds(r), style, null,
                    false);
        } else if (IStyle.BOUNDARY.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawBoundary(graphics, boundaryBounds(r), style,
                    null);
        } else if (IStyle.RELATIONSHIP.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawRelationship(graphics, relBounds(r), style,
                    null);
        } else if (IStyle.MAP.equalsIgnoreCase(type)) {
            StyleFigureUtils.drawSheetBackground(graphics, sheetBounds(r),
                    style, null);
        }
    }

    public static Rectangle topicBounds(Rectangle r) {
        int width = r.width * 7 / 8;
        int height = width * 9 / 20;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    public static Rectangle boundaryBounds(Rectangle r) {
        int width = r.width * 7 / 8;
        int height = width * 7 / 10;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    public static Rectangle relBounds(Rectangle r) {
        int width = r.width * 6 / 8;
        int height = width * 7 / 10;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    public static Rectangle sheetBounds(Rectangle r) {
        int width = r.width * 7 / 8;
        int height = width * 7 / 8;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

}