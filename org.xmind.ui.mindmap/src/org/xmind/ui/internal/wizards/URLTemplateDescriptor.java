/* *************************************import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
d. and others.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class URLTemplateDescriptor extends AbstractTemplateDescriptor {

    private URL url;

    private String name;

    public URLTemplateDescriptor(URL url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public URL getURL() {
        return url;
    }

    public InputStream newStream() {
        try {
            return url.openStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("Template{%s}", url); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof URLTemplateDescriptor))
            return false;
        URLTemplateDescriptor that = (URLTemplateDescriptor) obj;
        return this.url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

}