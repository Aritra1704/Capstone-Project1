package com.arpaul.geocare.common;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.arpaul.geocare.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Aritra on 20-09-2016.
 */

public class AppConstant {

    public static final float GEOFENCE_RADIUS_IN_METERS = 25; // 1 mile, 1.6 km

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final long LOCATION_UPDATES_IN_SECONDS = 60;

    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;

    public static final String GEO_FENCE_TIMESEC_FORMAT = "HH:mm:ss";

    public static final String ACTION_REFRESH               = "ACTION_REFRESH";
    public static final String ACTION_REFRESH_TRACK         = "ACTION_REFRESH_TRACK";
    public static final String EXTERNAL_FOLDER              = "/GeoCare/";
    public static final String EXTERNAL_FILENAME            = "GeoCare.txt";
    public static FirebaseAuth mFirebaseAuth;

    public static final String KEY_ACTIVITY_NOTI        = "KEY_ACTIVITY_NOTI";
    public static final String VALUE_CLEAR              = "CLEAR";

    public static int trackClickPosition = -1;
    public static int REQUEST_INVITE = 1501;

    public static void writeFile(String mValue) {

        try {
            String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + AppConstant.EXTERNAL_FOLDER + AppConstant.EXTERNAL_FILENAME;
            FileWriter fw = new FileWriter(filename, true);
            fw.write(mValue + "\n\n");
            fw.close();
        } catch (IOException ioe) {
        }

    }
}
