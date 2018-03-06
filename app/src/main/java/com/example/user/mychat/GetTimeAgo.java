package com.example.user.mychat;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

/**
 * Created by USER on 25-02-2018.
 */

public class GetTimeAgo extends Activity {

    private static final int SECOND_MILLIS=1000;
    private static final int MINUTE_MILLIS=60*SECOND_MILLIS;
    private static final int HOUR_MILLIS=60*MINUTE_MILLIS;
    private static final int DAY_MILLIS=24*HOUR_MILLIS;

    public GetTimeAgo() {
        super();
    }

    public static String getTimeAgo(long time, Context ctx)
    {
        if(time<1000000000000L)
        {
            time*=1000;
        }

        long now_time=System.currentTimeMillis();

        if(time>now_time || time<=0){return null;}

        final long diff=now_time-time;

        if(diff<MINUTE_MILLIS)
        {
            return "JUST NOW";
        }

        else if(diff<2*MINUTE_MILLIS)
        {
            return "A MINUTE AGO";
        }

        else if(diff<50*MINUTE_MILLIS)
        {
            return diff/MINUTE_MILLIS+" MINUTES AGO";
        }

        else if(diff<90*MINUTE_MILLIS)
        {
            return "AN HOUR AGO";
        }

        else if(diff<24*HOUR_MILLIS)
        {
            return diff/HOUR_MILLIS+ " HOURS AGO";
        }

        else if(diff<48*HOUR_MILLIS)
        {
            return "YESTERDAY";
        }

        else
        {
            return diff/DAY_MILLIS+" DAYS AGO";
        }
    }
}
