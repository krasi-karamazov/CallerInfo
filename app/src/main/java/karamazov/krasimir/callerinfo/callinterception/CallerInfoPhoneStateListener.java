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
        intent.putExtra(CallInterceptorService.INCOMING_NUMBER_KEY, incomingNumber);

        switch(state){
            case TelephonyManager.CALL_STATE_RINGING:
                CallerInfoLog.d("State ringing");
                wasRinging = true;
                mContext.startService(intent);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:

                //CallerInfoLog.d("State offhook");
                if (!wasRinging) {
                    mContext.stopService(intent);
                } else {
                    mContext.stopService(intent);
                }

                wasRinging = true;
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //CallerInfoLog.d("State idle");
                mContext.stopService(intent);
                if(wasRinging){
                    mContext.stopService(intent);
                }
                wasRinging = true;
                break;
        }
    }
}
