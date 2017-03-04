package talktranslator.app.ivanasen.talktranslator.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

/**
 * Created by ivan on 2/14/2017.
 */
public class PulsatingButton extends android.support.v7.widget.AppCompatButton {

    private static final int VOICE_INPUT_COLOR_ALPHA = 100;
    private static final float CORNER_RADIUS = 10;
    private static final float MAX_PULSE_RADIUS = 100;
    private static final float ANIMATION_TRANSLATION_Z = 20;

    private boolean mAnimationOn;
    private Paint mVoiceInputPaint;
    private AnimatableRectF mRectF;

    private int mRippleColor;

    private float mRmsdB;
    private float mPulseRadius;
    private ValueAnimator mValueAnimator;

    public PulsatingButton(Context context) {
        super(context);
        init();
    }

    public PulsatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PulsatingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mVoiceInputPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mVoiceInputPaint.setStyle(Paint.Style.FILL);
        mRectF = new AnimatableRectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mAnimationOn) {
            canvas.drawRoundRect(mRectF, CORNER_RADIUS, CORNER_RADIUS, mVoiceInputPaint);
        }
        super.onDraw(canvas);
    }

    public void setAnimationOn(boolean animate) {
        mAnimationOn = animate;
        mPulseRadius = 0;
        if (mAnimationOn) {
            ViewGroup root = (ViewGroup) getParent().getParent().getParent();
            root.removeView((View) getParent().getParent());
            root.addView((View) getParent().getParent());
        }

        invalidate();
    }

    public void onRmsChanged(float rmsdB) {
        float currentPulseRadius = Math.abs((rmsdB / 7) * MAX_PULSE_RADIUS);
        animatePulse(currentPulseRadius);
    }

    public void setPulseColor(int mRippleColor) {
        this.mRippleColor = mRippleColor;
        mVoiceInputPaint.setColor(mRippleColor);
        mVoiceInputPaint.setAlpha(VOICE_INPUT_COLOR_ALPHA);
    }

    private void animatePulse(float currentPulseRadius) {
        mValueAnimator = ValueAnimator.ofFloat(mPulseRadius, currentPulseRadius);
        mValueAnimator.setInterpolator(new OvershootInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPulseRadius = (float) animation.getAnimatedValue();

                mRectF.set(PulsatingButton.this.getPaddingLeft() / 3 - mPulseRadius,
                        PulsatingButton.this.getPaddingTop() / 3 - mPulseRadius,
                        PulsatingButton.this.getMeasuredWidth() - PulsatingButton.this.getPaddingRight() / 3 + mPulseRadius,
                        PulsatingButton.this.getMeasuredHeight() - PulsatingButton.this.getPaddingBottom() / 3 + mPulseRadius);
                PulsatingButton.this.invalidate();
            }
        });
        mValueAnimator.start();
    }

    private class AnimatableRectF extends RectF {
        public AnimatableRectF() {
            super();
        }

        public AnimatableRectF(float left, float top, float right, float bottom) {
            super(left, top, right, bottom);
        }

        public AnimatableRectF(RectF r) {
            super(r);
        }

        public AnimatableRectF(Rect r) {
            super(r);
        }

        public void setTop(float top) {
            this.top = top;
        }

        public void setBottom(float bottom) {
            this.bottom = bottom;
        }

        public void setRight(float right) {
            this.right = right;
        }

        public void setLeft(float left) {
            this.left = left;
        }

    }

}
