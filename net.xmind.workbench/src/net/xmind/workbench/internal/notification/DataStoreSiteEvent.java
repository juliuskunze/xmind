package net.xmind.workbench.internal.notification;

import net.xmind.signin.IDataStore;

public class DataStoreSiteEvent implements ISiteEvent {

    private IDataStore data;

    public DataStoreSiteEvent(IDataStore data) {
        this.data = data;
    }

    public String getActionText() {
        return data.getString(ATTR_ACTION_TEXT);
    }

    public String getEventUrl() {
        String value = data.getString(ATTR_EVENT_URL);
        if (value == null) {
            value = "http://www.xmind.net"; //$NON-NLS-1$
        }
        return value;
    }

    public String getId() {
        String value = data.getString(ATTR_ID);
        if (value == null) {
            value = "0"; //$NON-NLS-1$
        }
        return value;
    }

    public String getMoreUrl() {
        return data.getString(ATTR_MORE_URL);
    }

    public String getTitle() {
        String value = data.getString(ATTR_TITLE);
        if (value == null) {
            value = ""; //$NON-NLS-1$
        }
        return value;
    }

    public String getPrompt() {
        return data.getString(ATTR_PROMPT);
    }

    public boolean isOpenExternal() {
        return data.getBoolean(ATTR_OPEN_EXTERNAL);
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
