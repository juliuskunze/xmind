/**
 * 
 */
package net.xmind.signin.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.xmind.signin.IDataStore;

public class PropertyStore implements IDataStore {

    private Properties properties;

    public PropertyStore(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public long getLong(String key) {
        return Long.parseLong(properties.getProperty(key));
    }

    public boolean has(String key) {
        return properties.containsKey(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public String getString(String key) {
        return properties.getProperty(key);
    };

    public Map<Object, Object> toMap() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object key : properties.keySet()) {
            map.put(key, properties.get(key));
        }
        return map;
    }

}