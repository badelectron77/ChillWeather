package de.joesch_it.chillweather.weather.adapters;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.App;
import de.joesch_it.chillweather.helper.Helper;
import de.joesch_it.chillweather.ui.HourlyForecastActivity;
import de.joesch_it.chillweather.ui.MainActivity;
import de.joesch_it.chillweather.weather.data.Day;

import static de.joesch_it.chillweather.helper.App.HOURLY_FORECAST;
import static de.joesch_it.chillweather.helper.App.POSITION_OVERMORROW;
import static de.joesch_it.chillweather.helper.App.POSITION_TOMORROW;


public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private List<Day> mDays;

    public DayAdapter(List<Day> dayList) {
        mDays = dayList;
    }

    @Override
    public DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int resId = R.layout.daily_list_item;
        if(Helper.isTablet()) {
            resId = R.layout.daily_list_item_tablet;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);

        return new DayViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DayViewHolder holder, final int position) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        Day day = mDays.get(position);

        int temperature = day.getTemperatureMax();
        String unit = day.getUnit();

        holder.constraintLayout.setBackgroundColor(Color.parseColor(Helper.getTemperatureColor(temperature, unit)));
        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(String.valueOf(temperature));

        if (sharedPrefs.getBoolean("temperature_colored_background_switch", true)) {
            holder.temperatureLabel.setTextColor(Color.parseColor(Helper.getTemperatureColor(temperature, unit)));
        } else {
            holder.temperatureLabel.setTextColor(ContextCompat.getColor(App.getContext(), R.color.temperature_label));
        }

        holder.dayLabel.setText(Helper.getDayOfTheWeek(day.getTime(), day.getTimezone()));
        holder.dateLabel.setText(Helper.getWeekdayDate(day.getTime(), day.getTimezone()));
        holder.summaryLabel.setText(day.getSummary());

        if (position >= 0 && position <= 2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.constraintLayout.setBackground(
                        Helper.getPressedColorRippleDrawable(
                                Color.parseColor(Helper.getTemperatureColor(temperature, unit)),
                                Color.parseColor("#ffffffff")
                        )
                );
            }

            holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), HourlyForecastActivity.class);
                    intent.putExtra(HOURLY_FORECAST, MainActivity.mHourlyForecast);
                    if (position == 1) {
                        intent.putExtra(POSITION_TOMORROW, "true");
                    } else if (position == 2) {
                        intent.putExtra(POSITION_OVERMORROW, "true");
                    }
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDays.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout constraintLayout;
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView dayLabel;
        TextView summaryLabel;
        TextView dateLabel;

        DayViewHolder(View view) {
            super(view);
            constraintLayout = (ConstraintLayout) view.findViewById(R.id.dailyListConstraintLayout);
            iconImageView = (ImageView) view.findViewById(R.id.dailyListIconImageView);
            temperatureLabel = (TextView) view.findViewById(R.id.dailyListTemperatureLabel);
            dayLabel = (TextView) view.findViewById(R.id.dailyListWeekdayNameLabel);
            summaryLabel = (TextView) view.findViewById(R.id.dailyListSummaryTextView);
            dateLabel = (TextView) view.findViewById(R.id.dailyListDateLabel);
        }
    }
}