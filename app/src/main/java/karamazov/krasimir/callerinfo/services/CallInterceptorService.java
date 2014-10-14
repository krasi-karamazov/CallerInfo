package karamazov.krasimir.callerinfo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import org.apache.http.NameValuePair;
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
    private WebView mInfoView;
    private ProgressBar mProgressBar;
    private static final int TIMES_TO_TRY = 6;
    private int mTimesTried = 0;
    private GetInfoByNumberTask mTask;
    private String mPhoneNumber;

    @Override
    public void onCreate() {
        super.onCreate();
        CallerInfoLog.d("On Create service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras() != null){
            CallerInfoLog.d("On Start Command Service Intent has extras");
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.CALLERINFOPREFS_FILE_NAME, Context.MODE_PRIVATE);
            if(prefs.contains(Constants.SERVICE_ENABLED_KEY) && prefs.getBoolean(Constants.SERVICE_ENABLED_KEY, false)) {
                WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                final DisplayMetrics metrics = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                int offset = 0;
                if(prefs.contains(Constants.PERCENT_OFFSET_KEY)){
                    int offsetPercent = prefs.getInt(Constants.PERCENT_OFFSET_KEY, 0);
                    int screenHeight = metrics.heightPixels;
                    Double offsetDouble = (double)screenHeight * ((double)offsetPercent / (double)100);
                    offset  = offsetDouble.intValue();
                }

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, offset, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 0, PixelFormat.TRANSLUCENT);
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;


                params.gravity = Gravity.TOP | Gravity.LEFT;

                if(mView == null){
                    final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    mView = inflater.inflate(R.layout.activity_info, null);
                    mProgressBar = (ProgressBar)mView.findViewById(R.id.progress_bar);
                    mInfoView = (WebView)mView.findViewById(R.id.wv_data);

                    int height = metrics.heightPixels / 3;
                    if(prefs.contains(Constants.PERCENT_HEIGHT_KEY)){
                        int percent = prefs.getInt(Constants.PERCENT_HEIGHT_KEY, 0);
                        Double heightDouble = (double)metrics.heightPixels * ((double)percent / (double)100);
                        height = heightDouble.intValue();
                    }

                    RelativeLayout.LayoutParams contentParams = (RelativeLayout.LayoutParams)mInfoView.getLayoutParams();
                    if(contentParams == null){
                        contentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
                    }
                    contentParams.height = height;
                    mInfoView.setLayoutParams(contentParams);
                    final ImageView ivClose = (ImageView)mView.findViewById(R.id.iv_close);

                    ivClose.setOnClickListener(getOnClickListener());
                    WebSettings settings = mInfoView.getSettings();
                    settings.setDefaultTextEncodingName("utf-8");
                    wm.addView(mView, params);

                }

                mPhoneNumber = intent.getStringExtra(INCOMING_NUMBER_KEY);
                mTask = new GetInfoByNumberTask();
                mTask.execute(mPhoneNumber);
            }
        }else{
            CallerInfoLog.d("On Start Command Service Intent has no extras");
        }

        return START_STICKY;
    }

    private View.OnClickListener getOnClickListener() {
        return new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.iv_close:
                        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView(mView);
                        break;
                }
            }
        };
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first){
                first = false;
            }else{
                result.append("&");
            }

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private class GetInfoByNumberTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            CallerInfoLog.d("Executing task");
            String result = executeTask(strings[0]);

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.GONE);
            if(!TextUtils.isEmpty(s)){
                mInfoView.loadData(s, "text/html; charset=utf-8", "UTF-8");
            }else{
                mInfoView.loadData("Could not download data", "text/html; charset=utf-8", "utf-8");
            }
        }
    }
    private String executeTask(String phoneNumber) {
        mTimesTried += 1;

        CallerInfoLog.d("Tries = "  + mTimesTried);
        if(mTimesTried  > 1) {
            try{
                CallerInfoLog.d("Sleeping...");
                Thread.sleep(2000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        CallerInfoLog.d("Done Sleeping...");
        String result = null;
        if(mTimesTried == TIMES_TO_TRY) {
            CallerInfoLog.d("tried 6 times quitting, result is " + result);
            return result;
        }
        try{
            result = getStringFromServer(phoneNumber);
        }catch(Exception e) {
            e.printStackTrace();
            executeTask(phoneNumber);
        }

        CallerInfoLog.d("Got result " + result);
        return result;
    }

    private String getStringFromServer(String phoneNumber) throws Exception{
        final StringBuilder builder = new StringBuilder();
        URL url = new URL("http://www.estateassistant.eu/PhoneCallInfo/WebService2.ashx");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("param1", "12312"));
        params.add(new BasicNameValuePair("param2", phoneNumber));
        params.add(new BasicNameValuePair("param3", "715275712312"));

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
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
        return builder.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CallerInfoLog.d("State stopped");
        try{
            WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mView);
            mTask.cancel(true);
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
