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
package org.xmind.ui.internal.sharing;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.internal.sharing.LocalNetworkSharing;
import org.xmind.core.sharing.ILocalSharedMap;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharedMap;
import org.xmind.core.sharing.SharingConstants;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.IGraphicalToolTipProvider;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharedMapLabelProvider extends LabelProvider implements
        IFontProvider, IColorProvider, IGraphicalToolTipProvider {

    private Map<Object, Image> imageCache = new HashMap<Object, Image>();

    @Override
    public String getText(Object element) {
        if (element instanceof ISharedMap)
            return ((ISharedMap) element).getResourceName();
        if (element instanceof ISharedLibrary) {
            ISharedLibrary library = (ISharedLibrary) element;
            int mapCount = library.getMapCount();
            String name = library.getName();
            if (library.isLocal())
                return getLibraryTitle(mapCount, name);

            String arrangeMode = LocalNetworkSharingUI.getDefault()
                    .getPreferenceStore()
                    .getString(SharingConstants.PREF_ARRANGE_MODE);

            if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(arrangeMode)
                    || arrangeMode.equals(SharingConstants.ARRANGE_MODE_PEOPLE)) {
                String contactID = library.getContactID();
                if (contactID == null || "".equals(contactID)) { //$NON-NLS-1$
                    return getLibraryTitle(mapCount, name);
                }

                if (!LocalNetworkSharing.getDefault().getSharingService()
                        .getContactManager().isContact(contactID)) {
                    return name;
                }
            }
            return getLibraryTitle(mapCount, name);
        }
        return super.getText(element);
    }

    private String getLibraryTitle(int mapCount, String name) {
        if (mapCount == 0)
            return NLS
                    .bind(SharingMessages.SharedLibrary_title_withLibraryName_and_ZeroMaps,
                            name);
        if (mapCount == 1)
            return NLS
                    .bind(SharingMessages.SharedLibrary_title_withLibraryName_and_OneMap,
                            name);
        return NLS
                .bind(SharingMessages.SharedLibrary_title_withLibraryName_and_MoreThanOneMaps,
                        name, mapCount);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof ISharedMap) {
            ISharedMap map = (ISharedMap) element;
            String key = getMapKey(map);
            Image image = imageCache.get(key);
            if (image == null) {
                image = createImage(map);
                imageCache.put(key, image);
            }
            return image;
        }
        return super.getImage(element);
    }

    private String getMapKey(ISharedMap map) {
        return map.isMissing() ? map.getID() + "@missing" : map.getID(); //$NON-NLS-1$
    }

    private Image createImage(ISharedMap map) {
        byte[] thumbnail = map.getThumbnailData();
        if (thumbnail == null || "".equals(thumbnail)) //$NON-NLS-1$
            return null;
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(thumbnail);
            Image image = new Image(Display.getCurrent(), stream);
            if (map.isMissing()) {
                Image disabledImage = new Image(Display.getCurrent(), image,
                        SWT.IMAGE_GRAY);
                Rectangle size = disabledImage.getBounds();
                Image semiTransparentImage = new Image(Display.getCurrent(),
                        size.width, size.height);
                GC gc = new GC(semiTransparentImage);
                gc.setBackground(Display.getCurrent().getSystemColor(
                        SWT.COLOR_WHITE));
                gc.fillRectangle(0, 0, size.width, size.height);
                gc.setAlpha(0xDD);
                gc.drawImage(disabledImage, 0, 0);
                gc.dispose();
                image.dispose();
                disabledImage.dispose();
                image = semiTransparentImage;
            }
            return image;
        } catch (Throwable e) {
            LocalNetworkSharingUI.log(null, e);
            return null;
        }
    }

    public void invalidateImageCache(Object[] elements) {
        for (int i = 0; i < elements.length; i++) {
            Object element = elements[i];
            deleteImageCache(element);
            if (element instanceof ISharedMap) {
                deleteImageCache(((ISharedMap) element).getID() + "@missing"); //$NON-NLS-1$
                deleteImageCache(((ISharedMap) element).getID());
            }
        }
        fireLabelProviderChanged(new LabelProviderChangedEvent(this, elements));
    }

    private void deleteImageCache(Object element) {
        Image image = imageCache.remove(element);
        if (image != null) {
            image.dispose();
        }
    }

    @Override
    public void dispose() {
        Object[] images = imageCache.values().toArray();
        imageCache.clear();
        for (Object image : images) {
            if (image != null)
                ((Image) image).dispose();
        }
        super.dispose();
    }

    public Font getFont(Object element) {
        return FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                Util.isMac() ? -2 : -1);
    }

    public Color getForeground(Object element) {
        if (element instanceof ISharedMap) {
            ISharedMap map = (ISharedMap) element;
            if (map.isMissing()) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
            }
        }
        return null;
    }

    public Color getBackground(Object element) {
        return null;
    }

    private String formatTime(long time) {
        return time == 0 ? "(unknown)" : String.format( //$NON-NLS-1$
                "%1$tb %1$td, %1$tY %1$tH:%1$tM:%1$tS", Long.valueOf(time)); //$NON-NLS-1$
    }

    public IFigure getToolTipFigure(Object element) {
        if (element instanceof ISharedMap) {
            ISharedMap map = (ISharedMap) element;
            if (map.getSharedLibrary().isLocal()
                    && map instanceof ILocalSharedMap) {
                ILocalSharedMap localMap = (ILocalSharedMap) map;
                String path = localMap.getResourcePath();
                long modifiedTime = localMap.getResourceModifiedTime();
                long addedTime = localMap.getAddedTime();
                boolean missing = localMap.isMissing();

                IFigure tooltip = new Figure();
                ToolbarLayout layout = new ToolbarLayout(false);
                layout.setSpacing(1);
                tooltip.setLayoutManager(layout);
                if (missing) {
                    Label statusFigure = new Label(
                            SharingMessages.SharedMap_tooltip_MapIsMissing_warningText);
                    statusFigure.setFont(FontUtils
                            .getBold(JFaceResources.DEFAULT_FONT));
                    tooltip.add(statusFigure);
                }

                Label pathLabel = new Label(path);
                pathLabel.setFont(JFaceResources.getDefaultFont());
                tooltip.add(pathLabel);

                IFigure attrsFigure = new Figure();
                attrsFigure.setBorder(new MarginBorder(7, 0, 0, 0));
                ToolbarLayout attrLayout = new ToolbarLayout(false);
                attrLayout.setSpacing(1);
                attrsFigure.setLayoutManager(attrLayout);
                Font attrFont = FontUtils.getRelativeHeight(
                        JFaceResources.DEFAULT_FONT, -2);
                Label addedLabel = new Label(
                        NLS.bind(
                                SharingMessages.SharedMap_tooltip_AddedTime_text_withTime,
                                formatTime(addedTime)));
                addedLabel.setFont(attrFont);
                attrsFigure.add(addedLabel);
                if (!missing) {
                    Label modifiedLabel = new Label(
                            NLS.bind(
                                    SharingMessages.SharedMap_tooltip_ModifiedTime_text_withTime,
                                    formatTime(modifiedTime)));
                    modifiedLabel.setFont(attrFont);
                    attrsFigure.add(modifiedLabel);
                }
                tooltip.add(attrsFigure);
                return tooltip;
            }
        }
        return null;
    }

}