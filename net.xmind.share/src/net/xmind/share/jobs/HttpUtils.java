/*
 * Copyright (c) 2006-2008 XMind Ltd. and others.
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

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <h1>The whole process:</h1>
 * <p>
 * <h2>1. Before upload</h2>
 * </p>
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
 * </p>
 * <p>
 * <h2>2. Uploading file</h2>
 * </p>
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
 * </p>
 * <p>
 * <h2>3. Retrieve uploading progress</h2>
 * </p>
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
 * 404: Request file uploading progress is not found, maybe finished, canceled
 * or no exist.<br>
 * 200: OK<br>
 * read: {READ}<br>
 * total: {TOTAL}<br>
 * items: {ITEMS}<br>
 * progress: {PROGRESS} // 0 ~ 1
 * </p>
 * <p>
 * <h2>4. Cancel upload</h2>
 * </p>
 * <p>
 * [Request]<br>
 * Url: http://www.xmindshare.com/_res/upload/mapfile/{USERNAME}/{SESSION}<br>
 * Method: DELETE<br>
 * Headers: AuthToken
 * </p>
 * <p>
 * [Response]<br>
 * 400: Url is invalid 403: User is allowed only to cancel his own file upload
 * process.<br>
 * 200: OK
 * </p>
 * 
 * @author MANGOSOFT
 * 
 */
public class HttpUtils {

    private static final boolean DEBUG = false;

    /**
     * http://www.xmindshare.com/_res/upload/mapfile/{USERNAME}/{SESSION}
     */
    private static final String RES_URL = "http://share.xmind.net/_res/upload/mapfile/"; //$NON-NLS-1$

    private HttpUtils() {
    }

    private static void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    /**
     * Start upload, retrieve session and url.
     * 
     * @param client
     * @param token
     * @return [{SESSION}, {URL}]
     * @throws IOException
     * @throws HttpException
     * @throws JSONException
     */
    public static String[] prepareUpload(HttpClient client, String userName,
            String token, String title) throws HttpException, IOException,
            JSONException {
        debug("[prepare upload] userName=" + userName //$NON-NLS-1$ 
                + ", token=" + token //$NON-NLS-1$
                + ", title=" + title); //$NON-NLS-1$

        String postURL = mergeUrl(userName);
        debug("[prepare upload] url=" + postURL); //$NON-NLS-1$

        PostMethod method = new PostMethod(postURL);
        method.setRequestHeader("Content-Type", //$NON-NLS-1$
                "application/x-www-form-urlencoded; charset=UTF-8"); //$NON-NLS-1$
        setTokenAndJson(method, token);
        method.setParameter("title", title); //$NON-NLS-1$

        debug("[prepare upload] execute 'post'"); //$NON-NLS-1$
        int result = client.executeMethod(method);
        debug("[prepare upload] result=" + result); //$NON-NLS-1$
        if (result != HttpStatus.SC_OK)
            throw new HttpException("Failed to upload a map to server"); //$NON-NLS-1$
        String response = method.getResponseBodyAsString();
        debug("[prepare upload] response=" + response); //$NON-NLS-1$
        JSONObject json = new JSONObject(response);

        String[] data = new String[3];
        data[0] = json.getString("session"); //$NON-NLS-1$
        if (json.has("url")) { //$NON-NLS-1$
            data[1] = json.getString("url"); //$NON-NLS-1$
        }
        if (json.has("mapname")) { //$NON-NLS-1$
            data[2] = json.getString("mapname"); //$NON-NLS-1$
        }
        return data;
    }

    /**
     * Upload file
     * 
     * @param client
     * @param session
     * @param parts
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public static int uploadFile(HttpClient client, String userName,
            String session, Part[] parts) throws HttpException, IOException {
        PostMethod method = new PostMethod(mergeUrl(userName, session));
        method.setRequestEntity(new MultipartRequestEntity(parts, method
                .getParams()));
        return client.executeMethod(method);
    }

    public static double retrieveUploadingProcess(HttpClient client,
            String userName, String session, String token)
            throws HttpException, IOException, JSONException {
        GetMethod method = new GetMethod(mergeUrl(userName, session));
        setTokenAndJson(method, token);
        if (HttpStatus.SC_OK == client.executeMethod(method)) {
            JSONObject result = new JSONObject(method.getResponseBodyAsString());
            return result.getDouble("progress"); //$NON-NLS-1$
        } else {
            return -1;
        }
    }

    public static void cancelUploading(HttpClient client, String userName,
            String session, String token) throws HttpException, IOException {
        DeleteMethod method = new DeleteMethod(mergeUrl(userName, session));
        setTokenAndJson(method, token);
        client.executeMethod(method);
    }

//    public static String getUploadSession(HttpClient client, String token)
//            throws HttpException, IOException, JSONException {
//        JSONObject ret = startUpload(client, token);
//        return ret.getString("session"); //$NON-NLS-1$
//    }

    private static void setTokenAndJson(HttpMethod method, String token) {
        method.setRequestHeader("AuthToken", token); //$NON-NLS-1$
        method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String mergeUrl(String... paths) {
        StringBuilder sb = new StringBuilder(RES_URL.length() + paths.length
                * 10);
        sb.append(RES_URL);
        for (String path : paths) {
            if (sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }
            sb.append(path);
        }
        return sb.toString();
    }

    /**
     * @deprecated
     * @param client
     * @param email
     * @param password
     * @return
     * @throws IOException
     * @throws HttpException
     * @throws JSONException
     */
    public static JSONObject testSignIn(HttpClient client, String email,
            String password) throws IOException, HttpException, JSONException {
        // SignIn
        PostMethod method = new PostMethod(RES_URL + "sign-in"); //$NON-NLS-1$
        NameValuePair[] data = { //new NameValuePair("_method", "put"),
        new NameValuePair("email", email), //$NON-NLS-1$
                new NameValuePair("password", password) }; //$NON-NLS-1$
        method.setRequestBody(data);
        method.setRequestHeader("accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
        client.executeMethod(method);
        String body = method.getResponseBodyAsString();
        return new JSONObject(body);
    }

    /**
     * @deprecated
     * @return
     * @throws Exception
     */
    public static String testSignIn() throws Exception {
        String email = "rz2@xmind.org"; //$NON-NLS-1$
        String password = "test"; //$NON-NLS-1$
        HttpClient client = new HttpClient();
        JSONObject user = testSignIn(client, email, password);
        return user.getString("token"); //$NON-NLS-1$
    }

//    /**
//     * @deprecated
//     * @param file
//     * @throws HttpException
//     * @throws IOException
//     * @throws JSONException
//     */
//    public static void testUpload(File file) throws HttpException, IOException,
//            JSONException {
//        String email = "rz2@xmind.org"; //$NON-NLS-1$
//        String password = "test"; //$NON-NLS-1$
//
//        // Create HttpClient
//        HttpClient client = new HttpClient();
//
//        // SignIn
//        JSONObject user = testSignIn(client, email, password);
//        String token = user.getString("token"); //$NON-NLS-1$
//
//        // Get Upload Session
//        JSONObject map = startUpload(client, token);
//        String session = map.getString("session"); //$NON-NLS-1$
//
//        int count = 1;
//        // Start file uploading test.
//        Part[] parts = new Part[] { new FilePart("map", file) }; //$NON-NLS-1$
//
//        // Success.
//        if (uploadFile(client, session, parts) != HttpStatus.SC_OK) {
//            System.out.println(count + " failed"); //$NON-NLS-1$
//        }
//        count++;
//    }

}