package de.joesch_it.chillweather.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import java.util.List;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.App;
import de.joesch_it.chillweather.helper.AppCompatPreferenceActivity;

import static de.joesch_it.chillweather.helper.App.PREF_KEY_APP_THEME;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_AUTOREFRESH_FREQUENCY;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_AUTOREFRESH_SWITCH;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_COLORED_ICONS;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_FILE;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_WIDGET_TRANSPARENCY;
import static de.joesch_it.chillweather.helper.Helper.updateWidget;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String TAG = " ### " + SettingsActivity.class.getSimpleName() + " ###";

    //<editor-fold desc="OnPreferenceChangeListener">
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                //Log.v(TAG, "stringValue " + stringValue);
                //Log.v(TAG, "preference " + String.valueOf(preference));

                Context context = App.getContext();
                SharedPreferences sharedPref = context.getSharedPreferences(PREF_KEY_FILE, Context.MODE_PRIVATE);

                // App Theme
                if (String.valueOf(preference).contains(context.getString(R.string.pref_title_app_theme))
                        && !stringValue.equals(sharedPref.getString(PREF_KEY_APP_THEME, "0"))) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(PREF_KEY_APP_THEME, stringValue);
                    editor.apply();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            System.exit(0);
                        }
                    }, 200);
                }

                // Autorefresh frequency
                if (String.valueOf(preference).contains(context.getString(R.string.pref_title_autorefresh_frequency))
                        && !stringValue.equals(sharedPref.getString(PREF_KEY_AUTOREFRESH_FREQUENCY, "3"))) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(PREF_KEY_AUTOREFRESH_FREQUENCY, stringValue);
                    editor.apply();
                    updateWidget(context, true); // only AlarmManager refresh
                }

                // Widget transparency
                if (String.valueOf(preference).contains(context.getString(R.string.pref_title_widget_transparency))
                        && !stringValue.equals(sharedPref.getString(PREF_KEY_WIDGET_TRANSPARENCY, "80"))) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(PREF_KEY_WIDGET_TRANSPARENCY, stringValue);
                    editor.apply();
                    updateWidget(context, true); // design & AlarmManager refresh
                }

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
    //</editor-fold>

    //<editor-fold desc="OnPreferenceClickListener">
    private static Preference.OnPreferenceClickListener sPreferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {

            Context context = App.getContext();

            SharedPreferences sharedPref = context.getSharedPreferences(PREF_KEY_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            boolean actualColoredIconsSwitch = sharedPreferences.getBoolean("colored_icons_switch", true);
            boolean actualAutoRefreshSwitch = sharedPreferences.getBoolean("pref_autorefresh_weather_switch", true);

            if (sharedPref.getBoolean(PREF_KEY_COLORED_ICONS, true) != actualColoredIconsSwitch) {
                editor.putBoolean(PREF_KEY_COLORED_ICONS, actualColoredIconsSwitch);
                editor.apply();
                updateWidget(context, true); // design & AlarmManager refresh
            }

            if (sharedPref.getBoolean(PREF_KEY_AUTOREFRESH_SWITCH, true) != actualAutoRefreshSwitch) {
                editor.putBoolean(PREF_KEY_AUTOREFRESH_SWITCH, actualAutoRefreshSwitch);
                editor.apply();
                updateWidget(context, true); // only AlarmManager refresh
            }

            return false;
        }
    };
    //</editor-fold>

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resId, boolean first) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getString("app_theme", "0").equals("1")){
            //setTheme(R.style.SettingsThemeOrange);
            theme.applyStyle(R.style.SettingsThemeOrange, true);
        } else {
            theme.applyStyle(resId, true);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ColorsPreferenceFragment.class.getName().equals(fragmentName)
                || AutoRefreshPreferenceFragment.class.getName().equals(fragmentName)
                || WidgetPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ColorsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_colors);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("app_theme"));

            findPreference("colored_icons_switch").setOnPreferenceClickListener(sPreferenceClickListener);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AutoRefreshPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            findPreference("pref_autorefresh_weather_switch").setOnPreferenceClickListener(sPreferenceClickListener);

            bindPreferenceSummaryToValue(findPreference("autorefresh_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class WidgetPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_widgets);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("widget_transparency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
