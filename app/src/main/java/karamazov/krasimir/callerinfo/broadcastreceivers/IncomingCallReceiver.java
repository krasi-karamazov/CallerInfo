package karamazov.krasimir.callerinfo.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import karamazov.krasimir.callerinfo.services.CallInterceptorService;
import karamazov.krasimir.callerinfo.utils.CallerInfoLog;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final Intent myIntent=new Intent(context,CallInterceptorService.class);
        CallerInfoLog.d("sfgksdkgljdf");
        myIntent.putExtras(intent);
        context.startService(myIntent);
    }
}
