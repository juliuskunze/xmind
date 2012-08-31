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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.core.IImage;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.mindmap.IImagePart;

public class ImageDecorator extends Decorator {

    private static final ImageDecorator instance = new ImageDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        if (part instanceof IImagePart) {
            IImagePart imagePart = (IImagePart) part;
            if (figure instanceof SizeableImageFigure) {
                SizeableImageFigure imageFigure = (SizeableImageFigure) figure;
                decorateImageFigure(imagePart, imageFigure);
            }
        }
    }

    private void decorateImageFigure(IImagePart imagePart,
            SizeableImageFigure imageFigure) {
        imageFigure.setStretched(true);
        imageFigure.setConstrained(false);
        imageFigure.setImage(imagePart.getImage());
        IImage imageModel = imagePart.getImageModel();
        int width = imageModel.getWidth();
        int height = imageModel.getHeight();
        if (imagePart.getImageDescriptor() instanceof AttachmentImageDescriptor) {
            if (width >= 0 && height >= 0) {
                imageFigure.setPreferredSize(width, height);
            } else {
                Dimension imageSize = imageFigure.getImageSize();
                if (width >= 0) {
                    imageFigure.setPreferredSize(width, imageSize.height);
                } else if (height >= 0) {
                    imageFigure.setPreferredSize(imageSize.width, height);
                } else {
                    imageFigure.setPreferredSize(imageSize);
                }
            }
        } else {
            imageFigure.setPreferredSize(imageFigure.getImageSize());
        }
    }

    public static ImageDecorator getInstance() {
        return instance;
    }

}