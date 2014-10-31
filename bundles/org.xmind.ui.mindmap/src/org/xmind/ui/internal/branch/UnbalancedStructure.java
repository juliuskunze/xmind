package org.xmind.ui.internal.branch;

import org.xmind.ui.mindmap.IBranchPart;

public class UnbalancedStructure extends ClockwiseRadialStructure {

    @Override
    protected Object createStructureData(IBranchPart branch) {
        return new UnbalancedData(branch);
    }
}
