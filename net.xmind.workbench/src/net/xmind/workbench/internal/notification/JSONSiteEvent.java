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

    public String getText() {
        return json.optString(ATTR_TITLE, ""); //$NON-NLS-1$
    }

    public String getPrompt() {
        return json.optString(ATTR_PROMPT, null);
    }

    public boolean isOpenExternal() {
        return json.optBoolean(ATTR_OPEN_EXTERNAL, false);
    }

    public String getHTML() {
        return json.optString(ATTR_HTML, null);
    }

    public String getInternalUrl() {
        return json.optString(ATTR_INTERNAL_URL, null);
    }

    public String getCaption() {
        return json.optString(ATTR_CAPTION, null);
    }

    public int getDuration() {
        return json.optInt(ATTR_DURATION, -1);
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

}
