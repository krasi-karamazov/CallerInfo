package karamazov.krasimir.callerinfo.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import karamazov.krasimir.callerinfo.services.CallInterceptorService;
import karamazov.krasimir.callerinfo.utils.CallerInfoLog;
import karamazov.krasimir.callerinfo.utils.Constants;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private boolean wasRinging;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.PHONE_STATE")){
            if(intent.getExtras() != null) {
                final SharedPreferences prefs = context.getSharedPreferences(Constants.CALLERINFOPREFS_FILE_NAME, Context.MODE_PRIVATE);

                if(prefs.contains(Constants.SERVICE_ENABLED_KEY) && prefs.getBoolean(Constants.SERVICE_ENABLED_KEY, false)){
                    String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                    if(state != null){
                        Intent serviceIntent = new Intent(context, CallInterceptorService.class);
                        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                            CallerInfoLog.d("State ringing");
                            wasRinging = true;
                            String incomingNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                            serviceIntent.putExtra(CallInterceptorService.INCOMING_NUMBER_KEY, incomingNumber);
                            context.startService(serviceIntent);
                        }else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                            CallerInfoLog.d("State OFF HOOK");
                            if (!wasRinging) {
                                context.stopService(serviceIntent);
                            } else {
                                context.stopService(serviceIntent);
                            }
                        }else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                            CallerInfoLog.d("State idle");
                            context.stopService(serviceIntent);
                            if(wasRinging){
                                context.stopService(serviceIntent);
                            }
                            wasRinging = true;
                        }
                    }
                }
            }
        }
    }
}
