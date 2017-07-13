package de.joesch_it.chillweather.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.joesch_it.chillweather.R;
import de.joesch_it.chillweather.helper.App;

import static de.joesch_it.chillweather.helper.App.BUILD;


public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.weatherServiceTextView)
    TextView mWeatherServiceTextView;
    @BindView(R.id.versionTextView)
    TextView mVersionTextView;
    @BindView(R.id.aboutIntroTextView)
    TextView mAboutIntroTextView;
    @BindView(R.id.librariesTextView)
    TextView mLibrariesTextView;
    @BindView(R.id.iconsTextView)
    TextView mIconsTextView;
    @BindView(R.id.dateTextView)
    TextView mDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            // fetch current version
            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            String currentVersionName = packageInfo.versionName;

            mVersionTextView.setText(getString(R.string.version) + " " + currentVersionName);
            mDateTextView.setText("Build " + BUILD);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mAboutIntroTextView.setText(stripUnderlines(getString(R.string.this_app_was_built_by)));
        mAboutIntroTextView.setMovementMethod(LinkMovementMethod.getInstance());

        mWeatherServiceTextView.setText(stripUnderlines(getString(R.string.weather_service_list)));
        mWeatherServiceTextView.setMovementMethod(LinkMovementMethod.getInstance());

        mLibrariesTextView.setText(stripUnderlines(getString(R.string.libraries_list)));
        mLibrariesTextView.setMovementMethod(LinkMovementMethod.getInstance());

        mIconsTextView.setText(stripUnderlines(getString(R.string.icons_list)));
        mIconsTextView.setMovementMethod(LinkMovementMethod.getInstance());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "Stefan Jösch <stefan.joesch@joesch-it.de>", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                startActivity(Intent.createChooser(intent, getString(R.string.open_with)));
            }
        });
    }

    private Spannable stripUnderlines(String str) {
        @SuppressWarnings("deprecation")
        Spannable s = (Spannable) Html.fromHtml(str);
        /*for (URLSpan u: s.getSpans(0, s.length(), URLSpan.class)) {
            s.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0);
        }*/
        return s;
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
