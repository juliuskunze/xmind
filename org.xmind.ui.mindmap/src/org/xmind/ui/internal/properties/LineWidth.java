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
package org.xmind.ui.internal.properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public enum LineWidth {

    Thinnest("1pt", PropertyMessages.LineWidth_Thinnest, IMindMapImages.LINE_THINNEST), // //$NON-NLS-1$
    Thin("2pt", PropertyMessages.LineWidth_Thin, IMindMapImages.LINE_THIN), // //$NON-NLS-1$
    Medium("3pt", PropertyMessages.LineWidth_Medium, IMindMapImages.LINE_MEDIUM), // //$NON-NLS-1$
    Fat("4pt", PropertyMessages.LineWdith_Fat, IMindMapImages.LINE_FAT), // //$NON-NLS-1$
    Fattest("5pt", PropertyMessages.LineWidth_Fattest, IMindMapImages.LINE_FATTEST); //$NON-NLS-1$

    private String value;

    private String name;

    private String iconName;

    private LineWidth(String value, String name, String iconName) {
        this.value = value;
        this.name = name;
        this.iconName = iconName;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public ImageDescriptor getIcon() {
        return MindMapUI.getImages().get(iconName);
    }

    public static LineWidth findByValue(String value) {
        if (value == null)
            return null;
        for (LineWidth lineWidth : values()) {
            if (lineWidth.getValue().startsWith(value))
                return lineWidth;
        }
        return null;
    }
}