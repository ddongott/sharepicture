package com.example.ddong.xphoto;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddong on 2015-09-29.
 */
public class HttpHelper {
    private static final String TAG = "HttpHelper";
    private static final String URL_USERMANAGER = "http://192.168.0.111:8000/snippets/";
    private static final String URL_FACEBOOKSIGNUP = "http://192.168.0.111:8000/facebook-signup/";
    private static final String URL_SHAREMANAGER = "http://10.106.238.200:8000/sharemanager/";
    private static final HttpHelper instance = new HttpHelper();
    private Context mContext;

    // Private constructor prevents instantiation from other classes
    private HttpHelper() { }

    public static HttpHelper getInstance() {
        return instance;
    }

    public void setAppContext(Context context) {
        mContext = context;
    }

    public void postUserInfo(JSONObject jsonObject) {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        Log.d(TAG, "postUserInfo. get networkInfo: " + networkInfo.toString());

        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "Network connection available.");
            HttpTaskParams params = new HttpTaskParams(URL_USERMANAGER, jsonObject);
            new PostJsonTask().execute(params);
        } else {
            Log.e(TAG, "No network connection available.");
        }
    }

    public void facebookLogin(JSONObject jsonObject) {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        Log.d(TAG, "postUserInfo. get networkInfo: " + networkInfo.toString());

        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "Network connection available.");
            HttpTaskParams params = new HttpTaskParams(URL_FACEBOOKSIGNUP, jsonObject);
            new PostJsonTask().execute(params);
        } else {
            Log.e(TAG, "No network connection available.");
        }
    }

    public void sendJson(String request, JSONObject jsonObject) {
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        //String otherParametersUrServiceNeed =  "Company=acompany&Lng=test&MainPeriod=test&UserID=123&CourseDate=8:10:10";
        Log.d(TAG, "sendJson url: " + request);

        try {
            URL url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            Log.d(TAG, "sendJson: length: " + jsonObject.toString().length());
            connection.setFixedLengthStreamingMode(jsonObject.toString().length());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            String basicAuth = "Basic " + new String(Base64.encode("ddong:njtu-579".getBytes(), Base64.NO_WRAP ));
            connection.setRequestProperty ("Authorization", basicAuth);
            //connection.setRequestProperty("Host", "192.168.0.111");
            Log.d(TAG, "sendJson: connectting: ... ");

            connection.connect();
            //connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            Log.d(TAG, "sendJson: connected: ");

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            int HttpResult = connection.getResponseCode();
            if(HttpResult == HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(),"utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                System.out.println(""+sb.toString());

            }else{
                System.out.println(connection.getResponseMessage());
            }
        }
        catch (MalformedURLException e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        catch (IOException e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }

    }

    public void sendPhoto(String email, String path) {
        new SendPhotoTask().execute(email, path);
    }

    private class SendPhotoTask extends AsyncTask<String, Void, Void> {
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

                multipart.addFormField("datauuid", "de305d54-75b4-431b-adb2-eb6b9e546019");
                multipart.addFormField("owneruuid", SharePrefHelper.getInstance().getUserUUID());
                multipart.addFormField("sharetouuid", "de305d54-75b4-431b-adb2-eb6b9e546019");
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
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class PostJsonTask extends AsyncTask<HttpTaskParams, Void, Void> {
        @Override
        protected Void doInBackground(HttpTaskParams... params) {
            // params comes from the execute() call: params[0] is the url.
            Log.d(TAG, "PostJsonTask url: " + params[0].url);
            sendJson(params[0].url, params[0].jobj);
            return null;
        }
    }

    private static class HttpTaskParams {
        String url;
        JSONObject jobj;

        HttpTaskParams(String url, JSONObject obj) {
            this.url = url;
            this.jobj = obj;
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
            String basicAuth = "Basic " + new String(Base64.encode("ddong:njtu-579".getBytes(), Base64.NO_WRAP ));
            httpConn.setRequestProperty("Authorization", basicAuth);
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
}
