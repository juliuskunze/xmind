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
package org.xmind.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.util.ImageFormat;

public class DialogUtils {

    private DialogUtils() {
    }

    public static void makeDefaultImageSelectorDialog(FileDialog dialog,
            boolean withAllFileFilter) {
        makeImageSelectorDialog(dialog, withAllFileFilter, ImageFormat.values());
    }

    public static void makeImageSelectorDialog(FileDialog dialog,
            boolean withAllFileFilter, ImageFormat... imageFormats) {
        Collection<String> extensions = new ArrayList<String>();
        Collection<String> names = new ArrayList<String>();
        if (withAllFileFilter) {
            extensions.add("*.*"); //$NON-NLS-1$
            names.add(NLS.bind("{0} (*.*)", //$NON-NLS-1$
                    DialogMessages.AllFilesFilterName));
        }
        for (ImageFormat format : imageFormats) {
            List<String> exts = format.getExtensions();
            if (!exts.isEmpty()) {
                StringBuilder extBuilder = new StringBuilder(exts.size() * 5);
                StringBuilder extDescBuilder = new StringBuilder(
                        exts.size() * 5);
                for (String ext : exts) {
                    String pattern = "*" + ext; //$NON-NLS-1$
                    if (extBuilder.length() > 0)
                        extBuilder.append(";"); //$NON-NLS-1$
                    extBuilder.append(pattern);
                    if (extDescBuilder.length() > 0)
                        extDescBuilder.append(", "); //$NON-NLS-1$
                    extDescBuilder.append(pattern);
                }
                extensions.add(extBuilder.toString());
                names.add(NLS.bind("{0} ({1})", //$NON-NLS-1$ 
                        format.getDescription(), extDescBuilder.toString()));
            }
        }
        dialog.setFilterExtensions(extensions.toArray(new String[extensions
                .size()]));
        dialog.setFilterNames(names.toArray(new String[names.size()]));
    }

    public static boolean confirmOverwrite(Shell shell, String filePath) {
        return MessageDialog.openConfirm(shell,
                DialogMessages.ConfirmOverwrite_title, NLS.bind(
                        DialogMessages.ConfirmOverwrite_message, filePath));
    }

    public static boolean confirmRestart(Shell shell) {
        return new MessageDialog(null, DialogMessages.ConfirmRestart_title,
                null, DialogMessages.ConfirmRestart_message,
                MessageDialog.QUESTION, new String[] {
                        DialogMessages.ConfirmRestart_Restart,
                        DialogMessages.ConfirmRestart_Continue }, 1).open() == MessageDialog.OK;
    }
}