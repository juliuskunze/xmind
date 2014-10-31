package org.xmind.core;

public interface IEncryptionHandler {

    /**
     * @return The password
     */
    String retrievePassword() throws CoreException;

}
