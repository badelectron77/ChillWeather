package de.joesch_it.chillweather.helper;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.receiver.BigChillWidgetProvider;
import de.joesch_it.chillweather.receiver.ChillWidgetProvider;

import static de.joesch_it.chillweather.helper.App.BIG_CHILL_WIDGET_UPDATE;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_BIG_KEEP_VALUES;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_FILE;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_KEEP_VALUES;

public final class Helper {

    public static String getFormattedLocationName(String locationName) {

        String[] parts = locationName.split(",");
        locationName = parts.length > 2 ? parts[1] : parts[0];

        // remove starting numbers (http://regexr.com/)
        locationName = locationName.replaceAll("^\\d+", "");

        // remove words containing numbers
        locationName = locationName.replaceAll("\\w*\\d\\w*", "");

        return locationName.trim();
    }

    public static void updateSmallWidget(Context context) {

        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_KEY_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PREF_KEY_KEEP_VALUES, true);
        editor.apply();

        Intent intent = new Intent(context, ChillWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ChillWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    public static void updateBigWidget(Context context) {

        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_KEY_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PREF_KEY_BIG_KEEP_VALUES, true);
        editor.apply();

        Intent intent = new Intent(context, BigChillWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, BigChillWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public static String getTemperatureColor(int temperature, String unit) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        String bgColor = "#" + Integer.toHexString(ContextCompat.getColor(App.getContext(), R.color.heaven_blue));

        if (sharedPrefs.getBoolean("temperature_colored_background_switch", true)) {

            String transparency = "d6";

            if (unit.equals("us")) {

                if (temperature < -5) {
                    bgColor = "#" + transparency + "0066FF";
                } else if (temperature >= -5 && temperature < 10) {
                    bgColor = "#" + transparency + "0D63F2";
                } else if (temperature >= 10 && temperature < 16) {
                    bgColor = "#" + transparency + "1A61E6";
                } else if (temperature >= 16 && temperature < 21) {
                    bgColor = "#" + transparency + "265ED9";
                } else if (temperature >= 21 && temperature < 27) {
                    bgColor = "#" + transparency + "335CCC";

                } else if (temperature >= 27 && temperature < 32) {
                    bgColor = "#" + transparency + "4059BF";
                } else if (temperature >= 32 && temperature < 37) {
                    bgColor = "#" + transparency + "4C57B2";
                } else if (temperature >= 37 && temperature < 43) {
                    bgColor = "#" + transparency + "5954A6";
                } else if (temperature >= 43 && temperature < 48) {
                    bgColor = "#" + transparency + "665299";
                } else if (temperature >= 48 && temperature < 54) {
                    bgColor = "#" + transparency + "734F8C";

                } else if (temperature >= 54 && temperature < 59) {
                    bgColor = "#" + transparency + "804C80";
                } else if (temperature >= 59 && temperature < 64) {
                    bgColor = "#" + transparency + "8C4A73";
                } else if (temperature >= 64 && temperature < 70) {
                    bgColor = "#" + transparency + "994766";
                } else if (temperature >= 70 && temperature < 75) {
                    bgColor = "#" + transparency + "A64559";
                } else if (temperature >= 75 && temperature < 81) {
                    bgColor = "#" + transparency + "B2424D";

                } else if (temperature >= 81 && temperature < 86) {
                    bgColor = "#" + transparency + "BF4040";
                } else if (temperature >= 86 && temperature < 91) {
                    bgColor = "#" + transparency + "CC3D33";
                } else if (temperature >= 91 && temperature < 97) {
                    bgColor = "#" + transparency + "D93B26";
                } else if (temperature >= 97 && temperature < 102) {
                    bgColor = "#" + transparency + "E63819";
                } else if (temperature >= 102 && temperature < 108) {
                    bgColor = "#" + transparency + "F2360D";

                } else if (temperature >= 108) {
                    bgColor = "#" + transparency + "FF3300";
                }

            } else {

                if (temperature < -15) {
                    bgColor = "#" + transparency + "0066FF";
                } else if (temperature >= -15 && temperature < -12) {
                    bgColor = "#" + transparency + "0D63F2";
                } else if (temperature >= -12 && temperature < -9) {
                    bgColor = "#" + transparency + "1A61E6";
                } else if (temperature >= -9 && temperature < -6) {
                    bgColor = "#" + transparency + "265ED9";
                } else if (temperature >= -6 && temperature < -3) {
                    bgColor = "#" + transparency + "335CCC";

                } else if (temperature >= -3 && temperature < 0) {
                    bgColor = "#" + transparency + "4059BF";
                } else if (temperature >= 0 && temperature < 3) {
                    bgColor = "#" + transparency + "4C57B2";
                } else if (temperature >= 3 && temperature < 6) {
                    bgColor = "#" + transparency + "5954A6";
                } else if (temperature >= 6 && temperature < 9) {
                    bgColor = "#" + transparency + "665299";
                } else if (temperature >= 9 && temperature < 12) {
                    bgColor = "#" + transparency + "734F8C";

                } else if (temperature >= 12 && temperature < 15) {
                    bgColor = "#" + transparency + "804C80";
                } else if (temperature >= 15 && temperature < 18) {
                    bgColor = "#" + transparency + "8C4A73";
                } else if (temperature >= 18 && temperature < 21) {
                    bgColor = "#" + transparency + "994766";
                } else if (temperature >= 21 && temperature < 24) {
                    bgColor = "#" + transparency + "A64559";
                } else if (temperature >= 24 && temperature < 27) {
                    bgColor = "#" + transparency + "B2424D";

                } else if (temperature >= 27 && temperature < 30) {
                    bgColor = "#" + transparency + "BF4040";
                } else if (temperature >= 30 && temperature < 33) {
                    bgColor = "#" + transparency + "CC3D33";
                } else if (temperature >= 33 && temperature < 36) {
                    bgColor = "#" + transparency + "D93B26";
                } else if (temperature >= 36 && temperature < 39) {
                    bgColor = "#" + transparency + "E63819";
                } else if (temperature >= 39 && temperature < 42) {
                    bgColor = "#" + transparency + "F2360D";

                } else if (temperature >= 42) {
                    bgColor = "#" + transparency + "FF3300";
                }
            }
        }

        return bgColor;
    }

    public static Drawable getPressedColorRippleDrawable(int normalColor, int pressedColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(getPressedColorSelector(pressedColor), getColorDrawableFromColor(normalColor), null);
        }
        return null;
    }

    private static ColorStateList getPressedColorSelector(int pressedColor) {
        return new ColorStateList(
                new int[][]{new int[]{}},
                new int[]{pressedColor}
        );
    }

    private static ColorDrawable getColorDrawableFromColor(int color) {
        return new ColorDrawable(color);
    }

    public static String getDayOfTheWeek(long time, String timezone) {

        SimpleDateFormat formatterWeekday = new SimpleDateFormat("EEEE", Locale.getDefault());
        formatterWeekday.setTimeZone(TimeZone.getTimeZone(timezone));
        Date weekday = new Date(time * 1000);

        SimpleDateFormat formatterDatestring = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        formatterDatestring.setTimeZone(TimeZone.getTimeZone(timezone));
        String dateDatestring = formatterDatestring.format(new Date(time * 1000));
        String todayDatestring = formatterDatestring.format(new Date());

        if (dateDatestring.equals(todayDatestring)) {
            return App.getContext().getResources().getString(R.string.today);
        }

        return formatterWeekday.format(weekday);
    }

    public static String getWeekdayDate(long time, String timezone) {

        String format = "MMMM d";

        if (getCountryCode().equals("de")) {
            format = "d. MMMM";
        }

        SimpleDateFormat formatterWeekday = new SimpleDateFormat(format, Locale.getDefault());
        formatterWeekday.setTimeZone(TimeZone.getTimeZone(timezone));
        Date weekday = new Date(time * 1000);

        return formatterWeekday.format(weekday);
    }

    public static String getCountryCode() {

        String locale = Locale.getDefault().getLanguage(); // e.g. "de"

        if (locale.equals(new Locale("de").getLanguage())) {
            return "de";
        }
        return "en";
    }

    public static int getWidgetBackgroundDrawable(int transparencyPercent) {
        int resDrawable = R.drawable.bg_widget_000;
        switch (transparencyPercent) {
            case 0:
                resDrawable = R.drawable.bg_widget_000;
                break;
            case 10:
                resDrawable = R.drawable.bg_widget_010;
                break;
            case 20:
                resDrawable = R.drawable.bg_widget_020;
                break;
            case 30:
                resDrawable = R.drawable.bg_widget_030;
                break;
            case 40:
                resDrawable = R.drawable.bg_widget_040;
                break;
            case 50:
                resDrawable = R.drawable.bg_widget_050;
                break;
            case 60:
                resDrawable = R.drawable.bg_widget_060;
                break;
            case 70:
                resDrawable = R.drawable.bg_widget_070;
                break;
            case 80:
                resDrawable = R.drawable.bg_widget_080;
                break;
            case 90:
                resDrawable = R.drawable.bg_widget_090;
                break;
            case 100:
                resDrawable = R.drawable.bg_widget_100;
                break;
        }
        return resDrawable;
    }

    public static int getIconId(String iconString) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        String iconSetNumber = "01";

        if (!sharedPrefs.getBoolean("colored_icons_switch", true)) {
            iconSetNumber = "00";
        }

        int iconId = getResId("clear_day_" + iconSetNumber);

        switch (iconString) {
            case "clear-day":
                iconId = getResId("clear_day_" + iconSetNumber);
                break;
            case "clear-night":
                iconId = getResId("clear_night_" + iconSetNumber);
                break;
            case "rain":
                iconId = getResId("rain_" + iconSetNumber);
                break;
            case "snow":
                iconId = getResId("snow_" + iconSetNumber);
                break;
            case "sleet":
                iconId = getResId("sleet_" + iconSetNumber);
                break;
            case "wind":
                iconId = getResId("wind_" + iconSetNumber);
                break;
            case "fog":
                iconId = getResId("fog_" + iconSetNumber);
                break;
            case "cloudy":
                iconId = getResId("cloudy_" + iconSetNumber);
                break;
            case "partly-cloudy-day":
                iconId = getResId("partly_cloudy_" + iconSetNumber);
                break;
            case "partly-cloudy-night":
                iconId = getResId("cloudy_night_" + iconSetNumber);
                break;
        }
        return iconId;
    }

    public static int getPhotoBackgroundIconId(String iconString) {

        int iconId = getResId("clear_day_big");

        switch (iconString) {
            case "clear-day":
                iconId = getResId("clear_day_big");
                break;
            case "clear-night":
                iconId = getResId("clear_night_big");
                break;
            case "rain":
                iconId = getResId("rain_big");
                break;
            case "snow":
                iconId = getResId("snow_big");
                break;
            case "sleet":
                iconId = getResId("sleet_big");
                break;
            case "wind":
                iconId = getResId("wind_big");
                break;
            case "fog":
                iconId = getResId("fog_big");
                break;
            case "cloudy":
                iconId = getResId("cloudy_big");
                break;
            case "partly-cloudy-day":
                iconId = getResId("partly_cloudy_day_big");
                break;
            case "partly-cloudy-night":
                iconId = getResId("partly_cloudy_night_big");
                break;
        }
        return iconId;
    }

    private static int getResId(String pString) {
        Context context = App.getContext();
        return context.getResources().getIdentifier(pString, "drawable", context.getPackageName());
    }

    public static boolean isTablet() {
        Context context = App.getContext();
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return xlarge || large;
    }
}
