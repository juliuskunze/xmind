/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package org.xmind.gef.draw2d.graphics;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.PathData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.TextLayout;

/**
 * Rotate the coordinate 90 degrees clockwise.
 * 
 * @author frankshaka
 */
public class Rotate90Graphics extends Graphics {

    private Graphics real;

    private Pattern lastBgPattern = null;

    private Pattern lastFgPattern = null;

    public Rotate90Graphics(Graphics realGraphics) {
        this.real = realGraphics;
    }

    protected Graphics getGraphics() {
        return real;
    }

    protected float[] rotate(float x, float y) {
        return new float[] { -y, x };
    }

    protected int[] rotate(int x, int y) {
        return new int[] { -y, x };
    }

    protected Rectangle rotate(Rectangle r) {
        return new Rectangle(-r.y - r.height, r.x, r.height, r.width);
    }

    protected Point rotate(Point p) {
        return new Point(-p.y, p.x);
    }

    protected Path rotate(Path path) {
        PathData data = path.getPathData();
        Path newPath = new Path(path.getDevice());
        int index = 0;
        float[] points = data.points;
        float x, y, cx1, cy1, cx2, cy2;
        for (byte type : data.types) {
            switch (type) {
            case SWT.PATH_MOVE_TO:
                x = points[index++];
                y = points[index++];
                newPath.moveTo(-y, x);
                break;
            case SWT.PATH_LINE_TO:
                x = points[index++];
                y = points[index++];
                newPath.lineTo(-y, x);
                break;
            case SWT.PATH_CUBIC_TO:
                x = points[index++];
                y = points[index++];
                cx1 = points[index++];
                cy1 = points[index++];
                cx2 = points[index++];
                cy2 = points[index++];
                newPath.cubicTo(-y, x, -cy1, cx1, -cy2, cx2);
                break;
            case SWT.PATH_QUAD_TO:
                x = points[index++];
                y = points[index++];
                cx1 = points[index++];
                cy1 = points[index++];
                newPath.quadTo(-y, x, -cy1, cx1);
                break;
            case SWT.PATH_CLOSE:
                newPath.close();
                break;
            }
        }
        return newPath;
    }

    protected int[] rotate(int[] points) {
        int[] newPoints = new int[points.length];
        for (int i = 0; i < points.length; i += 2) {
            newPoints[i] = -points[i + 1];
            newPoints[i + 1] = points[i];
        }
        return newPoints;
    }

    protected PointList rotate(PointList points) {
        int size = points.size();
        PointList newPoints = new PointList(size);
        for (int i = 0; i < size; i++) {
            Point p = points.getPoint(i);
            newPoints.setPoint(rotate(p), i);
        }
        return newPoints;
    }

    private Pattern rotate(GradientPattern p) {
        GradientPattern p2 = new GradientPattern(p.getDevice(), //
                -p.y1, p.x1, //
                -p.y2, p.x2, //
                p.color1, p.alpha1, //
                p.color2, p.alpha2);
        return p2;
    }

    private Pattern rotate(ImagePattern p) {
        ImagePattern p2 = new ImagePattern(p.getDevice(), rotate(p.image));
        return p2;
    }

    private Image rotate(Image image) {

        return image;
    }

    public void clipRect(Rectangle r) {
        real.clipRect(rotate(r));
    }

    public void dispose() {
        if (lastBgPattern != null) {
            lastBgPattern.dispose();
            lastBgPattern = null;
        }
        if (lastFgPattern != null) {
            lastFgPattern.dispose();
            lastFgPattern = null;
        }
    }

    public void drawArc(int x, int y, int w, int h, int offset, int length) {
        real.drawArc(-y - h, x, h, w, offset, length);
    }

    public void drawFocus(int x, int y, int w, int h) {
        real.drawFocus(-y - h, x, h, w);
    }

    public void drawImage(Image srcImage, int x1, int y1, int w1, int h1,
            int x2, int y2, int w2, int h2) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.drawImage(srcImage, x1, y1, w1, h1, x2, y2, w2, h2);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawImage(Image srcImage, int x, int y) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.drawImage(srcImage, x, y);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        real.drawLine(-y1, x1, -y2, x2);
    }

    public void drawOval(int x, int y, int w, int h) {
        real.drawOval(-y - h, x, h, w);
    }

    public void drawPath(Path path) {
        Path p2 = rotate((Path) path);
        real.drawPath(p2);
        p2.dispose();
    }

    public void drawPoint(int x, int y) {
        real.drawPoint(-y, x);
    }

    public void drawPolygon(int[] points) {
        real.drawPolygon(rotate(points));
    }

    public void drawPolygon(PointList points) {
        real.drawPolygon(rotate(points));
    }

    public void drawPolyline(int[] points) {
        real.drawPolyline(rotate(points));
    }

    public void drawPolyline(PointList points) {
        real.drawPolyline(rotate(points));
    }

    public void drawRectangle(int x, int y, int width, int height) {
        real.drawRectangle(-y - height, x, height, width);
    }

    public void drawRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        real.drawRoundRectangle(rotate(r), arcHeight, arcWidth);
    }

    public void drawString(String s, int x, int y) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.drawString(s, x, y);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawText(String s, int x, int y, int style) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.drawText(s, x, y, style);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawText(String s, int x, int y) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.drawText(s, x, y);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawTextLayout(TextLayout layout, int x, int y,
            int selectionStart, int selectionEnd, Color selectionForeground,
            Color selectionBackground) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.drawTextLayout(layout, x, y, selectionStart, selectionEnd,
                selectionForeground, selectionBackground);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void fillArc(int x, int y, int w, int h, int offset, int length) {
        real.fillArc(-y - h, x, h, w, offset, length);
    }

    public void fillGradient(int x, int y, int w, int h, boolean vertical) {
        real.fillGradient(-y - h, x, h, w, !vertical);
    }

    public void fillOval(int x, int y, int w, int h) {
        real.fillOval(-y - h, x, h, w);
    }

    public void fillPath(Path path) {
        Path p2 = rotate((Path) path);
        real.fillPath(p2);
        p2.dispose();
    }

    public void fillPolygon(int[] points) {
        real.fillPolygon(rotate(points));
    }

    public void fillPolygon(PointList points) {
        real.fillPolygon(rotate(points));
    }

    public void fillRectangle(int x, int y, int width, int height) {
        real.fillRectangle(-y - height, x, height, width);
    }

    public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        real.fillRoundRectangle(rotate(r), arcHeight, arcWidth);
    }

    public void fillString(String s, int x, int y) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.fillString(s, x, y);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void fillText(String s, int x, int y) {
        boolean statePushed = false;
        try {
            real.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        real.rotate(90);
        real.fillText(s, x, y);
        real.rotate(-90);
        if (statePushed) {
            try {
                real.restoreState();
                real.popState();
            } catch (Throwable t) {
            }
        }
    }

    public double getAbsoluteScale() {
        return real.getAbsoluteScale();
    }

    public int getAlpha() {
        return real.getAlpha();
    }

    public int getAntialias() {
        return real.getAntialias();
    }

    public Color getBackgroundColor() {
        return real.getBackgroundColor();
    }

    public Rectangle getClip(Rectangle rect) {
        rect = real.getClip(rect);
        int rx = rect.y;
        int ry = -rect.x - rect.width;
        int rw = rect.height;
        int rh = rect.width;
        rect.x = rx;
        rect.y = ry;
        rect.width = rw;
        rect.height = rh;
        return rect;
    }

    public int getFillRule() {
        return real.getFillRule();
    }

    public Font getFont() {
        return real.getFont();
    }

    public FontMetrics getFontMetrics() {
        return real.getFontMetrics();
    }

    public Color getForegroundColor() {
        return real.getForegroundColor();
    }

    public int getInterpolation() {
        return real.getInterpolation();
    }

    public int getLineCap() {
        return real.getLineCap();
    }

    public int getLineJoin() {
        return real.getLineJoin();
    }

    public int getLineStyle() {
        return real.getLineStyle();
    }

    public int getLineWidth() {
        return real.getLineWidth();
    }

    public int getTextAntialias() {
        return real.getTextAntialias();
    }

    public boolean getXORMode() {
        return real.getXORMode();
    }

    public void popState() {
        real.popState();
    }

    public void pushState() {
        real.pushState();
    }

    public void restoreState() {
        real.restoreState();
    }

    public void rotate(float degrees) {
        real.rotate(degrees);
    }

    public void scale(double amount) {
        real.scale(amount);
    }

    public void scale(float horizontal, float vertical) {
        real.scale(horizontal, vertical);
    }

    public void setAlpha(int alpha) {
        real.setAlpha(alpha);
    }

    public void setAntialias(int value) {
        real.setAntialias(value);
    }

    public void setBackgroundColor(Color rgb) {
        real.setBackgroundColor(rgb);
    }

    public void setBackgroundPattern(Pattern pattern) {
        if (lastBgPattern != null) {
            lastBgPattern.dispose();
            lastBgPattern = null;
        }
        if (pattern instanceof GradientPattern) {
            Pattern p = rotate((GradientPattern) pattern);
            real.setBackgroundPattern(p);
            lastBgPattern = p;
        } else if (pattern instanceof ImagePattern) {
            Pattern p = rotate((ImagePattern) pattern);
            real.setBackgroundPattern(p);
            lastBgPattern = p;
        } else {
            real.setBackgroundPattern(pattern);
        }
    }

    public void setClip(Path path) {
        Path p2 = rotate((Path) path);
        real.setClip(p2);
        p2.dispose();
    }

    public void setClip(Rectangle r) {
        real.setClip(rotate(r));
    }

    public void setFillRule(int rule) {
        real.setFillRule(rule);
    }

    public void setFont(Font f) {
        real.setFont(f);
    }

    public void setForegroundColor(Color rgb) {
        real.setForegroundColor(rgb);
    }

    public void setForegroundPattern(Pattern pattern) {
        if (lastFgPattern != null) {
            lastFgPattern.dispose();
            lastFgPattern = null;
        }
        if (pattern instanceof GradientPattern) {
            Pattern p = rotate((GradientPattern) pattern);
            real.setForegroundPattern(p);
            lastFgPattern = p;
        } else if (pattern instanceof ImagePattern) {
            Pattern p = rotate((ImagePattern) pattern);
            real.setForegroundPattern(p);
            lastFgPattern = p;
        } else {
            real.setForegroundPattern(pattern);
        }
    }

    public void setInterpolation(int interpolation) {
        real.setInterpolation(interpolation);
    }

    public void setLineCap(int cap) {
        real.setLineCap(cap);
    }

    public void setLineDash(int[] dash) {
        real.setLineDash(dash);
    }

    public void setLineJoin(int join) {
        real.setLineJoin(join);
    }

    public void setLineStyle(int style) {
        real.setLineStyle(style);
    }

    public void setLineWidth(int width) {
        real.setLineWidth(width);
    }

    public void setTextAntialias(int value) {
        real.setTextAntialias(value);
    }

    public void setXORMode(boolean b) {
        real.setXORMode(b);
    }

    public void shear(float horz, float vert) {
        real.shear(horz, vert);
    }

    public void translate(float dx, float dy) {
        real.translate(-dy, dx);
    }

    public void translate(int dx, int dy) {
        real.translate(-dy, dx);
    }

}