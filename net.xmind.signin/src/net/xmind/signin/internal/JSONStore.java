/**
 * 
 */
package net.xmind.signin.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.xmind.signin.IDataStore;

import org.json.JSONObject;

public class JSONStore implements IDataStore {

    private JSONObject json;

    public JSONStore(JSONObject json) {
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }

    public boolean has(String key) {
        return json.has(key);
    }

    public long getLong(String key) {
        return json.optLong(key);
    }

    public boolean getBoolean(String key) {
        return json.optBoolean(key);
    }

    public int getInt(String key) {
        return json.optInt(key);
    }

    public String getString(String key) {
        return json.optString(key);
    }

    @SuppressWarnings("unchecked")
    public Map<Object, Object> toMap() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            Object key = keys.next();
            map.put(key, json.opt((String) key));
        }
        return map;
    }

}