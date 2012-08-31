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

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Frank Shaka
 */
public class FrameBorderLayout extends AbstractHintLayout {

    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer CENTER = new Integer(PositionConstants.CENTER);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer TOP = new Integer(PositionConstants.TOP);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer BOTTOM = new Integer(PositionConstants.BOTTOM);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer LEFT = new Integer(PositionConstants.LEFT);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer RIGHT = new Integer(PositionConstants.RIGHT);

    private IFigure center, left, top, bottom, right;
    private int vGap = 0, hGap = 0;

    private boolean useCenterAsHint = false;

    public boolean isUseCenterAsHint() {
        return useCenterAsHint;
    }

    public void setUseCenterAsHint(boolean useCenterAsHint) {
        this.useCenterAsHint = useCenterAsHint;
    }

    /**
     * @see org.eclipse.draw2d.AbstractHintLayout#calculateMinimumSize(IFigure,
     *      int, int)
     */
    protected Dimension calculateMinimumSize(IFigure container, int wHint,
            int hHint) {
        Insets border = container.getInsets();
        return new Dimension(border.getWidth(), border.getHeight());
//        int minWHint = 0, minHHint = 0;
//        if (wHint < 0) {
//            minWHint = -1;
//        }
//        if (hHint < 0) {
//            minHHint = -1;
//        }
//        Insets border = container.getInsets();
//        wHint = Math.max(minWHint, wHint - border.getWidth());
//        hHint = Math.max(minHHint, hHint - border.getHeight());
//        Dimension minSize = new Dimension();
//        int middleRowWidth = 0, middleRowHeight = 0;
//        int rows = 0, columns = 0;
//
//        Dimension centerSize;
//        if (useCenterAsHint && center != null && center.isVisible()) {
//            centerSize = center.getMinimumSize(wHint, hHint);
//        } else {
//            centerSize = null;
//        }
//
//        if (top != null && top.isVisible()) {
//            Dimension childSize = top.getMinimumSize(wHint, hHint);
//            hHint = Math.max(minHHint, hHint - (childSize.height + vGap));
//            minSize.setSize(childSize);
//            rows += 1;
//        }
//        if (bottom != null && bottom.isVisible()) {
//            Dimension childSize = bottom.getMinimumSize(wHint, hHint);
//            hHint = Math.max(minHHint, hHint - (childSize.height + vGap));
//            minSize.width = Math.max(minSize.width, childSize.width);
//            minSize.height += childSize.height;
//            rows += 1;
//        }
//        if (left != null && left.isVisible()) {
//            Dimension childSize = left.getMinimumSize(wHint, hHint);
//            middleRowWidth = childSize.width;
//            middleRowHeight = childSize.height;
//            wHint = Math.max(minWHint, wHint - (childSize.width + hGap));
//            columns += 1;
//        }
//        if (right != null && right.isVisible()) {
//            Dimension childSize = right.getMinimumSize(wHint, hHint);
//            middleRowWidth += childSize.width;
//            middleRowHeight = Math.max(childSize.height, middleRowHeight);
//            wHint = Math.max(minWHint, wHint - (childSize.width + hGap));
//            columns += 1;
//        }
//        if (center != null && center.isVisible()) {
//            Dimension childSize = center.getMinimumSize(wHint, hHint);
//            middleRowWidth += childSize.width;
//            middleRowHeight = Math.max(childSize.height, middleRowHeight);
//            columns += 1;
//        }
//
//        rows += columns > 0 ? 1 : 0;
//        // Add spacing, insets, and the size of the middle row
//        minSize.height += middleRowHeight + border.getHeight()
//                + ((rows - 1) * vGap);
//        minSize.width = Math.max(minSize.width, middleRowWidth)
//                + border.getWidth() + ((columns - 1) * hGap);
//
//        return minSize;
    }

    /**
     * @see AbstractLayout#calculatePreferredSize(IFigure, int, int)
     */
    protected Dimension calculatePreferredSize(IFigure container, int wHint,
            int hHint) {
//        int minWHint = 0, minHHint = 0;
//        if (wHint < 0)
//            minWHint = -1;
//        if (hHint < 0)
//            minHHint = -1;
//
        Insets border = container.getInsets();
//        wHint = Math.max(minWHint, wHint - border.getWidth());
//        hHint = Math.max(minHHint, hHint - border.getHeight());
        Dimension prefSize = new Dimension();
//        int rows = 0, columns = 0;

        if (center != null && center.isVisible()) {
            Dimension childSize = center.getPreferredSize();
            prefSize.setSize(childSize);
//            columns += 1;
        }

        if (top != null && top.isVisible()) {
//            wHint = prefSize.width;
//            hHint = Math.max(minHHint, hHint - (prefSize.height + vGap));
            Dimension childSize = top.getPreferredSize(prefSize.width, -1);
            prefSize.height += childSize.height + vGap;
//            rows += 1;
        } else if (bottom != null && bottom.isVisible()) {
//            hHint = Math.max(minHHint, hHint - (prefSize.height + vGap));
            Dimension childSize = bottom.getPreferredSize(prefSize.width, -1);
//            prefSize.width = Math.max(prefSize.width, childSize.width);
            prefSize.height += childSize.height + vGap;
//            rows += 1;
        } else if (left != null && left.isVisible()) {
//            wHint = Math.max(minWHint, wHint - (prefSize.width + hGap));
            Dimension childSize = left.getPreferredSize(-1, prefSize.height);
            prefSize.width += childSize.width + hGap;
//            columns += 1;
        } else if (right != null && right.isVisible()) {
            Dimension childSize = right.getPreferredSize(-1, prefSize.height);
            prefSize.width += childSize.width + hGap;
//            wHint = Math.max(minWHint, wHint - (childSize.width + hGap));
//            columns += 1;
        }

//        rows += columns > 0 ? 1 : 0;
        // Add spacing, insets, and the size of the middle row
//        prefSize.height += middleRowHeight + border.getHeight()
//                + ((rows - 1) * vGap);
//        prefSize.width = Math.max(prefSize.width, middleRowWidth)
//                + border.getWidth() + ((columns - 1) * hGap);
        prefSize.height += border.getHeight();
        prefSize.width += border.getHeight();

        return prefSize;
    }

    /**
     * @see org.eclipse.draw2d.LayoutManager#layout(IFigure)
     */
    public void layout(IFigure container) {
        Rectangle area = container.getClientArea();
        Rectangle rect = new Rectangle();

        int wHint = area.width;
        int hHint = area.height;
//        if ((top != null && top.isVisible())
//                || (bottom != null && bottom.isVisible())) {
//            hHint = -1;
//        } else if ((left != null && left.isVisible())
//                || (right != null && right.isVisible())) {
//            wHint = -1;
//        }

        Dimension centerSize;
        if (center != null && center.isVisible()) {
            centerSize = center.getPreferredSize(wHint, hHint);
        } else {
            centerSize = new Dimension();
        }

//        Dimension childSize;
        if (top != null && top.isVisible()) {
            rect.setLocation(area.x, area.y);
            rect.setSize(area.width, Math.max(0, area.height
                    - centerSize.height - vGap));
            top.setBounds(rect);
            area.y += rect.height + vGap;
            area.height -= rect.height + vGap;
        } else if (bottom != null && bottom.isVisible()) {
            rect.setLocation(area.x, area.y + centerSize.height + vGap);
            rect.setSize(area.width, Math.max(0, area.height
                    - centerSize.height - vGap));
            bottom.setBounds(rect);
            area.height -= rect.height + vGap;
        } else if (left != null && left.isVisible()) {
            rect.setLocation(area.x, area.y);
            rect.setSize(Math.max(0, area.width - centerSize.width - hGap),
                    area.height);
            left.setBounds(rect);
            area.x += rect.width + hGap;
            area.width -= rect.width + hGap;
        } else if (right != null && right.isVisible()) {
            rect.setLocation(area.x + centerSize.width + hGap, area.y);
            rect.setSize(Math.max(0, area.width - centerSize.width - hGap),
                    area.height);
            right.setBounds(rect);
            area.width -= rect.width + hGap;
        }

//        if (top != null && top.isVisible()) {
//            childSize = top.getPreferredSize(area.width, -1);
//            rect.setLocation(area.x, area.y);
//            rect.setSize(childSize);
//            rect.width = area.width;
//            top.setBounds(rect);
//            area.y += rect.height + vGap;
//            area.height -= rect.height + vGap;
//        }
//        if (bottom != null && bottom.isVisible()) {
//            childSize = bottom.getPreferredSize(Math.max(area.width, 0), -1);
//            rect.setSize(childSize);
//            rect.width = area.width;
//            rect.setLocation(area.x, area.y + area.height - rect.height);
//            bottom.setBounds(rect);
//            area.height -= rect.height + vGap;
//        }
//        if (left != null && left.isVisible()) {
//            childSize = left.getPreferredSize(-1, Math.max(0, area.height));
//            rect.setLocation(area.x, area.y);
//            rect.width = childSize.width;
//            rect.height = Math.max(0, area.height);
//            left.setBounds(rect);
//            area.x += rect.width + hGap;
//            area.width -= rect.width + hGap;
//        }
//        if (right != null && right.isVisible()) {
//            childSize = right.getPreferredSize(-1, Math.max(0, area.height));
//            rect.width = childSize.width;
//            rect.height = Math.max(0, area.height);
//            rect.setLocation(area.x + area.width - rect.width, area.y);
//            right.setBounds(rect);
//            area.width -= rect.width + hGap;
//        }
        if (center != null && center.isVisible()) {
            if (area.width < 0)
                area.width = 0;
            if (area.height < 0)
                area.height = 0;
            center.setBounds(area);
        }
    }

    /**
     * @see org.eclipse.draw2d.AbstractLayout#remove(IFigure)
     */
    public void remove(IFigure child) {
        if (center == child) {
            center = null;
        } else if (top == child) {
            top = null;
        } else if (bottom == child) {
            bottom = null;
        } else if (right == child) {
            right = null;
        } else if (left == child) {
            left = null;
        }
    }

    /**
     * Sets the location of hte given child in this layout. Valid constraints:
     * <UL>
     * <LI>{@link #CENTER}</LI>
     * <LI>{@link #TOP}</LI>
     * <LI>{@link #BOTTOM}</LI>
     * <LI>{@link #LEFT}</LI>
     * <LI>{@link #RIGHT}</LI>
     * <LI><code>null</code> (to remove a child's constraint)</LI>
     * </UL>
     * 
     * <p>
     * Ensure that the given Figure is indeed a child of the Figure on which
     * this layout has been set. Proper behaviour cannot be guaranteed if that
     * is not the case. Also ensure that every child has a valid constraint.
     * </p>
     * <p>
     * Passing a <code>null</code> constraint will invoke
     * {@link #remove(IFigure)}.
     * </p>
     * <p>
     * If the given child was assigned another constraint earlier, it will be
     * re-assigned to the new constraint. If there is another child with the
     * given constraint, it will be over-ridden so that the given child now has
     * that constraint.
     * </p>
     * 
     * @see org.eclipse.draw2d.AbstractLayout#setConstraint(IFigure, Object)
     */
    public void setConstraint(IFigure child, Object constraint) {
        remove(child);
        super.setConstraint(child, constraint);
        if (constraint == null) {
            return;
        }

        switch (((Integer) constraint).intValue()) {
        case PositionConstants.CENTER:
            center = child;
            break;
        case PositionConstants.TOP:
            top = child;
            break;
        case PositionConstants.BOTTOM:
            bottom = child;
            break;
        case PositionConstants.RIGHT:
            right = child;
            break;
        case PositionConstants.LEFT:
            left = child;
            break;
        default:
            break;
        }
    }

    /**
     * Sets the horizontal spacing to be used between the children. Default is
     * 0.
     * 
     * @param gap
     *            The horizonal spacing
     */
    public void setHorizontalSpacing(int gap) {
        hGap = gap;
    }

    /**
     * Sets the vertical spacing ot be used between the children. Default is 0.
     * 
     * @param gap
     *            The vertical spacing
     */
    public void setVerticalSpacing(int gap) {
        vGap = gap;
    }

}