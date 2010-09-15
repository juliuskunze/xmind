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
package org.xmind.ui.browser;

/**
 * 
 * @author Frank Shaka
 */
public interface IBrowserSupport {

    int AS_DEFAULT = 0;

    int AS_EXTERNAL = 1;

    int AS_VIEW = 1 << 1;

    int AS_EDITOR = 1 << 2;

    int AS_INTERNAL = AS_VIEW | AS_EDITOR;

    IBrowser createBrowser(int style, String browserClientId, String name,
            String tooltip);

    IBrowser createBrowser(int style, String browserClientId);

    IBrowser createBrowser(String browserClientId);

    IBrowser createBrowser(int style);

    IBrowser createBrowser();

}