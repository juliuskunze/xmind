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
package org.xmind.core.internal.factory;

import org.eclipse.core.runtime.IAdapterFactory;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;

@SuppressWarnings("unchecked")
public class CoreAdapterFactory implements IAdapterFactory {

    private static Class[] LIST = { IWorkbook.class };

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IWorkbook.class) {
            if (adaptableObject instanceof IWorkbookComponent)
                return ((IWorkbookComponent) adaptableObject)
                        .getOwnedWorkbook();
        }
        return null;
    }

    public Class[] getAdapterList() {
        return LIST;
    }

}