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
package org.xmind.ui.internal.sharing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.sharing.ISharingService;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;

public class SharedMapsDropSupport implements DropTargetListener {

    private Control control;

    private DropTarget dropTarget;

    private Shell tooltip = null;

    private ISharingService sharingService;

    public SharedMapsDropSupport(Control control) {
        this.control = control;
        this.dropTarget = new DropTarget(control, DND.DROP_DEFAULT
                | DND.DROP_COPY | DND.DROP_LINK);
        this.dropTarget
                .setTransfer(new Transfer[] { FileTransfer.getInstance() });
        this.dropTarget.addDropListener(this);
    }

    public void setSharingService(ISharingService sharingService) {
        this.sharingService = sharingService;
    }

    public void dispose() {
        if (this.tooltip != null) {
            this.tooltip.dispose();
            this.tooltip = null;
        }
        if (this.dropTarget != null) {
            this.dropTarget.dispose();
            this.dropTarget = null;
        }
    }

    public void dragEnter(DropTargetEvent event) {
        if (sharingService == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        event.detail = DND.DROP_COPY;
        showToolTip(event.x, event.y);
    }

    public void dragLeave(DropTargetEvent event) {
        hideToolTip();
    }

    public void dragOperationChanged(DropTargetEvent event) {
        if (sharingService == null) {
            hideToolTip();
            event.detail = DND.DROP_NONE;
            return;
        }

        event.detail = DND.DROP_COPY;
        showToolTip(event.x, event.y);
    }

    public void dragOver(DropTargetEvent event) {
        showToolTip(event.x, event.y);
    }

    public void drop(DropTargetEvent event) {
        hideToolTip();
        if (sharingService == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        if (event.data == null) {
            event.detail = DND.DROP_NONE;
        } else {
            addSharedMaps(event.display == null ? Display.getCurrent()
                    : event.display, (String[]) event.data);
        }
    }

    public void dropAccept(DropTargetEvent event) {
        hideToolTip();
    }

    private void addSharedMaps(final Display display, final String[] filePaths) {
        final ISharingService sharingService = this.sharingService;
        if (sharingService == null)
            return;

        Job shareJob = new Job(SharingMessages.ShareLocalFilesJob_jobName) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(null, filePaths.length);
                final List<String> nonXMindFiles = new ArrayList<String>();
                final List<File> files = new ArrayList<File>();
                for (int i = 0; i < filePaths.length; i++) {
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    String path = filePaths[i];
                    monitor.subTask(path);
                    File file = new File(path);
                    if (file.getName().endsWith(MindMapUI.FILE_EXT_XMIND)) {
//                        library.addSharedMap(file);
                        files.add(file);
                    } else {
                        nonXMindFiles.add(path);
                    }
                    monitor.worked(1);
                }
                monitor.done();
                if (!nonXMindFiles.isEmpty()) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            if (nonXMindFiles.size() == 1) {
                                MessageDialog.openInformation(
                                        control.getShell(),
                                        SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                        NLS.bind(
                                                SharingMessages.ShareLocalFilesJob_DetectedSingleNonXMindFile_dialogMessage,
                                                nonXMindFiles.get(0)));
                            } else {
                                MessageDialog.openInformation(
                                        control.getShell(),
                                        SharingMessages.CommonDialogTitle_LocalNetworkSharing,
                                        SharingMessages.ShareLocalFilesJob_DetectedMultipleNonXMindFiles_dialogMessage);
                            }
                        }
                    });
                }

                final IWorkbench workbench = PlatformUI.getWorkbench();
                if (workbench != null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            File[] filesArr = files.toArray(new File[files
                                    .size()]);
                            IWorkbenchWindow window = workbench
                                    .getActiveWorkbenchWindow();
                            if (window != null) {
                                SharingUtils.addSharedMaps(window.getShell(),
                                        sharingService, filesArr);
                            }
                        }
                    });
                }

                return Status.OK_STATUS;
            }
        };
        shareJob.setRule(sharingService);
        sharingService.registerJob(shareJob);
        shareJob.schedule();
    }

    private void showToolTip(int x, int y) {
        if (tooltip == null || tooltip.isDisposed()) {
            tooltip = new Shell(control.getShell(), SWT.TOOL);
            tooltip.setBackground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_BLACK));
            tooltip.setAlpha(128);
            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = 5;
            layout.marginHeight = 5;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 0;
            tooltip.setLayout(layout);

            tooltip.setLayout(layout);
            Label label = new Label(tooltip, SWT.CENTER | SWT.WRAP);
            label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
            label.setText(SharingMessages.SharedMapsDropSupport_DropToShare_toolTip);
            label.setFont(FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT,
                    14));
            label.setBackground(label.getParent().getBackground());
            label.setForeground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_WHITE));
            tooltip.pack(true);
            tooltip.setVisible(true);
            Listener motionListener = new Listener() {
                public void handleEvent(Event event) {
                    hideToolTip();
                }
            };
            tooltip.addListener(SWT.MouseEnter, motionListener);
            tooltip.addListener(SWT.MouseExit, motionListener);
            tooltip.addListener(SWT.MouseMove, motionListener);
        }
        Point size = tooltip.getSize();
        tooltip.setLocation(x - size.x / 2, y - size.y - 20);
    }

    private void hideToolTip() {
        if (tooltip != null) {
            tooltip.dispose();
            tooltip = null;
        }
    }
}