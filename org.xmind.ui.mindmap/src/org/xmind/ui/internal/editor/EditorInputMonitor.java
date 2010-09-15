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
package org.xmind.ui.internal.editor;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;

/**
 * @author frankshaka
 * 
 */
public class EditorInputMonitor implements ShellListener, IPropertyListener {

    private Shell shell;

    private MindMapEditor editor;

    private Boolean lastExisting = null;

    /**
     * 
     */
    public EditorInputMonitor(MindMapEditor editor) {
        this.editor = editor;
        this.shell = editor.getSite().getShell();
        if (shell != null) {
            shell.addShellListener(this);
        }
    }

    public void dispose() {
        if (shell != null) {
            shell.removeShellListener(this);
            shell = null;
        }
    }

    /**
     * 
     */
    private void checkFiles() {
        if (lastExisting == null) {
            recordLastExisting();
        } else {
            IEditorInput input = editor.getEditorInput();
            if (input != null) {
                if (input.exists() != lastExisting.booleanValue()) {
                    addDirtyMarker();
                }
            }
        }
    }

    private void addDirtyMarker() {
        IWorkbook workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);
        if (workbook instanceof ICoreEventSource2) {
            ((ICoreEventSource2) workbook).registerOnceCoreEventListener(
                    Core.WorkbookPreSaveOnce, ICoreEventListener.NULL);
        }
    }

    /**
     * 
     */
    private void recordLastExisting() {
        IEditorInput input = editor.getEditorInput();
        lastExisting = Boolean.valueOf(input == null ? false : input.exists());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events
     * .ShellEvent)
     */
    public void shellActivated(ShellEvent e) {
        checkFiles();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.ShellListener#shellClosed(org.eclipse.swt.events
     * .ShellEvent)
     */
    public void shellClosed(ShellEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt
     * .events.ShellEvent)
     */
    public void shellDeactivated(ShellEvent e) {
        recordLastExisting();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.ShellListener#shellDeiconified(org.eclipse.swt
     * .events.ShellEvent)
     */
    public void shellDeiconified(ShellEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.ShellListener#shellIconified(org.eclipse.swt.events
     * .ShellEvent)
     */
    public void shellIconified(ShellEvent e) {
    }

    public void propertyChanged(Object source, int propId) {
        if (propId == IEditorPart.PROP_INPUT) {
            recordLastExisting();
        }
    }

}
