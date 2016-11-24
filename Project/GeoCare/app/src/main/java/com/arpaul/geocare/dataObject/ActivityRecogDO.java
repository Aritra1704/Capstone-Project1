package com.arpaul.geocare.dataObject;

import java.util.ArrayList;

/**
 * Created by Aritra on 6/15/2016.
 */
public class ActivityRecogDO extends BaseDO {
    public int LocationId               = 0;
    public String LocationName          = "";
    public String Event                 = "";
    public String OccuranceDate         = "";
    public String OccuranceTime         = "";
    public String CurrentActivity       = "";

    public static final String LOCATIONID       = "LOCATIONID";
    public static final String LOCATIONNAME     = "LOCATIONNAME";
    public static final String EVENT            = "EVENT";
    public static final String OCCURANCEDATE    = "OCCURANCEDATE";
    public static final String OCCURANCETIME    = "OCCURANCETIME";
    public static final String CURRENT_ACTIVITY = "CURRENT_ACTIVITY";
}
