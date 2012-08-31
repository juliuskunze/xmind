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
package org.xmind.ui.gallery;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.util.Properties;

public class FrameDecorator extends Decorator {

    public static final FrameDecorator DEFAULT = new FrameDecorator();

//    public void activate(IGraphicalPart part, IFigure figure) {
//        super.activate(part, figure);
//        createContents(part, part.getContentPane());
//    }
//
//    protected void createContents(IGraphicalPart part, IFigure contentPane) {
//        SizeableImageFigure imageFigure = new SizeableImageFigure();
//        contentPane.add(imageFigure);
//    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        FrameFigure frame = (FrameFigure) part.getFigure();
        Object model = part.getModel();

        GalleryViewer viewer = (GalleryViewer) part.getSite().getViewer();
        Properties properties = viewer.getProperties();
        IBaseLabelProvider labelProvider = viewer.getLabelProvider();
        boolean hideTitle = properties.getBoolean(GalleryViewer.HideTitle,
                false);
        frame.setHideTitle(hideTitle);

        boolean flat = properties.getBoolean(GalleryViewer.FlatFrames, false);
        frame.setFlat(flat);

        int titlePlacement = properties.getInteger(
                GalleryViewer.TitlePlacement, GalleryViewer.TITLE_TOP);
        frame.setTitlePlacement(titlePlacement);
        if (!hideTitle) {
            decorateTitle(frame.getTitle(), model, labelProvider);
        }
//        decorateContent(part, model, properties, labelProvider);
    }

//    protected void decorateContent(IGraphicalPart part, Object model,
//            Properties properties, IBaseLabelProvider labelProvider) {
//        Image image = getImage(model, labelProvider);
//        IFigure contentPane = part.getContentPane();
//        if (!contentPane.getChildren().isEmpty()) {
//            Object imageFigure = contentPane.getChildren().get(0);
//            if (imageFigure instanceof SizeableImageFigure) {
//                decorateImage((SizeableImageFigure) imageFigure, image,
//                        properties);
//            }
//        }
//    }

    private String getText(Object element, IBaseLabelProvider labelProvider) {
        if (labelProvider instanceof ILabelProvider)
            return ((ILabelProvider) labelProvider).getText(element);
        return null;
    }

//    private Image getImage(Object element, IBaseLabelProvider labelProvider) {
//        if (labelProvider instanceof ILabelProvider)
//            return ((ILabelProvider) labelProvider).getImage(element);
//        return null;
//    }
//
//    protected void decorateImage(SizeableImageFigure imageFigure, Image image,
//            Properties properties) {
//        imageFigure.setImage(image);
//        boolean stretched = properties.getBoolean(GalleryViewer.ImageStretched,
//                false);
//        boolean constained = properties.getBoolean(
//                GalleryViewer.ImageConstrained, false);
//        imageFigure.setConstrained(constained);
//        imageFigure.setStretched(stretched);
//        Dimension size = (Dimension) properties
//                .get(GalleryViewer.FrameContentSize);
//        if (size == null) {
//            imageFigure.setPreferredSize(imageFigure.getImageSize());
//        } else {
//            imageFigure.setPreferredSize(size);
//        }
//    }

    protected void decorateTitle(ITextFigure titleFigure, Object model,
            IBaseLabelProvider labelProvider) {
        String text = getText(model, labelProvider);
        if (text == null)
            text = ""; //$NON-NLS-1$
        titleFigure.setText(text);
        if (labelProvider instanceof IFontProvider) {
            IFontProvider fontProvider = (IFontProvider) labelProvider;
            titleFigure.setFont(fontProvider.getFont(model));
        }
        if (labelProvider instanceof IColorProvider) {
            IColorProvider colorProvider = (IColorProvider) labelProvider;
            titleFigure.setForegroundColor(colorProvider.getForeground(model));
            titleFigure.setBackgroundColor(colorProvider.getBackground(model));
        }
    }

}