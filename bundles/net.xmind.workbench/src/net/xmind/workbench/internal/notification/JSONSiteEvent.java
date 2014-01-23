package net.xmind.workbench.internal.notification;

import org.json.JSONObject;

public class JSONSiteEvent implements ISiteEvent {

    private JSONObject json;

    public JSONSiteEvent(JSONObject json) {
        this.json = json;
    }

    public String getActionText() {
        return getString(json, ATTR_ACTION_TEXT, null);
    }

    public String getEventUrl() {
        return getString(json, ATTR_EVENT_URL, "http://www.xmind.net"); //$NON-NLS-1$
    }

    public String getId() {
        return getString(json, ATTR_ID, "0"); //$NON-NLS-1$
    }

    public String getMoreUrl() {
        return getString(json, ATTR_MORE_URL, null);
    }

    public String getText() {
        return getString(json, ATTR_TITLE, ""); //$NON-NLS-1$
    }

    public String getPrompt() {
        return getString(json, ATTR_PROMPT, null);
    }

    public boolean isOpenExternal() {
        return json.optBoolean(ATTR_OPEN_EXTERNAL, false);
    }

    public String getHTML() {
        return getString(json, ATTR_HTML, null);
    }

    public String getInternalUrl() {
        return getString(json, ATTR_INTERNAL_URL, null);
    }

    public String getCaption() {
        return getString(json, ATTR_CAPTION, null);
    }

    public int getDuration() {
        return json.optInt(ATTR_DURATION, -1);
    }

    public String getStyle() {
        return getString(json, ATTR_STYLE, null);
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

    public void setText(String title) {
        //read only, do nothing
    }

    public void setPrompt(String type) {
        //read only, do nothing
    }

    public void setHTML(String html) {
        //read only, do nothing
    }

    public void setInternalUrl(String url) {
        //read only, do nothing
    }

    public void setCaption(String caption) {
        //read only, do nothing
    }

    public void setDuration(int duration) {
        //read only, do nothing
    }

    public void setStyle(String style) {
        //read only, do nothing
    }

    private static String getString(JSONObject json, String key,
            String defaultValue) {
        Object o = json.opt(key);
        return o == null || JSONObject.NULL.equals(o) ? defaultValue : o
                .toString();
    }

}
