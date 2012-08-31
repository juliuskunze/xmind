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

import java.beans.PropertyChangeSupport;

import org.eclipse.jface.resource.ImageDescriptor;
import org.xmind.ui.internal.ITemplateDescriptor;

public abstract class AbstractTemplateDescriptor implements ITemplateDescriptor {

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private ImageDescriptor image = null;

    public ImageDescriptor getImage() {
        return image;
    }

    public void setImage(ImageDescriptor image) {
        ImageDescriptor oldImage = this.image;
        this.image = image;
        pcs.firePropertyChange(PROP_IMAGE, oldImage, image);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

}