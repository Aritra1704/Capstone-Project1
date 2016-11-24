package com.arpaul.geocare.dataAccess;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ARPaul on 07-01-2016.
 */
public class GCCPConstants {
    public static final String CONTENT_AUTHORITY = "com.arpaul.geocare.dataAccess.ContentProviderHelper";

    public static final String DATABASE_NAME                    = "GeoCare.sqlite";

    public static final String SAVED_LOCATION_TABLE_NAME        = "tblTagLocation";
    public static final String GEOFENCE_LOCATION_TABLE_NAME     = "tblGeoFenceLocation";
    public static final String ACTI_RECOG_TABLE_NAME            = "tblActiRecog";

    public static final String AS_SAVED_LOCATION_TABLE          = " tSL";
    public static final String AS_GEOFENCE_LOCATION_TABLE       = " tGL";
    public static final String AS_ACTI_RECOG_TABLE_TABLE        = " tAR";

    public static final int DATABASE_VERSION                   = 1;

    public static final String PATH_RELATIONSHIP_JOIN          = "relationship_join";

    public static final String DELIMITER = "/";
    public static final String TABLE_ID    = "_id";
    public static final String TAG_ID = "/#";
    public static final String TAG_ID_ALL = "/*";

    public static final String TABLE_INNER_JOIN = " INNER JOIN ";
    public static final String TABLE_LEFT_OUTER_JOIN = " LEFT OUTER JOIN ";
    public static final String TABLE_ON = " ON ";
    public static final String TABLE_DOT = ".";
    public static final String TABLE_EQUAL = " = ";
    public static final String TABLE_WHERE = " WHERE ";
    public static final String TABLE_AND = " AND ";
    public static final String TABLE_OR = " OR ";
    public static final String TABLE_IN = " IN ";
    public static final String TABLE_NOT_IN = " NOT IN ";
    public static final String TABLE_DISTINCT = " DISTINCT ";
    public static final String TABLE_QUES  = " = ? ";
    public static final String TABLE_LIKE  = " LIKE ? ";
    public static final String TABLE_NOT_QUES  = " != ? ";
    public static final String TABLE_IN_BRACKET  = " ( ? ) ";
    public static final String TABLE_GROUP_BY  = " GROUP BY ";
    public static final String TABLE_DATE  = " DATE";
    public static final String TABLE_ORDER_BY  = " ORDER BY ";
    public static final String TABLE_ASC  = " ASC ";
    public static final String TABLE_DESC  = " DESC ";
    public static final String TABLE_FTTIME  = " strftime('%H %M', ";
    public static final String TABLE_IN_ENDBRACKET  = " ) ";

    public static final String CONTENT = "content://";
    public static final Uri BASE_CONTENT_URI = Uri.parse(CONTENT + CONTENT_AUTHORITY);

    public static final Uri CONTENT_URI_SAVED_LOC = Uri.parse(CONTENT + CONTENT_AUTHORITY + DELIMITER + SAVED_LOCATION_TABLE_NAME);
    public static final Uri CONTENT_URI_GEOFENCE_LOC = Uri.parse(CONTENT + CONTENT_AUTHORITY + DELIMITER + GEOFENCE_LOCATION_TABLE_NAME);
    public static final Uri CONTENT_URI_ACTI_RECOG = Uri.parse(CONTENT + CONTENT_AUTHORITY + DELIMITER + ACTI_RECOG_TABLE_NAME);

    public static final Uri CONTENT_URI_RELATIONSHIP_JOIN = Uri.parse(CONTENT + CONTENT_AUTHORITY + DELIMITER + PATH_RELATIONSHIP_JOIN);

    public static final String PROVIDER_NAME = CONTENT_AUTHORITY;

    // create cursor of base type directory for multiple entries
    public static final String CONTENT_MULTIPLE_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + DELIMITER + CONTENT_AUTHORITY + DELIMITER + DATABASE_NAME;
    // create cursor of base type item for single entry
    public static final String CONTENT_BASE_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + DELIMITER + CONTENT_AUTHORITY + DELIMITER + DATABASE_NAME;

    public static Uri buildLocationUri(long id){
        return ContentUris.withAppendedId(CONTENT_URI_SAVED_LOC, id);
    }
}
