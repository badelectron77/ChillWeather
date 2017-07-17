package de.joesch_it.chillweather.ui;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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
import java.util.Calendar;
import java.util.Locale;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.App;
import de.joesch_it.chillweather.helper.Helper;
import de.joesch_it.chillweather.weather.Forecast;
import de.joesch_it.chillweather.weather.data.Current;
import de.joesch_it.chillweather.weather.deserializer.CurrentDeserializer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;
import static de.joesch_it.chillweather.helper.App.BOOT_COMPLETED;
import static de.joesch_it.chillweather.helper.App.CHILL_WIDGET_UPDATE;
import static de.joesch_it.chillweather.helper.App.CHILL_WIDGET_UPDATE2;
import static de.joesch_it.chillweather.helper.App.DEFAULT_LOCATION_LATITUDE;
import static de.joesch_it.chillweather.helper.App.DEFAULT_LOCATION_LONGITUDE;
import static de.joesch_it.chillweather.helper.App.DISPLACEMENT_IN_METERS;
import static de.joesch_it.chillweather.helper.App.FASTEST_UPDATE_INTERVAL_IN_MILLIS;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_FILE;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_KEEP_ICON;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_KEEP_LOCATION;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_KEEP_TEMPERATURE;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_KEEP_UPDATED;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_KEEP_VALUES;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_OLD_ALARM_MANAGER_REMOVED;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_SHOW_LOADING;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_SOMETHING_WRITTEN;
import static de.joesch_it.chillweather.helper.App.UPDATE_INTERVAL_IN_MILLIS;
import static de.joesch_it.chillweather.helper.App.WIDGET_BUTTON;

public class ChillWidgetProvider extends AppWidgetProvider implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    //<editor-fold desc="Fields">
    public static final String TAG = " ### " + ChillWidgetProvider.class.getSimpleName() + " ###";
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
        //Log.v(TAG, "onReceive()");

        if (intent.getAction().equals(CHILL_WIDGET_UPDATE2)
                || intent.getAction().equals(BOOT_COMPLETED)
                || intent.getAction().equals(WIDGET_BUTTON)) {

            SharedPreferences.Editor editor = mSharedPref.edit();

            if (intent.getAction().equals(WIDGET_BUTTON)) {
                editor.putBoolean(PREF_KEY_SHOW_LOADING, true);
                editor.apply();
            }

            if (intent.getAction().equals(BOOT_COMPLETED)) {
                editor.putBoolean(PREF_KEY_SOMETHING_WRITTEN, false);
                editor.apply();
            }

            ComponentName thisAppWidget = new ComponentName(mContext.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for (int appWidgetID : ids) {
                RemoteViews views = new RemoteViews(context.getPackageName(), getResId());
                updateChillWidget(mContext, appWidgetManager, appWidgetID, views);
            }
        }
    }

    private PendingIntent getChillWidgetUpdateIntent() {
        Intent intent = new Intent(CHILL_WIDGET_UPDATE);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getChillWidgetUpdateIntent2() {
        Intent intent = new Intent(CHILL_WIDGET_UPDATE2);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PREF_KEY_SHOW_LOADING, true);
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

        SharedPreferences.Editor editor = mSharedPref.edit().putBoolean(PREF_KEY_SOMETHING_WRITTEN, false);
        editor.apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getChillWidgetUpdateIntent2());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.v(TAG, "onUpdate()");

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), getResId());

            // open MainActivity onClick on widget
            Intent openIntent = new Intent(context, MainActivity.class);
            PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, 0);
            views.setOnClickPendingIntent(R.id.widgetLayout, openPendingIntent);

            // onClick listener on refresh button
            Intent refreshIntent = new Intent(WIDGET_BUTTON);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widgetRefreshButton, refreshPendingIntent);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // completely remove old AlarmManager PendingIntent ONCE
            if(mSharedPref.getBoolean(PREF_KEY_OLD_ALARM_MANAGER_REMOVED, false)) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putBoolean(PREF_KEY_OLD_ALARM_MANAGER_REMOVED, true);
                editor.apply();
                alarmManager.cancel(getChillWidgetUpdateIntent());
            }

            if (mSharedPreferences.getBoolean("pref_autorefresh_weather_switch", true)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int maxDifferenceInHours = Integer.valueOf(mSharedPreferences.getString("autorefresh_frequency", "3"));
                calendar.add(Calendar.HOUR, maxDifferenceInHours);
                alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), maxDifferenceInHours * 3600 * 1000, getChillWidgetUpdateIntent2());
                // DEBUG: Minutes instead of hours
                //alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), maxDifferenceInHours * 60 * 1000, getChillWidgetUpdateIntent2());
            } else {
                alarmManager.cancel(getChillWidgetUpdateIntent2());
            }

            updateChillWidget(context, appWidgetManager, appWidgetId, views);
        }
    }

    public void updateChillWidget(Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews updateViews) {
        //Log.v(TAG, "updateChillWidget()");

        int transparency = Integer.valueOf(mSharedPreferences.getString("widget_transparency", "80"));
        updateViews.setInt(R.id.widgetLayout, "setBackgroundResource", Helper.getWidgetBackgroundDrawable(transparency));

        boolean keepValues = mSharedPref.getBoolean(PREF_KEY_KEEP_VALUES, false);

        if (mSharedPref.getBoolean(PREF_KEY_SHOW_LOADING, true) || !mSharedPref.getBoolean(PREF_KEY_SOMETHING_WRITTEN, false)) {
            showLoading(updateViews);
            //Log.v(TAG, "showLoading");
            if (!Helper.isNetworkAvailable(context)) {
                updateViews.setTextViewText(R.id.widgetLoadingTextView, context.getString(R.string.network_is_unavailable));
            } else {
                SharedPreferences.Editor editor = mSharedPref.edit().putBoolean(PREF_KEY_SHOW_LOADING, false);
                editor.apply();
            }
        }

        if (Helper.isNetworkAvailable(context) && !keepValues) {
            //Log.v(TAG, "!keepValues");
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
            //Toast.makeText(context, "updateChillWidget() without forecast update", Toast.LENGTH_SHORT).show();
            //Log.v(TAG, "only orientation change");
            // only on orientation or design change
            SharedPreferences.Editor editor = mSharedPref.edit().putBoolean(PREF_KEY_KEEP_VALUES, false);
            editor.apply();

            updateViews.setTextViewText(R.id.widgetDateLabel, context.getString(R.string.updated) + " " + mSharedPref.getString(PREF_KEY_KEEP_UPDATED, context.getString(R.string.please_reload)));
            updateViews.setTextViewText(R.id.widgetTemperatureLabel, mSharedPref.getString(PREF_KEY_KEEP_TEMPERATURE, ""));
            updateViews.setTextViewText(R.id.widgetLocationLabel, mSharedPref.getString(PREF_KEY_KEEP_LOCATION, ""));
            updateViews.setImageViewResource(R.id.widgetIconImageView, Helper.getIconId(mSharedPref.getString(PREF_KEY_KEEP_ICON, "")));

            updateViews.setViewVisibility(R.id.widgetTemperatureLabel, View.VISIBLE);
            updateViews.setViewVisibility(R.id.widgetLocationLabel, View.VISIBLE);
            updateViews.setViewVisibility(R.id.widgetIconImageView, View.VISIBLE);
            updateViews.setViewVisibility(R.id.widgetLocationIconImageView, View.VISIBLE);
            updateViews.setViewVisibility(R.id.widgetDateLabel, View.VISIBLE);
            updateViews.setViewVisibility(R.id.widgetRefreshButton, View.VISIBLE);

            updateViews.setViewVisibility(R.id.widgetLoadingTextView, View.INVISIBLE);

        }
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    private int getResId() {
        int resId = R.layout.widget_chill_weather;
        if (Helper.isTablet()) {
            resId = R.layout.widget_chill_weather_tablet;
        }
        return resId;
    }

    private void showLoading(RemoteViews updateViews) {
        updateViews.setViewVisibility(R.id.widgetTemperatureLabel, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.widgetIconImageView, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.widgetLocationLabel, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.widgetLocationIconImageView, View.INVISIBLE);
        updateViews.setViewVisibility(R.id.widgetDateLabel, View.INVISIBLE);

        updateViews.setTextViewText(R.id.widgetLoadingTextView, mContext.getString(R.string.loading));
        updateViews.setViewVisibility(R.id.widgetRefreshButton, View.VISIBLE);
        updateViews.setViewVisibility(R.id.widgetLoadingTextView, View.VISIBLE);
    }

    private void showError(RemoteViews updateViews) {
        showLoading(updateViews);
        updateViews.setTextViewText(R.id.widgetLoadingTextView, mContext.getString(R.string.network_is_unavailable));
    }
    //</editor-fold>

    //<editor-fold desc="getForecast">
    private void getForecast(final AppWidgetManager appWidgetManager, final RemoteViews updateViews, final int appWidgetId) {

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
                            editor.putBoolean(PREF_KEY_SOMETHING_WRITTEN, true);
                            editor.putString(PREF_KEY_KEEP_TEMPERATURE, temperatureString);
                            editor.putString(PREF_KEY_KEEP_ICON, iconString);
                            editor.putString(PREF_KEY_KEEP_UPDATED, dateString);
                            editor.apply();

                            updateViews.setTextViewText(R.id.widgetDateLabel, mContext.getString(R.string.updated) + " " + dateString);
                            updateViews.setTextViewText(R.id.widgetTemperatureLabel, temperatureString);
                            updateViews.setImageViewResource(R.id.widgetIconImageView, iconId);

                            updateViews.setViewVisibility(R.id.widgetTemperatureLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.widgetIconImageView, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.widgetDateLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.widgetRefreshButton, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.widgetLocationLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.widgetLocationIconImageView, View.VISIBLE);

                            updateViews.setViewVisibility(R.id.widgetLoadingTextView, View.INVISIBLE);

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
                            editor.putBoolean(PREF_KEY_SOMETHING_WRITTEN, true);
                            editor.putString(PREF_KEY_KEEP_LOCATION, locationName);
                            editor.apply();

                            int length = locationName.length();
                            int maxL = 15;
                            if (Helper.isTablet()) {
                                maxL = 25;
                            }
                            if (length >= maxL) {
                                locationName = locationName.substring(0, Math.min(length, maxL)) + "...";
                            }
                            updateViews.setTextViewText(R.id.widgetLocationLabel, locationName);
                            updateViews.setViewVisibility(R.id.widgetLocationLabel, View.VISIBLE);
                            updateViews.setViewVisibility(R.id.widgetLocationIconImageView, View.VISIBLE);
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
        boolean sentJsonAlert = false;
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
            //Log.v(TAG, "buildGoogleApiClient()");
        }
    }

    protected void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLIS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLIS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT_IN_METERS);
            //Log.v(TAG, "createLocationRequest()");
        }
    }

    protected void startLocationUpdates() {
        //Log.v(TAG, "startLocationUpdates()");
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ChillWidgetProvider.this);
                        } catch (SecurityException e) {
                            showPermissionNotification();
                        }
                        break;
                }
                //updateUI();
            }
        });
    }

    private void showPermissionNotification() {
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(mContext)
                .setContentTitle(mContext.getText(R.string.need_multiple_permissions))
                .setContentText(mContext.getText(R.string.this_app_needs_location_permission))
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher_white)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    protected void stopLocationUpdates() {
        //Log.v(TAG, "stopLocationUpdates()");
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
        //Log.v(TAG, "onConnected()");
        if (mCurrentLocation == null) {
            try {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException e) {
                showPermissionNotification();
            }
        }
        if (mRequestingLocationUpdates) {
            //Log.v(TAG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }
        if (mCurrentLocation != null) {
            mLastLocationLatitude = mCurrentLocation.getLatitude();
            mLastLocationLongitude = mCurrentLocation.getLongitude();
        }
        //getForecast(mLastLocationLatitude, mLastLocationLongitude);
        //Log.i(TAG, "getForecast() in onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        // after reboot of phone...
        /*if (mLastLocationLatitude == DEFAULT_LOCATION_LATITUDE && mLastLocationLongitude == DEFAULT_LOCATION_LONGITUDE) {
            getForecast(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            Log.i(TAG, "getForecast() in onLocationChanged()");
        }*/

        mLastLocationLatitude = mCurrentLocation.getLatitude();
        mLastLocationLongitude = mCurrentLocation.getLongitude();

        // Köln
        //mLastLocationLatitude = 50.937101;
        //mLastLocationLongitude = 6.958117;

        // Weiler bei Monzingen (test with spaces in city name)
        //mLastLocationLatitude = 49.8278;
        //mLastLocationLongitude = 7.53834;

        // Anchorage
        //mLastLocationLatitude = 61.199972;
        //mLastLocationLongitude = -149.898872;

        // Southern pacific, in the middle of nowhere
        //mLastLocationLatitude = -65.487125;
        //mLastLocationLongitude = -152.912444;

        // Forsinard, Scotland
        //mLastLocationLatitude = 58.358318;
        //mLastLocationLongitude = -3.896534;

        // Siberia
        //mLastLocationLatitude = 76.217848;
        //mLastLocationLongitude = 110.347490;
    }
    //</editor-fold>
}

