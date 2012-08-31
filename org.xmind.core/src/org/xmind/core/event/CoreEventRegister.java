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
package org.xmind.core.event;

public class CoreEventRegister extends CoreEventRegisterBase {

    private ICoreEventSource source;

    private ICoreEventSupport support;

    private ICoreEventListener listener;

    public CoreEventRegister() {
    }

    public CoreEventRegister(ICoreEventSource source) {
        this.source = source;
    }

    public CoreEventRegister(ICoreEventSupport support) {
        this.support = support;
    }

    public CoreEventRegister(ICoreEventListener listener) {
        this.listener = listener;
    }

    public CoreEventRegister(ICoreEventSource source,
            ICoreEventListener listener) {
        this.source = source;
        this.listener = listener;
    }

    public CoreEventRegister(ICoreEventSupport support,
            ICoreEventListener listener) {
        this.support = support;
        this.listener = listener;
    }

    public ICoreEventRegistration register(String eventType) {
        if (listener == null)
            return null;

        ICoreEventRegistration reg;
        if (source != null) {
            reg = source.registerCoreEventListener(eventType, listener);
            addRegistration(reg);
        } else if (support != null) {
            reg = support.registerGlobalListener(eventType, listener);
            addRegistration(reg);
        } else {
            reg = null;
        }
        return reg;
    }

    public void setNextListener(ICoreEventListener listener) {
        this.listener = listener;
    }

    public void setNextSource(ICoreEventSource source) {
        this.source = source;
        this.support = null;
    }

    public void setNextSupport(ICoreEventSupport support) {
        this.source = null;
        this.support = support;
    }

}