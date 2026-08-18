package erg.com.nioshheatindex;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//import com.adobe.mobile.*;

import com.adobe.marketing.mobile.MobileCore;

import java.util.Locale;

public class MoreInfoWebViewActivity extends AppCompatActivity {

    private final String DEBUG_TAG = "MoreInfoWebViewActivity";
    private String value = "about";

    ImageView iv;
    WebView myWebView;
    TextView tv;
    Button btnto_en;
    Button btnto_es;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_more_info_web_view);

        //Config.setContext(this.getApplicationContext());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("tag");
        }

        String deviceLocale = Locale.getDefault().getLanguage();

        iv = findViewById(R.id.imageViewMoreTopPic);
        tv = findViewById(R.id.imageViewText);
        myWebView = findViewById(R.id.webView1);
        //myWebView.getSettings().setJavaScriptEnabled(true);
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
        String strPath = "file:///android_asset/about_this_app.html";
        if(lang.length() <= 0) {
            lang = Locale.getDefault().getLanguage();
        }
        try
        {
            switch (value) {
                case ("about"):
                    tv.setText(getString(R.string.txtabout));
                    iv.setContentDescription(getString(R.string.txtabout));
                    if("en".equals(lang)){
                        strPath = "file:///android_asset/about_this_app.html";
                    }else{
                        strPath = "file:///android_asset/about_this_app_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    //myWebView.loadUrl("file:///android_asset/about_this_app.html");
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtabout), getResources().getString(R.string.more_banner_text), "nav");
                    break;
                case ("contact"):
                    tv.setText(getString(R.string.txtcontactus));
                    iv.setContentDescription(getString(R.string.txtcontactus) + ". " + getString(R.string.txtContactNoEmergency));
                    if("en".equals(lang)){
                        strPath = "file:///android_asset/contact_osha.html";
                    }else{
                        strPath = "file:///android_asset/contact_osha_es.html";
                    }
                    myWebView.loadUrl(strPath);

                    //myWebView.loadUrl("file:///android_res/raw/contact_osha.html");
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtcontactus), getResources().getString(R.string.more_banner_text), "nav");
                    break;
                case ("moredetails"):

                    myWebView.setWebViewClient(new myWebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            ImageView iv = findViewById(R.id.imageViewMoreTopPic);
                            TextView tv = findViewById(R.id.imageViewText);
                            if(view.getUrl().toString().contains("first")) {
                                iv.setImageResource(R.drawable.firstaid);
                                iv.setContentDescription(getResources().getString(R.string.firstaid_banner_desc));
                                tv.setText(R.string.firstaid_banner_text);
                                tv.setContentDescription(getResources().getString(R.string.firstaid_banner_desc));
                            }
                            else if(view.getUrl().toString().contains("sign"))
                            {
                                iv.setImageResource(R.drawable.signsandsymptoms);
                                iv.setContentDescription(getResources().getString(R.string.symptoms_banner_desc));
                                tv.setText(R.string.symptoms_banner_text);
                                tv.setContentDescription(getResources().getString(R.string.symptoms_banner_desc));
                            }
                        }
                    });
                    tv.setText(getString(R.string.txtmoretips));
                    iv.setContentDescription(getString(R.string.txtmoretips));
                    if("en".equals(lang)){
                        strPath = "file:///android_asset/more_details.html";
                    }else{
                        strPath = "file:///android_asset/more_details_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    //myWebView.loadUrl("file:///android_res/raw/more_details.html");
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtmoretips), getResources().getString(R.string.more_banner_text), "nav");
                    break;
                case ("riskfactors"):
                    tv.setText(getString(R.string.txtriskfactors));
                    iv.setContentDescription(getString(R.string.txtriskfactors));
                    if("en".equals(lang)){
                        strPath = "file:///android_asset/risk_factors.html";
                    }else{
                        strPath = "file:///android_asset/risk_factors_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    //myWebView.loadUrl("file:///android_res/raw/risk_factors.html");
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtriskfactors), getResources().getString(R.string.more_banner_text), "nav");
                    break;
                case ("faq"):
                    tv.setText(getString(R.string.txtfaq));
                    iv.setContentDescription(getString(R.string.txtfaq));
                    if("en".equals(lang)){
                        strPath = "file:///android_asset/about_faq.html";
                    }else{
                        strPath = "file:///android_asset/about_faq_es.html";
                    }
                    myWebView.loadUrl(strPath);
                    //myWebView.loadUrl("file:///android_res/raw/about_faq.html");
                    // Send metrics data to Omniture
                    TelemetryProc.appLaunch(getString(R.string.txtfaq), getResources().getString(R.string.more_banner_text), "nav");
                    break;
                default:
                    break;
            }
        }
        catch(NumberFormatException nfe)
        {
            nfe.printStackTrace();
        }
    }

    private class myWebViewClient extends WebViewClient {
    }

}

