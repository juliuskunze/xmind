/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

package org.xmind.ui.dialogs;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * @author Frank Shaka
 * 
 */
public class ErrorDetails {

    private Throwable error;

    private String message;

    private long time;

    private String fullText = null;

    /**
     * 
     */
    public ErrorDetails(Throwable error, String message, long time) {
        this.error = error;
        this.message = message;
        this.time = time;
    }

    /**
     * @return the fullText
     */
    public String getFullText() {
        if (fullText == null) {
            fullText = buildFullText();
        }
        return fullText;
    }

    public void copyToClipboard() {
        Clipboard clipboard = new Clipboard(Display.getCurrent());
        try {
            clipboard.setContents(new Object[] { getFullText() },
                    new Transfer[] { TextTransfer.getInstance() });
        } finally {
            clipboard.dispose();
        }
    }

    private String buildFullText() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        if (message != null) {
            ps.println(message);
        }

        if (error != null) {
            error.printStackTrace(ps);
        }

        ps.println();
        ps.println();
        ps.println("== Environment =="); //$NON-NLS-1$
        ps.printf("Time: %1$tF %1$tT", time); //$NON-NLS-1$
        ps.println();

        String application = getSysProp("eclipse.application"); //$NON-NLS-1$
        if (application == null || "".equals(application)) { //$NON-NLS-1$
            IProduct product = Platform.getProduct();
            if (product != null) {
                application = product.getApplication();
            }
        }
        if ("org.xmind.cathy.application".equals(application)) { //$NON-NLS-1$
            printp(ps, "XMind Distribution Pack: %s", //$NON-NLS-1$
                    "org.xmind.product.distribution.id"); //$NON-NLS-1$
        } else {
            ps.println("XMind Installed as Plugins"); //$NON-NLS-1$
        }
        Bundle bundle = Platform.getBundle("org.xmind.ui.toolkit"); //$NON-NLS-1$
        ps.printf("XMind Version: %s", bundle.getVersion().toString()); //$NON-NLS-1$
        ps.println();
        printp(ps, "Operating System: %s %s (%s)", //$NON-NLS-1$ 
                "os.name", "os.version", "os.arch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        printp(ps, "Language: %s", "user.language"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Country: %s", "user.country"); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println();

        ps.println("== Platform =="); //$NON-NLS-1$
        printp(ps, "Launcher: %s", "eclipse.launcher.name"); //$NON-NLS-1$ //$NON-NLS-2$
        ps.printf("Application: %s", application == null ? "" : application); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println();
        printp(ps, "Product: %s", "eclipse.product"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Command Line Arguments: %s", "eclipse.commands"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "VM Arguments: %s", "eclipse.vmargs"); //$NON-NLS-1$ //$NON-NLS-2$
        ps.println();

        ps.println("== Java Properties =="); //$NON-NLS-1$
        printp(ps, "Java Version: %s", "java.version"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Java Vendor: %s", "java.vendor"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Java Runtime: %s", "java.runtime.name"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Version: %s", "java.runtime.version"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "Java VM: %s", "java.vm.name"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Version: %s", "java.vm.version"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Vendor: %s", "java.vm.vendor"); //$NON-NLS-1$ //$NON-NLS-2$
        printp(ps, "    Info: %s", "java.vm.info"); //$NON-NLS-1$ //$NON-NLS-2$

        ps.close();

        return out.toString();
    }

    private static String getSysProp(String key) {
        String value = System.getProperty(key);
        return value == null ? "" : value; //$NON-NLS-1$
    }

    private static void printp(PrintStream ps, String format, String... keys) {
        Object[] values = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = getSysProp(key);
            if (value.indexOf("\n") >= 0 || value.indexOf("\r") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
                value = value.replaceAll("\r\n|\r|\n", //$NON-NLS-1$
                        System.getProperty("line.separator") + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            values[i] = value;
        }
        ps.printf(format, values);
        ps.println();
    }

}
