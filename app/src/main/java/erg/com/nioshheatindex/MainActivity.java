package erg.com.nioshheatindex;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;
import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private String strWords;
    public static String sPage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setContentView(activity_main);
        setContentView(R.layout.activity_main);

        // Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        TelemetryProc.appLaunch("Tutorial", "Help", "nav");
        addNotification(getResources().getString(R.string.txttutorialintro));
        addNotification(getResources().getString(R.string.txttutorialpage1));

        Button btnBoarding = findViewById(R.id.btnBoarding);
        btnBoarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                buttonAction();
            }
        });
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
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

    private void addNotification(String STR) {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        if(isAccessibilityEnabled) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), STR, duration);
            toast.show();
        }
    }

    private void buttonAction() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("from");
            switch (value != null ? value : null)
            {
                case "HI":
                    startHeatIndex();
                    break;
                case "TDY":
                    startToday();
                    break;
                case "SYM":
                    startSymptoms();
                    break;
                case "FA":
                    startFirstAid();
                    break;
                case "MI":
                    startMore();
                    break;
                default:
                    break;
            }
        }
        else
        {
            startHeatIndex();
        }
    }

    private void startHeatIndex(){
        Intent launchHeatIndex = new Intent(this, HeatIndexActivity.class);
        startActivity(launchHeatIndex);
    }

    private void startToday() {
        Intent launchToday = new Intent(this, TodayActivity.class);
        startActivity(launchToday);
    }

    private void startSymptoms() {
        Intent launchSymptoms = new Intent(this, SymptomsActivity.class);
        startActivity(launchSymptoms);
    }

    private void startFirstAid() {
        Intent launchFirstAid = new Intent(this, FirstAidActivity.class);
        startActivity(launchFirstAid);
    }

    private void startMore() {
        Intent launchMore = new Intent(this, MoreActivity.class);
        startActivity(launchMore);
    }
}
