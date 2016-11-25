package com.arpaul.geocare.dataaccess;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by ARPaul on 26-11-2016.
 */

public class InsertDataPref {
    public static final int INSERT_PREF_LOC = 0;
    public static final int DELETE_ALL_DATA = 1;


    @IntDef({INSERT_PREF_LOC, DELETE_ALL_DATA})
    @Retention(RetentionPolicy.SOURCE)
    public @interface InsertDataPreference{};

}
