/* ******************************************************************************
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmind.core.IBoundary;
import org.xmind.core.ITopic;
import org.xmind.gef.Request;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.INavigableBranchStructureExtension;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class TopicNavigablePolicy extends MindMapNavigablePolicyBase {

    private boolean ignoreCache = true;

    private IBranchPart tempPart = null;

    private boolean isSummaryPart = false;

    private IBoundaryPart[] EMPTY = new IBoundaryPart[0];

    private Map<IBranchPart, List<IBoundaryPart>> map = null;

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || MindMapUI.REQ_NAV_CHILD.equals(requestType)
                || MindMapUI.REQ_NAV_SIBLING.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (MindMapUI.REQ_NAV_CHILD.equals(reqType)) {
            navChild(request);
        } else if (MindMapUI.REQ_NAV_SIBLING.equals(reqType)) {
            navSibling(request);
        } else {
            super.handle(request);
        }
    }

    private void navChild(Request request) {
        IPart source = request.getPrimaryTarget();
        if (source instanceof ITopicPart) {
            ITopicPart topicPart = (ITopicPart) source;
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                IBranchPart child = findFirstChild(branch);
                if (child != null) {
                    ITopicPart childTopicPart = child.getTopicPart();
                    if (childTopicPart != null)
                        setNavigationResult(request, Arrays
                                .asList(childTopicPart));
                }
            }
        }
    }

    private void navSibling(Request request) {
        IPart source = request.getPrimaryTarget();
        if (source instanceof ITopicPart) {
            ITopicPart topicPart = (ITopicPart) source;
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                if (branch.isCentral()) {
                    IBranchPart child = findFirstChild(branch);
                    if (child != null) {
                        setNavigationResult(request, Arrays.asList(child
                                .getTopicPart()));
                    }
                }
                IBranchPart sibling = findSucceedingSiblingOrAncestor(branch);
                if (sibling != branch) {
                    setNavigationResult(request, Arrays.asList(sibling
                            .getTopicPart()));
                }
            }
        }
    }

    protected IPart findNewNavParts(Request request, String navType,
            List<IPart> sources) {
        ITopicPart topicPart = MindMapUtils.findTopicPart(request
                .getPrimaryTarget());
        if (topicPart != null) {
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                IPart navPart = findNavPart(branch, navType);
                if (navPart != null && navPart.getStatus().isActive()) {
                    return navPart;
                }
            }
        }
        return null;
    }

    private IPart findNavPart(IBranchPart branch, String navType) {
        IStructure structure = branch.getBranchPolicy().getStructure(branch);
        if (structure instanceof INavigableBranchStructureExtension) {
            IPart navPart = ((INavigableBranchStructureExtension) structure)
                    .calcNavigation(branch, navType);
            if (navPart == null) {
                IBranchPart parent = branch.getParentBranch();
                if (parent != null) {
                    IStructure parentStructure = parent.getBranchPolicy()
                            .getStructure(parent);
                    if (parentStructure instanceof INavigableBranchStructureExtension) {
                        navPart = ((INavigableBranchStructureExtension) parentStructure)
                                .calcChildNavigation(parent, branch, navType,
                                        false);
                    }
                } else {
                    navPart = calcNavThroughFloatingAndCentral(branch, navType);
                }
                if (navPart == null) {
                    ignoreCache = true;
                    navPart = calcNavByPosition(branch, navType);
                }
            }
            return navPart;
        }
        return null;
    }

    protected void setNavCaches(List<IPart> sources, IPart target,
            String navType) {
        if (!ignoreCache) {
//            super.setNavCaches(sources, target, navType);
        }
        ignoreCache = false;
    }

    private IPart calcNavThroughFloatingAndCentral(IBranchPart branch,
            String navType) {
        return calcNavByPosition(branch, navType);
    }

    private IPart calcNavByPosition(IBranchPart branch, String navType) {
        return new PositionSearcher(branch, navType).search();
    }

    protected void findSequentialNavParts(Request request, String navType,
            IPart sequenceStart, List<IPart> sources, List<IPart> result) {
        ITopicPart endTopic = MindMapUtils.findTopicPart(sequenceStart);
        if (endTopic != null) {
            ITopicPart sourceTopic = MindMapUtils.findTopicPart(request
                    .getPrimaryTarget());
            if (sourceTopic != null && isSibling(sourceTopic, endTopic)) {
                IBranchPart endBranch = endTopic.getOwnerBranch();
                IBranchPart sourceBranch = sourceTopic.getOwnerBranch();
                IBranchPart parentBranch = sourceBranch.getParentBranch();
                if (parentBranch != null) {
                    IStructure parentStructure = parentBranch.getBranchPolicy()
                            .getStructure(parentBranch);
                    if (parentStructure instanceof INavigableBranchStructureExtension) {
                        INavigableBranchStructureExtension ext = (INavigableBranchStructureExtension) parentStructure;
                        IPart startPart = ext.calcChildNavigation(parentBranch,
                                sourceBranch, navType, true);
                        IBranchPart startBranch = MindMapUtils
                                .findBranch(startPart);
                        if (startBranch == null) {
                            startBranch = sourceBranch;
                        }
                        List<IBranchPart> list = new ArrayList<IBranchPart>();
                        ext.calcSequentialNavigation(parentBranch, startBranch,
                                endBranch, list);
                        for (IBranchPart branch : list) {
                            result.add(branch.getTopicPart());
                        }
                    }
                } else {
                    addSeqPartsFromFloatingAndCentral(navType, sourceBranch,
                            endBranch, result);
                }
            }
            if (!result.contains(endTopic))
                result.add(endTopic);
        } else {
            super.findSequentialNavParts(request, navType, sequenceStart,
                    sources, result);
        }
    }

    private void addSeqPartsFromFloatingAndCentral(String navType,
            IBranchPart sourceBranch, IBranchPart startBranch,
            List<IPart> result) {
    }

    private boolean isSibling(ITopicPart t1, ITopicPart t2) {
        IBranchPart b1 = t1.getOwnerBranch();
        IBranchPart b2 = t2.getOwnerBranch();
        if (b1 != null && b2 != null) {
            return b1.getParentBranch() == b2.getParentBranch();
        }
        return false;
    }

    protected IPart findNextOrPrev(IPart source, boolean nextOrPrev) {
        if (source != null)
            return findNextOrPrevTopic(source, nextOrPrev);
        return super.findNextOrPrev(source, nextOrPrev);
    }

    private IPart findNextOrPrevTopic(IPart current, boolean nextOrPrev) {
        refreshMap(current);
        if (nextOrPrev) {
            IPart nextPart = findSuccedingPart(current);
            if (map != null)
                map.clear();
            return nextPart;
        }
        IPart prevPart = findPrecedingPart(current);
        if (map != null)
            map.clear();
        return prevPart;
    }

    private IPart findSuccedingPart(IPart current) {
        if (current instanceof ITopicPart) {
            ITopicPart topicPart = (ITopicPart) current;
            IBranchPart branch = topicPart.getOwnerBranch();
            ILabelPart labelPart = getLabelPart(branch);
            if (labelPart != null)
                return labelPart;
            return getNextPart(branch);
        } else if (current instanceof ILabelPart) {
            IBranchPart branch = ((ILabelPart) current).getOwnedBranch();
            return getNextPart(branch);
        } else if (current instanceof IRelationshipPart) {
            IRelationshipPart toFindPart = (IRelationshipPart) current;
            IRelationshipPart findPart = findPrevOrNextRelationPart(toFindPart,
                    true);
            if (toFindPart == findPart)
                return findPart.getOwnerSheet().getCentralBranch()
                        .getTopicPart();
            else
                return findPart;
        } else if (current instanceof IBoundaryPart) {
            IBoundaryPart boundaryPart = (IBoundaryPart) current;
            return getSucceedingPart1(boundaryPart);
        }
        return current;
    }

    private IPart getNextPart(IBranchPart branch) {
        IPart child = getFirstChildPart(branch);
        if (child != null)
            return child;
        IBranchPart branchPart = findSucceedingSiblingOrAncestor(branch);
        return getSucceedingPart(branchPart, branch);
    }

    private IPart getSucceedingPart1(IBoundaryPart current) {
        refreshMap(current);
        IBoundary boundary = current.getBoundary();
        IBranchPart branch = current.getOwnedBranch();
        if (boundary.isMasterBoundary()) {
            return branch.getTopicPart();
        }
        int index = boundary.getStartIndex();
        IBoundaryPart retPart = null;
        List<IBoundaryPart> list = map.get(branch);
        if (list != null) {
            for (IBoundaryPart boundaryPart : list) {
                if (boundaryPart == current) {
                    if (retPart != null)
                        retPart = null;
                    continue;
                }
                int startIndex = boundaryPart.getBoundary().getStartIndex();
                if (index != startIndex) {
                    continue;
                } else {
                    if (retPart == null)
                        retPart = boundaryPart;
                    int endIndex = boundaryPart.getBoundary().getEndIndex();
                    if (endIndex >= retPart.getBoundary().getEndIndex())
                        retPart = boundaryPart;
                }
            }
        }
        if (retPart == null) {
            List<IBranchPart> branches = current.getEnclosingBranches();
            return branches.get(0).getTopicPart();
        }
        return retPart;
    }

    private IPart getFirstChildPart(IBranchPart current) {
        List<IBranchPart> subBranches = current.getSubBranches();
        if (!subBranches.isEmpty()) {
            IBranchPart childBranch = subBranches.get(0);
            if (hasBoundaryPart(current)) {
                IBoundaryPart part = getBoundaryPart(current, childBranch);
                if (part != null)
                    return part;
            }
            return childBranch.getTopicPart();
        }
        return null;
    }

    private IPart getSucceedingPart(IBranchPart succeedPart,
            IBranchPart currentPart) {
        refreshMap(succeedPart);
        if (succeedPart.isCentral()) {
            IRelationshipPart part = getRelationshipPart(succeedPart);
            if (part != null) {
                tempPart = currentPart;
                return part;
            }
        }
        IBranchPart parentBranch = succeedPart.getParentBranch();
        if (parentBranch == null)
            parentBranch = succeedPart;
        if (isSummaryPart) {
            parentBranch = succeedPart;
            isSummaryPart = false;
        }
        IBoundaryPart part = getBoundaryPart(parentBranch, succeedPart);
        if (part != null)
            return part;

        return succeedPart.getTopicPart();
    }

    private ILabelPart getLabelPart(IBranchPart branch) {
        ILabelPart labelPart = branch.getLabel();
        if (labelPart != null)
            return labelPart;
        return null;
    }

    private IRelationshipPart getRelationshipPart(IBranchPart branch) {
        ISheetPart sheetPart = (ISheetPart) branch.getParent();
        List<IRelationshipPart> relations = sheetPart.getRelationships();
        if (relations.size() > 0)
            return relations.get(0);
        return null;
    }

    private IBoundaryPart getBoundaryPart(IBranchPart parent,
            IBranchPart current) {
        IBoundaryPart retPart = null;
        ITopic topic = current.getTopic();
        List<ITopic> children = parent.getTopic().getAllChildren();
        int index1 = children.indexOf(topic);
        if (map == null)
            return retPart;
        List<IBoundaryPart> list = map.get(parent);
        if (list != null)
            for (IBoundaryPart boundaryPart : list) {
                IBoundary boundary = boundaryPart.getBoundary();
                int startIndex = boundary.getStartIndex();
                if (startIndex != index1)
                    continue;
                else {
                    if (retPart == null)
                        retPart = boundaryPart;
                    int endIndex = boundary.getEndIndex();
                    int index2 = retPart.getBoundary().getEndIndex();
                    if (endIndex >= index2)
                        retPart = boundaryPart;
                }
            }
        return retPart;
    }

    private IBranchPart findFirstChild(IBranchPart current) {
        List<IBranchPart> subBranches = current.getSubBranches();
        if (!subBranches.isEmpty()) {
            return subBranches.get(0);
        }
        subBranches = current.getSummaryBranches();
        if (!subBranches.isEmpty()) {
            return subBranches.get(0);
        }
        return null;
    }

    private IBranchPart findSucceedingSiblingOrAncestor(IBranchPart current) {
        IBranchPart parent = current.getParentBranch();
        if (parent != null) {
            List<IBranchPart> branches = parent.getSubBranches();
            int index = branches.indexOf(current);
            if (index >= 0 && index < branches.size() - 1) {
                return branches.get(index + 1);
            }
            int lastBranchIndex = branches.size() - 1;
            branches = parent.getSummaryBranches();
            if (branches.size() > 0 && index == lastBranchIndex) {
                isSummaryPart = true;
                return branches.get(0);
            }
            index = branches.indexOf(current);
            if (index >= 0 && index < branches.size() - 1) {
                isSummaryPart = true;
                return branches.get(index + 1);
            }

            return findSucceedingSiblingOrAncestor(parent);
        } else if (current.getParent() instanceof ISheetPart) {
            ISheetPart sheet = (ISheetPart) current.getParent();
            List<IBranchPart> floatingBranches = sheet.getFloatingBranches();
            if (current == sheet.getCentralBranch()) {
                if (!floatingBranches.isEmpty()) {
                    return floatingBranches.get(0);
                }
            } else {
                int index = floatingBranches.indexOf(current);
                if (index >= 0) {
                    if (index < floatingBranches.size() - 1) {
                        IBranchPart floatBranch = floatingBranches
                                .get(index + 1);
                        return floatBranch;
                    }
                    return sheet.getCentralBranch();
                }
            }
        }
        return current;
    }

    private boolean hasBoundaryPart(IBranchPart current) {
        List<IBoundaryPart> boundaries = current.getBoundaries();
        if (boundaries.isEmpty()) {
            return false;
        }
        return true;
    }

    private IPart findPrecedingPart(IPart current) {
        if (current instanceof ITopicPart) {
            ITopicPart topicPart = (ITopicPart) current;
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                if (branch.isCentral())
                    return null;
                IBranchPart prevBranch = findPrecedingBranch(branch);
                return getPrecedingPart(prevBranch, branch);
            }
        } else if (current instanceof ILabelPart) {
            IBranchPart branch = ((ILabelPart) current).getOwnedBranch();
            return branch.getTopicPart();
        } else if (current instanceof IRelationshipPart) {
            IRelationshipPart toFindPart = (IRelationshipPart) current;
            IRelationshipPart findPart = findPrevOrNextRelationPart(toFindPart,
                    false);
            if (toFindPart == findPart)
                return getPrecedingPart1(tempPart);
            else
                return findPart;

        } else if (current instanceof IBoundaryPart) {
            IBoundaryPart boundaryPart = (IBoundaryPart) current;
            return getPrevPart(boundaryPart);
        }
        return current;
    }

    private IPart getPrevPart(IBoundaryPart current) {
        refreshMap(current);
        IBoundary boundary = current.getBoundary();
        IBranchPart branch = current.getOwnedBranch();
        if (boundary.isMasterBoundary()) {
            IBranchPart prevBranch = findPrecedingBranch(branch);
            return getPrecedingPart1(prevBranch);
        }
        int index1 = boundary.getStartIndex();
        IBoundaryPart retPart = null;
        if (map != null) {
            List<IBoundaryPart> list = map.get(branch);
            if (list != null) {
                for (IBoundaryPart boundaryPart : list) {
                    if (current == boundaryPart)
                        break;
                    int startIndex = boundaryPart.getBoundary().getStartIndex();
                    if (startIndex != index1) {
                        continue;
                    } else {
                        if (retPart == null)
                            retPart = boundaryPart;
                        int endIndex = boundaryPart.getBoundary().getEndIndex();
                        int index2 = retPart.getBoundary().getEndIndex();
                        if (endIndex <= index2)
                            retPart = boundaryPart;
                    }
                }
            }
        }
        if (retPart == null) {
            List<IBranchPart> branches = current.getEnclosingBranches();
            IBranchPart branchPart = branches.get(0);
            IBranchPart prevBranch = findPrecedingBranch(branchPart);
            return getPrecedingPart1(prevBranch);
        }
        return retPart;
    }

    private IPart getPrecedingPart(IBranchPart prevBranch, IBranchPart branch) {
        refreshMap(branch);
        if (prevBranch == branch && prevBranch.isCentral())
            return prevBranch.getTopicPart();
        IBranchPart parent = branch.getParentBranch();
        if (parent == null)
            parent = branch;
        if (isSummaryPart) {
            parent = branch;
            isSummaryPart = false;
        }
        IBoundaryPart part = getPreBoundaryPart(parent, branch);
        if (part != null)
            return part;
        return getPrecedingPart1(prevBranch);
    }

    private IBoundaryPart getPreBoundaryPart(IBranchPart parent,
            IBranchPart current) {
        IBoundaryPart retPart = null;
        ITopic topic = current.getTopic();
        List<ITopic> children = parent.getTopic().getAllChildren();
        int index1 = children.indexOf(topic);
        if (map == null)
            return retPart;
        List<IBoundaryPart> list = map.get(parent);
        if (list != null) {
            for (IBoundaryPart boundaryPart : list) {
                IBoundary boundary = boundaryPart.getBoundary();
                int startIndex = boundary.getStartIndex();
                if (index1 != startIndex)
                    continue;
                else {
                    if (retPart == null)
                        retPart = boundaryPart;
                    int endIndex = boundary.getEndIndex();
                    int index2 = retPart.getBoundary().getEndIndex();
                    if (endIndex <= index2)
                        retPart = boundaryPart;
                }
            }
        }
        return retPart;
    }

    private IPart getPrecedingPart1(IBranchPart branch) {
        IPart labelPart = getLabelPart(branch);
        if (labelPart != null)
            return labelPart;
        return branch.getTopicPart();
    }

    private IBranchPart findPrecedingBranch(IBranchPart current) {
        IBranchPart parent = current.getParentBranch();
        if (parent != null) {
            List<IBranchPart> branches = parent.getSubBranches();
            int index = branches.indexOf(current);
            if (index > 0) {
                return findLastDescendant(branches.get(index - 1));
            }

            int lastBranchIndex = branches.size() - 1;
            branches = parent.getSummaryBranches();
            index = branches.indexOf(current);
            if (index == 0) {
                isSummaryPart = true;
                return parent.getSubBranches().get(lastBranchIndex);
            }
            if (index > 0) {
                isSummaryPart = true;
                return findLastDescendant(branches.get(index - 1));
            }
            return parent;

        } else if (current.getParent() instanceof ISheetPart) {
            ISheetPart sheet = ((ISheetPart) current.getParent());
            List<IBranchPart> floatingBranches = sheet.getFloatingBranches();
            if (current == sheet.getCentralBranch()) {
                if (!floatingBranches.isEmpty()) {
                    IBranchPart lastFloating = floatingBranches
                            .get(floatingBranches.size() - 1);
                    return findLastDescendant(lastFloating);
                }
            } else if (floatingBranches.contains(current)) {
                int index = floatingBranches.indexOf(current);
                if (index == 0) {
                    return findLastDescendant(sheet.getCentralBranch());
                }
                return findLastDescendant(floatingBranches.get(index - 1));
            }
        }
        return current;
    }

    private IBranchPart findLastDescendant(IBranchPart branch) {
        List<IBranchPart> summaryBranches = branch.getSummaryBranches();
        if (!summaryBranches.isEmpty()) {
            return findLastDescendant(summaryBranches.get(summaryBranches
                    .size() - 1));
        }
        List<IBranchPart> subBranches = branch.getSubBranches();
        if (!subBranches.isEmpty()) {
            return findLastDescendant(subBranches.get(subBranches.size() - 1));
        }
        return branch;
    }

    private IRelationshipPart findPrevOrNextRelationPart(
            IRelationshipPart current, boolean nextOrPrev) {
        ISheetPart sheetPart = current.getOwnerSheet();
        if (sheetPart != null) {
            List<IRelationshipPart> relationships = sheetPart
                    .getRelationships();
            int index = relationships.indexOf(current);
            if (nextOrPrev) {
                if (index >= 0 && index < relationships.size() - 1)
                    return relationships.get(index + 1);
            } else {
                if (index > 0)
                    return relationships.get(index - 1);
            }
        }
        return current;
    }

    private void refreshMap(IPart source) {
        IBranchPart current = null;
        if (source instanceof IBranchPart) {
            current = (IBranchPart) source;
        } else if (source instanceof ILabelPart) {
            current = ((ILabelPart) source).getOwnedBranch();
        } else if (source instanceof IBoundaryPart) {
            current = ((IBoundaryPart) source).getOwnedBranch();
        } else if (source instanceof ITopicPart) {
            current = ((ITopicPart) source).getOwnerBranch();
        }
        if (current == null)
            return;
        if (hasBoundaryPart(current)) {
            if (map == null)
                map = new HashMap<IBranchPart, List<IBoundaryPart>>();
            List<IBoundaryPart> boundaries = current.getBoundaries();
            List<IBoundaryPart> list = reSort(boundaries.toArray(EMPTY));
            map.put(current, list);
        }
        IBranchPart parent = current.getParentBranch();
        if (parent != null && hasBoundaryPart(parent)) {
            if (map == null)
                map = new HashMap<IBranchPart, List<IBoundaryPart>>();
            List<IBoundaryPart> list1 = parent.getBoundaries();
            List<IBoundaryPart> list2 = reSort(list1.toArray(EMPTY));
            map.put(parent, list2);
        }
    }

    private List<IBoundaryPart> reSort(IBoundaryPart[] boundaries) {
        List<IBoundaryPart> boundaryList = new ArrayList<IBoundaryPart>(
                boundaries.length);
        for (int i = 0; i < boundaries.length; i++)
            boundaryList.add(boundaries[i]);

        Collections.sort(boundaryList, new Comparator<IBoundaryPart>() {
            public int compare(IBoundaryPart o1, IBoundaryPart o2) {
                IBoundary b1 = o1.getBoundary();
                int startIndex1 = b1.getStartIndex();
                int endIndex1 = b1.getEndIndex();
                IBoundary b2 = o2.getBoundary();
                int startIndex2 = b2.getStartIndex();
                int endIndex2 = b2.getEndIndex();

                if (startIndex1 != startIndex2)
                    return startIndex1 - startIndex2;
                else if (startIndex1 == startIndex2)
                    return endIndex2 - endIndex1;
                return 0;
            }
        });
        return boundaryList;
    }
}