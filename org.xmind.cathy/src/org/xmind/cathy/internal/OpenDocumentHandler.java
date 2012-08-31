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

package org.xmind.cathy.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.xmind.cathy.internal.jobs.OpenFilesJob;

/**
 * @author Frank Shaka
 * 
 */
public class OpenDocumentHandler implements Listener {

    private List<String> filesToOpen = new ArrayList<String>(1);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(Event event) {
        String filepath = event.text;
        File file = new File(filepath);
        try {
            filepath = file.getCanonicalPath();
        } catch (Exception e) {
            filepath = file.getAbsolutePath();
        }
        filesToOpen.add(filepath);
    }

    public void checkAndOpenFiles(IWorkbench workbench) {
        if (filesToOpen.isEmpty())
            return;

        List<String> files = new ArrayList<String>(filesToOpen);
        filesToOpen.clear();
        OpenFilesJob job = new OpenFilesJob(workbench,
                WorkbenchMessages.CheckOpenFilesJob_CheckFiles_name, files);
        job.schedule();
    }

}
