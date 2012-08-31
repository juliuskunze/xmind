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
package org.xmind.ui.internal.wizards;

import java.io.InputStream;

import org.xmind.core.style.IStyle;
import org.xmind.ui.internal.WorkbookFactory;
import org.xmind.ui.mindmap.MindMapUI;

public class ThemeTemplateDescriptor extends AbstractTemplateDescriptor {

    private IStyle theme;

    public ThemeTemplateDescriptor(IStyle theme) {
        this.theme = theme;
    }

    public IStyle getTheme() {
        return theme;
    }

    public String getName() {
        return theme.getName();
    }

    public InputStream newStream() {
        return WorkbookFactory.createEmptyWorkbookStream(null, theme);
    }

    @Override
    public String toString() {
        if (theme == MindMapUI.getResourceManager().getBlankTheme()) {
            return "Template{theme:blank}"; //$NON-NLS-1$
        }
        return String.format("Template{theme:%s}", theme.getId()); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ThemeTemplateDescriptor))
            return false;
        ThemeTemplateDescriptor that = (ThemeTemplateDescriptor) obj;
        return this.theme.equals(that.theme);
    }

    @Override
    public int hashCode() {
        return theme.hashCode();
    }

}
