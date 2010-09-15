package org.xmind.ui.internal.actions;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;

public class OpenHomeMapAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public OpenHomeMapAction(IWorkbenchWindow window) {
        super(MindMapMessages.OpenHomeMap_text);
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
        setId("org.xmind.ui.openHomeMap"); //$NON-NLS-1$
        setToolTipText(MindMapMessages.OpenHomeMap_toolTip);
        setActionDefinitionId("org.xmind.ui.command.openHomeMap"); //$NON-NLS-1$
    }

    @Override
    public void run() {
        if (window == null)
            return;

        final String path = MindMapUIPlugin.getDefault().getPreferenceStore()
                .getString(PrefConstants.HOME_MAP_LOCATION);
        if (path == null || "".equals(path)) {//$NON-NLS-1$
            PreferencesUtil.createPreferenceDialogOn(window.getShell(),
                    "org.xmind.ui.GeneralPrefPage", null, null).open(); //$NON-NLS-1$
            return;
        }
        String errMessage = NLS.bind(
                DialogMessages.FailedToLoadWorkbook_message, path);
        SafeRunner.run(new SafeRunnable(errMessage) {
            public void run() throws Exception {
                IEditorInput input = MME.createFileEditorInput(path);
                window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }

    public void dispose() {
        window = null;
    }

}
