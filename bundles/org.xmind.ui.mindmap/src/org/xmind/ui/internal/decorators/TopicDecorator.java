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
package org.xmind.ui.internal.decorators;

import static org.xmind.ui.style.StyleUtils.createTopicDecoration;
import static org.xmind.ui.style.StyleUtils.getColor;
import static org.xmind.ui.style.StyleUtils.getInteger;
import static org.xmind.ui.style.StyleUtils.getLineStyle;
import static org.xmind.ui.style.StyleUtils.getString;
import static org.xmind.ui.style.StyleUtils.getStyleSelector;
import static org.xmind.ui.style.StyleUtils.isSameDecoration;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.internal.figures.TopicFigure;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class TopicDecorator extends Decorator {

    private static final TopicDecorator instance = new TopicDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (figure instanceof TopicFigure) {
            IGraphicalPart branch = MindMapUtils.findBranch(part);
            if (branch != null)
                part = branch;
            decorateTopic(part, getStyleSelector(part), (TopicFigure) figure);
        }
    }

    private void decorateTopic(IGraphicalPart part, IStyleSelector ss,
            TopicFigure figure) {
        ITopicDecoration shape = figure.getDecoration();

        String newShapeId = getString(part, ss, Styles.ShapeClass,
                Styles.TOPIC_SHAPE_ROUNDEDRECT);
        if (!isSameDecoration(shape, newShapeId)) {
            shape = createTopicDecoration(part, newShapeId);
            figure.setDecoration(shape);
        }
        if (shape != null) {
            String decorationId = shape.getId();
            shape.setAlpha(figure, 0xff);
            shape.setFillAlpha(figure, 0xff);
            shape.setLineAlpha(figure, 0xff);
            shape.setFillColor(figure, getColor(part, ss, Styles.FillColor,
                    decorationId, Styles.DEF_TOPIC_FILL_COLOR));

            shape.setGradient(figure, usesGradientColor(part));

            shape.setLeftMargin(figure, getInteger(part, ss, Styles.LeftMargin,
                    decorationId, 10));
            shape.setRightMargin(figure, getInteger(part, ss,
                    Styles.LeftMargin, decorationId, 10));
            shape.setTopMargin(figure, getInteger(part, ss, Styles.TopMargin,
                    decorationId, 5));
            shape.setBottomMargin(figure, getInteger(part, ss,
                    Styles.BottomMargin, decorationId, 5));
            shape.setLineColor(figure, getColor(part, ss, Styles.LineColor,
                    decorationId, Styles.DEF_TOPIC_LINE_COLOR));
            shape.setLineStyle(figure, getLineStyle(part, ss, decorationId,
                    SWT.LINE_SOLID));
            shape.setLineWidth(figure, getInteger(part, ss, Styles.LineWidth,
                    decorationId, 1));
            shape.setVisible(figure, true);

            if (shape instanceof ICorneredDecoration) {
                ((ICorneredDecoration) shape).setCornerSize(figure, getInteger(
                        part, ss, Styles.ShapeCorner, decorationId, 10));
            }
        }
        double angle = StyleUtils.getDouble(part, ss, Styles.RotateAngle, 0);
        figure.setRotationDegrees(angle);
    }

    private boolean usesGradientColor(IGraphicalPart part) {
        boolean isGColor = MindMapUI.isGradientColor();

        return part.getSite().getViewer().getProperties().getBoolean(
                IMindMapViewer.VIEWER_GRADIENT, isGColor);
    }

    public static TopicDecorator getInstance() {
        return instance;
    }
}