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

import static org.xmind.ui.gallery.GalleryLayout.ALIGN_CENTER;
import static org.xmind.ui.gallery.GalleryLayout.ALIGN_FILL;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.LayoutManager;
import org.xmind.gef.draw2d.AdvancedToolbarLayout;

public class ContentPane extends Figure {

    private AdvancedToolbarLayout layout = null;

    private FlowLayout wrapLayout = null;

    private int minorAlign = -1;

    private int minorSpacing = -1;

    /**
     * 
     */
    public ContentPane() {
        this(false, false, false);
    }

    /**
     * @param isHorizontal
     * @param stretchMinorAxis
     * @param wrap
     */
    public ContentPane(boolean isHorizontal, boolean stretchMinorAxis,
            boolean wrap) {
        if (wrap) {
            wrapLayout = new FlowLayout(isHorizontal);
            wrapLayout.setStretchMinorAxis(stretchMinorAxis);
            wrapLayout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
            wrapLayout.setMinorAlignment(FlowLayout.ALIGN_CENTER);
            wrapLayout.setMajorSpacing(10);
            wrapLayout.setMinorSpacing(5);
            super.setLayoutManager(wrapLayout);
        } else {
            layout = new AdvancedToolbarLayout(isHorizontal);
            layout.setStretchMinorAxis(stretchMinorAxis);
            layout.setMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
            layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
            layout.setInnerMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
            layout.setSpacing(10);
            super.setLayoutManager(layout);
        }

    }

    public void setLayoutManager(LayoutManager manager) {
        // Do nothing to prevent external layout manager to be set.
    }

    public boolean isHorizontal() {
        if (isWrap())
            return wrapLayout.isHorizontal();
        return layout.isHorizontal();
    }

    public void setHorizontal(boolean horizontal) {
        if (horizontal == isHorizontal())
            return;

        if (wrapLayout != null)
            wrapLayout.setHorizontal(horizontal);
        if (layout != null)
            layout.setHorizontal(horizontal);
        revalidate();
    }

    public boolean isWrap() {
        return getLayoutManager() == wrapLayout;
    }

    public void setWrap(boolean wrap) {
        if (wrap == isWrap())
            return;
        if (wrap) {
            if (wrapLayout == null) {
                boolean horizontal = isHorizontal();
                int majorAlignment = getMajorAlignment();
                int minorAlignment = getMinorAlignment();
                int majorSpacing = getMajorSpacing();
                int minorSpacing = getMinorSpacing();
                wrapLayout = new FlowLayout(horizontal);
                wrapLayout.setMajorAlignment(majorAlignment);
                wrapLayout.setMajorSpacing(majorSpacing);
                wrapLayout.setMinorSpacing(minorSpacing);
                boolean fill = minorAlignment == ALIGN_FILL;
                wrapLayout.setStretchMinorAxis(fill);
                wrapLayout.setMinorAlignment(fill ? ALIGN_CENTER
                        : minorAlignment);
            }
            super.setLayoutManager(wrapLayout);
        } else {
            if (layout == null) {
                boolean horizontal = isHorizontal();
                int majorAlignment = getMajorAlignment();
                int minorAlignment = getMinorAlignment();
                layout = new AdvancedToolbarLayout(horizontal);
                layout.setMajorAlignment(majorAlignment);
                layout.setSpacing(minorSpacing);
                boolean fill = minorAlignment == ALIGN_FILL;
                layout.setStretchMinorAxis(fill);
                layout.setMinorAlignment(fill ? ALIGN_CENTER : minorAlignment);
            }
            super.setLayoutManager(layout);
        }
    }

    public int getMajorAlignment() {
        if (isWrap())
            return wrapLayout.getMajorAlignment();
        return layout.getMajorAlignment();
    }

    public int getMinorAlignment() {
        return minorAlign;
    }

    public void setMajorAlignment(int alignment) {
        if (alignment == getMajorAlignment())
            return;

        if (wrapLayout != null)
            wrapLayout.setMajorAlignment(alignment);
        if (layout != null)
            layout.setMajorAlignment(alignment);
        revalidate();
    }

    public void setMinorAlignment(int alignment) {
        if (minorAlign >= 0 && alignment == getMinorAlignment())
            return;

        this.minorAlign = alignment;
        boolean fill = alignment == ALIGN_FILL;
        if (wrapLayout != null) {
            wrapLayout.setStretchMinorAxis(fill);
            wrapLayout.setMinorAlignment(fill ? ALIGN_CENTER : alignment);
        }
        if (layout != null) {
            layout.setStretchMinorAxis(fill);
            layout.setInnerMinorAlignment(fill ? ALIGN_CENTER : alignment);
        }
        revalidate();
    }

    public int getMajorSpacing() {
        if (isWrap())
            return wrapLayout.getMajorSpacing();
        return layout.getSpacing();
    }

    public void setMajorSpacing(int spacing) {
        if (spacing == getMajorSpacing())
            return;

        if (wrapLayout != null)
            wrapLayout.setMajorSpacing(spacing);
        if (layout != null)
            layout.setSpacing(spacing);
        revalidate();
    }

    public int getMinorSpacing() {
        return minorSpacing;
    }

    public void setMinorSpacing(int spacing) {
        if (minorSpacing >= 0 && spacing == getMinorSpacing())
            return;

        this.minorSpacing = spacing;
        if (wrapLayout != null)
            wrapLayout.setMinorSpacing(spacing);
        revalidate();
    }

}