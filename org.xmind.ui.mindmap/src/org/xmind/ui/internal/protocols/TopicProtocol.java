package org.xmind.ui.internal.protocols;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.core.IIdentifiable;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
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
        final Object element = findElement(uri, workbook);
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
//            if (analyzingTopics.contains(topic)) {
            return MindMapUI.getImages().getTopicIcon(topic, true);
//            }
//            analyzingTopics.add(topic);
        }
        return null;
//        try {
//            return MindMapUI.getImages().getElementIcon(element, true);
//        } catch (Throwable e) {
//            return null;
//        } finally {
//            if (element instanceof ITopic) {
//                analyzingTopics.remove(element);
//            }
//        }
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
//        IWorkbookRef workbookRef = MindMapUI.getWorkbookRefManager().findRef(
//                workbook);
//        List<IEditorPart> editors = workbookRef.getOpenedEditors();
//        if (editors.isEmpty())
//            return;
//
//        IEditorPart activeEditor = PlatformUI.getWorkbench()
//                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//        if (editors.contains(activeEditor)) {
//            ISelectionProvider selectionProvider = activeEditor.getSite()
//                    .getSelectionProvider();
//            if (selectionProvider != null) {
//                selectionProvider
//                        .setSelection(new StructuredSelection(element));
//            }
//        }
    }

    public boolean isHyperlinkModifiable(Object source, String uri) {
        return true;
    }

    /**
     * 
     * @param element
     * @return
     */
    public static String toXmindURL(Object element) {
        return toXmindURL(element, null);
    }

    /**
     * 
     * @param element
     * @param workbook
     * @return
     */
    private static String toXmindURL(Object element, IWorkbook workbook) {
        if (element instanceof IIdentifiable) {
            String id = ((IIdentifiable) element).getId();
            String mainPath = getMainPath(element, workbook);
            return "xmind:" + mainPath + "#" + id; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    /**
     * @param element
     * @param workbook
     * @return
     */
    private static String getMainPath(Object element, IWorkbook workbook) {
        if (workbook != null) {
            String file = workbook.getFile();
            if (file != null) {
                return file;
            }
        }
        return ""; //$NON-NLS-1$
    }

    public static Object findElement(String uri, IWorkbook workbook) {
        if (uri.startsWith("xmind:")) { //$NON-NLS-1$
            String path = uri.substring(6);
            if (path.startsWith("#")) { //$NON-NLS-1$
                String id = path.substring(1);
                Object element = workbook.getElementById(id);
                if (element instanceof ITopic) {
                    if (!isAttach((ITopic) element)) {
                        element = null;
                    }
                }
                return element;
            }
        }
        return null;
    }

    private static boolean isAttach(ITopic topic) {
        return topic.getPath().getWorkbook() == topic.getOwnedWorkbook();
    }

}
