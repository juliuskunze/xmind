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
package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
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
import org.xmind.ui.internal.spellsupport.SpellingSupport;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.richtext.FullRichTextActionBarContributor;
import org.xmind.ui.richtext.Hyperlink;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.IRichDocumentListener;
import org.xmind.ui.richtext.IRichTextAction;
import org.xmind.ui.richtext.IRichTextEditViewer;
import org.xmind.ui.richtext.IRichTextRenderer;
import org.xmind.ui.richtext.ImagePlaceHolder;
import org.xmind.ui.richtext.LineStyle;
import org.xmind.ui.richtext.RichTextUtils;
import org.xmind.ui.richtext.TextActionConstants;
import org.xmind.ui.texteditor.IMenuContributor;
import org.xmind.ui.texteditor.ISpellingActivation;
import org.xmind.ui.util.Logger;

public class NotesView extends ViewPart implements IPartListener,
        ISelectionListener, ICoreEventListener, IDocumentListener,
        IRichDocumentListener, IContributedContentsView,
        ISelectionChangedListener {

    private static final String NOTES_EDIT_CONTEXT_ID = "org.xmind.ui.context.notes.edit"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    private class ContextActivator implements FocusListener, DisposeListener {
        IContextActivation context;
        IContextService service;

        public ContextActivator(Control control) {
            control.addFocusListener(this);
            control.addDisposeListener(this);
        }

        public void focusLost(FocusEvent e) {
            deactivateContext();
        }

        private void deactivateContext() {
            if (service != null && context != null)
                service.deactivateContext(context);
            context = null;
        }

        public void focusGained(FocusEvent e) {
            activateContext();
        }

        private void activateContext() {
            if (service == null)
                service = (IContextService) getSite().getService(
                        IContextService.class);
            if (service != null) {
                context = service.activateContext(NOTES_EDIT_CONTEXT_ID);
            }
        }

        public void widgetDisposed(DisposeEvent e) {
            deactivateContext();
        }
    }

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
                                .format("Unexpected hyperlink range: start=%d, length=%d", //$NON-NLS-1$
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

        @Override
        public void fillContextMenu(IMenuManager menu) {
            menu.add(getGlobalAction(ActionFactory.UNDO.getId()));
            menu.add(getGlobalAction(ActionFactory.REDO.getId()));
            menu.add(new Separator());
            menu.add(getGlobalAction(ActionFactory.CUT.getId()));
            menu.add(getGlobalAction(ActionFactory.COPY.getId()));
            menu.add(getGlobalAction(ActionFactory.PASTE.getId()));
            menu.add(new Separator());
            menu.add(getGlobalAction(ActionFactory.SELECT_ALL.getId()));
            menu.add(new Separator());
            super.fillContextMenu(menu);
            if (spellingActivation != null) {
                IMenuContributor contributor = (IMenuContributor) spellingActivation
                        .getAdapter(IMenuContributor.class);
                if (contributor != null) {
                    menu.add(new Separator());
                    contributor.fillMenu(menu);
                }
            }
        }
    }

    private class CommitNotesHandler extends AbstractHandler {

        public Object execute(ExecutionEvent event) throws ExecutionException {
            saveNotes();
            IWorkbenchPage page = getSite().getPage();
            if (page != null && contributingEditor != null
                    && page == contributingEditor.getSite().getPage()) {
                page.activate(contributingEditor);
            }
            return null;
        }

    }

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

    private NotesViewRichTextActionBarContributor contributor;

    private ICoreEventRegister eventRegister;

    private IHandlerService handlerService;

    private List<IHandlerActivation> handlerActivations;

    private ISpellingActivation spellingActivation;

    private boolean savingNotes;

    private NotesFindReplaceOperationProvider notesOperationProvider = null;

    private ICoreEventRegistration saveNotesReg = null;

    private Map<String, IWorkbenchAction> workbenchActions = new HashMap<String, IWorkbenchAction>(
            7);

    private List<TextAction> textActions = new ArrayList<TextAction>(7);

    private IWorkbenchAction findReplaceAction;

    private IHandlerActivation commitHandlerActivation;

    public IWorkbenchPart getContributingPart() {
        return contributingEditor;
    }

    private boolean updating;

    public void createPartControl(Composite parent) {
        contributor = new NotesViewRichTextActionBarContributor();
        viewer = new NotesViewer();
        viewer.setContributor(contributor);
        viewer.createControl(parent);
        viewer.getImplementation().addPostSelectionChangedListener(this);
        viewer.setInput(null);

        addSpellChecker();

        activateHandlers();
        IActionBars actionBars = getViewSite().getActionBars();
        createActions(actionBars);
        contributor.fillMenu(actionBars.getMenuManager());
        new ContextActivator(viewer.getImplementation().getFocusControl());

        getSite().getPage().addPartListener(this);
        getSite().getPage().addSelectionListener(this);
        showBootstrapContent();
    }

    private void activateHandlers() {
        IHandlerService handlerService = (IHandlerService) getSite()
                .getService(IHandlerService.class);
        if (handlerService != null) {
            commitHandlerActivation = handlerService.activateHandler(
                    "org.xmind.ui.command.commitNotes", //$NON-NLS-1$
                    new CommitNotesHandler());
        }
    }

    private void addSpellChecker() {
        spellingActivation = SpellingSupport.getInstance().activateSpelling(
                viewer.getImplementation().getTextViewer());
    }

    private void createActions(IActionBars actionBars) {
        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        addGlobalTextAction(actionBars, window, ActionFactory.UNDO,
                ITextOperationTarget.UNDO);
        addGlobalTextAction(actionBars, window, ActionFactory.REDO,
                ITextOperationTarget.REDO);
        addGlobalTextAction(actionBars, window, ActionFactory.CUT,
                ITextOperationTarget.CUT);
        addGlobalTextAction(actionBars, window, ActionFactory.COPY,
                ITextOperationTarget.COPY);
        addGlobalTextAction(actionBars, window, ActionFactory.PASTE,
                ITextOperationTarget.PASTE);
        addGlobalTextAction(actionBars, window, ActionFactory.SELECT_ALL,
                ITextOperationTarget.SELECT_ALL);

        IWorkbenchAction action = ActionFactory.FIND.create(window);
        workbenchActions.put(action.getId(), action);
        actionBars.setGlobalActionHandler(action.getId(),
                findReplaceAction = new FindReplaceAction(window));

        registerTextActionHandlers();
    }

    private void addGlobalTextAction(IActionBars actionBars,
            IWorkbenchWindow window, ActionFactory actionFactory, int textOp) {
        IWorkbenchAction action = actionFactory.create(window);
        workbenchActions.put(action.getId(), action);
        TextAction textAction = new TextAction(textOp);
        textActions.add(textAction);
        actionBars.setGlobalActionHandler(action.getId(), textAction);
    }

    private IWorkbenchAction getGlobalAction(String actionId) {
        return workbenchActions == null ? null : workbenchActions.get(actionId);
    }

    private void registerTextActionHandlers() {
        handlerService = (IHandlerService) getSite().getService(
                IHandlerService.class);
        if (handlerService != null) {
            activateHandler(TextActionConstants.FONT_ID,
                    "org.xmind.ui.command.text.font"); //$NON-NLS-1$
            activateHandler(TextActionConstants.BOLD_ID,
                    "org.xmind.ui.command.text.bold"); //$NON-NLS-1$
            activateHandler(TextActionConstants.ITALIC_ID,
                    "org.xmind.ui.command.text.italic"); //$NON-NLS-1$
            activateHandler(TextActionConstants.UNDERLINE_ID,
                    "org.xmind.ui.command.text.underline"); //$NON-NLS-1$
            activateHandler(TextActionConstants.LEFT_ALIGN_ID,
                    "org.xmind.ui.command.text.leftAlign"); //$NON-NLS-1$
            activateHandler(TextActionConstants.CENTER_ALIGN_ID,
                    "org.xmind.ui.command.text.centerAlign"); //$NON-NLS-1$
            activateHandler(TextActionConstants.RIGHT_ALIGN_ID,
                    "org.xmind.ui.command.text.rightAlign"); //$NON-NLS-1$
        }
    }

    private void activateHandler(String actionId, String commandId) {
        IRichTextAction action = contributor.getRichTextAction(actionId);
        if (action != null) {
            action.setActionDefinitionId(commandId);
            IHandlerActivation activation = handlerService.activateHandler(
                    commandId, new ActionHandler(action));
            if (handlerActivations == null)
                handlerActivations = new ArrayList<IHandlerActivation>(10);
            handlerActivations.add(activation);
        }
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
        deactivateHandlers();
        if (spellingActivation != null) {
            spellingActivation.getSpellingSupport().deactivateSpelling(
                    spellingActivation);
            spellingActivation = null;
        }

        editorSelectionChanged(null);
        getSite().getPage().removePartListener(this);
        getSite().getPage().removeSelectionListener(this);

        if (handlerService != null && handlerActivations != null) {
            for (IHandlerActivation activation : handlerActivations) {
                handlerService.deactivateHandler(activation);
            }
        }
        handlerService = null;
        handlerActivations = null;

        super.dispose();

        if (findReplaceAction != null) {
            findReplaceAction.dispose();
            findReplaceAction = null;
        }
        if (workbenchActions != null) {
            for (IWorkbenchAction action : workbenchActions.values()) {
                action.dispose();
            }
            workbenchActions = null;
        }
        textActions = null;
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        viewer = null;
    }

    private void deactivateHandlers() {
        if (commitHandlerActivation != null) {
            commitHandlerActivation.getHandlerService().deactivateHandler(
                    commitHandlerActivation);
            commitHandlerActivation = null;
        }
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
        viewer.resetModified();
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
        ModifyNotesCommand modifyHtml = new ModifyNotesCommand(topic,
                adapter.makeNewHtmlContent(), INotes.HTML);
        ModifyNotesCommand modifyPlain = new ModifyNotesCommand(topic,
                adapter.makeNewPlainContent(), INotes.PLAIN);
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
            if (notesOperationProvider == null) {
                notesOperationProvider = new NotesFindReplaceOperationProvider(
                        this);
            }
            return notesOperationProvider;
        } else if (adapter == IFindReplaceTarget.class) {
            if (viewer != null) {
                IRichTextEditViewer rtViewer = viewer.getImplementation();
                if (rtViewer != null) {
                    TextViewer textViewer = rtViewer.getTextViewer();
                    if (textViewer != null)
                        return textViewer.getFindReplaceTarget();
                }
            }
        } else if (adapter == ITextViewer.class) {
            if (viewer != null) {
                IRichTextEditViewer rtViewer = viewer.getImplementation();
                if (rtViewer != null)
                    return rtViewer.getTextViewer();
            }
        } else if (adapter == IRichTextEditViewer.class) {
            if (viewer != null) {
                return viewer.getImplementation();
            }
        } else if (adapter == ITopicPart.class) {
            return currentTopicPart;
        } else if (adapter == ITopic.class) {
            return currentTopicPart == null ? null : currentTopicPart
                    .getTopic();
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
        IWorkbook workbook = (IWorkbook) contributingEditor
                .getAdapter(IWorkbook.class);
        if (workbook instanceof ICoreEventSource2) {
            saveNotesReg = ((ICoreEventSource2) workbook)
                    .registerOnceCoreEventListener(Core.WorkbookPreSaveOnce,
                            new SaveNotesJob());
            if (DEBUG)
                System.out.println("Job acitvated"); //$NON-NLS-1$
        }
    }

    private void deactivateJob() {
        if (saveNotesReg != null) {
            saveNotesReg.unregister();
            saveNotesReg = null;
        }
    }

    private void updateTextActions() {
        if (viewer == null || viewer.getControl().isDisposed()
                || textActions == null || textActions.isEmpty())
            return;
        TextViewer textViewer = viewer.getImplementation().getTextViewer();
        if (textViewer != null) {
            for (TextAction action : textActions) {
                action.update(textViewer);
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