package karamazov.krasimir.callerinfo.callinterception;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import karamazov.krasimir.callerinfo.services.CallInterceptorService;
import karamazov.krasimir.callerinfo.utils.CallerInfoLog;

/**
 * Created by krasimir.karamazov on 6/24/2014.
 */
public class CallerInfoPhoneStateListener extends PhoneStateListener {

    private Context mContext;
    private boolean wasRinging;
    public CallerInfoPhoneStateListener(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        Intent intent = new Intent(mContext, CallInterceptorService.class);
        CallerInfoLog.d("State " + state);
        /*if(state == TelephonyManager.CALL_STATE_RINGING) {
            intent.putExtra(CallInterceptorService.INCOMING_NUMBER_KEY, incomingNumber);
            mContext.startService(intent);
            CallerInfoLog.d("Ring");
        }else{
            mContext.stopService(intent);
            CallerInfoLog.d("ELSE");
        }*/
        switch(state){
            case TelephonyManager.CALL_STATE_RINGING:

                wasRinging = true;
                mContext.startService(intent);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:


                if (!wasRinging) {
                    // Start your new activity
                } else {
                    mContext.stopService(intent);
                }

                // this should be the last piece of code before the break
                wasRinging = true;
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                mContext.stopService(intent);
                // this should be the last piece of code before the break
                wasRinging = true;
                break;
        }
    }
}
