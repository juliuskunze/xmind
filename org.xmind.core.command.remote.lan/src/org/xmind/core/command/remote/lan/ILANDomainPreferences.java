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
/**
 * 
 */
package org.xmind.core.command.remote.lan;

/**
 * @author Frank Shaka
 */
public interface ILANDomainPreferences {

    /**
     * The id of the command service domain provided by this plugin.
     */
    String DOMAIN_ID = "org.xmind.core.command.remote.domain.lan"; //$NON-NLS-1$

    /**
     * The default port number of the local command socket server (value is
     * <code>13231</code>).
     * 
     * @see {@link java.net.ServerSocket#bind(java.net.SocketAddress, int)}
     */
    int DEFAULT_PORT = 13231;

    /**
     * The default backlog value of the local command socket server (value is
     * <code>50</code>).
     * 
     * @see {@link java.net.ServerSocket#bind(java.net.SocketAddress, int)}
     * @see {@link java.net.SocketImpl#listen(int)}
     */
    int DEFAULT_BACKLOG = 50;

    /**
     * The default value for showing incoming commands (value is
     * <code>false</code>).
     */
    boolean DEFAULT_SHOW_INCOMING_COMMANDS = false;

    /**
     * Preference key for port number of the local command socket server (value
     * is <code>"port"</code>).
     */
    String PREF_PORT = "port"; //$NON-NLS-1$

    /**
     * Preference key for backlog value of the local command socket server
     * (value is <code>"backlog"</code>).
     */
    String PREF_BACKLOG = "backlog"; //$NON-NLS-1$

    /**
     * Preference key for showing incoming commands and their handling process
     * in UI (value is <code>"showIncomingCommands"</code>).
     */
    String PREF_SHOW_INCOMING_COMMANDS = "showIncomingCommands"; //$NON-NLS-1$

    /**
     * Gets an integer value from this preference store.
     * 
     * @param key
     *            the key of the preference
     * @param defaultValue
     *            the default value of the preference
     * @return the value of the preference
     */
    int getInt(String key, int defaultValue);

    /**
     * Gets a boolean value from this preference store.
     * 
     * @param key
     *            the key of the preference
     * @param defaultValue
     *            the default value of the preference
     * @return the value of the preference
     */
    boolean getBool(String key, boolean defaultValue);

    /**
     * Gets a String value from this preference store.
     * 
     * @param key
     *            the key of the preference
     * @param defaultValue
     *            the default value of the preference
     * @return the value of the preference
     */
    String getString(String key, String defaultValue);

    /**
     * Sets the value of an integer preference.
     * 
     * @param key
     *            the key of preference to set
     * @param value
     *            the value to set
     */
    void setInt(String key, int value);

    /**
     * Sets the value of a boolean preference.
     * 
     * @param key
     *            the key of preference to set
     * @param value
     *            the value to set
     */
    void setBoolean(String key, boolean value);

    /**
     * Sets the value of a String preference.
     * 
     * @param key
     *            the key of preference to set
     * @param value
     *            the value to set
     */
    void setString(String key, String value);

    /**
     * Returns the default port number that the local command server socket is
     * intended to bind to.
     * 
     * @return the default port number of the local command server
     * @see #DEFAULT_PORT
     * @see #PREF_PORT
     */
    int getPortNumber();

    /**
     * Returns the backlog value that controls the size of the waiting
     * connection queue.
     * 
     * @return the default backlog value
     * @see #DEFAULT_BACKLOG
     * @see #PREF_BACKLOG
     */
    int getBacklog();

    /**
     * Determines whether the command handling process will be shown in the UI.
     * 
     * @return <code>true</code> if the command handling process will be shown
     *         in the UI, or <code>false</code> otherwise
     * @see #DEFAULT_SHOW_INCOMING_COMMANDS
     * @see #PREF_SHOW_INCOMING_COMMANDS
     */
    boolean isShowingIncomingCommands();

}
