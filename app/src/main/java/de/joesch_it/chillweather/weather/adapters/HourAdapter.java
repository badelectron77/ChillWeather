package de.joesch_it.chillweather.weather.adapters;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.App;
import de.joesch_it.chillweather.helper.Helper;
import de.joesch_it.chillweather.weather.data.Hour;

public class HourAdapter extends BaseAdapter {

    private Context mContext;
    private Hour[] mHours;

    public HourAdapter(Context context, Hour[] hours) {
        mContext = context;
        mHours = hours;

    }

    @Override public int getCount() {
        return mHours.length;
    }

    @Override public Object getItem(int position) {
        return mHours[position];
    }

    @Override public long getItemId(int position) {
        return 0;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        ViewHolder holder;

        if (convertView == null) {

            int resId = R.layout.hourly_list_item;
            if(Helper.isTablet()) {
                resId = R.layout.hourly_list_item_tablet;
            }

            convertView = LayoutInflater.from(mContext).inflate(resId, parent, false);
            holder = new ViewHolder();
            holder.layout = (ConstraintLayout) convertView.findViewById(R.id.hourlyListConstraintLayout);
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.hourlyListIconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.hourlyListTemperatureLabel);
            holder.weekdayLabel = (TextView) convertView.findViewById(R.id.hourlyListWeekdayNameLabel);
            holder.summaryLabel = (TextView) convertView.findViewById(R.id.hourlyListSummaryTextView);
            holder.hourLabel = (TextView) convertView.findViewById(R.id.hourlyListHourTextView);
            holder.amPmLabel = (TextView) convertView.findViewById(R.id.hourlyListAmPmTextView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Hour hour = mHours[position];
        int temperature = hour.getTemperature();
        String unit = hour.getUnit();

        holder.layout.setBackgroundColor(Color.parseColor(Helper.getTemperatureColor(temperature, unit)));

        if (sharedPrefs.getBoolean("temperature_colored_background_switch", true)) {
            holder.temperatureLabel.setTextColor(Color.parseColor(Helper.getTemperatureColor(temperature, unit)));
        }
        holder.iconImageView.setImageResource(hour.getIconId());
        holder.temperatureLabel.setText(String.valueOf(temperature));
        holder.hourLabel.setText(hour.getHour());
        holder.amPmLabel.setText(hour.getAmPm());
        holder.weekdayLabel.setText(Helper.getDayOfTheWeek(hour.getTime(), hour.getTimezone()));
        holder.summaryLabel.setText(hour.getSummary());

        return convertView;
    }

    private static class ViewHolder {
        ConstraintLayout layout;
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView hourLabel;
        TextView amPmLabel;
        TextView weekdayLabel;
        TextView summaryLabel;
    }
}
