package net.xmind.share.jobs;

import java.io.File;
import java.io.IOException;

import net.xmind.share.Info;
import net.xmind.share.XmindSharePlugin;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String RES_URL = "http://www.xmind.net/_fs/mapfile/"; //$NON-NLS-1$

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
     * Url: http://www.xmindshare.com/_res/upload/mapfile/{USERNAME}<br>
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
        try {
            debug("[prepare upload] userName=" + userName //$NON-NLS-1$ 
                    + ", token=" + authToken //$NON-NLS-1$
                    + ", title=" + title); //$NON-NLS-1$

            String url = makeUploadURL(userName);
            debug("[prepare upload] url=" + url); //$NON-NLS-1$

            PostMethod method = new PostMethod(url);
            method.setRequestHeader("Content-Type", //$NON-NLS-1$
                    "application/x-www-form-urlencoded; charset=UTF-8"); //$NON-NLS-1$
            setTokenAndJson(method, authToken);
            method.setParameter("title", title); //$NON-NLS-1$

            debug("[prepare upload] execute 'post'"); //$NON-NLS-1$
            HttpClient client = new HttpClient();
            int code = client.executeMethod(method);
            debug("[prepare upload] result code=" + code); //$NON-NLS-1$
            if (code != HttpStatus.SC_OK)
                return error(code);

            String resp = method.getResponseBodyAsString();
            debug("[prepare upload] response=" + resp); //$NON-NLS-1$
            JSONObject json = new JSONObject(resp);
            code = json.optInt("_code", -1); //$NON-NLS-1$
            if (code != HttpStatus.SC_OK)
                return error(code);

            this.sessionId = json.getString("session"); //$NON-NLS-1$
            this.permalink = json.optString("url", null); //$NON-NLS-1$
            String mapname = json.optString("mapname", null); //$NON-NLS-1$
            if (mapname != null) {
                this.viewLink = "http://www.xmind.net/xmind/map/" + userName //$NON-NLS-1$ 
                        + "/" + authToken + "/" + mapname; //$NON-NLS-1$ //$NON-NLS-2$
            }
            return ok();
        } catch (IOException e) {
            return error(e);
        } catch (JSONException e) {
            return error(e);
        }
    }

    /**
     * Upload file.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_res/upload/mapfile/{USERNAME}/{SESSION}<br>
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
        try {
            Part[] parts = new Part[] { new FilePart("map", sourceFile) }; //$NON-NLS-1$
            PostMethod method = new PostMethod(makeUploadURL(userName,
                    sessionId));
            method.setRequestEntity(new MultipartRequestEntity(parts, method
                    .getParams()));
            HttpClient client = new HttpClient();
            int code = client.executeMethod(method);
            if (code != HttpStatus.SC_OK) {
                return error(code);
            }
            return ok();
        } catch (IOException e) {
            return error(e);
        }
    }

    /**
     * Retrieve uploading progress.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_res/upload/mapfile/{USERNAME}/{SESSION}<br>
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
        try {
            GetMethod method = new GetMethod(makeUploadURL(userName, sessionId));
            setTokenAndJson(method, authToken);
            HttpClient client = new HttpClient();
            int code = client.executeMethod(method);
            if (code != HttpStatus.SC_OK) {
                return error(code);
            }
            JSONObject resp = new JSONObject(method.getResponseBodyAsString());
            if (resp.has("status")) { //$NON-NLS-1$
                String status = resp.getString("status"); //$NON-NLS-1$
                debug("[upload progress] status=" + status); //$NON-NLS-1$
                if ("forbidden".equals(status)) { //$NON-NLS-1$
                    return error(CODE_VERIFICATION_FAILURE);
                } else if ("error".equals(status)) { //$NON-NLS-1$
                    return error(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                } else if ("finished".equals(status)) { //$NON-NLS-1$
                    setStatus(COMPLETED);
                    return ok();
                }
            }
            uploadProgress = resp.getDouble("progress"); //$NON-NLS-1$
            return ok();
        } catch (IOException e) {
            return error(e);
        } catch (JSONException e) {
            return error(e);
        }
    }

    /**
     * Cancel uploading.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_res/upload/mapfile/{USERNAME}/{SESSION}<br>
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
        setStatus(CANCELING);
        try {
            DeleteMethod method = new DeleteMethod(makeUploadURL(userName,
                    sessionId));
            setTokenAndJson(method, authToken);
            HttpClient client = new HttpClient();
            int code = client.executeMethod(method);
            if (code != HttpStatus.SC_OK)
                return error(code);
            setStatus(CANCELED);
            return ok();
        } catch (IOException e) {
            return error(e);
        }
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
        setError(code, null);
        return termination;
    }

    private IStatus error(Throwable err) {
        setError(0, err);
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

    private static void debug(String message) {
        if (DEBUG) {
            XmindSharePlugin.log(message);
        }
    }

    /**
     * http://www.xmind.net/_fs/mapfile/{USERNAME}/{SESSION}
     */
    private static String makeUploadURL(String... paths) {
        return makeURL(RES_URL, paths);
    }

    public static String makeURL(String url, String... paths) {
        StringBuilder sb = new StringBuilder(url.length() + paths.length * 10);
        sb.append(url);
        for (String path : paths) {
            if (sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }
            sb.append(path);
        }
        return sb.toString();
    }

    public static void setTokenAndJson(HttpMethod method, String token) {
        method.setRequestHeader("AuthToken", token); //$NON-NLS-1$
        method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
