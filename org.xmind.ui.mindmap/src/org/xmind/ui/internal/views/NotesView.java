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
package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.Core;
import org.xmind.core.INotes;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.gef.EditDomain;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyNotesCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.FindReplaceAction;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider;
import org.xmind.ui.internal.notes.NotesFindReplaceOperationProvider;
import org.xmind.ui.internal.notes.NotesViewer;
import org.xmind.ui.internal.notes.RichDocumentNotesAdapter;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.richtext.FullRichTextActionBarContributor;
import org.xmind.ui.richtext.Hyperlink;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.IRichDocumentListener;
import org.xmind.ui.richtext.IRichTextAction;
import org.xmind.ui.richtext.IRichTextActionBarContributor;
import org.xmind.ui.richtext.IRichTextEditViewer;
import org.xmind.ui.richtext.IRichTextRenderer;
import org.xmind.ui.richtext.ImagePlaceHolder;
import org.xmind.ui.richtext.LineStyle;
import org.xmind.ui.richtext.RichTextUtils;
import org.xmind.ui.util.Logger;

public class NotesView extends ViewPart implements IPartListener,
        ISelectionListener, ICoreEventListener, IDocumentListener,
        IRichDocumentListener, IContributedContentsView,
        ISelectionChangedListener {

    private static boolean DEBUG = false;

    private class InsertImageAction extends Action implements IRichTextAction {

        private IRichTextEditViewer viewer;

        public InsertImageAction(IRichTextEditViewer viewer) {
            super(MindMapMessages.InsertImage_text, MindMapUI.getImages().get(
                    IMindMapImages.INSERT_IMAGE, true));
            this.viewer = viewer;
            setToolTipText(MindMapMessages.NotesView_InsertImage_toolTip);
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.INSERT_IMAGE, false));
        }

        public void run() {
            if (viewer == null || viewer.getControl().isDisposed()
                    || adapter == null)
                return;

            String path = getPath();
            if (path == null)
                return;

            Image image = adapter.createImageFromFile(path);
            if (image == null)
                return;

            viewer.getRenderer().insertImage(image);
        }

        private String getPath() {
            FileDialog fd = new FileDialog(getSite().getShell(), SWT.OPEN);
            DialogUtils.makeDefaultImageSelectorDialog(fd, true);
            return fd.open();
        }

        public void dispose() {
            viewer = null;
        }

        public void selctionChanged(IRichTextEditViewer viewer,
                ISelection selection) {
        }

    }

    private class InsertHyperlinkAction extends Action implements
            IRichTextAction {

        private IRichTextEditViewer viewer;

        public InsertHyperlinkAction(IRichTextEditViewer viewer) {
            super(MindMapMessages.InsertHyperlinkAction_text, MindMapUI
                    .getImages().get(IMindMapImages.HYPERLINK, true));
            setToolTipText(MindMapMessages.InserthyperlinkAction_toolTip);
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.HYPERLINK, false));
            this.viewer = viewer;
        }

        public void run() {
            IRichTextRenderer renderer = viewer.getRenderer();
            ITextSelection selection = (ITextSelection) viewer.getSelection();
            String oldText = selection.getText();

            int start = selection.getOffset();
            int end = start + selection.getLength();

            Hyperlink[] oldHyperlinks = renderer.getSelectionHyperlinks();
            String oldHref = null;
            Hyperlink oldHyperlink = null;
            if (oldHyperlinks.length == 1) {
                Hyperlink link = oldHyperlinks[0];
                if (link.start <= selection.getOffset()
                        && link.end() >= selection.getOffset()
                                + selection.getLength()) {
                    // selection within the hyperlink
                    oldHyperlink = link;
                    oldHref = link.href;
                    try {
                        oldText = viewer.getDocument().get(link.start,
                                link.length);
                        start = link.start;
                        end = start + link.length;

                    } catch (BadLocationException e) {
                        String message = String
                                .format(
                                        "Unexpected hyperlink range: start=%d, length=%d", //$NON-NLS-1$
                                        link.start, link.length);
                        Logger.log(e, message);
                    }
                }
            }

            ImagePlaceHolder[] images = viewer.getDocument().getImages();
            int temp = -1;
            for (int i = 0; i < images.length; i++) {
                ImagePlaceHolder image = images[i];
                if (image.offset >= end)
                    break;
                if (image.offset >= start && image.offset <= end) {
                    temp++;
                    int offset = image.offset - start;
                    oldText = oldText.substring(0, offset - temp)
                            + oldText.substring(offset + 1 - temp);
                }
            }

            NotesHyperlinkDialog dialog = new NotesHyperlinkDialog(getSite()
                    .getShell(), oldHref, oldText);
            int ret = dialog.open();
            if (ret == NotesHyperlinkDialog.OK) {
                String newText = dialog.getDisplayText();
                String newHref = dialog.getHref();
                if (oldHyperlink != null && newText.equals(oldText)) {
                    if (!oldHyperlink.href.equals(newHref)) {
                        RichTextUtils.replaceHyperlinkHref(
                                viewer.getDocument(), oldHyperlink, newHref);
                    }
                } else {
                    if ("".equals(newText)) { //$NON-NLS-1$
                        newText = newHref;
                    }
                    renderer.insertHyperlink(newHref, newText);
                }
            }
        }

        public void dispose() {
            viewer = null;
        }

        public void selctionChanged(IRichTextEditViewer viewer,
                ISelection selection) {
        }

    }

    private class TextAction extends Action {

        private int op;

        public TextAction(int op) {
            this.op = op;
        }

        public void run() {
            if (viewer == null || viewer.getControl().isDisposed())
                return;

            TextViewer textViewer = viewer.getImplementation().getTextViewer();
            if (textViewer.canDoOperation(op)) {
                textViewer.doOperation(op);
            }
        }

        public void update(TextViewer textViewer) {
            setEnabled(textViewer.canDoOperation(op));
        }
    }

    private class NotesViewRichTextActionBarContributor extends
            FullRichTextActionBarContributor {

        private IRichTextAction insertImageAction;
        private IRichTextAction insertHyperlinkAction;

        protected void makeActions(IRichTextEditViewer viewer) {
            super.makeActions(viewer);

            insertImageAction = new InsertImageAction(viewer);
            addRichTextAction(insertImageAction);

            insertHyperlinkAction = new InsertHyperlinkAction(viewer);
            addRichTextAction(insertHyperlinkAction);
        }

        public void fillToolBar(IToolBarManager toolbar) {
            super.fillToolBar(toolbar);
            toolbar.add(new Separator());
            toolbar.add(insertImageAction);
            toolbar.add(insertHyperlinkAction);
        }
    }

//    private class SaveNotesJob implements IPreSaveListener {
//    
//        public boolean preSave(IWorkbenchPart part) {
//            saveNotes();
//            return true;
//        }
//    }

    private class SaveNotesJob implements ICoreEventListener {

        public void handleCoreEvent(CoreEvent event) {
            saveNotes();
        }

    }

    private IGraphicalEditor contributingEditor;

    private ISelection currentSelection;

    private ITopicPart currentTopicPart;

    private NotesViewer viewer;

    private RichDocumentNotesAdapter adapter;

    private IRichTextActionBarContributor contributor;

    private ICoreEventRegister eventRegister;

    private boolean savingNotes;

    private NotesFindReplaceOperationProvider notesOperationProvider = null;

//    private IPreSaveListener saveNotesJob = null;
//    private SaveNotesJob saveNotesJob = null;
    private ICoreEventRegistration saveNotesReg = null;

    private List<String> textActionIds = new ArrayList<String>(7);

    public IWorkbenchPart getContributingPart() {
        return contributingEditor;
    }

    private boolean updating;

    public void init(IViewSite site) throws PartInitException {
        site.getPage().addSelectionListener(this);
        super.init(site);
    }

    public void createPartControl(Composite parent) {
        contributor = new NotesViewRichTextActionBarContributor();

        viewer = new NotesViewer();
        viewer.setContributor(contributor);
        viewer.createControl(parent);

        contributor.fillMenu(getViewSite().getActionBars().getMenuManager());
        viewer.setInput(null);

        createActions();
        viewer.getImplementation().addPostSelectionChangedListener(this);

        // Listen to part activation events.
        getSite().getPage().addPartListener(this);
        showBootstrapContent();
    }

    private void createActions() {
        TextAction undo = new TextAction(ITextOperationTarget.UNDO);
        undo.setText(MindMapMessages.NotesView_UndoTyping_text);
        undo.setToolTipText(MindMapMessages.NotesView_UndoTyping_toolTip);
        setTextActionHandler(ActionFactory.UNDO.getId(), undo);

        TextAction redo = new TextAction(ITextOperationTarget.REDO);
        redo.setText(MindMapMessages.NotesView_RedoTyping_text);
        redo.setToolTipText(MindMapMessages.NotesView_RedoTyping_toolTip);
        setTextActionHandler(ActionFactory.REDO.getId(), redo);

        setTextActionHandler(ActionFactory.SELECT_ALL.getId(), new TextAction(
                ITextOperationTarget.SELECT_ALL));
        setTextActionHandler(ActionFactory.COPY.getId(), new TextAction(
                ITextOperationTarget.COPY));
        setTextActionHandler(ActionFactory.CUT.getId(), new TextAction(
                ITextOperationTarget.CUT));
        setTextActionHandler(ActionFactory.PASTE.getId(), new TextAction(
                ITextOperationTarget.PASTE));
        setTextActionHandler(ActionFactory.DELETE.getId(), new TextAction(
                ITextOperationTarget.DELETE));

        FindReplaceAction findReplaceAction = new FindReplaceAction(this);
        setTextActionHandler(ActionFactory.FIND.getId(), findReplaceAction);

    }

    private void setTextActionHandler(String id, Action action) {
        textActionIds.add(id);
        getViewSite().getActionBars().setGlobalActionHandler(id, action);
    }

    private void showBootstrapContent() {
        IEditorPart activeEditor = getSite().getPage().getActiveEditor();
        if (activeEditor != null) {
            partActivated(activeEditor);
            ISelection selection;
            if (contributingEditor == null) {
                selection = null;
            } else {
                selection = contributingEditor.getSite().getSelectionProvider()
                        .getSelection();
            }
            editorSelectionChanged(selection);
        }
    }

    public void dispose() {
        editorSelectionChanged(null);
        // stop listening to part activation
        getSite().getPage().removePartListener(this);
        getSite().getPage().removeSelectionListener(this);

        super.dispose();
        textActionIds.clear();
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        viewer = null;
        if (notesOperationProvider != null)
            notesOperationProvider = null;
    }

    public void setFocus() {
        if (viewer != null) {
            viewer.getImplementation().getFocusControl().setFocus();
        }
    }

    public void partActivated(IWorkbenchPart part) {
        if (DEBUG)
            System.out.println("Part activated: " + part); //$NON-NLS-1$
        if (part == this || !(part instanceof IEditorPart))
            return;

        if (part instanceof IGraphicalEditor) {
            setContributingEditor((IGraphicalEditor) part);
        }
    }

    private void setContributingEditor(IGraphicalEditor editor) {
        this.contributingEditor = editor;
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (DEBUG)
            System.out.println("Part closed: " + part); //$NON-NLS-1$
        if (part == this.contributingEditor) {
            selectionChanged(part, null);
            setContributingEditor(null);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {

        if (DEBUG)
            System.out.println("Part deactivated: " + part); //$NON-NLS-1$

//       if the NotesView was closed ,then to run this: 
        if (part == this) {
            saveNotes();
        }
    }

    private void saveNotes() {
        if (adapter == null || currentTopicPart == null || viewer == null
                || viewer.getControl().isDisposed() || !viewer.hasModified()) {
            deactivateJob();
            return;
        }

        if (DEBUG)
            System.out.println("Start saving notes"); //$NON-NLS-1$

        savingNotes = true;
        ITopic topic = currentTopicPart.getTopic();
        ICommandStack cs = getCommandStack();
        if (cs != null) {
            doSaveNotes(topic, cs);
        } else {
            forceSaveNotes(topic);
        }
        savingNotes = false;

        deactivateJob();

        if (DEBUG)
            System.out.println("End saving notes"); //$NON-NLS-1$
    }

    private void forceSaveNotes(ITopic topic) {
        INotes notes = topic.getNotes();
        notes.setContent(INotes.HTML, adapter.makeNewHtmlContent());
        notes.setContent(INotes.PLAIN, adapter.makeNewPlainContent());
    }

    private void doSaveNotes(ITopic topic, ICommandStack cs) {
        ModifyNotesCommand modifyHtml = new ModifyNotesCommand(topic, adapter
                .makeNewHtmlContent(), INotes.HTML);
        ModifyNotesCommand modifyPlain = new ModifyNotesCommand(topic, adapter
                .makeNewPlainContent(), INotes.PLAIN);
        CompoundCommand cmd = new CompoundCommand(modifyHtml, modifyPlain);
        cmd.setLabel(CommandMessages.Command_ModifyNotes);
        cs.execute(cmd);
    }

    private ICommandStack getCommandStack() {
        EditDomain domain = currentTopicPart.getSite().getViewer()
                .getEditDomain();
        if (domain != null) {
            return domain.getCommandStack();
        }
        return null;
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        } else if (adapter == ISaveablePart.class) {
            return getSaveablePart();
        } else if (adapter == IFindReplaceOperationProvider.class) {
            if (notesOperationProvider == null)
                notesOperationProvider = new NotesFindReplaceOperationProvider(
                        viewer);
            return notesOperationProvider;
        }
        return super.getAdapter(adapter);
    }

    private ISaveablePart getSaveablePart() {
        if (getContributingPart() instanceof ISaveablePart)
            return (ISaveablePart) getContributingPart();
        return null;
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part != contributingEditor)
            return;

        editorSelectionChanged(selection);
    }

    private void editorSelectionChanged(ISelection selection) {
        if (selection == currentSelection
                || (selection != null && selection.equals(currentSelection)))
            return;

        currentSelection = selection;
        setCurrentTopicPart(findSelectedTopicPart());
    }

    private void setCurrentTopicPart(ITopicPart topicPart) {
        if (topicPart == currentTopicPart)
            return;

        unhookTopic();
        saveNotes();
        this.currentTopicPart = topicPart;
        forceRefreshViewer();
        hookTopic();
    }

    private void forceRefreshViewer() {
        RichDocumentNotesAdapter oldAdapter = this.adapter;
        if (viewer != null && !viewer.getControl().isDisposed()) {
            this.adapter = createNotesAdapter();
            if (DEBUG)
                if (adapter != null)
                    System.out.println("New adapter created"); //$NON-NLS-1$
            unhookDocument();
            viewer.setInput(adapter);
            if (adapter != null) {
                hookDocument();
            }
        } else {
            this.adapter = null;
        }
        if (oldAdapter != null) {
            oldAdapter.dispose();
            if (DEBUG)
                System.out.println("Old adapter disposed"); //$NON-NLS-1$
        }
        update();
    }

    private void hookDocument() {
        IRichDocument document = viewer.getImplementation().getDocument();
        if (document != null) {
            document.addDocumentListener(this);
            document.addRichDocumentListener(this);
            if (DEBUG)
                System.out.println("Document hooked"); //$NON-NLS-1$
        }
    }

    private void unhookDocument() {
        IRichDocument document = viewer.getImplementation().getDocument();
        if (document != null) {
            document.removeDocumentListener(this);
            document.removeRichDocumentListener(this);
            if (DEBUG)
                System.out.println("Document unhooked"); //$NON-NLS-1$
        }
    }

    private void unhookTopic() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            if (DEBUG)
                System.out.println("Model listeners uninstalled"); //$NON-NLS-1$
            eventRegister = null;
        }
    }

    private void hookTopic() {
        if (currentTopicPart != null) {
            ITopic topic = currentTopicPart.getTopic();
            if (topic instanceof ICoreEventSource) {
                if (eventRegister == null)
                    eventRegister = new CoreEventRegister(
                            (ICoreEventSource) topic, this);
                eventRegister.register(Core.TopicNotes);
                if (DEBUG)
                    System.out.println("Model listeners installed"); //$NON-NLS-1$
            }
        }
    }

    private RichDocumentNotesAdapter createNotesAdapter() {
        if (currentTopicPart == null)
            return null;
        ITopic topic = currentTopicPart.getTopic();
        return new RichDocumentNotesAdapter(topic);
    }

    private ITopicPart findSelectedTopicPart() {
        if (contributingEditor == null)
            return null;

        if (currentSelection == null || currentSelection.isEmpty()
                || !(currentSelection instanceof IStructuredSelection))
            return null;

        Object o = ((IStructuredSelection) currentSelection).getFirstElement();

        IGraphicalEditorPage page = contributingEditor.getActivePageInstance();
        if (page == null)
            return null;

        IPart part = page.getViewer().findPart(o);
        if (part instanceof ITopicPart)
            return (ITopicPart) part;

        return null;
    }

    public void handleCoreEvent(CoreEvent event) {
        String eventType = event.getType();
        if (Core.TopicNotes.equals(eventType)) {
            handleNotesChanged();
        }
    }

    private void handleNotesChanged() {
        if (savingNotes)
            return;

        forceRefreshViewer();
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    public void documentChanged(DocumentEvent event) {
        update();
    }

    private void updateJob() {
        if (viewer == null || viewer.getControl().isDisposed())
            return;

        if (viewer.hasModified()) {
            activateJob();
        } else {
            deactivateJob();
        }
    }

    private void activateJob() {
        if (saveNotesReg != null && saveNotesReg.isValid())
            return;

        saveNotesReg = null;
        //saveNotesJob = new SaveNotesJob();
        IWorkbook workbook = (IWorkbook) contributingEditor
                .getAdapter(IWorkbook.class);
        if (workbook instanceof ICoreEventSource2) {
            saveNotesReg = ((ICoreEventSource2) workbook)
                    .registerOnceCoreEventListener(Core.WorkbookPreSaveOnce,
                            new SaveNotesJob());
            if (DEBUG)
                System.out.println("Job acitvated"); //$NON-NLS-1$
        }
//        if (contributingEditor instanceof ISaveablePart3) {
//            ((ISaveablePart3) contributingEditor)
//                    .addPreSaveListener(saveNotesJob);
//            if (DEBUG)
//                System.out.println("Job acitvated"); //$NON-NLS-1$
//        }
    }

    private void deactivateJob() {
        if (saveNotesReg != null) {
            saveNotesReg.unregister();
            saveNotesReg = null;
        }
//        if (saveNotesJob == null)
//            return;
//
//        IWorkbookRef ref = (IWorkbookRef) contributingEditor
//                .getAdapter(IWorkbookRef.class);
//        if (ref != null) {
//            ref.removeDirtyMarker(saveNotesJob);
//            if (DEBUG)
//                System.out.println("Job deactivated"); //$NON-NLS-1$
//        }
//
//        if (contributingEditor instanceof ISaveablePart3) {
//            ((ISaveablePart3) contributingEditor)
//                    .removePreSaveListener(saveNotesJob);
//            if (DEBUG)
//                System.out.println("Job deactivated"); //$NON-NLS-1$
//        }
//        saveNotesJob = null;
    }

    private void updateTextActions() {
        if (viewer == null || viewer.getControl().isDisposed())
            return;
        TextViewer textViewer = viewer.getImplementation().getTextViewer();
        if (textViewer != null) {
            IActionBars actionBars = getViewSite().getActionBars();
            for (String id : textActionIds) {
                IAction handler = actionBars.getGlobalActionHandler(id);
                if (handler instanceof TextAction) {
                    ((TextAction) handler).update(textViewer);
                }
            }
        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        update();
    }

    public void imageChanged(IRichDocument document,
            ImagePlaceHolder[] oldImages, ImagePlaceHolder[] newImages) {
        update();
    }

    public void lineStyleChanged(IRichDocument document,
            LineStyle[] oldLineStyles, LineStyle[] newLineStyles) {
        update();
    }

    public void textStyleChanged(IRichDocument document,
            StyleRange[] oldTextStyles, StyleRange[] newTextStyles) {
        update();
    }

    public void hyperlinkChanged(IRichDocument document,
            Hyperlink[] oldHyperlinks, Hyperlink[] newHyperlinks) {
        update();
    }

    private void update() {
        if (updating)
            return;

        updating = true;
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                updateJob();
                updateTextActions();
                updating = false;
            }

        });
    }

}