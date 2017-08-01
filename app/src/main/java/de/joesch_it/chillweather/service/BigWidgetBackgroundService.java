package de.joesch_it.chillweather.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import de.joesch_it.chillweather.receiver.BigChillWidgetProvider;


public class BigWidgetBackgroundService extends Service {

    private static final String TAG = " ### " + BigWidgetBackgroundService.class.getSimpleName() + " ###";
    private static BroadcastReceiver mMinuteTickReceiver;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (intent.hasExtra("SHUTDOWN")) {
                if (intent.getBooleanExtra("SHUTDOWN", false)) {

                    if(mMinuteTickReceiver!=null) {
                        unregisterReceiver(mMinuteTickReceiver);
                        mMinuteTickReceiver = null;
                    }
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
        }

        if(mMinuteTickReceiver==null) {
            registerOnTickReceiver();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        if(mMinuteTickReceiver!=null) {
            unregisterReceiver(mMinuteTickReceiver);
            mMinuteTickReceiver = null;
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerOnTickReceiver() {
        mMinuteTickReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                Intent timeTick=new Intent(BigChillWidgetProvider.ACTION_TICK);
                sendBroadcast(timeTick);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mMinuteTickReceiver, filter);
    }
}