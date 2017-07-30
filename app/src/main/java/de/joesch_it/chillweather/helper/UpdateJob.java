package de.joesch_it.chillweather.helper;


import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import static de.joesch_it.chillweather.helper.Helper.updateBigWidget;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class UpdateJob extends JobService {

    public static int JOB_ID=9;

    @Override
    public boolean onStartJob(JobParameters params) {

        updateBigWidget();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
