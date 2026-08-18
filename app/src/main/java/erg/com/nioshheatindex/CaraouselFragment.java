package erg.com.nioshheatindex;

import android.os.Build;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

public class CaraouselFragment extends Fragment {

    private boolean spanish = false;

    public static Fragment newInstance(TodayActivity context, int pos, float scale, String hour, String hi, int rl) {
        Bundle b = new Bundle();
        b.putInt("pos", pos);
        b.putFloat("scale", scale);
        b.putString("hour", hour);
        b.putString("hi", hi);
        b.putInt("rl", rl);
        return Fragment.instantiate(context, CaraouselFragment.class.getName(), b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        if (container == null) {
            return null;
        }

        //get the Locale of the Operating System to check if Spanish or English
        getLocale();
       //// LinearLayout l = (LinearLayout) inflater.inflate(R.layout.mf, container, false);
        ConstraintLayout l = (ConstraintLayout)inflater.inflate(R.layout.mf, container, false);
        int pos = this.getArguments().getInt("pos");
        String hour = this.getArguments().getString("hour");
        String hi = this.getArguments().getString("hi");
        int risklevel = this.getArguments().getInt("rl");
        TextView tv = l.findViewById(R.id.text);
        boolean cels = TodayActivity.useCelsius;

        if(hour == "")
        {
            if (spanish) {
                tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.nodatacard_es));
            }
            else
            {
                tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.nodatacard));
            }
        }
        else
        {
            String htmlString = "";
            if(cels)
            {
                double strC = Double.parseDouble(hi);
                //58.3 C
                if(strC > 58.3) {
                //if(strC > 26.7) {
                    //htmlString = "<html><small><b>" + hour + "</b></small><br/><br/><big><b>" + getResources().getString(R.string.greaterthan) + hi + getResources().getString(R.string.celsiussymbol) + "</b></big><br/><br/></html>";
                    htmlString = "<html><small><b>" + hour + "</b></small><br/><br/><big><b>" + getResources().getString(R.string.greaterthan) + getResources().getString(R.string.fiftyeightthree) + getResources().getString(R.string.celsiussymbol) + "</b></big><br/><br/></html>";
                }else{
                    htmlString = "<html><small><b>" + hour + "</b></small><br/><br/><big><b>" + hi + getResources().getString(R.string.celsiussymbol) + "</b></big><br/><br/></html>";
                }
            }else{
                int converted = Integer.parseInt(hi);
                if (converted >= 137){
                //if (converted > 80){
                    //htmlString = "<html><small><b>" + hour + "</b></small><br/><br/><big><b>" + getResources().getString(R.string.greaterthan) + hi + getResources().getString(R.string.fahrenheitsymbol) + "</b></big><br/><br/></html>";
                    htmlString = "<html><small><b>" + hour + "</b></small><br/><br/><big><b>" + getResources().getString(R.string.greaterthan) + getResources().getString(R.string.onethirtyseven) + getResources().getString(R.string.fahrenheitsymbol) + "</b></big><br/><br/></html>";
                }else{
                    htmlString = "<html><small><b>" + hour + "</b></small><br/><br/><big><b>" + hi + getResources().getString(R.string.fahrenheitsymbol) + "</b></big><br/><br/></html>";
                }
            }
            tv.setText(fromHtml(htmlString));
            if (spanish) {
                switch (risklevel) {
                    case 0:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.minimalcard_es));
                        break;
                    case 1:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.minimalcard_es));
                        break;
                    case 2:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.caution_es));
                        break;
                    case 3:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.warning_es));
                        break;
                    case 4:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.danger_es));
                        break;
                    default:
                        break;
                }
            } else {
                switch (risklevel) {
                    case 0:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.minimalcard));
                        break;
                    case 1:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.minimalcard));
                        break;
                    case 2:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.caution));
                        break;
                    case 3:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.warning));
                        break;
                    case 4:
                        tv.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.danger));
                        break;
                    default:
                        break;
                }
            }
        }
        CarouselLinearLayout root = l.findViewById(R.id.root);
        float scale = this.getArguments().getFloat("scale");
        root.setScaleBoth(scale);
        return l;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    private void getLocale() {
        spanish = !Locale.getDefault().getLanguage().equalsIgnoreCase("en");
    }

}

