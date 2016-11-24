package com.arpaul.geocare.geoFence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.arpaul.geocare.GeoFenceActivity;
import com.arpaul.geocare.R;
import com.arpaul.geocare.activityRecognition.ActivityRecogNotiService;
import com.arpaul.geocare.common.AppConstant;
import com.arpaul.geocare.dataAccess.GCCPConstants;
import com.arpaul.geocare.dataObject.ActivityRecogDO;
import com.arpaul.geocare.dataObject.GeoFenceLocationDO;
import com.arpaul.geocare.dataObject.PrefLocationDO;
import com.arpaul.utilitieslib.CalendarUtils;
import com.arpaul.utilitieslib.StringUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ARPaul on 30-10-2016.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    protected static final  String TAG = "GeofenceIntentService";
    private Location position;
    public GeofenceTransitionsIntentService(){
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
//                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            position = geofencingEvent.getTriggeringLocation();
            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
        }
    }

    private String getGeofenceTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        trackPositions(geofenceTransitionString + "]" + triggeringGeofencesIdsString);
        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return getString(R.string.geofence_currently_in);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private void trackPositions(String triggeringGeofences) {
        String[] geofenceEvent = triggeringGeofences.split("]");

        AppConstant.writeFile("\nGeofence: " + triggeringGeofences + " Date: " + CalendarUtils.getDateinPattern(CalendarUtils.DATE_TIME_FORMAT_T));

        if(geofenceEvent != null && geofenceEvent.length > 0) {

            Cursor cursor = getContentResolver().query(GCCPConstants.CONTENT_URI_SAVED_LOC,
                    new String[]{PrefLocationDO.LOCATIONID, PrefLocationDO.ADDRESS},
                    PrefLocationDO.LOCATIONNAME + GCCPConstants.TABLE_LIKE,
                    new String[]{geofenceEvent[1].trim()},
                    null);
            if(cursor != null && cursor.moveToFirst()) {
                PrefLocationDO objPrefLocationDO = new PrefLocationDO();
                objPrefLocationDO.LocationId = StringUtils.getInt(cursor.getString(cursor.getColumnIndex(PrefLocationDO.LOCATIONID)));
                objPrefLocationDO.Address = cursor.getString(cursor.getColumnIndex(PrefLocationDO.ADDRESS));

                AppConstant.writeFile("\n" + objPrefLocationDO.Address);

                String date = CalendarUtils.getDateinPattern(CalendarUtils.DATE_FORMAT1);
                String time = CalendarUtils.getDateinPattern(AppConstant.GEO_FENCE_TIMESEC_FORMAT);
                String locationname = geofenceEvent[1];
                String event = geofenceEvent[0];
                Uri uri = null;

                ContentValues cValues = new ContentValues();
                cValues.put(GeoFenceLocationDO.LOCATIONID, objPrefLocationDO.LocationId);
                cValues.put(GeoFenceLocationDO.LOCATIONNAME, locationname);
                cValues.put(GeoFenceLocationDO.ADDRESS, objPrefLocationDO.Address);
                cValues.put(GeoFenceLocationDO.LATITUDE, position.getLatitude());
                cValues.put(GeoFenceLocationDO.LONGITUDE, position.getLongitude());
                cValues.put(GeoFenceLocationDO.EVENT, event);
                cValues.put(GeoFenceLocationDO.OCCURANCEDATE, date);
                cValues.put(GeoFenceLocationDO.OCCURANCETIME, time);

                int update = getContentResolver().update(GCCPConstants.CONTENT_URI_GEOFENCE_LOC,
                        cValues,
                        GeoFenceLocationDO.LOCATIONNAME + GCCPConstants.TABLE_QUES + GCCPConstants.TABLE_AND +
                        GeoFenceLocationDO.EVENT + GCCPConstants.TABLE_QUES + GCCPConstants.TABLE_AND +
                        GeoFenceLocationDO.OCCURANCEDATE + GCCPConstants.TABLE_QUES + GCCPConstants.TABLE_AND +
                        GCCPConstants.TABLE_FTTIME + GeoFenceLocationDO.OCCURANCETIME + GCCPConstants.TABLE_IN_ENDBRACKET +
                        GCCPConstants.TABLE_EQUAL +
                        GCCPConstants.TABLE_FTTIME + "'" + time + "'" + GCCPConstants.TABLE_IN_ENDBRACKET,
                        new String[]{locationname, event, date});
                if(update < 1) {
                    AppConstant.writeFile("\nGeofenceTransition Insert: " +
                            " name: " + locationname +
                            " event: " + event +
                            " date: " + date +
                            " time: " + time);

                    uri = getContentResolver().insert(GCCPConstants.CONTENT_URI_GEOFENCE_LOC, cValues);

                    Intent intent = new Intent(getApplicationContext(), ActivityRecogNotiService.class);
                    intent.putExtra(ActivityRecogDO.LOCATIONID, objPrefLocationDO.LocationId);
                    intent.putExtra(ActivityRecogDO.LOCATIONNAME, locationname);
                    intent.putExtra(ActivityRecogDO.EVENT, event);
                    intent.putExtra(ActivityRecogDO.OCCURANCEDATE, date);
                    intent.putExtra(ActivityRecogDO.OCCURANCETIME, time);
                    startService(intent);
                } else {
                    AppConstant.writeFile("\nGeofenceTransition Update: " +
                            " name: " + locationname +
                            " event: " + event +
                            " date: " + date +
                            " time: " + time);
                }
                cursor.close();
                sendNotification(objPrefLocationDO.LocationId, event + ": " + locationname);
            }
        }
    }

    private void sendNotification(int locationId, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), GeoFenceActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(GeoFenceActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.svg_geofence)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.svg_geofence))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(locationId, builder.build());
    }
}
