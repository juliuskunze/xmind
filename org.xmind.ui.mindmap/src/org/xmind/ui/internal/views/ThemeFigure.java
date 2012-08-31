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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.style.IStyle;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.ui.style.Styles;

public class ThemeFigure extends Figure {

    private static final Rectangle RECT = new Rectangle();

    private IStyle theme = null;

    private Image defaultImage = null;

    public ThemeFigure() {
    }

    public IStyle getTheme() {
        return theme;
    }

    public void setTheme(IStyle theme) {
        if (theme == this.theme)
            return;

        this.theme = theme;
        repaint();
    }

    public Image getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(Image defaultImage) {
        if (defaultImage == this.defaultImage)
            return;
        this.defaultImage = defaultImage;
        repaint();
    }

//    public boolean isDefault() {
//        return isDefault;
//    }
//
//    public void setDefault(boolean isDefault) {
//        if (isDefault == this.isDefault)
//            return;
//        this.isDefault = isDefault;
//        repaint();
//    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        super.paint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        drawTheme(graphics);
    }

    protected void drawTheme(Graphics graphics) {
        if (theme == null)
            return;

        graphics.setAntialias(SWT.ON);
        graphics.setTextAntialias(SWT.ON);

        Rectangle r = getClientArea(RECT);
        drawTheme(graphics, theme, r);
    }

    protected void drawTheme(Graphics graphics, IStyle theme, Rectangle r) {
        IStyle sheetStyle = theme.getDefaultStyle(Styles.FAMILY_MAP);
        IStyle centralStyle = theme
                .getDefaultStyle(Styles.FAMILY_CENTRAL_TOPIC);
        IStyle mainStyle = theme.getDefaultStyle(Styles.FAMILY_MAIN_TOPIC);
        Rectangle centralBounds = centralBounds(r);
        Rectangle mainBounds1 = mainBounds1(r);
        Rectangle mainBounds2 = mainBounds2(r);

        StyleFigureUtils.drawSheetBackground(graphics, r, sheetStyle,
                StyleFigureUtils.defaultSheetStyle, false);

        boolean tapered = true;
        Color lineColor1 = StyleFigureUtils.getBranchConnectionColor(mainStyle,
                StyleFigureUtils.defaultMainStyle, centralStyle,
                StyleFigureUtils.defaultCentralStyle, 0, ColorConstants.gray);
        graphics.setForegroundColor(lineColor1);
        StyleFigureUtils.drawLine(graphics, centralBounds, centralStyle,
                StyleFigureUtils.defaultCentralStyle, false, mainBounds1,
                mainStyle, StyleFigureUtils.defaultMainStyle, true, tapered);
        StyleFigureUtils.drawTopic(graphics, mainBounds1, mainStyle,
                StyleFigureUtils.defaultMainStyle, true);

        Color lineColor2 = StyleFigureUtils.getBranchConnectionColor(mainStyle,
                StyleFigureUtils.defaultMainStyle, centralStyle,
                StyleFigureUtils.defaultCentralStyle, 1, ColorConstants.gray);
        graphics.setForegroundColor(lineColor2);
        StyleFigureUtils.drawLine(graphics, centralBounds, centralStyle,
                StyleFigureUtils.defaultCentralStyle, false, mainBounds2,
                mainStyle, StyleFigureUtils.defaultMainStyle, true, tapered);
        StyleFigureUtils.drawTopic(graphics, mainBounds2, mainStyle,
                StyleFigureUtils.defaultMainStyle, true);

        StyleFigureUtils.drawTopic(graphics, centralBounds, centralStyle,
                StyleFigureUtils.defaultCentralStyle, false);

        if (defaultImage != null) {
//            org.eclipse.swt.graphics.Rectangle imgBounds = defaultImage
//                    .getBounds();
//            int w = imgBounds.width;
//            int h = imgBounds.height;
            graphics.drawImage(defaultImage, r.x + 1, r.y + 1);
        }
    }

    public static Rectangle centralBounds(Rectangle r) {
        int x = r.x + r.width * 3 / 10;
        int y = r.y + r.height * 5 / 10;
        int w = r.width * 4 / 10;
        int h = r.height * 5 / 10;
        x -= w / 2;
        y -= h / 2;
        return new Rectangle(x, y, w, h);
    }

    public static Rectangle mainBounds1(Rectangle r) {
        float x = r.x + r.width * 7.5f / 10;
        float y = r.y + r.height * 2.5f / 10;
        float w = r.width * 3.0f / 10;
        float h = r.height * 2.5f / 10;
        x -= w / 2;
        y -= h / 2;
        return new Rectangle((int) x, (int) y, (int) w, (int) h);
    }

    public static Rectangle mainBounds2(Rectangle r) {
        float x = r.x + r.width * 7.5f / 10;
        float y = r.y + r.height * 7.5f / 10;
        float w = r.width * 3.0f / 10;
        float h = r.height * 2.5f / 10;
        x -= w / 2;
        y -= h / 2;
        return new Rectangle((int) x, (int) y, (int) w, (int) h);
    }

}