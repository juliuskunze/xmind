package net.xmind.share.jobs;

import java.io.File;
import java.io.IOException;

import net.xmind.share.Info;
import net.xmind.share.XmindSharePlugin;
import net.xmind.signin.IDataStore;
import net.xmind.signin.internal.XMindNetRequest;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.json.JSONException;

/**
 * The whole process:
 * <ol>
 * <li>Retrieve upload session</li>
 * <li>Upload file</li>
 * <li>Retrieve uploading progress</li>
 * <li>Cancel uploading</li>
 * </ol>
 * 
 * @author Frank Shaka
 * 
 */
public class UploadSession {

    public static final int INITIALIZED = 0;

    public static final int PREPARING = 1;

    public static final int UPLOADING = 2;

    public static final int COMPLETED = 3;

    public static final int CANCELING = 4;

    public static final int CANCELED = 5;

    public static final int CODE_VERIFICATION_FAILURE = 1001;

    private static final boolean DEBUG = System
            .getProperty("org.xmind.debug.share") != null; //$NON-NLS-1$

    private static final String UPLOAD_API = "/_fs/mapfile/%s"; //$NON-NLS-1$
    private static final String SESSION_API = "/_fs/mapfile/%s/%s"; //$NON-NLS-1$
//    private static final String RES_URL = "http://www.xmind.net/_fs/mapfile/"; //$NON-NLS-1$
//    private static final String RES_URL = "http://172.16.231.141:8080/upload/"; //$NON-NLS-1$

    private String userName;

    private String authToken;

    private String title;

    private File sourceFile;

    private String sessionId;

    private int status;

    private IStatus termination;

    private String permalink;

    private String viewLink;

    private double uploadProgress;

    private XMindNetRequest prepareRequest = null;

    private XMindNetRequest transferRequest = null;

    private XMindNetRequest retrieveProgressRequest = null;

    public UploadSession(Info info) {
        this.userName = info.getString(Info.USER_ID);
        Assert.isNotNull(this.userName);
        this.authToken = info.getString(Info.TOKEN);
        Assert.isNotNull(this.authToken);
        this.title = info.getString(Info.TITLE);
        Assert.isNotNull(this.title);
        this.sourceFile = (File) info.getProperty(Info.FILE);
        Assert.isNotNull(this.sourceFile);
        setStatus(INITIALIZED);
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getViewLink() {
        return viewLink;
    }

    public double getUploadProgress() {
        return uploadProgress;
    }

    public int getStatus() {
        return status;
    }

    public IStatus getError() {
        return termination;
    }

    public boolean hasError() {
        return termination != null && !termination.isOK();
    }

    /**
     * Retrieve upload session.
     * <p>
     * [Request:]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}<br>
     * Method: POST<br>
     * Headers: AuthToken<br>
     * Params: title:{TITLE}<br>
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid, or the required fields not present in request.<br>
     * 401: User must signin.<br>
     * 403: User is allowed only to upload file for themselves.<br>
     * 500: Server error.<br>
     * 200: OK<br>
     * session: {SESSION}<br>
     * url: {URL}
     * 
     * @throws IOException
     * @throws JSONException
     */
    public IStatus prepare() {
        setStatus(PREPARING);
        debug("[upload][prepare] userName=%s, authToken=%s, title=%s", //$NON-NLS-1$ 
                userName, authToken, title);

        XMindNetRequest request = new XMindNetRequest();
        if (DEBUG)
            request.debug();
        prepareRequest = request;
        request.uri(UPLOAD_API, userName);
        request.setAuthToken(authToken);
        request.addParameter("title", title); //$NON-NLS-1$
        request.post();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getCode();
        IDataStore data = request.getData();

        debug("[upload][prepare] response code=%s", code); //$NON-NLS-1$
        debug("[upload][prepare] response text='%s'", request.getResponseText()); //$NON-NLS-1$

        if (code == HttpStatus.SC_OK && data != null) {
            String sessionId = data.getString("session"); //$NON-NLS-1$
            if (sessionId == null)
                return error(500);
            this.sessionId = sessionId;
            this.permalink = data.getString("url"); //$NON-NLS-1$
            String mapname = data.getString("mapname"); //$NON-NLS-1$
            if (mapname != null) {
                this.viewLink = String.format(
                        "http://www.xmind.net/xmind/map/%s/%s/%s", //$NON-NLS-1$
                        userName, authToken, mapname);
            }
            return ok();
        }

        return error(code, request.getException());

//        try {
//            String url = makeUploadURL(userName);
//            debug("[prepare upload] url=" + url); //$NON-NLS-1$
//
//            HttpClient client = new HttpClient();
//            request.setURI(new URI(url, true, client.getParams()
//                    .getUriCharset()));
//            request.setRequestHeader("Content-Type", //$NON-NLS-1$
//                    "application/x-www-form-urlencoded; charset=UTF-8"); //$NON-NLS-1$
//            setTokenAndJson(request, authToken);
//            request.setParameter("title", title); //$NON-NLS-1$
//
//            debug("[prepare upload] execute 'post'"); //$NON-NLS-1$
//            int code = client.executeMethod(request);
//            debug("[prepare upload] result code=" + code); //$NON-NLS-1$
//            if (code != HttpStatus.SC_OK)
//                return error(code);
//
//            String resp = request.getResponseBodyAsString();
//            debug("[prepare upload] response=" + resp); //$NON-NLS-1$
//            JSONObject json = new JSONObject(resp);
//            code = json.optInt("_code", -1); //$NON-NLS-1$
//            if (code != HttpStatus.SC_OK)
//                return error(code);
//
//            this.sessionId = json.getString("session"); //$NON-NLS-1$
//            this.permalink = json.optString("url", null); //$NON-NLS-1$
//            String mapname = json.optString("mapname", null); //$NON-NLS-1$
//            if (mapname != null) {
//                this.viewLink = "http://www.xmind.net/xmind/map/" + userName //$NON-NLS-1$ 
//                        + "/" + authToken + "/" + mapname; //$NON-NLS-1$ //$NON-NLS-2$
//            }
//            return ok();
//        } catch (IOException e) {
//            return error(e);
//        } catch (JSONException e) {
//            return error(e);
//        }
    }

    /**
     * Upload file.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}/{SESSION}<br>
     * Method: POST<br>
     * Params: map:{MAP FILE}
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid, or request is not valid file upload request, or User
     * cancel uploading process.<br>
     * 500: Server error.<br>
     * 200: OK
     * 
     * @throws IOException
     */
    public IStatus transfer() {
        setStatus(UPLOADING);

        debug("[upload][transfer] userName=%s, sessionId=%s, sourceFile=%s", //$NON-NLS-1$
                userName, sessionId, sourceFile.getAbsolutePath());

        XMindNetRequest request = new XMindNetRequest().multipart();
        if (DEBUG)
            request.debug();
        transferRequest = request;
        request.uri(SESSION_API, userName, sessionId);
        request.addParameter("map", sourceFile); //$NON-NLS-1$
        request.post();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getCode();
        debug("[upload][transfer] response code=%s", code); //$NON-NLS-1$
        debug("[upload][transfer] response text=%s", request.getResponseText()); //$NON-NLS-1$

        if (code == HttpStatus.SC_OK)
            return ok();

        return error(code, request.getException());
//        
//        try {
//            Part[] parts = new Part[] { new FilePart("map", sourceFile) }; //$NON-NLS-1$
//            String url = makeUploadURL(userName, sessionId);
//            HttpClient client = new HttpClient();
//            request.setURI(new URI(url, true, client.getParams()
//                    .getUriCharset()));
//            request.setRequestEntity(new MultipartRequestEntity(parts, request
//                    .getParams()));
//            int code = client.executeMethod(request);
//            if (code != HttpStatus.SC_OK) {
//                return error(code);
//            }
//            return ok();
//        } catch (IOException e) {
//            return error(e);
//        }
    }

    /**
     * Retrieve uploading progress.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}/{SESSION}<br>
     * Method: GET<br>
     * Headers: AuthToken
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid<br>
     * 403: User is allowed only view its own file uploading progress.<br>
     * 404: Request file uploading progress is not found, maybe finished,
     * canceled or no exist.<br>
     * 200: OK<br>
     * status: <br>
     * read: {READ}<br>
     * total: {TOTAL}<br>
     * items: {ITEMS}<br>
     * progress: {PROGRESS} // 0 ~ 1
     * 
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public IStatus retrieveProgress() {
        debug("[upload][progress] userName=%s, sessionId=%s", //$NON-NLS-1$
                userName, sessionId);

        XMindNetRequest request = new XMindNetRequest();
        if (DEBUG)
            request.debug();
        retrieveProgressRequest = request;
        request.uri(SESSION_API, userName, sessionId);
        request.setAuthToken(authToken);
        request.get();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getCode();
        IDataStore data = request.getData();
        debug("[upload][progress] response code=%s", code); //$NON-NLS-1$
        debug("[upload][progress] response text=%s", request.getResponseText()); //$NON-NLS-1$

        if (code == HttpStatus.SC_OK && data != null) {
            String status = data.getString("status"); //$NON-NLS-1$
            if (status != null) {
                debug("[upload][progress] status=%s", status); //$NON-NLS-1$
                if ("forbidden".equals(status)) { //$NON-NLS-1$
                    return error(CODE_VERIFICATION_FAILURE);
                } else if ("error".equals(status)) { //$NON-NLS-1$
                    return error(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                } else if ("finished".equals(status)) { //$NON-NLS-1$
                    setStatus(COMPLETED);
                    return ok();
                }
            }
            uploadProgress = data.getDouble("progress"); //$NON-NLS-1$
            return ok();
        }

        return error(code, request.getException());

//        try {
//            HttpClient client = new HttpClient();
//            request.setURI(new URI(makeUploadURL(userName, sessionId), true,
//                    client.getParams().getUriCharset()));
//            setTokenAndJson(request, authToken);
//            int code = client.executeMethod(request);
//            if (code != HttpStatus.SC_OK) {
//                return error(code);
//            }
//            String respBody = request.getResponseBodyAsString();
//            debug("[upload session info] " + respBody); //$NON-NLS-1$
//            JSONObject resp = new JSONObject(respBody);
//            if (resp.has("status")) { //$NON-NLS-1$
//                String status = resp.getString("status"); //$NON-NLS-1$
//                debug("[upload progress] status=" + status); //$NON-NLS-1$
//                if ("forbidden".equals(status)) { //$NON-NLS-1$
//                    return error(CODE_VERIFICATION_FAILURE);
//                } else if ("error".equals(status)) { //$NON-NLS-1$
//                    return error(HttpStatus.SC_INTERNAL_SERVER_ERROR);
//                } else if ("finished".equals(status)) { //$NON-NLS-1$
//                    setStatus(COMPLETED);
//                    return ok();
//                }
//            }
//            uploadProgress = resp.getDouble("progress"); //$NON-NLS-1$
//            return ok();
//        } catch (IOException e) {
//            return error(e);
//        } catch (JSONException e) {
//            return error(e);
//        }
    }

    /**
     * Cancel uploading.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}/{SESSION}<br>
     * Method: DELETE<br>
     * Headers: AuthToken
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid<br>
     * 403: User is allowed only to cancel his own file<br>
     * upload process.<br>
     * 200: OK
     * 
     * @throws IOException
     */
    public IStatus cancel() {
        if (prepareRequest != null)
            prepareRequest.abort();
        if (transferRequest != null)
            transferRequest.abort();
        if (retrieveProgressRequest != null)
            retrieveProgressRequest.abort();

        debug("[upload][cancel] userName=%s, sessionId=%s", //$NON-NLS-1$
                userName, sessionId);

        if (sessionId == null)
            return Status.CANCEL_STATUS;

        setStatus(CANCELING);
        XMindNetRequest request = new XMindNetRequest();
        if (DEBUG)
            request.debug();
        request.uri(SESSION_API, userName, sessionId);
        request.setAuthToken(authToken);
        request.delete();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getCode();
        debug("[upload][cancel] response code=%s", code); //$NON-NLS-1$
        debug("[upload][cancel] response text=%s", request.getResponseText()); //$NON-NLS-1$

        if (code == HttpStatus.SC_OK) {
            setStatus(CANCELED);
            return ok();
        }

        return error(code, request.getException());

//        try {
//            DeleteMethod method = new DeleteMethod(makeUploadURL(userName,
//                    sessionId));
//            setTokenAndJson(method, authToken);
//            HttpClient client = new HttpClient();
//            int code = client.executeMethod(method);
//            if (code != HttpStatus.SC_OK)
//                return error(code);
//            setStatus(CANCELED);
//            return ok();
//        } catch (IOException e) {
//            return error(e);
//        }
    }

    private void setStatus(int status) {
        this.status = status;
    }

    private IStatus ok() {
        if (termination == null)
            termination = Status.OK_STATUS;
        return termination;
    }

    private IStatus error(int code) {
        return error(code, null);
    }

    private IStatus error(int code, Throwable error) {
        setError(code, error);
        return termination;
    }

    private void setError(int code, Throwable err) {
        if (hasError() || status == COMPLETED || status == CANCELED)
            return;
        termination = createTermination(code, err);
    }

    private static IStatus createTermination(int code, Throwable err) {
        return new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID, code,
                null, err);
    }

    private static void debug(String message, Object... values) {
        if (DEBUG) {
            XmindSharePlugin.log(String.format(message, values));
        }
    }

//    /**
//     * http://www.xmind.net/_fs/mapfile/{USERNAME}/{SESSION}
//     */
//    private static String makeUploadURL(String... paths) {
//        return makeURL(RES_URL, paths);
//    }
//
//    public static String makeURL(String url, String... paths) {
//        StringBuilder sb = new StringBuilder(url.length() + paths.length * 10);
//        sb.append(url);
//        for (String path : paths) {
//            if (sb.charAt(sb.length() - 1) != '/') {
//                sb.append('/');
//            }
//            try {
//                sb.append(URLEncoder.encode(path, "UTF-8")); //$NON-NLS-1$
//            } catch (UnsupportedEncodingException e) {
//                sb.append(path);
//            }
//        }
//        return sb.toString();
//    }
//
//    public static void setTokenAndJson(HttpMethod method, String token) {
//        method.setRequestHeader("AuthToken", token); //$NON-NLS-1$
//        method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
//    }

}
