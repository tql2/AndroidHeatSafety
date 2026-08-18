package erg.com.nioshheatindex;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CarouselPagerAdapter  extends FragmentPagerAdapter implements ViewPager.PageTransformer  {

    //public final static float BIG_SCALE = 1.0f; // S8+, S9
    public final static float BIG_SCALE = 0.87f; // S20+
    private final static float SMALL_SCALE = 0.7f; // S8+, S9
    //private final static float SMALL_SCALE = 0.68f; // S20+
    private final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;
    private final TodayActivity context;

    public CarouselPagerAdapter(TodayActivity context, FragmentManager fm) {
        super(fm);
        FragmentManager fm1 = fm;
        this.context = context;
  }

    @Override
    public Fragment getItem(int position){
        String hour = "12 PM"; //hour
        String hi = "92"; //heat index
        String hiCelsius = "0.0";
        boolean cels = TodayActivity.useCelsius;
        int r = 0;
        float scale;

        //array list to hold HeatIndexActivity.todayStringArray
        List<String> a1 = null;

        // make the first pager bigger than others
        if (position == TodayActivity.FIRST_PAGE) {
            scale = BIG_SCALE;
        }
        else
        {
            scale = SMALL_SCALE;
        }
        position = position % TodayActivity.PAGES;

        if(HeatIndexActivity.isManual || !TodayActivity.onLine && !isCacheEnabled())
        {
            return CaraouselFragment.newInstance(context, 0, scale, "", "", 0);
        }
        else
        {
            a1 = HeatIndexActivity.todayStringArray;
            if(a1.size() > 0)
            {
                String string = a1.get(position);
                String[] parts = string.split(",");
                String time = parts[0]; //hour
                hi = parts[1]; //heat index
                hi = hi.replace(".0", "");


                if(cels)
                {
                    hiCelsius = getCelsius(hi);
                }

                Date parsed = null;
                try
                {
                    SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    parsed = sourceFormat.parse(time);
                    SimpleDateFormat destFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    hour = destFormat.format(parsed);
                    //hour = "7:15 am";
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                int h = Integer.valueOf(hi);

                if (h < 60)
                {
                    r = 1; // Minimal
                }
                else if (h < 80)
                {
                    r = 2; // Caution
                }
                else if (h < 95)
                {
                    r = 3; // Warning
                }
                else {
                    r = 4; // Danger
                }
            }
            else
            {
                return CaraouselFragment.newInstance(context, position, scale, "", "", 0);
            }
            if(!cels)
            {
                return CaraouselFragment.newInstance(context, position, scale, hour, hi, r);
            }
            else
            {
                return CaraouselFragment.newInstance(context, position, scale, hour, hiCelsius, r);
            }
        }
    }

    @Override
    public int getCount(){
        return TodayActivity.PAGES * TodayActivity.LOOPS;
    }

    @Override
    public void transformPage(View page, float position) {
        CarouselLinearLayout myLinearLayout = page.findViewById(R.id.root);

        float scale = BIG_SCALE;
        //float scale = SMALL_SCALE;
        if (position > 0) {
            scale = scale - position * DIFF_SCALE;
        } else {
            scale = scale + position * DIFF_SCALE;
        }
        if (scale < 0) scale = 0;
        myLinearLayout.setScaleBoth(scale);
    }

    public String getCelsius(String s) {
        double dblFahrenheit = 0;
        double dblConvertedTemp = 0;
        DecimalFormat dfTenth = new DecimalFormat("#.#");
        String str = null;
        //EditText etTemp = (EditText) findViewById(R.id.temp);
        // TextView tvCelsius = (TextView) findViewById(R.id.tvCelsiusValue);
        //String strFah = etTemp.getText().toString();
        if (!s.isEmpty()){
            dblFahrenheit = Double.parseDouble(s);
            if (dblFahrenheit <= 212)
            {
                dblConvertedTemp = (5.0/9.0) * (dblFahrenheit - 32);

                str = dfTenth.format(dblConvertedTemp);
                //tvCelsius.setText (dfTenth.format(dblConvertedTemp));
            }
        }
        return str;
    }

    public boolean isCacheEnabled(){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sSetting = mPrefs.getBoolean("cache_switch", false);
        boolean b = true;
        if(!sSetting)
        {
            b = false;
        }
        return b;
    }
    }

