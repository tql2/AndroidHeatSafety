package erg.com.nioshheatindex;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Locale;

class CustomSwipeAdapter extends PagerAdapter {

    private final Context ctx;
    private boolean spanish = false;

    public CustomSwipeAdapter(Context ctx)
    {
        getLocale();
        this.ctx = ctx;

    }

    @Override
    public int getCount() {
        return 4; //image_resources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return (view== o);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        ImageView imageView = (ImageView)item_view.findViewById(R.id.image_view);
        if(spanish)
        {
            int[] image_resources = {R.drawable.slide1_es, R.drawable.slide2_es, R.drawable.slide3_es, R.drawable.slide4_es};
            imageView.setImageResource(image_resources[position]);
        }
        else {
            int[] image_resources = {R.drawable.slide1, R.drawable.slide2, R.drawable.slide3, R.drawable.slide4};
            imageView.setImageResource(image_resources[position]);
        }
        container.addView(item_view);
        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }

    private void getLocale() {
        spanish = !Locale.getDefault().getLanguage().equalsIgnoreCase("en");
    }
}
