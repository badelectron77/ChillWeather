package de.joesch_it.chillweather.weather.data;


import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.App;
import de.joesch_it.chillweather.helper.Helper;

public class Hour implements Parcelable {

    public static final Creator<Hour> CREATOR = new Creator<Hour>() {
        @Override public Hour createFromParcel(Parcel in) {
            return new Hour(in);
        }

        @Override public Hour[] newArray(int size) {
            return new Hour[size];
        }
    };
    private String mUnit;
    private long mTime;
    private String mSummary;
    private double mTemperature;
    private String mIcon;
    private String mTimezone;

    private Hour(Parcel in) {

        mUnit = in.readString();
        mTime = in.readLong();
        mSummary = in.readString();
        mTemperature = in.readDouble();
        mIcon = in.readString();
        mTimezone = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mUnit);
        dest.writeLong(mTime);
        dest.writeString(mSummary);
        dest.writeDouble(mTemperature);
        dest.writeString(mIcon);
        dest.writeString(mTimezone);
    }

    public Hour() {
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getTimezone() {
        return mTimezone;
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

    public int getIconId() {
        return Helper.getIconId(mIcon);
    }

    public String getHour() {

        // default
        String format = "h"; // Hour in am/pm (1-12)

        if (Helper.getCountryCode().equals(new Locale("de").getLanguage())) format = "H"; // Hour in day (0-23)

        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public String getAmPm() {

        if (Helper.getCountryCode().equals(new Locale("de").getLanguage())) {
            return App.getContext().getResources().getString(R.string.o_clock); // "Uhr"
        }

        // get AM or PM
        SimpleDateFormat formatter = new SimpleDateFormat("a", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }
}
