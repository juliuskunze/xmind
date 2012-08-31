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
package net.xmind.signin.internal;

import static net.xmind.signin.internal.EncodingUtils.urlEncode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.xmind.signin.IDataStore;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.json.JSONException;
import org.json.JSONObject;

public class XMindNetRequest {

    private static final String DEFAULT_DOMAIN = "www.xmind.net"; //$NON-NLS-1$

    public static final int ERROR = 599;

    private static class Parameter {
        public String name;
        public Object value;

        public Parameter(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getValue() {
            return value == null ? "" : value.toString(); //$NON-NLS-1$
        }
    }

    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8"; //$NON-NLS-1$

    private boolean useHTTPS;

    private HttpMethodBase method = null;

    private String uri = null;

    private List<NameValuePair> headers = new ArrayList<NameValuePair>();

    private List<Parameter> params = new ArrayList<Parameter>();

    private String domain = DEFAULT_DOMAIN;

    private boolean multipart = false;

    private int code = 0;

    private IDataStore data = null;

    private String responseText = null;

    private boolean aborted = false;

    private Throwable exception = null;

    private boolean debugging = System
            .getProperty("org.xmind.debug.httprequests") != null; //$NON-NLS-1$

    public XMindNetRequest() {
        this(false);
    }

    public XMindNetRequest(boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    public int getCode() {
        return code;
    }

    public IDataStore getData() {
        return data;
    }

    public XMindNetRequest uri(String api, Object... values) {
        this.uri = EncodingUtils.format(api, values);
        return this;
    }

    public XMindNetRequest domain(String domain) {
        this.domain = domain;
        return this;
    }

    public XMindNetRequest useHTTPS() {
        this.useHTTPS = true;
        return this;
    }

    public XMindNetRequest multipart() {
        this.multipart = true;
        return this;
    }

    public XMindNetRequest setAuthToken(String authToken) {
        return this.addHeader("AuthToken", authToken); //$NON-NLS-1$
    }

    public XMindNetRequest addHeader(String name, String value) {
        headers.add(new NameValuePair(name, value));
        return this;
    }

    public XMindNetRequest addParameter(String name, Object value) {
        params.add(new Parameter(name, value));
        return this;
    }

    protected HttpMethodBase getMethod() {
        return method;
    }

    public void abort() {
        if (method != null)
            method.abort();
        aborted = true;
    }

    public boolean isAborted() {
        return aborted;
    }

    public XMindNetRequest get() {
        this.method = new GetMethod();
        return execute();
    }

    public XMindNetRequest put() {
        this.method = new PutMethod();
        return execute();
    }

    public XMindNetRequest delete() {
        this.method = new DeleteMethod();
        return execute();
    }

    public XMindNetRequest post() {
        this.method = new PostMethod();
        return execute();
    }

    protected XMindNetRequest execute() {
        if (isAborted())
            return this;
        prepare();
        try {
            parseResponse(new HttpClient().executeMethod(method));
        } catch (Exception e) {
            parseResponse(ERROR);
        }
        return this;
    }

    public String getResponseText() {
        return responseText;
    }

    public String getResponseHeader(String name) {
        Header header = method.getResponseHeader(name);
        return header == null ? null : header.getValue();
    }

    public Throwable getException() {
        return exception;
    }

    protected void prepare() {
        setHeaders();

        String uri = this.uri;
        if (!params.isEmpty()) {
            if (method instanceof EntityEnclosingMethod) {
                RequestEntity entity = generateRequestEntity();
                if (entity != null) {
                    ((EntityEnclosingMethod) method).setRequestEntity(entity);
                }
            } else {
                uri = generateQueryString(uri, "?"); //$NON-NLS-1$
            }
        }

        StringBuffer sb = new StringBuffer();
        if (useHTTPS) {
            sb.append("https://"); //$NON-NLS-1$
        } else {
            sb.append("http://"); //$NON-NLS-1$
        }
        sb.append(domain);
        sb.append(uri);
        uri = sb.toString();

        if (debugging) {
            Activator.log(String.format(
                    "Request: method=%s, uri='%s', headers=%s", //$NON-NLS-1$ 
                    method.getClass().getSimpleName(), uri, headers));
        }

        try {
            method.setURI(new URI(uri, false));
        } catch (Exception e) {
            //should not happen
        }

    }

    private void setHeaders() {
        for (NameValuePair header : headers) {
            method.setRequestHeader(header.getName(), header.getValue());
        }
        if (method.getRequestHeader("Accept") == null) { //$NON-NLS-1$
            method.setRequestHeader("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private RequestEntity generateRequestEntity() {
        if (multipart) {
            List<Part> parts = new ArrayList<Part>(params.size());
            for (Parameter param : params) {
                if (param.value instanceof File) {
                    try {
                        parts.add(new FilePart(param.name, (File) param.value));
                    } catch (FileNotFoundException e) {
                    }
                } else {
                    parts.add(new StringPart(param.name, param.getValue(),
                            "utf-8")); //$NON-NLS-1$
                }
            }
            return new MultipartRequestEntity(parts.toArray(new Part[parts
                    .size()]), method.getParams());
        } else {
            String query = generateQueryString();
            try {
                return new ByteArrayRequestEntity(
                        query.getBytes("utf-8"), DEFAULT_CONTENT_TYPE); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                //should not happen
            }
        }
        return null;
    }

    protected String generateQueryString(String... prefix) {
        StringBuffer sb = new StringBuffer(params.size() * 15);
        for (String p : prefix) {
            sb.append(p);
        }
        for (int i = 0; i < params.size(); i++) {
            Parameter param = params.get(i);
            if (i > 0) {
                sb.append('&');
            }
            sb.append(urlEncode(param.name));
            sb.append('=');
            sb.append(urlEncode(param.getValue()));
        }
        return sb.toString();
    }

    protected void parseResponse(int rawCode) {
        this.code = rawCode;
        this.data = null;
        if (rawCode == ERROR) {
            this.responseText = null;
        } else {
            this.responseText = extractResponseText();
            if (this.responseText != null) {
                String respType = getResponseHeader("Content-Type"); //$NON-NLS-1$
                if (respType != null
                        && respType.indexOf("application/json") >= 0) { //$NON-NLS-1$
                    try {
                        this.data = new JSONStore(new JSONObject(
                                this.responseText));
                    } catch (JSONException e) {
                        exception = e;
                    }
                    if (this.data != null) {
                        if (rawCode == HttpStatus.SC_OK
                                && this.data.has("_code")) { //$NON-NLS-1$
                            this.code = this.data.getInt("_code"); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        if (debugging) {
            Activator.log(String.format("Response: code=%s, text='%s'", //$NON-NLS-1$ 
                    code, responseText));
        }
    }

    private String extractResponseText() {
        try {
            return method.getResponseBodyAsString();
        } catch (IOException e) {
            exception = e;
            return null;
        }
    }

    public XMindNetRequest debug() {
        this.debugging = true;
        return this;
    }

}
