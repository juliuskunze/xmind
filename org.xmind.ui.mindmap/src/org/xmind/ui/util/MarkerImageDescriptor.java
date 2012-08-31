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
package org.xmind.ui.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageUtils;

public class MarkerImageDescriptor extends ImageDescriptor {

    private static ImageDescriptor ErrorImage = null;

    private IMarker marker;

    private IMarkerRef markerRef;

    private String markerId;

    private int widthHint;

    private int heightHint;

    protected MarkerImageDescriptor(IMarker marker, int widthHint,
            int heightHint) {
        this.marker = marker;
        this.markerRef = null;
        this.markerId = marker.getId();
        this.widthHint = widthHint;
        this.heightHint = heightHint;
    }

    protected MarkerImageDescriptor(IMarkerRef markerRef, int widthHint,
            int heightHint) {
        this.marker = null;
        this.markerRef = markerRef;
        this.markerId = markerRef.getMarkerId();
        this.widthHint = widthHint;
        this.heightHint = heightHint;
    }

    @Override
    public ImageData getImageData() {
        InputStream in = getStream();
        ImageData result = null;
        if (in != null) {
            try {
                result = new ImageData(in);
                result = performScale(result);
            } catch (SWTException e) {
                Logger.log(e, "Unable to create image from marker: [" //$NON-NLS-1$
                        + this.markerId + "] " //$NON-NLS-1$
                        + (marker != null ? marker.getResourcePath() : "")); //$NON-NLS-1$
                // if (e.code != SWT.ERROR_INVALID_IMAGE) {
                //  throw e;
                //  // fall through otherwise
                // }
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    //System.err.println(getClass().getName()+".getImageData(): "+
                    //  "Exception while closing InputStream : "+e);
                }
            }
        }
        if (result == null) {
            result = getErrorImage().getImageData();
        }
        return result;
    }

    private ImageData performScale(ImageData result) {
        if (widthHint >= 0 || heightHint >= 0) {
            int w = widthHint;
            int h = heightHint;
            if (w < 0)
                w = result.width;
            if (h < 0)
                h = result.height;
            result = result.scaledTo(w, h);
        } else {
            double hScale = (double) result.width / MindMapUI.MAX_MARKER_WIDTH;
            double vScale = (double) result.height
                    / MindMapUI.MAX_MARKER_HEIGHT;
            boolean shouldScaleWidth = hScale > 1;
            boolean shouldScaleHeight = vScale > 1;
            if (shouldScaleWidth || shouldScaleHeight) {
                int w, h;
                if (hScale > vScale) {
                    w = MindMapUI.MAX_MARKER_WIDTH;
                    h = (int) (result.height / hScale);
                } else {
                    w = (int) (result.width / vScale);
                    h = MindMapUI.MAX_MARKER_HEIGHT;
                }
                result = result.scaledTo(w, h);
            }
        }
        return result;
    }

    private IMarker getMarker() {
        if (marker == null) {
            if (markerRef != null) {
                return markerRef.getMarker();
            }
        }
        return marker;
    }

    private InputStream getStream() {
        IMarker m = getMarker();
        if (m == null)
            return null;

        IMarkerResource res = m.getResource();
        if (res == null)
            return null;

        InputStream in = res.getInputStream();
        if (in == null)
            return null;

        return new BufferedInputStream(in);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MarkerImageDescriptor))
            return false;
        MarkerImageDescriptor that = (MarkerImageDescriptor) obj;
        if ((this.widthHint < 0 ? that.widthHint >= 0
                : this.widthHint != that.widthHint)
                || (this.heightHint < 0 ? that.heightHint >= 0
                        : this.heightHint != that.heightHint))
            return false;
        IMarker thisMarker = this.getMarker();
        return thisMarker != null && thisMarker.equals(that.getMarker());
    }

    @Override
    public int hashCode() {
        return markerId.hashCode();
    }

    @Override
    public String toString() {
        return "MarkerImageDescriptor(marker=" + markerId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static ImageDescriptor getErrorImage() {
        if (ErrorImage == null) {
            ErrorImage = ImageUtils.createErrorImage(
                    MindMapUI.DEF_MARKER_WIDTH, MindMapUI.DEF_MARKER_HEIGHT);
        }
        return ErrorImage;
    }

    public static ImageDescriptor createFromMarker(IMarker marker) {
        if (marker == null)
            return getErrorImage();
        return new MarkerImageDescriptor(marker, -1, -1);
    }

    public static ImageDescriptor createFromMarkerRef(IMarkerRef markerRef) {
        if (markerRef == null)
            return getErrorImage();
        return new MarkerImageDescriptor(markerRef, -1, -1);
    }

    public static ImageDescriptor createFromMarker(IMarker marker,
            int widthHint, int heightHint) {
        if (marker == null)
            return getErrorImage();
        return new MarkerImageDescriptor(marker, widthHint, heightHint);
    }

    public static ImageDescriptor createFromMarkerRef(IMarkerRef markerRef,
            int widthHint, int heightHint) {
        if (markerRef == null)
            return getErrorImage();
        return new MarkerImageDescriptor(markerRef, widthHint, heightHint);
    }

}