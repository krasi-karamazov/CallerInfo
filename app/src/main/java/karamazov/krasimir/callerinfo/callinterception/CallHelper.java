package karamazov.krasimir.callerinfo.callinterception;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import karamazov.krasimir.callerinfo.ui.InfoActivity;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class CallHelper {

    private Context mContext;
    private TelephonyManager mTelephonyManager;
    private CallStateChangedListener mCallStateChangedListener;

    private class CallStateChangedListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch(state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    try{
                        Thread.sleep(3000);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(mContext, InfoActivity.class);
                    intent.putExtra(InfoActivity.INCOMING_NUMBER_KEY, incomingNumber);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
    public CallHelper(Context context) {
        mContext = context;
        mCallStateChangedListener = new CallStateChangedListener();
    }

    public void start() {
        mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mCallStateChangedListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void stop() {
        mTelephonyManager.listen(mCallStateChangedListener, PhoneStateListener.LISTEN_NONE);
    }
}
