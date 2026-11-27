package com.duneclock.screensaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DuneDreamService extends DreamService {
    public static final String PREFS_NAME = "DuneClockPrefs";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_SPEED = "speed";
    public static final String KEY_BRIGHTNESS = "brightness";

    public static final int DEFAULT_FONT_SIZE = 325;
    public static final float DEFAULT_SPEED = 0.88f;
    public static final int DEFAULT_BRIGHTNESS = 150;

    private BouncingClockView clockView;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            setInteractive(false);
            setFullscreen(true);
            clockView = new BouncingClockView(this);
            setContentView(clockView);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        if (clockView != null) {
            clockView.startAnimationLoop();
        }
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        if (clockView != null) {
            clockView.stopAnimationLoop();
        }
    }

    public static class BouncingClockView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.getDefault());
        private final Rect textBounds = new Rect();
        private final Handler handler = new Handler(Looper.getMainLooper());

        private float x = -1f, y = -1f;
        private float vx = DEFAULT_SPEED;
        private float vy = DEFAULT_SPEED;
        private boolean running = false;

        private final Runnable frameRunnable = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                invalidate();
                handler.postDelayed(this, 16);
            }
        };

        public BouncingClockView(Context context) {
            super(context);
            paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            reloadPreferences(context);
        }

        public void reloadPreferences(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int fontSize = prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
            float speed = prefs.getFloat(KEY_SPEED, DEFAULT_SPEED);
            int brightness = prefs.getInt(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);

            if (brightness < 30) brightness = 30;
            if (brightness > 255) brightness = 255;

            paint.setTextSize((float) fontSize);
            paint.setColor(Color.rgb(brightness, brightness, brightness));

            float dirX = (vx < 0) ? -1f : 1f;
            float dirY = (vy < 0) ? -1f : 1f;
            vx = dirX * speed;
            vy = dirY * speed;
            invalidate();
        }

        public void startAnimationLoop() {
            if (!running) {
                running = true;
                handler.removeCallbacks(frameRunnable);
                handler.post(frameRunnable);
            }
        }

        public void stopAnimationLoop() {
            running = false;
            handler.removeCallbacks(frameRunnable);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            startAnimationLoop();
        }

        @Override
        protected void onDetachedFromWindow() {
            stopAnimationLoop();
            super.onDetachedFromWindow();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            String time = sdf.format(new Date());
            paint.getTextBounds(time, 0, time.length(), textBounds);

            float textWidth = paint.measureText(time);
            float textHeight = textBounds.height();

            if (x < 0f || y < 0f) {
                Random r = new Random();
                int maxX = Math.max(1, (int) (w - textWidth));
                int maxY = Math.max(1, (int) (h - textHeight));
                x = r.nextInt(maxX);
                y = textHeight + r.nextInt(maxY);
            }

            x += vx;
            y += vy;

            if (x <= 0f) {
                x = 0f;
                vx = Math.abs(vx);
            } else if (x + textWidth >= w) {
                x = Math.max(0f, w - textWidth);
                vx = -Math.abs(vx);
            }

            if (y - textHeight <= 0f) {
                y = textHeight;
                vy = Math.abs(vy);
            } else if (y >= h) {
                y = h;
                vy = -Math.abs(vy);
            }

            canvas.drawText(time, x, y, paint);
        }
    }
}
