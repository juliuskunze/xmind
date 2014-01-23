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
package net.xmind.share.dialog;

import net.xmind.share.Info;

import org.eclipse.jface.dialogs.DialogPage;


public abstract class UploaderPage extends DialogPage implements IUploaderPage {

    private IUploaderPageContainer container;

    protected IUploaderPageContainer getContainer() {
        return container;
    }

    public void setContainer(IUploaderPageContainer container) {
        this.container = container;
    }

    protected Info getInfo() {
        return container.getInfo();
    }

    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
        if (getContainer() != null) {
            getContainer().updateMessage();
        }
    }

    public void setMessage(String newMessage, int newType) {
        super.setMessage(newMessage, newType);
        if (getContainer() != null) {
            getContainer().updateMessage();
        }
    }

}