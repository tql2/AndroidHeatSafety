package erg.com.nioshheatindex;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class GaugeView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF gaugeBounds = new RectF();

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
        drawModernGauge(canvas);
    }

    private void drawModernGauge(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        canvas.drawColor(Color.TRANSPARENT);
        float scale = Math.min(width / 360f, height / 220f);
        float centerX = width / 2f;
        float centerY = height * 0.76f;
        float radius = Math.min(width * 0.36f, height * 0.62f);
        float stroke = Math.max(18f, 26f * scale);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(2, 2, width - 2, height - 2), 24f * scale, 24f * scale, paint);

        gaugeBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(stroke);

        drawArc(canvas, 180f, 45f, Color.rgb(22, 123, 164));
        drawArc(canvas, 225f, 45f, Color.rgb(246, 196, 68));
        drawArc(canvas, 270f, 45f, Color.rgb(239, 135, 51));
        drawArc(canvas, 315f, 45f, Color.rgb(202, 54, 57));

        double safeNeedle = Math.max(0d, Math.min(1d, mNeedle));
        double angle = Math.toRadians(180d + safeNeedle * 180d);
        float needleX = centerX + (float) Math.cos(angle) * (radius - stroke * 0.8f);
        float needleY = centerY + (float) Math.sin(angle) * (radius - stroke * 0.8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(3f, 5f * scale));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.rgb(23, 50, 58));
        canvas.drawLine(centerX, centerY, needleX, needleY, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, Math.max(8f, 10f * scale), paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, Math.max(3f, 4f * scale), paint);

        float textSize = Math.max(14f, 17f * scale);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans", Typeface.BOLD));
        paint.setTextSize(textSize);
        paint.setColor(Color.rgb(23, 50, 58));
        String feelsLike = mFeelsLikeTemp == null || mFeelsLikeTemp.trim().isEmpty() ? "--" : mFeelsLikeTemp;
        canvas.drawText(feelsLike, centerX, centerY - 18f * scale, paint);
        paint.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        paint.setTextSize(Math.max(11f, 12f * scale));
        paint.setColor(Color.rgb(83, 105, 112));
        canvas.drawText(mSpanish ? "Sensación térmica" : "Feels like", centerX, centerY + 2f * scale, paint);
        if (mTime != null && !mTime.trim().isEmpty() && !"--".equals(mTime)) {
            canvas.drawText((mSpanish ? "A las " : "At ") + mTime, centerX, centerY + 20f * scale, paint);
        }

        paint.setTextSize(Math.max(10f, 11f * scale));
        paint.setColor(Color.rgb(83, 105, 112));
        canvas.drawText(mSpanish ? "Mínimo" : "Low", centerX - radius + 12f * scale, centerY + 28f * scale, paint);
        canvas.drawText(mSpanish ? "Extremo" : "Extreme", centerX + radius - 18f * scale, centerY + 28f * scale, paint);
    }

    private void drawArc(Canvas canvas, float start, float sweep, int color) {
        paint.setColor(color);
        canvas.drawArc(gaugeBounds, start, sweep, false, paint);
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
