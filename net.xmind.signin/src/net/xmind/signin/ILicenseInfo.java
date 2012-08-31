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
package net.xmind.signin;

/**
 * 
 * @author Frank Shaka
 */
public interface ILicenseInfo {

    int VALID_PRO_SUBSCRIPTION = 1;

    int VALID_PRO_LICENSE = 1 << 1;

    int VALID_PLUS_LICENSE = 1 << 2;

    int NO_LICENSING_INFO = 1 << 10;

    int EXPIRED_SUBSCRIPTION = 1 << 11;

    int NOT_SUBSCRIBED = 1 << 12;

    int INVALID_LICENSE_KEY = 1 << 13;

    int ERROR = 1 << 30;

    int VERIFYING = 1 << 31;

    int VALID = VALID_PRO_SUBSCRIPTION | VALID_PRO_LICENSE | VALID_PLUS_LICENSE;

    /**
     * 
     * @return
     */
    int getType();

    /**
     * 
     * @return
     */
    Throwable getError();

    /**
     * Returns the representative name of the entity who the current XMind
     * Pro/Plus is licensed to.
     * 
     * @return
     */
    String getLicensedTo();

}
