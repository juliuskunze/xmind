package org.xmind.ui.wizards;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * A Viewer element sorter that sorts Elements by their name attribute. Note
 * that capitalization differences are not considered by this sorter, so a < B <
 * c.
 * 
 * NOTE exceptions to the above: an element with the system's reserved category
 * for Other Wizards will always be sorted such that it will ultimately be
 * placed at the end of the sorted result, and an elemen with the reserved
 * category name for General wizards will always be placed at the beginning of
 * the sorted result.
 * 
 * @since 3.2
 */
class DataTransferWizardCollectionComparator extends ViewerComparator {
    /**
     * Static instance of this class.
     */
    public final static DataTransferWizardCollectionComparator INSTANCE = new DataTransferWizardCollectionComparator();

    /**
     * Creates an instance of <code>DataTransferWizardCollectionSorter</code>.
     * Since this is a stateless sorter, it is only accessible as a singleton;
     * the private visibility of this constructor ensures this.
     */
    private DataTransferWizardCollectionComparator() {
        super();
    }

    public int category(Object element) {
        if (element instanceof WizardCollectionElement) {
            String id = ((WizardCollectionElement) element).getId();
            if (WizardsRegistryReader.GENERAL_WIZARD_CATEGORY.equals(id)) {
                return 1;
            }
            if (WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY.equals(id)) {
                return 3;
            }
            return 2;
        }
        return super.category(element);
    }

    /**
     * Return true if this sorter is affected by a property change of
     * propertyName on the specified element.
     */
    public boolean isSorterProperty(Object object, String propertyId) {
        return propertyId.equals(IBasicPropertyConstants.P_TEXT);
    }
}
