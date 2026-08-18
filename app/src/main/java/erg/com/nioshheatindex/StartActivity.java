package erg.com.nioshheatindex;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;
import com.adobe.marketing.mobile.Analytics;

import static erg.com.nioshheatindex.R.layout.activity_start;

public class StartActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private View mLayout;
    SharedPreferences mPrefs;
    final String welcomeScreenShownPref = "welcomeScreenShown";
    final String celsiusSwitchPref = "celsius_switch";
    final String cacheSwitchPref = "cache_switch";

    final String uaPref = "agreed";
    Boolean agreed;

    Boolean welcomeScreenShown;
    LinearLayout loPerms;
    LinearLayout loNotice;

    private boolean meetReqs = true;
    private boolean meetRAM = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(activity_start);

        // Send metrics data to Omniture
        MobileCore.setApplication(this.getApplication());
        //MobileCore.setLogLevel(LoggingMode.DEBUG);
        try{
            Analytics.registerExtension();
            UserProfile.registerExtension();
            Identity.registerExtension();
            Signal.registerExtension();
            Lifecycle.registerExtension();
            MobileCore.start(new AdobeCallback() {
                                 @Override
                                 public void call(Object o) {
                                     //MobileCore.configureWithAppID("b36c1852e229/497b2a9ad311/launch-944a7ee871f0-development");
                                     MobileCore.configureWithAppID("b36c1852e229/497b2a9ad311/launch-6729c519d4bd");
                                 }
                             }
            );

        }catch (InvalidInitException e){
            System.out.println("WCS Error: " + e.getMessage());
        }

        TelemetryProc.appLaunch("Heat Index", "Launch Application", "app");

        mLayout = findViewById(R.id.activity_start);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);
        agreed = mPrefs.getBoolean(uaPref, false);

        loPerms = findViewById(R.id.loShowNeedPerms);
        loNotice = findViewById(R.id.loShowNeedPerms);

        Button btnLow = findViewById(R.id.btnLow);
        btnLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                getStarted();
            }
        });

        ActivityManager actManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);

        long totalMemory = memInfo.totalMem;
        if (totalMemory < 1000000000){
            meetRAM = false;
        }

        boolean meetOS = true;
        if(!meetOS || !meetRAM){
            meetReqs = false;
        }

        if(!meetReqs) {
            //view.setVisibility(View.VISIBLE);
            loNotice.setVisibility(View.VISIBLE);

            ImageView ivOS = findViewById(R.id.ivOS);
            ImageView ivMem = findViewById(R.id.ivMem);
            if (!meetOS) {
                ivOS.setImageResource(R.drawable.bad);
            }
            if (!meetRAM) {
                ivMem.setImageResource(R.drawable.bad);
            }
        }
        else
        {
            getStarted();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobileCore.setApplication(getApplication());
        MobileCore.lifecycleStart(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobileCore.lifecyclePause();
    }

    private void getStarted()
    {
        if(Build.VERSION.SDK_INT >= 23) {
            //only api 23 and up
            permissionsCheck();
        }else{
            //only api 22 and down
            startMainActivity();
        }
    }

    public void permissionsCheck()
    {
        try
        {
            // Verify that all required location permissions have been granted.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // Location permissions have not been granted.
                requestLocationPermissions();
            }
            else
            {
                // Location permissions have been granted..
                startMainActivity();
            }
        }
        catch (SecurityException e)
        {
            //Nothing to do here
        }
    }

    private void requestLocationPermissions()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)|| ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION))
        {
            loPerms.setVisibility(View.VISIBLE);
            Snackbar.make(mLayout, R.string.txtLocationPermissionNotice, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(StartActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                }
            }).show();
        }
        else
        {
            // Location permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_LOCATION)
        {
            // We have requested multiple permissions for Location, so all of them need to be checked.
            if (PermissionUtil.verifyPermissions(grantResults))
            {
                // All required permissions have been granted, display contacts fragment.
                Snackbar.make(mLayout, R.string.txtLocationPermissionGranted, Snackbar.LENGTH_SHORT).show();
                startMainActivity();
            }
            else
            {
                //Snackbar.make(mLayout, R.string.txtLocationPermissionDenied, Snackbar.LENGTH_SHORT).show();
                loPerms.setVisibility(View.VISIBLE);
                Snackbar.make(mLayout, R.string.txtLocationPermissionNotice, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(StartActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                    }
                }).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startMainActivity() {
        if(!agreed)
        {
            finish();
            startActivity(new Intent(StartActivity.this, UAScreen.class));
        }else if (!welcomeScreenShown) {
            //Start MainActivity (Tutorial)
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.putBoolean(celsiusSwitchPref, false);
            editor.putBoolean(cacheSwitchPref, false);
            editor.apply(); // Very important to save the preference
            finish();
            startActivity(new Intent(StartActivity.this, MainActivity.class));
        }
        else
        {
            //MainActivity (Tutorial) already ran. Start HeatIndexActivity.
            startHeatIndex();
        }
    }

    private void startHeatIndex(){
        Intent launchHeatIndex = new Intent(this, HeatIndexActivity.class);
        startActivity(launchHeatIndex);
    }
}
