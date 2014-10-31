package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.internal.wizards.WorkbookImportWizard;

public class ImportFromWorkbookHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IWorkbench workbench = PlatformUI.getWorkbench();
                ISelection selection;
                ISelectionService ss = (ISelectionService) workbench
                        .getService(ISelectionService.class);
                if (ss != null) {
                    selection = ss.getSelection();
                    if (!(selection instanceof IStructuredSelection))
                        selection = StructuredSelection.EMPTY;
                } else {
                    selection = StructuredSelection.EMPTY;
                }

                WorkbookImportWizard wizard = new WorkbookImportWizard();
                wizard.init(workbench, (IStructuredSelection) selection);

                WizardDialog dialog = new WizardDialog(Display.getCurrent()
                        .getActiveShell(), wizard);
                dialog.open();
            }

        });

        return null;
    }

}
