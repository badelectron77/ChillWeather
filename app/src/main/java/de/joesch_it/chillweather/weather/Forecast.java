package de.joesch_it.chillweather.weather;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.joesch_it.chillweather.helper.App;
import de.joesch_it.chillweather.weather.data.Current;
import de.joesch_it.chillweather.weather.data.Day;
import de.joesch_it.chillweather.weather.data.Hour;

public class Forecast {

    private Current mCurrent;
    private Hour[] mHourlyForecast;
    private Day[] mDailyForecast;

    public Current getCurrent() {
        return mCurrent;
    }

    public void setCurrent(Current current) {
        mCurrent = current;
    }

    public Hour[] getHourlyForecast() {
        return mHourlyForecast;
    }

    public void setHourlyForecast(Hour[] hourlyForecast) {
        mHourlyForecast = hourlyForecast;
    }

    public Day[] getDailyForecast() {
        return mDailyForecast;
    }

    public void setDailyForecast(Day[] dailyForecast) {
        mDailyForecast = dailyForecast;
    }

    public static int getIconId(String iconString) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        String iconSetNumber = "01";

        if(!sharedPrefs.getBoolean("colored_icons_switch", true)) {
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

    private static int getResId(String pString){
        Context context = App.getContext();
        return context.getResources().getIdentifier(pString, "drawable", context.getPackageName());
    }
}
