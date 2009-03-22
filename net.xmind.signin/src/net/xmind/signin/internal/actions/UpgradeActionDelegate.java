package net.xmind.signin.internal.actions;

import net.xmind.signin.IVerifyListener;
import net.xmind.signin.XMindNetEntry;
import net.xmind.signin.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class UpgradeActionDelegate extends XMindNetActionDelegate implements
        IWorkbenchWindowActionDelegate, IActionDelegate2 {

    private static IVerifyListener verifyListener = null;

    private static boolean isUpgrading = false;

    private IWorkbenchWindow window;

    private IAction action;

    private boolean isPro;

    public void dispose() {
        this.window = null;
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
        this.isPro = Platform.getBundle("org.xmind.meggy") != null; //$NON-NLS-1$
        installVerifyListener();
        setURL("http://www.xmind.net/pro/buy/"); //$NON-NLS-1$
        update();
    }

    /**
     * 
     */
    private void installVerifyListener() {
        if (isPro)
            return;
        if (verifyListener == null) {
            verifyListener = new IVerifyListener() {
                public void notifyValidity(IStatus validity) {
                    int code = validity.getCode();
                    if (code == VALID) {
                        doUpgrade(PlatformUI.getWorkbench().getDisplay());
                    } else if (isUpgrading) {
                        if (code == NOT_SUBSCRIBED || code == EXPIRED) {
                            gotoBuyPage(PlatformUI.getWorkbench().getDisplay());
                        }
                    }
                    isUpgrading = false;
                }
            };
            XMindNetEntry.addVerifyListener(verifyListener);
        }
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
            if (isPro) {
                action.setText(Messages.Renew_text);
                action.setText(Messages.Renew_toolTip);
            } else {
                action.setText(Messages.Upgrade_text);
                action.setToolTipText(Messages.Upgrade_toolTip);
            }
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
        if (isPro) {
            renew();
        } else {
            upgrade();
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

    /**
     * 
     */
    private void upgrade() {
        if (isUpgrading || UpgradeRunnable.isRunning())
            return;

        isUpgrading = true;
        if (XMindNetEntry.hasSignedIn()) {
            //final Properties userInfo = XMindNetEntry.getCurrentUserInfo();
            Job verifyJob = new Job(Messages.Upgrade_jobName) {
                protected IStatus run(IProgressMonitor monitor) {
                    while (isUpgrading) {
                        if (monitor.isCanceled())
                            return Status.CANCEL_STATUS;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    return Status.OK_STATUS;
                }
            };
            verifyJob.schedule();
            XMindNetEntry.verify();
        } else {
            XMindNetEntry.signIn();
        }
    }

    private void doUpgrade(final Display display) {
        if (UpgradeRunnable.isRunning())
            return;

        display.asyncExec(new UpgradeRunnable());
    }

    private void gotoBuyPage(final Display display) {
        display.asyncExec(new Runnable() {
            public void run() {
                UpgradeActionDelegate.super.gotoURL();
            }
        });
    }

    /**
     * 
     */
    private void renew() {
        gotoBuyPage(PlatformUI.getWorkbench().getDisplay());
    }

}
