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
package org.xmind.cathy.internal.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BaseNewWizardMenu;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.actions.NewFromMoreTemplateAction;
import org.xmind.ui.internal.actions.NewFromTemplateFileAction;
import org.xmind.ui.internal.actions.NewFromTemplateURLAction;
import org.xmind.ui.internal.actions.NewWorkbookAction;
import org.xmind.ui.internal.wizards.NewFromTemplateWizard;
import org.xmind.ui.internal.wizards.NewWorkbookWizard;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ResourceFinder;

/**
 * 
 * @author frankshaka
 * @deprecated Use {@link NewFromTemplateWizard} and {@link NewWorkbookWizard}
 */
@SuppressWarnings("unchecked")
@Deprecated
public class NewMenu extends BaseNewWizardMenu {

    private static final String TEMPLATES_PATH = "templates"; //$NON-NLS-1$

    private static final String TEMPLATES_DIR = TEMPLATES_PATH + "/"; //$NON-NLS-1$

    private IAction newWorkbookAction;

    private IAction newFromTemplateAction;

    public NewMenu(IWorkbenchWindow window) {
        super(window, "org.xmind.ui.newMenu"); //$NON-NLS-1$
        this.newWorkbookAction = new NewWorkbookAction(window);
        this.newFromTemplateAction = new NewFromMoreTemplateAction(window);
    }

    public IAction getNewWorkbookAction() {
        return newWorkbookAction;
    }

    public IAction getNewFromTemplateAction() {
        return newFromTemplateAction;
    }

    protected void addItems(List list) {
        list.add(new ActionContributionItem(newWorkbookAction));
        list.add(new Separator());

        addTemplateFileActions(list);

        list.add(new Separator());
        list.add(new ActionContributionItem(newFromTemplateAction));
        list.add(new Separator());

        super.addItems(list);
    }

    private void addTemplateFileActions(List list) {
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        if (bundle == null)
            return;

        Element element = getTemplateListElement(bundle);
        if (element == null) {
            String mainPath = getTemplatePath(bundle);
            if (mainPath == null)
                return;

            File dir = new File(mainPath);
            if (!dir.isDirectory())
                return;

            for (String name : dir.list()) {
                if (name.endsWith(MindMapUI.FILE_EXT_TEMPLATE)) {
                    String path = new File(dir, name).getAbsolutePath();
                    list.add(new ActionContributionItem(
                            new NewFromTemplateFileAction(getWindow(), path)));
                }
            }
        } else {
            Properties properties = getTemplateListProperties(bundle);
            Iterator<Element> it = DOMUtils.childElementIterByTag(element,
                    "template"); //$NON-NLS-1$
            while (it.hasNext()) {
                Element templateEle = it.next();
                String resourcePath = templateEle.getAttribute("resource"); //$NON-NLS-1$
                if (!"".equals(resourcePath)) { //$NON-NLS-1$
                    if (!resourcePath.startsWith("/")) { //$NON-NLS-1$
                        resourcePath = TEMPLATES_DIR + resourcePath;
                    }
                    URL url = findTemplateResource(bundle, resourcePath);
                    if (url != null) {
                        String name = templateEle.getAttribute("name"); //$NON-NLS-1$
                        if (name.startsWith("%")) { //$NON-NLS-1$
                            if (properties != null) {
                                name = properties
                                        .getProperty(name.substring(1));
                            } else {
                                name = null;
                            }
                        }
                        if (name == null || "".equals(name)) { //$NON-NLS-1$
                            name = FileUtils
                                    .getNoExtensionFileName(resourcePath);
                        }
                        IAction action = new NewFromTemplateURLAction(
                                getWindow(), url, name);
                        list.add(new ActionContributionItem(action));
                    }
                }
            }
        }
    }

    private URL findTemplateResource(Bundle bundle, String resourcePath) {
        return FileLocator.find(bundle, new Path("$nl$/" + resourcePath), null); //$NON-NLS-1$
    }

    private Properties getTemplateListProperties(Bundle bundle) {
        URL propURL = ResourceFinder.findResource(bundle, TEMPLATES_DIR,
                "templates", ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
        if (propURL != null) {
            try {
                InputStream is = propURL.openStream();
                try {
                    Properties properties = new Properties();
                    properties.load(is);
                    return properties;
                } finally {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    private Element getTemplateListElement(Bundle bundle) {
        URL xmlURL = FileLocator.find(bundle, new Path(TEMPLATES_DIR
                + "templates.xml"), null); //$NON-NLS-1$
        if (xmlURL == null)
            return null;
        try {
            InputStream is = xmlURL.openStream();
            if (is != null) {
                try {
                    Document doc = DOMUtils.loadDocument(is);
                    if (doc != null)
                        return doc.getDocumentElement();
                } finally {
                    is.close();
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    private String getTemplatePath(Bundle bundle) {
        URL url = FileLocator.find(bundle, new Path("templates"), null); //$NON-NLS-1$
        if (url == null)
            return null;
        try {
            url = FileLocator.toFileURL(url);
        } catch (IOException e) {
            return null;
        }
        return url.getFile();
    }

}