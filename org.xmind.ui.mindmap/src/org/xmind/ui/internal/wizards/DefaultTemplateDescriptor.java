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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.WorkbookFactory;
import org.xmind.ui.util.Logger;

public class DefaultTemplateDescriptor extends AbstractTemplateDescriptor {

    private String id;

    private String name;

    private byte[] data;

    public DefaultTemplateDescriptor(String id, String name) {
        this.id = id;
        this.name = name;
        try {
            this.data = readData(WorkbookFactory.createEmptyWorkbookStream());
        } catch (IOException e) {
            Logger.log(e, "Failed to load default template."); //$NON-NLS-1$
            this.data = new byte[0];
        }
    }

    public DefaultTemplateDescriptor(String id, String name, InputStream stream)
            throws IOException {
        this.id = id;
        this.name = name;
        this.data = readData(stream);
    }

    private byte[] readData(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileUtils.transfer(stream, output);
        return output.toByteArray();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public InputStream newStream() {
        return new ByteArrayInputStream(data);
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
