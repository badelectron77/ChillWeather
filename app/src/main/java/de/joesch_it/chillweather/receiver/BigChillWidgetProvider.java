package de.joesch_it.chillweather.receiver;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.App;
import de.joesch_it.chillweather.helper.Helper;
import de.joesch_it.chillweather.ui.MainActivity;
import de.joesch_it.chillweather.weather.data.Current;
import de.joesch_it.chillweather.weather.data.Forecast;
import de.joesch_it.chillweather.weather.deserializer.CurrentDeserializer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static de.joesch_it.chillweather.helper.App.BIG_CHILL_WIDGET_BUTTON;
import static de.joesch_it.chillweather.helper.App.BIG_CHILL_WIDGET_UPDATE;
import static de.joesch_it.chillweather.helper.App.BOOT_COMPLETED;
import static de.joesch_it.chillweather.helper.App.DEFAULT_LOCATION_LATITUDE;
import static de.joesch_it.chillweather.helper.App.DEFAULT_LOCATION_LONGITUDE;
import static de.joesch_it.chillweather.helper.App.DISPLACEMENT_IN_METERS;
import static de.joesch_it.chillweather.helper.App.FASTEST_UPDATE_INTERVAL_IN_MILLIS;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_KEEP_ICON;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_KEEP_LOCATION;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_KEEP_TEMPERATURE;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_KEEP_UPDATED;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_KEEP_VALUES;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_SHOW_LOADING;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_WIDGET_REFRESH_TIME;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_FILE;
import static de.joesch_it.chillweather.helper.App.UPDATE_INTERVAL_IN_MILLIS;

public class BigChillWidgetProvider extends AppWidgetProvider
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    //<editor-fold desc="Fields">
    public static final String TAG = " ### " + BigChillWidgetProvider.class.getSimpleName() + " ###";
    private static final int FORECAST_MAX_DELAY_IN_MILLIS = 3000;
    private final Context mContext = App.getContext();
    protected Boolean mRequestingLocationUpdates;
    protected Location mCurrentLocation;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    private SharedPreferences mSharedPref = mContext.getSharedPreferences(PREF_KEY_FILE, Context.MODE_PRIVATE);
    private double mLastLocationLatitude = DEFAULT_LOCATION_LATITUDE;
    private double mLastLocationLongitude = DEFAULT_LOCATION_LONGITUDE;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    //</editor-fold>

    //<editor-fold desc="AppWidgetProvider">
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //Log.v(TAG, "onReceive() in groß");

        String action = intent.getAction();

        if (action.equals(BIG_CHILL_WIDGET_UPDATE)
                || action.equals(BOOT_COMPLETED)
                || action.equals(BIG_CHILL_WIDGET_BUTTON)
                || action.equals(Intent.ACTION_TIME_TICK)
                || action.equals(Intent.ACTION_TIME_CHANGED)
                || action.equals(Intent.ACTION_TIMEZONE_CHANGED)
                || action.equals(Intent.ACTION_DATE_CHANGED)
                ) {

            SharedPreferences.Editor editor = mSharedPref.edit();
            boolean keepValues = true;

            boolean doRefresh = mSharedPreferences.getBoolean("pref_autorefresh_weather_switch", true);
            int actualMaxDifferenceInHoursForRefresh = Integer.valueOf(mSharedPreferences.getString("autorefresh_frequency", "3"));
            if (doRefresh) {
                long nowTime = System.currentTimeMillis() / 1000L; // current time in seconds
                long lastRefreshTime = mSharedPref.getLong(PREF_KEY_BIG_WIDGET_REFRESH_TIME, nowTime);
                long maxDifferenceInSeconds = actualMaxDifferenceInHoursForRefresh * 3600L;

                //Log.v(TAG, "maxDifferenceInSeconds " + maxDifferenceInSeconds);
                //Log.v(TAG, "nowTime " + nowTime);
                //Log.v(TAG, "lastRefreshTime " + lastRefreshTime);
                //Log.v(TAG, "nowTime - lastRefreshTime " + String.valueOf(nowTime - lastRefreshTime));

                if (maxDifferenceInSeconds > 0 && nowTime - lastRefreshTime > maxDifferenceInSeconds) {
                    // refresh weather data
                    keepValues = false;
                }
            }

            if (intent.getAction().equals(BIG_CHILL_WIDGET_BUTTON)
                    || intent.getAction().equals(BOOT_COMPLETED)) {
                // show "loading" after boot completed or after widget refresh button clicked
                editor.putBoolean(PREF_KEY_BIG_SHOW_LOADING, true);
                keepValues = false;
            }

            editor.putBoolean(PREF_KEY_BIG_KEEP_VALUES, keepValues);
            editor.apply();

            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(thisAppWidget);

            for (int appWidgetID : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), getResId());
                updateBigChillWidget(context, appWidgetManager, appWidgetID, views);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        //Log.v(TAG, "onEnabled()");

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PREF_KEY_BIG_SHOW_LOADING, true);
        editor.apply();
        //Log.v(TAG, "onEnabled()");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //Log.v(TAG, "onDisabled()");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }

        SharedPreferences.Editor editor = mSharedPref.edit().putBoolean(PREF_KEY_BIG_SHOW_LOADING, true);
        editor.apply();

        context.getApplicationContext().unregisterReceiver(this);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            //Log.v(TAG, "onUpdate()");

            RemoteViews views = new RemoteViews(context.getPackageName(), getResId());

            // open MainActivity onClick on widget
            Intent openIntent = new Intent(context, MainActivity.class);
            PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.bigWidgetLayout, openPendingIntent);

            // onClick listener on refresh button
            Intent refreshIntent = new Intent(BIG_CHILL_WIDGET_BUTTON);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.bigWidgetRefreshButton, refreshPendingIntent);

            // update every minute
            context.getApplicationContext().registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_TICK));
            context.getApplicationContext().registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_CHANGED));
            context.getApplicationContext().registerReceiver(this, new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
            context.getApplicationContext().registerReceiver(this, new IntentFilter(Intent.ACTION_DATE_CHANGED));

            updateBigChillWidget(context, appWidgetManager, appWidgetId, views);
        }
    }

    public void updateBigChillWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews updateViews) {
        //Log.v(TAG, "updateChillWidget()");

        int transparency = Integer.valueOf(mSharedPreferences.getString("widget_transparency", "80"));
        updateViews.setInt(R.id.bigWidgetLayout, "setBackgroundResource", Helper.getWidgetBackgroundDrawable(transparency));

        // set the current time
        updateViews.setTextViewText(R.id.bigWidgetClockLabel, getFormattedMinuteTimeForBigWidget());

        boolean keepValues = mSharedPref.getBoolean(PREF_KEY_BIG_KEEP_VALUES, false);

        if (mSharedPref.getBoolean(PREF_KEY_BIG_SHOW_LOADING, true)) {
            showLoading(updateViews);
            if (!Helper.isNetworkAvailable(context)) {
                // no network
                updateViews.setTextViewText(R.id.bigWidgetLoadingTextView, context.getString(R.string.network_is_unavailable));

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // show latest values after some seconds, if we have them
                        if (!mSharedPref.getString(PREF_KEY_BIG_KEEP_TEMPERATURE, "").isEmpty()) {
                            loadOldValues(context, updateViews);
                            appWidgetManager.updateAppWidget(appWidgetId, updateViews);
                        }
                    }
                }, 2000);

            } else {
                // network available
                SharedPreferences.Editor editor = mSharedPref.edit().putBoolean(PREF_KEY_BIG_SHOW_LOADING, false);
                editor.apply();
            }
        }

        if (Helper.isNetworkAvailable(context) && !keepValues) {

            long nowTime = System.currentTimeMillis() / 1000L;
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putLong(PREF_KEY_BIG_WIDGET_REFRESH_TIME, nowTime);
            editor.apply();

            startLocationStuff();

            // get Forecast after some seconds of location updates
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    getForecast(appWidgetManager, updateViews, appWidgetId);
                    stopLocationUpdates();
                }
            }, FORECAST_MAX_DELAY_IN_MILLIS);
        }

        if (keepValues) {
            SharedPreferences.Editor editor = mSharedPref.edit().putBoolean(PREF_KEY_BIG_KEEP_VALUES, false);
            editor.apply();

            // load old weather values
            loadOldValues(context, updateViews);
        }
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    public String getFormattedMinuteTimeForBigWidget() {

        // default
        String format = "H:mm"; // 5:28

        //if (Helper.getCountryCode().equals(new Locale("de").getLanguage())) format = "H:mm"; // 17:28

        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        Calendar c = Calendar.getInstance();
        formatter.setTimeZone(c.getTimeZone());
        Date dateTime = new Date();
        return formatter.format(dateTime);
    }

    private void loadOldValues(Context context, RemoteViews updateViews) {

        updateViews.setTextViewText(R.id.bigWidgetDateLabel, context.getString(R.string.updated) + " " + mSharedPref.getString(PREF_KEY_BIG_KEEP_UPDATED, context.getString(R.string.please_reload)));
        updateViews.setTextViewText(R.id.bigWidgetTemperatureLabel, mSharedPref.getString(PREF_KEY_BIG_KEEP_TEMPERATURE, ""));
        updateViews.setTextViewText(R.id.bigWidgetLocationLabel, mSharedPref.getString(PREF_KEY_BIG_KEEP_LOCATION, ""));
        updateViews.setImageViewResource(R.id.bigWidgetIconImageView, Helper.getIconId(mSharedPref.getString(PREF_KEY_BIG_KEEP_ICON, "")));

        updateViews.setViewVisibility(R.id.bigWidgetTemperatureLabel, View.VISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetLocationLabel, View.VISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetIconImageView, View.VISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetLocationIconImageView, View.VISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetDateLabel, View.VISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetRefreshButton, View.VISIBLE);

        updateViews.setViewVisibility(R.id.bigWidgetLoadingTextView, View.INVISIBLE);
    }

    private int getResId() {
        int resId = R.layout.widget_chill_weather_big;
        if (Helper.isTablet()) {
            resId = R.layout.widget_chill_weather_big_tablet;
        }
        return resId;
    }

    private void showLoading(RemoteViews updateViews) {
        updateViews.setViewVisibility(R.id.bigWidgetTemperatureLabel, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetIconImageView, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetLocationLabel, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetLocationIconImageView, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetDateLabel, View.INVISIBLE);

        updateViews.setTextViewText(R.id.bigWidgetLoadingTextView, mContext.getString(R.string.loading));
        updateViews.setViewVisibility(R.id.bigWidgetRefreshButton, View.VISIBLE);
        updateViews.setViewVisibility(R.id.bigWidgetLoadingTextView, View.VISIBLE);
    }

    private void showError(RemoteViews updateViews) {
        showLoading(updateViews);
        updateViews.setTextViewText(R.id.bigWidgetLoadingTextView, mContext.getString(R.string.network_is_unavailable));
    }
    //</editor-fold>

    //<editor-fold desc="getForecast">
    private void getForecast(final AppWidgetManager appWidgetManager, final RemoteViews updateViews, final int appWidgetId) {
        //Log.v(TAG, "getForecast() in groß");

        String DARKSKY_API_KEY = mContext.getString(R.string.darksky_api_key);

        final String locale = Locale.getDefault().getLanguage();

        final String forecastUrl = "https://api.darksky.net/forecast/" + DARKSKY_API_KEY
                + "/" + mLastLocationLatitude + "," + mLastLocationLongitude
                + "?lang=" + locale + "&units=auto&exclude=minutely,hourly,daily";
        //Log.v(TAG, forecastUrl);

        if (Helper.isNetworkAvailable(mContext)) {

            final OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder().url(forecastUrl).build();
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    showError(updateViews);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            Forecast forecast;
                            String jsonData = response.body().string();
                            forecast = parseForecastDetails(jsonData);
                            Current current = forecast.getCurrent();
                            String temperatureString = current.getTemperature() + mContext.getString(R.string.degree);
                            String dateString = current.getFormattedTimeForWidget();
                            String iconString = current.getIconString();
                            int iconId = Helper.getIconId(iconString);

                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putBoolean(PREF_KEY_BIG_SHOW_LOADING, false);
                            editor.putString(PREF_KEY_BIG_KEEP_TEMPERATURE, temperatureString);
                            editor.putString(PREF_KEY_BIG_KEEP_ICON, iconString);
                            editor.putString(PREF_KEY_BIG_KEEP_UPDATED, dateString);
                            editor.apply();

                            updateViews.setTextViewText(R.id.bigWidgetDateLabel, mContext.getString(R.string.updated) + " " + dateString);
                            updateViews.setTextViewText(R.id.bigWidgetTemperatureLabel, temperatureString);
                            updateViews.setImageViewResource(R.id.bigWidgetIconImageView, iconId);

                            updateViews.setViewVisibility(R.id.bigWidgetTemperatureLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.bigWidgetIconImageView, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.bigWidgetDateLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.bigWidgetRefreshButton, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.bigWidgetLocationLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.bigWidgetLocationIconImageView, View.VISIBLE);

                            updateViews.setViewVisibility(R.id.bigWidgetLoadingTextView, View.INVISIBLE);

                            appWidgetManager.updateAppWidget(appWidgetId, updateViews);

                        } catch (JSONException | NullPointerException e) {
                            showError(updateViews);
                        }
                    }
                }
            });
            getLocationNameFromCoordinates(appWidgetManager, updateViews, appWidgetId);
        } else {
            showError(updateViews);
        }
    }

    private void getLocationNameFromCoordinates(final AppWidgetManager appWidgetManager, final RemoteViews updateViews, final int appWidgetId) {

        String GOOGLE_GEOCODING_API_KEY = mContext.getString(R.string.google_geocoding_api_key);

        final String geocodingUrl = "https://maps.googleapis.com/maps/api/geocode/json"
                + "?latlng=" + mLastLocationLatitude + "," + mLastLocationLongitude
                + "&key=" + GOOGLE_GEOCODING_API_KEY;

        //Log.v(TAG, geocodingUrl);

        if (Helper.isNetworkAvailable(mContext)) {

            final OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder().url(geocodingUrl).build();
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    showError(updateViews);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {

                    if (response.isSuccessful()) {
                        try {
                            String jsonLocData = response.body().string();

                            JSONObject locationData = new JSONObject(jsonLocData);
                            JSONArray results = locationData.getJSONArray("results");
                            String locationName;
                            if (results.length() > 1) {
                                JSONObject locationObj = results.getJSONObject(0);
                                String tmpLocationName = locationObj.getString("formatted_address");
                                locationName = Helper.getFormattedLocationName(tmpLocationName);
                            } else {
                                locationName = mContext.getString(R.string.no_location_available);
                            }

                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putBoolean(PREF_KEY_BIG_SHOW_LOADING, false);
                            editor.putString(PREF_KEY_BIG_KEEP_LOCATION, locationName);
                            editor.apply();

                            int length = locationName.length();
                            int maxL = 15;
                            if (Helper.isTablet()) {
                                maxL = 25;
                            }
                            if (length > maxL) {
                                locationName = locationName.substring(0, Math.min(length, maxL)) + "...";
                            }
                            updateViews.setTextViewText(R.id.bigWidgetLocationLabel, locationName);
                            updateViews.setViewVisibility(R.id.bigWidgetLocationLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.bigWidgetLocationIconImageView, View.VISIBLE);
                            appWidgetManager.updateAppWidget(appWidgetId, updateViews);

                        } catch (JSONException | NullPointerException e) {
                            showError(updateViews);
                        }
                    }
                }
            });
        } else {
            showError(updateViews);
        }
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Current.class, new CurrentDeserializer());
        Gson gson = gsonBuilder.create();

        final Forecast forecast = new Forecast();
        forecast.setCurrent(gson.fromJson(jsonData, Current.class));
        return forecast;
    }
    //</editor-fold>

    //<editor-fold desc="Location">
    private void startLocationStuff() {
        mRequestingLocationUpdates = true;
        if (playServicesAvailable()) {
            buildGoogleApiClient();
            createLocationRequest();
            buildLocationSettingsRequest();
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private boolean playServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(mContext);
        return result == ConnectionResult.SUCCESS;
    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLIS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLIS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT_IN_METERS);
        }
    }

    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, BigChillWidgetProvider.this);
                        } catch (SecurityException e) {
                            //
                        }
                        break;
                }
            }
        });
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,
                    this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    mRequestingLocationUpdates = false;
                }
            });
        }
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {
            try {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException e) {
                //
            }
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        if (mCurrentLocation != null) {
            mLastLocationLatitude = mCurrentLocation.getLatitude();
            mLastLocationLongitude = mCurrentLocation.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        mLastLocationLatitude = mCurrentLocation.getLatitude();
        mLastLocationLongitude = mCurrentLocation.getLongitude();
    }
    //</editor-fold>
}
