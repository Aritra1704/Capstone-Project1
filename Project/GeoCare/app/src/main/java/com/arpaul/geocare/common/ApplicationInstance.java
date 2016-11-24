package com.arpaul.geocare.common;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Aritra on 19-09-2016.
 */
public class ApplicationInstance extends Application {

    public static final int LOADER_FETCH_ADDRESS            = 1;
    public static final int LOADER_FETCH_LOCATION           = 2;
    public static final int LOADER_SAVE_LOCATION            = 3;
    public static final int LOADER_FETCH_ALL_LOCATION       = 4;
    public static final int LOADER_FETCH_TRACK_LOCATION     = 5;

    public static final String LOCK_APP_DB              = "LOCK_APP_DB";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
