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
package org.xmind.ui.internal.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;
import org.xmind.ui.viewers.ICompositeProvider;

public class TemplateImageLoader extends Job {

    private static final String THUMBNAIL_JPEG = "Thumbnails/thumbnail.jpg"; //$NON-NLS-1$
    private static final String THUMBNAIL_PNG = "Thumbnails/thumbnail.png"; //$NON-NLS-1$

    /**
     * Image cache are automatically cleared out in TIMEOUT milliseconds after
     * the last loading. Default CACHE_TIMEOUT is 5 minutes.
     */
    private static final int CACHE_TIMEOUT = 1000 * 300;

    /**
     * Job will be automatically stopped in FINISH_TIMEOUT milliseconds after
     * the last loading. Default FINISH_TIMEOUT is 30 minutes.
     */
    private static final int FINISH_TIMEOUT = 1000 * 1800;

    private Display display;

    private ICompositeProvider compositeProvider = null;

    private MindMapImageExporter exporter;

    private Queue<ITemplateDescriptor> templateQueue = new LinkedList<ITemplateDescriptor>();

    private Map<ITemplateDescriptor, ImageDescriptor> loadedImages = new HashMap<ITemplateDescriptor, ImageDescriptor>();

    private int cacheTimeout = 0;

    public TemplateImageLoader(Display display) {
        super("Template Image Loader"); //$NON-NLS-1$
        this.display = display;
        this.exporter = new MindMapImageExporter(display);
        setSystem(true);
        schedule();
    }

    public void loadImage(ITemplateDescriptor template) {
        templateQueue.offer(template);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            final GhostShellProvider shellProvider = new GhostShellProvider(
                    display);
            this.compositeProvider = shellProvider;
            try {
                loop(monitor);
            } finally {
                shellProvider.dispose();
            }
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        }
    }

    private void loop(IProgressMonitor monitor) throws InterruptedException {
        long lastLoadTimestamp = System.currentTimeMillis();
        do {
            ITemplateDescriptor template = templateQueue.poll();

            if (monitor.isCanceled())
                return;

            if (template != null) {
                loadImage(template, monitor);
                lastLoadTimestamp = System.currentTimeMillis();
                cacheTimeout = CACHE_TIMEOUT;
            } else {
                if (cacheTimeout > 0
                        && System.currentTimeMillis() - lastLoadTimestamp > cacheTimeout) {
                    clearCache();
                    cacheTimeout = 0;
                } else if (cacheTimeout == 0
                        && System.currentTimeMillis() - lastLoadTimestamp > FINISH_TIMEOUT) {
                    clearCache();
                    return;
                }
            }

            if (monitor.isCanceled())
                return;

            if (template == null) {
                Thread.sleep(100);
            }

            if (monitor.isCanceled())
                return;

        } while (true);
    }

    private void loadImage(final ITemplateDescriptor template,
            final IProgressMonitor monitor) {
        ImageDescriptor image = loadedImages.get(template);
        if (image != null) {
            template.setImage(image);
        } else {
            InputStream stream = template.newStream();
            if (stream != null) {
                try {
                    image = loadImageFromExistingThumbnail(stream);
                } catch (Throwable e) {
                    Logger.log(e, "Failed to load image: " + template.getName()); //$NON-NLS-1$
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (image == null) {
                stream = template.newStream();
                if (stream != null) {
                    try {
                        image = loadImageFromThumbnailExporter(stream);
                    } catch (Throwable e) {
                        Logger.log(e, NLS.bind("Failed to load image: {0}", //$NON-NLS-1$
                                template.getName()));
                    } finally {
                        try {
                            stream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            if (image != null) {
                loadedImages.put(template, image);
                template.setImage(image);
            }
        }
    }

    protected ImageDescriptor loadImageFromExistingThumbnail(InputStream stream)
            throws IOException {
        ZipInputStream zin = new ZipInputStream(stream);
        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            if (THUMBNAIL_PNG.equals(entry.getName())
                    || THUMBNAIL_JPEG.equals(entry.getName())) {
                try {
                    return loadImageFromThumbnail(zin);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            entry = zin.getNextEntry();
        }
        return null;
    }

    private ImageDescriptor loadImageFromThumbnail(InputStream stream) {
        ImageLoader loader = new ImageLoader();
        loader.load(stream);
        return ImageDescriptor.createFromImageData(loader.data[0]);
    }

    protected ImageDescriptor loadImageFromThumbnailExporter(InputStream stream)
            throws Exception {
        IStorage storage = new ByteArrayStorage();
        FileUtils.extractZipStream(stream, storage.getOutputTarget());
        try {
            IWorkbook workbook = Core.getWorkbookBuilder().loadFromStorage(
                    storage);
            exporter.setSource(new MindMap(workbook.getPrimarySheet()),
                    compositeProvider, null, null);
            Image image = exporter.createImage();
            return ImageDescriptor.createFromImage(image);
        } catch (CoreException e) {
            if (e.getType() == Core.ERROR_WRONG_PASSWORD
                    || e.getType() == Core.ERROR_CANCELLATION) {
                return MindMapUI.getImages().get(
                        IMindMapImages.DEFAULT_THUMBNAIL);
            }
            throw e;
        }
    }

    private void clearCache() {
        if (loadedImages.isEmpty())
            return;

        loadedImages.clear();
    }

    @Override
    protected void canceling() {
        Thread thread = getThread();
        if (thread != null) {
            thread.interrupt();
        }
        super.canceling();
    }

}