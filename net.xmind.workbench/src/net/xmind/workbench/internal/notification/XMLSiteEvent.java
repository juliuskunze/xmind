/**
 * 
 */
package net.xmind.workbench.internal.notification;

import org.eclipse.ui.IMemento;

public class XMLSiteEvent implements ISiteEvent {

    private IMemento element;

    public XMLSiteEvent(IMemento element) {
        super();
        this.element = element;
    }

    private String attr(String name) {
        return element.getString(name);
    }

    public String getId() {
        return attr(ATTR_ID);
    }

    public void setId(String id) {
        element.putString(ATTR_ID, id);
    }

    public String getTitle() {
        return element.getTextData();
    }

    public void setTitle(String title) {
        element.putTextData(title);
    }

    public String getEventUrl() {
        return attr(ATTR_EVENT_URL);
    }

    public void setEventUrl(String url) {
        element.putString(ATTR_EVENT_URL, url);
    }

    public String getMoreUrl() {
        return attr(ATTR_MORE_URL);
    }

    public void setMoreUrl(String url) {
        element.putString(ATTR_MORE_URL, url);
    }

    public String getPrompt() {
        return attr(ATTR_PROMPT);
    }

    public void setPrompt(String type) {
        element.putString(ATTR_PROMPT, type);
    }

    public boolean isOpenExternal() {
        return Boolean.TRUE.equals(element.getBoolean(ATTR_OPEN_EXTERNAL));
    }

    public void setOpenExternal(boolean value) {
        element.putBoolean(ATTR_OPEN_EXTERNAL, value);
    }

    public String getActionText() {
        return attr(ATTR_ACTION_TEXT);
    }

    public void setActionText(String actionText) {
        element.putString(ATTR_ACTION_TEXT, actionText);
    }

    public int hashCode() {
        return element.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof XMLSiteEvent))
            return false;
        XMLSiteEvent that = (XMLSiteEvent) obj;
        return this.element.equals(that.element);
    }

    public String toString() {
        return "SiteEvent{id=" + getId() //$NON-NLS-1$
                + ",type=" + getPrompt() //$NON-NLS-1$
                + ",title=" + getTitle() //$NON-NLS-1$
                + ",url=" + getEventUrl() //$NON-NLS-1$
                + "}"; //$NON-NLS-1$
    }
}