package erg.com.nioshheatindex;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

//import com.adobe.mobile.*;

import com.adobe.marketing.mobile.MobileCore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static erg.com.nioshheatindex.MainActivity.sPage;

public class RemindersActivity extends AppCompatActivity {

    String strStartHour, strEndHour, strSummary, strValue;
    public static String strTextViewSummary = "";
    List<String> hoursArray;
    Spinner spnrStartTimes;
    Spinner spnrEndTimes;
    private Button btn15;
    private Button btn30;
    private Button btn60;
    private Button vBtnSun;
    private Button vBtnMon;
    private Button vBtnTue;
    private Button vBtnWed;
    private Button vBtnThu;
    private Button vBtnFri;
    private Button vBtnSat;
    TextView tvSummary;
    SharedPreferences prefs;
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reminders);

        //Lock the Device Orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        strValue = sPage;
        prefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        showPrefs();


        btnDone = findViewById(R.id.btnDone);
        tvSummary = findViewById(R.id.tvSummary);
        String sum = prefs.getString("summary", "");
        tvSummary.setText(sum);

        // Send metrics data to Omniture
        //Config.setContext(this.getApplicationContext());
        TelemetryProc.appLaunch(getString(R.string.txtreminder), getResources().getString(R.string.txtreminder), "nav");

        hoursArray = Arrays.asList(getResources().getStringArray(R.array.hours_array));
        final ArrayAdapter<String> hoursArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_hours, hoursArray) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        hoursArrayAdapter.setDropDownViewResource(R.layout.spinner_hours);

        final Button btnSet = findViewById(R.id.btnSetAlarm);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(RemindersActivity.this).create();
                alertDialog.setTitle(getString(R.string.msg_title_set));
                alertDialog.setMessage(strSummary);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                // Send metrics data to Omniture
                                TelemetryProc.appLaunch(getString(R.string.msg_title_set), getResources().getString(R.string.txtreminder), "nav");
                                setCalEvents();
                            }
                        });
                alertDialog.show();
            }
        });

        spnrStartTimes = findViewById(R.id.spinnerStartTime);
        spnrStartTimes.setAdapter(hoursArrayAdapter);

        spnrStartTimes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    strStartHour = (String) parent.getItemAtPosition(position);
                    int End = spnrEndTimes.getSelectedItemPosition();
                    if (End < position) {
                        spnrEndTimes.setSelection(position);
                    }
                    setTime();
                    spnrEndTimes.requestFocus();
//                    if(passCheck()){
//                        btnSet.setEnabled(true);
//                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        spnrEndTimes = findViewById(R.id.spinnerEndTime);
        spnrEndTimes.setAdapter(hoursArrayAdapter);
        spnrEndTimes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    strEndHour = (String) parent.getItemAtPosition(position);
                    int Start = spnrStartTimes.getSelectedItemPosition();
                    if (Start > position) {
                        spnrStartTimes.setSelection(position);
                    }
                    setTime();
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }else{
                        btnSet.setEnabled(false);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initializePickers();

        btn15 = findViewById(R.id.btn15);
        btn15.setTag(R.string.selected, 0);
        btn15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) btn15.getTag(R.string.selected);
                if (i == 0) {
                    btn15.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.button_border_selected);
                    btn15.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    btn15.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.button_border);
                    btn15.setTextColor(Color.parseColor("#000000"));
                }
                btn30.setBackgroundResource(R.drawable.button_border);
                btn30.setTextColor(Color.parseColor("#000000"));
                btn60.setBackgroundResource(R.drawable.button_border);
                btn60.setTextColor(Color.parseColor("#000000"));
                if(passCheck()){
                    btnSet.setEnabled(true);
                }
                showMessage();
            }
        });


        btn30 = findViewById(R.id.btn30);
        btn30.setTag(R.string.selected, 0);
        btn30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) btn30.getTag(R.string.selected);
                if (i == 0) {
                    btn30.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.button_border_selected);
                    btn30.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    btn30.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.button_border);
                    btn30.setTextColor(Color.parseColor("#000000"));
                }
                btn15.setBackgroundResource(R.drawable.button_border);
                btn15.setTextColor(Color.parseColor("#000000"));
                btn60.setBackgroundResource(R.drawable.button_border);
                btn60.setTextColor(Color.parseColor("#000000"));
                if(passCheck()){
                    btnSet.setEnabled(true);
                }
                showMessage();
            }
        });

        btn60 = findViewById(R.id.btn60);
        btn60.setTag(R.string.selected, 0);
        btn60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) btn60.getTag(R.string.selected);
                if (i == 0) {
                    btn60.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.button_border_selected);
                    btn60.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    btn60.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.button_border);
                    btn60.setTextColor(Color.parseColor("#000000"));
                }
                btn15.setBackgroundResource(R.drawable.button_border);
                btn15.setTextColor(Color.parseColor("#000000"));
                btn30.setBackgroundResource(R.drawable.button_border);
                btn30.setTextColor(Color.parseColor("#000000"));
                if(passCheck()){
                    btnSet.setEnabled(true);
                }
                showMessage();
            }
        });

        switch (prefs.getInt("interval", 0)) {
            case 15:
                btn15.setTag(R.string.selected, 1);
                btn15.setBackgroundResource(R.drawable.button_border_selected);
                btn15.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 30:
                btn30.setTag(R.string.selected, 1);
                btn30.setBackgroundResource(R.drawable.button_border_selected);
                btn30.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 60:
                btn60.setTag(R.string.selected, 1);
                btn60.setBackgroundResource(R.drawable.button_border_selected);
                btn60.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            default:
                break;
        }

        Button btnClear = findViewById(R.id.btnClearAlarm);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearReminders();
            }
        });

        // Setup Sunday
        vBtnSun = findViewById(R.id.btnSunday);
        if (prefs.getBoolean("sun", false)) {
            vBtnSun.setTag(R.string.selected, 1);
            vBtnSun.setBackgroundResource(R.drawable.white_border);
            vBtnSun.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            vBtnSun.setTag(R.string.selected, 0);
            vBtnSun.setBackgroundResource(R.drawable.black_border);
            vBtnSun.setTextColor(Color.parseColor("#000000"));
        }
        vBtnSun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) vBtnSun.getTag(R.string.selected);
                if (i > 0) {
                    vBtnSun.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.black_border);
                    vBtnSun.setTextColor(Color.parseColor("#000000"));
                    btnSet.setEnabled(false);
                } else {
                    vBtnSun.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.white_border);
                    vBtnSun.setTextColor(Color.parseColor("#FFFFFF"));
                    setPrefBool("sun", true);
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }
                }
                showMessage();
            }
        });

        // Setup Monday
        vBtnMon = findViewById(R.id.btnMonday);
        if (prefs.getBoolean("mon", false)) {
            vBtnMon.setTag(R.string.selected, 1);
            vBtnMon.setBackgroundResource(R.drawable.white_border);
            vBtnMon.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            vBtnMon.setTag(R.string.selected, 0);
            vBtnMon.setBackgroundResource(R.drawable.black_border);
            vBtnMon.setTextColor(Color.parseColor("#000000"));
        }
        vBtnMon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) vBtnMon.getTag(R.string.selected);
                if (i > 0) {
                    vBtnMon.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.black_border);
                    vBtnMon.setTextColor(Color.parseColor("#000000"));
                    btnSet.setEnabled(false);
                } else {
                    vBtnMon.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.white_border);
                    vBtnMon.setTextColor(Color.parseColor("#FFFFFF"));
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }
                }
                showMessage();
            }
        });

        // Setup Tuesday
        vBtnTue = findViewById(R.id.btnTuesday);
        if (prefs.getBoolean("tue", false)) {
            vBtnTue.setTag(R.string.selected, 1);
            vBtnTue.setBackgroundResource(R.drawable.white_border);
            vBtnTue.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            vBtnTue.setTag(R.string.selected, 0);
            vBtnTue.setBackgroundResource(R.drawable.black_border);
            vBtnTue.setTextColor(Color.parseColor("#000000"));
        }
        vBtnTue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) vBtnTue.getTag(R.string.selected);
                if (i > 0) {
                    vBtnTue.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.black_border);
                    vBtnTue.setTextColor(Color.parseColor("#000000"));
                    btnSet.setEnabled(false);
                } else {
                    vBtnTue.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.white_border);
                    vBtnTue.setTextColor(Color.parseColor("#FFFFFF"));
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }
                }
                showMessage();
            }
        });

        // Setup Wednesday
        vBtnWed = findViewById(R.id.btnWednesday);
        if (prefs.getBoolean("wed", false)) {
            vBtnWed.setTag(R.string.selected, 1);
            vBtnWed.setBackgroundResource(R.drawable.white_border);
            vBtnWed.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            vBtnWed.setTag(R.string.selected, 0);
            vBtnWed.setBackgroundResource(R.drawable.black_border);
            vBtnWed.setTextColor(Color.parseColor("#000000"));
        }
        vBtnWed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) vBtnWed.getTag(R.string.selected);
                if (i > 0) {
                    vBtnWed.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.black_border);
                    vBtnWed.setTextColor(Color.parseColor("#000000"));
                    btnSet.setEnabled(false);
                } else {
                    vBtnWed.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.white_border);
                    vBtnWed.setTextColor(Color.parseColor("#FFFFFF"));
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }
                }
                showMessage();
            }
        });

        // Setup Thursday
        vBtnThu = findViewById(R.id.btnThursday);
        if (prefs.getBoolean("thu", false)) {
            vBtnThu.setTag(R.string.selected, 1);
            vBtnThu.setBackgroundResource(R.drawable.white_border);
            vBtnThu.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            vBtnThu.setTag(R.string.selected, 0);
            vBtnThu.setBackgroundResource(R.drawable.black_border);
            vBtnThu.setTextColor(Color.parseColor("#000000"));
        }
        vBtnThu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) vBtnThu.getTag(R.string.selected);
                if (i > 0) {
                    vBtnThu.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.black_border);
                    vBtnThu.setTextColor(Color.parseColor("#000000"));
                    btnSet.setEnabled(false);
                } else {
                    vBtnThu.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.white_border);
                    vBtnThu.setTextColor(Color.parseColor("#FFFFFF"));
                    setPrefBool("thu", true);
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }
                }
                showMessage();
            }
        });

        // Setup Friday
        vBtnFri = findViewById(R.id.btnFriday);
        if (prefs.getBoolean("fri", false)) {
            vBtnFri.setTag(R.string.selected, 1);
            vBtnFri.setBackgroundResource(R.drawable.white_border);
            vBtnFri.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            vBtnFri.setTag(R.string.selected, 0);
            vBtnFri.setBackgroundResource(R.drawable.black_border);
            vBtnFri.setTextColor(Color.parseColor("#000000"));
        }
        vBtnFri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) vBtnFri.getTag(R.string.selected);
                if (i > 0) {
                    vBtnFri.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.black_border);
                    vBtnFri.setTextColor(Color.parseColor("#000000"));
                    btnSet.setEnabled(false);
                } else {
                    vBtnFri.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.white_border);
                    vBtnFri.setTextColor(Color.parseColor("#FFFFFF"));
                    // add function to check interval
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }
                }
                showMessage();
            }
        });

        vBtnSat = findViewById(R.id.btnSaturday);
        if (prefs.getBoolean("sat", false)) {
            vBtnSat.setTag(R.string.selected, 1);
            vBtnSat.setBackgroundResource(R.drawable.white_border);
            vBtnSat.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            vBtnSat.setTag(R.string.selected, 0);
            vBtnSat.setBackgroundResource(R.drawable.black_border);
            vBtnSat.setTextColor(Color.parseColor("#000000"));
        }
        vBtnSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) vBtnSat.getTag(R.string.selected);
                if (i > 0) {
                    vBtnSat.setTag(R.string.selected, 0);
                    v.setBackgroundResource(R.drawable.black_border);
                    vBtnSat.setTextColor(Color.parseColor("#000000"));
                    btnSet.setEnabled(false);
                } else {
                    vBtnSat.setTag(R.string.selected, 1);
                    v.setBackgroundResource(R.drawable.white_border);
                    vBtnSat.setTextColor(Color.parseColor("#FFFFFF"));
                    setPrefBool("sat", true);
                    if(passCheck()){
                        btnSet.setEnabled(true);
                    }
                }
                showMessage();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goBack();
                comparePrefs();
            }
        });
    }

    private void goBack() {
        try {
            if (strValue.length() > 0) {
                switch (strValue) {
                    case ("heatindexactivity"):
                        startHeatIndex();
                        break;
                    case ("moreactivity"):
                        startMore();
                        break;
                    case ("firstaidactivity"):
                        startFirstAid();
                        break;
                    case ("todayactivity"):
                        startToday();
                        break;
                    case ("symptomsactivity"):
                        startSymptoms();
                        break;
                    default:
                        startHeatIndex();
                        break;
                }
            } else {
                startHeatIndex();
            }
        } catch (NumberFormatException nfe) {
            //nfe.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // Simply Do nothing!
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

    private void setTime() {
        int Start = spnrStartTimes.getSelectedItemPosition();
        int End = spnrEndTimes.getSelectedItemPosition();
        if (Start < End) {
            btn15.setEnabled(true);
            btn30.setEnabled(true);
            btn60.setEnabled(true);
        } else {
            btn15.setEnabled(false);
            btn30.setEnabled(false);
            btn60.setEnabled(false);
            btn15.setBackgroundResource(R.drawable.button_border);
            btn15.setTextColor(Color.parseColor("#000000"));
            btn30.setBackgroundResource(R.drawable.button_border);
            btn30.setTextColor(Color.parseColor("#000000"));
            btn60.setBackgroundResource(R.drawable.button_border);
            btn60.setTextColor(Color.parseColor("#000000"));
            btn15.setTag(R.string.selected, 0);
            btn30.setTag(R.string.selected, 0);
            btn60.setTag(R.string.selected, 0);
        }
        showMessage();
    }

    private void showMessage() {
        String interval, dow, sTime, eTime;
        sTime = spnrStartTimes.getSelectedItem().toString();
        eTime = spnrEndTimes.getSelectedItem().toString();
        // Interval
        interval = "";
        int i;
        int v = 0;
        i = (int) btn15.getTag(R.string.selected);
        if (i == 1) {
            interval = "15";
            v = 15;
        }
        i = (int) btn30.getTag(R.string.selected);
        if (i == 1) {
            interval = "30";
            v = 30;
        }
        i = (int) btn60.getTag(R.string.selected);
        if (i == 1) {
            interval = "60";
            v = 60;
        }
        //Day of Week
        dow = "";
        i = (int) vBtnSun.getTag(R.string.selected);
        if (i == 1) {
            dow = "Sunday";
        }

        i = (int) vBtnMon.getTag(R.string.selected);
        if (i == 1) {
            if (dow.length() > 0) {
                dow = dow + ", Monday";
            } else {
                dow = dow + "Monday";
            }
        }

        i = (int) vBtnTue.getTag(R.string.selected);
        if (i == 1) {
            if (dow.length() > 0) {
                dow = dow + ", Tuesday";
            } else {
                dow = dow + "Tuesday";
            }
        }

        i = (int) vBtnWed.getTag(R.string.selected);
        if (i == 1) {
            if (dow.length() > 0) {
                dow = dow + ", Wednesday";
            } else {
                dow = dow + "Wednesday";
            }
        }

        i = (int) vBtnThu.getTag(R.string.selected);
        if (i == 1) {
            if (dow.length() > 0) {
                dow = dow + ", Thursday";
            } else {
                dow = dow + "Thursday";
            }
        }

        i = (int) vBtnFri.getTag(R.string.selected);
        if (i == 1) {
            if (dow.length() > 0) {
                dow = dow + ", Friday";
            } else {
                dow = dow + "Friday";
            }
        }

        i = (int) vBtnSat.getTag(R.string.selected);
        if (i == 1) {
            if (dow.length() > 0) {
                dow = dow + ", Saturday";
            } else {
                dow = dow + "Saturday";
            }
        }
        // Create Summary Text
        //sb.setLength(0);

        strTextViewSummary = "";
        strSummary = "";

        // Return to this screen to clear reminders. Hydration reminders were added every
        strSummary = getResources().getString(R.string.msg_prompt_set) + " " + dow;
        // Set reminders every Sunday
        strTextViewSummary = getResources().getString(R.string.Reminders_set_text_dow) + " " + dow;
        if (interval.length() > 0) {
            // 15 minutes apart,
            strTextViewSummary = strTextViewSummary + " " + interval + " " + getResources().getString(R.string.Reminders_set_text_interval);
            strSummary = strSummary + " " + interval + " " + getResources().getString(R.string.Reminders_set_text_interval);
        }
        // from 12:00 PM to 1:00 PM
        strTextViewSummary = strTextViewSummary + " " + getResources().getString(R.string.Reminders_set_text_start) + " " + sTime + " " + getResources().getString(R.string.Reminders_set_text_end) + " " + eTime + ".";
        strSummary = strSummary + " " + getResources().getString(R.string.Reminders_set_text_start) + " " + sTime + " " + getResources().getString(R.string.Reminders_set_text_end) + " " + eTime + ".";
        tvSummary.setText(strTextViewSummary);
    }

    // Added sharedPreferences to set Time Pickers
    private void initializePickers() {
        int iStartHr = prefs.getInt("startHour", 0); //0 is the default value.
        int iEndHr = prefs.getInt("endHour", 0); //0 is the default value.
        if (iEndHr == iStartHr) {
            btn15 = findViewById(R.id.btn15);
            btn15.setEnabled(false);
            btn30 = findViewById(R.id.btn30);
            btn30.setEnabled(false);
            btn60 = findViewById(R.id.btn60);
            btn60.setEnabled(false);
        }
        // set the value for current hours
        spnrStartTimes.setSelection(iStartHr);
        spnrEndTimes.setSelection(iEndHr);

    }

    private void clearReminders(){
        AlertDialog alertDialog = new AlertDialog.Builder(RemindersActivity.this).create();
        alertDialog.setTitle(getString(R.string.msg_title_clear));
        alertDialog.setMessage(getString(R.string.msg_prompt_clear));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("startHour", 12);
                        editor.putInt("endHour", 12);
                        editor.putInt("interval", 0);
                        editor.putBoolean("sun", false); // change back
                        editor.putBoolean("mon", false);
                        editor.putBoolean("tue", false);
                        editor.putBoolean("wed", false);
                        editor.putBoolean("thu", false);
                        editor.putBoolean("fri", false);
                        editor.putBoolean("sat", false);
                        editor.putString("summary", getResources().getString(R.string.txtSummary));
                        editor.apply();
                        String sTitle = getResources().getString(R.string.txtemoji_title);
                        Uri deleteUri = null;
                        Uri eventUri;
                        eventUri = Uri.parse("content://com.android.calendar/events");
                        String projection[] = {"_id", "title"};
                        Cursor cursor = getContentResolver().query(eventUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String calName;
                            String calID;
                            int nameCol = cursor.getColumnIndex(projection[1]);
                            int idCol = cursor.getColumnIndex(projection[0]);
                            do {
                                calName = cursor.getString(nameCol);
                                calID = cursor.getString(idCol);
                                if (calName != null && calName.contains(sTitle)) {
                                    deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(String.valueOf(calID)));
                                    int rows = getContentResolver().delete(deleteUri, null, null);

                                }
                            } while (cursor.moveToNext());
                            cursor.close();
                            TelemetryProc.appLaunch(getString(R.string.msg_title_clear), getResources().getString(R.string.txtreminder), "nav");
                            clearActivity();
                        }
                    }
                });
        alertDialog.show();
    }

    boolean passCheck(){
        int i;
        int val = 0;
        Boolean b = true;

        i = (int) btn15.getTag(R.string.selected);
        if (i == 1) {
            val = 15;
        }
        i = (int) btn30.getTag(R.string.selected);
        if (i == 1) {
            val = 30;
        }
        i = (int) btn60.getTag(R.string.selected);
        if (i == 1) {
            val = 60;
        }
        // Make sure an Interval is selected if the time frame is greater than 0
        if(spnrStartTimes.getSelectedItemPosition() != spnrEndTimes.getSelectedItemPosition() && val == 0)
        {
            b = false;
        }

        int ctr = 0;
        i = (int) vBtnSun.getTag(R.string.selected);
        if (i == 1) {
            ctr = ctr + 1;
        }
        i = (int) vBtnMon.getTag(R.string.selected);
        if (i == 1) {
            ctr = ctr + 1;
        }
        i = (int) vBtnTue.getTag(R.string.selected);
        if (i == 1) {
            ctr = ctr + 1;
        }
        i = (int) vBtnWed.getTag(R.string.selected);
        if (i == 1) {
            ctr = ctr + 1;
        }
        i = (int) vBtnThu.getTag(R.string.selected);
        if (i == 1) {
            ctr = ctr + 1;
        }
        i = (int) vBtnFri.getTag(R.string.selected);
        if (i == 1) {
            ctr = ctr + 1;
        }
        i = (int) vBtnSat.getTag(R.string.selected);
        if (i == 1) {
            ctr = ctr + 1;
        }
        if (ctr == 0)
        {
            b = false;
        }
        return b;
    }

    private void setCalEvents() {
        int val = 0;
        int i, alarmInterval;

        int startHour = spnrStartTimes.getSelectedItemPosition();
        int endHour = spnrEndTimes.getSelectedItemPosition();

        i = (int) btn15.getTag(R.string.selected);
        if (i == 1) {
            val = 15;
        }
        i = (int) btn30.getTag(R.string.selected);
        if (i == 1) {
            val = 30;
        }
        i = (int) btn60.getTag(R.string.selected);
        if (i == 1) {
            val = 60;
        }

        setPrefInt("interval", val);
        setPrefInt("startHour", startHour);
        setPrefInt("endHour", endHour);
        setPrefString("summary", strTextViewSummary);
        alarmInterval = val;

        // Day of Week - Create an array of Days to be scheduled
        int ctr = 0;
        i = (int) vBtnSun.getTag(R.string.selected);
        if (i == 1) {
            setPrefBool("sun", true);
            ctr = ctr + 1;
        } else {
            setPrefBool("sun", false);
        }

        i = (int) vBtnMon.getTag(R.string.selected);
        if (i == 1) {
            setPrefBool("mon", true);
            ctr = ctr + 1;
        }else{
            setPrefBool("mon", false);
        }

        i = (int) vBtnTue.getTag(R.string.selected);
        if (i == 1) {
            setPrefBool("tue", true);
            ctr = ctr + 1;
        }else{
            setPrefBool("tue", false);
        }

        // WCS Maybe
        i = (int) vBtnWed.getTag(R.string.selected);
        if (i == 1) {
            setPrefBool("wed", true);
            ctr = ctr + 1;
        }else{
            setPrefBool("wed", false);
        }

        i = (int) vBtnThu.getTag(R.string.selected);
        if (i == 1) {
            setPrefBool("thu", true);
            ctr = ctr + 1;
        }else{
            setPrefBool("thu", false);
        }

        i = (int) vBtnFri.getTag(R.string.selected);
        if (i == 1) {
            setPrefBool("fri", true);
            ctr = ctr + 1;
        }else{
            setPrefBool("fri", false);
        }

        i = (int) vBtnSat.getTag(R.string.selected);
        if (i == 1) {
            setPrefBool("sat", true);
            ctr = ctr + 1;
        }else{
            setPrefBool("sat", false);
        }

        String[] days = new String[ctr];
        int ctr2 = 0;
        i = (int) vBtnSun.getTag(R.string.selected);
        if (i == 1) {
            days[ctr2] = "SU";
            ctr2 = ctr2 + 1;
        }
        i = (int) vBtnMon.getTag(R.string.selected);
        if (i == 1) {
            days[ctr2] = "MO";
            ctr2 = ctr2 + 1;
        }
        i = (int) vBtnTue.getTag(R.string.selected);
        if (i == 1) {
            days[ctr2] = "TU";
            ctr2 = ctr2 + 1;
        }
        i = (int) vBtnWed.getTag(R.string.selected);
        if (i == 1) {
            days[ctr2] = "WE";
            ctr2 = ctr2 + 1;
        }
        i = (int) vBtnThu.getTag(R.string.selected);
        if (i == 1) {
            days[ctr2] = "TH";
            ctr2 = ctr2 + 1;
        }
        i = (int) vBtnFri.getTag(R.string.selected);
        if (i == 1) {
            days[ctr2] = "FR";
            ctr2 = ctr2 + 1;
        }
        i = (int) vBtnSat.getTag(R.string.selected);
        if (i == 1) {
            days[ctr2] = "SA";
            ctr2 = ctr2 + 1;
        }

        if(ctr > 0) {

            String sTitle = getResources().getString(R.string.txtemoji_title);
            Uri deleteUri = null;
            Uri eventUri;
            eventUri = Uri.parse("content://com.android.calendar/events");

            String projection[] = {"_id", "title"};
            Cursor cursor = getContentResolver().query(eventUri, null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String calName;
                    String calID;
                    int nameCol = cursor.getColumnIndex(projection[1]);
                    int idCol = cursor.getColumnIndex(projection[0]);
                    do {
                        calName = cursor.getString(nameCol);
                        calID = cursor.getString(idCol);
                        if (calName != null && calName.contains(sTitle)) {
                            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(String.valueOf(calID)));
                            int rows = getContentResolver().delete(deleteUri, null, null);
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            }


            addReminderInCalendar(startHour, endHour, alarmInterval, days);
        }else
            {
                //do nothing
            }
    }

    private void addReminderInCalendar(int startHour, int endHour, int intrvl, String[] days) {
        int dur = 0;
        int interval = 0;
        String startTime = "";
        switch (startHour) {
            case 0:
                startTime = "12:00 AM";
                break;
            case 1:
                startTime = "01:00 AM";
                break;
            case 2:
                startTime = "02:00 AM";
                break;
            case 3:
                startTime = "03:00 AM";
                break;
            case 4:
                startTime = "04:00 AM";
                break;
            case 5:
                startTime = "05:00 AM";
                break;
            case 6:
                startTime = "06:00 AM";
                break;
            case 7:
                startTime = "07:00 AM";
                break;
            case 8:
                startTime = "08:00 AM";
                break;
            case 9:
                startTime = "09:00 AM";
                break;
            case 10:
                startTime = "10:00 AM";
                break;
            case 11:
                startTime = "11:00 AM";
                break;
            case 12:
                startTime = "12:00 PM";
                break;
            case 13:
                startTime = "01:00 PM";
                break;
            case 14:
                startTime = "02:00 PM";
                break;
            case 15:
                startTime = "03:00 PM";
                break;
            case 16:
                startTime = "04:00 PM";
                break;
            case 17:
                startTime = "05:00 PM";
                break;
            case 18:
                startTime = "06:00 PM";
                break;
            case 19:
                startTime = "07:00 PM";
                break;
            case 20:
                startTime = "08:00 PM";
                break;
            case 21:
                startTime = "09:00 PM";
                break;
            case 22:
                startTime = "10:00 PM";
                break;
            case 23:
                startTime = "11:00 PM";
                break;
            default:
                break;
        }

        int smin = startHour * 60;
        int emin = endHour * 60;
        dur = emin - smin;
        if (dur > 0)
        {
            interval = dur / intrvl;
        }

        int iHr;
        int iMin;
        Calendar instance = Calendar.getInstance();
        try
        {
            instance.setTime(new SimpleDateFormat("hh:mm a").parse(startTime));
            iHr = instance.get(Calendar.HOUR_OF_DAY);
            iMin = instance.get(Calendar.MINUTE);
            // Set reminder on the start time hour
            setEvent(iHr, iMin, days);
        } catch (ParseException e)
        {
           // e.printStackTrace();
        }
        int i=1;
        while(i++ <= interval){
            instance.add(Calendar.MINUTE, intrvl);
            iHr = instance.get(Calendar.HOUR_OF_DAY);
            iMin = instance.get(Calendar.MINUTE);
            // Set reminder for each interval
            setEvent(iHr, iMin, days);
        }
    }

    private void setPrefBool(String prefName, Boolean isBool){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(prefName, isBool);
        editor.apply();
    }

    private void setPrefInt(String prefName, int ival){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(prefName, ival);
        editor.apply();
    }

    private void setPrefString(String prefName, String sval){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefName, sval);
        editor.apply();
    }

    public void setEvent(int hour, int min, String[] days ){
        // Set variables
        long startMillis = 0;
        long endMillis = 0;
        int hr = hour;
        int m = min;
        // Set Calendar Event Times
        Calendar cal = Calendar.getInstance();
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hour, min);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hour, min);
        endMillis = endTime.getTimeInMillis();
        Uri EVENTS_URI = Uri.parse(getCalendarUriBase(true) + "events");
        ContentResolver cr = getContentResolver();
        TimeZone timeZone = TimeZone.getDefault();

        // Create RRULE string for re-occurrence
        // Set BYDAY parameter
        String byDay = "BYDAY=";
        int size = days.length;
        for (int i = 0; i < size; i++) {
            if(i < size - 1)
            {
                byDay = byDay + days[i] + ",";
            }else {
                byDay = byDay + days[i];
            }
        }

        // Set WKST parameter from current day: SU,MO,TU,WE,TH,FR,SA
        String wkST = "";
        String sDOW = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

        prefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        switch (sDOW) {
            case "Sunday":
                wkST = "WKST=SU";
                //editor.putBoolean("sun", true);
                break;
            case "Monday":
                wkST = "WKST=MO";
                //editor.putBoolean("mon", true);
                break;
            case "Tuesday":
                wkST = "WKST=TU";
                //editor.putBoolean("tue", true);
                break;
            case "Wednesday":
                wkST = "WKST=WE";
                // WCS Maybe
                //editor.putBoolean("wed", true);
                break;
            case "Thursday":
                wkST = "WKST=TH";
                //editor.putBoolean("thu", true);
                break;
            case "Friday":
                wkST = "WKST=FR";
                //editor.putBoolean("fri", true);
                break;
            case "Saturday":
                wkST = "WKST=SA";
                //editor.putBoolean("sat", true);
                break;
            default:
                break;
        }
        editor.apply();

        // set UNTIL date for 1 month from today
        String unTIL, yr, mnth, day = "";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        Date dt = calendar.getTime();
        yr = (String) DateFormat.format("yyyy", dt); // 2013
        mnth = (String) DateFormat.format("MM",   dt); // 06
        day = (String) DateFormat.format("dd", dt); // 20
        unTIL = "UNTIL=" + yr + mnth + day;

        // Create RRULE String
        String recurString ="FREQ=WEEKLY;" + unTIL + ";" + wkST;
        if(byDay != "BYDAY=")
        {
            recurString ="FREQ=WEEKLY;" + unTIL + ";" + wkST + ";" + byDay;
        }

        // Insert an event in calendar
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.TITLE, getResources().getString(R.string.txtemoji_title));
        //values.put(CalendarContract.Events.DESCRIPTION, "Be sure to drink at least 4.oz of cool water.");
        values.put(CalendarContract.Events.ALL_DAY, 0);
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        values.put(CalendarContract.Events.RRULE, recurString);
        Uri event = cr.insert(EVENTS_URI, values);
        // Add reminder for event added
        Uri REMINDERS_URI = Uri.parse(getCalendarUriBase(true) + "reminders");
        values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, Long.parseLong(event != null ? event.getLastPathSegment() : null));
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        values.put(CalendarContract.Reminders.MINUTES, 0);
        cr.insert(REMINDERS_URI, values);
        //Toast.makeText(getApplicationContext(), R.string.msg_prompt_set, Toast.LENGTH_SHORT).show();
        //WCS Added
        AlertDialog alertDialog = new AlertDialog.Builder(RemindersActivity.this).create();
        alertDialog.setTitle(getString(R.string.msg_title_set));
        alertDialog.setMessage(strSummary);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        TelemetryProc.appLaunch(getString(R.string.msg_title_set), getResources().getString(R.string.txtreminder), "nav");

                        // Set all preferences here


                        clearActivity();

                    }
                });
        alertDialog.show();
        // removed this line to add dialogue
        // clearActivity();
    }

    private void clearActivity(){
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    // Returns Calendar Base URI, supports both new and old OS
    private String getCalendarUriBase(boolean eventUri) {
        Uri calendarURI = null;
        try {
            calendarURI = (eventUri) ? Uri.parse("content://com.android.calendar/") : Uri.parse("content://com.android.calendar/calendars");
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return calendarURI != null ? calendarURI.toString() : null;
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

    private void comparePrefs() {
        AlertDialog.Builder buildMsg = new AlertDialog.Builder(this);

        Boolean hrsChanged = false;
        Boolean intrvlChanged = false;
        Boolean daysChanged = false;
        Boolean noIntSelected = false;


        // Check Times for Changes
        if(spnrStartTimes.getSelectedItemPosition() != prefs.getInt("startHour", 0)){
            hrsChanged = true;
        }
        if(spnrEndTimes.getSelectedItemPosition() != prefs.getInt("endHour", 0)){
            hrsChanged = true;
        }

        // Check Intervals for Changes
        int i = (int) btn15.getTag(R.string.selected);
        int val = 0;
        if (i == 1) {
            val = 15;
        }
        i = (int) btn30.getTag(R.string.selected);
        if (i == 1) {
            val = 30;
        }
        i = (int) btn60.getTag(R.string.selected);
        if (i == 1) {
            val = 60;
        }
        if(val != prefs.getInt("interval", 0))
        {
            intrvlChanged = true;
        }

        // Make sure an Interval is selected if the time frame is greater than 0
        if(spnrStartTimes.getSelectedItemPosition() != spnrEndTimes.getSelectedItemPosition() && val == 0)
        {
            noIntSelected = true;
        }

        // Check Days for Changes
        int sun = (int) vBtnSun.getTag(R.string.selected);
        Boolean bSun = false;
        if(sun == 1){
            bSun = true;
        }
        if (bSun != prefs.getBoolean("sun", false) ) {
            daysChanged = true;
        }

        int mon = (int) vBtnMon.getTag(R.string.selected);
        Boolean bMon = false;
        if (mon == 1) {
            bMon = true;
        }
        if (bMon != prefs.getBoolean("mon", false) ) {
            daysChanged = true;
        }

        int tue = (int) vBtnTue.getTag(R.string.selected);
        Boolean bTue = false;
        if (tue == 1) {
            bTue = true;
        }
        if (bTue != prefs.getBoolean("tue", false) ){
            daysChanged = true;
        }

        int wed = (int) vBtnWed.getTag(R.string.selected);
        Boolean bWed = false;
        if (wed == 1) {
            bWed = true;
        }
        if (bWed != prefs.getBoolean("wed", false) ){
            daysChanged = true;
        }

        int thu = (int) vBtnThu.getTag(R.string.selected);
        Boolean bThu = false;
        if (thu == 1) {
            bThu = true;
        }
        if (bThu != prefs.getBoolean("thu", false) ){
            daysChanged = true;
        }

        int fri = (int) vBtnFri.getTag(R.string.selected);
        Boolean bFri = false;
        if (fri == 1) {
            bFri = true;
        }
        if (bFri != prefs.getBoolean("fri", false) ){
            daysChanged = true;
        }

        int sat = (int) vBtnSat.getTag(R.string.selected);
        Boolean bSat = false;
        if (sat == 1) {
            bSat = true;
        }
        if (bSat != prefs.getBoolean("sat", false) ){
            daysChanged = true;
        }

        if(daysChanged && !noIntSelected) {
            buildMsg.setTitle(getString(R.string.alert_changed_msg_title));
            buildMsg.setMessage(getString(R.string.alert_changed_msg_text));
            buildMsg.setCancelable(true);
            buildMsg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    setCalEvents();
                }
            });
            buildMsg.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    goBack();
                }
            });

            AlertDialog alert = buildMsg.create();
            alert.show();
        }
        else if(hrsChanged || intrvlChanged || noIntSelected)
        {
            buildMsg.setTitle(getString(R.string.alert_changed_msg_title));
            buildMsg.setMessage(getString(R.string.alert_unfinished_msg_text));
            buildMsg.setCancelable(true);
            buildMsg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    //setCalEvents();
                }
            });
            buildMsg.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    goBack();
                }
            });

            AlertDialog alert = buildMsg.create();
            alert.show();
        }
        else{
            goBack();
        }
    }

    private void showPrefs() {
        prefs =  getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        int startHour = prefs.getInt("startHour", 0); //0 is the default value.
        int endHour = prefs.getInt("endHour", 0); //0 is the default value.
        int interval = prefs.getInt("interval", 0); //0 is the default value.
        boolean sun = prefs.getBoolean("sun", false);  // getting boolean
        boolean mon = prefs.getBoolean("mon", false);  // getting boolean
        boolean tue = prefs.getBoolean("tue", false);  // getting boolean
        boolean wed = prefs.getBoolean("wed", false);  // getting boolean
        boolean thu = prefs.getBoolean("thu", false);  // getting boolean
        boolean fri = prefs.getBoolean("fri", false);  // getting boolean
        boolean sat = prefs.getBoolean("sat", false);  // getting boolean
        boolean alreadyInitialized = prefs.getBoolean("alreadyInitialized", false);  // getting boolean
        String summary = prefs.getString("summary", "");
        String str = "startHour = " + startHour + "; endHour = " + endHour + "; Interval = " + interval + "; sun = " + sun + "; mon = " + mon + "; tue = " + tue + "; wed = " + wed + "; thu = " + thu + "; fri = " + fri + "; sat = " + sat + "; Summary = " + summary;

    }
}
