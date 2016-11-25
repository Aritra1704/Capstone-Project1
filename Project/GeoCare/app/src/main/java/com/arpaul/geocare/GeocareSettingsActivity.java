package com.arpaul.geocare;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Aritra on 04-11-2016.
 */

public class GeocareSettingsActivity extends BaseActivity {

    private View llSettingsActivity;
    private TextView tvUploadDb;

    @Override
    public void initialize() {
        llSettingsActivity = baseInflater.inflate(R.layout.activity_geocare_settings,null);
        llBody.addView(llSettingsActivity, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        initialiseControls();

        bindControls();
    }

    private void bindControls(){
        tvUploadDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyAppDbtoSdcard();
            }
        });
    }

    private void initialiseControls(){

        tvUploadDb = (TextView) llSettingsActivity.findViewById(R.id.tvUploadDb);
    }
}
