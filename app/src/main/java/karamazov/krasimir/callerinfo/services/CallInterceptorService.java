package karamazov.krasimir.callerinfo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import karamazov.krasimir.callerinfo.R;
import karamazov.krasimir.callerinfo.utils.CallerInfoLog;
import karamazov.krasimir.callerinfo.utils.Constants;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class CallInterceptorService extends Service {
    public static final String INCOMING_NUMBER_KEY = "incoming_number";

    private View mView;
    private TextView mInfoTextView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.CALLERINFOPREFS_FILE_NAME, Context.MODE_PRIVATE);
        if(prefs.contains(Constants.SERVICE_ENABLED_KEY) && prefs.getBoolean(Constants.SERVICE_ENABLED_KEY, false)) {

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_HORIZONTAL;

            WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            if(mView == null){
                final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = inflater.inflate(R.layout.activity_info, null);
                mInfoTextView = (TextView)mView.findViewById(R.id.tv_info);
                wm.addView(mView, params);
            }
            new GetInfoByNumberTask().execute("0888888888");

        }
        return START_STICKY;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private class GetInfoByNumberTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL("http://www.estateassistant.eu/PhoneCallInfo/WebService1.ashx");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("param1", "12312"));
                params.add(new BasicNameValuePair("param2", "0888888888"));
                params.add(new BasicNameValuePair("param3", "715275712312"));

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                InputStream stream = conn.getInputStream();
                byte[] buffer = new byte[1024];
                StringBuilder builder = new StringBuilder();
                while((stream.read(buffer)) != -1) {
                    builder.append(new String(buffer, "UTF-8"));
                }

                mInfoTextView.setText(builder.toString());
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CallerInfoLog.d("State stopped");
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(mView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
