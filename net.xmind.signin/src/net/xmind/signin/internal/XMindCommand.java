package net.xmind.signin.internal;

import java.util.Properties;

import net.xmind.signin.util.IDataStore;
import net.xmind.signin.util.JSONStore;

import org.json.JSONException;
import org.json.JSONObject;

public class XMindCommand {

    public static final String STATUS = "xmind_status"; //$NON-NLS-1$

    public static final String JSON = "xmind_json"; //$NON-NLS-1$

    public static final String COMMAND = "xmind_cmd"; //$NON-NLS-1$

    private String text;

    private Properties properties = new Properties();

    private IDataStore json = null;

    public XMindCommand(String text) {
        this.text = text;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String getCode() {
        return get(STATUS);
    }

    public String getJSONString() {
        return get(JSON);
    }

    public IDataStore getJSON() {
        if (json == null)
            json = createJSON();
        return json;
    }

    private IDataStore createJSON() {
        String json = getJSONString();
        if (json != null) {
            try {
                return new JSONStore(new JSONObject(json));
            } catch (JSONException e) {
            }
        }
        return null;
    }

    public String getCommand() {
        return get(COMMAND);
    }

    public boolean parse() {
        if (!text.startsWith("xmind_")) //$NON-NLS-1$
            return false;

        String[] parts = text.split(";"); //$NON-NLS-1$
        for (String part : parts) {
            String[] kv = part.split("="); //$NON-NLS-1$
            if (kv.length == 2) {
                properties.setProperty(kv[0], kv[1]);
            }
        }
        return true;
    }

}
