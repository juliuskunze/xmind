/* ******************************************************************************
 * Copyright (c) 2006-2009 XMind Ltd. and others.
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
package org.xmind.cathy.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.xmind.core.util.FileUtils;

public class Log {

    public static final String SINGLETON = ".singleton"; //$NON-NLS-1$

    public static final String OPENING = ".opening"; //$NON-NLS-1$

    public static final String K_PRIMARY_WINDOW = "PRIMARY_WINDOW"; //$NON-NLS-1$

    private static String lineSeparator = null;

    private File file;

    private Properties properties = null;

    public Log(File file) {
        this.file = file;
    }

    public Properties getProperties() {
        if (properties == null) {
            loadProperties();
        }
        return properties;
    }

    private void loadProperties() {
        properties = new Properties();

        if (file.isFile() && file.canRead()) {
            try {
                FileInputStream is = new FileInputStream(file);
                try {
                    properties.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                CathyPlugin
                        .log(
                                e,
                                "Failed to load properties from log file: " + file.getAbsolutePath()); //$NON-NLS-1$
            }
        }
    }

    public void saveProperties() {
        if (properties == null)
            return;

        FileUtils.ensureFileParent(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            try {
                properties.store(out, null);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            CathyPlugin
                    .log(
                            e,
                            "Failed to save properties to log file: " + file.getAbsolutePath()); //$NON-NLS-1$
        }
    }

    public String[] getContents() {
        if (!file.isFile() || !file.canRead())
            return new String[0];
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("macosx".equals(Platform.getOS())) //$NON-NLS-1$
                        line = new String(line.getBytes(), "utf-16"); //$NON-NLS-1$
                    line = line.trim();
                    if (!"".equals(line)) //$NON-NLS-1$
                        lines.add(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            CathyPlugin.log(e,
                    "Failed to read log file: " + file.getAbsolutePath()); //$NON-NLS-1$
        }
        return lines.toArray(new String[lines.size()]);
    }

    public void setContents(String... contents) {
        write(true, contents);
    }

    public void append(String... lines) {
        if (lines.length == 0)
            return;
        write(true, lines);
    }

    private void write(boolean append, String... contents) {
        FileUtils.ensureFileParent(file);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file,
                    append));
            try {
                for (String line : contents) {
                    writer.write(line);
                    writer.newLine();
                }
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            CathyPlugin.log(e,
                    "Failed to write log file: " + file.getAbsolutePath()); //$NON-NLS-1$
        }
    }

    public void delete() {
        file.delete();
        properties = null;
    }

    public boolean exists() {
        return file.exists();
    }

    public static Log get(String name) {
        URL url = Platform.getInstanceLocation().getURL();
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
            //ignore this exception
        }
        File file = new File(url.getFile(), name);
        Log log = new Log(file);
        return log;
    }

    public static String getLineSeparator() {
        if (lineSeparator == null)
            lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
        return lineSeparator;
    }
}