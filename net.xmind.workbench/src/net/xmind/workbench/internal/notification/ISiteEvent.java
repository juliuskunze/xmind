package net.xmind.workbench.internal.notification;

public interface ISiteEvent {

    String ATTR_ID = "id"; //$NON-NLS-1$

    String ATTR_PROMPT = "prompt"; //$NON-NLS-1$

    String ATTR_TITLE = "title"; //$NON-NLS-1$

    String ATTR_EVENT_URL = "url"; //$NON-NLS-1$

    String ATTR_MORE_URL = "more-url"; //$NON-NLS-1$

    String ATTR_ACTION_TEXT = "action-text"; //$NON-NLS-1$

    String ATTR_OPEN_EXTERNAL = "open-external"; //$NON-NLS-1$

    void setActionText(String actionText);

    String getActionText();

    void setOpenExternal(boolean value);

    boolean isOpenExternal();

    void setPrompt(String type);

    String getPrompt();

    void setMoreUrl(String url);

    String getMoreUrl();

    void setEventUrl(String url);

    String getEventUrl();

    void setTitle(String title);

    String getTitle();

    String getId();

}
