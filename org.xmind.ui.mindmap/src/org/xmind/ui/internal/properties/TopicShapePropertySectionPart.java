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
package org.xmind.ui.internal.properties;

import java.util.List;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.gef.Request;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;

public class TopicShapePropertySectionPart extends StyledPropertySectionPart {

    private class FillColorOpenListener implements IOpenListener {
        public void open(OpenEvent event) {
            changeFillColor((IColorSelection) event.getSelection());
        }
    }

    private class ShapeSelectionChangedListener implements
            ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof IDecorationDescriptor) {
                changeShape(((IDecorationDescriptor) o).getId());
            }
        }
    }

    private static List<IDecorationDescriptor> TopicShapes;

    private MComboViewer shapeViewer;

    private ColorPicker fillColorPicker;

    protected void createContent(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        line1.setLayout(layout);

        createLineContent(line1);
    }

    private void createLineContent(Composite parent) {
        shapeViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        shapeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        shapeViewer.getControl().setToolTipText(
                PropertyMessages.TopicShape_toolTip);
        shapeViewer.setContentProvider(new ArrayContentProvider());
        shapeViewer.setLabelProvider(new DecorationLabelProvider());
        shapeViewer.setInput(getTopicShapes());
        shapeViewer
                .addSelectionChangedListener(new ShapeSelectionChangedListener());

        fillColorPicker = new ColorPicker(ColorPicker.AUTO | ColorPicker.CUSTOM
                | ColorPicker.NONE, PaletteContents.getDefault());
        fillColorPicker.getAction().setToolTipText(
                PropertyMessages.TopicFillColor_toolTip);
        fillColorPicker.addOpenListener(new FillColorOpenListener());

        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(fillColorPicker);
        ToolBar barControl = colorBar.createControl(parent);
        barControl.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));
    }

    public void setFocus() {
        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            shapeViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        shapeViewer = null;
        fillColorPicker = null;
    }

    protected void doRefresh() {
        String shapeId = null;
        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            shapeId = getStyleValue(Styles.ShapeClass, null);
            IDecorationDescriptor element = getSelectableShape(shapeId);
            if (element == null) {
                shapeViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                shapeViewer.setSelection(new StructuredSelection(element));
            }
        }
        if (fillColorPicker != null) {
            if (shapeId == null)
                shapeId = getStyleValue(Styles.ShapeClass, null);
            updateColorPicker(fillColorPicker, Styles.FillColor, shapeId);
        }
    }

    private IDecorationDescriptor getSelectableShape(String shapeId) {
        if (shapeId == null)
            return null;
        IDecorationDescriptor descriptor = MindMapUI.getDecorationManager()
                .getDecorationDescriptor(shapeId);
        if (!getTopicShapes().contains(descriptor))
            return null;
        return descriptor;
    }

    private void changeShape(String newShape) {
//        String autoValue = getAutoValue(Styles.SHAPE_CLASS, null);
//        if (newShape.equals(autoValue))
//            newShape = null;
        Request request = createStyleRequest(CommandMessages.Command_ModifyTopicShape);
        addStyle(request, Styles.ShapeClass, newShape);
        sendRequest(request);
    }

    protected void changeFillColor(IColorSelection selection) {
        changeColor(selection, Styles.FillColor,
                CommandMessages.Command_ModifyFillColor);
    }

    private static List<IDecorationDescriptor> getTopicShapes() {
        if (TopicShapes == null) {
            TopicShapes = MindMapUI.getDecorationManager().getDescriptors(
                    IDecorationManager.CATEGORY_TOPIC_SHAPE);
        }
        return TopicShapes;
    }

}