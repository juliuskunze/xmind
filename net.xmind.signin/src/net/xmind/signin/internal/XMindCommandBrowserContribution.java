package net.xmind.signin.internal;

import org.eclipse.jface.action.IContributionManager;
import org.xmind.ui.browser.IBrowserViewer;
import org.xmind.ui.browser.IBrowserViewerContribution;
import org.xmind.ui.browser.IBrowserViewerContribution2;

public class XMindCommandBrowserContribution implements
        IBrowserViewerContribution, IBrowserViewerContribution2 {

    public void fillToolBar(IBrowserViewer viewer, IContributionManager toolBar) {
    }

    public void installBrowserListeners(IBrowserViewer viewer) {
        viewer.addPropertyChangeListener(UserInfoManager.getDefault()
                .getXMindCommandListener());
    }

    public void uninstallBrowserListeners(IBrowserViewer viewer) {
        viewer.removePropertyChangeListener(UserInfoManager.getDefault()
                .getXMindCommandListener());
    }

}
