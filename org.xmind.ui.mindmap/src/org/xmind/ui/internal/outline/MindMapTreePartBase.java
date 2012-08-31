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
package org.xmind.ui.internal.outline;

import org.eclipse.swt.graphics.Image;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.tree.TreePart;
import org.xmind.ui.util.MindMapUtils;

public class MindMapTreePartBase extends TreePart implements ICoreEventListener {

    private ICoreEventRegister eventRegister = null;

    public MindMapTreePartBase(Object model) {
        setModel(model);
    }

    protected void installModelListeners() {
        super.installModelListeners();
        Object m = getModel();
        if (m instanceof ICoreEventSource) {
            eventRegister = new CoreEventRegister((ICoreEventSource) m, this);
            registerCoreEvents(eventRegister);
        }
    }

    protected void registerCoreEvents(ICoreEventRegister register) {
    }

    public void handleCoreEvent(CoreEvent event) {
    }

    protected void uninstallModelListeners() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
        }
    }

    protected String getText() {
        return MindMapUtils.trimSingleLine(MindMapUtils.getText(getModel()));
    }

    protected Image getImage() {
        return MindMapUtils.getImage(getModel());
    }

}