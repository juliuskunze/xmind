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
package org.xmind.ui.internal.spelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmind.core.Core;
import org.xmind.core.util.FileUtils;

/**
 * @author Frank Shaka
 * 
 */
public class SpellCheckerRegistry {

    private static class FileSpellCheckerDescriptor implements
            ISpellCheckerDescriptor {

        private File file;

        /**
         * Creates a new instance.
         */
        public FileSpellCheckerDescriptor(File file) {
            this.file = file;
        }

        /**
         * @return the file
         */
        public File getFile() {
            return file;
        }

        public String getName() {
            return file.getName();
        }

        public InputStream openStream() throws IOException {
            return new FileInputStream(file);
        }

    }

    private static SpellCheckerRegistry instance = null;

    private List<ISpellCheckerDescriptor> descriptors = null;

    /**
     * @return the descriptors
     */
    public List<ISpellCheckerDescriptor> getDescriptors() {
        lazyLoad();
        return descriptors;
    }

    private void lazyLoad() {
        if (descriptors != null)
            return;
        doLazyLoad();
        if (descriptors == null)
            descriptors = Collections.emptyList();
    }

    private void doLazyLoad() {
        File dir = getUserDictDir();
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
            return;

        File[] dictFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith("."); //$NON-NLS-1$
            }
        });
        for (File dictFile : dictFiles) {
            if (dictFile.isFile() && dictFile.canRead()) {
                if (descriptors == null)
                    descriptors = new ArrayList<ISpellCheckerDescriptor>();
                descriptors.add(new FileSpellCheckerDescriptor(dictFile));
            }
        }
    }

    private File getUserDictDir() {
        return new File(Core.getWorkspace().getAbsolutePath("spelling/user")); //$NON-NLS-1$
    }

    public ISpellCheckerDescriptor importDictFile(File sourceDictFile)
            throws IOException {
        String name = sourceDictFile.getName();
        File targetFile = FileUtils.ensureFileParent(createFile(
                getUserDictDir(), FileUtils.getNoExtensionFileName(name),
                FileUtils.getExtension(name)));
        FileUtils.copy(sourceDictFile, targetFile);
        if (descriptors == null || descriptors.isEmpty()) {
            descriptors = new ArrayList<ISpellCheckerDescriptor>();
        }
        FileSpellCheckerDescriptor descriptor = new FileSpellCheckerDescriptor(
                targetFile);
        descriptors.add(descriptor);
        return descriptor;
    }

    private File createFile(File dir, String prefix, String suffix) {
        int i = 1;
        File file = new File(dir, prefix + suffix);
        while (file.exists()) {
            i++;
            file = new File(dir, prefix + " (" + i + ")" + suffix); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return file;
    }

    public void removeDictionary(ISpellCheckerDescriptor descriptor) {
        FileSpellCheckerDescriptor fileDescriptor = (FileSpellCheckerDescriptor) descriptor;
        if (descriptors != null && !descriptors.isEmpty()) {
            descriptors.remove(descriptor);
        }
        File file = fileDescriptor.getFile();
        file.delete();
        SpellCheckerAgent.resetSpellChecker();
    }

    /**
     * @return the registry
     */
    public static SpellCheckerRegistry getInstance() {
        if (instance == null)
            instance = new SpellCheckerRegistry();
        return instance;
    }
}
