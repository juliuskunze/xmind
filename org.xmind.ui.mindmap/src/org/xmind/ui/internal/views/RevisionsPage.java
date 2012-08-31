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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.Page;
import org.xmind.core.Core;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.AddSheetCommand;
import org.xmind.ui.commands.DeleteRevisionCommand;
import org.xmind.ui.commands.DeleteSheetCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.internal.dialogs.RevisionPreviewDialog;
import org.xmind.ui.viewers.SWTUtils;

/**
 * @author Frank Shaka
 * 
 */
public class RevisionsPage extends Page implements ICoreEventListener,
        IAdaptable {

    /**
     * @author Frank Shaka
     * 
     */
    public static class RevisionContentProvider implements
            IStructuredContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            return ((IRevisionManager) inputElement).getRevisions().toArray();
        }

    }

    private static class RevisionNumberLabelProvider extends
            ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.valueOf(revision.getRevisionNumber());
        }

    }

    private static class RevisionTimeLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.format("%tT", revision.getTimestamp()); //$NON-NLS-1$
        }

    }

    private static class RevisionDateLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.format("%tF", revision.getTimestamp()); //$NON-NLS-1$
        }

    }

    private class RevisionOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            handleOpen(event.getSelection());
        }

    }

    private class RemoveRevisionAction extends Action implements
            ISelectionChangedListener {

        private List<IRevision> revisionsToRemove = new ArrayList<IRevision>();

        public RemoveRevisionAction() {
            setId(ActionConstants.REMOVE_REVISION_ID);
            setEnabled(false);
        }

        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            revisionsToRemove.clear();
            if (!selection.isEmpty()
                    && selection instanceof IStructuredSelection) {
                Iterator it = ((IStructuredSelection) selection).iterator();
                while (it.hasNext()) {
                    Object element = it.next();
                    if (element instanceof IRevision) {
                        revisionsToRemove.add((IRevision) element);
                    }
                }
            }
            setEnabled(!revisionsToRemove.isEmpty());
        }

        @Override
        public void run() {
            if (revisionsToRemove.isEmpty())
                return;
            List<Command> commands = new ArrayList<Command>(
                    revisionsToRemove.size());
            for (IRevision revision : revisionsToRemove) {
                commands.add(new DeleteRevisionCommand(revision));
            }
            String label;
            if (revisionsToRemove.size() > 1) {
                label = MindMapMessages.DeleteMultipleRevisionsCommand_label;
            } else {
                label = MindMapMessages.DeleteSingleRevisionCommand_label;
            }
            Command command = new CompoundCommand(label, commands);
            ICommandStack commandStack = (ICommandStack) source
                    .getParentEditor().getAdapter(ICommandStack.class);
            if (commandStack != null) {
                commandStack.execute(command);
            } else {
                command.execute();
            }
        }

    }

    private class RevertToRevisionAction extends Action implements
            ISelectionChangedListener {

        private IRevision revision = null;

        public RevertToRevisionAction() {
            setId(ActionConstants.REVERT_TO_REVISION_ID);
            setEnabled(false);
        }

        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            this.revision = null;
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) selection;
                if (ss.size() == 1) {
                    Object element = ss.getFirstElement();
                    if (element instanceof IRevision) {
                        this.revision = (IRevision) element;
                    }
                }
            }
            setEnabled(this.revision != null);
        }

        @Override
        public void run() {
            if (revision == null)
                return;

            final IRevisionManager manager = revision.getOwnedManager();
            IRevision latestRevision = manager.getLatestRevision();
            if (latestRevision == null
                    || sheet.getModifiedTime() > latestRevision.getTimestamp()) {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        manager.addRevision(sheet);
                    }
                });
            }

            final IWorkbook workbook = sheet.getOwnedWorkbook();
            final ISheet targetSheet = (ISheet) workbook.importElement(revision
                    .getContent());
            if (targetSheet == null)
                return;

            final int index = sheet.getIndex();
            List<Command> commands = new ArrayList<Command>(2);
            commands.add(new DeleteSheetCommand(sheet));
            commands.add(new AddSheetCommand(targetSheet, workbook, index));
            Command command = new CompoundCommand(
                    MindMapMessages.RevertToRevisionCommand_label, commands);
            ICommandStack commandStack = (ICommandStack) source
                    .getParentEditor().getAdapter(ICommandStack.class);
            if (commandStack != null) {
                commandStack.execute(command);
            } else {
                command.execute();
            }

        }
    }

    private class PreviewRevisionAction extends Action implements
            ISelectionChangedListener {

        private ISelection selection;

        public PreviewRevisionAction() {
            setId(ActionConstants.PREVIEW_REVISIONS);
            setEnabled(false);
        }

        public void selectionChanged(SelectionChangedEvent event) {
            this.selection = event.getSelection();
            setEnabled(!this.selection.isEmpty());
        }

        @Override
        public void run() {
            handleOpen(selection);
        }
    }

    private IGraphicalEditorPage source;

    private ISheet sheet;

    private Control control;

    private TableViewer viewer;

    private Label titleLabel;

    private IRevisionManager revisionManager;

    private CoreEventRegister coreEventRegister = new CoreEventRegister(this);

    private CoreEventRegister topicEventRegister = new CoreEventRegister(this);

    private List<IAction> actions = new ArrayList<IAction>(3);

    /**
     * 
     */
    public RevisionsPage(IGraphicalEditorPage source) {
        this.source = source;
        this.sheet = (ISheet) source.getInput();
        this.revisionManager = this.sheet.getOwnedWorkbook()
                .getRevisionRepository()
                .getRevisionManager(sheet.getId(), IRevision.SHEET);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        Control control = createTitleLabel(composite);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Control control2 = createViewer(composite);
        control2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        setControl(composite);

        registerCoreEvents();

        getSite().setSelectionProvider(viewer);

        addAction(new RemoveRevisionAction());
        addAction(new RevertToRevisionAction());
        addAction(new PreviewRevisionAction());
        getSite().getActionBars().updateActionBars();
    }

    private void addAction(IAction action) {
        actions.add(action);
        if (action.getId() != null) {
            getSite().getActionBars().setGlobalActionHandler(action.getId(),
                    action);
        }
        if (action instanceof ISelectionChangedListener) {
            getSite().getSelectionProvider().addSelectionChangedListener(
                    (ISelectionChangedListener) action);
        }
    }

    private void setControl(Control control) {
        this.control = control;
    }

    private Control createTitleLabel(Composite parent) {
        titleLabel = new Label(parent, SWT.NONE);
        titleLabel.setText(getTitleText());
        return titleLabel;
    }

    private Control createViewer(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setContentProvider(new RevisionContentProvider());

        TableViewerColumn col0 = new TableViewerColumn(viewer, SWT.RIGHT);
        col0.getColumn().setText("#"); //$NON-NLS-1$
        col0.getColumn().setWidth(36);
        col0.setLabelProvider(new RevisionNumberLabelProvider());

        TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.LEFT);
        col1.getColumn().setText(MindMapMessages.RevisionsView_DateColumn_text);
        col1.getColumn().setWidth(120);
        col1.setLabelProvider(new RevisionDateLabelProvider());

        TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.LEFT);
        col2.getColumn().setText(MindMapMessages.RevisionsView_TimeColumn_text);
        col2.getColumn().setWidth(120);
        col2.setLabelProvider(new RevisionTimeLabelProvider());

        viewer.setInput(revisionManager);

        viewer.addOpenListener(new RevisionOpenListener());
        viewer.getTable().addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (SWTUtils.matchKey(e.stateMask, e.keyCode, 0, SWT.SPACE)) {
                    handleOpen(viewer.getSelection());
                }
            }
        });
        return viewer.getControl();
    }

    /**
     * 
     */
    private void registerCoreEvents() {
        if (revisionManager instanceof ICoreEventSource) {
            coreEventRegister.setNextSource((ICoreEventSource) revisionManager);
            coreEventRegister.register(Core.RevisionAdd);
            coreEventRegister.register(Core.RevisionRemove);
        }
        if (sheet instanceof ICoreEventSource) {
            coreEventRegister.setNextSource((ICoreEventSource) sheet);
            coreEventRegister.register(Core.TitleText);
            coreEventRegister.register(Core.RootTopic);
        }
        ITopic rootTopic = sheet.getRootTopic();
        if (rootTopic instanceof ICoreEventSource) {
            topicEventRegister.setNextSource((ICoreEventSource) rootTopic);
            topicEventRegister.register(Core.TitleText);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        for (IAction action : actions) {
            if (action instanceof ISelectionChangedListener) {
                getSite().getSelectionProvider()
                        .removeSelectionChangedListener(
                                (ISelectionChangedListener) action);
            }
        }
        actions.clear();
        topicEventRegister.unregisterAll();
        coreEventRegister.unregisterAll();
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return control;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private void viewRevision(IRevision revision) {
        List<IRevision> revisions = revisionManager.getRevisions();
        int index = revisions.indexOf(revision);
        RevisionPreviewDialog dialog = new RevisionPreviewDialog(getSite()
                .getShell(), sheet, revisions, index);
//        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
//            Table table = viewer.getTable();
//            Rectangle r = table.getItem(index).getBounds();
//            Point p = table.toDisplay(r.x, r.y);
//            final Rectangle sourceBounds = new Rectangle(p.x, p.y, r.width,
//                    r.height);
//            dialog.open(sourceBounds);
//        } else {
        dialog.open();
//        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventListener#handleCoreEvent(org.xmind.core
     * .event.CoreEvent)
     */
    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.RevisionAdd.equals(type) || Core.RevisionRemove.equals(type)) {
            asyncExec(new Runnable() {
                public void run() {
                    viewer.refresh();
                }
            });
        } else if (Core.TitleText.equals(type)) {
            asyncExec(new Runnable() {
                public void run() {
                    if (titleLabel != null && !titleLabel.isDisposed()) {
                        titleLabel.setText(getTitleText());
                    }
                }
            });
        } else if (Core.RootTopic.equals(type)) {
            topicEventRegister.unregisterAll();
            ITopic rootTopic = sheet.getRootTopic();
            if (rootTopic instanceof ICoreEventSource) {
                topicEventRegister.setNextSource((ICoreEventSource) rootTopic);
                topicEventRegister.register(Core.TitleText);
            }
        }
    }

    private void asyncExec(Runnable runnable) {
        getSite().getShell().getDisplay().asyncExec(runnable);
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ISelectionProvider.class)
            return viewer;
        return null;
    }

    /**
     * @param selection
     */
    private void handleOpen(ISelection selection) {
        if (selection.isEmpty())
            return;
        IRevision revision = (IRevision) ((IStructuredSelection) selection)
                .getFirstElement();
        viewRevision(revision);
    }

    private String getTitleText() {
        return String
                .format("%s (%s)", sheet.getTitleText(), sheet.getRootTopic().getTitleText()); //$NON-NLS-1$
    }

}
