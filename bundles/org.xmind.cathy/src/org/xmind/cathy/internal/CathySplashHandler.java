package org.xmind.cathy.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

public class CathySplashHandler extends AbstractSplashHandler {

    public CathySplashHandler() {
    }

    private Image image;

    public void init(final Shell splash) {
        super.init(splash);
        final Display display = splash.getDisplay();
        final GC shellGC = new GC(splash);
        URL url = FileLocator.find(Platform.getBundle(CathyPlugin.PLUGIN_ID),
                new Path("icons/progress.gif"), null); //$NON-NLS-1$
        final ImageLoader loader = new ImageLoader();
        InputStream imageStream = null;
        try {
            imageStream = url.openStream();
            final ImageData[] imageDataArray = loader.load(imageStream);
            if (imageDataArray.length > 1) {
                Thread animateThread = new Thread("Animation") { //$NON-NLS-1$
                    @Override
                    public void run() {
                        /*
                         * Create an off-screen image to draw on.
                         */
                        Image offScreenImage = new Image(display,
                                loader.logicalScreenWidth,
                                loader.logicalScreenHeight);
                        GC offScreenImageGC = new GC(offScreenImage);
                        try {
                            /*
                             * Create the first image and draw it on the
                             * off-screen image.
                             */
                            int imageDataIndex = 0;
                            ImageData imageData = imageDataArray[imageDataIndex];

                            if (image != null && !image.isDisposed())
                                image.dispose();
                            image = new Image(display, imageData);
                            offScreenImageGC.drawImage(image, 0, 0,
                                    imageData.width, imageData.height,
                                    imageData.x, imageData.y, imageData.width,
                                    imageData.height);

                            /*
                             * Now loop through the images, creating and drawing
                             * each one on the off-screen image before drawing
                             * it on the shell.
                             */
                            while (loader.repeatCount == 0) {
                                if (splash == null || splash.isDisposed()) {
                                    loader.repeatCount = 1;
                                }

                                imageDataIndex = (imageDataIndex + 1)
                                        % imageDataArray.length;
                                imageData = imageDataArray[imageDataIndex];
                                image.dispose();
                                image = new Image(display, imageData);
                                offScreenImageGC.drawImage(image, 0, 0,
                                        imageData.width, imageData.height,
                                        imageData.x, imageData.y,
                                        imageData.width, imageData.height);

                                /* Draw the off-screen image to the shell. */
                                shellGC.drawImage(image, 233, 215);

                                /*
                                 * Sleep for the specified delay time (adding
                                 * commonly-used slow-down fudge factors).
                                 */
                                try {
                                    int ms = imageData.delayTime * 10;
                                    if (ms < 20)
                                        ms += 30;
                                    if (ms < 30)
                                        ms += 10;
                                    Thread.sleep(ms);
                                } catch (InterruptedException e) {
                                }
                            }
                        } catch (SWTException ex) {
                            CathyPlugin
                                    .log("There was an error animating the GIF"); //$NON-NLS-1$
                        } finally {
                            if (offScreenImage != null
                                    && !offScreenImage.isDisposed())
                                offScreenImage.dispose();
                            if (offScreenImageGC != null
                                    && !offScreenImageGC.isDisposed())
                                offScreenImageGC.dispose();
                            if (image != null && !image.isDisposed())
                                image.dispose();
                        }
                    }
                };
                animateThread.start();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SWTException ex) {
            CathyPlugin.log("There was an error loading the GIF"); //$NON-NLS-1$
        } finally {
            try {
                imageStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        super.dispose();
    }

}
