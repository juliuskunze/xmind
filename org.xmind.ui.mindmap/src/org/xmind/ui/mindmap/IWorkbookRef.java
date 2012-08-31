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
package org.xmind.ui.mindmap;

import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.ICommandStack;

public interface IWorkbookRef {

    /**
     * 
     * @return
     */
    IWorkbook getWorkbook();

    /**
     * 
     * @return
     */
    ICommandStack getCommandStack();

//    /**
//     * 
//     * @param selection
//     * @param reveal
//     * @param forceFocus
//     *            TODO
//     */
//    void setSelection(ISelection selection, boolean reveal, boolean forceFocus);

//    /**
//     * 
//     * @param marker
//     */
//    void addDirtyMarker(IDirtyMarker marker);
//
//    /**
//     * 
//     * @param marker
//     */
//    void removeDirtyMarker(IDirtyMarker marker);

    /**
     * @return
     * @deprecated
     */
    List<IEditorPart> getOpenedEditors();

    /**
     * @return
     * @deprecated
     */
    boolean isForceDirty();

    /**
     * @deprecated
     */
    void forceDirty();
}
