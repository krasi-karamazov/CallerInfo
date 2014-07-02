package karamazov.krasimir.callerinfo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
    private ScrollView mScrollContainer;
    private ProgressBar mProgressBar;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(isConnectedToInternet()){
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.CALLERINFOPREFS_FILE_NAME, Context.MODE_PRIVATE);
            if(prefs.contains(Constants.SERVICE_ENABLED_KEY) && prefs.getBoolean(Constants.SERVICE_ENABLED_KEY, false)) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 10, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 0, PixelFormat.TRANSLUCENT);
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                params.gravity = Gravity.TOP;
                if(mView == null){
                    final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    mView = inflater.inflate(R.layout.activity_info, null);
                    mProgressBar = (ProgressBar)mView.findViewById(R.id.progress_bar);
                    mScrollContainer = (ScrollView)mView.findViewById(R.id.sc_content_container);
                    DisplayMetrics metrics = new DisplayMetrics();
                    wm.getDefaultDisplay().getMetrics(metrics);
                    int height = metrics.heightPixels / 3;
                    final LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
                    mScrollContainer.setLayoutParams(contentParams);
                    final ImageView ivClose = (ImageView)mView.findViewById(R.id.iv_close);
                    ivClose.setOnClickListener(getOnClickListener());
                    mInfoTextView = (TextView)mView.findViewById(R.id.tv_info);
                    wm.addView(mView, params);
                }

                String phoneNumber = intent.getStringExtra(INCOMING_NUMBER_KEY);
                if(TextUtils.isEmpty(phoneNumber)) {
                    phoneNumber = "0888888888";
                }
                new GetInfoByNumberTask().execute(phoneNumber);

            }
        }

        return START_STICKY;
    }

    private View.OnClickListener getOnClickListener() {
        return new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                wm.removeView(mView);
            }
        };
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
            final StringBuilder builder = new StringBuilder();
            try{
                URL url = new URL("http://www.estateassistant.eu/PhoneCallInfo/WebService2.ashx");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("param1", "12312"));
                params.add(new BasicNameValuePair("param2", strings[0]));
                params.add(new BasicNameValuePair("param3", "715275712312"));

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                InputStream stream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                String str;
                while((str = reader.readLine()) != null) {
                    builder.append(str);
                }

            }catch(Exception e){
                return null;
            }
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.GONE);
            mScrollContainer.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(s)){
                Spanned htmlContent = Html.fromHtml(s);
                mInfoTextView.setText(htmlContent);
                mScrollContainer.requestLayout();
            }else{
                mInfoTextView.setText("Could not download data");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CallerInfoLog.d("State stopped");
        try{
            WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mView);
        }catch(Exception e){

        }

    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
