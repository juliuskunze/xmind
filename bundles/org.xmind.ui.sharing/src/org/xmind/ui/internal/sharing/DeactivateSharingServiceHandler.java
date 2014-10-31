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
package org.xmind.ui.internal.sharing;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.xmind.core.sharing.ISharingService;

public class DeactivateSharingServiceHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISharingService sharingService = LocalNetworkSharingUI.getDefault()
                .getSharingService();
        if (sharingService != null)
            ToggleSharingServiceStatusJob.startToggle(sharingService, false,
                    null, false);
        return null;
    }

}
