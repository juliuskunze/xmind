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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionFactory;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.print.PageSetupDialog;
import org.xmind.ui.internal.print.PrintClient;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;

public class PrintMapAction extends PageAction {

    public PrintMapAction(IGraphicalEditorPage page) {
        super(ActionFactory.PRINT.getId(), page);
    }

    public void run() {
        IGraphicalEditor editor = getEditor();
        if (editor == null)
            return;

        IGraphicalEditorPage page = getPage();
        if (page == null)
            return;

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null || !(viewer instanceof IMindMapViewer))
            return;

        IMindMapViewer mmv = (IMindMapViewer) viewer;
        IMindMap mindMap = mmv.getMindMap();
        if (mindMap == null)
            return;

        Shell parentShell = editor.getSite().getShell();
        if (parentShell == null || parentShell.isDisposed())
            return;

        PageSetupDialog pageSetupDialog = new PageSetupDialog(parentShell,
                editor, page, mmv, mindMap);

        while (true) {
            int open = pageSetupDialog.open();
            if (open == PageSetupDialog.CANCEL)
                return;

            PrintDialog printDialog = new PrintDialog(parentShell);
            PrinterData printerData = printDialog.open();
            if (printerData != null) {
                print(printerData, pageSetupDialog.getSettings(), mindMap,
                        parentShell);
                return;
            }
        }
    }

    private void print(PrinterData printerData, IDialogSettings settings,
            final IMindMap source, Shell parentShell) {
        final PrintClient client = new PrintClient(source.getCentralTopic()
                .getTitleText(), parentShell, printerData, settings);
        Display display = parentShell.getDisplay();
        try {
            BusyIndicator.showWhile(display, new Runnable() {
                public void run() {
                    client.print(source);
                }
            });
        } finally {
            client.dispose();
        }
    }
}