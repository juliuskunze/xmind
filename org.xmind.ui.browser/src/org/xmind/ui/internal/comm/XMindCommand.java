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
package org.xmind.ui.internal.comm;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

import org.xmind.ui.comm.IXMindCommand;
import org.xmind.ui.internal.browser.BrowserPlugin;

public class XMindCommand implements IXMindCommand {

    public static final String XMIND_SCHEME = "xmind"; //$NON-NLS-1$

    public static final String XMIND_PROTOCOL = XMIND_SCHEME + ":"; //$NON-NLS-1$

    private String source;

    private String commandName;

    private Properties arguments;

    private String target;

    private XMindCommand(String source, String commandName,
            Properties arguments, String target) {
        this.source = source;
        this.commandName = commandName;
        this.arguments = arguments;
        this.target = target;
    }

    public String getCommandName() {
        return commandName;
    }

    public Properties getArguments() {
        return arguments;
    }

    public String getArgument(String key) {
        return arguments.getProperty(key);
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public static XMindCommand parseURI(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            BrowserPlugin.log("Malformed URI: " + uriString); //$NON-NLS-1$
            return null;
        }

        if (!XMIND_SCHEME.equals(uri.getScheme())) {
            BrowserPlugin.log("Not a XMind command URI: " + uriString); //$NON-NLS-1$
            return null;
        }

        return new XMindCommand(uri.getHost(),
                removeHeadingTrailingSlashes(uri.getPath()),
                parseArguments(uri.getRawQuery()), uri.getFragment());
    }

    private static String removeHeadingTrailingSlashes(String path) {
        if (path.startsWith("/")) //$NON-NLS-1$
            path = path.substring(1);
        if (path.endsWith("/")) //$NON-NLS-1$
            path = path.substring(0, path.length() - 1);
        return path;
    }

    private static Properties parseArguments(String query) {
        if (query == null || "".equals(query)) //$NON-NLS-1$
            return new Properties();
        Properties args = new Properties();
        for (String kv : query.split("&")) { //$NON-NLS-1$
            int sep = kv.indexOf('=');
            if (sep < 0) {
                args.setProperty(urlDecode(kv), ""); //$NON-NLS-1$
            } else {
                args.setProperty(urlDecode(kv.substring(0, sep)),
                        urlDecode(kv.substring(sep + 1)));
            }
        }
        return args;
    }

    private static String urlDecode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            BrowserPlugin.log(
                    "Failed to decode XMind command argument: " + text, e); //$NON-NLS-1$
            return text;
        }
    }

    public static void main(String[] args) {
        try {
//            URI uri = new URI(
//                    "xmind://www.xmind.net/openFile?file=%2FUsers%2Ffrankshaka%2FWorkspace%2Fxmind-build&url=http%3A%2F%2Fs3.amazonaws.com%2Fxmind-share%2Fwefwfw.xmind#xmind-share%2Fwefwfw.xmind"); //$NON-NLS-1$
            String filePath = URLEncoder
                    .encode("C:\\\\Users\\\\frankshaka\\\\Workspace\\\\test\\\\aa.xmind", //$NON-NLS-1$
                            "UTF-8"); //$NON-NLS-1$
            URI uri = new URI("xmind://localhost/openFile?file=" + filePath); //$NON-NLS-1$
            System.out.println(uri.getPath());
            System.out.println(uri.getRawPath());
            System.out.println(uri.getQuery());
            System.out.println(uri.getRawQuery());
            System.out.println(uri.getFragment());
            System.out.println(uri.getRawFragment());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
