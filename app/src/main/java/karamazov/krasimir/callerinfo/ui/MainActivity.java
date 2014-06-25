package karamazov.krasimir.callerinfo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.CompoundButton;
import butterknife.ButterKnife;
import org.jraf.android.backport.switchwidget.Switch;
import butterknife.InjectView;
import karamazov.krasimir.callerinfo.R;
import karamazov.krasimir.callerinfo.callinterception.CallerInfoPhoneStateListener;
import karamazov.krasimir.callerinfo.services.CallInterceptorService;
import karamazov.krasimir.callerinfo.utils.Constants;

public class MainActivity extends ActionBarActivity {
    @InjectView(R.id.switch_should_detect_calls)
    Switch mSwitch;
    private SharedPreferences mPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mSwitch.setOnCheckedChangeListener(getOnCheckedChangeListener());
        mPreferences = getSharedPreferences(Constants.CALLERINFOPREFS_FILE_NAME, MODE_PRIVATE);
        if(mPreferences.contains(Constants.SERVICE_ENABLED_KEY)) {
            mSwitch.setChecked(mPreferences.getBoolean(Constants.SERVICE_ENABLED_KEY, false));
        }
    }

    private CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                if(checked) {
                    editor.putBoolean(Constants.SERVICE_ENABLED_KEY, true);
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    telephonyManager.listen(new CallerInfoPhoneStateListener(MainActivity.this), PhoneStateListener.LISTEN_CALL_STATE);

                }else{
                    editor.putBoolean(Constants.SERVICE_ENABLED_KEY, false);
                }
                editor.commit();
            }
        };
    }
}
