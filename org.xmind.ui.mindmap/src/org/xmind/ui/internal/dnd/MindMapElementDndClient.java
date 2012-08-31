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
package org.xmind.ui.internal.dnd;

import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.xmind.gef.IViewer;
import org.xmind.gef.dnd.IDndClient;

public class MindMapElementDndClient implements IDndClient {

    private MindMapElementTransfer transfer = MindMapElementTransfer
            .getInstance();

    public Object toTransferData(Object[] viewerElements, IViewer viewer) {
        return viewerElements;
    }

    public Object[] toViewerElements(Object transferData, IViewer viewer,
            Object target) {
        if (transferData != null && transferData instanceof Object[])
            return (Object[]) transferData;
        return null;
    }

    public Object getData(Transfer transfer, TransferData data) {
        if (transfer == this.transfer)
            return this.transfer.nativeToJava(data);
        return null;
    }

    public Transfer getTransfer() {
        return transfer;
    }

}