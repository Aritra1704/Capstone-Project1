package com.arpaul.geocare.dataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.arpaul.geocare.dataObject.ActivityRecogDO;
import com.arpaul.geocare.dataObject.GeoFenceLocationDO;
import com.arpaul.geocare.dataObject.PrefLocationDO;


/**
 * Created by ARPaul on 09-05-2016.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;


    static final String CREATE_SAVED_LOCATION_DB_TABLE =
            " CREATE TABLE IF NOT EXISTS " + GCCPConstants.SAVED_LOCATION_TABLE_NAME +
                    " (" + GCCPConstants.TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PrefLocationDO.LOCATIONID       + " INTEGER NOT NULL, " +
                    PrefLocationDO.LOCATIONNAME     + " VARCHAR NOT NULL, " +
                    PrefLocationDO.ADDRESS          + " VARCHAR NOT NULL, " +
                    PrefLocationDO.LATITUDE         + " DOUBLE NOT NULL, " +
                    PrefLocationDO.LONGITUDE        + " DOUBLE NOT NULL, " +
                    PrefLocationDO.RADIUS           + " INTEGER NOT NULL " +
                    ");";

    static final String CREATE_GEOFENCE_LOCATION_DB_TABLE =
            " CREATE TABLE IF NOT EXISTS " + GCCPConstants.GEOFENCE_LOCATION_TABLE_NAME +
                    " (" + GCCPConstants.TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GeoFenceLocationDO.LOCATIONID       + " INTEGER NOT NULL, " +
                    GeoFenceLocationDO.LOCATIONNAME     + " VARCHAR NOT NULL, " +
                    GeoFenceLocationDO.ADDRESS          + " VARCHAR NOT NULL, " +
                    GeoFenceLocationDO.LATITUDE         + " DOUBLE NOT NULL, " +
                    GeoFenceLocationDO.LONGITUDE        + " DOUBLE NOT NULL, " +
                    GeoFenceLocationDO.EVENT            + " VARCHAR NOT NULL, " +
                    GeoFenceLocationDO.OCCURANCEDATE    + " VARCHAR NOT NULL, " +
                    GeoFenceLocationDO.OCCURANCETIME    + " VARCHAR NOT NULL " +
                    ");";

    static final String CREATE_ACTI_RECOG_LOCATION_DB_TABLE =
            " CREATE TABLE IF NOT EXISTS " + GCCPConstants.ACTI_RECOG_TABLE_NAME +
                    " (" + GCCPConstants.TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ActivityRecogDO.LOCATIONID       + " INTEGER NOT NULL, " +
                    ActivityRecogDO.LOCATIONNAME     + " VARCHAR NOT NULL, " +
                    ActivityRecogDO.EVENT            + " VARCHAR NOT NULL, " +
                    ActivityRecogDO.OCCURANCEDATE    + " VARCHAR NOT NULL, " +
                    ActivityRecogDO.OCCURANCETIME    + " VARCHAR NOT NULL, " +
                    ActivityRecogDO.CURRENT_ACTIVITY + " VARCHAR NOT NULL " +
                    ");";

    DataBaseHelper(Context context){
        super(context, GCCPConstants.DATABASE_NAME, null, GCCPConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SAVED_LOCATION_DB_TABLE);
        db.execSQL(CREATE_GEOFENCE_LOCATION_DB_TABLE);
        db.execSQL(CREATE_ACTI_RECOG_LOCATION_DB_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GCCPConstants.SAVED_LOCATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GCCPConstants.GEOFENCE_LOCATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GCCPConstants.ACTI_RECOG_TABLE_NAME);
        onCreate(db);
    }
}
