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
package org.xmind.core.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;

public class TopicPropertyTester extends PropertyTester {

    private static final String P_TYPE = "type"; //$NON-NLS-1$

    private static final String P_STRUCTURE_CLASS = "structureClass"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (receiver instanceof ITopic) {
            ITopic topic = (ITopic) receiver;
            if (P_TYPE.equals(property)) {
                return expectedValue != null
                        && expectedValue.equals(getType(topic));
            } else if (P_STRUCTURE_CLASS.equals(property)) {
                String structureClass = topic.getStructureClass();
                return expectedValue == null ? structureClass == null
                        : expectedValue.equals(structureClass);
            }
        }
        Assert.isTrue(false);
        return false;
    }

    private String getType(ITopic topic) {
        return topic.getType();
    }

}
