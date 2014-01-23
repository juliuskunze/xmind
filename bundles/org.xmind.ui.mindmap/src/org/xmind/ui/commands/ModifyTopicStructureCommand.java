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
package org.xmind.ui.commands;

import java.util.Collection;

import org.eclipse.ui.ISourceProvider;
import org.xmind.core.ITopic;
import org.xmind.gef.command.ModifyCommand;

public class ModifyTopicStructureCommand extends ModifyCommand {

    public ModifyTopicStructureCommand(ITopic source, String newStructureClass) {
        super(source, newStructureClass);
    }

    public ModifyTopicStructureCommand(Collection<ITopic> sources,
            String newStructureClass) {
        super(sources, newStructureClass);
    }

    public ModifyTopicStructureCommand(ISourceProvider sourceProvider,
            String newStructureClass) {
        super(sourceProvider, newStructureClass);
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic) {
            return ((ITopic) source).getStructureClass();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value == null || value instanceof String) {
                topic.setStructureClass((String) value);
            }
        }
    }

}