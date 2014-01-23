/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share.dialog;

import net.xmind.share.Info;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.IMeta;
import org.xmind.ui.viewers.ImagePreviewViewer;

public class CutPreviewViewer extends ImagePreviewViewer {

    private Info info;

    public CutPreviewViewer() {
        super();
    }

    public void setInfo(Info info) {
        this.info = info;
        Image img = (Image) info.getProperty(Info.FULL_IMAGE);
        if (img != null) {
            if (info.hasProperty(IMeta.ORIGIN_X)
                    && info.hasProperty(IMeta.ORIGIN_Y)) {
                setImage(img, info.getInt(IMeta.ORIGIN_X, 0),
                        info.getInt(IMeta.ORIGIN_Y, 0));
            } else {
                setImage(img);
            }
        }
    }

    protected void createRatioControls(Composite parent) {
        super.createRatioControls(parent);
        if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
            setBackgroundColor(parent.getDisplay().getSystemColor(
                    SWT.COLOR_LIST_BACKGROUND));
        }
    }

    public void setX(double x) {
        super.setX(x);
        if (info != null)
            info.setInt(Info.X, (int) x);
    }

    public void setY(double y) {
        super.setY(y);
        if (info != null)
            info.setInt(Info.Y, (int) y);
    }

    public void setRatio(double ratio) {
        super.setRatio(ratio);
        if (info != null)
            info.setDouble(Info.SCALE, ratio);
    }

}