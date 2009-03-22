/**
 * 
 */
package org.xmind.ui.internal.actions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.xmind.core.IFileEntry;
import org.xmind.core.ITopic;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author frankshaka
 * 
 */
public class SaveAttachmentAsAction extends PageAction implements
        ISelectionAction {

    private ITopic topic = null;

    /**
     * 
     */
    public SaveAttachmentAsAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.SAVE_ATTACHMENT_AS.getId(), page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (isDisposed())
            return;

        if (topic == null)
            return;

        final ITopic sourceTopic = this.topic;
        String url = sourceTopic.getHyperlink();
        if (url == null || !HyperlinkUtils.isAttachmentURL(url))
            return;

        final String entryPath = HyperlinkUtils.toAttachmentPath(url);
        final IFileEntry entry = sourceTopic.getOwnedWorkbook().getManifest()
                .getFileEntry(entryPath);
        if (entry == null)
            return;

        final InputStream is = entry.getInputStream();
        if (is == null)
            return;

        try {
            String ext = FileUtils.getExtension(entryPath);
            FileDialog dialog = new FileDialog(getPage().getControl()
                    .getShell(), SWT.SAVE);
            dialog.setFilterExtensions(new String[] { "*.*" }); //$NON-NLS-1$
            dialog.setFilterNames(new String[] { NLS.bind("{0} (*.*)", //$NON-NLS-1$
                    DialogMessages.AllFilesFilterName) });
            String name = sourceTopic.getTitleText();
            if (name != null && !name.endsWith(ext)) {
                name += ext;
            }
            if (name != null) {
                dialog.setFileName(name);
            }
            final String targetPath = dialog.open();
            if (targetPath == null)
                return;

            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    FileOutputStream os = new FileOutputStream(targetPath);
                    FileUtils.transfer(is, os, true);
                }
            });
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.ui.actions.ISelectionAction#setSelection(org.eclipse.jface
     * .viewers.ISelection)
     */
    public void setSelection(ISelection selection) {
        this.topic = getTopic(selection);
        setEnabled(MindMapUtils.isSingleTopic(selection)
                && hasAttachment(selection));

    }

    private ITopic getTopic(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object o = ((IStructuredSelection) selection).getFirstElement();
            if (o instanceof ITopic)
                return (ITopic) o;
        }
        return null;
    }

    /**
     * @param selection
     * @return
     */
    private boolean hasAttachment(ISelection selection) {
        ITopic topic = (ITopic) ((IStructuredSelection) selection)
                .getFirstElement();
        if (topic == null)
            return false;
        String url = topic.getHyperlink();
        if (url == null)
            return false;
        return HyperlinkUtils.isAttachmentURL(url);
    }

}
