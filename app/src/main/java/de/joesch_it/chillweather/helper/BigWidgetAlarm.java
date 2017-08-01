package de.joesch_it.chillweather.helper;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import de.joesch_it.chillweather.receiver.BigChillWidgetProvider;


public class BigWidgetAlarm {
    private static final String TAG = " ### " + BigWidgetAlarm.class.getSimpleName() + " ###";

    private final int ALARM_ID = 0;
    private static final int INTERVAL_MILLIS = 60000;
    private Context mContext;


    public BigWidgetAlarm(Context context){
        mContext = context;
    }


    public void startAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, INTERVAL_MILLIS);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(BigChillWidgetProvider.ACTION_TICK);
        PendingIntent removedIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Log.d(TAG, "StartAlarm");
        alarmManager.cancel(removedIntent);
        // needs RTC_WAKEUP to wake the device
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL_MILLIS, pendingIntent);
    }

    public void stopAlarm() {
        //Log.d(TAG, "StopAlarm");

        Intent alarmIntent = new Intent(BigChillWidgetProvider.ACTION_TICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}

