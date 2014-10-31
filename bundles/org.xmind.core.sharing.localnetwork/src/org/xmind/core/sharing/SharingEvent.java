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
package org.xmind.core.sharing;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharingEvent {

    public static enum Type {
        SERVICE_STATUS_CHANGED, //
        LIBRARY_ADDED, LIBRARY_REMOVED, LIBRARY_NAME_CHANGED, //
        SHARED_MAP_ADDED, SHARED_MAP_REMOVED, SHARED_MAP_UPDATED, //
        CONTACT_ADDED;
    }

    private Type eventType;

    private int oldStatus;

    private int newStatus;

    private ISharedLibrary library;

    private String contactID;

    private ISharedMap map;

    private ISharedMap[] maps;

    public SharingEvent(Type eventType) {
        this(eventType, null, null);
    }

    public SharingEvent(Type eventType, ISharedLibrary library) {
        this(eventType, library, null);
    }

    public SharingEvent(Type eventType, ISharedLibrary library, ISharedMap map) {
        this.eventType = eventType;
        this.library = library;
        this.map = map;
    }

    public SharingEvent(Type eventType, ISharedLibrary library,
            String contactID, ISharedMap[] maps) {
        this(eventType, library, null);
        this.contactID = contactID;
        this.maps = maps;
    }

    public SharingEvent(int oldStatus, int newStatus) {
        this.eventType = Type.SERVICE_STATUS_CHANGED;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public boolean isLocal() {
        return library == null || library.isLocal();
    }

    public Type getType() {
        return eventType;
    }

    public ISharedLibrary getLibrary() {
        return library;
    }

    public String getContactID() {
        return contactID;
    }

    public ISharedMap getMap() {
        return map;
    }

    public ISharedMap[] getMaps() {
        return maps;
    }

    public int getOldStatus() {
        return oldStatus;
    }

    public int getNewStatus() {
        return newStatus;
    }

}
