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

    public static final PaletteItem Black = new PaletteItem(0, PaletteMessages.PaletteItem_Black);
    public static final PaletteItem White = new PaletteItem(0xffffff, PaletteMessages.PaletteItem_White);
    public static final PaletteItem Black80 = new PaletteItem(0xcccccc,
            PaletteMessages.PaletteItem_Black_Lighter80);
    public static final PaletteItem Black60 = new PaletteItem(0x999999,
            PaletteMessages.PaletteItem_Black_Lighter60);
    public static final PaletteItem Black40 = new PaletteItem(0x666666,
            PaletteMessages.PaletteItem_Black_Lighter40);
    public static final PaletteItem Black20 = new PaletteItem(0x535353,
            PaletteMessages.PaletteItem_Black_Lighter20);

    public static final PaletteItem Red = new PaletteItem(0xff0d00, PaletteMessages.PaletteItem_Red);
    public static final PaletteItem Red80 = new PaletteItem(0xfdcfcd,
            PaletteMessages.PaletteItem_Red_Lighter80);
    public static final PaletteItem Red60 = new PaletteItem(0xff9e99,
            PaletteMessages.PaletteItem_Red_Lighter60);
    public static final PaletteItem Red40 = new PaletteItem(0xff6e66,
            PaletteMessages.PaletteItem_Red_Lighter40);
    public static final PaletteItem Red25 = new PaletteItem(0xbf1e1b,
            PaletteMessages.PaletteItem_Red_Darker25);
    public static final PaletteItem Red50 = new PaletteItem(0x800a04,
            PaletteMessages.PaletteItem_Red_Darker50);

    public static final PaletteItem Orange = new PaletteItem(0xff8a00, PaletteMessages.PaletteItem_Orange);
    public static final PaletteItem Orange80 = new PaletteItem(0xffe8cc,
            PaletteMessages.PaletteItem_Orange_Lighter80);
    public static final PaletteItem Orange60 = new PaletteItem(0xffd099,
            PaletteMessages.PaletteItem_Orange_Lighter60);
    public static final PaletteItem Orange40 = new PaletteItem(0xffb966,
            PaletteMessages.PaletteItem_Orange_Lighter40);
    public static final PaletteItem Orange25 = new PaletteItem(0xbf6a15,
            PaletteMessages.PaletteItem_Orange_Darker25);
    public static final PaletteItem Orange50 = new PaletteItem(0x804701,
            PaletteMessages.PaletteItem_Orange_Darker50);

    public static final PaletteItem Yellow = new PaletteItem(0xffd800, PaletteMessages.PaletteItem_Yellow);
    public static final PaletteItem Yellow80 = new PaletteItem(0xfff7cc,
            PaletteMessages.PaletteItem_Yellow_Lighter80);
    public static final PaletteItem Yellow60 = new PaletteItem(0xffef99,
            PaletteMessages.PaletteItem_Yellow_Lighter60);
    public static final PaletteItem Yellow40 = new PaletteItem(0xffe866,
            PaletteMessages.PaletteItem_Yellow_Lighter40);
    public static final PaletteItem Yellow25 = new PaletteItem(0xbfa306,
            PaletteMessages.PaletteItem_Yellow_Darker25);
    public static final PaletteItem Yellow50 = new PaletteItem(0x806e00,
            PaletteMessages.PaletteItem_Yellow_Darker50);

    public static final PaletteItem Green = new PaletteItem(0xa4e100, PaletteMessages.PaletteItem_Green);
    public static final PaletteItem Green80 = new PaletteItem(0xedf9cc,
            PaletteMessages.PaletteItem_Green_Lighter80);
    public static final PaletteItem Green60 = new PaletteItem(0xdbf399,
            PaletteMessages.PaletteItem_Green_Lighter60);
    public static final PaletteItem Green40 = new PaletteItem(0xc8ed66,
            PaletteMessages.PaletteItem_Green_Lighter40);
    public static final PaletteItem Green25 = new PaletteItem(0x97bf32,
            PaletteMessages.PaletteItem_Green_Darker25);
    public static final PaletteItem Green50 = new PaletteItem(0x60801d,
            PaletteMessages.PaletteItem_Green_Darker50);

    public static final PaletteItem Blue = new PaletteItem(0x00aeff, PaletteMessages.PaletteItem_Blue);
    public static final PaletteItem Blue80 = new PaletteItem(0xccefff,
            PaletteMessages.PaletteItem_Blue_Lighter80);
    public static final PaletteItem Blue60 = new PaletteItem(0x99dfff,
            PaletteMessages.PaletteItem_Blue_Lighter60);
    public static final PaletteItem Blue40 = new PaletteItem(0x66ceff,
            PaletteMessages.PaletteItem_Blue_Lighter40);
    public static final PaletteItem Blue25 = new PaletteItem(0x0083bf,
            PaletteMessages.PaletteItem_Blue_Darker25);
    public static final PaletteItem Blue50 = new PaletteItem(0x005780,
            PaletteMessages.PaletteItem_Blue_Darker50);

    public static final PaletteItem Indigo = new PaletteItem(0x245dff, PaletteMessages.PaletteItem_Indigo);
    public static final PaletteItem Indigo80 = new PaletteItem(0xd3dfff,
            PaletteMessages.PaletteItem_Indigo_Lighter80);
    public static final PaletteItem Indigo60 = new PaletteItem(0xa7beff,
            PaletteMessages.PaletteItem_Indigo_Lighter60);
    public static final PaletteItem Indigo40 = new PaletteItem(0x7c9eff,
            PaletteMessages.PaletteItem_Indigo_Lighter40);
    public static final PaletteItem Indigo25 = new PaletteItem(0x1b47bf,
            PaletteMessages.PaletteItem_Indigo_Darker25);
    public static final PaletteItem Indigo50 = new PaletteItem(0x012180,
            PaletteMessages.PaletteItem_Indigo_Darker50);

    public static final PaletteItem Purple = new PaletteItem(0x8a2bff, PaletteMessages.PaletteItem_Purple);
    public static final PaletteItem Purple80 = new PaletteItem(0xe8d5ff,
            PaletteMessages.PaletteItem_Purple_Lighter80);
    public static final PaletteItem Purple60 = new PaletteItem(0xd0aaff,
            PaletteMessages.PaletteItem_Purple_Lighter60);
    public static final PaletteItem Purple40 = new PaletteItem(0xb980ff,
            PaletteMessages.PaletteItem_Purple_Lighter40);
    public static final PaletteItem Purple25 = new PaletteItem(0x6821bf,
            PaletteMessages.PaletteItem_Purple_Darker25);
    public static final PaletteItem Purple50 = new PaletteItem(0x451680,
            PaletteMessages.PaletteItem_Purple_Darker50);

}