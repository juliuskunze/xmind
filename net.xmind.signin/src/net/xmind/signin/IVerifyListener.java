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

package net.xmind.signin;

import org.eclipse.core.runtime.IStatus;

/**
 * @author Frank Shaka
 * 
 */
public interface IVerifyListener {

    int VALID = 0;

    int VERIFYING = 1 << 0;

    int NOT_SIGNED_IN = 1 << 1;

    int NOT_SUBSCRIBED = 1 << 2;

    int EXPIRED = 1 << 3;

    int ERROR = 1 << 31;

    int INVALID = ERROR | NOT_SUBSCRIBED | EXPIRED;

    /**
     * Notifies this callback when verification is done.
     * 
     * @param validity
     *            The result of the verification; <code>code</code> is one of
     *            VALID, ERROR, NOT_SUBSCRIVED, EXPIRED; <code>exception</code>
     *            is the exception object thrown if some internal or web error
     *            occurred (<code>code</code> is ERROR)
     */
    void notifyValidity(IStatus validity);

}
