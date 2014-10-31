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
package net.xmind.share.dialog;

import net.xmind.share.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class ThumbnailUploaderPage extends UploaderPage {

    private CutPreviewViewer viewer;

    public ThumbnailUploaderPage() {
        setTitle(Messages.UploaderDialog_ThumbnailPage_title);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 20;
        layout.marginTop += 5;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.WRAP);
        label.setText(Messages.UploaderDialog_Thumbnail_description);
        GridData labelData = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(labelData);

        if (viewer == null) {
            viewer = new CutPreviewViewer();
            viewer.setInfo(getInfo());
        }
        viewer.createControl(composite);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.getControl().addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    getShell().traverse(e.detail);
                }
            }
        });

        labelData.widthHint = viewer.getControl().computeSize(SWT.DEFAULT,
                SWT.DEFAULT).x;

        setControl(composite);
    }

    public void setFocus() {
        if (viewer != null && !viewer.getControl().isDisposed())
            viewer.setFocus();
    }
}