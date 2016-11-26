package com.arpaul.geocare.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.arpaul.geocare.dataobject.PrefLocationDO;
import com.arpaul.utilitieslib.LogUtils;

import static com.arpaul.geocare.dataaccess.InsertDataPref.DELETE_ALL_DATA;
import static com.arpaul.geocare.dataaccess.InsertDataPref.INSERT_PREF_LOC;

/**
 * Created by Aritra on 14-07-2016.
 */
public class InsertLoader extends AsyncTaskLoader {

    private Context context;
    private Object data;
    private InsertDataType dataType;

    private final String TAG = "InsertLoader";
    private int dataPref;

    public final static String BUNDLE_INSERTLOADER      = "BUNDLE_INSERTLOADER";

    /**
     *
     * @param context
     */
    public InsertLoader(Context context){
        super(context);
        onContentChanged();
        this.context = context;
    }

    /**
     *
     * @param context
     * @param dataType
     * @param bundle
     */
    public InsertLoader(Context context, InsertDataType dataType, Bundle bundle){
        super(context);
        onContentChanged();
        this.context = context;
        this.dataType = dataType;
        if(bundle != null)
            data = bundle.get(BUNDLE_INSERTLOADER);
    }

    /**
     *
     * @param context
     * @param dataPref
     * @param bundle
     */
    public InsertLoader(Context context, int dataPref, Bundle bundle){
        super(context);
        onContentChanged();
        this.context = context;
        this.dataType = dataType;
        if(bundle != null)
            data = bundle.get(BUNDLE_INSERTLOADER);

        this.dataPref = dataPref;
    }

    /**
     *
     * @param context
     * @param dataType
     * @param data
     */
    public InsertLoader(Context context, InsertDataType dataType, Object data){
        super(context);
        onContentChanged();
        this.context = context;
        this.data = data;
        this.dataType = dataType;
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged())
            forceLoad();
    }

    @Override
    public Object loadInBackground() {

        @InsertDataPref.InsertDataPreference int dataPreference = dataPref;
        switch (dataPreference) {
            case INSERT_PREF_LOC:
                if(data != null && data instanceof PrefLocationDO){
                    PrefLocationDO objPrefLocationDO = (PrefLocationDO) data;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PrefLocationDO.LOCATIONID, objPrefLocationDO.LocationId);
                    contentValues.put(PrefLocationDO.LOCATIONNAME, objPrefLocationDO.LocationName);
                    contentValues.put(PrefLocationDO.ADDRESS, objPrefLocationDO.Address);
                    contentValues.put(PrefLocationDO.LATITUDE, objPrefLocationDO.Latitude);
                    contentValues.put(PrefLocationDO.LONGITUDE, objPrefLocationDO.Longitude);
                    contentValues.put(PrefLocationDO.RADIUS, objPrefLocationDO.Radius);

                    String address = "";
                    int tryUpdate = context.getContentResolver().update(GCCPConstants.CONTENT_URI_SAVED_LOC,
                            contentValues,
                            PrefLocationDO.LOCATIONID + GCCPConstants.TABLE_QUES,
                            new String[]{objPrefLocationDO.LocationId + ""});

                    if (tryUpdate <= 0){
                        Uri uri = context.getContentResolver().insert(GCCPConstants.CONTENT_URI_SAVED_LOC, contentValues);
                        if(uri != null)
                            address = objPrefLocationDO.Address;
                    } else {
                        address = objPrefLocationDO.Address;
                    }

                    return address;
                }
                break;

            case DELETE_ALL_DATA:
                break;
            default:

                break;
        }

//        switch (dataType){
//            case INSERT_PREF_LOC:
//                if(data != null && data instanceof PrefLocationDO){
//                    PrefLocationDO objPrefLocationDO = (PrefLocationDO) data;
//                    ContentValues contentValues = new ContentValues();
//                    contentValues.put(PrefLocationDO.LOCATIONID, objPrefLocationDO.LocationId);
//                    contentValues.put(PrefLocationDO.LOCATIONNAME, objPrefLocationDO.LocationName);
//                    contentValues.put(PrefLocationDO.ADDRESS, objPrefLocationDO.Address);
//                    contentValues.put(PrefLocationDO.LATITUDE, objPrefLocationDO.Latitude);
//                    contentValues.put(PrefLocationDO.LONGITUDE, objPrefLocationDO.Longitude);
//                    contentValues.put(PrefLocationDO.RADIUS, objPrefLocationDO.Radius);
//
//                    String address = "";
//                    int tryUpdate = context.getContentResolver().update(GCCPConstants.CONTENT_URI_SAVED_LOC,
//                            contentValues,
//                            PrefLocationDO.LOCATIONID + GCCPConstants.TABLE_QUES,
//                            new String[]{objPrefLocationDO.LocationId + ""});
//
//                    if (tryUpdate <= 0){
//                        Uri uri = context.getContentResolver().insert(GCCPConstants.CONTENT_URI_SAVED_LOC, contentValues);
//                        if(uri != null)
//                            address = objPrefLocationDO.Address;
//                    } else {
//                        address = objPrefLocationDO.Address;
//                    }
//
//                    return address;
//                }
//                break;
//
//
//            default:
//
//                break;
//        }
        return null;
    }

    @Override
    protected void onStopLoading() {
        if (LogUtils.isLogEnable)
            Log.i(TAG, "+++ onStopLoading() called! +++");

        cancelLoad();
    }

    @Override
    protected void onReset() {
        if (LogUtils.isLogEnable)
            Log.i(TAG, "+++ onReset() called! +++");

        // Ensure the loader is stopped.
        onStopLoading();

    }

    @Override
    public void forceLoad() {
        if (LogUtils.isLogEnable)
            Log.i(TAG, "+++ forceLoad() called! +++");
        super.forceLoad();
    }

}
