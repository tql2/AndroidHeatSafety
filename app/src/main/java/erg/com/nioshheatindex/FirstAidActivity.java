package erg.com.nioshheatindex;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import com.adobe.mobile.*;

import com.adobe.marketing.mobile.MobileCore;

import java.util.Locale;

public class FirstAidActivity extends AppCompatActivity {

    private boolean spanish = false;
    WebView myWebView;
    Button btnto_en;
    Button btnto_es;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_first_aid);

        MainActivity.sPage = "firstaidactivity";

        // Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        TelemetryProc.appLaunch("Information", "First Aid", "nav");

        //get the Locale of the Operating System
        String deviceLocale = Locale.getDefault().getLanguage();

        myWebView = findViewById(R.id.webView1);
        //myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setSupportZoom(true);
        myWebView.setWebViewClient(new myWebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                Button faButton = findViewById(R.id.faButton);
                Button sButton = findViewById(R.id.sButton);
                ImageView iv = findViewById(R.id.imageViewFirstAidTopPic);
                TextView tv = findViewById(R.id.imageViewTextFirstAid);
                if(view.getUrl().toString().contains("signs")) {
                    iv.setImageResource(R.drawable.signsandsymptoms);
                    iv.setContentDescription(getResources().getString(R.string.symptoms_banner_desc));
                    tv.setText(R.string.symptoms_banner_text);
                    tv.setContentDescription(getResources().getString(R.string.symptoms_banner_desc));
                    addNotification(getResources().getString(R.string.symptoms_banner_desc));

                    // set NavBar button to focused state
                    Drawable top = ContextCompat.getDrawable(getApplicationContext(), R.drawable.symptomsbigred);
                    sButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.extreme));
                    sButton.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);

                    Drawable top2 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.firstaidbig);
                    faButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    faButton.setCompoundDrawablesWithIntrinsicBounds(null, top2 , null, null);
                }
                else
                {
                    iv.setImageResource(R.drawable.firstaid);
                    iv.setContentDescription(getResources().getString(R.string.firstaid_banner_desc));
                    tv.setText(R.string.firstaid_banner_text);
                    tv.setContentDescription(getResources().getString(R.string.firstaid_banner_desc));
                    addNotification(getResources().getString(R.string.firstaid_banner_desc));
                    // set NavBar button to focused state
                    Drawable top = ContextCompat.getDrawable(getApplicationContext(), R.drawable.firstaidbigred);
                    faButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.extreme));
                    faButton.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);

                    Drawable top2 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.symptomsbig);
                    sButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    sButton.setCompoundDrawablesWithIntrinsicBounds(null, top2, null, null);
                }
            }
        });

        btnto_en = findViewById(R.id.btnToEN);
        btnto_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnto_es.setVisibility(View.VISIBLE);
                btnto_en.setVisibility(View.INVISIBLE);
                displayContent("en");
            }
        });

        btnto_es = findViewById(R.id.btnToES);
        btnto_es.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnto_en.setVisibility(View.VISIBLE);
                btnto_es.setVisibility(View.INVISIBLE);
                displayContent("es");
            }
        });

        displayContent(deviceLocale);
        //myWebView.loadUrl("file:///android_res/raw/first_aid.html");

        Button btnheatindex = findViewById(R.id.hiButton);
        btnheatindex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHeatIndex();
            }
        });

        Button btntoday = findViewById(R.id.tdyButton);
        btntoday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startToday();
            }
        });

        Button btnsymptoms = findViewById(R.id.sButton);
        btnsymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSymptoms();
            }
        });

        Button btnmore = findViewById(R.id.mButton);
        btnmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMore();
            }
        });

        ImageButton btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {startHome();}
        });

        ImageButton btnReminders = findViewById(R.id.btnReminders);
        btnReminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReminder();
            }
        });
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

    public void displayContent(String lang){
        String strPath = "file:///android_asset/first_aid.html";
        if(lang.length() <= 0) {
            lang = Locale.getDefault().getLanguage();
        }
        if(lang == "es")
        {
            strPath = "file:///android_asset/first_aid_es.html";
        }
        myWebView.loadUrl(strPath);
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

    private class myWebViewClient extends WebViewClient {
    }

    private void startHeatIndex() {
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

    private void startMore() {
        Intent launchMore = new Intent(this, MoreActivity.class);
        startActivity(launchMore);
    }

    private void startHome() {
        Intent launchHome = new Intent(getApplicationContext(), MainActivity.class);
        launchHome.putExtra("from","FA");
        startActivity(launchHome);
    }

    private void startReminder() {
        Intent launchSettings = new Intent(this, InitializeReminders.class);
        startActivity(launchSettings);
    }
}
