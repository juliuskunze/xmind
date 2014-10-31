package org.xmind.core.sharing;

public interface IContactManager {

    int NO_ACCESS = 1;

    int ACCESS_WRITE = 1 << 1;

    int ACCESS_READ = 1 << 2;

    int ACCESS_WRITE_READ = ACCESS_READ | ACCESS_WRITE;

    ISharedContact getMyselfContact();

    boolean isContact(String contactID);

    int getAccessLevel(String contactID);

    ISharedContact addContact(String contactID, String name, int accessLevel);

    ISharedContact[] getContacts();

    ISharedContact findContactByID(String contactID);

    void updateContactName(String contactID, String name);

    String getVerificationCode();

}
