/**
 * 
 */
package net.xmind.signin;

import java.util.Collections;
import java.util.Map;

public interface IDataStore {

    public static final IDataStore EMPTY = new IDataStore() {

        private Map<Object, Object> map = Collections.emptyMap();

        public Map<Object, Object> toMap() {
            return map;
        }

        public boolean has(String key) {
            return false;
        }

        public String getString(String key) {
            return null;
        }

        public long getLong(String key) {
            return 0;
        }

        public int getInt(String key) {
            return 0;
        }

        public boolean getBoolean(String key) {
            return false;
        }
    };

    boolean has(String key);

    String getString(String key);

    int getInt(String key);

    boolean getBoolean(String key);

    long getLong(String key);

    Map<Object, Object> toMap();

}