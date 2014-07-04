package karamazov.krasimir.callerinfo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;

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
    @InjectView(R.id.et_percent)
    EditText mPercentField;

    private SharedPreferences mPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mPreferences = getSharedPreferences(Constants.CALLERINFOPREFS_FILE_NAME, MODE_PRIVATE);
        if(mPreferences.contains(Constants.SERVICE_ENABLED_KEY)) {
            mSwitch.setChecked(mPreferences.getBoolean(Constants.SERVICE_ENABLED_KEY, false));
        }

        mPercentField.setText(Integer.valueOf(mPreferences.getInt(Constants.PERCENT_OFFSET_KEY, 0)).toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_done) {
            final SharedPreferences.Editor editor = mPreferences.edit();
            if(!TextUtils.isEmpty(mPercentField.getText())) {

                Integer percentValue = Integer.valueOf(mPercentField.getText().toString());
                if(percentValue > 100) {
                    percentValue = 100;
                }else if(percentValue < 0){
                    percentValue = 0;
                }
                editor.putInt(Constants.PERCENT_OFFSET_KEY, percentValue);
            }else{
                editor.putInt(Constants.PERCENT_OFFSET_KEY, 0);
            }

            if(mSwitch.isChecked()) {
                editor.putBoolean(Constants.SERVICE_ENABLED_KEY, true);
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(new CallerInfoPhoneStateListener(MainActivity.this), PhoneStateListener.LISTEN_CALL_STATE);

            }else{
                editor.putBoolean(Constants.SERVICE_ENABLED_KEY, false);
            }

            editor.commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
