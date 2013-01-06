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
package org.xmind.ui.internal.comm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.ui.comm.IXMindCommand;
import org.xmind.ui.comm.IXMindCommandHandler;
import org.xmind.ui.internal.browser.BrowserPlugin;

public class XMindCommandSupport extends RegistryReader {

    private static final String EXT_POINT_ID = "xmindCommandHandlers"; //$NON-NLS-1$

    private static class XMindCommandHandlerDescriptor {

        private String id;

        private Pattern pattern;

        private IConfigurationElement element;

        private IXMindCommandHandler handler;

        public XMindCommandHandlerDescriptor(String id,
                IConfigurationElement element) {
            String namePattern = element
                    .getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND);
            if (namePattern == null)
                namePattern = ""; //$NON-NLS-1$
            this.id = id;
            this.pattern = Pattern.compile(namePattern);
            this.element = element;
            this.handler = null;
        }

        public String getId() {
            return id;
        }

        public boolean canHandle(String commandName) {
            Matcher matcher = pattern.matcher(commandName);
            return matcher.matches();
        }

        public boolean handle(IXMindCommand command) {
            Matcher matcher = pattern.matcher(command.getCommandName());
            if (!matcher.matches())
                return false;

            if (!ensureHandler())
                return false;

            int total = matcher.groupCount();
            String[] groups = new String[total];
            for (int i = 0; i < total; i++) {
                groups[i] = matcher.group(i);
            }
            return handler.handleXMindCommand(command, groups);
        }

        private boolean ensureHandler() {
            if (handler != null)
                return true;
            try {
                Object obj = element
                        .createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
                if (obj instanceof IXMindCommandHandler) {
                    handler = (IXMindCommandHandler) obj;
                    return true;
                }
            } catch (CoreException e) {
                BrowserPlugin.log(e);
            }
            return true;
        }

        @Override
        public String toString() {
            return "XMindCommandHandler{" + getId() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    private static final XMindCommandSupport instance = new XMindCommandSupport();

    private List<XMindCommandHandlerDescriptor> descriptors = null;

    private Map<String, List<XMindCommandHandlerDescriptor>> cache = null;

    private XMindCommandSupport() {
    }

    private List<XMindCommandHandlerDescriptor> getHandlers() {
        ensureLoaded();
        return descriptors;
    }

    private void ensureLoaded() {
        if (descriptors != null)
            return;
        lazyLoad();
        if (descriptors == null)
            descriptors = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), BrowserPlugin.PLUGIN_ID,
                EXT_POINT_ID);
    }

    @Override
    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if ("handler".equals(name)) { //$NON-NLS-1$
            readContribution(element);
            readElementChildren(element);
            return true;
        }
        return false;
    }

    private void readContribution(IConfigurationElement element) {
        String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        if (id == null) {
            BrowserPlugin
                    .getDefault()
                    .getLog()
                    .log(new Status(IStatus.ERROR, element
                            .getNamespaceIdentifier(),
                            "Invalid extension: (id missing)")); //$NON-NLS-1$
            return;
        }
        if (getClassValue(element, IWorkbenchRegistryConstants.ATT_CLASS) == null) {
            BrowserPlugin
                    .getDefault()
                    .getLog()
                    .log(new Status(IStatus.ERROR, element
                            .getNamespaceIdentifier(),
                            "Invalid extension: (class missing)")); //$NON-NLS-1$
            return;
        }

        registerContribution(new XMindCommandHandlerDescriptor(id, element));
    }

    private void registerContribution(XMindCommandHandlerDescriptor contribution) {
        if (descriptors == null)
            descriptors = new ArrayList<XMindCommandHandlerDescriptor>();
        descriptors.add(contribution);
    }

    public boolean handleCommand(String uri) {
        XMindCommand command = XMindCommand.parseURI(uri);
        if (command == null)
            return false;
        return handleCommand(command);
    }

    public synchronized boolean handleCommand(IXMindCommand command) {
        if (cache == null) {
            cache = new HashMap<String, List<XMindCommandHandlerDescriptor>>();
        }
        List<XMindCommandHandlerDescriptor> list = cache.get(command
                .getCommandName());
        if (list == null) {
            list = collectHandlers(command.getCommandName());
            cache.put(command.getCommandName(), list);
        }
        boolean handled = false;
        for (Object desc : list.toArray()) {
            try {
                if (((XMindCommandHandlerDescriptor) desc).handle(command)) {
                    handled = true;
                    break;
                }
            } catch (Throwable e) {
                BrowserPlugin.log(e);
            }
        }
        return handled;
    }

    private List<XMindCommandHandlerDescriptor> collectHandlers(
            String commandName) {
        List<XMindCommandHandlerDescriptor> list = new ArrayList<XMindCommandHandlerDescriptor>();
        for (Object desc : getHandlers().toArray()) {
            if (((XMindCommandHandlerDescriptor) desc).canHandle(commandName)) {
                list.add((XMindCommandHandlerDescriptor) desc);
            }
        }
        return list;
    }

    public static XMindCommandSupport getInstance() {
        return instance;
    }

}
