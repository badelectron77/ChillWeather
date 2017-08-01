package de.joesch_it.chillweather.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import de.joesch_it.chillweather.receiver.BigChillWidgetProvider;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BigWidgetRepeatingJob extends JobService {

    private final static String TAG = " ### " + BigWidgetRepeatingJob.class.getSimpleName() + " ###";

    @Override
    public boolean onStartJob(JobParameters params) {
        //Log.d(TAG, "onStartJob");
        Intent intent=new Intent(BigChillWidgetProvider.JOB_TICK);
        sendBroadcast(intent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
