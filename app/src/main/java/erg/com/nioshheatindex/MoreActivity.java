package erg.com.nioshheatindex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.adobe.marketing.mobile.MobileCore;

//import com.adobe.mobile.*;

public class MoreActivity extends AppCompatActivity {

    private final Context context = this;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_more);

        MainActivity.sPage = "moreactivity";

        final Zoomlayout zoomlayout = findViewById(R.id.zoomLayout);
        zoomlayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                zoomlayout.init(MoreActivity.this);
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    zoomlayout.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }
        });

        // Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        TelemetryProc.appLaunch("Information", getResources().getString(R.string.more_banner_text), "nav");

        this.setTitle(getResources().getString(R.string.more_banner_text));
        addNotification(getResources().getString(R.string.more_banner_desc));

        // set NavBar button to focused state
        Drawable top = ContextCompat.getDrawable(this, R.drawable.morebigred);
        Button hiButton = findViewById(R.id.mButton);
        hiButton.setTextColor(ContextCompat.getColor(this, R.color.extreme));
        hiButton.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);

        Button btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent AboutIntent = new Intent(context, MoreInfoWebViewActivity.class);
                AboutIntent.putExtra("tag","about");
                startActivity(AboutIntent);
            }
        });

        Button btnContact = findViewById(R.id.btnContact);
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent ContactIntent = new Intent(context, MoreInfoWebViewActivity.class);
                ContactIntent.putExtra("tag","contact");
                startActivity(ContactIntent);
            }
        });

        Button btnMoreDetails = findViewById(R.id.btnMoreDetails);
        btnMoreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent MoreDetailsIntent = new Intent(context, MoreInfoWebViewActivity.class);
                MoreDetailsIntent.putExtra("tag","moredetails");
                startActivity(MoreDetailsIntent);
            }
        });

        Button btnRiskFactors = findViewById(R.id.btnRiskFactors);
        btnRiskFactors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent RiskFactorsIntent = new Intent(context, MoreInfoWebViewActivity.class);
                RiskFactorsIntent.putExtra("tag","riskfactors");
                startActivity(RiskFactorsIntent);
            }
        });

        Button btnFAQ = findViewById(R.id.btnFAQ);
        btnFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent FAQIntent = new Intent(context, MoreInfoWebViewActivity.class);
                FAQIntent.putExtra("tag","faq");
                startActivity(FAQIntent);
            }
        });

        Button btnReminder = findViewById(R.id.btnReminder);
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent launchSettings = new Intent(context, InitializeReminders.class);
                startActivity(launchSettings);
            }
        });

        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent launchSettings = new Intent(context, Settings.class);
                startActivity(launchSettings);
            }
        });



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

        Button btnfirstaid = findViewById(R.id.faButton);
        btnfirstaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFirstAid();
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

    private void addNotification(String STR){
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        if(isAccessibilityEnabled) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), STR, duration);
            toast.show();
        }
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

    private void startFirstAid() {
        Intent launchFirstAid = new Intent(this, FirstAidActivity.class);
        startActivity(launchFirstAid);
    }

    private void startHome() {
        Intent launchHome = new Intent(getApplicationContext(), MainActivity.class);
        launchHome.putExtra("from","MI");
        startActivity(launchHome);
    }

    private void startReminder() {
        Intent launchReminder = new Intent(MoreActivity.this, InitializeReminders.class);
        startActivity(launchReminder);
    }
}
