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
package org.xmind.ui.internal.browser;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.browser.IBrowserViewer;
import org.xmind.ui.browser.IBrowserViewerContainer;

public class InternalBrowserView extends ViewPart implements
        IBrowserViewerContainer {

    private class OpenInExternalAction extends Action {
        /**
         * 
         */
        public OpenInExternalAction() {
            super(BrowserMessages.BrowserView_OpenInExternalBrowser_text,
                    BrowserImages.getImageDescriptor(BrowserImages.BROWSER));
            setToolTipText(BrowserMessages.BrowserView_OpenInExternalBrowser_toolTip);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            if (viewer == null || viewer.getControl() == null
                    || viewer.getControl().isDisposed())
                return;

            IBrowser browser = BrowserSupport.getInstance().createBrowser(
                    IBrowserSupport.AS_EXTERNAL);
            try {
                browser.openURL(viewer.getURL());
            } catch (PartInitException e) {
                BrowserPlugin.log(e);
            }
        }
    }

    public static final String BROWSER_VIEW_ID = "org.xmind.ui.BrowserView"; //$NON-NLS-1$

    //private static Map<String, Integer> numbers = new HashMap<String, Integer>();

    private BrowserViewer viewer;

    private String clientId;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void createPartControl(Composite parent) {
        initActions();
        viewer = new BrowserViewer(parent, SWT.NONE, this);
    }

    /**
     * 
     */
    private void initActions() {
        IActionBars actionBars = getViewSite().getActionBars();

        OpenInExternalAction openInExternalAction = new OpenInExternalAction();

        IMenuManager menu = actionBars.getMenuManager();
        menu.add(openInExternalAction);
        menu.add(new Separator());
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        IToolBarManager toolBar = actionBars.getToolBarManager();
        toolBar.add(openInExternalAction);
        toolBar.add(new Separator());
        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    public void setFocus() {
        viewer.setFocus();
    }

    public void openURL(String url) {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.setURL(url);
        }
    }

    public BrowserViewer getViewer() {
        return viewer;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IBrowserViewer.class)
            return viewer;
        if (adapter == IBrowserViewerContainer.class)
            return this;
        return super.getAdapter(adapter);
    }

    public boolean close() {
        try {
            getSite().getPage().hideView(this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public IActionBars getActionBars() {
        return getViewSite().getActionBars();
    }

    public String getClientId() {
        return clientId;
    }

    public void openInExternalBrowser(String url) {
        BrowserUtil.gotoUrl(url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.browser.IBrowserViewerContainer#openNewBrowser()
     */
    public Browser openNewBrowser() {
        final Browser[] ret = new Browser[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IBrowser browser = BrowserSupport.getInstance().createBrowser(
                        IBrowserSupport.AS_EDITOR, getClientId());
                browser.openURL(""); //$NON-NLS-1$
                if (browser instanceof InternalBrowser) {
                    IWorkbenchPart part = ((InternalBrowser) browser).getPart();
                    if (part instanceof InternalBrowserEditor) {
                        ret[0] = ((InternalBrowserEditor) part).getViewer()
                                .getBrowser();
                    }
                }
            }
        });
        return ret[0];
    }

//    private String getNewSecondaryId() {
//        Integer num = numbers.get(getClientId());
//        if (num == null) {
//            num = Integer.valueOf(1);
//        } else {
//            num = Integer.valueOf(num.intValue() + 1);
//        }
//        numbers.put(getClientId(), num);
//        return getClientId() + "-" + num.toString(); //$NON-NLS-1$
//    }

}