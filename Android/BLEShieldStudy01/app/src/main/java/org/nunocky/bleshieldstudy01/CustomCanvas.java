package org.nunocky.bleshieldstudy01;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

public class CustomCanvas extends View {
    private static final String TAG = "CustomCanvas";
    private static final long DELAY_MS = 1000 / 15;
    private static final float MARGIN_X = 0.2f;
    private static final float MARGIN_Y = 0.1f;
    private static final float SPRING = 0.3f;

    Paint paint_circle = new Paint();


    float mRaduis;
    float mCircleX, mCircleY, center_x, center_y;
    private boolean mActive;
    private boolean mTouchOn;

    float mNormalX, mNormalY;

    public CustomCanvas(Context context) {
        super(context);
        paint_circle.setTextSize(32.0f);
    }

    Handler handler = new Handler();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCircleX = center_x = getWidth() / 2.0f;
        mCircleY = center_y = getHeight() / 2.0f;
        Log.d(TAG, "(ﾟ∀ﾟ)");
        Log.d(TAG, "canvas size: " + getWidth() + "mCircleX" + getHeight());
        mRaduis = getWidth() / 5;
        mActive = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                move();
            }
        }, DELAY_MS);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mActive = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, event.toString());
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                mTouchOn = true;
                mCircleX = event.getX(0);
                mCircleY = event.getY(0);
                updateXY();
                break;
            case MotionEvent.ACTION_UP:
                mTouchOn = false;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);

        paint_circle.setColor(Color.YELLOW);

        canvas.drawCircle(mCircleX, mCircleY, mRaduis, paint_circle);

        @SuppressLint("DefaultLocale")
        String str = String.format("dir=%d, spped=%d", (int) mNormalX, (int) mNormalY);

        canvas.drawText(str, 0, 32, paint_circle);
    }

    private void updateXY() {
        //float dx, dy;
        float dx = center_x - mCircleX;
        float dy = center_y - mCircleY;

        mCircleX += SPRING * dx;
        mCircleY += SPRING * dy;

        // 正規化
        mNormalX = (mCircleX - center_x) / center_x;
        mNormalY = (mCircleY - center_y) / center_y;
//        Log.d(TAG, "move " + mNormalX + " " + mNormalY);

        if (-MARGIN_X < mNormalX && mNormalX < MARGIN_X) {
            mNormalX = 0;
        }

        if (-MARGIN_Y < mNormalY && mNormalY < MARGIN_Y) {
            mNormalY = 0;
        }

        mNormalX *= 127.0f;
        mNormalY *= -127.0f;

        mNormalX = Math.max(-127.0f, mNormalX);
        mNormalX = Math.min(mNormalX, 127.0f);

        mNormalY = Math.max(-127.0f, mNormalY);
        mNormalY = Math.min(mNormalY, 127.0f);
    }

    private void move() {
        if (!mTouchOn)
            updateXY();

        ControlEvent ev = new ControlEvent();
        ev.direction = (int) mNormalX;
        ev.speed = (int) mNormalY;

        EventBus.getDefault().post(ev);

        if (mActive) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    move();
                    invalidate();
                }
            }, DELAY_MS);
        }
    }
}
