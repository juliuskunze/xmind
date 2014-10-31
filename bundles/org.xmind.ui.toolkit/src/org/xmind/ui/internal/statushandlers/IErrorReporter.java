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
package org.xmind.ui.internal.statushandlers;

import org.eclipse.swt.program.Program;

public interface IErrorReporter {

    public static class Default implements IErrorReporter {

        private static IErrorReporter delegate = null;

        private static Default instance = new Default();

        private Default() {
        }

        public boolean report(StatusDetails error) throws InterruptedException {
            if (delegate != null && delegate.report(error))
                return true;
            return Program.launch(error.buildMailingURL());
        }

        public static Default getInstance() {
            return instance;
        }

        public static void setDelegate(IErrorReporter errorReporter) {
            Default.delegate = errorReporter;
        }
    }

    /**
     * Reports the specified error to the product vendor. This method may block
     * the current thread.
     * 
     * @param error
     *            the error to report
     * @return <code>true</code> if reported successfully, or <code>false</code>
     *         otherwise
     */
    boolean report(StatusDetails error) throws InterruptedException;

}
