package org.xmind.core.internal.sharing;

import org.xmind.core.sharing.ISharedContact;

/**
 * @author Jason Wong
 */
public class SharedContact implements ISharedContact {

    private final String id;

    private String name;

    public SharedContact(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getID() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
