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
package org.xmind.ui.internal.branch;

import java.util.HashSet;
import java.util.Set;

import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IBranchStyleSelector;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.MindMapStyleSelectorBase;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class DefaultBranchStyleSelector extends MindMapStyleSelectorBase
        implements IBranchStyleSelector {

    private static DefaultBranchStyleSelector instance = null;

    private Set<String> inheritedStyleKeys = null;

    protected DefaultBranchStyleSelector() {
        registerInheritedStyleKey(Styles.LineColor);
        registerInheritedStyleKey(Styles.LineWidth);
        registerInheritedStyleKey(Styles.LinePattern);
    }

    protected void registerInheritedStyleKey(String key) {
        if (key == null)
            return;
        if (inheritedStyleKeys == null)
            inheritedStyleKeys = new HashSet<String>();
        inheritedStyleKeys.add(key);
    }

    protected String getThemeStyleValue(IGraphicalPart part, String familyName,
            String key) {
        if (Styles.LineColor.equals(key)
                && Styles.FAMILY_MAIN_TOPIC.equals(familyName)
                && part instanceof IBranchPart) {
            String value = StyleUtils
                    .getIndexedBranchLineColor((IBranchPart) part);
            if (isValidValue(part, key, value))
                return value;
        }
        if (inheritedStyleKeys != null && inheritedStyleKeys.contains(key)) {
            if (part instanceof IBranchPart) {
                String value = ParentValueProvider.getValueProvider(
                        (IBranchPart) part).getParentValue(key);
                if (value != null)
                    return value;
            }
        }
        return super.getThemeStyleValue(part, familyName, key);
    }

    public void flushStyleCaches(IBranchPart branch) {
        ParentValueProvider.flush(branch);
    }

    public String getFamilyName(IGraphicalPart part) {
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            String branchType = branch.getBranchType();
            if (MindMapUI.BRANCH_CENTRAL.equals(branchType))
                return Styles.FAMILY_CENTRAL_TOPIC;
            if (MindMapUI.BRANCH_MAIN.equals(branchType))
                return Styles.FAMILY_MAIN_TOPIC;
            if (MindMapUI.BRANCH_FLOATING.equals(branchType))
                return Styles.FAMILY_FLOATING_TOPIC;
            if (MindMapUI.BRANCH_SUMMARY.equals(branchType))
                return Styles.FAMILY_SUMMARY_TOPIC;
        }
        return Styles.FAMILY_SUB_TOPIC;
    }

    public static DefaultBranchStyleSelector getDefault() {
        if (instance == null)
            instance = new DefaultBranchStyleSelector();
        return instance;
    }

}