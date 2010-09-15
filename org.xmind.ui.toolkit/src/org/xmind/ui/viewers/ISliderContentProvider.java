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
package org.xmind.ui.viewers;

import org.eclipse.jface.viewers.IContentProvider;

public interface ISliderContentProvider extends IContentProvider {

    /**
     * Gets values from the given input. At least the minimum and maximum values
     * should be returned. This is useful when the values are discrete.
     * 
     * @param input
     * @return
     */
    Object[] getValues(Object input);

    /**
     * Returns the value of the given input on the specified point.
     * 
     * @param input
     * @param portion
     *            The portion of the value from the minimum to the desired value
     *            comparing to the whole min-max value.
     * @return
     */
    Object getValue(Object input, double portion);

    /**
     * Returns the portion of the given value comparing to the min-max value.
     * 
     * @param input
     * @param value
     * @return
     */
    double getPortion(Object input, Object value);

}