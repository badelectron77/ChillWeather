package de.joesch_it.chillweather.ui;


import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.Helper;
import de.joesch_it.chillweather.weather.Forecast;
import de.joesch_it.chillweather.weather.adapters.DayAdapter;
import de.joesch_it.chillweather.weather.data.Current;
import de.joesch_it.chillweather.weather.data.Day;
import de.joesch_it.chillweather.weather.data.Hour;
import de.joesch_it.chillweather.weather.deserializer.CurrentDeserializer;
import de.joesch_it.chillweather.weather.deserializer.DayDeserializer;
import de.joesch_it.chillweather.weather.deserializer.HourDeserializer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static de.joesch_it.chillweather.helper.App.DEFAULT_LOCATION_LATITUDE;
import static de.joesch_it.chillweather.helper.App.DEFAULT_LOCATION_LONGITUDE;
import static de.joesch_it.chillweather.helper.App.DISPLACEMENT_IN_METERS;
import static de.joesch_it.chillweather.helper.App.FASTEST_UPDATE_INTERVAL_IN_MILLIS;
import static de.joesch_it.chillweather.helper.App.HOURLY_FORECAST;
import static de.joesch_it.chillweather.helper.App.PERMISSION_REQUEST_CODE_CALLBACK;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_AUTOREFRESH_FREQUENCY;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_AUTOREFRESH_SWITCH;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_COLORED_ICONS;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_FILE;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_WIDGET_TRANSPARENCY;
import static de.joesch_it.chillweather.helper.App.STORE_URL;
import static de.joesch_it.chillweather.helper.App.UPDATE_INTERVAL_IN_MILLIS;
import static de.joesch_it.chillweather.helper.App.permissionsRequired;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    //<editor-fold desc="Fields">
    public static final String TAG = " ### " + MainActivity.class.getSimpleName() + " ###";
    protected static final int STATUS_GETTING_WEATHER = 200;
    protected static final int STATUS_SHOW_WEATHER = 201;
    protected static final int STATUS_NO_NETWORK = 202;
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES";
    protected final static String KEY_LOCATION = "KEY_LOCATION";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1; // Constant used in the location settings dialog.
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int FORECAST_MAX_DELAY_IN_MILLIS = 1500;
    static public Hour[] mHourlyForecast;
    protected Boolean mRequestingLocationUpdates;
    protected Location mCurrentLocation;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected SharedPreferences mSharedPreferences;
    @BindView(R.id.darkskyImageView)
    ImageView mDarkskyImageView;
    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @BindView(R.id.humidityValue)
    TextView mHumidityValue;
    @BindView(R.id.precipValue)
    TextView mPrecipValue;
    @BindView(R.id.summaryLabel)
    TextView mSummaryLabel;
    @BindView(R.id.timeLabel)
    TextView mTimeLabel;
    @BindView(R.id.iconImageView)
    ImageView mIconImageView;
    @BindView(R.id.degreeTextView)
    TextView mDegreeTextView;
    @BindView(R.id.locationLabel)
    TextView mLocationLabel;
    @BindView(R.id.networkIsUnavailable)
    TextView mNetworkIsUnavailable;
    @BindView(R.id.dailyListRecyclerView)
    RecyclerView mDailyListRecyclerView;
    @BindView(R.id.nestedScrollView)
    NestedScrollView mNestedScrollView;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.location_date_linear_layout)
    LinearLayout mLocationIconDateLinearLayout;
    @BindView(R.id.splashImageView)
    ImageView mSplashImageView;
    @BindView(R.id.humidity_rain_linear_layout)
    LinearLayout mhumidityRainLinearLayout;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private String GOOGLE_GEOCODING_API_KEY;
    private String DARKSKY_API_KEY;
    private boolean mSentJsonAlert;
    private String mLocationName = "";
    private double mLastLocationLatitude = DEFAULT_LOCATION_LATITUDE;
    private double mLastLocationLongitude = DEFAULT_LOCATION_LONGITUDE;
    private Forecast mForecast;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Gson mGson;
    private List<Day> mDayList = new ArrayList<>();
    private DayAdapter mAdapter;
    private boolean mMenuRefreshClicked = false;
    private boolean mFirstStart;
    private boolean mColoredIcons;
    private boolean mTempColoredBackgrounds;
    private String mAppTheme;
    private boolean mTempPhotoBackground;
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAppTheme = mSharedPreferences.getString("app_theme", "2");
        switch (mAppTheme) {
            case "0":
                setTheme(R.style.WeHaveAToolbarAndNoActionBar);
                setContentView(R.layout.activity_main);
                makeNormalToolBar();
                break;
            case "1":
                setTheme(R.style.WeHaveAToolbarAndNoActionBarOrange);
                setContentView(R.layout.activity_main);
                makeNormalToolBar();
                break;
            default:
                setTheme(R.style.WeHaveAToolbarAndNoActionBar);
                setContentView(R.layout.activity_main);
                makeToolBarTransparent();
                // Status Bar Color
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Color.parseColor("#25000000"));
                }
                break;
        }

        ButterKnife.bind(this);
        GOOGLE_GEOCODING_API_KEY = getString(R.string.google_geocoding_api_key);
        DARKSKY_API_KEY = getString(R.string.darksky_api_key);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Current.class, new CurrentDeserializer());
        gsonBuilder.registerTypeAdapter(Day[].class, new DayDeserializer());
        gsonBuilder.registerTypeAdapter(Hour[].class, new HourDeserializer());
        mGson = gsonBuilder.create();
        mSentJsonAlert = false;
        mRequestingLocationUpdates = true;
        updateValuesFromBundle(savedInstanceState);
        if (playServicesAvailable()) {
            buildGoogleApiClient();
            createLocationRequest();
            buildLocationSettingsRequest();
        }
        mAdapter = new DayAdapter(mDayList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mDailyListRecyclerView.setLayoutManager(layoutManager);
        mDailyListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mDailyListRecyclerView.setAdapter(mAdapter);

        // setting actual values to hidden preferences
        SharedPreferences sharedPref = getSharedPreferences(PREF_KEY_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PREF_KEY_COLORED_ICONS, mSharedPreferences.getBoolean("colored_icons_switch", true));
        editor.putBoolean(PREF_KEY_AUTOREFRESH_SWITCH, mSharedPreferences.getBoolean("pref_autorefresh_weather_switch", true));
        editor.putString(PREF_KEY_AUTOREFRESH_FREQUENCY, mSharedPreferences.getString("autorefresh_frequency", "3"));
        editor.putString(PREF_KEY_WIDGET_TRANSPARENCY, mSharedPreferences.getString("widget_transparency", "80"));
        editor.apply();

        mColoredIcons = mSharedPreferences.getBoolean("colored_icons_switch", true);
        mTempColoredBackgrounds = mSharedPreferences.getBoolean("temperature_colored_background_switch", true);
        mTempPhotoBackground = mSharedPreferences.getBoolean("photo_background_switch", true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mDailyListRecyclerView.addItemDecoration(dividerItemDecoration);

        mFirstStart = true;
        toggleRefresh(STATUS_GETTING_WEATHER);
        getDelayedForecast();

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getForecast();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mSharedPreferences.getString("app_theme", "2").equals("2")) {
            makeToolBarTransparent();
        }

        scrollToTop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && playServicesAvailable() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        boolean resumeRefresh = false;

        // if "no network available" screen visible, do refresh
        if (mNetworkIsUnavailable.getVisibility() == View.VISIBLE && Helper.isNetworkAvailable(this)) {
            resumeRefresh = true;
        }

        // check if a time-based refresh is necessary according to the user preferences
        boolean doRefresh = mSharedPreferences.getBoolean("pref_autorefresh_weather_switch", true);
        int actualMaxDifferenceInHoursForRefresh = Integer.valueOf(mSharedPreferences.getString("autorefresh_frequency", "3"));
        if (!mFirstStart && doRefresh && mForecast != null) {
            long storedWeatherTime = mForecast.getCurrent().getTime();
            long nowTime = System.currentTimeMillis() / 1000L;
            long maxDifferenceInSeconds = actualMaxDifferenceInHoursForRefresh * 3600L;
            if (maxDifferenceInSeconds > 0 && nowTime - storedWeatherTime > maxDifferenceInSeconds) {
                resumeRefresh = true;
            }
        }

        // refresh weather data if user changed icon set in settings
        boolean actualColoredIcons = mSharedPreferences.getBoolean("colored_icons_switch", true);
        if (mColoredIcons != actualColoredIcons) {
            mColoredIcons = actualColoredIcons;
            resumeRefresh = true;
        }

        // refresh weather data if user changed background colors in settings
        boolean actualTempColoredBackgrounds = mSharedPreferences.getBoolean("temperature_colored_background_switch", true);
        if (mTempColoredBackgrounds != actualTempColoredBackgrounds) {
            mTempColoredBackgrounds = actualTempColoredBackgrounds;
            resumeRefresh = true;
        }

        // refresh photo background if user changed background settings
        boolean actualTempPhotoBackground = mSharedPreferences.getBoolean("photo_background_switch", true);
        if (mTempPhotoBackground != actualTempPhotoBackground) {
            mTempPhotoBackground = actualTempPhotoBackground;
            resumeRefresh = true;
        }

        if (resumeRefresh) {
            // get a delayed Forecast do get the correct location first
            getDelayedForecast();
        }
    }

    private void getDelayedForecast() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                getForecast();
            }
        }, FORECAST_MAX_DELAY_IN_MILLIS);
    }

    private void getForecast() {

        final String locale = Locale.getDefault().getLanguage();

        final String forecastUrl = "https://api.darksky.net/forecast/" + DARKSKY_API_KEY
                + "/" + mLastLocationLatitude + "," + mLastLocationLongitude
                + "?lang=" + locale + "&units=auto&exclude=minutely";
        //Log.v(TAG, forecastUrl);

        // Köln
        //mLastLocationLatitude = 50.937101
        //mLastLocationLongitude = 6.958117

        // Weiler bei Monzingen (test with spaces in city name)
        //mLastLocationLatitude = 49.8278
        //mLastLocationLongitude = 7.53834

        // Anchorage
        //mLastLocationLatitude = 61.199972
        //mLastLocationLongitude = -149.898872

        // Southern pacific, in the middle of nowhere
        //mLastLocationLatitude = -65.487125
        //mLastLocationLongitude = -152.912444

        // Forsinard, Scotland
        //mLastLocationLatitude = 58.358318
        //mLastLocationLongitude = -3.896534

        // Siberia
        //mLastLocationLatitude = 76.217848
        //mLastLocationLongitude = 110.347490

        // Paleochora
        //mLastLocationLatitude = 35.231418
        //mLastLocationLongitude = 23.680851

        if (Helper.isNetworkAvailable(this)) {

            toggleRefresh(STATUS_GETTING_WEATHER);

            final OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder().url(forecastUrl).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh(STATUS_NO_NETWORK);
                        }
                    });
                    mSentJsonAlert = true;
                    //Log.e(TAG, "$$$" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {

                    if (response.isSuccessful()) {
                        try {
                            String jsonData = response.body().string();
                            mForecast = parseForecastDetails(jsonData);
                            mHourlyForecast = mForecast.getHourlyForecast();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toggleRefresh(STATUS_SHOW_WEATHER);
                                    updateDisplay();
                                }
                            });

                        } catch (JSONException | NullPointerException e) {
                            //Log.e(TAG, "JSON Exception caught: ", e);
                        }
                    } else {
                        mSentJsonAlert = true;
                        //Log.e(TAG, "IOException caught: Unexpected code " + response);
                    }
                }
            });
            getLocationNameFromCoordinates();
        } else {
            // no network available
            alertUserAboutNoNetwork();
            if (!mMenuRefreshClicked) {
                // After starting the app show the "no network" screen. But don't show this screen
                // when the user clicked the refresh button! He rather reads old data than no data.
                toggleRefresh(STATUS_NO_NETWORK);
            }
        }
        if (mSentJsonAlert) {
            // Problems with json request.
            mSentJsonAlert = false;
            alertUserAboutNoNetwork();
        }
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {

        final Forecast forecast = new Forecast();
        forecast.setCurrent(mGson.fromJson(jsonData, Current.class));
        forecast.setDailyForecast(mGson.fromJson(jsonData, Day[].class));
        forecast.setHourlyForecast(mGson.fromJson(jsonData, Hour[].class));
        return forecast;
    }

    //<editor-fold desc="Location">
    private void updateValuesFromBundle(Bundle savedInstanceState) { // Updates fields based on data stored in the bundle.
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
        }
    }

    private boolean playServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
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

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        //Log.i(TAG, "User chose not to make required location settings changes.");
                        //mRequestingLocationUpdates = false;
                        break;
                }
                break;
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
                        //Log.i(TAG, "All location settings are satisfied.");

                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);
                        } catch (SecurityException e) {
                            //Show Information about why you need the permission
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.need_multiple_permissions);
                            builder.setMessage(R.string.this_app_needs_location_permission);
                            builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings ");
                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            //Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                }
                //updateUI();
            }
        });
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state.
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

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else {
            buildGoogleApiClient();
        }
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mCurrentLocation == null) {
            try {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException e) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.need_multiple_permissions));
                builder.setMessage(getString(R.string.this_app_needs_location_permission));
                builder.setPositiveButton(getString(R.string.grant), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        }
        if (mRequestingLocationUpdates) {
            //Log.i(TAG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }
        if (mCurrentLocation != null) {
            mLastLocationLatitude = mCurrentLocation.getLatitude();
            mLastLocationLongitude = mCurrentLocation.getLongitude();
        }
        //getDelayedForecast();
    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "onLocationChanged()", Toast.LENGTH_SHORT).show();

        mCurrentLocation = location;

        mLastLocationLatitude = mCurrentLocation.getLatitude();
        mLastLocationLongitude = mCurrentLocation.getLongitude();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        //Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void getLocationNameFromCoordinates() {

        final String geocodingUrl = "https://maps.googleapis.com/maps/api/geocode/json"
                + "?latlng=" + mLastLocationLatitude + "," + mLastLocationLongitude
                + "&key=" + GOOGLE_GEOCODING_API_KEY;

        // Köln
        //mLastLocationLatitude = 50.937101;
        //mLastLocationLongitude = 6.958117;

        // Weiler bei Monzingen (test with spaces in city name)
        //mLastLocationLatitude = 49.8278
        //mLastLocationLongitude = 7.53834

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

        // english Address with words containing numbers
        // 53.898833  -2.000750

        /*final String geocodingUrl = "https://maps.googleapis.com/maps/api/geocode/json"
                + "?latlng=" + 49.8278 + "," + 7.53834
                + "&key=" + GOOGLE_GEOCODING_API_KEY;*/

        //Log.v(TAG, geocodingUrl);

        if (Helper.isNetworkAvailable(this)) {

            final OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder().url(geocodingUrl).build();
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //Log.e(TAG, "$$$" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {

                    if (response.isSuccessful()) {
                        try {
                            String jsonLocData = response.body().string();

                            JSONObject locationData = new JSONObject(jsonLocData);
                            JSONArray results = locationData.getJSONArray("results");
                            if (results.length() > 1) {
                                JSONObject locationObj = results.getJSONObject(0);
                                String tmpLocationName = locationObj.getString("formatted_address");
                                mLocationName = Helper.getFormattedLocationName(tmpLocationName);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLocationLabel.setText(mLocationName);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLocationLabel.setText(R.string.no_location_available);
                                    }
                                });
                            }
                        } catch (JSONException | NullPointerException e) {
                            //Log.e(TAG, "JSONException caught: ", e);
                        }
                    }
                }
            });
        }
    }

    //</editor-fold>

    //<editor-fold desc="OptionsMenu">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refreshMenuIcon:
                mMenuRefreshClicked = true;
                //mGetForecastLocked = false;
                getForecast();
                break;
            case R.id.hourlyIcon:
                if (mForecast != null) {
                    Intent intentHourly = new Intent(this, HourlyForecastActivity.class);
                    intentHourly.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
                    // ACRA debugging line:
                    //intentHourly.putExtra("blubb", mForecast.getHourlyForecast());
                    startActivity(intentHourly);
                } else {
                    mMenuRefreshClicked = true;
                    //mGetForecastLocked = false;
                    getForecast();
                }
                break;
            case R.id.settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                break;
            case R.id.about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                break;
            case R.id.rateThisApp:
                Uri uriUrl = Uri.parse(STORE_URL);
                Intent launchPlayStore = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchPlayStore);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //</editor-fold>

    //<editor-fold desc="UI">
    private void toggleRefresh(int status) {

        switch (status) {
            case STATUS_GETTING_WEATHER:
                scrollToTop();
                mSwipeRefreshLayout.setRefreshing(true);

                if (mFirstStart) {
                    mFirstStart = false;
                    mSplashImageView.setVisibility(View.VISIBLE);
                    mDarkskyImageView.setVisibility(View.INVISIBLE);
                    mDegreeTextView.setVisibility(View.INVISIBLE);
                    mIconImageView.setVisibility(View.INVISIBLE);
                    mhumidityRainLinearLayout.setVisibility(View.INVISIBLE);
                    mTemperatureLabel.setVisibility(View.INVISIBLE);
                    mLocationIconDateLinearLayout.setVisibility(View.INVISIBLE);
                    mSummaryLabel.setVisibility(View.INVISIBLE);
                    mDailyListRecyclerView.setVisibility(View.INVISIBLE);
                }

                break;

            case STATUS_SHOW_WEATHER:
                mNetworkIsUnavailable.setVisibility(View.INVISIBLE);
                mSplashImageView.setVisibility(View.INVISIBLE);

                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                mDegreeTextView.setVisibility(View.VISIBLE);
                mIconImageView.setVisibility(View.VISIBLE);
                mhumidityRainLinearLayout.setVisibility(View.VISIBLE);
                mTemperatureLabel.setVisibility(View.VISIBLE);
                mSummaryLabel.setVisibility(View.VISIBLE);
                mDailyListRecyclerView.setVisibility(View.VISIBLE);

                mLocationIconDateLinearLayout.setVisibility(View.VISIBLE);

                mDarkskyImageView.setVisibility(View.VISIBLE);

                break;

            case STATUS_NO_NETWORK:

                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                mDegreeTextView.setVisibility(View.INVISIBLE);
                mIconImageView.setVisibility(View.INVISIBLE);
                mhumidityRainLinearLayout.setVisibility(View.INVISIBLE);
                mTemperatureLabel.setVisibility(View.INVISIBLE);
                mLocationIconDateLinearLayout.setVisibility(View.INVISIBLE);
                mSummaryLabel.setVisibility(View.INVISIBLE);
                mDailyListRecyclerView.setVisibility(View.INVISIBLE);
                mDarkskyImageView.setVisibility(View.INVISIBLE);

                mNetworkIsUnavailable.setVisibility(View.VISIBLE);
                mNetworkIsUnavailable.setText(R.string.network_is_unavailable);

                break;
        }
    }

    private void scrollToTop() {
        mNestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                //mNestedScrollView.smoothScrollTo(0,0);
                mNestedScrollView.fullScroll(View.FOCUS_UP);
                //mAppBar.setExpanded(true, true);
            }
        });
    }

    public void alertUserAboutNoNetwork() {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        Snackbar mySnackbar = Snackbar.make(mNestedScrollView, R.string.network_is_unavailable, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void updateDisplay() {

        Current current = mForecast.getCurrent();

        // background for current temperature
        int temperature = current.getTemperature();
        String unit = current.getUnit();
        String iconString = current.getIconString();
        boolean transparent = true;
        //Log.v(TAG, iconString);

        boolean withPhoto = mSharedPreferences.getBoolean("photo_background_switch", true);
        int background = R.drawable.bg_gradient;
        if(mAppTheme.equals("1")) {
            background = R.drawable.bg_gradient_orange;
        }

        if (withPhoto) {

            switch (iconString) {
                case "rain":
                    transparent = false;
                    break;
                case "snow":
                    transparent = false;
                    break;
                case "sleet":
                    transparent = false;
                    break;
                case "wind":
                    transparent = false;
                    break;
                case "cloudy":
                    transparent = false;
                    break;
                case "partly-cloudy-day":
                    transparent = false;
                    break;
                default:
                    transparent = true;
            }

            background = Helper.getPhotoBackgroundIconId(iconString);
            mNestedScrollView.setBackgroundResource(R.drawable.bg_transparent);
            getWindow().setBackgroundDrawableResource(background);

        } else if (!withPhoto && mSharedPreferences.getString("app_theme", "2").equals("2")) {
            // no photo AND transparent theme
            getWindow().setBackgroundDrawableResource(background);
        } else {
            getWindow().setBackgroundDrawableResource(background);
            int bottomBgColor;
            if (mSharedPreferences.getBoolean("temperature_colored_background_switch", true)) {
                bottomBgColor = R.color.heaven_blue;
                if (mAppTheme.equals("1")) {
                    bottomBgColor = R.color.colorPrimaryOrange;
                }
            } else {
                bottomBgColor = R.color.colorPrimary;
            }
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            Color.parseColor(Helper.getTemperatureColor(temperature, unit)),
                            ContextCompat.getColor(this, bottomBgColor)}
            );
            gradientDrawable.setCornerRadius(0f);
            mNestedScrollView.setBackground(gradientDrawable);
        }

        toggleTextBackgroundDrawable(transparent);

        mTimeLabel.setText(getString(R.string.updated) + " " + current.getFormattedTime());
        mTemperatureLabel.setText(String.valueOf(temperature));
        mPrecipValue.setText(String.valueOf(current.getPrecipChance()) + " %");
        mHumidityValue.setText(String.valueOf(current.getHumidity()) + " %");
        mSummaryLabel.setText(String.valueOf(current.getSummary()));
        Drawable drawable = ContextCompat.getDrawable(this, Helper.getIconId(iconString));
        mIconImageView.setImageDrawable(drawable);

        if (!mDayList.isEmpty()) {
            mDayList.clear();
        }
        Day[] days = mForecast.getDailyForecast();
        Collections.addAll(mDayList, days);
        mAdapter.notifyDataSetChanged();

        mNestedScrollView.setFocusableInTouchMode(true);
        mNestedScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
    }

    private void toggleTextBackgroundDrawable(boolean transparent) {

        int backGroundDrawable = R.drawable.bg_text_transparent;
        if(!transparent) {
            // Background drawable is visible
            backGroundDrawable = R.drawable.bg_text_pointed_corners;
            mSummaryLabel.setBackgroundResource(R.drawable.bg_text_round_corners);
        } else {
            mSummaryLabel.setBackgroundResource(backGroundDrawable);
        }

        mLocationIconDateLinearLayout.setBackgroundResource(backGroundDrawable);
        mhumidityRainLinearLayout.setBackgroundResource(backGroundDrawable);
    }

    private void makeNormalToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        // no transparent Toolbar (doesen't work on Android 4 devices)
        setSupportActionBar(toolbar);
    }

    private void makeToolBarTransparent() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        // transparent Toolbar (doesen't work on Android 4 devices)
        toolbar.getBackground().setAlpha(0);
        setSupportActionBar(toolbar);

        // remove shadow under toolbar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            StateListAnimator stateListAnimator;
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar_main);
            stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(appBarLayout, "elevation", 0));
            appBarLayout.setStateListAnimator(stateListAnimator);
        }
    }

    @Override
    public void onBackPressed() {
        // enable fake HOME button pressed
        moveTaskToBack(true);
    }
    //</editor-fold>
}
























