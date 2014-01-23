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
package org.xmind.ui.internal.spreadsheet.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class Cell extends BranchStructureData {

    private Chart ownedChart;

    private Row ownedRow;

    private Column ownedColumn;

    private List<Item> items = new ArrayList<Item>();

    private Dimension prefContentSize = null;

    private int contentHeight = -1;

    private Rectangle bounds = null;

    public Cell(Chart ownedChart, Row ownedRow, Column ownedColumn) {
        super(ownedRow.getHead());
        this.ownedChart = ownedChart;
        this.ownedRow = ownedRow;
        this.ownedColumn = ownedColumn;
    }

    public Chart getOwnedChart() {
        return ownedChart;
    }

    public Column getOwnedColumn() {
        return ownedColumn;
    }

    public Row getOwnedRow() {
        return ownedRow;
    }

    void addItem(Item item) {
        items.add(item);
        item.setOwnedCell(this);
    }

    void removeItem(Item item) {
        items.remove(item);
        item.setOwnedCell(null);
    }

    public List<Item> getItems() {
        return items;
    }

    public Dimension getPrefContentSize() {
        if (prefContentSize == null) {
            prefContentSize = calcPrefContentSize();
        }
        return prefContentSize;
    }

    private Dimension calcPrefContentSize() {
        int w = 0;
        int h = 0;
        int minorSpacing = getOwnedRow().getMinorSpacing();
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            IFigure itemFigure = item.getBranch().getFigure();
            Dimension size = itemFigure.getPreferredSize();
            w = Math.max(w, size.width);
            h += size.height;
            if (it.hasNext())
                h += minorSpacing;
        }
        return new Dimension(w, h);
    }

    public int getContentHeight() {
        if (contentHeight < 0) {
            contentHeight = calcContentHeight();
        }
        return contentHeight;
    }

    private int calcContentHeight() {
        if (!items.isEmpty()) {
            Item first = items.get(0);
            Item last = items.get(items.size() - 1);
            int height = last.getBranch().getFigure().getBounds().bottom()
                    - first.getBranch().getFigure().getBounds().y;
            IInsertion ins = (IInsertion) MindMapUtils.getCache(getBranch(),
                    IInsertion.CACHE_INSERTION);
            if (ins != null
                    && (ins.getIndex() == 0 || ins.getIndex() == items.size())
                    && getOwnedColumn()
                            .getHead()
                            .equals(
                                    MindMapUtils
                                            .getCache(
                                                    getBranch(),
                                                    Spreadsheet.KEY_INSERTION_COLUMN_HEAD))) {
                height += ins.getSize().height
                        + getOwnedRow().getMinorSpacing();
            }
            return height;
        }
        return 0;
    }

    public Item findItem(IBranchPart itemBranch) {
        for (Item item : items) {
            if (item.getBranch() == itemBranch)
                return item;
        }
        return null;
    }

    public Item getPreviousItem(Item item) {
        int index = getItemIndex(item);
        if (index > 0)
            return items.get(index - 1);
        return null;
    }

    public Item getNextItem(Item item) {
        int index = getItemIndex(item);
        if (index < items.size() - 1)
            return items.get(index + 1);
        return null;
    }

    public int getItemIndex(Item item) {
        return items.indexOf(item);
    }

    public int getX() {
        return getOwnedColumn().getLeft();
    }

    public int getY() {
        return getOwnedRow().getTop();
    }

    public int getWidth() {
        return getOwnedColumn().getWidth();
    }

    public int getHeight() {
        return getOwnedRow().getHeight();
    }

    public Rectangle getBounds() {
        if (bounds == null) {
            bounds = new Rectangle(getX(), getY(), getWidth(), getHeight());
        }
        return bounds;
    }

    public String toString() {
        return "[" + getOwnedRow().toString() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + getOwnedColumn().toString() + "]"; //$NON-NLS-1$
    }

}