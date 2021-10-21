package com.tomatedigital.androidutils.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;


import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.tomatedigital.androidutils.Constants;

public class MultitouchListener implements View.OnTouchListener {

    private final View view;
    private int initialViewX;
    private int initialViewY;

    private float initialEventY;
    private float initialEventX;
    private final WindowManager windowManager;

    private final String viewId;

    private final ScaleGestureDetector scaleGestureDetector;

    private boolean allowScale;
    private boolean allowDrag;

    private boolean dragging;


    private final float pxPerMmX;
    private final float pxPerMmY;
    private MotionEvent lastEvt;

    public MultitouchListener(@NonNull final View view, @NonNull final String viewUniqueId) {
        this.allowDrag = true;
        this.allowScale = true;

        this.view = view;
        this.viewId = viewUniqueId;
        this.windowManager = ((WindowManager) this.view.getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE));

        final DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();

        this.pxPerMmX = dm.xdpi / 25.4f;
        this.pxPerMmY = dm.ydpi / 25.4f;

        this.scaleGestureDetector = new ScaleGestureDetector(this.view.getContext(), new ScaleListener((int) (dm.widthPixels * 0.1), dm.widthPixels, (int) (dm.heightPixels * 0.1), dm.heightPixels));

    }


    public void setAllowScale(boolean allowScale) {
        this.allowScale = allowScale;
    }

    public void setAllowDrag(boolean allowDrag) {
        this.allowDrag = allowDrag;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(@NonNull final View v, @NonNull final MotionEvent event) {

        if (!this.allowDrag && !this.allowScale)
            return false;

        boolean result = false;
        final int action = event.getAction();
        final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.view.getLayoutParams();
        this.lastEvt = event;

        if (this.allowDrag && event.getPointerCount() == 1) {

            if (action == MotionEvent.ACTION_MOVE && this.dragging) {
                result = true;
                float o = event.getRawX();
                float p = event.getRawY();

                layoutParams.x = this.initialViewX + ((int) (o - this.initialEventX));
                layoutParams.y = this.initialViewY + ((int) (p - this.initialEventY));

            } else if (action == MotionEvent.ACTION_DOWN) {
                this.initialViewX = layoutParams.x;
                this.initialViewY = layoutParams.y;
                this.initialEventX = event.getRawX();
                this.initialEventY = event.getRawY();


                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    this.dragging = this.lastEvt != null && Math.abs((this.lastEvt.getRawX() - this.initialEventX) / this.pxPerMmX) < 7 && Math.abs((this.lastEvt.getRawY() - this.initialEventY) / this.pxPerMmY) < 7;
                    if (this.dragging) {
                        Vibrator vib = (Vibrator) this.view.getContext().getSystemService(Context.VIBRATOR_SERVICE);

                        // Vibrate for 500 milliseconds
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            vib.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        else
                            vib.vibrate(100);

                    }
                }, 800);
            }


        } else if (this.allowScale)
            result = this.scaleGestureDetector.onTouchEvent(event);


        if (result)
            this.windowManager.updateViewLayout(this.view, layoutParams);

        if (action == MotionEvent.ACTION_UP) {
            this.view.getContext().getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, Context.MODE_PRIVATE).edit()
                    .putInt(Constants.DefaultSharedPreferences.FloatingWindow.X(this.viewId), layoutParams.x)
                    .putInt(Constants.DefaultSharedPreferences.FloatingWindow.Y(this.viewId), layoutParams.y)
                    .putInt(Constants.DefaultSharedPreferences.FloatingWindow.WIDTH(this.viewId), layoutParams.width)
                    .putInt(Constants.DefaultSharedPreferences.FloatingWindow.HEIGHT(this.viewId), layoutParams.height)
                    .apply();

            this.lastEvt = null;
            this.dragging = false;
        }


        return result;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private final int minAllowedWidth;
        private final int maxAllowedWidth;

        private final int maxAllowedHeight;
        private final int minAllowedHeight;

        private ScaleListener(int minAllowedWidth, int maxAllowedWidth, int minAllowedHeight, int maxAllowedHeight) {
            this.minAllowedWidth = minAllowedWidth;
            this.maxAllowedWidth = maxAllowedWidth;
            this.maxAllowedHeight = maxAllowedHeight;
            this.minAllowedHeight = minAllowedHeight;
        }


        @Override
        public boolean onScale(@NonNull final ScaleGestureDetector detector) {


            final WindowManager.LayoutParams windowLayout = (WindowManager.LayoutParams) view.getLayoutParams();

            final int initialWidth = windowLayout.width;
            final int initialHeight = windowLayout.height;

            float factorX = 1 + ((detector.getCurrentSpanX() - detector.getPreviousSpanX()) / this.maxAllowedWidth);
            float factorY = 1 + ((detector.getCurrentSpanY() - detector.getPreviousSpanY()) / this.maxAllowedHeight);


            windowLayout.width = (int) Math.max(this.minAllowedWidth, Math.min(factorX * windowLayout.width, this.maxAllowedWidth));
            windowLayout.height = (int) Math.max(this.minAllowedHeight, Math.min(factorY * windowLayout.height, this.maxAllowedHeight));


            return Math.abs(initialWidth - windowLayout.width) > pxPerMmX/2 || Math.abs(initialHeight - windowLayout.height) > pxPerMmY/2;
        }
    }


}
