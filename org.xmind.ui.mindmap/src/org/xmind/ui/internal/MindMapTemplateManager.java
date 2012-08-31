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

package org.xmind.ui.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.wizards.DefaultTemplateDescriptor;
import org.xmind.ui.internal.wizards.FileTemplateDescriptor;
import org.xmind.ui.internal.wizards.ThemeTemplateDescriptor;
import org.xmind.ui.internal.wizards.URLTemplateDescriptor;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ResourceFinder;

/**
 * @author Frank Shaka
 * 
 */
public class MindMapTemplateManager {

    private static final MindMapTemplateManager instance = new MindMapTemplateManager();

    private static final String DEFAULT_TEMPLATE_ID = "org.xmind.ui.template.default"; //$NON-NLS-1$

    private static final String TEMPLATES_PATH = "templates"; //$NON-NLS-1$
    private static final String TEMPLATES_DIR = TEMPLATES_PATH + "/"; //$NON-NLS-1$

    private MindMapTemplateManager() {
    }

    public List<ITemplateDescriptor> importTemplates(String... fileNames) {
        List<ITemplateDescriptor> importedTemplates = new ArrayList<ITemplateDescriptor>(
                fileNames.length);
        for (String fileName : fileNames) {
            ITemplateDescriptor template = importCustomTemplate(fileName);
            if (template != null) {
                importedTemplates.add(template);
            }
        }
        return importedTemplates;
    }

    public ITemplateDescriptor importCustomTemplate(String path) {
        String dirPath = getCustomTemplatesLocation();
        File dir = new File(dirPath);
        FileUtils.ensureDirectory(dir);
        File sourceFile = new File(path);
        String fileName = sourceFile.getName();
        File targetFile = createNonConflictingFile(dir, fileName);
        try {
            FileUtils.copy(sourceFile, targetFile);
            return new FileTemplateDescriptor(targetFile);
        } catch (IOException e) {
        }
        return null;
    }

    public List<ITemplateDescriptor> loadAllTemplates() {
        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
        loadDefaultTemplates(templates);
        loadSystemTemplates(templates);
        loadCustomTemplates(templates);
        return templates;
    }

    public List<ITemplateDescriptor> loadDefaultTemplates() {
        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
        loadDefaultTemplates(templates);
        return templates;
    }

    public List<ITemplateDescriptor> loadSystemTemplates() {
        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
        loadSystemTemplates(templates);
        return templates;
    }

    public List<ITemplateDescriptor> loadCustomTemplates() {
        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
        loadCustomTemplates(templates);
        return templates;
    }

    private void loadDefaultTemplates(List<ITemplateDescriptor> templates) {
        templates.add(new DefaultTemplateDescriptor(DEFAULT_TEMPLATE_ID,
                WizardMessages.ChooseTemplateWizardPage_BlankMap_title));
    }

    private void loadCustomTemplates(List<ITemplateDescriptor> templates) {
        String templatesDir = getCustomTemplatesLocation();
        loadTemplatesFromDir(templates, new File(templatesDir));
    }

    private String getCustomTemplatesLocation() {
        return Core.getWorkspace().getAbsolutePath(TEMPLATES_PATH);
    }

    private void loadTemplatesFromDir(List<ITemplateDescriptor> templates,
            File templatesDir) {
        SortedSet<ITemplateDescriptor> set = new TreeSet<ITemplateDescriptor>(
                new Comparator<ITemplateDescriptor>() {
                    public int compare(ITemplateDescriptor t1,
                            ITemplateDescriptor t2) {
                        File f1 = ((FileTemplateDescriptor) t1).getFile();
                        File f2 = ((FileTemplateDescriptor) t2).getFile();
                        return f1.compareTo(f2);
                    }
                });
        if (templatesDir != null && templatesDir.isDirectory()) {
            for (String fileName : templatesDir.list()) {
                if (fileName.endsWith(MindMapUI.FILE_EXT_TEMPLATE)
                        || fileName.endsWith(MindMapUI.FILE_EXT_XMIND)) {
                    File file = new File(templatesDir, fileName);
                    if (file.isFile() && file.canRead()) {
                        set.add(new FileTemplateDescriptor(file));
                    }
                }
            }
        }
        templates.addAll(set);
    }

    private void loadSystemTemplates(List<ITemplateDescriptor> templates) {
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        if (bundle != null) {
            Element element = getTemplateListElement(bundle);
            if (element != null) {
                java.util.Properties properties = getTemplateListProperties(bundle);
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
                                    name = properties.getProperty(name
                                            .substring(1));
                                } else {
                                    name = null;
                                }
                            }
                            if (name == null || "".equals(name)) { //$NON-NLS-1$
                                name = FileUtils
                                        .getNoExtensionFileName(resourcePath);
                            }
                            templates.add(new URLTemplateDescriptor(url, name));
                        }
                    }
                }
            }
        }
    }

    private URL findTemplateResource(Bundle bundle, String resourcePath) {
        return FileLocator.find(bundle, new Path("$nl$/" + resourcePath), null); //$NON-NLS-1$
    }

    private java.util.Properties getTemplateListProperties(Bundle bundle) {
        URL propURL = ResourceFinder.findResource(bundle, TEMPLATES_DIR,
                "templates", ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
        if (propURL != null) {
            try {
                InputStream is = propURL.openStream();
                try {
                    java.util.Properties properties = new java.util.Properties();
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

    protected File createNonConflictingFile(File rootDir, String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String name = dotIndex < 0 ? fileName : fileName.substring(0, dotIndex);
        String ext = dotIndex < 0 ? "" : fileName.substring(dotIndex); //$NON-NLS-1$
        File targetFile = new File(rootDir, fileName);
        int i = 1;
        while (targetFile.exists()) {
            i++;
            targetFile = new File(rootDir, String.format(
                    "%s %s%s", name, i, ext)); //$NON-NLS-1$
        }
        return targetFile;
    }

    public String getTemplateSymbolicName(ITemplateDescriptor template) {
        if (template instanceof URLTemplateDescriptor) {
            return ((URLTemplateDescriptor) template).getURL().toExternalForm();
        } else if (template instanceof FileTemplateDescriptor) {
            return "file:" + ((FileTemplateDescriptor) template).getFile().getAbsolutePath(); //$NON-NLS-1$
        } else if (template instanceof ThemeTemplateDescriptor) {
            IStyle theme = ((ThemeTemplateDescriptor) template).getTheme();
            if (theme == MindMapUI.getResourceManager().getBlankTheme()) {
                return "theme:blank"; //$NON-NLS-1$
            } else {
                return "theme:" + theme.getId(); //$NON-NLS-1$
            }
        } else if (template instanceof DefaultTemplateDescriptor) {
            return "default:" + ((DefaultTemplateDescriptor) template).getId(); //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    public static MindMapTemplateManager getInstance() {
        return instance;
    }

}
