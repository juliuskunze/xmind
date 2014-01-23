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
package org.xmind.core.internal.command.remote.lan;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.xmind.core.command.remote.lan.ILANDomainPreferences;

/**
 * @author Frank Shaka
 */
public class LANDomainPreferences implements ILANDomainPreferences {

    private IEclipsePreferences defaultScope;

    private IEclipsePreferences currentScope;

    /**
     * 
     */
    public LANDomainPreferences(String pluginId) {
        this.defaultScope = DefaultScope.INSTANCE.getNode(pluginId);
        this.currentScope = ConfigurationScope.INSTANCE.getNode(pluginId);
    }

    public int getPortNumber() {
        return getInt(PREF_PORT, DEFAULT_PORT);
    }

    public int getBacklog() {
        return getInt(PREF_BACKLOG, DEFAULT_BACKLOG);
    }

    public boolean isShowingIncomingCommands() {
        return getBool(PREF_SHOW_INCOMING_COMMANDS,
                DEFAULT_SHOW_INCOMING_COMMANDS);
    }

    public int getInt(String key, int defaultValue) {
        return currentScope.getInt(key, defaultScope.getInt(key, defaultValue));
    }

    public boolean getBool(String key, boolean defaultValue) {
        return currentScope.getBoolean(key,
                defaultScope.getBoolean(key, defaultValue));
    }

    public String getString(String key, String defaultValue) {
        return currentScope.get(key, defaultScope.get(key, defaultValue));
    }

    public void setInt(String key, int value) {
        currentScope.putInt(key, value);
    }

    public void setBoolean(String key, boolean value) {
        currentScope.putBoolean(key, value);
    }

    public void setString(String key, String value) {
        currentScope.put(key, value);
    }

}
