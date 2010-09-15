package net.xmind.signin.internal;

import java.util.Properties;

import net.xmind.signin.IDataStore;
import net.xmind.signin.IXMindNetCommand;

import org.json.JSONException;
import org.json.JSONObject;

public class XMindNetCommand implements IXMindNetCommand {

    public static final String STATUS = "xmind_status"; //$NON-NLS-1$

    public static final String JSON = "xmind_json"; //$NON-NLS-1$

    public static final String COMMAND = "xmind_cmd"; //$NON-NLS-1$

    private String text;

    private Properties properties = new Properties();

    private IDataStore json = null;

    public XMindNetCommand(String text) {
        this.text = text;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String getCode() {
        return get(STATUS);
    }

    public String getContentString() {
        return get(JSON);
    }

    public IDataStore getContent() {
        if (json == null)
            json = createJSON();
        return json;
    }

    private IDataStore createJSON() {
        String json = getContentString();
        if (json != null) {
            try {
                return new JSONStore(new JSONObject(json));
            } catch (JSONException e) {
            }
        }
        return null;
    }

    public String getCommandName() {
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
