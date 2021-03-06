package de.joesch_it.chillweather.helper;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;

import java.lang.ref.WeakReference;

import de.joesch_it.chillweather.R;

import static de.joesch_it.chillweather.helper.Helper.updateBigWidget;
import static de.joesch_it.chillweather.helper.Helper.updateSmallWidget;


public class App extends Application {

    public static final String BUILD = "11.08.2017 13:56";
    public static final String STORE_URL = "https://play.google.com/store/apps/details?id=de.joesch_it.chillweather";
    public static final String TAG = " ### " + App.class.getSimpleName() + " ###";
    public static final String POSITION_TOMORROW = "POSITION_TOMORROW";
    public static final String POSITION_OVERMORROW = "POSITION_OVERMORROW";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    public static final String PREF_KEY_FILE = "de.joesch_it.chillweather.PREFERENCE_KEY_FILE";
    public static final String PREF_KEY_COLORED_ICONS = "PREF_KEY_COLORED_ICONS";
    public static final String PREF_KEY_AUTOREFRESH_SWITCH = "PREF_KEY_AUTOREFRESH_SWITCH";
    public static final String PREF_KEY_AUTOREFRESH_FREQUENCY = "PREF_KEY_AUTOREFRESH_FREQUENCY";
    public static final String PREF_KEY_APP_THEME = "PREF_KEY_APP_THEME";
    public static final String PREF_KEY_USE_GPS = "PREF_KEY_USE_GPS";
    public static final String PREF_KEY_FOUND_LAT = "PREF_KEY_FOUND_LAT";
    public static final String PREF_KEY_FOUND_LNG = "PREF_KEY_FOUND_LNG";

    // small widget
    public static final String PREF_KEY_WIDGET_TRANSPARENCY = "PREF_KEY_WIDGET_TRANSPARENCY";
    public static final String PREF_KEY_SHOW_LOADING = "PREF_KEY_SHOW_LOADING";
    public static final String PREF_KEY_KEEP_VALUES = "PREF_KEY_KEEP_VALUES";
    public static final String PREF_KEY_KEEP_TEMPERATURE = "PREF_KEY_KEEP_TEMPERATURE";
    public static final String PREF_KEY_KEEP_ICON = "PREF_KEY_KEEP_ICON";
    public static final String PREF_KEY_KEEP_LOCATION = "PREF_KEY_KEEP_LOCATION";
    public static final String PREF_KEY_KEEP_UPDATED = "PREF_KEY_KEEP_UPDATED";
    public static final String CHILL_WIDGET_UPDATE2 = "de.joesch_it.chillweather.CHILL_WIDGET_UPDATE2";
    public static final String CHILL_WIDGET_BUTTON = "de.joesch_it.chillweather.CHILL_WIDGET_BUTTON";

    // big widget
    public static final String PREF_KEY_BIG_SHOW_LOADING = "PREF_KEY_BIG_SHOW_LOADING";
    public static final String PREF_KEY_BIG_WIDGET_REFRESH_TIME = "PREF_KEY_BIG_WIDGET_REFRESH_TIME";
    public static final String PREF_KEY_BIG_KEEP_VALUES = "PREF_KEY_BIG_KEEP_VALUES";
    public static final String PREF_KEY_BIG_KEEP_TEMPERATURE = "PREF_KEY_BIG_KEEP_TEMPERATURE";
    public static final String PREF_KEY_BIG_KEEP_ICON = "PREF_KEY_BIG_KEEP_ICON";
    public static final String PREF_KEY_BIG_KEEP_LOCATION = "PREF_KEY_BIG_KEEP_LOCATION";
    public static final String PREF_KEY_BIG_KEEP_UPDATED = "PREF_KEY_BIG_KEEP_UPDATED";
    public static final String BIG_CHILL_WIDGET_BUTTON = "de.joesch_it.chillweather.BIG_CHILL_WIDGET_BUTTON";

    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final int DISPLACEMENT_IN_METERS = 100;
    public static final long FORECAST_DELAY_IN_MILLIS_MAIN_ACTIVITY = 1500;
    public static final long FORECAST_DELAY_IN_MILLIS_WIDGETS = 1500;
    public static final long UPDATE_INTERVAL_IN_MILLIS = 10000; // The desired interval for location updates. Inexact.
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLIS = UPDATE_INTERVAL_IN_MILLIS / 2;
    public static final double DEFAULT_LOCATION_LATITUDE = -65.487125; // in the middle of nowhere...
    public static final double DEFAULT_LOCATION_LONGITUDE = -152.912444;
    public static final int PERMISSION_REQUEST_CODE_CALLBACK = 100;
    public static final int PERMISSION_REQUEST_CODE_SETTING = 101;
    public static String[] permissionsRequired = new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private static WeakReference<Context> mContext;

    public static Context getContext() {
        return mContext.get();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<Context>(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRAConfiguration config = null;
        try {
            config = new ConfigurationBuilder(this)
                    .setFormUri(getString(R.string.acra_uri))
                    .build();
        } catch (ACRAConfigurationException e) {
            //Log.e(MainActivity.TAG, "ACRAConfigurationException", e);
        }

        if (config != null) {
            ACRA.init(this, config);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Log.v(TAG, "onConfigurationChanged()");
        updateSmallWidget(this);
        updateBigWidget(this);
    }
}
