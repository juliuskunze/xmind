package org.xmind.ui.internal.protocols;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IProtocol;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class TopicProtocol implements IProtocol {

//    private Collection<ITopic> analyzingTopics = new HashSet<ITopic>();

    public TopicProtocol() {
    }

    public IAction createOpenHyperlinkAction(final Object context,
            final String uri) {
        final IWorkbook workbook = MindMapUtils.findWorkbook(context);
        if (workbook == null)
            return null;
        final Object element = HyperlinkUtils.findElement(uri, workbook);
//        if (element == null)
//            return null;
        ImageDescriptor icon = getIcon(element);
        if (icon == null) {
            icon = MindMapUI.getImages().get(IMindMapImages.UNKNOWN_FILE, true);
        }
        String title = getTitle(element);
        String sheetTitle = getSheetTitle(element);
        String name;
        if (sheetTitle != null) {
            name = String.format("%s (%s)", title, sheetTitle); //$NON-NLS-1$
        } else {
            name = title;
        }

        Action action = new Action(
                MindMapMessages.TopicProtocol_GoToTopic_text, icon) {
            public void run() {
                if (element != null) {
                    navigateTo(context, element, workbook);
                }
            }
        };
        action.setToolTipText(name);
        return action;
    }

    /**
     * @param element
     * @return
     */
    private ImageDescriptor getIcon(Object element) {
        if (element instanceof ITopic) {
            ITopic topic = (ITopic) element;
            return MindMapUI.getImages().getTopicIcon(topic, true);
        }
        return null;
    }

    /**
     * @param element
     * @return
     */
    private String getSheetTitle(Object element) {
        ISheet sheet = getSheet(element);
        return sheet == null ? null : sheet.getTitleText();
    }

    /**
     * @param element
     * @return
     */
    private ISheet getSheet(Object element) {
        return MindMapUtils.findSheet(element);
    }

    /**
     * @param element
     * @return
     */
    private String getTitle(Object element) {
        return MindMapUtils.getText(element);
    }

    private static void navigateTo(Object context, Object element,
            IWorkbook workbook) {
        if (context instanceof IAdaptable) {
            ISelectionProvider selectionProvider = (ISelectionProvider) ((IAdaptable) context)
                    .getAdapter(ISelectionProvider.class);
            if (selectionProvider != null) {
                selectionProvider
                        .setSelection(new StructuredSelection(element));
                return;
            }
        }
    }

    public boolean isHyperlinkModifiable(Object source, String uri) {
        return true;
    }

}
