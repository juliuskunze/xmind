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
package org.xmind.core;

public class CoreException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5654915714687349526L;

    private int type;

    private String localizedMessage;

    private String codeInfo;

    public CoreException(int type) {
        super();
        this.type = type;
    }

    public CoreException(int type, String codeInfo) {
        super();
        this.type = type;
        this.codeInfo = codeInfo;
    }

    public CoreException(int type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    public CoreException(int type, String codeInfo, Throwable cause) {
        super(cause);
        this.type = type;
        this.codeInfo = codeInfo;
    }

    public int getType() {
        return type;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

    public boolean isMessageLocalized() {
        return localizedMessage != null;
    }

    void setCodeInfo(String info) {
        this.codeInfo = info;
    }

    public String getCodeInfo() {
        return codeInfo;
    }

    public String getLocalizedMessage() {
        if (isMessageLocalized())
            return localizedMessage;
        return super.getLocalizedMessage();
    }

    public String getMessage() {
        if (isMessageLocalized())
            return localizedMessage;
        return super.getMessage();
    }

}