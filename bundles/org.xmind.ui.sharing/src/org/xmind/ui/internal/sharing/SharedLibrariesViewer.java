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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.xmind.core.sharing.ISharingService;
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

    protected void hookControl(Control control) {
        super.hookControl(control);
        dropSupport = new SharedMapsDropSupport(control);
    }

    protected void handleDispose(DisposeEvent event) {
        super.handleDispose(event);
        if (dropSupport != null) {
            dropSupport.dispose();
            dropSupport = null;
        }
    }

    protected Control createSectionContent(Composite parent, Object category) {
        Composite wrap = (Composite) super.createSectionContent(parent,
                category);

        Composite warningWidget = getWidgetFactory().createComposite(wrap,
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

        Label warningLabel = getWidgetFactory().createLabel(warningWidget,
                SharingMessages.SharedLibrariesViewer_LibrarySection_NoSharedMaps_warningText,
                SWT.WRAP);
        warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        warningLabel.setForeground(Display.getCurrent().getSystemColor(
                SWT.COLOR_GRAY));

        warningWidgets.put(category, warningWidget);

        return wrap;
    }

    protected void refreshSectionContent(Control content, Object category,
            Object element) {
        super.refreshSectionContent(content, category, element);
        if (element == null) {
            boolean hasMaps = ((ITreeContentProvider) getContentProvider())
                    .hasChildren(category);
            GalleryViewer viewer = getNestedViewer(category);
            Control viewerControl = viewer == null ? null : viewer.getControl();
            Control warningWidget = warningWidgets.get(category);
            if (viewerControl != null && !viewerControl.isDisposed()) {
                viewerControl.setVisible(hasMaps);
                ((GridData) viewerControl.getLayoutData()).exclude = !hasMaps;
            }
            if (warningWidget != null && !warningWidget.isDisposed()) {
                warningWidget.setVisible(!hasMaps);
                ((GridData) warningWidget.getLayoutData()).exclude = hasMaps;
            }
        }
    }

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
    }

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

    public void setSelection(ISelection selection) {
        super.setSelection(selection, true);
    }

}
