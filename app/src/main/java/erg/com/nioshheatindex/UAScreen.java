package erg.com.nioshheatindex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.adobe.marketing.mobile.MobileCore;

//import com.adobe.mobile.Config;

public class UAScreen extends AppCompatActivity {

    Button btnAgree;
    SharedPreferences mPrefs;
    final String uaPref = "agreed";
    Boolean agreed;
    final String welcomeScreenShownPref = "welcomeScreenShown";
    Boolean welcomeScreenShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_u_a_screen);

        // Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        TelemetryProc.appLaunch("Terms", "User Agreement", "nav");

        addNotification(getResources().getString(R.string.ua_banner_desc));

        TextView textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);
        agreed = mPrefs.getBoolean(uaPref, false);

        btnAgree = findViewById(R.id.btnAgree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMainActivity();
            }
        });
    }

    // Disable back button
    @Override
    public void onBackPressed() {
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

    private void addNotification(String STR){
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        if(isAccessibilityEnabled) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), STR, duration);
            toast.show();
        }
    }

    private void startMainActivity() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(uaPref, true);
        editor.apply(); // Very important to save the preference
        if (!welcomeScreenShown) {
            //Start MainActivity (Tutorial)
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.apply(); // Very important to save the preference
            finish();
            startActivity(new Intent(UAScreen.this, MainActivity.class));
        } else {
            //MainActivity (Tutorial) already ran. Start HeatIndexActivity.
            startHeatIndex();
        }
    }

    private void startHeatIndex(){
        Intent launchHeatIndex = new Intent(this, HeatIndexActivity.class);
        startActivity(launchHeatIndex);
    }

}