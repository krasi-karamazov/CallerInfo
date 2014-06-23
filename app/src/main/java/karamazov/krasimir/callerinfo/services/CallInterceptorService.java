package karamazov.krasimir.callerinfo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import karamazov.krasimir.callerinfo.callinterception.CallHelper;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class CallInterceptorService extends Service {
    private CallHelper mCallHelper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCallHelper = new CallHelper(this);
        int res = super.onStartCommand(intent, flags, startId);
        mCallHelper.start();
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallHelper.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
