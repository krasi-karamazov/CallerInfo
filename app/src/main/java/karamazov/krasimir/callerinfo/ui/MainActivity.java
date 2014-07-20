package karamazov.krasimir.callerinfo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import butterknife.ButterKnife;
import org.jraf.android.backport.switchwidget.Switch;
import butterknife.InjectView;
import karamazov.krasimir.callerinfo.R;
import karamazov.krasimir.callerinfo.utils.Constants;

public class MainActivity extends ActionBarActivity {
    @InjectView(R.id.switch_should_detect_calls)
    Switch mSwitch;
    @InjectView(R.id.et_percent)
    EditText mPercentField;
    @InjectView(R.id.et_percent_from_screen_height)
    EditText mPercentHeightField;

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
        mPercentHeightField.setText(Integer.valueOf(mPreferences.getInt(Constants.PERCENT_HEIGHT_KEY, 30)).toString());
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


            normalizeAndRecordvalue(mPercentField, editor, Constants.PERCENT_OFFSET_KEY);
            normalizeAndRecordvalue(mPercentHeightField, editor, Constants.PERCENT_HEIGHT_KEY);

            if(mSwitch.isChecked()) {
                editor.putBoolean(Constants.SERVICE_ENABLED_KEY, true);
            }else{
                editor.putBoolean(Constants.SERVICE_ENABLED_KEY, false);
            }

            editor.commit();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void normalizeAndRecordvalue(EditText field, SharedPreferences.Editor editor, String key) {
        if(!TextUtils.isEmpty(field.getText())) {

            Integer percentValue = Integer.valueOf(field.getText().toString());
            if(percentValue > 100) {
                percentValue = 100;
            }else if(percentValue < 0){
                percentValue = 0;
            }
            editor.putInt(key, percentValue);
        }else{
            editor.putInt(key, 0);
        }
    }
}
