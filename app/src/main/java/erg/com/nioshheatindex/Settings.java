package erg.com.nioshheatindex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
//import com.adobe.mobile.*;

import com.adobe.marketing.mobile.MobileCore;

import static android.view.KeyEvent.ACTION_DOWN;

public class Settings extends AppCompatActivity {

    Switch cacheSwitch;
    Switch celsiusSwitch;
    Button btnSave;
    //private static final String TALKBACK_SETTING_ACTIVITY_NAME = "com.android.talkback.TalkBackPreferencesActivity";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_settings);

        // Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        TelemetryProc.appLaunch(getString(R.string.txtSettings), getResources().getString(R.string.txtSettings), "nav");

        final Zoomlayout zoomlayout = findViewById(R.id.zoomLayout);
        zoomlayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                zoomlayout.init(Settings.this);
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    zoomlayout.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }
        });

        celsiusSwitch = findViewById(R.id.switchCelsius);
        cacheSwitch = findViewById(R.id.switchCache);
        btnSave = findViewById(R.id.btnSave);

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bcelsius = mSettings.getBoolean("celsius_switch", false);
        boolean bcache = mSettings.getBoolean("cache_switch", false);
        if(bcelsius)
        {
            celsiusSwitch.setChecked(true);
        }
        else
        {
            celsiusSwitch.setChecked(false);
        }
        if(bcache)
        {
            cacheSwitch.setChecked(true);
        }
        else
        {
            cacheSwitch.setChecked(false);
        }


        celsiusSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == ACTION_DOWN) {
                    if (!celsiusSwitch.isChecked()) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this);
                        alertDialogBuilder
                                .setTitle(R.string.alert_celsius_title_on)
                                .setMessage(R.string.alert_celsius_on)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        celsiusSwitch.performClick();
                                        turnCelsiusSwitchOn();
                                        addNotification(getResources().getString(R.string.alert_celsius_enabled));
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        return true;
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this);
                        alertDialogBuilder
                                .setTitle(R.string.alert_celsius_title_off)
                                .setMessage(R.string.alert_celsius_off)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        celsiusSwitch.performClick();
                                        turnCelsiusSwitchOff();
                                        addNotification(getResources().getString(R.string.alert_celsius_disabled));
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        return true;
                    }
                }
                return false;
            }
        });

        cacheSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == ACTION_DOWN) {
                    if (!cacheSwitch.isChecked()) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this);
                        alertDialogBuilder
                                .setTitle(R.string.alert_cache_title_on)
                                .setMessage(R.string.alert_cache_on)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        cacheSwitch.performClick();
                                        turnCacheSwitchOn();
                                        addNotification(getResources().getString(R.string.alert_cache_enabled));
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        return true;
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this);
                        alertDialogBuilder
                                .setTitle(R.string.alert_cache_title_off)
                                .setMessage(R.string.alert_cache_off)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        cacheSwitch.performClick();
                                        turnCacheSwitchOff();
                                        addNotification(getResources().getString(R.string.alert_cache_disabled));
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        return true;
                    }
                }
                return false;
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startMore();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MobileCore.setApplication(getApplication());
        MobileCore.lifecycleStart(null);

        addNotification(getResources().getString(R.string.about_settings));
    }

    @Override
    public void onPause() {
        super.onPause();
        MobileCore.lifecyclePause();
    }

    private void addNotification(String STR){
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        if(isAccessibilityEnabled) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), STR, duration);
            toast.show();
        }
    }

    private void turnCelsiusSwitchOn(){
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSettings.edit().putBoolean("celsius_switch", true).apply();
    }

    private void turnCelsiusSwitchOff(){
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSettings.edit().putBoolean("celsius_switch", false).apply();
    }

    private void turnCacheSwitchOn(){
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSettings.edit().putBoolean("cache_switch", true).apply();
    }

    private void turnCacheSwitchOff(){
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSettings.edit().putBoolean("cache_switch", false).apply();
    }

    private void startMore() {
        Intent launchMore = new Intent(this, MoreActivity.class);
        startActivity(launchMore);
    }
}
