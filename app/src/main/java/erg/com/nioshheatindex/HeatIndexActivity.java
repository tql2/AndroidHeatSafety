package erg.com.nioshheatindex;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.DigitsKeyListener;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.adobe.mobile.*;

import com.adobe.marketing.mobile.MobileCore;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static erg.com.nioshheatindex.R.id.view;

public class HeatIndexActivity extends AppCompatActivity {

    //private final String DEBUG_TAG = "HeatIndexActivity";
    private String strParsedTime = "";
    private String strParsedVecTime = "";
    private int valueTemp = 0;  //will be displayed in the temperature box
    private int valueHumid = 0; //will be displayed in the humidity box
    private double valueCurrentHeatIndex = 0;   //feels like
    private double valueMaxHeatIndex = 0;   //max heat index
    private int r;   //Risk
    private int h;   //Hour
    private int ThermometerRiskLevel = 0; //Risk Level
    private final int myVecSize = 24; //the size of the data for today's calculation
    public static final List<String> todayStringArray = new ArrayList<String>(); //Cached list of hours with calculated HI available to other activities
    public static String siteLocation = null;
    public static String defaultLocation = null;
    private boolean spanish = false;
    private static List<String> myLocationVec = new ArrayList<>(); //Cached Location data
    private static List<String> myTempVec = new ArrayList<>(); //Cached Temperature data
    private static List<String> myHumidVec = new ArrayList<>(); //Cached Humidity data
    private static List<String> myTimeVec = new ArrayList<>(); //Cached Time data
    private ProcessXML weatherRequest;
    public static double dblLatitude = 0;
    public static double dblLongitude = 0;
    public static String defaultTZ = null;
    public static boolean isManual = false;
    public Hashtable myStates = new Hashtable();
    public String strCelsius = null;
    public String strCelsiusLike = null;
    public Boolean onLine = true;
    public Boolean useCache = false;
    public Boolean useCelsius = false;
    EditText editLocation;

    final String alertScreenShownPref = "alertScreenShown";
    Boolean alertScreenShown;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the Title
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_heat_index);

        MainActivity.sPage = "heatindexactivity";

        //Add pinch and zoom to layout
        final Zoomlayout zoomlayout = findViewById(R.id.zoomLayout);
        zoomlayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                zoomlayout.init(HeatIndexActivity.this);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        zoomlayout.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        // Add double click to refresh Heat gauge
        final View vw = findViewById(R.id.view);
        vw.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
                refreshActivity(vw);
            }

            @Override
            public void onDoubleClick(View v) {
                refreshActivity(vw);
            }
        });


        //Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        //Config.setDebugLogging(true);
        TelemetryProc.appLaunch("Temperature Gauge", "Heat Index", "nav");

        //get the Locale of the Operating System and current datetime
        getLocale();
        addNotification(getResources().getString(R.string.hi_banner_desc));

        // set NavBar button to focused state
        Drawable top = ContextCompat.getDrawable(this,R.drawable.heatbigred);
        Button hiButton = findViewById(R.id.hiButton);
        hiButton.setTextColor(ContextCompat.getColor(this, R.color.extreme));
        hiButton.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

        //final EditText editLocation = (findViewById(R.id.location));
        editLocation = (findViewById(R.id.location));
        final EditText editText1 = (findViewById(R.id.temp));
        final EditText editText2 = (findViewById(R.id.humidity));
        final View nbView = findViewById(R.id.include);
        final Button btnUseCurrent = findViewById(R.id.btnUseCurrent);


        btnUseCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the Location EditText to use current location
                editLocation.setText(defaultLocation);
            }
        });

        // Set keyboard input type for Temperature and Humidity EditText
        if(isCelsiusEnabled())
        {
            editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText1.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText1.setKeyListener(DigitsKeyListener.getInstance(true,true));
        }
        editText2.setKeyListener(DigitsKeyListener.getInstance(false,false));

        editLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //hide precautions button and text
                hideStuff();
                nbView.setVisibility(View.GONE);
                editLocation.setText("");
                btnUseCurrent.setVisibility(View.VISIBLE);
                editLocation.setCursorVisible(true);
                return false;
            }
        });




        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editLocation.getText().length() <= 0)
                {
                    addNotification(getResources().getString(R.string.editlocation_desc));
                }
                editLocation.setText("");
            }
        });

        editLocation.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        String strNewLocation = null;
                        String loc = editLocation.getText().toString();
                        if (loc.length() <= 0)
                        {
                            loc = defaultLocation;
                        }
                        // hide calc btn
                        btnUseCurrent.setVisibility(View.GONE);
                        //Show precautions button and text
                        showStuff();
                        // hide virtual keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        // put focus of location edittext
                        editLocation.setCursorVisible(false);
                        nbView.setVisibility(View.VISIBLE);
                        strNewLocation = getLocationByAddress(loc);
                        if (strNewLocation != getString(R.string.txtNotFound))
                        {
                            if (dblLatitude != 0 && dblLongitude != 0)
                            {
                                getWeatherByCoordinates(dblLatitude, dblLongitude);
                            }
                            else
                            {
                                showToastNoAddressFound();
                            }
                        }
                        else
                        {
                            showToastNoAddressFound();
                        }
                    return true;
                }
                return false;
            }
        });

        editText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                nbView.setVisibility(View.GONE);
                //hide precautions button and text
                hideStuff();
                editText1.setText("");
                editText1.setCursorVisible(true);
                if(isCelsiusEnabled())
                {
                    editText1.setHint(getString(R.string.pref_title_celsius));
                }
                else
                {
                    editText1.setHint(getString(R.string.txtTemperature));
                }
                return false;
            }
        });

        editText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCelsiusEnabled()) {
                    addNotification(getResources().getString(R.string.editTemperature_desc_cels));
                }
                else
                {
                    addNotification(getResources().getString(R.string.editTemperature_desc));
                }
                editText1.setText("");
            }
        });
        editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {

                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE)
                {
                    String sTemp = String.valueOf(editText1.getText());
                    String sShowTemp = sTemp;
                    if(isCelsiusEnabled()) {
                        if(sTemp.length() > 0 )
                        {
                            float inputValue = (float) parseUserNumber(sTemp);
                            float tmp = convertCelsiusToFahrenheit(inputValue);
                            sTemp = String.valueOf(tmp);
                        }
                    }
                    //Show precautions button and text
                    showStuff();
                    // In Range Temp Values are -50 F = -45.55 C;  140 F = 60.00 C
                    double parsedTemperature = sTemp.length() > 0 ? parseUserNumber(sTemp) : Double.NaN;
                    if (!Double.isNaN(parsedTemperature) && parsedTemperature >= -50 && parsedTemperature <= 140)
                    {
                        nbView.setVisibility(View.VISIBLE);
                        editLocation.setText(getString(R.string.labelForCalculated));
                        if(isCelsiusEnabled())
                        {
                            editText1.setText(sShowTemp + getResources().getString(R.string.celsiussymbol));
                        }
                        else
                        {
                            editText1.setText(sShowTemp + getResources().getString(R.string.fahrenheitsymbol));
                        }
                        // hide virtual keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        manualDataAction();
                    }
                    else
                    {
                        hideStuff();
                        editLocation.setText("");
                        editLocation.setHint(R.string.txtNumOutOfRange);
                        addNotification(getResources().getString(R.string.alert_temp_outrange));
                        editText1.setText("");
                        editText1.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        editText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                nbView.setVisibility(View.GONE);
                //hide precautions button and text
                hideStuff();
                editText2.setText("");
                editText2.setHint(getString(R.string.txtHumidity));
                editText2.setCursorVisible(true);
                return false;
            }
        });

        editText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotification(getResources().getString(R.string.editHumidity_desc));
                editText2.setText("");
            }
        });

        editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE)
                {
                    String sHumidity = String.valueOf(editText2.getText());

                    //Show precautions button and text
                    showStuff();

                    // catch if number is out of range here before manual function
                    double parsedHumidity = sHumidity.length() > 0 ? parseUserNumber(sHumidity) : Double.NaN;
                    if (!Double.isNaN(parsedHumidity) && parsedHumidity >= 0 && parsedHumidity <= 100)
                    {

                        editLocation.setText(getString(R.string.labelForCalculated));
                        String humVal = String.valueOf(editText2.getText());
                        humVal = humVal + getResources().getString(R.string.humidityUnit);
                        editText2.setText(humVal);

                        if(editText1.getText().length() > 0) {
                            nbView.setVisibility(View.VISIBLE);
                            // hide virtual keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            manualDataAction();
                        }
                        else
                        {
                            editText1.requestFocus();
                        }
                    }
                    else
                    {
                        hideStuff();
                        editLocation.setText("");
                        editLocation.setHint(R.string.txtNumOutOfRange);
                        addNotification(getResources().getString(R.string.alert_humidity_outrange));
                        editText2.setText("");
                        editText2.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });


        //getCurrentLocationName();



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
            public void onClick(View view) {
                startHome();
            }
        });

        ImageButton btnReminder = findViewById(R.id.btnReminders);
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReminder();
            }
        });
    }

    public void hideStuff(){
        TextView mainText = findViewById(R.id.tvMainText);
        Button btnAllPurpose = findViewById(R.id.btnAP);
        mainText.setVisibility(View.GONE);
        btnAllPurpose.setVisibility(View.GONE);
    }

    public void showStuff(){
        TextView mainText = findViewById(R.id.tvMainText);
        Button btnAllPurpose = findViewById(R.id.btnAP);
        mainText.setVisibility(View.VISIBLE);
        btnAllPurpose.setVisibility(View.VISIBLE);
    }

    // Disable back button
    @Override
    public void onBackPressed() {
    }

    private boolean isInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        android.net.Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null
                && (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    private void checkCachedData() {
        if(isCacheEnabled() && myTempVec.size() > 0)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            getHeatIndexFromCache();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobileCore.setApplication(getApplication());
        MobileCore.lifecycleStart(null);

        if(isInternetConnection()) {
            onLine = true;
            useCache = isCacheEnabled();
            useCelsius = isCelsiusEnabled();
            isManual = false;
            if (isLocationEnabled()) {
                getCurrentLocationName();
            } else {
                showToastNoLocationFound();
            }
        } else {
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

    private void disablePrecautionsBtn() {
        TextView mainText = findViewById(R.id.tvMainText);
        Button btnAllPurpose = findViewById(R.id.btnAP);
        mainText.setText(R.string.retrymessage);
        btnAllPurpose.setBackgroundColor(ContextCompat.getColor(this,R.color.retry));
        btnAllPurpose.setText(R.string.retry);
        btnAllPurpose.setTextColor(ContextCompat.getColor(this,R.color.black));
        btnAllPurpose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
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
                checkCachedData();
            }
        });
        dialog.show();
    }

    private void getCurrentLocationName(){
        GPSTracker gps = new GPSTracker(HeatIndexActivity.this);
        String addressStr = null;
        double latitude = 0;
        double longitude = 0;
        if (gps.canGetLocation()) {
            try
            {
                if((dblLatitude-(int)dblLatitude)!=0 && (dblLongitude-(int)dblLongitude)!=0 )
                {
                    latitude = dblLatitude;
                    longitude = dblLongitude;
                }
                else
                {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                }
                if((latitude-(int)latitude)!=0.0 && (longitude-(int)longitude)!=0.0 )
                {
                    Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                    List<Address> geoResults = null;
                    geoResults = geocoder.getFromLocation(latitude, longitude, 1);
                    while (geoResults.size()==0)
                    {
                        geoResults = geocoder.getFromLocation(latitude, longitude, 1);
                    }
                    if (geoResults.size() > 0)
                    {
                        Address adrs = geoResults.get(0);
                        String cityStr = null;
                        String stateStr = null;
                        String strDictState = null;
                        if (adrs.getLocality() != null)
                        {
                            if(adrs.getLocality().length() > 0)
                            {
                                cityStr = adrs.getLocality().toString();
                            }
                        }
                        if (adrs.getAdminArea() == null)
                        {
                            if(adrs.getCountryName() != null )
                            {
                                stateStr = adrs.getCountryName().toString();
                            }
                        }
                        else
                        {
                            if(adrs.getAdminArea().length() > 0)
                            {
                                stateStr = adrs.getAdminArea().toString();
                            }
                        }
                        if(stateStr != null) {
                            if (adrs.hasLatitude() && adrs.hasLongitude()) {
                                dblLatitude = adrs.getLatitude();
                                dblLongitude = adrs.getLongitude();
                            }
                        }
                        strDictState = getState(stateStr);
                        if (cityStr != null && cityStr.length() > 0 || strDictState != null && strDictState.length() > 0 )
                        {
                            addressStr = cityStr + " " + strDictState;
                        }
                    }
                }
                else
                {
                    //showToastNoAddressFound();
                    //WCS 2024 showToastNoLocationFound();
                    Toast.makeText(this,"IsManual = " + isManual,Toast.LENGTH_LONG).show();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
        }
        if(addressStr == null)
        {
            if(dblLatitude != 0 && dblLongitude != 0)
            {
                getWeatherByCoordinates(dblLatitude, dblLongitude);
            }
            else
            {
                //showToastNoAddressFound();
                showToastNoLocationFound();
            }
        }
        else
        {
            getWeatherByCoordinates(latitude, longitude);
        }

    }

    public String getLocationByAddress(String addressStr) {
        dblLatitude = 0;
        dblLongitude = 0;
        String cityName = getString(R.string.txtNotFound);
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> geoResults = null;
        try
        {
            int ctr = 0;
            geoResults = geocoder.getFromLocationName(addressStr, 1);
            while (geoResults.size()==0 && ctr < 3) {
                geoResults = geocoder.getFromLocationName(addressStr, 1);
                ctr++;
            }
            if (geoResults.size() > 0)
            {
                Address adrs = geoResults.get(0);
                String cityStr = null;
                String stateStr = null;
                //String strDictState = null;
                if (adrs.getLocality() != null)
                {
                    if(adrs.getLocality().length() > 0)
                    {
                        cityStr = adrs.getLocality().toString();
                    }
                }
                if (adrs.getAdminArea() == null)
                {
                    if(adrs.getCountryName() != null )
                    {
                        stateStr = adrs.getCountryName().toString();
                    }
                }
                else
                {
                    if(adrs.getAdminArea().length() > 0)
                    {
                        stateStr = adrs.getAdminArea().toString();
                    }
                }
                if(stateStr != null) {
                    if (adrs.hasLatitude() && adrs.hasLongitude()) {
                        dblLatitude = adrs.getLatitude();
                        dblLongitude = adrs.getLongitude();
                    }
                }
                String strDictState = getState(stateStr);
                if (cityStr != null && cityStr.length() > 0 || strDictState != null && strDictState.length() > 0 )
                {
                    cityName = cityStr + " " + strDictState;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherByCoordinates(double lat, double lng){
        try
        {
            if (lat != 0 && lng != 0)
            {
                weatherRequest = new ProcessXML(this, lat, lng);
                weatherRequest.execute();
            }
            else
            {
                showToastNoLocationFound();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void callBackData(ParsedDataSetWeather myData){
        if (myData == null) {
            showToastServerOutage();
            return;
        }
        double myValue;
        myValue = getHeatIndexFromNoaaData(myData);
        EditText editText1 = findViewById(R.id.temp);
        EditText editText2 = findViewById(R.id.humidity);
        try
        {
            if (myValue == 0)
            {
                //WCS 2024
                //showToastNoAddressFound();
                showToastServerOutage();
            }
            else
            {
                String tempVal = null;
                String humVal = null;
                if(isCelsiusEnabled()) {
                    tempVal = convertFahrenheitToCelsius(Integer.toString(valueTemp));
                    tempVal =  tempVal + getResources().getString(R.string.celsiussymbol);
                    editText1.setText(String.format(tempVal, Locale.getDefault()));
                }
                else
                {
                    tempVal = Integer.toString(valueTemp);
                    tempVal = tempVal + getResources().getString(R.string.fahrenheitsymbol);
                    editText1.setText(String.format(tempVal, Locale.getDefault()));
                }

                humVal = Integer.toString(valueHumid);
                humVal = humVal + getResources().getString(R.string.humidityUnit);
                editText2.setText(humVal);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getState( String st){
        String s = st;
        String lastTwo = null;
        if (st != null && st.length() >= 2) {
            lastTwo = st.substring(st.length() - 2);
        }
        if (lastTwo != null && lastTwo.length() >= 2)
        {
            //set up the hashtable for state list
            myStates.put("AL", "Alabama");
            myStates.put("AK", "Alaska");
            myStates.put("AZ", "Arizona");
            myStates.put("AR", "Arkansas");
            myStates.put("CA", "California");
            myStates.put("CO", "Colorado");
            myStates.put("CT", "Connecticut");
            myStates.put("DE", "Delaware");
            myStates.put("FL", "Florida");
            myStates.put("GA", "Georgia");
            myStates.put("GU", "Guam");
            myStates.put("HI", "Hawaii");
            myStates.put("ID", "Idaho");
            myStates.put("IL", "Illinois");
            myStates.put("IN", "Indiana");
            myStates.put("IA", "Iowa");
            myStates.put("KS", "Kansas");
            myStates.put("KY", "Kentucky");
            myStates.put("LA", "Louisiana");
            myStates.put("ME", "Maine");
            myStates.put("MD", "Maryland");
            myStates.put("MA", "Massachusetts");
            myStates.put("MI", "Michigan");
            myStates.put("MN", "Minnesota");
            myStates.put("MS", "Mississippi");
            myStates.put("MO", "Missouri");
            myStates.put("MT", "Montana");
            myStates.put("NE", "Nebraska");
            myStates.put("NV", "Nevada");
            myStates.put("NH", "New Hampshire");
            myStates.put("NJ", "New Jersey");
            myStates.put("NM", "New Mexico");
            myStates.put("NY", "New York");
            myStates.put("NC", "North Carolina");
            myStates.put("ND", "North Dakota");
            myStates.put("OH", "Ohio");
            myStates.put("OK", "Oklahoma");
            myStates.put("OR", "Oregon");
            myStates.put("PA", "Pennsylvania");
            myStates.put("PR", "Puerto Rico");
            myStates.put("RI", "Rhode Island");
            myStates.put("SC", "South Carolina");
            myStates.put("SD", "South Dakota");
            myStates.put("TN", "Tennessee");
            myStates.put("TX", "Texas");
            myStates.put("UT", "Utah");
            myStates.put("VA", "Virginia");
            myStates.put("VI", "Virgin Islands");
            myStates.put("VT", "Vermont");
            myStates.put("WA", "Washington");
            myStates.put("WV", "West Virginia");
            myStates.put("WI", "Wisconsin");
            myStates.put("WY", "Wyoming");
            myStates.put("D.C.", "District of Columbia");
            Object stateAb = myStates.get(lastTwo);
            if (stateAb != null) {
                s = removeLastChar(s) + stateAb.toString();
            }
        }
        return s;
    }

    @NonNull
    private static String removeLastChar(String str) {
        return str.substring(0,str.length()-2);
    }

    private double getHeatIndexFromNoaaData(ParsedDataSetWeather parsedExampleDataSet){
        try {
            //the size of the data for today's calculation
            todayStringArray.clear();
            //temperature vector, humid vector and time vector for storing the data from XML
            myLocationVec = parsedExampleDataSet.getlocation();
            myTempVec = parsedExampleDataSet.gettemperature();
            myHumidVec = parsedExampleDataSet.gethumidity();
            myTimeVec = parsedExampleDataSet.getmaxtime();

            // Populate edittext location will location description from XML
            for (int i = 0; i < myLocationVec.size(); i++) {
                String val = (String) myLocationVec.get(i);
                String[] parts = val.split(", ");
                String Loc = parts.length > 0 ? parts[0].trim() : val.trim();
                String renameLoc = getState(Loc);
                EditText editLocation = findViewById(R.id.location);
                editLocation.setText(renameLoc);
                siteLocation = renameLoc;
                if(defaultLocation == null)
                {
                    defaultLocation = siteLocation;
                }
            }

            int size = Math.min(myVecSize, Math.min(myTempVec.size(),
                    Math.min(myHumidVec.size(), myTimeVec.size())));
            if (size == 0) {
                return 0;
            }

            double[] myHIarray = new double[size];
            int[] myTempArray2 = new int[size];
            int[] myHumidArray2 = new int[size];
            String[] myTimeArray = new String[size];
            for (int i = 0; i < size; i++) {
                myTempArray2[i] = stringToInt(myTempVec.get(i));
                myHumidArray2[i] = stringToInt(myHumidVec.get(i));
                String val = myTimeVec.get(i);
                strParsedVecTime = parseTimeVecDate(val);
                myTimeArray[i] = strParsedVecTime;
            }

            //calculate heat index for each hour and put them in myHIarray
            for (int i = 0; i < size; i++) {
                myHIarray[i] = calculateHeatIndex.heatIndexCal(myTempArray2[i], myHumidArray2[i]);
                if (i < 14) {
                    todayStringArray.add(myTimeArray[i] + "," + myHIarray[i]);
                }
            }

            if(defaultTZ.length() > 0)
            {
                strParsedTime = getDate(defaultTZ);
            }

            double myValue = 0;   //variable for storing the HI value shown on the screen later

            //current value is calculated
            valueCurrentHeatIndex = 0;
            if (size > 0) {
                valueTemp = myTempArray2[0];
                valueHumid = myHumidArray2[0];
                myValue = myHIarray[0];
                valueCurrentHeatIndex = myValue;
            }

            //find the max for today
            valueMaxHeatIndex = 0;
            if (size > 0) {
                for (int i = 0; i < size; i++)
                {
                    if (i == 0)
                    {
                        myValue = myHIarray[i];
                    }
                    else
                    {
                        if (myValue < myHIarray[i])
                        {
                            myValue = myHIarray[i];
                        }
                    }
                }
                valueMaxHeatIndex = myValue;
                if (valueCurrentHeatIndex > 0)
                {
                    h = (int)valueCurrentHeatIndex;
                }
                else
                {
                    h = (int)valueMaxHeatIndex;
                }
                if (h < 60)
                {
                    ThermometerRiskLevel = 1; // minimal risk
                }
                else if (h < 80)
                {
                    ThermometerRiskLevel = 2; // low risk
                }
                else if (h < 95)
                {
                    ThermometerRiskLevel = 3; // high risk
                }
                else {
                    ThermometerRiskLevel = 4; // extreme risk
                }
                r = ThermometerRiskLevel;

                setGuage(r, strParsedTime, h, spanish, valueTemp, valueHumid, false);
            }

            int myCountMax = 0;
            if (size > 0) {
                for (int i = 0; i < size; i++)
                {
                    if (myTempArray2[i] < 80)
                    {
                        myCountMax = myCountMax + 1;
                    }
                }
                if (myCountMax == size) {
                    valueTemp = myTempArray2[0];
                    valueHumid = myHumidArray2[0];
                }
            }
            return myValue;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    private void setGuage(int threatLevel, String currentTime, int currentTemp, boolean spanish, double temp, int humidity, boolean manual){

        String sttsTemp = Double.toString(temp);
        String sttsLike = Integer.toString(currentTemp);
        if(isCelsiusEnabled())
        {
            strCelsiusLike = convertFahrenheitToCelsius(String.valueOf(sttsLike));
            strCelsius = convertFahrenheitToCelsius(String.valueOf(sttsTemp));
        }
        else
        {
            if(sttsTemp.contains(".0"))
            {
                sttsTemp = sttsTemp.replace(".0", "");
            }
        }
        String sttsLevel = Integer.toString(threatLevel);
        String sttsTime = currentTime;
        String sttsHum = Integer.toString(humidity);
        TextView mainText = findViewById(R.id.tvMainText);
        EditText editTextTemp = findViewById(R.id.temp);
        EditText editTextHumid = findViewById(R.id.humidity);

        if(isCelsiusEnabled())
        {
            editTextTemp.setHint(sttsTemp +  getResources().getString(R.string.celsiussymbol));
        }
        else
        {
            editTextTemp.setHint(sttsTemp +  getResources().getString(R.string.fahrenheitsymbol));
        }
        editTextHumid.setHint(sttsHum + getResources().getString(R.string.humidityUnit));
        String strWords = "";
        Button btnAllPurpose = findViewById(R.id.btnAP);
        String strRiskLevel = "";
        switch (threatLevel)
        {
            case 0:
                mainText.setText(R.string.retrymessage);
                btnAllPurpose.setBackgroundColor(ContextCompat.getColor(this, R.color.retry));
                btnAllPurpose.setText(R.string.retry);
                btnAllPurpose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
                break;
            case 1:
                strRiskLevel = getResources().getString(R.string.txtMinimal);
                mainText.setText(R.string.txtMinimalRisk);
                btnAllPurpose.setBackgroundColor(ContextCompat.getColor(this,R.color.minimal));
                btnAllPurpose.setText(R.string.hi_allpurpose_btn_minrisk);
                btnAllPurpose.setTextColor(ContextCompat.getColor(this,R.color.white));
                btnAllPurpose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                        i.putExtra("risklevel","1");
                        startActivity(i);
                    }
                });
                break;
            case 2:
                strRiskLevel = getResources().getString(R.string.txtcaution);
                mainText.setText(R.string.txtLowRisk);
                btnAllPurpose.setBackgroundColor(ContextCompat.getColor(this,R.color.low));
                btnAllPurpose.setText(R.string.hi_precations_btn_text);
                btnAllPurpose.setTextColor(ContextCompat.getColor(this,R.color.black));
                btnAllPurpose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                        i.putExtra("risklevel","2");
                        startActivity(i);
                    }
                });
                break;
            case 3:
                strRiskLevel = getResources().getString(R.string.txtwarning);
                mainText.setText(R.string.txtHighRisk);
                btnAllPurpose.setBackgroundColor(ContextCompat.getColor(this,R.color.high));
                btnAllPurpose.setText(R.string.hi_precations_btn_text);
                btnAllPurpose.setTextColor(ContextCompat.getColor(this,R.color.black));
                btnAllPurpose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                        i.putExtra("risklevel","3");
                        startActivity(i);
                    }
                });
                break;
            case 4:
                strRiskLevel = getResources().getString(R.string.txtdanger);
                mainText.setText(R.string.txtExtremeRisk);
                btnAllPurpose.setBackgroundColor(ContextCompat.getColor(this,R.color.extreme));
                btnAllPurpose.setText(R.string.hi_precations_btn_text);
                btnAllPurpose.setTextColor(ContextCompat.getColor(this,R.color.white));
                btnAllPurpose.setOnClickListener(new View.OnClickListener() {
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

        if(threatLevel == 0)
        {
        }
        else if(manual)
        {
            String sTemp = sttsTemp;
            String sLike = sttsLike;
            String sCelsius = strCelsius;
            String sCelsiusLike = strCelsiusLike;


            if(sttsTemp.contains("-"))
            {
                sTemp = "negative " + sttsTemp;
            }


            if(sttsLike.contains("-"))
            {
                sLike = "negative " + sttsLike;
            }

            if(isCelsiusEnabled())
            {
                if(sCelsius.contains(".")) {
                    sCelsius = sCelsius.replace(".", " " + getResources().getString(R.string.txtpoint) + " ");
                }
                if(sCelsiusLike.contains(".")) {
                    sCelsiusLike = sCelsiusLike.replace(".", " " + getResources().getString(R.string.txtpoint) + " ");
                }
                strWords = getResources().getString(R.string.txtheatindexmanualsentencepart1) + " " + sCelsius + " " + getResources().getString(R.string.txtheatindexmanualsentencepart2celsius) + " " + sttsHum + " " + getResources().getString(R.string.txtheatindexmanualsentencepart3) + " " + sCelsiusLike + " " + getResources().getString(R.string.txtheatindexmanualsentencepart4celsius) + " " + strRiskLevel;
            }
            else
            {
                if(sTemp.contains(".0"))
                {
                    sTemp = sTemp.replace(".0", " ");
                }
                strWords = getResources().getString(R.string.txtheatindexmanualsentencepart1) + " " + sTemp + " " + getResources().getString(R.string.txtheatindexmanualsentencepart2) + " " + sttsHum + " " + getResources().getString(R.string.txtheatindexmanualsentencepart3) + " " + sLike + " " + getResources().getString(R.string.txtheatindexmanualsentencepart4) + " " + strRiskLevel;
            }

            addNotification(strWords);
        }
        else
        {
            String sCelsius = strCelsius;
            String sCelsiusLike = strCelsiusLike;
            if(isCelsiusEnabled())
            {
                if(sCelsius.contains(".")) {
                    sCelsius = sCelsius.replace(".", " " + getResources().getString(R.string.txtpoint) + " ");
                }
                if(sCelsiusLike.contains(".")) {
                    sCelsiusLike = sCelsiusLike.replace(".", " " + getResources().getString(R.string.txtpoint) + " ");
                }
                strWords = getResources().getString(R.string.txtheatindexsentencepart1) + " " + siteLocation + " " + getResources().getString(R.string.txtheatindexsentencepart1_1) + " " + sttsTime + getResources().getString(R.string.txtheatindexsentencepart2) + " " + sCelsius + " " + getResources().getString(R.string.txtheatindexsentencepart3celsius) + " " + sttsHum + " " + getResources().getString(R.string.txtheatindexsentencepart4) + " " + sCelsiusLike + " " + getResources().getString(R.string.txtheatindexsentencepart5celsius) + " " + strRiskLevel;
            }
            else
            {
                if(sttsTemp.contains(".0"))
                {
                    sttsTemp = sttsTemp.replace(".0", " ");
                }
                strWords = getResources().getString(R.string.txtheatindexsentencepart1) + " " + siteLocation + " " + getResources().getString(R.string.txtheatindexsentencepart1_1) + " " + sttsTime + getResources().getString(R.string.txtheatindexsentencepart2) + " " + sttsTemp + " " + getResources().getString(R.string.txtheatindexsentencepart3) + " " + sttsHum + " " + getResources().getString(R.string.txtheatindexsentencepart4) + " " + sttsLike + " " + getResources().getString(R.string.txtheatindexsentencepart5) + " " + strRiskLevel;
            }
            addNotification(strWords);
        }

        final GaugeView myGaugeView = findViewById(view);
        myGaugeView.setmSpanish(spanish);
        final double currentNeedle = heatIndexToPercent(currentTemp);
        final int finalTemp = currentTemp;
        myGaugeView.setmNeedle(0.0f);
        myGaugeView.setmFeelsLikeTemp(String.valueOf(0f).concat("°F"));
        myGaugeView.setmTime(currentTime);
        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(1000);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                myGaugeView.setmNeedle(cubicEaseOut((float)animation.getAnimatedValue()) * currentNeedle);
                if(isCelsiusEnabled())
                {
                    double strC = Double.parseDouble(strCelsiusLike);

                    //58.3 C
                    if(strC > 58.3){
                        //myGaugeView.setmFeelsLikeTemp(">" + strCelsiusLike.concat("°C"));
                        myGaugeView.setmFeelsLikeTemp(">" + getResources().getString(R.string.fiftyeightthree) + "°C");
                        showHeatNotice("C");
                    }else{
                        myGaugeView.setmFeelsLikeTemp(strCelsiusLike.concat("°C"));
                    }
                }
                else
                {
                    int workingTemp = (int)(cubicEaseOut((float)animation.getAnimatedValue()) * finalTemp);
                    if(workingTemp > 137){
                        //myGaugeView.setmFeelsLikeTemp(">" + String.valueOf(workingTemp).concat("°F"));
                        myGaugeView.setmFeelsLikeTemp(">" + getResources().getString(R.string.onethirtyseven) + "°F");
                        showHeatNotice("F");
                    }else{
                        myGaugeView.setmFeelsLikeTemp(String.valueOf(workingTemp).concat("°F"));
                    }
                }
            }
        });
        animation.start();
    }

    public void showHeatNotice(String type){
        String noticeText;
        if("F".equals(type)){
            noticeText = getResources().getString(R.string.heatnoticealert_F);
        }else{
            noticeText = getResources().getString(R.string.heatnoticealert_C);
        }
        SpannableString spannableStr = new SpannableString(noticeText);
        if (noticeText.length() >= 211) {
            spannableStr.setSpan(new StyleSpan(Typeface.ITALIC), 190, 211, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View viewDialog = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog, null);
        alert.setView(viewDialog);
        TextView title = (TextView) viewDialog.findViewById(R.id.title);
        TextView message = (TextView) viewDialog.findViewById(R.id.message);
        title.setText(R.string.notice);
        message.setText(spannableStr);
        alert.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });
       alert.create();
       alert.show();
    }

    private void manualDataAction() {
        isManual = true;
        EditText etTemp = findViewById(R.id.temp);
        EditText etHumidity = findViewById(R.id.humidity);
        String sTemp = String.valueOf(etTemp.getText());
        sTemp = sTemp.replace("℃", "");
        sTemp = sTemp.replace("℉", "");
        if(isCelsiusEnabled())
        {
            float inputValue = (float) parseUserNumber(sTemp);
            float tmp = convertCelsiusToFahrenheit(inputValue);
            sTemp = String.valueOf(tmp);
        }
        String sHumidity = String.valueOf(etHumidity.getText());
        sHumidity = sHumidity.replace("%", "");
        try
        {
            Calendar newCal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            strParsedTime = sdf.format(newCal.getTime());
            double temp = parseUserNumber(sTemp);
            int humidity = (int) parseUserNumber(sHumidity);
            if (Double.isNaN(temp) || Double.isNaN(humidity)) {
                throw new NumberFormatException("Invalid manual weather input");
            }
            double heatIndex = calculateHeatIndex.heatIndexCal(temp, humidity);

            if (heatIndex < 60) {
                ThermometerRiskLevel = 1; // Minimal
            } else if (heatIndex < 80) {
                ThermometerRiskLevel = 2; // Caution
            } else if (heatIndex < 95) {
                ThermometerRiskLevel = 3; // Warning
            } else {
                ThermometerRiskLevel = 4; // Danger
            }
            r = ThermometerRiskLevel;
            todayStringArray.clear();
            siteLocation = null;
            setGuage(r, strParsedTime, (int) heatIndex, spanish, temp, humidity, true);
        }
        catch (Exception e)
        {
            //Log.e(DEBUG_TAG, "WeatherQueryError", e);
        }
    }

    public double getHeatIndexFromCache(){

        String cachedStartTime = null;
        // Populate edittext location will location description from XML
        for (int i = 0; i < myLocationVec.size(); i++)
        {
            String val = myLocationVec.get(i);
            String parts[] = val.split(", ");
            String Loc = parts[0];
            String renameLoc = getState(Loc);
            EditText editLocation = findViewById(R.id.location);
            editLocation.setText(renameLoc);
            siteLocation = renameLoc;
        }

        int size = Math.min(myVecSize, Math.min(myTempVec.size(),
                Math.min(myHumidVec.size(), myTimeVec.size())));
        if (size == 0) {
            return 0;
        }
        double[] myHIarray = new double[size];
        int[] myTempArray2 = new int[size];
        int[] myHumidArray2 = new int[size];
        for (int i = 0; i < size; i++) {
            myTempArray2[i] = stringToInt(myTempVec.get(i));
            myHumidArray2[i] = stringToInt(myHumidVec.get(i));
        }

        String[] myTimeArray = new String[size];
        for (int i = 0; i < size; i++)
        {
            String val = myTimeVec.get(i);

            if(i == 1)
            {
                cachedStartTime = parseTimeVecDate(val);
            }
            strParsedVecTime = parseTimeVecDate(val);
            myTimeArray[i] = strParsedVecTime;
        }

        //calculate heat index for each hour and put them in myHIarray
        for (int i = 0; i < size; i++)
        {
            myHIarray[i] = calculateHeatIndex.heatIndexCal(myTempArray2[i], myHumidArray2[i]);
            todayStringArray.add(myTimeArray[i] + "," + myHIarray[i]);
        }

        if(defaultTZ.length() > 0)
        {
            strParsedTime = getDate(defaultTZ);
        }

        double myValue = 0;   //variable for storing the HI value shown on the screen later

        //current value is calculated
        valueCurrentHeatIndex = 0;
        if (size > 0) {
            valueTemp = myTempArray2[0];
            valueHumid = myHumidArray2[0];
            myValue = myHIarray[0];
            valueCurrentHeatIndex = myValue;
        }

        //find the max for today
        valueMaxHeatIndex = 0;
        if (size > 0) {
            for (int i = 0; i < size; i++)
            {
                if (i == 0)
                {
                    myValue = myHIarray[i];
                }
                else
                {
                    if (myValue < myHIarray[i])
                    {
                        myValue = myHIarray[i];
                    }
                }
            }
            valueMaxHeatIndex = myValue;
            if (valueCurrentHeatIndex > 0)
            {
                h = (int)valueCurrentHeatIndex;
            }
            else
            {
                h = (int)valueMaxHeatIndex;
            }
            if (h < 60)
            {
                ThermometerRiskLevel = 1; // Minimal
            }
            else if (h < 80)
            {
                ThermometerRiskLevel = 2; // Caution
            }
            else if (h < 95)
            {
                ThermometerRiskLevel = 3; // Warning
            }
            else {
                ThermometerRiskLevel = 4; // Danger
            }
            r = ThermometerRiskLevel;
            String hours = getHoursDiff(cachedStartTime);
            int hrsStale = 0;
            try
            {
                hrsStale = Integer.parseInt(hours);
            }
            catch(NumberFormatException nfe)
            {
                hrsStale = 0;
            }
            if(hrsStale < 10) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.reachabilityerror);
                alert.setMessage(R.string.txtUseCachedData);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        addNotification(getResources().getString(R.string.txtusingcache));
                        setGuage(r, strParsedTime, h, spanish, valueTemp, valueHumid, false);
                    }
                });
                alert.show();
            }
            else
            {
                TextView tvL = findViewById(R.id.location);
                tvL.setText("");
                tvL.setHint(getResources().getString(R.string.hint_location_edittext));

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.reachabilityerror);
                alert.setMessage(R.string.alert_nonetwork_nocache_msg);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        addNotification(getResources().getString(R.string.alert_nonetwork_nocache_msg));
                    }
                });
                alert.show();
            }
        }
        return myValue;
    }

    protected double heatIndexToPercent(int heatIndex) {
        double heatIndexF = (double) heatIndex;
        if (heatIndexF < 60) {
            return 0.0f; // minimal risk of heat, pegged at the bottom
        } else if (heatIndexF < 80) {
            return (double) ((heatIndexF - 60)/19 * 0.333 + 0.0001); // low risk 0.0001
        } else if (heatIndexF < 95) {
            if (heatIndexF >= 80 && heatIndexF < 94) {
                return (double) (((heatIndexF - 80)/14 * 0.333) + 0.3340); // high risk  0.3339
            }else{
                return (double) (((heatIndexF - 80)/14 * 0.333) + 0.3338); // high risk  0.3339
            }
        } else if (heatIndexF < 131) {
            return (double) (((heatIndexF - 95)/35 * 0.333) + 0.680); //highest risk* 0.667
        } else {
            return 1.0f; //highest risk, pegged at top of scale
        }
    }

    private void getLocale(){
        spanish = !Locale.getDefault().getLanguage().equalsIgnoreCase("en");
        TimeZone tz = TimeZone.getDefault();
        defaultTZ = tz.toString();
    }

    private String getHoursDiff(String datePrev) {
        String daysAsTime = "";
        long day = 0, diff = 0;
        String outputPattern = "yyyy:MM:dd HH:mm:ss";
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Calendar c = Calendar.getInstance();
        String dateCurrent = outputFormat.format(c.getTime());
        datePrev = datePrev.replace("-",":");
        try
        {
            Date  date1 = outputFormat.parse(datePrev);
            Date date2 = outputFormat.parse(dateCurrent);
            diff = date2.getTime() - date1.getTime();
            day = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        if (day == 0) {
            long hour = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
            if (hour == 0) {
                daysAsTime = "0";
            }
            else {
                daysAsTime = String.valueOf(hour);
            }
        }
        else
        {
            daysAsTime = "24";
        }
        return daysAsTime;
    }

    protected float cubicEaseOut(float percentage) {
        float easedPercentage = (percentage - 1);
        return easedPercentage * easedPercentage * easedPercentage + 1;
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

    public void refreshActivity (View v){
        Intent launch = new Intent(this, HeatIndexActivity.class);
        startActivity(launch);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private String parseTimeVecDate(String xmldate){

        String strXmlTime = "";
        String strFullXmlDate = xmldate.substring(0,19);
        String strExtractedDate = strFullXmlDate.substring(0,10);
        String strExtractedTime = strFullXmlDate.substring(11);
        String strExtractedTZ = "GMT" + strFullXmlDate.substring(19);
        defaultTZ =  "GMT" + xmldate.substring(19);
        String strXmlDate = strExtractedDate + " " + strExtractedTime;
        try
        {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = sourceFormat.parse(strXmlDate); // => Date is in UTC now
            TimeZone tz = TimeZone.getTimeZone(strExtractedTZ);
            SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            destFormat.setTimeZone(tz);
            strXmlTime = destFormat.format(parsed);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            strXmlTime = "00-00-0000 00:00";
        }
        return strXmlTime;
    }

    private String getDate(String strTZ) {
        String sampm = null;
        String shr = null;
        String stime = null;
        String smin = null;
        String sTZ = null;
        sTZ = strTZ.replace("--", "+");
        TimeZone tz = TimeZone.getTimeZone(sTZ);
        Calendar c = Calendar.getInstance(tz);
        shr = String.format("%02d", c.get(Calendar.HOUR_OF_DAY));
        switch (shr)
        {
            case "00":
                shr = "12";
                sampm = " AM";
                break;
            case "01":
                shr = "1";
                sampm = " AM";
                break;
            case "02":
                shr = "2";
                sampm = " AM";
                break;
            case "03":
                shr = "3";
                sampm = " AM";
                break;
            case "04":
                shr = "4";
                sampm = " AM";
                break;
            case "05":
                shr = "5";
                sampm = " AM";
                break;
            case "06":
                shr = "6";
                sampm = " AM";
                break;
            case "07":
                shr = "7";
                sampm = " AM";
                break;
            case "08":
                shr = "8";
                sampm = " AM";
                break;
            case "09":
                shr = "9";
                sampm = " AM";
                break;

            case "10":
                shr = "10";
                sampm = " AM";
                break;
            case "11":
                shr = "11";
                sampm = " AM";
                break;
            case "12":
                shr = "12";
                sampm = " PM";
                break;
            case "13":
                shr = "1";
                sampm = " PM";
                break;
            case "14":
                shr = "2";
                sampm = " PM";
                break;
            case "15":
                shr = "3";
                sampm = " PM";
                break;
            case "16":
                shr = "4";
                sampm = " PM";
                break;
            case "17":
                shr = "5";
                sampm = " PM";
                break;
            case "18":
                shr = "6";
                sampm = " PM";
                break;
            case "19":
                shr = "7";
                sampm = " PM";
                break;
            case "20":
                shr = "8";
                sampm = " PM";
                break;
            case "21":
                shr = "9";
                sampm = " PM";
                break;
            case "22":
                shr = "10";
                sampm = " PM";
                break;
            case "23":
                shr = "11";
                sampm = " PM";
                break;
            case "24":
                shr = "12";
                sampm = " PM";
                break;
            default:
                break;
        }
        smin = ":" + String.format("%02d", c.get(Calendar.MINUTE));
        stime = shr + smin + sampm;
        return stime;
    }

    private double parseUserNumber(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim().replaceAll("[^0-9,.-]", "");
        if (value.isEmpty()) {
            throw new NumberFormatException("Empty numeric value");
        }
        char decimalSeparator = java.text.DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator();
        if (decimalSeparator == ',') {
            value = value.replace(".", "").replace(',', '.');
        } else {
            value = value.replace(",", "");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return Double.NaN;
        }
    }

    private int stringToInt(String inValue) {
        int retValue = 0;
        try
        {
            retValue = Integer.parseInt(inValue);
        }
        catch (Exception e)
        {
           // Log.e(DEBUG_TAG, "Integer Parser error ", e);
        }
        return retValue;
    }

    private void showToastServerOutage(){
        dblLatitude = 0;
        dblLongitude = 0;
        Context context = getApplicationContext();
        CharSequence text = getString(R.string.txtServerOutage);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_ServerOutage_Title);
        alert.setMessage(R.string.txtServerOutage);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
             //   getCurrentLocationName();
            }
        });
        alert.show();
    }

    @Override
    protected void onDestroy() {
        if (weatherRequest != null) {
            weatherRequest.cancel();
            weatherRequest = null;
        }
        super.onDestroy();
    }

    private void showToastNoLocationFound(){
        dblLatitude = 0;
        dblLongitude = 0;
        Context context = getApplicationContext();
        CharSequence text = getString(R.string.txtLocationNotFound);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_NoLocationFound_Title);
        alert.setMessage(R.string.txtLocationNotFound);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                getCurrentLocationName();
            }
        });
        alert.show();
    }

    private void showToastNoAddressFound(){
        dblLatitude = 0;
        dblLongitude = 0;
        Context context = getApplicationContext();
        CharSequence text = getString(R.string.txtAddressNotFound);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_locationNotFound_Title);
        alert.setMessage(R.string.txtAddressNotFound);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getCurrentLocationName();
            }
        });
        alert.show();
    }

    public String convertFahrenheitToCelsius(String s){
        // Change Farenheit to Celsius
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
                if(!str.contains("."))
                {
                    str = str + ".0";
                }
            }
        }
        return str;
    }

    private float convertCelsiusToFahrenheit(float celsius) {
        // Converts to Fahrenheit
        return ((celsius * 9) / 5) + 32;
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

    private void startHome() {
        Intent launchHome = new Intent(this, MainActivity.class);
        launchHome.putExtra("from","HI");
        startActivity(launchHome);
    }

    private void startReminder() {
        Intent launchSettings = new Intent(this, InitializeReminders.class);
        startActivity(launchSettings);
    }

}
