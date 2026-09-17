package corecloud.xtc.share;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class QuantumIndicatorView extends View {

    private int dotCount = 6;

    private float maxW;
    private float minW;
    private float dotHeight;
    private float dotMargin;
    private float radius;

    private Paint bgPaint;
    private Paint sliderPaint;
    private RectF rectF;

    private float flowPosition = 0f;
    private SpringAnimation flowSpring;

    private static final FloatPropertyCompat<QuantumIndicatorView> FLOW_POSITION =
            new FloatPropertyCompat<QuantumIndicatorView>("flowPosition") {
                @Override
                public float getValue(QuantumIndicatorView object) {
                    return object.flowPosition;
                }
                @Override
                public void setValue(QuantumIndicatorView object, float value) {
                    object.flowPosition = value;
                    object.invalidate();
                }
            };

    public QuantumIndicatorView(Context context) {
        super(context);
        initValues();
    }

    public QuantumIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initValues();
    }

    public QuantumIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValues();
    }

    private void initValues() {
        float density = getResources().getDisplayMetrics().density;
        maxW = 16f * density;
        minW = 5f * density;
        dotHeight = 5f * density;
        dotMargin = 8f * density;
        radius = dotHeight / 2f;

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0x33FFFFFF);
        bgPaint.setStyle(Paint.Style.FILL);

        sliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sliderPaint.setColor(0xFFFFFFFF);
        sliderPaint.setStyle(Paint.Style.FILL);

        rectF = new RectF();

        flowSpring = new SpringAnimation(this, FLOW_POSITION);
        SpringForce force = new SpringForce();
        force.setDampingRatio(0.6f);
        force.setStiffness(400f);
        flowSpring.setSpring(force);
    }

    public void setDotCount(int count) {
        this.dotCount = count;
        requestLayout();
    }

    public void onPageScrolled(int position, float positionOffset) {
        if (getWidth() == 0 || dotCount <= 0) return;

        if (flowSpring.isRunning()) {
            flowSpring.cancel();
        }

        this.flowPosition = position + positionOffset;
        invalidate();
    }

    public void onPageSelected(int position) {
        if (getWidth() == 0) return;
        flowSpring.animateToFinalPosition(position);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) (dotHeight + getPaddingTop() + getPaddingBottom());
        float totalWidth = (minW * (dotCount - 1)) + (dotMargin * (dotCount - 1)) + maxW;
        int width = (int) (totalWidth + getPaddingLeft() + getPaddingRight());
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dotCount <= 0) return;

        float top = (getHeight() - dotHeight) / 2f;
        float bottom = top + dotHeight;

        int currentIntPos = (int) flowPosition;
        float fraction = flowPosition - currentIntPos;

        float totalWidth = (minW * (dotCount - 1)) + (dotMargin * (dotCount - 1)) + maxW;
        float startX = (getWidth() - totalWidth) / 2f;

        float[] dynamicWidths = new float[dotCount];
        for (int i = 0; i < dotCount; i++) {
            dynamicWidths[i] = minW;
        }

        if (currentIntPos < dotCount - 1) {
            dynamicWidths[currentIntPos] = minW + (maxW - minW) * (1f - fraction);
            dynamicWidths[currentIntPos + 1] = minW + (maxW - minW) * fraction;
        } else {
            dynamicWidths[dotCount - 1] = maxW;
        }

        float activeLeft = 0f;
        float activeRight = 0f;

        if (currentIntPos < dotCount - 1) {
            float originLeft = startX + currentIntPos * (minW + dotMargin);
            float originRight = originLeft + maxW;

            float targetLeft = startX + (currentIntPos + 1) * (minW + dotMargin);
            float targetRight = targetLeft + maxW;

            if (fraction < 0.5f) {
                float localFraction = fraction / 0.5f;
                activeLeft = originLeft;
                activeRight = originRight + (targetRight - originRight) * localFraction;
            } else {
                float localFraction = (fraction - 0.5f) / 0.5f;
                activeLeft = originLeft + (targetLeft - originLeft) * localFraction;
                activeRight = targetRight;
            }
        } else {
            activeLeft = startX + (dotCount - 1) * (minW + dotMargin);
            activeRight = activeLeft + maxW;
        }

        float currentX = startX;
        for (int i = 0; i < dotCount; i++) {
            float w = dynamicWidths[i];
            rectF.set(currentX, top, currentX + w, bottom);
            canvas.drawRoundRect(rectF, radius, radius, bgPaint);
            currentX += w + dotMargin;
        }

        if (activeRight > activeLeft) {
            rectF.set(activeLeft, top, activeRight, bottom);
            canvas.drawRoundRect(rectF, radius, radius, sliderPaint);
        }
    }
}