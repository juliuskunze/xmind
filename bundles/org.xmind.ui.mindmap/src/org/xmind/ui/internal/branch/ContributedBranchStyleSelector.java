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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IBranchStyleSelector;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.LayeredStyleSelector;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.Logger;

public class ContributedBranchStyleSelector extends LayeredStyleSelector
        implements IBranchStyleSelector {

    private BranchPolicyManager manager;

    private IConfigurationElement element;

    private Map<String, List<OverridedStyle>> overridedStyles;

    private Map<String, String> inheritedStyleKeys;

    public ContributedBranchStyleSelector(BranchPolicyManager manager,
            IConfigurationElement element) {
        this.element = element;
        loadInheritedStyles();
    }

    private void loadInheritedStyles() {
//        IConfigurationElement[] children = element
//                .getChildren(RegistryConstants.TAG_INHERITED_STYLE);
//        for (IConfigurationElement child : children) {
//            String key = child.getAttribute(ATT_KEY);
//            if (key != null) {
//                registerInheritedStyleKey(key,
//                        Styles.LAYER_BEFORE_THEME_VALUE);
//            }
//        }
        registerInheritedStyleKey(Styles.LineColor,
                Styles.LAYER_BEFORE_THEME_VALUE);
        registerInheritedStyleKey(Styles.LineWidth,
                Styles.LAYER_BEFORE_THEME_VALUE);
        registerInheritedStyleKey(Styles.LinePattern,
                Styles.LAYER_BEFORE_DEFAULT_VALUE);
    }

    protected void registerInheritedStyleKey(String key, String layer) {
        if (key == null)
            return;
        if (inheritedStyleKeys == null)
            inheritedStyleKeys = new HashMap<String, String>();
        inheritedStyleKeys.put(key, layer);
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

    public void flushStyleCaches(IBranchPart branch) {
        ParentValueProvider.flush(branch);
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
        return super.getThemeStyleValue(part, familyName, key);
    }

    protected String getLayeredProperty(IGraphicalPart part, String layerName,
            String familyName, String key) {
        if (part instanceof IBranchPart && inheritedStyleKeys != null
                && layerName.equals(inheritedStyleKeys.get(key))) {
            String value = getLayeredProperty(part,
                    Styles.LAYER_BEFORE_PARENT_VALUE, familyName, key);
            if (isValidValue(part, key, value)) {
                return getCheckedValue(value);
            }
            value = ParentValueProvider.getValueProvider((IBranchPart) part)
                    .getParentValue(key);
            if (isValidValue(part, key, value))
                return getCheckedValue(value);
        }
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            List<OverridedStyle> overrideds = getOverridedStyles(key);
            if (overrideds != null && !overrideds.isEmpty()) {
                String value = getOverridedValue(branch, layerName, key,
                        overrideds);
                if (value != null)
                    return value;
            }
        }
        return null;
    }

    private String getOverridedValue(IBranchPart branch, String layerName,
            String key, List<OverridedStyle> overridedStyles) {
        IEvaluationContext context = null;
        for (OverridedStyle overridedStyle : overridedStyles) {
            if (overridedStyle.isOnLayer(layerName)) {
                if (context == null)
                    context = BranchPolicyManager
                            .createBranchEvaluationContext(branch);
                if (overridedStyle.isApplicableTo(context)) {
                    String value = overridedStyle.getValue(branch, layerName);
                    if (value != null)
                        return value;
                }
            }
        }
        return null;
    }

    private List<OverridedStyle> getOverridedStyles(String key) {
        ensureLoaded();
        return overridedStyles.get(key);
    }

    private void ensureLoaded() {
        if (overridedStyles != null)
            return;

        lazyLoad();
        if (overridedStyles == null)
            overridedStyles = Collections.emptyMap();
    }

    private void lazyLoad() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_OVERRIDED_STYLE);
        for (IConfigurationElement child : children) {
            loadOverridedStyle(child);
        }
    }

    private void loadOverridedStyle(IConfigurationElement element) {
        try {
            OverridedStyle overridedStyle = new OverridedStyle(manager, element);
            registerOverridedStyle(overridedStyle);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load overrided style: " + element); //$NON-NLS-1$
        }
    }

    private void registerOverridedStyle(OverridedStyle overridedStyle) {
        String key = overridedStyle.getKey();
        if (overridedStyles == null)
            overridedStyles = new HashMap<String, List<OverridedStyle>>();
        List<OverridedStyle> list = overridedStyles.get(key);
        if (list == null) {
            list = new ArrayList<OverridedStyle>();
            overridedStyles.put(key, list);
        }
        list.add(overridedStyle);
    }

}