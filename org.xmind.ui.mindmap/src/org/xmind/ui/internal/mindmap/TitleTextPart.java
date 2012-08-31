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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.ui.mindmap.ITitleTextPart;

public class TitleTextPart extends MindMapPartBase implements ITitleTextPart {

    protected TitleTextPart() {
    }

    protected IFigure createFigure() {
        return new RotatableWrapLabel();
    }

    public ITextFigure getTextFigure() {
        return (ITextFigure) super.getFigure();
    }

    protected void registerCoreEvents(ICoreEventSource source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.TitleText);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TitleText.equals(type)) {
            update();
        } else {
            super.handleCoreEvent(event);
        }
    }
}