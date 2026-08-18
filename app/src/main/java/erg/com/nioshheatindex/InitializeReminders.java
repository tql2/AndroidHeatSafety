package erg.com.nioshheatindex;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class InitializeReminders extends AppCompatActivity {

    private static final int REQUEST_CALENDAR = 0;
    //private static String[] PERMISSIONS_LOCATION = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
    private static String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
    SharedPreferences prefs;
    private View mLayout;
    TextView txtNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initalize_reminders);


        txtNotice = findViewById(R.id.tvNotice);
        prefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        mLayout = findViewById(R.id.activity_initialize);
        boolean b = prefs.getBoolean("alreadyInitialized", false);
        if(!b) {
            setPreferences();
        }

        getStarted();
    }

    private void getStarted(){
        if(Build.VERSION.SDK_INT >= 23) {
            //only api 23 and up
            permissionsCheck();
        }else{
            //only api 22 and down
            startReminderActivity();
        }
    }

    public void permissionsCheck(){
        try
        {
            // Verify that all required location permissions have been granted.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            {
                // Calendar permissions have not been granted.
                requestCalendarPermissions();
            }
            else
            {
                // Location permissions have been granted..
                startReminderActivity();
            }
        }
        catch (SecurityException e)
        {
            //Nothing to do here
        }
    }

    private void requestCalendarPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CALENDAR)|| ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_CALENDAR))
        {
            txtNotice.setVisibility(View.VISIBLE);
            Snackbar.make(mLayout, R.string.txtCalendarPermissionNotice, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    //ActivityCompat.requestPermissions(InitializeReminders.this, PERMISSIONS_LOCATION, REQUEST_CALENDAR);
                    ActivityCompat.requestPermissions(InitializeReminders.this, PERMISSIONS_CALENDAR, REQUEST_CALENDAR);
                }
            }).show();
        }
        else
        {
            // Location permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_CALENDAR, REQUEST_CALENDAR);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == REQUEST_CALENDAR)
        {
            // We have requested multiple permissions for Location, so all of them need to be checked.
            if (PermissionUtil.verifyPermissions(grantResults))
            {
                // All required permissions have been granted, display contacts fragment.
                Snackbar.make(mLayout, R.string.txtCalendarPermissionGranted, Snackbar.LENGTH_SHORT).show();
                startReminderActivity();
            }
            else
            {
                //Snackbar.make(mLayout, R.string.txtCalendarPermissionDenied, Snackbar.LENGTH_SHORT).show();
                txtNotice.setVisibility(View.VISIBLE);
                Snackbar.make(mLayout, R.string.txtCalendarPermissionNotice, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(InitializeReminders.this, PERMISSIONS_CALENDAR, REQUEST_CALENDAR);
                    }
                }).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setPreferences(){
        //Get Preferences
        prefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("startHour", 12);
        editor.putInt("endHour", 12);
        editor.putInt("interval", 0);
        editor.putBoolean("sun", false); // change back
        editor.putBoolean("mon", false);
        editor.putBoolean("tue", false);
        editor.putBoolean("wed", false);
        editor.putBoolean("thu", false);
        editor.putBoolean("fri", false);
        editor.putBoolean("sat", false);
        editor.putBoolean("alreadyInitialized", true);
        editor.putString("summary", getResources().getString(R.string.txtSummary));
        editor.apply(); // Very important to save the preference
        //startReminderActivity();
    }

    private void startReminderActivity() {
        Intent launchSettings = new Intent(this, RemindersActivity.class);
        startActivity(launchSettings);
    }

}
