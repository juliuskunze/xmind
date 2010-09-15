/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
package org.xmind.gef.part;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Brian Sun
 */
public class PartRegistry {

    private Map<Object, IPart> modelToPart = new HashMap<Object, IPart>();;

    public void register(Object model, IPart p) {
        if (p == null || model == null)
            return;
        modelToPart.put(model, p);
    }

    public void unregister(Object model, IPart p) {
        if (p == null || model == null)
            return;
        IPart part = modelToPart.get(model);
        if (p == part)
            modelToPart.remove(model);
    }

    public IPart getPartByModel(Object model) {
        if (model == null)
            return null;
        return modelToPart.get(model);
    }

    public void clear() {
        modelToPart.clear();
    }

}