/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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
package org.xmind.gef.image;

import java.io.OutputStream;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.xmind.gef.internal.image.ConstrainedExportAreaProvider;
import org.xmind.gef.internal.image.FittedExportAreaProvider;
import org.xmind.gef.internal.image.SWTImageWriter;
import org.xmind.gef.internal.image.StretchedExportAreaProvider;

public class ImageExportUtils {

    public static IExportAreaProvider createExportAreaProvider(IFigure figure) {
        return createExportAreaProvider(figure, null,
                ResizeConstants.RESIZE_NONE, -1, -1, null);
    }

    public static IExportAreaProvider createExportAreaProvider(IFigure figure,
            Insets margins) {
        return createExportAreaProvider(figure, null,
                ResizeConstants.RESIZE_NONE, -1, -1, margins);
    }

    public static IExportAreaProvider createExportAreaProvider(IFigure figure,
            int resizeStrategy, int wHint, int hHint) {
        return createExportAreaProvider(figure, null, resizeStrategy, wHint,
                hHint, null);
    }

    public static IExportAreaProvider createExportAreaProvider(IFigure figure,
            int resizeStrategy, int wHint, int hHint, Insets margins) {
        return createExportAreaProvider(figure, null, resizeStrategy, wHint,
                hHint, margins);
    }

    public static IExportAreaProvider createExportAreaProvider(IFigure figure,
            Rectangle sourceArea) {
        return createExportAreaProvider(figure, sourceArea,
                ResizeConstants.RESIZE_NONE, -1, -1, null);
    }

    public static IExportAreaProvider createExportAreaProvider(IFigure figure,
            Rectangle sourceArea, Insets margins) {
        return createExportAreaProvider(figure, sourceArea,
                ResizeConstants.RESIZE_NONE, -1, -1, margins);
    }

    public static IExportAreaProvider createExportAreaProvider(IFigure figure,
            Rectangle sourceArea, int resizeStrategy, int constrainedWidth,
            int constrainedHeight, Insets margins) {
        if (resizeStrategy == ResizeConstants.RESIZE_STRETCH) {
            return new StretchedExportAreaProvider(figure, sourceArea,
                    constrainedWidth, constrainedHeight, margins);
        } else if (resizeStrategy == ResizeConstants.RESIZE_FIT) {
            return new FittedExportAreaProvider(figure, sourceArea,
                    constrainedWidth, constrainedHeight, margins);
        } else if (resizeStrategy == ResizeConstants.RESIZE_CONSTRAIN) {
            return new ConstrainedExportAreaProvider(figure, sourceArea,
                    constrainedWidth, constrainedHeight, margins);
        }
        return new ExportAreaProvider(figure, sourceArea, constrainedWidth,
                constrainedHeight, margins);
    }

    public static ImageDescriptor createImageDescriptor(IFigure figure) {
        return FigureImageDescriptor.createFromFigure(figure,
                createExportAreaProvider(figure, null,
                        ResizeConstants.RESIZE_NONE, -1, -1, null));
    }

    public static ImageDescriptor createImageDescriptor(IFigure figure,
            Insets margins) {
        return FigureImageDescriptor.createFromFigure(figure,
                createExportAreaProvider(figure, null,
                        ResizeConstants.RESIZE_NONE, -1, -1, margins));
    }

    public static ImageDescriptor createImageDescriptor(IFigure figure,
            int resizeStrategy, int wHint, int hHint) {
        return FigureImageDescriptor.createFromFigure(figure,
                createExportAreaProvider(figure, null, resizeStrategy, wHint,
                        hHint, null));
    }

    public static ImageDescriptor createImageDescriptor(IFigure figure,
            int resizeStrategy, int wHint, int hHint, Insets margins) {
        return FigureImageDescriptor.createFromFigure(figure,
                createExportAreaProvider(figure, null, resizeStrategy, wHint,
                        hHint, margins));
    }

    public static ImageDescriptor createImageDescriptor(IFigure figure,
            Rectangle sourceArea) {
        return FigureImageDescriptor.createFromFigure(figure,
                createExportAreaProvider(figure, sourceArea,
                        ResizeConstants.RESIZE_NONE, -1, -1, null));
    }

    public static ImageDescriptor createImageDescriptor(IFigure figure,
            Rectangle sourceArea, Insets margins) {
        return FigureImageDescriptor.createFromFigure(figure,
                createExportAreaProvider(figure, sourceArea,
                        ResizeConstants.RESIZE_NONE, -1, -1, margins));
    }

    public static ImageDescriptor createImageDescriptor(IFigure figure,
            Rectangle sourceArea, int resizeStrategy, int constrainedWidth,
            int constrainedHeight, Insets margins) {
        return FigureImageDescriptor.createFromFigure(figure,
                createExportAreaProvider(figure, sourceArea, resizeStrategy,
                        constrainedWidth, constrainedHeight, margins));
    }

    public static ImageWriter createImageWriter(Image image, int format,
            OutputStream output) {
//        if (format == SWT.IMAGE_PNG) {
//            return new PngWriter(image, output);
//        }
        return new SWTImageWriter(image, format, output);
    }

    public static ImageWriter createImageWriter(ImageData[] imageData,
            int format, OutputStream output) {
        return new SWTImageWriter(imageData, format, output);
    }

}