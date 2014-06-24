package karamazov.krasimir.callerinfo.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import karamazov.krasimir.callerinfo.callinterception.CallerInfoPhoneStateListener;
import karamazov.krasimir.callerinfo.utils.Constants;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new CallerInfoPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
    }
}
