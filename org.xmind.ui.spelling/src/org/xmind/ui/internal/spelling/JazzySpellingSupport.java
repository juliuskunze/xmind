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
package org.xmind.ui.internal.spelling;

import org.eclipse.swt.widgets.Control;
import org.xmind.ui.texteditor.IControlContentAdapter2;
import org.xmind.ui.texteditor.ISpellingSupport;

public class JazzySpellingSupport implements ISpellingSupport {

    private static final String KEY_SPELLING_HELPER = "KEY_SPELLING_HELPER"; //$NON-NLS-1$

    public JazzySpellingSupport() {
    }

    public void install(Control textWidget, IControlContentAdapter2 adapter) {
        if (!SpellingPlugin.isSpellingCheckEnabled())
            return;

        if (textWidget.getData(KEY_SPELLING_HELPER) instanceof SpellingHelper)
            return;

        SpellingHelper helper = new SpellingHelper(textWidget, adapter);
        textWidget.setData(KEY_SPELLING_HELPER, helper);
    }

}