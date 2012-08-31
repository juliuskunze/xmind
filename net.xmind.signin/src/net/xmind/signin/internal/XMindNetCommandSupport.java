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
package net.xmind.signin.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xmind.signin.IXMindNetCommandHandler;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.ui.browser.IPropertyChangingListener;
import org.xmind.ui.browser.PropertyChangingEvent;
import org.xmind.ui.internal.browser.BrowserViewer;

public class XMindNetCommandSupport implements PropertyChangeListener,
        IPropertyChangingListener {

    private Map<String, List<IXMindNetCommandHandler>> handlers = new HashMap<String, List<IXMindNetCommandHandler>>(
            1);

    public void propertyChange(PropertyChangeEvent evt) {
        //do nothing
    }

    public void propertyChanging(PropertyChangingEvent event) {
        if (event.getSource() instanceof BrowserViewer) {
            String url = ((BrowserViewer) event.getSource()).getURL();
            if (isXMindUrl(url)) {
                XMindNetCommand command = new XMindNetCommand((String) event
                        .getNewValue());
                if (!command.parse())
                    return;

                event.doit = false;
                fireXMindNetCommand(command);
            }
        }
    }

    private boolean isXMindUrl(String url) {
        if (url != null) {
            try {
                String host = new URI(url).getHost();
                return host != null && host.endsWith(".xmind.net"); //$NON-NLS-1$
            } catch (Exception e) {

            }
        }
        return false;
    }

    public void addXMindNetCommandHandler(String commandName,
            IXMindNetCommandHandler handler) {
        List<IXMindNetCommandHandler> handlerList = handlers.get(commandName);
        if (handlerList == null) {
            handlerList = new ArrayList<IXMindNetCommandHandler>();
            handlers.put(commandName, handlerList);
        }
        handlerList.add(handler);
    }

    public void removeXMindNetCommandHandler(String commandName,
            IXMindNetCommandHandler handler) {
        List<IXMindNetCommandHandler> handlerList = handlers.get(commandName);
        if (handlerList != null && !handlerList.isEmpty()) {
            handlerList.remove(handler);
            if (handlerList.isEmpty()) {
                handlers.remove(commandName);
            }
        }
    }

    private boolean fireXMindNetCommand(final XMindNetCommand command) {
        String cmd = command.getCommandName();
        if (cmd != null) {
            List<IXMindNetCommandHandler> handlerList = handlers.get(cmd);
            if (handlerList != null && !handlerList.isEmpty()) {
                return fireXMindCommand(command, handlerList.toArray());
            }
        }
        String code = command.getCode();
        if (code != null) {
            List<IXMindNetCommandHandler> handlerList = handlers.get(code);
            if (handlerList != null && !handlerList.isEmpty()) {
                return fireXMindCommand(command, handlerList.toArray());
            }
        }
        return false;
    }

    private boolean fireXMindCommand(final XMindNetCommand command,
            Object[] handlers) {
        final boolean[] handled = new boolean[1];
        handled[0] = false;
        for (final Object handler : handlers) {
            if (handled[0])
                return true;
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    handled[0] = ((IXMindNetCommandHandler) handler)
                            .handleXMindNetCommand(command);
                }
            });
            if (handled[0])
                return true;
        }
        return handled[0];
    }
}
