package erg.com.nioshheatindex;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class GaugeView extends View {

    //WCS private float mNeedle;
    private double mNeedle;
    private String mFeelsLikeTemp;
    private boolean mSpanish;
    private String mTime;

    public GaugeView(Context context) {
        super(context);
        init(null, 0);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
        mNeedle = a.getFloat(R.styleable.GaugeView_needle, 0);
        mFeelsLikeTemp = a.getString(R.styleable.GaugeView_feelsLikeTemp);
        mSpanish = a.getBoolean(R.styleable.GaugeView_spanish, false);
        mTime = a.getString(R.styleable.GaugeView_time);
        a.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        StyleKitESDark2.drawHeatIndexESDark(canvas, getContext(),new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), StyleKitESDark2.ResizingBehavior.AspectFit, mNeedle, mFeelsLikeTemp, mSpanish, mTime, false);
    }

    public void setmNeedle(double needle) {
      mNeedle = needle;
      invalidate();
    }

    public void setmFeelsLikeTemp(String feelsLikeTemp) {
        mFeelsLikeTemp = feelsLikeTemp;
        invalidate();
    }

    public void setmSpanish(boolean spanish) {
        mSpanish = spanish;
        invalidate();
    }

    public void setmTime(String time) {
        mTime = time;
        invalidate();
    }


}
