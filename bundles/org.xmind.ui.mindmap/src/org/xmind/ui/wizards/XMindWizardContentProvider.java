package org.xmind.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WizardContentProvider;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class XMindWizardContentProvider extends WizardContentProvider {

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof WizardCollectionElement) {
            ArrayList<Object> list = new ArrayList<Object>();
            WizardCollectionElement element = (WizardCollectionElement) parentElement;

            Object[] childCollections = element.getChildren();
            for (int i = 0; i < childCollections.length; i++) {
                handleChild(childCollections[i], list);
            }

            IWizardDescriptor[] childWizards = element.getWizards();
            for (int i = 0; i < childWizards.length; i++) {
                if (needHideProFeatureWizards()) {
                    String wizardId = childWizards[i].getId();
                    if (!getProFeatureExportWizardIds().contains(wizardId))
                        handleChild(childWizards[i], list);
                } else
                    handleChild(childWizards[i], list);
            }

            // flatten lists with only one category
            if (list.size() == 1
                    && list.get(0) instanceof WizardCollectionElement) {
                return getChildren(list.get(0));
            }

            return list.toArray();
        } else if (parentElement instanceof AdaptableList) {
            AdaptableList aList = (AdaptableList) parentElement;
            Object[] children = aList.getChildren();
            ArrayList<Object> list = new ArrayList<Object>(children.length);
            for (int i = 0; i < children.length; i++) {
                handleChild(children[i], list);
            }
            // if there is only one category, return it's children directly (flatten list)
            if (list.size() == 1
                    && list.get(0) instanceof WizardCollectionElement) {
                return getChildren(list.get(0));
            }

            return list.toArray();
        } else {
            return new Object[0];
        }
    }

    private void handleChild(Object element, ArrayList<Object> list) {
        if (element instanceof WizardCollectionElement) {
            if (hasChildren(element)) {
                list.add(element);
            }
        } else {
            list.add(element);
        }
    }

    private boolean needHideProFeatureWizards() {
        String licenseName = System
                .getProperty("org.xmind.product.license_type"); //$NON-NLS-1$
        if ("Pro".equals(licenseName) || "Plus".equals(licenseName) //$NON-NLS-1$//$NON-NLS-2$
                || "Sub".equals(licenseName)) //$NON-NLS-1$
            return false;

        boolean needHideProFeafures = MindMapUIPlugin.getDefault()
                .getPreferenceStore()
                .getBoolean(PrefConstants.HIDE_PRO_FEATURES);
        if (needHideProFeafures)
            return true;

        return false;
    }

    private List<String> getProFeatureExportWizardIds() {
        ArrayList<String> proWizardIds = new ArrayList<String>();
        proWizardIds.add("org.xmind.ui.export.mindmanager"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.exports.vector.exportsvg"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.exports.vector.exportpdfmap"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.word"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.pdf"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.rtf"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.ppt"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.exportexcel"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.exportcsv"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.odt"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.odp"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.exportods"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.export.exportmpp"); //$NON-NLS-1$
        proWizardIds.add("org.xmind.ui.aspose.import.document"); //$NON-NLS-1$
        return proWizardIds;
    }

}
