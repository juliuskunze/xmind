/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package org.xmind.ui.texteditor;

import org.eclipse.jface.text.ITextViewer;

public interface ISpellingSupport {

    ISpellingSupport NULL = new ISpellingSupport() {
        public void install(ITextViewer textViewer,
                IControlContentAdapter2 adapter) {
        }
//        public void install(Control textWidget, IControlContentAdapter2 adapter) {
//        }

    };

//    void install(Control textWidget, IControlContentAdapter2 adapter);
    void install(ITextViewer textViewer, IControlContentAdapter2 adapter);

}