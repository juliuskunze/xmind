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
package org.xmind.ui.internal.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.internal.layers.BackgroundLayer;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class SheetDecorator extends Decorator {

    private static final SheetDecorator instance = new SheetDecorator();

    private static final String CACHE_WALLPAPER_KEY = "org.xmind.ui.cache.wallpaperKey"; //$NON-NLS-1$

    protected static class WallpaperImageRegistry {
        private static class Key {
            IWorkbook workbook;
            String entryPath;

            public Key(IWorkbook workbook, String entryPath) {
                this.workbook = workbook;
                this.entryPath = entryPath;
            }

            public int hashCode() {
                return workbook.hashCode() ^ entryPath.hashCode();
            }

            public boolean equals(Object obj) {
                if (obj == this)
                    return true;
                if (obj == null || !(obj instanceof Key))
                    return false;
                Key that = (Key) obj;
                return this.workbook.equals(that.workbook)
                        && this.entryPath.equals(that.entryPath);
            }
        }

        private static class Entry {

            Key key;

            ImageDescriptor imageDescriptor;

            Image image;

            Set<IGraphicalPart> hosts;

            public Entry(Key key) {
                this.key = key;
                this.imageDescriptor = createImageDescriptor();
            }

            private ImageDescriptor createImageDescriptor() {
                return AttachmentImageDescriptor.createFromEntryPath(
                        key.workbook, key.entryPath);
            }

            public Image getImage(IGraphicalPart host) {
                if (hosts == null)
                    hosts = new HashSet<IGraphicalPart>();
                hosts.add(host);
                if (image == null) {
                    image = imageDescriptor.createImage(false);
                }
                return image;
            }

            public void remove(IGraphicalPart host) {
                if (hosts != null)
                    hosts.remove(host);
            }

            public boolean isEmpty() {
                return hosts == null || hosts.isEmpty();
            }

            public void dispose() {
                if (image != null) {
                    image.dispose();
                    image = null;
                }
            }

        }

        private Map<Key, Entry> map = new HashMap<Key, Entry>();

        public Image getImage(IGraphicalPart host, IStyleSelector ss) {
            Key oldKey = (Key) MindMapUtils.getCache(host, CACHE_WALLPAPER_KEY);
            Key newKey = createKey(host, ss);
            MindMapUtils.setCache(host, CACHE_WALLPAPER_KEY, newKey);
            if (oldKey != null && !oldKey.equals(newKey)) {
                remove(host, oldKey);
            }
            if (newKey != null) {
                Entry entry = map.get(newKey);
                if (entry == null) {
                    entry = new Entry(newKey);
                    map.put(newKey, entry);
                }
                if (entry != null)
                    return entry.getImage(host);
            }
            return null;
        }

        private Key createKey(IGraphicalPart host, IStyleSelector ss) {
            String entryPath = getWallpaperAttachmentPath(host, ss);
            if (entryPath != null) {
                IWorkbook workbook = getWorkbook(host);
                if (workbook != null)
                    return new Key(workbook, entryPath);
            }
            return null;
        }

        public void removeHost(IGraphicalPart host, IStyleSelector ss) {
            Key key = createKey(host, ss);
            if (key != null) {
                remove(host, key);
            }
        }

        private void remove(IGraphicalPart host, Key key) {
            Entry entry = map.get(key);
            if (entry != null) {
                entry.remove(host);
                if (entry.isEmpty()) {
                    map.remove(key);
                    entry.dispose();
                }
            }
        }

        private String getWallpaperAttachmentPath(IGraphicalPart host,
                IStyleSelector ss) {
            String value = ss.getStyleValue(host, Styles.Background);
            if (value != null) {
                if (HyperlinkUtils.isAttachmentURL(value)) {
                    return HyperlinkUtils.toAttachmentPath(value);
                }
            }
            return null;
        }

        private IWorkbook getWorkbook(IGraphicalPart host) {
            IViewer viewer = host.getSite().getViewer();
            if (viewer instanceof IMindMapViewer) {
                ISheet sheet = ((IMindMapViewer) viewer).getSheet();
                if (sheet != null)
                    return sheet.getOwnedWorkbook();
            }
            return null;
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

    }

    private WallpaperImageRegistry imageRegistry = null;

    protected SheetDecorator() {
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        IStyleSelector ss = StyleUtils.getStyleSelector(part);
        decorateSheet(part, ss, figure);
    }

    private void decorateSheet(IGraphicalPart part, IStyleSelector ss,
            IFigure figure) {
        IGraphicalViewer viewer = (IGraphicalViewer) part.getSite().getViewer();
        Layer layer = viewer.getLayer(GEF.LAYER_BACKGROUND);
        if (layer != null) {
            decorateBackground(part, ss, layer);
        }
    }

    private void decorateBackground(IGraphicalPart part, IStyleSelector ss,
            Layer layer) {
        layer.setBackgroundColor(StyleUtils.getColor(part, ss,
                Styles.FillColor, null, Styles.DEF_SHEET_FILL_COLOR));

        if (layer instanceof BackgroundLayer) {
            BackgroundLayer bgLayer = (BackgroundLayer) layer;
            bgLayer.setWallpaper(getWallpaper(part, ss));
            bgLayer.setSubAlpha(getWallpaperAlpha(part, ss));
        }
    }

    private int getWallpaperAlpha(IGraphicalPart part, IStyleSelector ss) {
        double opacity = StyleUtils.getDouble(part, ss, Styles.Opacity, 0.8);
        return (int) Math.round(opacity * 255);
    }

    /**
     * Create a new wallpaper pattern or return no pattern.
     * 
     * @param part
     * @param ss
     * @return
     */
    private Pattern getWallpaper(IGraphicalPart part, IStyleSelector ss) {
        Image image = getWallpaperImage(part, ss);
        return image == null ? null : new Pattern(Display.getCurrent(), image);
    }

    private Image getWallpaperImage(IGraphicalPart part, IStyleSelector ss) {
        if (imageRegistry == null)
            imageRegistry = new WallpaperImageRegistry();
        return imageRegistry.getImage(part, ss);
    }

    public void deactivate(IGraphicalPart part, IFigure figure) {
        IGraphicalViewer viewer = (IGraphicalViewer) part.getSite().getViewer();
        Layer layer = viewer.getLayer(GEF.LAYER_BACKGROUND);
        if (layer instanceof BackgroundLayer) {
            Pattern wallpaper = ((BackgroundLayer) layer).getWallpaper();
            if (wallpaper != null) {
                wallpaper.dispose();
            }
            ((BackgroundLayer) layer).setWallpaper(null);
        }
        if (imageRegistry != null) {
            imageRegistry.removeHost(part, StyleUtils.getStyleSelector(part));
        }
        super.deactivate(part, figure);
    }

    public static SheetDecorator getInstance() {
        return instance;
    }
}