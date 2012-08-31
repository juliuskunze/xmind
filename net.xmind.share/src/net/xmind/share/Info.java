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
package net.xmind.share;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.xmind.core.IMeta;

public class Info {

    /**
     * <p>
     * Key="Thumbnail/X"
     * </p>
     * <p>
     * Value Type: {@link Integer}
     * </p>
     * <p>
     * (Also used as a key for setting and getting related metadata)
     * </p>
     */
    public static final String X = IMeta.THUMBNAIL + IMeta.SEP + "X"; //$NON-NLS-1$

    /**
     * <p>
     * Key="Thumbnail/Y"
     * </p>
     * <p>
     * Value Type: {@link Integer}
     * </p>
     * <p>
     * (Also used as a key for setting and getting related metadata)
     * </p>
     */
    public static final String Y = IMeta.THUMBNAIL + IMeta.SEP + "Y"; //$NON-NLS-1$

    /**
     * <p>
     * Key="Thumbnail/Scale"
     * </p>
     * <p>
     * Value Type: {@link Double}
     * </p>
     * <p>
     * (Also used as a key for setting and getting related metadata)
     * </p>
     */
    public static final String SCALE = IMeta.THUMBNAIL + IMeta.SEP + "Scale"; //$NON-NLS-1$

    /**
     * "Share" is a prefix of all keys related to <a
     * href="http://share.xmind.net">share.xmind.net</a>.
     */
    public static final String SHARE = "Share"; //$NON-NLS-1$

    /**
     * <p>
     * Key="Share/AllowDownload"
     * </p>
     * <p>
     * Values: [{@link #PublicView}, {@link #Public}, {@link #Private}]
     * <p>
     * (Also used as a key for setting and getting related metadata)
     * </p>
     * 
     * @see #PublicView
     * @see #Public
     * @see #Private
     * @deprecated use #DOWNLOADABLE and #ACCESSIBILITY
     */
    public static final String ALLOW_DOWNLOAD = SHARE + IMeta.SEP
            + "AllowDownload"; //$NON-NLS-1$

    /**
     * <p>
     * Value="0"
     * </p>
     * <p>
     * Public view, private download.
     * </p>
     * 
     * @see #ALLOW_DOWNLOAD
     * @see #Public
     * @see #Private
     * @deprecated
     */
    public static final String PublicView = "0"; //$NON-NLS-1$

    /**
     * <p>
     * Value="1"
     * </p>
     * <p>
     * Public view and download.
     * </p>
     * 
     * @see #ALLOW_DOWNLOAD
     * @see #PublicView
     * @see #Private
     * @deprecated
     */
    public static final String Public = "1"; //$NON-NLS-1$

    /**
     * <p>
     * Value="2"
     * </p>
     * <p>
     * Private view and download.
     * </p>
     * 
     * @see #ALLOW_DOWNLOAD
     * @see #PublicView
     * @see #Public
     * @deprecated
     */
    public static final String Private = "2"; //$NON-NLS-1$

    public static final String DOWNLOADABLE = SHARE + IMeta.SEP
            + "Downloadable"; //$NON-NLS-1$

    public static final String DOWNLOADABLE_NO = "0"; //$NON-NLS-1$
    public static final String DOWNLOADABLE_YES = "1"; //$NON-NLS-1$

    public static final String PRIVACY = SHARE + IMeta.SEP + "Privacy"; //$NON-NLS-1$

    public static final String PRIVACY_PUBLIC = "1"; //$NON-NLS-1$
    public static final String PRIVACY_PRIVATE = "2"; //$NON-NLS-1$
    public static final String PRIVACY_UNLISTED = "3"; //$NON-NLS-1$

    /**
     * <p>
     * Key="Share/LanguageChannel"
     * </p>
     * <p>
     * Values: ["en", "zh", "fr", "de", "es", "ja", "ww" (Worldwide)]
     * <p>
     * (Also used as a key for setting and getting related metadata)
     * </p>
     * 
     */
    public static final String LANGUAGE_CHANNEL = SHARE + IMeta.SEP
            + "LanguageChannel"; //$NON-NLS-1$

    public static final List<String> LANGUAGE_CODES = Arrays.asList(//
            "en", //$NON-NLS-1$
            "zh", //$NON-NLS-1$
            "fr", //$NON-NLS-1$
            "de", //$NON-NLS-1$
            "ja", //$NON-NLS-1$
            "es", //$NON-NLS-1$
            "ww" //$NON-NLS-1$
    );

    /**
     * <p>
     * Key="Description"
     * </p>
     * <p>
     * Value Type: {@link String}
     * </p>
     * <p>
     * (Also used as a key for setting and getting related metadata)
     * </p>
     */
    public static final String DESCRIPTION = IMeta.DESCRIPTION;

    /**
     * <p>
     * Key="Title"
     * </p>
     * <p>
     * Value Type: {@link String}
     * </p>
     * <p>
     * <b>WARNING: FOR INTERNAL USE ONLY! NOT PART OF THE METADATA.</b>
     * </p>
     */
    public static final String TITLE = "Title"; //$NON-NLS-1$

    /**
     * <p>
     * Key="FullImage"
     * </p>
     * <p>
     * Value Type: {@link org.eclipse.swt.graphics.Image}
     * </p>
     * <p>
     * <b>WARNING: FOR INTERNAL USE ONLY! NOT PART OF THE METADATA.</b>
     * </p>
     */
    public static final String FULL_IMAGE = "FullImage"; //$NON-NLS-1$

    /**
     * <p>
     * Key="UserName"
     * </p>
     * <p>
     * Value Type: {@link String}
     * </p>
     * <p>
     * <b>WARNING: FOR INTERNAL USE ONLY! NOT PART OF THE METADATA.</b>
     * </p>
     */
    public static final String USER_ID = "UserID"; //$NON-NLS-1$

    /**
     * <p>
     * Key="UserName"
     * </p>
     * <p>
     * Value Type: {@link String}
     * </p>
     * <p>
     * <b>WARNING: FOR INTERNAL USE ONLY! NOT PART OF THE METADATA.</b>
     * </p>
     */
    public static final String TOKEN = "Token"; //$NON-NLS-1$

    /**
     * <p>
     * Key="Workbook"
     * </p>
     * <p>
     * Value Type: {@link org.xmind.core.IWorkbook}
     * </p>
     * <p>
     * <b>WARNING: FOR INTERNAL USE ONLY! NOT PART OF THE METADATA.</b>
     * </p>
     */
    public static final String WORKBOOK = "Workbook"; //$NON-NLS-1$

    /**
     * <p>
     * Key="File"
     * </p>
     * <p>
     * Value Type: {@link java.io.File}
     * </p>
     * <p>
     * <b>WARNING: FOR INTERNAL USE ONLY! NOT PART OF THE METADATA.</b>
     * </p>
     */
    public static final String FILE = "File"; //$NON-NLS-1$

    private Map<String, Object> properties = new HashMap<String, Object>();

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public boolean isProperty(String key, Object value) {
        Object property = getProperty(key);
        return property == value
                || (property != null && property.equals(value));
    }

    public void setProperty(String key, Object value) {
        Object oldValue = getProperty(key);
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
        Object newValue = getProperty(key);
        support.firePropertyChange(key, oldValue, newValue);
    }

    public void setInt(String key, int value) {
        setProperty(key, Integer.valueOf(value));
    }

    public void setDouble(String key, double value) {
        setProperty(key, Double.valueOf(value));
    }

    public void setBoolean(String key, boolean value) {
        setProperty(key, Boolean.valueOf(value));
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getString(String key, String defaultString) {
        Object property = getProperty(key);
        return property instanceof String ? (String) property : defaultString;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public int getInt(String key, int defaultInt) {
        Object property = getProperty(key);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        }
        return defaultInt;
    }

    public double getDouble(String key, double defaultValue) {
        Object property = getProperty(key);
        if (property instanceof Double) {
            return ((Double) property).doubleValue();
        }
        return defaultValue;
    }

    public boolean getBoolean(String key) {
        Object property = getProperty(key);
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue();
        return false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String key,
            PropertyChangeListener listener) {
        support.addPropertyChangeListener(key, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String key,
            PropertyChangeListener listener) {
        support.removePropertyChangeListener(key, listener);
    }

    @Override
    public String toString() {
        return properties.toString();
    }

    public static String getDefaultLanguageCode() {
        String lang = Platform.getNL();
        int index = lang.indexOf("_"); //$NON-NLS-1$
        if (index > 0)
            lang = lang.substring(0, index);
        index = LANGUAGE_CODES.indexOf(lang);
        if (index < 0)
            lang = LANGUAGE_CODES.get(LANGUAGE_CODES.size() - 1);
        return lang;
    }

}