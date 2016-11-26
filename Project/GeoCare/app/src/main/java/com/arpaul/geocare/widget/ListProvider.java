package com.arpaul.geocare.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.arpaul.geocare.R;
import com.arpaul.geocare.common.AppConstant;
import com.arpaul.geocare.dataaccess.GCCPConstants;
import com.arpaul.geocare.dataobject.ActivityRecogDO;
import com.arpaul.geocare.dataobject.GeoFenceLocationDO;
import com.arpaul.utilitieslib.CalendarUtils;
import com.arpaul.utilitieslib.StringUtils;

import java.util.ArrayList;

/**
 * Created by ARPaul on 26-11-2016.
 */

public class ListProvider implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList listItemList = new ArrayList();
    private Context context = null;
    private int appWidgetId;
    private static final int CURSOR_LOADER_ID = 1;
    //private QuoteCursorAdapter mCursorAdapter;
    //private Cursor mCursor;
    private ArrayList<ActivityRecogDO> arrActivityRecogDO = new ArrayList<>();

    public ListProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        loadData();
    }

    @Override
    public void onDataSetChanged() {
        loadData();
    }

    private void loadData(){
        Thread thread = new Thread() {
            public void run() {
                query();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    private void query(){

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(
                GCCPConstants.GEOFENCE_LOCATION_TABLE_NAME + GCCPConstants.AS_GEOFENCE_LOCATION_TABLE +
                        GCCPConstants.TABLE_LEFT_OUTER_JOIN +
                        GCCPConstants.ACTI_RECOG_TABLE_NAME + GCCPConstants.AS_ACTI_RECOG_TABLE_TABLE +
                        GCCPConstants.TABLE_ON +
                        //based on location id
                        GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + GeoFenceLocationDO.LOCATIONID + GCCPConstants.TABLE_EQUAL +
                        GCCPConstants.AS_ACTI_RECOG_TABLE_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.LOCATIONID + GCCPConstants.TABLE_AND +
                        //based on occurance date
                        GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + GeoFenceLocationDO.OCCURANCEDATE + GCCPConstants.TABLE_EQUAL +
                        GCCPConstants.AS_ACTI_RECOG_TABLE_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.OCCURANCEDATE + GCCPConstants.TABLE_AND +
                        //based on occurance time
                        GCCPConstants.TABLE_FTTIME + GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + GeoFenceLocationDO.OCCURANCETIME + GCCPConstants.TABLE_IN_ENDBRACKET +
                        GCCPConstants.TABLE_EQUAL +
                        GCCPConstants.TABLE_FTTIME + GCCPConstants.AS_ACTI_RECOG_TABLE_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.OCCURANCETIME + GCCPConstants.TABLE_IN_ENDBRACKET +

                        GCCPConstants.TABLE_ORDER_BY +
                        GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + GeoFenceLocationDO.OCCURANCEDATE +
                        GCCPConstants.TABLE_DESC);

        Cursor cursor = context.getContentResolver().query(GCCPConstants.CONTENT_URI_RELATIONSHIP_JOIN,
                new String[]{GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.LOCATIONID,
                        GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.LOCATIONNAME,
                        GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.EVENT,
                        GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.OCCURANCEDATE,
                        GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.OCCURANCETIME,

                        GCCPConstants.AS_ACTI_RECOG_TABLE_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.CURRENT_ACTIVITY},
                queryBuilder.getTables(),
                null,
                null);

        if(cursor != null && cursor.moveToFirst()){
            ActivityRecogDO objActiRecogDO = null;
            do{
                objActiRecogDO = new ActivityRecogDO();
                objActiRecogDO.LocationId = StringUtils.getInt(cursor.getString(cursor.getColumnIndex(GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.LOCATIONID)));
                objActiRecogDO.LocationName = cursor.getString(cursor.getColumnIndex(GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.LOCATIONNAME));
                objActiRecogDO.Event = cursor.getString(cursor.getColumnIndex(GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.EVENT));
                objActiRecogDO.OccuranceDate = cursor.getString(cursor.getColumnIndex(GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.OCCURANCEDATE));
                objActiRecogDO.OccuranceTime = cursor.getString(cursor.getColumnIndex(GCCPConstants.AS_GEOFENCE_LOCATION_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.OCCURANCETIME));

                objActiRecogDO.CurrentActivity = cursor.getString(cursor.getColumnIndex(GCCPConstants.AS_ACTI_RECOG_TABLE_TABLE + GCCPConstants.TABLE_DOT + ActivityRecogDO.CURRENT_ACTIVITY));
                arrActivityRecogDO.add(objActiRecogDO);
            } while(cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return arrActivityRecogDO.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_adapter_cell_track);
        ActivityRecogDO objGeoFenceLocDO = arrActivityRecogDO.get(position);
        remoteView.setTextViewText(R.id.tvLocationName, objGeoFenceLocDO.LocationName);
        remoteView.setTextViewText(R.id.tvEvent, objGeoFenceLocDO.Event);

        remoteView.setTextViewText(R.id.tvActiRecog, objGeoFenceLocDO.CurrentActivity);
        String geoFenceDate =
                CalendarUtils.getDateinPattern(objGeoFenceLocDO.OccuranceDate, CalendarUtils.DATE_FORMAT1, CalendarUtils.DATE_FORMAT_WITH_COMMA);
        remoteView.setTextViewText(R.id.tvLocationDate, context.getString(R.string.on) + geoFenceDate);
        String geoFenceTime =
                CalendarUtils.getDateinPattern(objGeoFenceLocDO.OccuranceTime, AppConstant.GEO_FENCE_TIMESEC_FORMAT, CalendarUtils.TIME_HOUR_MINUTE);
        remoteView.setTextViewText(R.id.tvLocationTime, context.getString(R.string.at) + geoFenceTime);

        return remoteView;
    }
}
