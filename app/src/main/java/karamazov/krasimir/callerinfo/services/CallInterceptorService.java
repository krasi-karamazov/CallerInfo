package karamazov.krasimir.callerinfo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import karamazov.krasimir.callerinfo.R;
import karamazov.krasimir.callerinfo.utils.CallerInfoLog;
import karamazov.krasimir.callerinfo.utils.Constants;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class CallInterceptorService extends Service {
    public static final String INCOMING_NUMBER_KEY = "incoming_number";

    private View mView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.CALLERINFOPREFS_FILE_NAME, Context.MODE_PRIVATE);
        if(prefs.contains(Constants.SERVICE_ENABLED_KEY) && prefs.getBoolean(Constants.SERVICE_ENABLED_KEY, false)) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_HORIZONTAL;

            WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

            final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = inflater.inflate(R.layout.activity_info, null);
            wm.addView(mView, params);
        }

        return START_STICKY;
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
