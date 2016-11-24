package com.arpaul.geocare.activityRecognition;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arpaul.geocare.common.AppConstant;
import com.arpaul.geocare.dataObject.ActivityRecogDO;
import com.arpaul.geocare.dataObject.GeoFenceLocationDO;
import com.arpaul.geocare.geoFence.GeofenceTransitionsIntentService;
import com.arpaul.utilitieslib.CalendarUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;

/**
 * Created by ARPaul on 19-11-2016.
 */

public class ActivityRecogNotiService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private GoogleApiClient mGoogleApiClient;
    private String name, event, date, time;
    private int locationID;
    private boolean clearActiNoti = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.hasExtra(ActivityRecogDO.LOCATIONID)) {
            locationID = intent.getExtras().getInt(ActivityRecogDO.LOCATIONID, 0);
            name = intent.getExtras().getString(ActivityRecogDO.LOCATIONNAME);
            event = intent.getExtras().getString(ActivityRecogDO.EVENT);
            date = intent.getExtras().getString(ActivityRecogDO.OCCURANCEDATE);
            time = intent.getExtras().getString(ActivityRecogDO.OCCURANCETIME);
        } else if(intent.hasExtra(AppConstant.KEY_ACTIVITY_NOTI)) {
            if(intent.getExtras().getString(AppConstant.KEY_ACTIVITY_NOTI).equalsIgnoreCase(AppConstant.VALUE_CLEAR))
                clearActiNoti = true;
        }
        buildGoogleApiClient();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

        return START_NOT_STICKY;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(!clearActiNoti) {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    AppConstant.DETECTION_INTERVAL_IN_MILLISECONDS,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
        } else {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedIntentService.class);

        intent.putExtra(ActivityRecogDO.LOCATIONID, locationID);
        intent.putExtra(ActivityRecogDO.LOCATIONNAME, name);
        intent.putExtra(ActivityRecogDO.EVENT, event);
        intent.putExtra(ActivityRecogDO.OCCURANCEDATE, date);
        intent.putExtra(ActivityRecogDO.OCCURANCETIME, time);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
