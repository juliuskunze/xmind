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

import org.xmind.ui.internal.WorkbookFactory;

public class DefaultTemplateDescriptor extends AbstractTemplateDescriptor {

    private String id;

    private String name;

    public DefaultTemplateDescriptor(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getSymbolicName() {
        return "default:" + getId(); //$NON-NLS-1$
    }

    public String getName() {
        return name;
    }

    public InputStream newStream() {
        return WorkbookFactory.createEmptyWorkbookStream();
    }

    @Override
    public String toString() {
        return String.format("Template{%s:%s}", id, name); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof DefaultTemplateDescriptor))
            return false;
        DefaultTemplateDescriptor that = (DefaultTemplateDescriptor) obj;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
