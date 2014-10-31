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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.gef.Request;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;

public class TopicBorderPropertySectionPart extends StyledPropertySectionPart {

    private class BorderLineColorOpenListener implements IOpenListener {
        public void open(OpenEvent event) {
            changeBorderLineColor((IColorSelection) event.getSelection());
        }
    }

    private class BorderLineWidthSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof LineWidth) {
                changeBorderLineWidth((LineWidth) o);
            }
        }
    }

    private ColorPicker borderLineColorPicker;

    private MComboViewer borderLineWidthViewer;

    protected void doRefresh() {
        String lineShapeId = getLineShapeId();
        refreshWithShapeId(lineShapeId);
    }

    protected String getLineShapeId() {
        return getStyleValue(Styles.LineClass, null);
    }

    protected void refreshWithShapeId(String lineShapeId) {
        if (borderLineWidthViewer != null
                && !borderLineWidthViewer.getControl().isDisposed()) {
            String borderLineWidth = getStyleValue(Styles.BorderLineWidth,
                    lineShapeId);
            LineWidth element = LineWidth.findByValue(borderLineWidth);

            if (Styles.TOPIC_SHAPE_NO_BORDER.equals(getStyleValue(
                    Styles.ShapeClass, null)))
                element = LineWidth.None;

            if (element == null)
                element = LineWidth.findByValue(getStyleValue(Styles.LineWidth,
                        lineShapeId));
            if (element == null)
                element = LineWidth.Thinnest;

            borderLineWidthViewer
                    .setSelection(new StructuredSelection(element));
        }
        if (borderLineColorPicker != null) {
            updateColorPicker(borderLineColorPicker, Styles.BorderLineColor,
                    lineShapeId);
        }
    }

    protected void updateColorPicker(ColorPicker picker, String styleKey,
            String decorationId) {
        String autoColor = getAutoValue(styleKey, decorationId);
        if (autoColor == null)
            autoColor = getAutoValue(styleKey, decorationId);
        picker.setAutoColor(StyleUtils.convertRGB(styleKey, autoColor));
        String userColor = getUserValue(styleKey);
        if (userColor == null)
            userColor = getUserValue(styleKey);
        int type;
        if (userColor == null) {
            type = IColorSelection.AUTO;
            userColor = autoColor;
        } else {
            type = IColorSelection.CUSTOM;
        }
        if (type != IColorSelection.AUTO && Styles.NONE.equals(userColor)) {
            type = IColorSelection.NONE;
        }
        RGB color = StyleUtils.convertRGB(Styles.TextColor, userColor);
        picker.setSelection(new ColorSelection(type, color));
    }

    protected void createContent(Composite parent) {
        Composite line = new Composite(parent, SWT.NONE);
        line.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        line.setLayout(layout);

        createLineContent(line);
    }

    private void createLineContent(Composite parent) {
        borderLineWidthViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        borderLineWidthViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        borderLineWidthViewer.getControl().setToolTipText(
                PropertyMessages.LineWidth_toolTip);
        borderLineWidthViewer.setContentProvider(new ArrayContentProvider());
        borderLineWidthViewer.setLabelProvider(new LineWidthLabelProvider());
        borderLineWidthViewer.setInput(LineWidth.values());
        borderLineWidthViewer
                .addSelectionChangedListener(new BorderLineWidthSelectionChangedListener());

        borderLineColorPicker = new ColorPicker(ColorPicker.AUTO
                | ColorPicker.CUSTOM, PaletteContents.getDefault());
        borderLineColorPicker.getAction().setToolTipText(
                PropertyMessages.LineColor_toolTip);
        borderLineColorPicker
                .addOpenListener(new BorderLineColorOpenListener());
        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(borderLineColorPicker);
        ToolBar barControl = colorBar.createControl(parent);
        barControl.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));
    }

    public void setFocus() {
        if (borderLineWidthViewer != null
                && !borderLineWidthViewer.getControl().isDisposed()) {
            borderLineWidthViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        borderLineWidthViewer = null;
        borderLineColorPicker = null;
    }

    protected void changeBorderLineColor(IColorSelection selection) {
        changeColor(selection, Styles.BorderLineColor,
                CommandMessages.Command_ModifyBorderColor);
    }

    protected void changeBorderLineWidth(LineWidth lineWidth) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyBorderShape);
        String value = lineWidth == null ? null : lineWidth.getValue();
        addStyle(request, Styles.BorderLineWidth, value);
        sendRequest(request);
    }

}