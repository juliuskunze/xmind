/**
 * 
 */
package net.xmind.signin.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
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
        try {
            return json.getLong(key);
        } catch (JSONException e) {
            throw new NumberFormatException();
        }
    }

    public boolean getBoolean(String key) {
        try {
            return json.getBoolean(key);
        } catch (JSONException e) {
        }
        return false;
    }

    public int getInt(String key) {
        try {
            return json.getInt(key);
        } catch (JSONException e) {
            throw new NumberFormatException();
        }
    }

    public String getString(String key) {
        try {
            return json.getString(key);
        } catch (JSONException e) {
        }
        return null;
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