package com.arpaul.geocare;

import android.*;
import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arpaul.geocare.common.AppConstant;
import com.arpaul.geocare.dataAccess.GCCPConstants;
import com.arpaul.utilitieslib.FileUtils;
import com.arpaul.utilitieslib.LogUtils;
import com.arpaul.utilitieslib.PermissionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

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
