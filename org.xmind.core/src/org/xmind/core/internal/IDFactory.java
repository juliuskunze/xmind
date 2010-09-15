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
package org.xmind.core.internal;

import java.math.BigInteger;
import java.util.Random;

import org.xmind.core.IIdFactory;

/**
 * @author briansun
 * 
 */
public class IDFactory implements IIdFactory {

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * @return
     */
    public String createId() {
        BigInteger bi = new BigInteger(128, random);
        String id = bi.toString(32);
        while (id.length() < 26) {
            id = "0" + id; //$NON-NLS-1$
        }
        return id;
    }
}