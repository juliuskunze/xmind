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
package org.xmind.ui.internal.print;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.graphics.Rotate90Graphics;
import org.xmind.gef.draw2d.graphics.ScaledGraphics;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapExportContentProvider;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.UnitConvertor;

public class PrintClient {

    private static final int VIEWER_MARGIN = 15;

    private static final int TEXT_MARGIN = 5;

    private String jobName;

    private PrinterData printerData;

    private IDialogSettings settings;

    private Shell parentShell;

    private Printer printer;

    private Rectangle pageBounds;

    private Rectangle pageClientArea;

    private Insets pageMargins;

    private Point dpi;

    private boolean needRotate = false;

    private boolean jobStarted = false;

    private List<MindMapExportContentProvider> providers = null;

    public PrintClient(String jobName, Shell parentShell,
            PrinterData printerData, IDialogSettings settings) {
        this.jobName = jobName;
        this.parentShell = parentShell;
        this.printerData = printerData;
        this.settings = settings;
    }

    public PrintClient print(IMindMap source) {
        if (!jobStarted) {
            if (!getPrinter().startJob(jobName))
                return this;
            jobStarted = true;
        }
        if (!getPrinter().startPage())
            return this;

        GC gc = new GC(getPrinter());
        gc.setClipping(pageClientArea.x, pageClientArea.y - 1,
                pageClientArea.width, pageClientArea.height + 1);
        drawSourceContent(gc, source);

        gc.setClipping((org.eclipse.swt.graphics.Rectangle) null);

        if (!settings.getBoolean(PrintConstants.NO_BORDER)) {
            drawBorder(gc);
        }

        String headerText = settings.get(PrintConstants.HEADER_TEXT);
        if (headerText != null && !"".equals(headerText)) { //$NON-NLS-1$
            drawHeader(gc, headerText);
        }

        String footerText = settings.get(PrintConstants.FOOTER_TEXT);
        if (footerText != null && !"".equals(footerText)) { //$NON-NLS-1$
            drawFooter(gc, footerText);
        }

        gc.dispose();
        getPrinter().endPage();
        return this;
    }

    private void drawHeader(GC gc, String text) {
        Font font = getFont(PrintConstants.HEADER_FONT);
        try {
            drawText(gc, text, font, getAlign(PrintConstants.HEADER_ALIGN,
                    PositionConstants.CENTER), true);
        } finally {
            font.dispose();
        }
    }

    private void drawFooter(GC gc, String text) {
        Font font = getFont(PrintConstants.FOOTER_FONT);
        try {
            drawText(gc, text, font, getAlign(PrintConstants.FOOTER_ALIGN,
                    PositionConstants.RIGHT), false);
        } finally {
            font.dispose();
        }
    }

    private int getAlign(String alignKey, int defaultAlign) {
        return PrintConstants.toDraw2DAlignment(settings.get(alignKey),
                defaultAlign);
    }

    private Font getFont(String fontKey) {
        Font font = null;
        String fontValue = settings.get(fontKey);
        if (fontValue != null) {
            FontData[] fontData = FontUtils.toFontData(fontValue);
            if (fontData != null) {
                for (FontData fd : fontData) {
                    fd.setHeight(fd.getHeight() * dpi.y
                            / UnitConvertor.getScreenDpi().y);
                }
                font = new Font(Display.getCurrent(), fontData);
            }
        }
        if (font == null) {
            FontData[] defaultFontData = JFaceResources
                    .getDefaultFontDescriptor().getFontData();
            int defaultHeight = defaultFontData[0].getHeight();
            font = new Font(Display.getCurrent(), FontUtils.newHeight(
                    defaultFontData, defaultHeight * dpi.y
                            / UnitConvertor.getScreenDpi().y));
        }
        return font;
    }

    private void drawText(GC gc, String text, Font font, int alignment,
            boolean top) {
        RotatableWrapLabel label = new RotatableWrapLabel();
        label.setText(text);
        label.setFont(font);
        label.setTextAlignment(alignment);
        label.setForegroundColor(parentShell.getDisplay().getSystemColor(
                SWT.COLOR_BLACK));
        int width = needRotate ? pageClientArea.height : pageClientArea.width;
        int height = needRotate ? pageClientArea.width : pageClientArea.height;
        int marginWidth = TEXT_MARGIN * dpi.x / UnitConvertor.getScreenDpi().x;
        int marginHeight = TEXT_MARGIN * dpi.y / UnitConvertor.getScreenDpi().y;
        if (needRotate) {
            int temp = marginWidth;
            marginWidth = marginHeight;
            marginHeight = temp;
        }
        width -= marginWidth * 2;
        height -= marginHeight * 2;

        Dimension size = label.getPreferredSize(width, -1);
        int x = -width / 2;
        int y = top ? -height / 2 : height / 2 - size.height;
        label.setBounds(new Rectangle(x, y, width, size.height));

        SWTGraphics baseGraphics = new SWTGraphics(gc);
        baseGraphics.translate(pageClientArea.x + pageClientArea.width / 2,
                pageClientArea.y + pageClientArea.height / 2);

        Graphics graphics = baseGraphics;

        Rotate90Graphics rotatedGraphics = null;
        if (needRotate) {
            rotatedGraphics = new Rotate90Graphics(graphics);
            graphics = rotatedGraphics;
        }
        try {
            label.paint(graphics);
        } catch (Throwable e) {
            Logger.log(e, "Error occurred while printing"); //$NON-NLS-1$
        } finally {
            if (rotatedGraphics != null) {
                rotatedGraphics.dispose();
            }
            baseGraphics.dispose();
        }
    }

    private void drawBorder(GC gc) {
        gc.setForeground(parentShell.getDisplay().getSystemColor(
                SWT.COLOR_BLACK));
        gc.setLineWidth(1);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.drawRectangle(pageClientArea.x, pageClientArea.y,
                pageClientArea.width, pageClientArea.height);
    }

    private void drawSourceContent(GC gc, IMindMap source) {
        MindMapExportContentProvider provider = new MindMapExportContentProvider(
                parentShell, source);
        if (providers == null) {
            providers = new ArrayList<MindMapExportContentProvider>();
        }
        providers.add(provider);
        provider.setProperty(IMindMapViewer.VIEWER_MARGIN, Math.max(
                pageBounds.width, pageBounds.height));
        provider.setMargin(VIEWER_MARGIN * dpi.x
                / UnitConvertor.getScreenDpi().x);
        provider.setProperty(IMindMapViewer.VIEWER_GRADIENT, Boolean.FALSE);
        provider.setResizeStrategy(ResizeConstants.RESIZE_STRETCH,
                needRotate ? pageClientArea.height : pageClientArea.width,
                needRotate ? pageClientArea.width : pageClientArea.height);
        try {
            Rectangle exportArea = provider.getExportArea();
            double scale = provider.getScale();
            IFigure contents = provider.getContents();
            if (settings.getBoolean(PrintConstants.NO_BACKGROUND)) {
                Layer layer = provider.getViewer().getLayer(
                        GEF.LAYER_BACKGROUND);
                if (layer != null) {
                    layer.setOpaque(false);
                }
            }

            SWTGraphics baseGraphcis = new SWTGraphics(gc);
            baseGraphcis.translate(pageClientArea.x
                    - (needRotate ? (-exportArea.y - exportArea.height)
                            : exportArea.x), pageClientArea.y
                    - (needRotate ? exportArea.x : exportArea.y));
            Graphics graphics = baseGraphcis;
            ScaledGraphics scaledGraphics = null;
            Rotate90Graphics rotatedGraphics = null;
            if (scale > 0) {
                scaledGraphics = new ScaledGraphics(graphics);
                scaledGraphics.scale(scale);
                graphics = scaledGraphics;
            }
            if (needRotate) {
                rotatedGraphics = new Rotate90Graphics(graphics);
                graphics = rotatedGraphics;
            }
            try {
                contents.paint(graphics);
            } catch (Throwable e) {
                Logger.log(e, "Error occurred while painting mind map: " //$NON-NLS-1$
                        + source.getCentralTopic().getTitleText());
            } finally {
                if (rotatedGraphics != null) {
                    rotatedGraphics.dispose();
                }
                if (scaledGraphics != null) {
                    scaledGraphics.dispose();
                }
                baseGraphcis.dispose();
            }
        } catch (Throwable e) {
            Logger.log(e, "Error occurred while painting mind map: " //$NON-NLS-1$
                    + source.getCentralTopic().getTitleText());
        }
    }

    protected Printer getPrinter() {
        if (printer == null) {
            printer = createPrinter();
            getPrinterInfos();
        }
        return printer;
    }

    private void getPrinterInfos() {
        dpi = new Point(printer.getDPI());
        pageBounds = new Rectangle(printer.getBounds());
        Rectangle trim = new Rectangle(printer.computeTrim(0, 0, 0, 0));
        pageMargins = new Insets(-trim.y, -trim.x, trim.right(), trim.bottom());

        pageClientArea = new Rectangle(printer.getClientArea());
        pageClientArea.x = pageBounds.x
                + (pageBounds.width - pageClientArea.width) / 2;
        pageClientArea.y = pageBounds.y
                + (pageBounds.height - pageClientArea.height) / 2;
        int leftMargin = getUserMargin(PrintConstants.LEFT_MARGIN);
        int rightMargin = getUserMargin(PrintConstants.RIGHT_MARGIN);
        int topMargin = getUserMargin(PrintConstants.TOP_MARGIN);
        int bottomMargin = getUserMargin(PrintConstants.BOTTOM_MARGIN);
        pageClientArea.expand(pageMargins);
        pageClientArea.x += leftMargin;
        pageClientArea.y += topMargin;
        pageClientArea.width -= leftMargin + rightMargin;
        pageClientArea.height -= topMargin + bottomMargin;

        needRotate = pageBounds.height > pageBounds.width;
    }

    private int getUserMargin(String key) {
        double margin;
        try {
            margin = settings.getDouble(key);
        } catch (NumberFormatException e) {
            margin = PrintConstants.DEFAULT_MARGIN;
        }
        double dpi;
        if (PrintConstants.LEFT_MARGIN.equals(key)
                || PrintConstants.RIGHT_MARGIN.equals(key)) {
            dpi = this.dpi.x;
        } else {
            dpi = this.dpi.y;
        }
        return (int) (margin * dpi);
    }

    private Printer createPrinter() {
        return new Printer(printerData);
    }

    public void dispose() {
        if (printer != null) {
            if (!printer.isDisposed()) {
                printer.endJob();
            }
            printer.dispose();
            printer = null;
        }
        jobStarted = false;
        if (providers != null) {
            for (Object o : providers.toArray()) {
                ((MindMapExportContentProvider) o).dispose();
            }
            providers = null;
        }
    }
}