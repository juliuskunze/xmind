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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * @author Frank Shaka
 */
public class SizeableImageFigure extends ReferencedFigure implements IHasImage {

    private static final Rectangle IMAGE_CLIENT_AREA = new Rectangle();

    private static final int FLAG_STRETCHED = MAX_FLAG << 1;

    private static final int FLAG_CONSTRAINED = MAX_FLAG << 2;

    static {
        MAX_FLAG = FLAG_CONSTRAINED;
    }

    private Image img = null;

    private Dimension imgSize = null;

    private int alpha = -1;

    /**
     * @param parent
     */
    public SizeableImageFigure() {
        this(null);
    }

    public SizeableImageFigure(Image image) {
        setImage(image);
    }

    /**
     * Sets the Image that this ImageFigure displays.
     * <p>
     * IMPORTANT: Note that it is the client's responsibility to dispose the
     * given image.
     * 
     * @param image
     *            The Image to be displayed. It can be <code>null</code>.
     */
    public void setImage(Image image) {
        if (img == image)
            return;
        img = image;
        imgSize = null;
        setMinimumSize(new Dimension(1, 1));
        revalidate();
        repaint();
    }

    /**
     * @return the initial size of the containing image or an empty size if no
     *         image is set
     */
    public Dimension getImageSize() {
        if (imgSize == null) {
            if (img != null) {
                imgSize = new Dimension(img);
            } else
                imgSize = new Dimension();
        }
        return imgSize;
    }

    public boolean isConstrained() {
        return getFlag(FLAG_CONSTRAINED);
    }

    public void setConstrained(boolean constrained) {
        if (constrained == isConstrained())
            return;
        setFlag(FLAG_CONSTRAINED, constrained);
        repaint();
    }

    public boolean isStretched() {
        return getFlag(FLAG_STRETCHED);
    }

    public void setStretched(boolean stretched) {
        if (stretched == isStretched())
            return;
        setFlag(FLAG_STRETCHED, stretched);
        repaint();
    }

    /**
     * @return The Image that this Figure displays
     */
    public Image getImage() {
        return img;
    }

    /**
     * @return the alpha
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     * @see IUseTransparency#DONT_USE_ALPHA
     * @param alpha
     *            the alpha to set
     */
    public void setAlpha(int alpha) {
        if (this.alpha == alpha)
            return;
        this.alpha = alpha;
        repaint();
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(Graphics)
     */
    protected void paintFigure(final Graphics graphics) {
        super.paintFigure(graphics);

        Image image = getImage();
        if (image != null) {
            graphics.setAntialias(SWT.ON);
            if (getAlpha() != IUseTransparency.DONT_USE_ALPHA)
                graphics.setAlpha(getAlpha());
            paintImage(graphics, image);
        }
    }

    protected void paintImage(Graphics graphics, Image image) {
        if (image != null) {
            Dimension imageSize;
            if (image == this.img)
                imageSize = getImageSize();
            else {
                imageSize = new Dimension(image);
            }
            paintImage(graphics, image, imageSize,
                    getImageClientArea(imageSize));
        }
    }

    protected Rectangle getImageClientArea(Dimension imageSize) {
        Rectangle area = getClientArea(IMAGE_CLIENT_AREA);
        boolean constrained = isConstrained();
        boolean stretched = isStretched();
        if (constrained
                && (stretched || imageSize.width > area.width || imageSize.height > area.height)) {
            adaptAreaToRatio(area, imageSize);
        } else if (!stretched) {
            adaptAreaToSize(area, imageSize);
        }
        return area;
    }

    protected void adaptAreaToSize(Rectangle area, Dimension size) {
        area.x += (area.width - size.width) / 2;
        area.width = size.width;
        area.y += (area.height - size.height) / 2;
        area.height = size.height;
    }

    protected void adaptAreaToRatio(Rectangle area, Dimension ratio) {
        int a = ratio.width * area.height;
        int b = ratio.height * area.width;
        if (a > b) {
            int h = area.width == 0 ? 0 : b / ratio.width;
            area.y += (area.height - h) / 2;
            area.height = h;
        } else if (a < b) {
            int w = area.height == 0 ? 0 : a / ratio.height;
            area.x += (area.width - w) / 2;
            area.width = w;
        }
    }

    protected void paintImage(Graphics graphics, Image image,
            Dimension imageSize, Rectangle clientArea) {
        if (clientArea.width == imageSize.width
                && clientArea.height == imageSize.height) {
            graphics.drawImage(image, clientArea.x, clientArea.y);
        } else {
            graphics.drawImage(image, 0, 0, imageSize.width, imageSize.height,
                    clientArea.x, clientArea.y, clientArea.width,
                    clientArea.height);
        }
    }

}