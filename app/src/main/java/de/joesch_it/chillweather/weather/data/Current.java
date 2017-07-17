package de.joesch_it.chillweather.weather.data;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.joesch_it.chillweather.helper.Helper;

public class Current {

    private String mUnit;
    private long mTime;
    private String mIcon;
    private double mTemperature;
    private double mHumidity;
    private double mPrecipChance;
    private String mSummary;
    private String mTimezone;

    public String getFormattedTime() {

        // default
        String format = "EEEE, d MMM, H:mm a"; // Wed, 7 June 5:28 PM

        if (Helper.getCountryCode().equals(new Locale("de").getLanguage())) format = "EEEE, d. MMM H:mm"; // Mi, 7. Juni 17:28

        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public String getFormattedTimeForWidget() {

        // default
        String format = "dd-MM H:mm a"; // 02-06  5:28 PM

        if (Helper.getCountryCode().equals(new Locale("de").getLanguage())) format = "dd.MM. H:mm"; // 02.06. 17:28

        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getIconString() {
        return mIcon;
    }

    public int getTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public int getHumidity() {
        return (int) Math.round(mHumidity * 100);
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public int getPrecipChance() {
        return (int) Math.round(mPrecipChance * 100);
    }

    public void setPrecipChance(double precipChance) {
        mPrecipChance = precipChance;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        mUnit = unit;
    }
}
