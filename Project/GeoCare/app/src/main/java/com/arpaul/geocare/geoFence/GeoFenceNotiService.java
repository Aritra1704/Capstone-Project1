package com.arpaul.geocare.geoFence;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.arpaul.geocare.R;
import com.arpaul.geocare.common.AppConstant;
import com.arpaul.geocare.dataAccess.GCCPConstants;
import com.arpaul.geocare.dataObject.PrefLocationDO;
import com.arpaul.utilitieslib.LogUtils;
import com.arpaul.utilitieslib.StringUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by ARPaul on 31-10-2016.
 */

public class GeoFenceNotiService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    protected ArrayList<Geofence> mGeofenceList;
    private final String LOG_TAG = "GeoFenceNotiService";
    private ArrayList<PrefLocationDO> arrPrefLocationDO = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGeofenceList = new ArrayList<Geofence>();

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
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Cursor cursor = getContentResolver().query(GCCPConstants.CONTENT_URI_SAVED_LOC,
                new String[]{PrefLocationDO.LOCATIONID, PrefLocationDO.LOCATIONNAME, PrefLocationDO.ADDRESS,
                        PrefLocationDO.LATITUDE, PrefLocationDO.LONGITUDE, PrefLocationDO.RADIUS},
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            PrefLocationDO objPrefLocationDO = null;
            arrPrefLocationDO.clear();
            do {
                objPrefLocationDO = new PrefLocationDO();
                objPrefLocationDO.LocationId = StringUtils.getInt(cursor.getString(cursor.getColumnIndex(PrefLocationDO.LOCATIONID)));
                objPrefLocationDO.LocationName = cursor.getString(cursor.getColumnIndex(PrefLocationDO.LOCATIONNAME));
                objPrefLocationDO.Address = cursor.getString(cursor.getColumnIndex(PrefLocationDO.ADDRESS));
                objPrefLocationDO.Latitude = StringUtils.getDouble(cursor.getString(cursor.getColumnIndex(PrefLocationDO.LATITUDE)));
                objPrefLocationDO.Longitude = StringUtils.getDouble(cursor.getString(cursor.getColumnIndex(PrefLocationDO.LONGITUDE)));
                objPrefLocationDO.Radius = StringUtils.getInt(cursor.getString(cursor.getColumnIndex(PrefLocationDO.RADIUS)));

                arrPrefLocationDO.add(objPrefLocationDO);
            } while (cursor.moveToNext());

            removeGeoFenceList();
            // Get the geofences used. Geofence data is hard coded in this sample.
            populateGeofenceList();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        LogUtils.infoLog(LOG_TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.infoLog(LOG_TAG, "GoogleApiClient connection has failed");
        mGoogleApiClient.connect();
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            LogUtils.infoLog(LOG_TAG, "Geofences Added");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, status.getStatusCode());
            LogUtils.infoLog(LOG_TAG, errorMessage);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public void addGeofencesButtonHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mGeofencePendingIntent = getGeofencePendingIntent();
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    mGeofencePendingIntent
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void populateGeofenceList() {
        for (PrefLocationDO objPrefLocationDO : arrPrefLocationDO) {
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this geofence.
                    .setRequestId(objPrefLocationDO.LocationName)

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            objPrefLocationDO.Latitude,
                            objPrefLocationDO.Longitude,
                            objPrefLocationDO.Radius
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(AppConstant.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
//                            Geofence.GEOFENCE_TRANSITION_DWELL |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }

        addGeofencesButtonHandler();
    }

    private void removeGeoFenceList() {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }
}
