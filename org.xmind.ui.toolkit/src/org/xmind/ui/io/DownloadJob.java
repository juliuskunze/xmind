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
package org.xmind.ui.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.xmind.ui.internal.ToolkitPlugin;

public class DownloadJob extends Job {

    public static final int SUCCESS = IStatus.OK;

    public static final int FAILED = IStatus.ERROR;

    public static final int CANCELED = IStatus.WARNING;

    private String pluginId;

    private String sourceURL;

    private String targetPath;

    private OutputStream targetStream;

    public DownloadJob(String jobName, String sourceURL, String targetPath) {
        this(jobName, sourceURL, targetPath, ToolkitPlugin.PLUGIN_ID);
    }

    public DownloadJob(String jobName, String sourceURL, String targetPath,
            String pluginId) {
        super(jobName);
        Assert.isNotNull(sourceURL);
        Assert.isNotNull(targetPath);
        this.sourceURL = sourceURL;
        this.targetPath = targetPath;
        this.pluginId = pluginId;
    }

    public DownloadJob(String jobName, String sourceURL,
            OutputStream targetStream) {
        this(jobName, sourceURL, targetStream, ToolkitPlugin.PLUGIN_ID);
    }

    public DownloadJob(String jobName, String sourceURL,
            OutputStream targetStream, String pluginId) {
        super(jobName);
        Assert.isNotNull(sourceURL);
        Assert.isNotNull(targetStream);
        this.sourceURL = sourceURL;
        this.targetStream = targetStream;
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public String getTargetPath() {
        return targetPath;
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);

        URL url;
        try {
            url = new URL(sourceURL);
        } catch (MalformedURLException e) {
            return errorStatus(e);
        }

        monitor.subTask(NLS.bind(Messages.ConnectingSource, getSourceURL()));
        URLConnection connection;
        int length;
        try {
            connection = url.openConnection();
            length = connection.getContentLength();
        } catch (IOException e) {
            return errorStatus(e);
        }

        InputStream is;
        try {
            is = connection.getInputStream();
        } catch (IOException e) {
            return errorStatus(e);
        }

        monitor.subTask(NLS.bind(Messages.InitializingTarget, getTargetPath()));
        OutputStream os;
        if (targetStream != null) {
            os = targetStream;
        } else {
            try {
                os = new FileOutputStream(targetPath);
            } catch (FileNotFoundException e) {
                safeClose(is);
                return errorStatus(e);
            }
        }

        is = new MonitoredInputStream(is, monitor);
        os = new MonitoredOutputStream(os, monitor);

        monitor.subTask(Messages.TransferingData);
        IProgressMonitor monitor2 = length < 0 ? null : new SubProgressMonitor(
                monitor, 100);
        if (monitor2 != null) {
            monitor2.beginTask(null, Math.max(1, length / 1024));
        }
        String total = length < 0 ? null : String.format("%.1fK", //$NON-NLS-1$
                length / 1024.0d);
        byte[] buffer = new byte[1024];
        int downloaded = 0;
        int num;
        int worked = 0;
        try {
            while ((num = is.read(buffer)) > 0) {
                os.write(buffer, 0, num);
                downloaded += num;
                String taskName = (total == null ? String.format("(%.1fK)", //$NON-NLS-1$ 
                        (downloaded / 1024.0)) : String.format("(%.1fK/%s)", //$NON-NLS-1$ 
                        (downloaded / 1024.0), total));
                monitor.subTask(Messages.TransferingData + " " + taskName); //$NON-NLS-1$
                if (monitor2 != null) {
                    monitor2.worked(1);
                } else {
                    if (worked < 99) {
                        worked++;
                        monitor.worked(1);
                    }
                }
            }
        } catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                return new Status(IStatus.WARNING, pluginId, CANCELED, NLS
                        .bind(Messages.DownloadCanceled, getSourceURL(),
                                getTargetPath()), null);
            } else {
                return errorStatus(e);
            }
        } finally {
            safeClose(is);
            safeClose(os);
        }

        monitor.done();
        return new Status(IStatus.OK, pluginId, SUCCESS, NLS.bind(
                Messages.DownloadFinished, getSourceURL(), getTargetPath()),
                null);
    }

    private static void safeClose(InputStream is) {
        try {
            is.close();
        } catch (IOException ignore) {
        }
    }

    private static void safeClose(OutputStream os) {
        try {
            os.close();
        } catch (IOException ignore) {
        }
    }

    private IStatus errorStatus(Throwable e) {
        return new Status(IStatus.ERROR, pluginId, FAILED, NLS.bind(
                Messages.DownloadFailed, getSourceURL(), getTargetPath()), e);
    }

}