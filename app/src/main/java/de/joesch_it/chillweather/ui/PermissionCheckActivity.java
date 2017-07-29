package de.joesch_it.chillweather.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import de.joesch_it.chillweather.R;

import static de.joesch_it.chillweather.helper.App.PERMISSION_REQUEST_CODE_CALLBACK;
import static de.joesch_it.chillweather.helper.App.PERMISSION_REQUEST_CODE_SETTING;
import static de.joesch_it.chillweather.helper.App.PREF_KEY_FILE;
import static de.joesch_it.chillweather.helper.App.permissionsRequired;

public class PermissionCheckActivity extends AppCompatActivity {

    //public static final String TAG = " ### " + PermissionCheckActivity.class.getSimpleName() + " ###";
    private boolean mSentToSettings = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // immediately redirect to MainActivity if all permissions granted
        if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        // permissions not granted, so we proceed with the permissions check
        setContentView(R.layout.activity_permission_check);

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY_FILE, MODE_PRIVATE);

        if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) {

            // no permission granted yet (after clicking the button)

            //Log.v(TAG, "01");


            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[1])) {

                // show permission rationale (Gründe, Begründung)

                //Log.v(TAG, "02");

                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.need_multiple_permissions));
                builder.setMessage(getString(R.string.this_app_needs_location_permission));
                builder.setPositiveButton(getString(R.string.grant), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(PermissionCheckActivity.this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else if (sharedPreferences.getBoolean(permissionsRequired[0], false)) {

                // one permission still needed

                //Log.v(TAG, "03: permissionsRequired[0] = " + String.valueOf(sharedPreferences.getBoolean(permissionsRequired[0], false)));

                // Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.need_multiple_permissions));
                builder.setMessage(getString(R.string.this_app_needs_location_permission));
                builder.setPositiveButton(getString(R.string.grant), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mSentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, PERMISSION_REQUEST_CODE_SETTING);
                        //Toast.makeText(getBaseContext(), "Go to Permissions to Grant Camera and Location", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else {

                //Log.v(TAG, "04");

                //just request the permission
                ActivityCompat.requestPermissions(this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);

            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.apply();

        } else {

            //Log.v(TAG, "05");
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Log.v(TAG, "06 onRequestPermissionsResult() called");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE_CALLBACK) {

            //Log.v(TAG, "07");

            //check if all permissions are granted

            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {

                    //Log.v(TAG, "08");
                    allgranted = true;

                } else {

                    //Log.v(TAG, "09");
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {

                //Log.v(TAG, "10");
                proceedAfterPermission();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[1])) {

                //Log.v(TAG, "11");

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.need_multiple_permissions));
                builder.setMessage(getString(R.string.this_app_needs_location_permission));
                builder.setPositiveButton(getString(R.string.grant), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(PermissionCheckActivity.this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override  public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }
            /*else {

                //Log.v(TAG, "12");
                //Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }*/
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Log.v(TAG, "13");

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE_SETTING) {

            //Log.v(TAG, "14");

            if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {

                //Log.v(TAG, "15");
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    @Override protected void onPostResume() {

        //Log.v(TAG, "17");

        super.onPostResume();

        if (mSentToSettings) {

            //Log.v(TAG, "18");

            if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {

                //Log.v(TAG, "19");

                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    private void proceedAfterPermission() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
