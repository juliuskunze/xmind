/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
package net.xmind.share.actions;

import net.xmind.share.Uploader;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.IMindMapViewer;


/**
 * @author briansun
 * 
 */
public class UploadAction implements IEditorActionDelegate {

    private IEditorPart editor;

    private IMindMapViewer currentViewer;

    /**
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = targetEditor;
        currentViewer = findCurrentViewer();
        action.setEnabled(currentViewer != null);
    }

    private IMindMapViewer findCurrentViewer() {
        if (editor instanceof IGraphicalEditor) {
            IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                    .getActivePageInstance();
            if (page != null) {
                IGraphicalViewer viewer = page.getViewer();
                if (viewer instanceof IMindMapViewer)
                    return (IMindMapViewer) viewer;
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (currentViewer == null)
            return;

        Control control = currentViewer.getControl();
        if (control == null || control.isDisposed())
            return;

        new Uploader(control.getShell(), currentViewer).upload();

//        MindMapExtractor extractor = new MindMapExtractor(currentViewer);
//        IWorkbook workbook = extractor.extract();
//        MindMapPreviewBuilder previewBuilder = new MindMapPreviewBuilder(
//                workbook);
//        Shell shell = currentViewer.getControl().getShell();
//        try {
//            previewBuilder.save(shell);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Point origin = previewBuilder.getOrigin();
//        Image fullImage = getPreviewImage(workbook, shell.getDisplay());
//        if (fullImage != null) {
//            savePreviewData(workbook, fullImage, origin, shell);
//            fullImage.dispose();
//        }

    }

//    private void savePreviewData(IWorkbook workbook, Image fullImage,
//            Point origin, Shell shell) {
//        UploadDialog dialog = new UploadDialog(shell);
//        dialog.setFullImage(fullImage, origin.x, origin.y);
//        dialog.open();
//
//    }
//
//    private Image getPreviewImage(IWorkbook workbook, Display display) {
//        IFileEntry entry = workbook.getManifest().getFileEntry(
//                MindMapPreviewBuilder.PATH_THUMBNAIL);
//        if (entry != null) {
//            InputStream in = entry.getInputStream();
//            if (in != null) {
//                try {
//                    return new Image(display, in);
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        in.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return null;
//    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        currentViewer = findCurrentViewer();
        action.setEnabled(currentViewer != null);
    }

}