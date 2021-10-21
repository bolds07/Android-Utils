package com.tomatedigital.androidutils.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.tomatedigital.androidutils.Constants;


public class FloatingWindow {

    public static final int VERTICAL_EDGE = 2;
    public static final int ORIZONTAL_EDGE = 3;

   
    private final SharedPreferences sp;
    private final WindowManager wm;
    private final FrameLayout main;
    private final DisplayMetrics dm;
    private final String windowId;


    private boolean running = false;
    private MultitouchListener handler;

    public FloatingWindow(@NonNull final Context context, @NonNull final View view, @NonNull final String windowId) {
        this.wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        this.main = new FrameLayout(context.getApplicationContext());
        this.sp = context.getSharedPreferences(Constants.DefaultSharedPreferences.PREFERENCES_FILE, Context.MODE_PRIVATE);
        this.windowId = windowId;

        this.dm = Resources.getSystem().getDisplayMetrics();


        this.setContentView(view);

    }


    public boolean isRunning() {
        return this.running;
    }


    public void setAllowScale(boolean allowScale) {
        this.handler.setAllowScale(allowScale);
    }

    public void setAllowDrag(boolean allowDrag) {
        this.handler.setAllowDrag(allowDrag);
    }

     

    public void close() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || this.main.isAttachedToWindow())
            this.wm.removeView(this.main);


    }


    @SuppressLint("ClickableViewAccessibility")
    private void setContentView(@NonNull final View view) {

        this.main.addView(view);


        this.main.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                running = true;
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                running = false;
            }
        });

        this.handler = new MultitouchListener(this.main, this.windowId);
        this.main.setOnTouchListener(this.handler);

        int type = WindowManager.LayoutParams.TYPE_TOAST;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;


        final DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                sp.getInt(Constants.DefaultSharedPreferences.FloatingWindow.WIDTH(this.windowId), (int) (this.dm.widthPixels / 2.5)),
                sp.getInt(Constants.DefaultSharedPreferences.FloatingWindow.HEIGHT(this.windowId), (int) (this.dm.heightPixels / 2.5)),
                sp.getInt(Constants.DefaultSharedPreferences.FloatingWindow.X(this.windowId), this.dm.widthPixels / 16),
                sp.getInt(Constants.DefaultSharedPreferences.FloatingWindow.Y(this.windowId), this.dm.heightPixels / 16),
                type,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.horizontalMargin = 0;
        params.verticalMargin = 0;

        this.wm.addView(this.main, params);
    }


    public void moveToOppositeEdge(final int edge) {

        final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.main.getLayoutParams();

        int newX = layoutParams.x;
        int newY = layoutParams.y;
        if (edge % ORIZONTAL_EDGE == 0) {
            int leftDist = layoutParams.x;
            int rightDist = this.dm.widthPixels - (layoutParams.x + layoutParams.width);

            if (leftDist < rightDist)
                newX = layoutParams.x + rightDist;
            else
                newX = 0;
        }
        if (edge % VERTICAL_EDGE == 0) {
            int topDist = layoutParams.y;
            int bottomDist = this.dm.heightPixels - (layoutParams.y + layoutParams.height);

            if (topDist <bottomDist)
                newY = layoutParams.y + bottomDist;
            else
                newY = 0;
        }


        layoutParams.x = newX;
        layoutParams.y = newY;
        this.wm.updateViewLayout(this.main, layoutParams);


    }
}
