package de.joesch_it.chillweather.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.Arrays;
import java.util.Calendar;

import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.weather.adapters.HourAdapter;
import de.joesch_it.chillweather.weather.data.Hour;

import static de.joesch_it.chillweather.helper.App.HOURLY_FORECAST;
import static de.joesch_it.chillweather.helper.App.POSITION_OVERMORROW;
import static de.joesch_it.chillweather.helper.App.POSITION_TOMORROW;

public class HourlyForecastActivity extends AppCompatActivity {

    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPreferences.getString("app_theme", "2");
        if (theme.equals("1")) {
            setTheme(R.style.HourlyThemeOrange);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (theme.equals("2")) {
                int color = ContextCompat.getColor(this, R.color.colorPrimary);
                actionBar.setBackgroundDrawable(new ColorDrawable(color));
            }
            actionBar.setTitle(R.string.hourly);
        }
        setContentView(R.layout.activity_hourly_forecast);
        mListView = (ListView) findViewById(R.id.hourlyListView);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(HOURLY_FORECAST);
        Hour[] hours = Arrays.copyOf(parcelables, parcelables.length, Hour[].class);

        HourAdapter adapter = new HourAdapter(this, hours);
        mListView.setAdapter(adapter);

        // scroll to midnight of the selected day
        if (intent.getStringExtra(POSITION_TOMORROW) != null) {

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long timeTillMidnightInMillis = (c.getTimeInMillis() - System.currentTimeMillis());
            int timeTillMidnightInHours = (int) Math.floor(timeTillMidnightInMillis / 1000 / 3600) + 1;
            mListView.setSelection(timeTillMidnightInHours);
        }

        // scroll to midnight of the selected day
        if (intent.getStringExtra(POSITION_OVERMORROW) != null) {

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 2);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long timeTillMidnightInMillis = (c.getTimeInMillis() - System.currentTimeMillis());
            int timeTillMidnightInHours = (int) Math.floor(timeTillMidnightInMillis / 1000 / 3600) + 1;
            mListView.setSelection(timeTillMidnightInHours);
        }
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
}
