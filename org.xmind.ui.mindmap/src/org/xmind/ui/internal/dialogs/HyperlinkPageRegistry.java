package org.xmind.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class HyperlinkPageRegistry extends RegistryReader {

    private static HyperlinkPageRegistry instance = null;

    public static HyperlinkPageRegistry getInstance() {
        if (instance == null)
            instance = new HyperlinkPageRegistry();
        return instance;
    }

    private List<HyperlinkPageDescriptor> hyperlinkPageDescriptors = null;

    private HyperlinkPageRegistry() {
    }

    public List<HyperlinkPageDescriptor> getHyperlinkPageDescriptors() {
        ensureLoaded();
        return hyperlinkPageDescriptors;
    }

    private void ensureLoaded() {
        if (hyperlinkPageDescriptors != null)
            return;
        lazyLoad();
        if (hyperlinkPageDescriptors == null)
            hyperlinkPageDescriptors = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                RegistryConstants.EXT_HYPERLINKPAGE);
    }

    @Override
    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (name.equals(RegistryConstants.TAG_HYPER_PAGE)) {
            readHyperlinkPageDescriptor(element);
            return true;
        }
        return false;
    }

    public void readHyperlinkPageDescriptor(IConfigurationElement element) {
        try {
            registerHyperlinkPageDescriptor(new HyperlinkPageDescriptor(element));
        } catch (CoreException e) {
            Logger.log(e);
        }
    }

    private void registerHyperlinkPageDescriptor(
            HyperlinkPageDescriptor hyperlinkPageDescriptor) {
        if (hyperlinkPageDescriptors == null)
            hyperlinkPageDescriptors = new ArrayList<HyperlinkPageDescriptor>();
        hyperlinkPageDescriptors.add(hyperlinkPageDescriptor);
    }

}
