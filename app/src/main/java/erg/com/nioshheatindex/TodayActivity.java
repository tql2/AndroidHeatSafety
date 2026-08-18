package erg.com.nioshheatindex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;

import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//import com.adobe.mobile.*;
import com.adobe.marketing.mobile.MobileCore;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class TodayActivity extends FragmentActivity {

    public static int PAGES = 14;
    public final static int LOOPS = 1;
    public final static int FIRST_PAGE = -1;

    private CarouselPagerAdapter adapter;
    private ViewPager pager;
    private String siteLocation = null;
    private Button btnAP;
    private TextView txtMainText;
    private static String strWords;
    private static final List<String> todayWordsArray = new ArrayList<String>(); //Cached list of sentences for Today hourly carousel

    public static Boolean onLine = true;
    public static Boolean useCache = true;
    public static Boolean useCelsius = false;

    private TextView tvLocationName;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        TelemetryProc.appLaunch("Hourly Carousel", "Hourly Index", "nav");

        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_today);

        MainActivity.sPage = "todayactivity";

        tvLocationName = findViewById(R.id.atLocation);
        siteLocation = HeatIndexActivity.siteLocation;

        final Zoomlayout zoomlayout = findViewById(R.id.zoomLayout);
        zoomlayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                zoomlayout.init(TodayActivity.this);
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    zoomlayout.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }
        });

        btnAP = findViewById(R.id.btnAP);
        txtMainText = findViewById(R.id.tvMainText);
        //get the Locale of the Operating System
        getLocale();
        addNotification(getResources().getString(R.string.today_banner_desc));

        if(todayWordsArray.size() > 0)
        {
            String str = getResources().getString(R.string.today_banner_text) + " " + todayWordsArray.get(0).toString();
            this.setTitle(str);
        }

        // check for Cached Data
        if(!onLine && !useCache) {
            tvLocationName.setText(getResources().getString(R.string.txtNoLocation));
        }
        else
        {
            String str1 = "";
            String str2 = "";
            if (siteLocation != null) {
                str1 = getResources().getString(R.string.location);
                str2 = siteLocation;
                tvLocationName.setText(str1 + " " + str2);
            } else {
                tvLocationName.setText(getResources().getString(R.string.txtNoLocation));
            }
        }



        pager = findViewById(R.id.myviewpager);
        adapter = new CarouselPagerAdapter(this, this.getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setPageTransformer(false, adapter);
        pager.setCurrentItem(FIRST_PAGE);
        pager.setOffscreenPageLimit(5);
        // Get screen resolution to set Carousel margin size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int useSize = 0;
        if(screenWidth <= 720){useSize = -280;}
        if(screenWidth > 720 && screenWidth <= 1080){useSize = -420;}
        if(screenWidth > 1080){useSize = -560;}
        pager.setPageMargin(useSize);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }
            @Override
            public void onPageSelected(int index)
            {
                if(todayWordsArray.size() > 0)
                {
                    strWords = todayWordsArray.get(index);
                    addNotification(strWords);
                }
                renderButton(pager.getCurrentItem());
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //a1 = HeatIndexActivity.todayStringArray;

        renderButton(0);


        // set NavBar button to focused state
        Drawable top = ContextCompat.getDrawable(this, R.drawable.clockbigred);
        Button hiButton = findViewById(R.id.tdyButton);
        hiButton.setTextColor(ContextCompat.getColor(this, R.color.extreme));
        hiButton.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);

        Button btnheatindex = findViewById(R.id.hiButton);
        btnheatindex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHeatIndex();
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
    public void onResume(){
        super.onResume();
        MobileCore.setApplication(getApplication());
        MobileCore.lifecycleStart(null);

        if(isInternetConnection()) {
            if (isLocationEnabled()){
                onLine = true;
                useCache = isCacheEnabled();
                useCelsius = isCelsiusEnabled();
                setWordArray();
                strWords = todayWordsArray.get(pager.getCurrentItem());
                addNotification(strWords);
            }else{
                onLine = false;
            }
        }else {
            onLine = false;
        }

        if(!onLine){
            disablePrecautionsBtn();
            alertNoNetwork();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobileCore.lifecyclePause();
    }

    public boolean isCelsiusEnabled(){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sSetting = mPrefs.getBoolean("celsius_switch", false);
        boolean b = true;
        if(!sSetting)
        {
            b = false;
        }
        return b;
    }

    public boolean isCacheEnabled(){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sSetting = mPrefs.getBoolean("cache_switch", false);
        boolean b = true;
        if(!sSetting)
        {
            b = false;
        }
        return b;
    }

    private boolean isInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void disablePrecautionsBtn() {
        TextView atLocation = (findViewById(R.id.atLocation));
        atLocation.setText(getResources().getString(R.string.txtNoLocation));
        TextView mainText = findViewById(R.id.tvMainText);
        Button btnAllPurpose = findViewById(R.id.btnAP);
        mainText.setText(R.string.retrymessage);
        btnAllPurpose.setBackgroundColor(ContextCompat.getColor(this, R.color.retry));
        btnAllPurpose.setText(R.string.retry);
        btnAllPurpose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
                startHeatIndex();
            }
        });
    }

    private void alertNoNetwork() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getResources().getString(R.string.txtNoNetwork_Title));
        dialog.setMessage(getResources().getString(R.string.txtNoNetwork));
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick (DialogInterface dialog,int which)
            {
                dialog.cancel();
                alertUsingCache();
            }
        });
        dialog.show();
    }

    private void alertUsingCache(){
        if(isCacheEnabled()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(R.string.txtusingcache);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    addNotification(getResources().getString(R.string.txtusingcache));
                }
            });
            alert.show();
            tvLocationName.setText(siteLocation);
            renderButton(0);
        }
        else
        {

        }
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

    private void setWordArray(){

        siteLocation = HeatIndexActivity.siteLocation;

        String threatLevel = "minimal";
        //array list to hold HeatIndexActivity.todayStringArray
        List<String> array = HeatIndexActivity.todayStringArray;
        todayWordsArray.clear();

        if(!onLine && !useCache)
        {
            todayWordsArray.add(getResources().getString(R.string.txtnoarraydata));
        }
        else
        {
            if (array.size() > 0) {
                String hour = "12 PM"; //hour
                String hi = "92"; //heat index
                for (int c = 0; c < array.size(); c++) {
                    String string = array.get(c);
                    String[] parts = string.split(",");
                    String time = parts[0]; //hour
                    hi = parts[1]; //heat index
                    hi = hi.replace(".0", "");
                    Date parsed = null;
                    try {
                        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        parsed = sourceFormat.parse(time);
                        SimpleDateFormat destFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        hour = destFormat.format(parsed);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //int h = Integer.valueOf(hi);
                    int h = parseInt(hi);
                    if (h < 60) {
                        threatLevel = getResources().getString(R.string.txtminimal);// "minimal";
                    } else if (h < 80) {
                        threatLevel = getResources().getString(R.string.txtcaution);//"caution";
                    } else if (h < 95) {
                        threatLevel = getResources().getString(R.string.txtwarning);//"warning";
                    } else {
                        threatLevel = getResources().getString(R.string.txtdanger);//"dangerous";
                    }

                    String sentence = null;
                    boolean cels = useCelsius;
                    if (c == 0) {
                        if (!cels) {
                            sentence = getResources().getString(R.string.txtFor) + " " + siteLocation + " " + getResources().getString(R.string.txttodaynowsentencepart1) + " " + hour + getResources().getString(R.string.txttodaynowsentencepart2) + " " + hi + " " + getResources().getString(R.string.txttodaynowsentencepart3) + " " + threatLevel;
                        } else {
                            hi = getCelsius(hi);
                            sentence = getResources().getString(R.string.txtFor) + " " + siteLocation + " " + getResources().getString(R.string.txttodaynowsentencepart1) + " " + hour + getResources().getString(R.string.txttodaynowsentencepart2) + " " + hi + " " + getResources().getString(R.string.txttodaynowsentencepart3celsius) + " " + threatLevel;
                        }
                    } else {
                        if (!cels) {
                            sentence = getResources().getString(R.string.txtFor) + " " + siteLocation + " " + getResources().getString(R.string.txttodaylatersentencepart1) + " " + hour + getResources().getString(R.string.txttodaylatersentencepart2) + " " + hi + " " + getResources().getString(R.string.txttodaylatersentencepart3) + " " + threatLevel;

                        } else {
                            hi = getCelsius(hi);
                            sentence = getResources().getString(R.string.txtFor) + " " + siteLocation + " " + getResources().getString(R.string.txttodaylatersentencepart1) + " " + hour + getResources().getString(R.string.txttodaylatersentencepart2) + " " + hi + " " + getResources().getString(R.string.txttodaylatersentencepart3celsius) + " " + threatLevel;
                        }
                    }
                    todayWordsArray.add(sentence);
                }
            }
            else
            {
                todayWordsArray.clear();
                todayWordsArray.add(getResources().getString(R.string.txtnoarraydata));
            }
        }
    }

    private String getLocale() {
        boolean spanish = !Locale.getDefault().getLanguage().equalsIgnoreCase("en");
        return null;
    }

    public String getCelsius(String s){
        double dblFahrenheit = 0;
        double dblConvertedTemp = 0;
        DecimalFormat dfTenth = new DecimalFormat("#.#");
        String str = null;
        if (!s.isEmpty()){
            dblFahrenheit = Double.parseDouble(s);
            if (dblFahrenheit <= 212)
            {
                dblConvertedTemp = (5.0/9.0) * (dblFahrenheit - 32);

                str = dfTenth.format(dblConvertedTemp);
            }
        }
        return str;
    }

    private void renderButton(int r){
        String hi = "92"; //heat index
        int rl = 0;
        if(HeatIndexActivity.isManual || !isInternetConnection() && !isCacheEnabled())
        {
            PAGES = 1;
            adapter.notifyDataSetChanged();
            txtMainText.setText(R.string.retrymessage);
            btnAP.setBackgroundColor(ContextCompat.getColor(this, R.color.retry));
            btnAP.setContentDescription(getResources().getString(R.string.retrybtn_desc));
            btnAP.setTextColor(Color.BLACK);
            btnAP.setText(R.string.retry);
            btnAP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startHeatIndex();
                    finish();
                }
            });
            tvLocationName.setText(getResources().getString(R.string.txtNoLocation));
        }
        else
        {
            PAGES = 14;
            adapter.notifyDataSetChanged();
            //array list to hold HeatIndexActivity.todayStringArray
            List<String> a1 = HeatIndexActivity.todayStringArray;
            if(a1.size() > 0)
            {
                tvLocationName.setText(getResources().getString(R.string.location) + " " + siteLocation);
                String string = a1.get(r);
                String[] parts = string.split(",");
                hi = parts[1]; //heat index
                hi = hi.replace(".0", "");
                int h = Integer.valueOf(hi);
                ////int h = 137;
                if(rl == 0 )
                {
                    if (h < 60)
                    {
                        rl = 1; // minimal risk
                    }
                    else if (h < 80)
                    {
                        rl = 2; // low risk
                    }
                    else if (h < 95)
                    {
                        rl = 3; // high risk
                    }
                    else {
                        rl = 4; // extreme risk
                    }
                }
                switch (rl)
                {
                    case 0:
                        btnAP.setBackgroundColor(ContextCompat.getColor(this, R.color.retry));
                        btnAP.setText(R.string.txtNoData);
                        btnAP.setContentDescription(getResources().getString(R.string.retrybtn_desc));
                        btnAP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                                i.putExtra("risklevel","0");
                                startActivity(i);
                            }
                        });
                        break;
                    case 1:
                        txtMainText.setText(R.string.txtMinimalRisk);
                        btnAP.setBackgroundColor(ContextCompat.getColor(this, R.color.minimal));
                        btnAP.setTextColor(ContextCompat.getColor(this, R.color.white));
                        btnAP.setText(R.string.hi_allpurpose_btn_minrisk);
                        btnAP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                                i.putExtra("risklevel","1");
                                startActivity(i);
                            }
                        });
                        break;
                    case 2:
                        txtMainText.setText(R.string.txtLowRisk);
                        btnAP.setBackgroundColor(ContextCompat.getColor(this, R.color.low));
                        btnAP.setText(R.string.hi_precations_btn_text);
                        btnAP.setTextColor(ContextCompat.getColor(this, R.color.black));
                        btnAP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                                i.putExtra("risklevel","2");
                                startActivity(i);
                            }
                        });
                        break;
                    case 3:
                        txtMainText.setText(R.string.txtHighRisk);
                        btnAP.setBackgroundColor(ContextCompat.getColor(this,R.color.high));
                        btnAP.setText(R.string.hi_precations_btn_text);
                        btnAP.setTextColor(ContextCompat.getColor(this, R.color.black));
                        btnAP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                                i.putExtra("risklevel","3");
                                startActivity(i);
                            }
                        });
                        break;
                    case 4:
                        ////////////////////////
                        int converted = Integer.valueOf(hi);
                        if(converted > 137){
                        //if (converted > 80){
                            String noticeText = getResources().getString(R.string.txtExtremeRiskAlert);
                            SpannableString spannableStr = new SpannableString(noticeText);
                            int spanStart = "en".equals(getLocale()) ? 66 : 86;
                            int spanEnd = "en".equals(getLocale()) ? 82 : 108;
                            if (noticeText.length() >= spanEnd) {
                                spannableStr.setSpan(new StyleSpan(Typeface.ITALIC), spanStart, spanEnd,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            txtMainText.setText(spannableStr);
                        }else{
                            txtMainText.setText(R.string.txtExtremeRisk);
                        }
                        //////////////////////////

                        btnAP.setBackgroundColor(ContextCompat.getColor(this,R.color.extreme));
                        btnAP.setText(R.string.hi_precations_btn_text);
                        btnAP.setTextColor(ContextCompat.getColor(this, R.color.white));
                        btnAP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                                i.putExtra("risklevel","4");
                                startActivity(i);
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
            else
            {
                PAGES = 1;
                adapter.notifyDataSetChanged();
                tvLocationName.setText(getResources().getString(R.string.txtNoLocation));
                txtMainText.setText(R.string.retrymessage);
                btnAP.setBackgroundColor(ContextCompat.getColor(this, R.color.retry));
                btnAP.setContentDescription(getResources().getString(R.string.retrybtn_desc));
                btnAP.setTextColor(Color.BLACK);
                btnAP.setText(R.string.retry);
                btnAP.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startHeatIndex();
                        finish();
                    }
                });

            }
            btnAP.invalidate();
        }
    }

    private void startHeatIndex() {
        Intent launchHeatIndex = new Intent(this, HeatIndexActivity.class);
        startActivity(launchHeatIndex);
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

    private void startHome() {
        Intent launchHome = new Intent(getApplicationContext(), MainActivity.class);
        launchHome.putExtra("from","TDY");
        startActivity(launchHome);
    }

    private void startReminder() {
        Intent launchSettings = new Intent(this, InitializeReminders.class);
        startActivity(launchSettings);
    }
}
