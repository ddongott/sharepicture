package com.example.ddong.xphoto;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ddong on 2015-09-29.
 */
public class HttpHelper {
    private static final String TAG = "HttpHelper";
    private static final String URL_SERVER = "http://192.168.0.108:8000/";
    private static final String URL_FACEBOOKSIGNUP = URL_SERVER + "facebook-signup/";
    private static final String URL_SHAREMANAGER = URL_SERVER + "sharemanager/";
    private static final String URL_USER = URL_SERVER + "users/";
    private static final HttpHelper instance = new HttpHelper();
    private Context mContext;
    private AsyncTask<String, Void, Void> mPendingTask = null;
    private static java.net.CookieManager msCookieManager = new java.net.CookieManager();

    // Private constructor prevents instantiation from other classes
    private HttpHelper() { }

    public static HttpHelper getInstance() {
        return instance;
    }

    public void setAppContext(Context context) {
        mContext = context;
    }

    public void facebookLogin(AccessToken token) {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        Log.d(TAG, "postUserInfo. get networkInfo: " + networkInfo.toString());

        if (networkInfo != null && networkInfo.isConnected()) {
            JSONObject object = new JSONObject();
            try {
                //object.put("uuid", mSharePrefHelper.getUserUUID());
                //object.put("username","yaya");
                //object.put("password","1234");
                //object.put("facebookid", mSharePrefHelper.getFacebookId());
                object.put("gcm_token", SharePrefHelper.getInstance().getGcmToken());
                Log.d(TAG, "gcm token: " + SharePrefHelper.getInstance().getGcmToken());
                object.put("access_token",token.getToken());
                Log.d(TAG, "Facebook token: " + token.getToken());
            } catch (JSONException e) {
                Log.d(TAG, "Exception when posting user info");
            }

            HttpTaskCallback callback = new HttpTaskCallback() {
                @Override
                public void onTaskCompleted(JSONObject results) {
                    AccountManager.getInstance().loginSuccess(results);
                }
            };

            Log.d(TAG, "Network connection available.");
            HttpTaskParams params = new HttpTaskParams("POST", URL_FACEBOOKSIGNUP, object);
            new PostJsonTask(callback).execute(params);
        } else {
            Log.e(TAG, "No network connection available.");
        }
    }

    public void updateGCMToken(String gcmtoken) {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        Log.d(TAG, "updateGCMToken. get networkInfo: " + networkInfo.toString());

        if (networkInfo != null && networkInfo.isConnected()) {
            JSONObject object = new JSONObject();
            SharePrefHelper sharepreference = SharePrefHelper.getInstance();

            try {
                //object.put("username", sharepreference.getUserName());
                //object.put("first_name",sharepreference.getFirstName());
                //object.put("last_name",sharepreference.getLastName());
                //object.put("email", sharepreference.getEmails());
                object.put("gcm_token", gcmtoken);
                String user_id = sharepreference.getUserID();
                String url = URL_USER + user_id + "/";
                Log.d(TAG, "updateGCMToken gcm token: " + gcmtoken);
                HttpTaskParams params = new HttpTaskParams("PATCH", url, object);
                new PostJsonTask(null).execute(params);
            } catch (JSONException e) {
                Log.d(TAG, "Exception when posting user info");
            }
        } else {
            Log.e(TAG, "updateGCMToken No network connection available.");
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class PostJsonTask extends AsyncTask<HttpTaskParams, Void, JSONObject> {
        public HttpTaskCallback mCallback = null;

        public PostJsonTask(HttpTaskCallback callback) {
            mCallback = callback;
        }

        @Override
        protected JSONObject doInBackground(HttpTaskParams... params) {
            // params comes from the execute() call: params[0] is the url.
            Log.d(TAG, "PostJsonTask url: " + params[0].url);
            return sendJson(params[0].method, params[0].url, params[0].jobj);
        }

        @Override
        protected void onPostExecute(JSONObject result)
        {
            // Call activity method with results
            if (mCallback != null) {
                mCallback.onTaskCompleted(result);
            }
        }
    }

    private static class HttpTaskParams {
        String method;
        String url;
        JSONObject jobj;

        HttpTaskParams(String method, String url, JSONObject obj) {
            this.method = method;
            this.url = url;
            this.jobj = obj;
        }
    }

    public JSONObject sendJson(String method, String request, JSONObject jsonObject) {
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        //String otherParametersUrServiceNeed =  "Company=acompany&Lng=test&MainPeriod=test&UserID=123&CourseDate=8:10:10";
        Log.d(TAG, "sendJson url: " + request);

        try {
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            connection.setUseCaches(false);
            Log.d(TAG, "sendJson: length: " + jsonObject.toString().length());
            connection.setFixedLengthStreamingMode(jsonObject.toString().length());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            //String basicAuth = "Basic " + new String(Base64.encode("ddong:njtu-579".getBytes(), Base64.NO_WRAP ));
            //connection.setRequestProperty ("Authorization", basicAuth);
            //connection.setRequestProperty("Host", "192.168.0.111");
            if(msCookieManager.getCookieStore().getCookies().size() > 0)
            {
                Log.d(TAG,"Add cookie for the connection: " + TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                connection.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));

                HttpCookie csrfCookie = null;
                for (HttpCookie cookie : msCookieManager.getCookieStore().getCookies()) {
                    Log.d(TAG,"get cookie: " + cookie.getName());
                    if (cookie.getName().equals("csrftoken")) {
                        Log.d(TAG,"get csrftoken: ");
                        csrfCookie = cookie;
                        break;
                    }
                }

                if(csrfCookie != null) {
                    Log.d(TAG,"Add X-CSRFToken: " + csrfCookie.getValue());
                    connection.setRequestProperty("X-CSRFToken", csrfCookie.getValue());
                }
            }

            Log.d(TAG, "sendJson: connectting: ... ");

            connection.connect();
            //connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            Log.d(TAG, "sendJson: connected: ");

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            int HttpResult = connection.getResponseCode();
            if(HttpResult == HttpURLConnection.HTTP_OK){
                Map<String, List<String>> headerFields = connection.getHeaderFields();
                List<String> cookiesHeader = headerFields.get("Set-Cookie");

                if(cookiesHeader != null)
                {
                    for (String cookie : cookiesHeader)
                    {
                        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                    Log.d(TAG, cookiesHeader.toString());
                }

                //read response
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();
                String line;

                while ((line = rd.readLine()) != null) {
                    sb.append(line + '\n');
                }
                Log.d(TAG, sb.toString());
                JSONObject jobj = new JSONObject(sb.toString());

                return jobj;
            }else{
                if(mPendingTask != null) {
                    mPendingTask.cancel(true);
                }
                Log.d(TAG,connection.getResponseMessage());
            }
        }
        catch (MalformedURLException e) {
            if(mPendingTask != null) {
                mPendingTask.cancel(true);
            }
            Log.d(TAG, "Exception: " + e.toString());
        }
        catch (IOException e) {
            if(mPendingTask != null) {
                mPendingTask.cancel(true);
            }
            Log.d(TAG, "Exception: " + e.toString());
        }
        catch (JSONException e) {
            if(mPendingTask != null) {
                mPendingTask.cancel(true);
            }
            Log.d(TAG, "Exception: " + e.toString());
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    public void sendPhoto(String email, String path) {
        AccountManager am = AccountManager.getInstance();
        if(!am.getLoginStatus()) {
            am.login();
        }
        new SendPhotoTask().execute(email, path);
    }

    private class SendPhotoTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            mPendingTask = this;
            //make sure login successfully
            while (!AccountManager.getInstance().getLoginStatus()) {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            String email = params[0];
            String path = params[1];
            String charset = "UTF-8";
            Log.d(TAG, "File to be uploaded: " + path);
            Log.d(TAG, "email to be share: " + email);


            File uploadFile = new File(path);

            try {
                MultipartUtility multipart = new MultipartUtility(URL_SHAREMANAGER, charset);

                multipart.addFormField("shareto", "UNKOWN");
                multipart.addFormField("sharetoemail", email);
                multipart.addFormField("expire", "1998-02-02T12:22:00Z");
                multipart.addFilePart("data", uploadFile);

                List<String> response = multipart.finish();

                System.out.println("SERVER REPLIED:");

                for (String line : response) {
                    System.out.println(line);
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }

            return null;
        }

        @Override
        protected void onCancelled(Void result) {
            Log.d(TAG, "onCancelled");
            mPendingTask = null;
        }
    }


    /**
     * This utility class provides an abstraction layer for sending multipart HTTP
     * POST requests to a web server.
     * @author www.codejava.net
     *
     */
    private class MultipartUtility {
        private final String boundary;
        private static final String LINE_FEED = "\r\n";
        private HttpURLConnection httpConn;
        private String charset;
        private OutputStream outputStream;
        private PrintWriter writer;

        /**
         * This constructor initializes a new HTTP POST request with content type
         * is set to multipart/form-data
         * @param requestURL
         * @param charset
         * @throws IOException
         */
        public MultipartUtility(String requestURL, String charset)
                throws IOException {
            this.charset = charset;

            // creates a unique boundary based on time stamp
            boundary = "===" + System.currentTimeMillis() + "===";

            URL url = new URL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setConnectTimeout(30000);
            httpConn.setReadTimeout(30000);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
            if(msCookieManager.getCookieStore().getCookies().size() > 0)
            {
                Log.d(TAG,"Add cookie for the connection: " + TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                httpConn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));

                HttpCookie csrfCookie = null;
                for (HttpCookie cookie : msCookieManager.getCookieStore().getCookies()) {
                    Log.d(TAG,"get cookie: " + cookie.getName());
                    if (cookie.getName().equals("csrftoken")) {
                        Log.d(TAG,"get csrftoken: ");
                        csrfCookie = cookie;
                        break;
                    }
                }

                if(csrfCookie != null) {
                    Log.d(TAG,"Add X-CSRFToken: " + csrfCookie.getValue());
                    httpConn.setRequestProperty("X-CSRFToken", csrfCookie.getValue());
                }
            }
            //String basicAuth = "Basic " + new String(Base64.encode("ddong:njtu-579".getBytes(), Base64.NO_WRAP ));
            //httpConn.setRequestProperty("Authorization", AccountManager.getInstance().getAccessToken().toString());
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);

            outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                    true);
        }

        /**
         * Adds a form field to the request
         * @param name field name
         * @param value field value
         */
        public void addFormField(String name, String value) {
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                    .append(LINE_FEED);
            writer.append("Content-Type: text/plain; charset=" + charset).append(
                    LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(value).append(LINE_FEED);
            writer.flush();
        }

        /**
         * Adds a upload file section to the request
         * @param fieldName name attribute in <input type="file" name="..." />
         * @param uploadFile a File to be uploaded
         * @throws IOException
         */
        public void addFilePart(String fieldName, File uploadFile)
                throws IOException {
            String fileName = uploadFile.getName();
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append(
                    "Content-Disposition: form-data; name=\"" + fieldName
                            + "\"; filename=\"" + fileName + "\"")
                    .append(LINE_FEED);
            writer.append(
                    "Content-Type: "
                            + URLConnection.guessContentTypeFromName(fileName))
                    .append(LINE_FEED);
            Log.d(TAG,"Content-Type: " + URLConnection.guessContentTypeFromName(fileName));
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

            writer.append(LINE_FEED);
            writer.flush();
        }

        /**
         * Adds a header field to the request.
         * @param name - name of the header field
         * @param value - value of the header field
         */
        public void addHeaderField(String name, String value) {
            writer.append(name + ": " + value).append(LINE_FEED);
            writer.flush();
        }

        /**
         * Completes the request and receives response from the server.
         * @return a list of Strings as response in case the server returned
         * status OK, otherwise an exception is thrown.
         * @throws IOException
         */
        public List<String> finish() throws IOException {
            List<String> response = new ArrayList<String>();

            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();

            // checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_CREATED) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpConn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
                reader.close();
                httpConn.disconnect();
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }

            return response;
        }
    }

    public void downloadMessage(String id, HttpTaskCallback callback) {
        Log.d(TAG,"downloadMessage: " + id);
        AccountManager am = AccountManager.getInstance();
        if(!am.getLoginStatus()) {
            am.login();
        }
        String path = URL_SHAREMANAGER + id + "/";
        new GetPhotoTask(callback).execute(path);
    }

    private class GetPhotoTask extends AsyncTask<String, Void, JSONObject> {
        public HttpTaskCallback mCallback = null;

        public GetPhotoTask(HttpTaskCallback callback) {
            mCallback = callback;
        }
        @Override
        protected void onPreExecute() {
            //make sure login successfully
            while (!AccountManager.getInstance().getLoginStatus()) {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            String request = params[0];

            HttpURLConnection connection = null;
            BufferedReader rd  = null;
            StringBuilder sb = null;
            String line = null;
            //String otherParametersUrServiceNeed =  "Company=acompany&Lng=test&MainPeriod=test&UserID=123&CourseDate=8:10:10";
            Log.d(TAG, "Get url: " + request);

            try {
                URL url = new URL(request);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                if(msCookieManager.getCookieStore().getCookies().size() > 0)
                {
                    Log.d(TAG,"Add cookie for the connection: " + TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
                    //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                    connection.setRequestProperty("Cookie",
                            TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));

                    HttpCookie csrfCookie = null;
                    for (HttpCookie cookie : msCookieManager.getCookieStore().getCookies()) {
                        Log.d(TAG,"get cookie: " + cookie.getName());
                        if (cookie.getName().equals("csrftoken")) {
                            Log.d(TAG,"get csrftoken: ");
                            csrfCookie = cookie;
                            break;
                        }
                    }

                    if(csrfCookie != null) {
                        Log.d(TAG,"Add X-CSRFToken: " + csrfCookie.getValue());
                        connection.setRequestProperty("X-CSRFToken", csrfCookie.getValue());
                    }
                }
                Log.d(TAG, "GetPhotoTask: connectting: ... ");

                connection.connect();
                //connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                Log.d(TAG, "GetPhotoTask: connected: ");
                int HttpResult = connection.getResponseCode();
                if(HttpResult == HttpURLConnection.HTTP_OK) {
                    rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    sb = new StringBuilder();

                    while ((line = rd.readLine()) != null) {
                        sb.append(line + '\n');
                    }
                    Log.d(TAG, sb.toString());
                    JSONObject jobj = new JSONObject(sb.toString());
                    return jobj;
                }
            }
            catch (MalformedURLException e) {
                Log.d(TAG, "Exception: " + e.toString());
            }
            catch (IOException e) {
                Log.d(TAG, "Exception: " + e.toString());
            }
            catch (JSONException e) {
                Log.d(TAG, "Exception: " + e.toString());
            }

            finally {
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result)
        {
            // Call activity method with results
            if (mCallback != null) {
                mCallback.onTaskCompleted(result);
            }
        }

	}

    public void downloadData(String url) {
        new DownloadTask().execute(url);
    }

    private class DownloadTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            // params comes from the execute() call: params[0] is the url.
            Log.d(TAG, "PostJsonTask url: " + url);
            try {
                downloadFile(url);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
          * Downloads a file from a URL
          * @param fileURL HTTP URL of the file to be downloaded
          * @param saveDir path of the directory to save the file
          * @throws IOException
          */
    private void downloadFile(String fileURL)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                    fileURL.length());
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            // opens an output stream to save into file
            FileOutputStream outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    public interface HttpTaskCallback {
        public void onTaskCompleted(JSONObject results);
    }
}
