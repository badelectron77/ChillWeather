package de.joesch_it.chillweather.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import de.joesch_it.chillweather.receiver.BigChillWidgetProvider;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BigWidgetRepeatingJob extends JobService {

    private final static String TAG = "### WidgetRepeatingJob";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");
        Intent intent=new Intent(BigChillWidgetProvider.JOB_TICK);
        sendBroadcast(intent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
