/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.xmind.workbench.internal.notification;

public interface ISiteEvent {

    String ATTR_ID = "id"; //$NON-NLS-1$

    String ATTR_PROMPT = "prompt"; //$NON-NLS-1$

    String ATTR_CAPTION = "caption"; //$NON-NLS-1$

    String ATTR_TITLE = "title"; //$NON-NLS-1$

    String ATTR_EVENT_URL = "url"; //$NON-NLS-1$

    String ATTR_MORE_URL = "more-url"; //$NON-NLS-1$

    String ATTR_ACTION_TEXT = "action-text"; //$NON-NLS-1$

    String ATTR_OPEN_EXTERNAL = "open-external"; //$NON-NLS-1$

    String ATTR_HTML = "html"; //$NON-NLS-1$

    String ATTR_INTERNAL_URL = "internal-url"; //$NON-NLS-1$

    String ATTR_DURATION = "duration"; //$NON-NLS-1$

    String ATTR_STYLE = "style"; //$NON-NLS-1$

    String getId();

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

    void setText(String text);

    String getText();

    String getHTML();

    void setHTML(String html);

    String getInternalUrl();

    void setInternalUrl(String url);

    String getCaption();

    void setCaption(String caption);

    int getDuration();

    void setDuration(int duration);

    String getStyle();

    void setStyle(String style);

}
