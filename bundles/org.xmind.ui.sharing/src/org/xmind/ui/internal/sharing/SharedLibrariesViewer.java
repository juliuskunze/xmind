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
package org.xmind.ui.internal.sharing;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.xmind.core.internal.sharing.LocalNetworkSharing;
import org.xmind.core.sharing.IContactManager;
import org.xmind.core.sharing.ISharedLibrary;
import org.xmind.core.sharing.ISharingService;
import org.xmind.core.sharing.SharingConstants;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.CategorizedGalleryViewer;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class SharedLibrariesViewer extends CategorizedGalleryViewer {

    private Map<Object, Control> warningWidgets = new HashMap<Object, Control>();

    private Map<Object, Control> connectWidgets = new HashMap<Object, Control>();

    private Map<Object, Control> previousVersionTipWidgets = new HashMap<Object, Control>();

    private SharedMapsDropSupport dropSupport = null;

    public SharedLibrariesViewer() {
        setContentProvider(new SharedMapsContentProvider());
        setLabelProvider(new SharedMapLabelProvider());

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        setEditDomain(editDomain);

        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout(
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_FILL, 1, 1,
                new Insets(5)));
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.FrameContentSize, new Dimension(100, 60));
        properties.set(GalleryViewer.PackFrameContent, Boolean.TRUE);
        properties
                .set(GalleryViewer.TitlePlacement, GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
    }

    @Override
    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        if (dropSupport != null) {
            if (input instanceof ISharingService) {
                dropSupport.setSharingService((ISharingService) input);
            } else {
                dropSupport.setSharingService(null);
            }
        }
    }

    @Override
    protected void hookControl(Control control) {
        super.hookControl(control);
        dropSupport = new SharedMapsDropSupport(control);
    }

    @Override
    protected void handleDispose(DisposeEvent event) {
        super.handleDispose(event);
        if (dropSupport != null) {
            dropSupport.dispose();
            dropSupport = null;
        }
    }

    @Override
    protected Control createSectionContent(Composite parent,
            final Object category) {
        Composite wrap = (Composite) super.createSectionContent(parent,
                category);

        createWarningWidget(wrap, category);
        createConnectWidget(wrap, category);
        createPreviousVersionTipWidget(wrap, category);

        return wrap;
    }

    @Override
    protected void refreshSectionContent(Control content, Object category,
            Object element) {
        super.refreshSectionContent(content, category, element);
        if (element == null)
            refreshSectionContent(category);
    }

    private void refreshSectionContent(Object category) {
        boolean hasMaps = ((ITreeContentProvider) getContentProvider())
                .hasChildren(category);
        boolean isPreviousVersion = isPreviousVersion(category);
        boolean showConnectButton = showConnectButton(category);

        boolean showViewerControl = !isPreviousVersion && !showConnectButton
                && hasMaps;
        boolean showWarningWidget = !isPreviousVersion && !showConnectButton
                && !hasMaps;
        boolean showConnectWidget = !isPreviousVersion && showConnectButton;

        showWidgets(category, showViewerControl, showWarningWidget,
                showConnectWidget, isPreviousVersion);
    }

    private void showWidgets(Object category, boolean showViewerControl,
            boolean showWaringWidget, boolean showConnectWidget,
            boolean showPreviousTipWidget) {
        GalleryViewer viewer = getNestedViewer(category);
        Control viewerControl = viewer == null ? null : viewer.getControl();
        showWidget(viewerControl, showViewerControl);

        Control warningWidget = warningWidgets.get(category);
        showWidget(warningWidget, showWaringWidget);

        Control connectWidget = connectWidgets.get(category);
        showWidget(connectWidget, showConnectWidget);

        Control tipWidget = previousVersionTipWidgets.get(category);
        showWidget(tipWidget, showPreviousTipWidget);
    }

    private void showWidget(Control control, boolean visible) {
        if (control != null && !control.isDisposed()) {
            control.setVisible(visible);
            ((GridData) control.getLayoutData()).exclude = !visible;
        }
    }

    private boolean showConnectButton(Object category) {
        IPreferenceStore prefStore = LocalNetworkSharingUI.getDefault()
                .getPreferenceStore();
        String arrangeMode = prefStore
                .getString(SharingConstants.PREF_ARRANGE_MODE);
        if (SharingConstants.ARRANGE_MODE_PEOPLE.equals(arrangeMode)
                || IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(arrangeMode)) {
            if (category instanceof ISharedLibrary) {
                ISharedLibrary library = (ISharedLibrary) category;
                if (library.isLocal())
                    return false;

                String contactID = library.getContactID();
                if (contactID == null || "".equals(contactID)) //$NON-NLS-1$
                    return false;

                IContactManager contactManager = LocalNetworkSharing
                        .getDefault().getSharingService().getContactManager();

                if (!contactManager.isContact(contactID))
                    return true;
            }
        }
        return false;
    }

    private boolean isPreviousVersion(Object category) {
        IPreferenceStore prefStore = LocalNetworkSharingUI.getDefault()
                .getPreferenceStore();
        String arrangeMode = prefStore
                .getString(SharingConstants.PREF_ARRANGE_MODE);
        if (SharingConstants.ARRANGE_MODE_PEOPLE.equals(arrangeMode)
                || IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(arrangeMode)) {
            if (category instanceof ISharedLibrary) {
                ISharedLibrary library = (ISharedLibrary) category;
                if (library.isLocal())
                    return false;

                String contactId = library.getContactID();
                if (contactId == null)
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void disposeSectionContent(Composite parent, Object category) {
        super.disposeSectionContent(parent, category);
        Control warningWidget = warningWidgets.remove(category);
        if (warningWidget != null && !warningWidget.isDisposed()) {
            if (warningWidget instanceof Composite) {
                Control[] children = ((Composite) warningWidget).getChildren();
                for (int i = 0; i < children.length; i++) {
                    children[i].setMenu(null);
                }
            }
            warningWidget.setMenu(null);
            warningWidget.dispose();
        }

        Control connectWidget = connectWidgets.remove(category);
        if (connectWidget != null && !connectWidget.isDisposed()) {
            if (connectWidget instanceof Composite) {
                Control[] childer = ((Composite) connectWidget).getChildren();
                for (int i = 0; i < childer.length; i++) {
                    childer[i].setMenu(null);
                }
            }
            connectWidget.setMenu(null);
            connectWidget.dispose();
        }

        Control tipWidget = previousVersionTipWidgets.remove(category);
        if (tipWidget != null && !tipWidget.isDisposed()) {
            if (tipWidget instanceof Composite) {
                Control[] children = ((Composite) tipWidget).getChildren();
                for (int i = 0; i < children.length; i++) {
                    children[i].setMenu(null);
                }
            }
            tipWidget.setMenu(null);
            tipWidget.dispose();
        }
    }

    @Override
    public void setFocus() {
        for (Object category : getCategories()) {
            GalleryViewer viewer = getNestedViewer(category);
            if (viewer != null && viewer.getControl() != null
                    && !viewer.getControl().isDisposed()
                    && viewer.getControl().isVisible()) {
                if (viewer.setFocus())
                    return;
            }
        }
        super.setFocus();
    }

    @Override
    public void setSelection(ISelection selection) {
        super.setSelection(selection, true);
    }

    private ISharedLibrary getRemoteLibrary(Object category) {
        if (category != null && category instanceof ISharedLibrary)
            return (ISharedLibrary) category;
        return null;
    }

    private void createWarningWidget(Composite parent, Object category) {
        Composite warningWidget = getWidgetFactory().createComposite(parent,
                SWT.WRAP);
        GridLayout warningLayout = new GridLayout(1, false);
        warningLayout.marginWidth = 0;
        warningLayout.marginHeight = 0;
        warningLayout.verticalSpacing = 0;
        warningLayout.horizontalSpacing = 0;
        warningLayout.marginBottom = 10;
        warningWidget.setLayout(warningLayout);
        warningWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        Label warningLabel = getWidgetFactory()
                .createLabel(
                        warningWidget,
                        SharingMessages.SharedLibrariesViewer_LibrarySection_NoSharedMaps_warningText,
                        SWT.WRAP);
        warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        warningLabel.setForeground(Display.getCurrent().getSystemColor(
                SWT.COLOR_GRAY));

        warningWidgets.put(category, warningWidget);
    }

    private void createConnectWidget(Composite parent, final Object category) {
        Composite connectWidget = getWidgetFactory().createComposite(parent,
                SWT.NONE);
        GridLayout connectLayout = new GridLayout(1, false);
        connectLayout.marginWidth = 0;
        connectLayout.marginHeight = 0;
        connectLayout.verticalSpacing = 0;
        connectLayout.horizontalSpacing = 0;
        connectWidget.setLayout(connectLayout);
        GridData connectData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        connectWidget.setLayoutData(connectData);

        final Button connectButton = new Button(connectWidget, SWT.PUSH);
        connectButton.setText(SharingMessages.SharedLibrary_ConnectButton_text);
        connectWidget.setVisible(false);
        connectData.exclude = true;
        connectButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (!SharingUtils
                        .connectRemoteLibrary(getRemoteLibrary(category)))
                    return;

                connectButton.setEnabled(false);
                connectButton
                        .setText(SharingMessages.SharedLibrary_ConnectingButton_text);
                connectButton.getParent().layout();
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(120000);//set disable 120s.
                        } catch (InterruptedException e) {
                        } finally {
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                    if (connectButton != null
                                            && !connectButton.isDisposed()) {
                                        connectButton.setEnabled(true);
                                        connectButton
                                                .setText(SharingMessages.SharedLibrary_ConnectButton_text);
                                        connectButton.getParent().layout();
                                    }
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        });

        connectWidgets.put(category, connectWidget);
    }

    private void createPreviousVersionTipWidget(Composite parent,
            Object category) {
        Composite tipWidget = getWidgetFactory().createComposite(parent,
                SWT.WRAP);
        GridLayout tipLayout = new GridLayout(2, false);
        tipLayout.marginWidth = 0;
        tipLayout.marginHeight = 0;
        tipLayout.verticalSpacing = 0;
        tipLayout.horizontalSpacing = 0;
        tipLayout.marginBottom = 10;
        tipWidget.setLayout(tipLayout);
        tipWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label imageLabel = new Label(tipWidget, SWT.NONE);
        imageLabel
                .setLayoutData(new GridData(SWT.CENTER, SWT.LEFT, false, true));
        imageLabel.setImage(getImage(parent));
        imageLabel.setBackground(tipWidget.getBackground());

        Label tipLabel = getWidgetFactory().createLabel(tipWidget,
                SharingMessages.SharedLibrariesViewer_previousVersionTipText, SWT.WRAP);
        tipLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        tipLabel.setForeground(Display.getCurrent().getSystemColor(
                SWT.COLOR_GRAY));

        previousVersionTipWidgets.put(category, tipWidget);
    }

    private Image getImage(Composite parent) {
        URL url = Platform.getBundle(LocalNetworkSharingUI.PLUGIN_ID).getEntry(
                "icons/info.png"); //$NON-NLS-1$
        if (url != null) {
            try {
                return new Image(null, url.openStream());
            } catch (IOException e) {
            }
        }
        return parent.getDisplay().getSystemImage(SWT.ICON_INFORMATION);
    }

}
