package net.xmind.workbench.internal.actions;

import net.xmind.signin.internal.Messages;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class UpgradeActionDelegate extends XMindNetActionDelegate implements
        IWorkbenchWindowActionDelegate, IActionDelegate2 {

    private IWorkbenchWindow window;

    private IAction action;

    public void dispose() {
        this.window = null;
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
        setURL("http://www.xmind.net/pro/buy/"); //$NON-NLS-1$
        update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action) {
        this.action = action;
        update();
    }

    /**
     * 
     */
    private void update() {
        if (action != null) {
            action.setText(Messages.Renew_text);
            action.setText(Messages.Renew_toolTip);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action
     * .IAction, org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    public void run(IAction action) {
        if (window == null)
            return;

        UpgradeActionDelegate.super.gotoURL();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

}
