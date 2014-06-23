package karamazov.krasimir.callerinfo.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import karamazov.krasimir.callerinfo.R;
import karamazov.krasimir.callerinfo.utils.CallerInfoLog;

/**
 * Created by krasimir.karamazov on 6/23/2014.
 */
public class InfoActivity extends FragmentActivity {

    public static final String INCOMING_NUMBER_KEY = "incoming_number_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        if(getIntent().getExtras() != null) {
            final String incomingNumber = getIntent().getExtras().getString(INCOMING_NUMBER_KEY);
            if(!TextUtils.isEmpty(incomingNumber)) {
                CallerInfoLog.d("incomingNumber " + incomingNumber);
            }
        }
    }
}
