/**
 * 
 */
package net.xmind.signin.util;

import java.util.Map;

public interface IDataStore {

    boolean has(String key);

    String getString(String key);

    int getInt(String key);

    boolean getBoolean(String key);

    long getLong(String key);

    Map<Object, Object> toMap();

}