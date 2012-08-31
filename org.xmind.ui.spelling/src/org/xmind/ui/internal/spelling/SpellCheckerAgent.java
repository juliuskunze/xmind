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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jazzy.Activator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xmind.core.Core;
import org.xmind.core.util.FileUtils;

import com.swabunga.spell.engine.Configuration;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;

public class SpellCheckerAgent {

    private static final SpellCheckerAgent instance = new SpellCheckerAgent();

    private SpellChecker spellChecker;

    private Configuration configuration;

    private List<ISpellCheckerVisitor> visitors;

    public static void resetSpellChecker() {
        getInstance().spellChecker = null;
    }

    public static void visitSpellChecker(ISpellCheckerVisitor visitor) {
        getInstance().doVisitSpellChecker(visitor);
    }

    public static void setConfigurations(IPreferenceStore prefStore) {
        getInstance().doSetConfigurations(prefStore);
    }

    private synchronized void doVisitSpellChecker(ISpellCheckerVisitor visitor) {
        if (spellChecker != null) {
            visitor.handleWith(spellChecker);
            return;
        }

        if (visitors != null) {
            visitors.add(visitor);
            return;
        }

        visitors = new ArrayList<ISpellCheckerVisitor>();
        visitors.add(visitor);

        new Job(Messages.loadingSpellChecker) {
            protected IStatus run(IProgressMonitor monitor) {
                return loadSpellChecker(monitor);
            }
        }.schedule();
    }

    private IStatus loadSpellChecker(IProgressMonitor monitor) {
        monitor.beginTask(null, 5);

        monitor.subTask(Messages.creatingSpellCheckerInstance);
        SpellChecker spellChecker = Activator.createSpellChecker();
        if (!SpellingPlugin.getDefault().getPreferenceStore().getBoolean(
                SpellingPlugin.DEFAULT_SPELLING_CHECKER_DISABLED)) {
            Activator.addDefaultDictionaries(spellChecker);
        }
        monitor.worked(1);

        monitor.subTask(Messages.addingSystemDictionary);
        addSystemDictionary(spellChecker);
        monitor.worked(1);

        monitor.subTask(Messages.addingUserDictionary);
        addUserDictionary(spellChecker);
        monitor.worked(1);

        monitor.subTask(Messages.initializingSpellingSettings);
        setConfigurations(spellChecker);
        monitor.worked(1);

        this.spellChecker = spellChecker;
        monitor.subTask(Messages.notifyingSpellingVisitors);
        notifyVisitors();
        monitor.done();

        return new Status(IStatus.OK, SpellingPlugin.PLUGIN_ID,
                "Finish loading spell checker"); //$NON-NLS-1$
    }

    private void notifyVisitors() {
        if (visitors == null)
            return;

        for (ISpellCheckerVisitor visitor : visitors) {
            if (visitor != null) {
                visitor.handleWith(spellChecker);
            }
        }
        visitors = null;
    }

    private void setConfigurations(SpellChecker spellChecker) {
        configuration = spellChecker.getConfiguration();
        doSetConfigurations(SpellingPlugin.getDefault().getPreferenceStore());
    }

    private void addUserDictionary(SpellChecker spellChecker) {
        for (ISpellCheckerDescriptor descriptor : SpellCheckerRegistry
                .getInstance().getDescriptors()) {
            try {
                spellChecker.addDictionary(new SpellDictionaryHashMap(
                        new InputStreamReader(descriptor.openStream())));
            } catch (IOException e) {
                SpellingPlugin.log(e);
            }
        }

        File userDict = FileUtils.ensureFileParent(new File(Core.getWorkspace()
                .getAbsolutePath("spelling/user.dict"))); //$NON-NLS-1$
        if (!userDict.exists()) {
            try {
                new FileOutputStream(userDict).close();
            } catch (IOException ignore) {
            }
        }
        try {
            spellChecker
                    .setUserDictionary(new SpellDictionaryHashMap(userDict));
        } catch (IOException e) {
            SpellingPlugin.log(e);
        }
    }

    private void addSystemDictionary(SpellChecker spellChecker) {
        try {
            spellChecker.addDictionary(new SpellDictionaryHashMap(
                    new InputStreamReader(SpellCheckerAgent.class
                            .getResourceAsStream("xmind.dict")))); //$NON-NLS-1$
        } catch (IOException e) {
            SpellingPlugin.log(e);
        }
    }

    private void doSetConfigurations(IPreferenceStore ps) {
        if (configuration == null)
            return;

        configuration.setBoolean(Configuration.SPELL_IGNOREDIGITWORDS, ps
                .getBoolean(Configuration.SPELL_IGNOREDIGITWORDS));
        configuration.setBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES,
                ps.getBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES));
        configuration.setBoolean(Configuration.SPELL_IGNOREMIXEDCASE, ps
                .getBoolean(Configuration.SPELL_IGNOREMIXEDCASE));
        configuration
                .setBoolean(
                        Configuration.SPELL_IGNORESENTENCECAPITALIZATION,
                        ps
                                .getBoolean(Configuration.SPELL_IGNORESENTENCECAPITALIZATION));
        configuration.setBoolean(Configuration.SPELL_IGNOREUPPERCASE, ps
                .getBoolean(Configuration.SPELL_IGNOREUPPERCASE));
    }

    private static SpellCheckerAgent getInstance() {
        return instance;
    }

}