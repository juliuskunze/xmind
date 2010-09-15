package net.xmind.workbench.internal.notification;

import org.json.JSONObject;

public class JSONSiteEvent implements ISiteEvent {

    private JSONObject json;

    public JSONSiteEvent(JSONObject json) {
        this.json = json;
    }

    public String getActionText() {
        return json.optString(ATTR_ACTION_TEXT, null);
    }

    public String getEventUrl() {
        return json.optString(ATTR_EVENT_URL, "http://www.xmind.net"); //$NON-NLS-1$
    }

    public String getId() {
        return json.optString(ATTR_ID, "0"); //$NON-NLS-1$
    }

    public String getMoreUrl() {
        return json.optString(ATTR_MORE_URL, null);
    }

    public String getTitle() {
        return json.optString(ATTR_TITLE, ""); //$NON-NLS-1$
    }

    public String getPrompt() {
        return json.optString(ATTR_PROMPT, null);
    }

    public boolean isOpenExternal() {
        return json.optBoolean(ATTR_OPEN_EXTERNAL, false);
    }

    public void setActionText(String actionText) {
        //read only, do nothing
    }

    public void setEventUrl(String url) {
        //read only, do nothing
    }

    public void setMoreUrl(String url) {
        //read only, do nothing
    }

    public void setOpenExternal(boolean value) {
        //read only, do nothing
    }

    public void setTitle(String title) {
        //read only, do nothing
    }

    public void setPrompt(String type) {
        //read only, do nothing
    }

}
