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
package org.xmind.ui.color;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Frank Shaka
 */
public class PaletteItem {

    public RGB color;

    public String description;

    public PaletteItem(RGB color, String description) {
        this.color = color;
        this.description = description;
    }

    public PaletteItem(int rgb, String description) {
        this(new RGB((rgb >> 16) & 0xff, // r 
                (rgb >> 8) & 0xff, //g
                rgb & 0xff), //b
                description);
    }

    public String toString() {
        return "{color=" + color + ", description=" + description + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public int hashCode() {
        if (color != null)
            return color.hashCode();
        return 0;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof PaletteItem))
            return false;
        PaletteItem that = (PaletteItem) obj;
        return equals(this.color, that.color)
                && equals(this.description, that.description);
    }

    private static boolean equals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    public static final PaletteItem Black = new PaletteItem(0,
            PaletteMessages.PaletteItem_Black);
    public static final PaletteItem Red = new PaletteItem(0xff0000,
            PaletteMessages.PaletteItem_Red);
    public static final PaletteItem Yellow = new PaletteItem(0xffff00,
            PaletteMessages.PaletteItem_Yellow);
    public static final PaletteItem Green = new PaletteItem(0x00ff00,
            PaletteMessages.PaletteItem_Green);
    public static final PaletteItem Blue = new PaletteItem(0x0000ff,
            PaletteMessages.PaletteItem_Blue);
    public static final PaletteItem Purple = new PaletteItem(0xff00ff,
            PaletteMessages.PaletteItem_Purple);
    public static final PaletteItem DarkGray = new PaletteItem(0x404040,
            PaletteMessages.PaletteItem_DarkGray);
    public static final PaletteItem DarkRed = new PaletteItem(0x800000,
            PaletteMessages.PaletteItem_DarkRed);
    public static final PaletteItem DarkYellow = new PaletteItem(0x808000,
            PaletteMessages.PaletteItem_DarkYellow);
    public static final PaletteItem DarkGreen = new PaletteItem(0x008000,
            PaletteMessages.PaletteItem_DarkGreen);
    public static final PaletteItem DarkBlue = new PaletteItem(0x000080,
            PaletteMessages.PaletteItem_DarkBlue);
    public static final PaletteItem DarkPurple = new PaletteItem(0x800080,
            PaletteMessages.PaletteItem_DarkPurple);
    public static final PaletteItem Gray = new PaletteItem(0x808080,
            PaletteMessages.PaletteItem_Gray);
    public static final PaletteItem Rose = new PaletteItem(0xFF9D96,
            PaletteMessages.PaletteItem_Rose);
    public static final PaletteItem Orange = new PaletteItem(0xffa500,
            PaletteMessages.PaletteItem_Orange);
    public static final PaletteItem Lemon = new PaletteItem(0xFFE76D,
            PaletteMessages.PaletteItem_Lemon);
    public static final PaletteItem LimeGreen = new PaletteItem(0x32cd32,
            PaletteMessages.PaletteItem_LimeGreen);
    public static final PaletteItem Turquoise = new PaletteItem(0x40e0d0,
            PaletteMessages.PaletteItem_Turquoise);
    public static final PaletteItem LightGray = new PaletteItem(0xc0c0c0,
            PaletteMessages.PaletteItem_LightGray);
    public static final PaletteItem Copper = new PaletteItem(0xcd853f,
            PaletteMessages.PaletteItem_Copper);
    public static final PaletteItem GoldenOlive = new PaletteItem(0xB27737,
            PaletteMessages.PaletteItem_GoldenOlive);
    public static final PaletteItem Beige = new PaletteItem(0xFbFad8,
            PaletteMessages.PaletteItem_Beige);
    public static final PaletteItem Sapphire = new PaletteItem(0x9EB0CE,
            PaletteMessages.PaletteItem_Sapphire);
    public static final PaletteItem Lavender = new PaletteItem(0xCE94BA,
            PaletteMessages.PaletteItem_Lavender);
    public static final PaletteItem White = new PaletteItem(0xffffff,
            PaletteMessages.PaletteItem_White);
    public static final PaletteItem Khaki = new PaletteItem(0x7B5B40,
            PaletteMessages.PaletteItem_Khaki);
    public static final PaletteItem ForestGreen = new PaletteItem(0x739E73,
            PaletteMessages.PaletteItem_ForestGreen);
    public static final PaletteItem AntiqueBlue = new PaletteItem(0x667A8C,
            PaletteMessages.PaletteItem_AntiqueBlue);
    public static final PaletteItem Indigo = new PaletteItem(0x6B00a2,
            PaletteMessages.PaletteItem_Indigo);
    public static final PaletteItem Violet = new PaletteItem(0x9d6b84,
            PaletteMessages.PaletteItem_Violet);

}