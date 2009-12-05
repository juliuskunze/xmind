/*
 * Copyright (c) 2006-2009 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share.jobs;

import java.io.File;
import java.io.FileNotFoundException;

import net.xmind.share.Messages;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.eclipse.osgi.util.NLS;

public class TransferFileJob implements Runnable {

    private String userName;

    private String session;

    private Part[] parts;

    private Exception exception = null;

    private boolean done = false;

    private HttpClient client;

    public TransferFileJob(String userName, String session, File file)
            throws FileNotFoundException {
        super();
        this.userName = userName;
        this.session = session;
        this.parts = new Part[] { new FilePart("map", file) }; //$NON-NLS-1$
    }

    public void run() {
        client = new HttpClient();
        try {
            int ret = HttpUtils.uploadFile(client, userName, session, parts);
            if (ret != HttpStatus.SC_OK)
                exception = new HttpException(NLS.bind(
                        Messages.TransferFileJob_ErrorCode_message, ret));
        } catch (Exception e) {
            this.exception = e;
        }
        client = null;
        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public Exception getException() {
        return exception;
    }
}