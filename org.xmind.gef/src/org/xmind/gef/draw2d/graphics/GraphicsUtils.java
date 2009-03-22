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

import java.lang.reflect.Field;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.GEF;

/**
 * @author Brian Sun
 */
public class GraphicsUtils {

//    public static void closeAdvanced(Graphics g) {
//        GC gc = findGC(g);
//        if (gc != null)
//            gc.setAdvanced(false);
//    }
//
//    public static void openAdvanced(Graphics g) {
//        GC gc = findGC(g);
//        if (gc != null)
//            gc.setAdvanced(true);
//    }
//
//    public static boolean setClipping(Graphics g, Region region) {
//        GC gc = findGC(g);
//        if (gc != null) {
//            gc.setClipping(region);
//            return true;
//        }
//        return false;
//    }
//
//    private static Graphics findSWTGraphics(Graphics g) {
//        while (!(g instanceof SWTGraphics)) {
//            if (g == null)
//                return null;
//            if (g instanceof MapModeGraphics)
//                g = ((MapModeGraphics) g).getGraphics();
//            else if (g instanceof ScaledGraphics)
//                g = ((ScaledGraphics) g).getGraphics();
//            else if (g instanceof AlphaGraphics)
//                g = ((AlphaGraphics) g).getDelegate();
//            else if (g instanceof Rotate90Graphics)
//                g = ((Rotate90Graphics) g).getGraphics();
////            else if (g instanceof GrayedGraphics)
////                g = ((GrayedGraphics) g).getDelegate();
//            else {
//                Graphics delegatingGraphics = null;
//                try {
//                    Field[] fields = g.getClass().getDeclaredFields();
//                    for (Field f : fields) {
//                        Class<?> c = f.getDeclaringClass();
//                        if (Graphics.class.isAssignableFrom(c)) {
//                            f.setAccessible(true);
//                            delegatingGraphics = (Graphics) (f.get(g));
//                        }
//                    }
//                    continue;
//                } catch (Exception e) {
//                }
//                if (delegatingGraphics == null || delegatingGraphics == g)
//                    return null;
//                g = delegatingGraphics;
//            }
//        }
//        return g;
//    }
//
//    public static GC findGC(Graphics g) {
//        g = findSWTGraphics(g);
//        if (g == null)
//            return null;
//        Class c = SWTGraphics.class;
//        try {
//            Field f = c.getDeclaredField("gc"); //$NON-NLS-1$
//            f.setAccessible(true);
//            return (GC) (f.get(g));
//        } catch (Throwable e) {
//            Logger.log("Cannot find a field called 'gc' in this graphics: " //$NON-NLS-1$
//                    + g, e);
//        }
//        return null;
//    }

    public static GC findGC2(Graphics g) {
        try {
            while (g != null) {
                Object o = findGCOrGraphics(g);
                if (o == null)
                    return null;
                if (o instanceof GC)
                    return (GC) o;
                if (o instanceof Graphics)
                    g = (Graphics) o;
            }
        } catch (Throwable t) {
        }
        return null;
    }

    private static Object findGCOrGraphics(Graphics g) throws Exception {
        Field[] fs = g.getClass().getDeclaredFields();
        for (Field f : fs) {
            Class<?> c = f.getType();
            if (GC.class.isAssignableFrom(c)) {
                return getFieldObject(f, g);
            } else if (Graphics.class.isAssignableFrom(c)) {
                Object g2 = getFieldObject(f, g);
                return g2 == g ? null : g2;
            }
        }
        return null;
    }

    private static Object getFieldObject(Field f, Object o) throws Exception {
        boolean acc = f.isAccessible();
        f.setAccessible(true);
        Object ret = f.get(o);
        f.setAccessible(acc);
        return ret;
    }

    public static void fixGradientBugForCarbon(Graphics graphics, IFigure figure) {
        fixGradientBugForCarbon(graphics, figure.getBounds());
    }

    public static void fixGradientBugForCarbon(Graphics graphics,
            Rectangle bounds) {
        if (GEF.IS_CARBON) {
            graphics.pushState();
            graphics.setAlpha(0);
            graphics.setBackgroundColor(ColorConstants.white);
            graphics.fillRectangle(bounds);
            graphics.popState();
        }
    }

//    public static void preserveAdvancedAndRun(Graphics g, boolean advanced,
//            final Runnable runnable) {
//        g.pushState();
//        GC gc = findGC(g);
//        boolean oldAd = gc.getAdvanced();
//        gc.setAdvanced(advanced);
//        SafeRunner.run(new SafeRunnable() {
//            public void run() throws Exception {
//                runnable.run();
//            }
//        });
////        g.setAntialias( SWT.OFF );
////        g.setAntialias( SWT.ON );
//        gc.setAdvanced(oldAd);
////        g.setAlpha( 0xff - g.getAlpha() );
////        g.setAlpha( 0xff - g.getAlpha() );
//        g.popState();
//        g.restoreState();
//    }
//
//    public static void closeAdvancedAndRun(Graphics g, final Runnable runnable) {
//        g.pushState();
//        GC gc = findGC(g);
//        boolean advanced = gc.getAdvanced();
//        gc.setAdvanced(false);
//        SafeRunner.run(new SafeRunnable() {
//            public void run() throws Exception {
//                runnable.run();
//            }
//        });
//        gc.setAdvanced(advanced);
//        g.popState();
//        g.restoreState();
//    }

    private static final GraphicsUtils normal = new GraphicsUtils(false);
    private static final GraphicsUtils advanced = new GraphicsUtils(true);

    public static GraphicsUtils getNormal() {
        return normal;
    }

    public static GraphicsUtils getAdvanced() {
        return advanced;
    }

    private GC gc = null;
    private Font appliedFont = null;
    private FontMetrics metrics = null;

    protected GraphicsUtils(boolean advanced) {
        getGC().setAdvanced(advanced);
    }

    public GC getGC() {
        if (gc == null) {
            gc = new GC(new Shell());
        }
        return gc;
    }

    protected void setFont(Font f) {
        if (appliedFont == f || f.equals(appliedFont))
            return;
        getGC().setFont(f);
        appliedFont = f;
        metrics = null;
    }

    /**
     * @return the appliedFont
     */
    public Font getAppliedFont() {
        return appliedFont;
    }

    public Dimension getTextSize(String text, Font font) {
        setFont(font);
        return getTextSize(text);
    }

    public Dimension getStringSize(String string, Font font) {
        setFont(font);
        return getStringSize(string);
    }

    /**
     * @param text
     * @return
     */
    public Dimension getTextSize(String text) {
        return new Dimension(getGC().textExtent(text));
    }

    /**
     * @param string
     * @return
     */
    public Dimension getStringSize(String string) {
        return new Dimension(getGC().stringExtent(string));
    }

    /**
     * Returns the FontMetrics associated with the passed Font.
     * 
     * @param f
     *            the font
     * @return the FontMetrics for the given font
     * @see GC#getFontMetrics()
     */
    public FontMetrics getFontMetrics(Font f) {
        setFont(f);
        if (metrics == null)
            metrics = getGC().getFontMetrics();
        return metrics;
    }

    public static final int CENTER = 0;

    public static final int TRAIL = 1;

    public static final int LEAD = 2;

    public static final int PATH = 3;

    public String constrain(String path, int maxWidth, Font font,
            int startPositionHint) {
        Dimension size = getTextSize(path, font);
        if (size.width > maxWidth) {
            StringBuffer sb = new StringBuffer(path);
            int right;
            int left;
            int start;

            switch (startPositionHint) {
            case PATH:
                left = 1;
                right = sb.lastIndexOf(".") - 2; //$NON-NLS-1$
                start = right * 5 / 6;
                break;
            case LEAD:
                left = 0;
                right = sb.length() - 2;
                start = 0;
                break;
            case TRAIL:
                left = 1;
                right = sb.length() - 1;
                start = Math.max(right, left);
                break;
            default:
                left = 1;
                right = sb.length() - 2;
                start = right / 2;
            }
            sb.replace(start, start + 1, "..."); //$NON-NLS-1$
            size = getTextSize(sb.toString());
            boolean down = true;
            while (size.width > maxWidth && start <= right && start > left
                    && right > left) {
                if ((down || (right >= 0 && start + 4 > right)) && start > left) {
                    start--;
                }
                down = !down;
                sb.replace(start, start + 4, "..."); //$NON-NLS-1$
                if (right >= 0)
                    right--;
                size = getTextSize(sb.toString());
            }
            path = sb.toString();
        }
        return path;
    }

}