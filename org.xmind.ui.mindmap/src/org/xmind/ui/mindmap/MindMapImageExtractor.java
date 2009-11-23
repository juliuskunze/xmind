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
package org.xmind.ui.mindmap;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.image.FigureImageDescriptor;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.gef.util.Properties;
import org.xmind.ui.util.Logger;

public class MindMapImageExtractor {

    private static final int DEFAULT_MARGIN = 15;

    private Display display;

    private Composite parent;

    private ISheet sheet;

    private ITopic centralTopic;

    private Properties properties;

    private Image image;

    private Integer margin = null;

    private Point origin;

    private int resizeStrategy = ResizeConstants.RESIZE_NONE;

    private int widthHint = -1;

    private int heightHint = -1;

    public MindMapImageExtractor(Composite parent, ISheet sheet,
            ITopic centralTopic) {
        this(parent.getDisplay(), parent, sheet, centralTopic);
    }

    public MindMapImageExtractor(Display display, ISheet sheet,
            ITopic centralTopic) {
        this(display, null, sheet, centralTopic);
    }

    private MindMapImageExtractor(Display display, Composite parent,
            ISheet sheet, ITopic centralTopic) {
        this.display = display;
        this.parent = parent;
        this.sheet = sheet;
        this.centralTopic = centralTopic;
        setProperty(IMindMapViewer.VIEWER_CENTERED, Boolean.TRUE);
        setProperty(IMindMapViewer.VIEWER_CORNERED, Boolean.TRUE);
        setProperty(IMindMapViewer.VIEWER_MARGIN, Integer
                .valueOf(DEFAULT_MARGIN));
    }

    public void setProperty(String key, Object value) {
        if (properties == null)
            properties = new Properties();
        properties.set(key, value);
    }

    public Object getProperty(String key) {
        return properties == null ? null : properties.get(key);
    }

    public Image getImage() {
        if (image == null) {
            if (Thread.currentThread() != display.getThread()) {
                display.syncExec(new Runnable() {
                    public void run() {
                        image = createImage();
                    }
                });
            } else {
                image = createImage();
            }
        }
        return image;
    }

    public void setMargin(Integer margin) {
        this.margin = margin;
    }

    public void setResizeStrategy(int resizeStrategy, int widthHint,
            int heightHint) {
        this.resizeStrategy = resizeStrategy;
        this.widthHint = widthHint;
        this.heightHint = heightHint;
    }

    public int getResizeStrategy() {
        return resizeStrategy;
    }

    public int getWidthHint() {
        return widthHint;
    }

    public int getHeightHint() {
        return heightHint;
    }

    private Image createImage() {
        final MindMapExportContentProvider provider;
        if (parent != null) {
            provider = new MindMapExportContentProvider(parent, sheet,
                    centralTopic);
        } else {
            provider = new MindMapExportContentProvider(display, sheet,
                    centralTopic);
        }
        provider.setProperties(properties);
        provider.setMargin(margin);
        provider.setResizeStrategy(resizeStrategy, widthHint, heightHint);
        Image image;
        try {
            image = FigureImageDescriptor.createFromFigure(
                    provider.getContents(), provider).createImage(false,
                    display);
            origin = provider.getOrigin();
        } catch (Throwable e) {
            image = null;
            Logger.log(e);
        } finally {
            display.asyncExec(new Runnable() {
                public void run() {
                    provider.dispose();
                }
            });
        }
        return image;
    }

    public Point getOrigin() {
        return origin;
    }

    public void dispose() {
        if (image != null) {
            image.dispose();
            image = null;
        }
    }
}