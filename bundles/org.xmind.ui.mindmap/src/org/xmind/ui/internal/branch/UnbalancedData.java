package org.xmind.ui.internal.branch;

import static org.xmind.ui.internal.branch.BaseRadialStructure.CACHE_NUMBER_RIGHT_BRANCHES;

import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class UnbalancedData extends RadialData {

    public final static String STRUCTUREID_UNBALANCED = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
    public final static String EXTENTION_UNBALANCEDSTRUCTURE = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
    public final static String EXTENTIONELEMENT_RIGHTNUMBER = "right-number";//$NON-NLS-1$

    public UnbalancedData(IBranchPart branch) {
        super(branch);
    }

    @Override
    public int getNumRight() {
        IBranchPart branch = getBranch();
        Integer num = (Integer) MindMapUtils.getCache(branch,
                CACHE_NUMBER_RIGHT_BRANCHES);
        if (num != null)
            return num.intValue();
        int superRightNum = super.getNumRight();
//        if (branch.isCentral()) {
        boolean isChangedStructure = false;
        ITopicExtension extension = branch.getTopic().getExtension(
                EXTENTION_UNBALANCEDSTRUCTURE);
        if (extension == null) {
            extension = branch.getTopic().createExtension(
                    EXTENTION_UNBALANCEDSTRUCTURE);
            isChangedStructure = true;
        }
        ITopicExtensionElement element = extension.getContent()
                .getCreatedChild(EXTENTIONELEMENT_RIGHTNUMBER);
        if (isChangedStructure) {
            element.setTextContent(String.valueOf(superRightNum));
            return superRightNum;
        }
        String rightNum = element.getTextContent();
        if (rightNum != null) {
            int value = Integer.valueOf(rightNum).intValue();
            return value;
//            if (value <= branch.getTopic().getChildren(ITopic.ATTACHED).size())
//            else {
//                branch.getTopic()
//                        .deleteExtension(EXTENTION_UNBALANCEDSTRUCTURE);
//                return 0;
//            }
        } else {
            element.setTextContent(String.valueOf(0));
            return 0;
        }
//        }
//        return superRightNum;
    }

}
