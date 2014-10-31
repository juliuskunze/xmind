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

import java.util.Comparator;

import org.xmind.core.ITopic;

public class TopicCompartor implements Comparator<ITopic> {

    public int compare(ITopic o1, ITopic o2) {
        ITopic a1 = o1;
        ITopic a2;
        ITopic parent = a1.getParent();
        do {
            a2 = findSibling(a1, parent, o2);
            if (a2 != null) {
                return compareSibling(a1, a2);
            }
            a1 = parent;
            parent = a1.getParent();
        } while (a1 != null);
        return 1;
    }

    private int compareSibling(ITopic t1, ITopic t2) {
        if (t1.equals(t2))
            return 0;
        return t1.getIndex() - t2.getIndex();
    }

    private ITopic findSibling(ITopic t, ITopic parent, ITopic child) {
        if (t == null || child == null)
            return null;

        ITopic p = child.getParent();
        if ((parent == null && p == null)
                || (parent != null && parent.equals(p))) {
            return child;
        }
        return findSibling(t, parent, p);
    }

}