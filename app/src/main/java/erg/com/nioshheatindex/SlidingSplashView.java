package erg.com.nioshheatindex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import static android.content.Context.ACCESSIBILITY_SERVICE;

/**
 * Created by mmv4 on 11/15/2017.
 */

public class SlidingSplashView extends FrameLayout
{
    private String strWords;
    private OnSetImageListener mOnSetImageListener;
    ViewPager.OnPageChangeListener mOnPageChangeListener;

    public SlidingSplashView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public SlidingSplashView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @SuppressLint("Range")
    private void init(Context context, AttributeSet attrs){
        LayoutInflater.from(context).inflate(R.layout.sliding_splash_view,this);
        ViewPager mViewPager = findViewById(R.id.pager_splash);
        ImageViewPagerAdapter mViewPagerAdapter = new ImageViewPagerAdapter(context, mOnSetImageListener);
        if(!isInEditMode())
            if(attrs != null){
                TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingSplashView, 0,0);
                int id = typedArray.getResourceId(R.styleable.SlidingSplashView_imageResources,0);
                if(id != 0){
                    TypedArray typed = context.getResources().obtainTypedArray(id);
                    int[] drawables = new int[typed.length()];
                    for(int i = 0 ; i < drawables.length ; ++i){
                        drawables[i] = typed.getResourceId(i,0);
                    }
                    typed.recycle();
                    mViewPagerAdapter.setImageResources(drawables);
                }
                typedArray.recycle();
            }
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position)
            {
                switch (position)
                {
                    case 0:
                        strWords = getResources().getString(R.string.txttutorialpage1);
                        break;
                    case 1:
                        strWords = getResources().getString(R.string.txttutorialpage2);
                        break;
                    case 2:
                        strWords = getResources().getString(R.string.txttutorialpage3);
                        break;
                    case 3:
                        strWords = getResources().getString(R.string.txttutorialpage4);
                        break;
                    default:
                        break;
                }
                addNotification(strWords);
           }

           @Override
           public void onPageScrollStateChanged(int state) {

           }
       });

        TabLayout tabLayout = findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager,true);
    }

    private void addNotification(String STR) {
        AccessibilityManager am = (AccessibilityManager) this.getContext().getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        if(isAccessibilityEnabled) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this.getContext(), STR, duration);
            toast.show();
        }
    }
}
