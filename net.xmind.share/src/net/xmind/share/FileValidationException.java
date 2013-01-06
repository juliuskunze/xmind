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

package net.xmind.share;

/**
 * @author Frank Shaka
 * 
 */
public class FileValidationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -8068548909942785543L;

    /**
     * 
     */
    public FileValidationException() {
        super();
    }

    /**
     * @param message
     */
    public FileValidationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public FileValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
