package erg.com.nioshheatindex;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
//import com.adobe.mobile.*;
import com.adobe.marketing.mobile.MobileCore;

import java.util.Locale;

public class WebViewActivity extends AppCompatActivity {

    private final String DEBUG_TAG = "WebViewActivity";
    private String riskValue = "0";
    WebView myWebView;
    TextView tv;
    Button btnto_en;
    Button btnto_es;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Config.setContext(this.getApplicationContext());

        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_web_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            riskValue = extras.getString("risklevel");
        }

        String deviceLocale = Locale.getDefault().getLanguage();

        tv = findViewById(R.id.imageViewText);

        myWebView = findViewById(R.id.webView1);
       // myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setSupportZoom(true);

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
        String strPath = "file:///android_asset/more_details.html";
        int riskNum = 0;
        if(lang.length() <= 0) {
            lang = Locale.getDefault().getLanguage();
        }
        try {
            riskNum = Integer.parseInt(riskValue);
            switch (riskNum){
                case(0):
                    break;
                case(1):
                    tv.setText(R.string.more_banner_text);
                    if(lang == "en"){
                        strPath = "file:///android_asset/more_details.html";
                    }else{
                        strPath = "file:///android_asset/more_details_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtminimal), getString(R.string.hi_precations_btn_text), "nav");
                    break;
                case(2):
                    tv.setText(R.string.hi_precations_btn_text);
                    //myWebView.loadUrl("file:///android_res/raw/precautions_lower.html");
                    if(lang == "en"){
                        strPath = "file:///android_asset/precautions_moderate.html";
                    }else{
                        strPath = "file:///android_asset/precautions_moderate_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtcaution), getString(R.string.hi_precations_btn_text), "nav");
                    break;
                case(3):
                    tv.setText(R.string.hi_precations_btn_text);
                    //myWebView.loadUrl("file:///android_res/raw/precautions_high.html");
                    if(lang == "en"){
                        strPath = "file:///android_asset/precautions_high.html";
                    }else{
                        strPath = "file:///android_asset/precautions_high_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtwarning), getString(R.string.hi_precations_btn_text), "nav");
                    break;
                case(4):
                    tv.setText(R.string.hi_precations_btn_text);
                    //myWebView.loadUrl("file:///android_res/raw/precautions_veryhigh.html");
                    if(lang == "en"){
                        strPath = "file:///android_asset/precautions_veryhigh.html";
                    }else{
                        strPath = "file:///android_asset/precautions_veryhigh_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtdanger), getString(R.string.hi_precations_btn_text), "nav");
                    break;
                default:
                    break;
            }
        } catch(NumberFormatException nfe){
            nfe.printStackTrace();
        }
    }

}
