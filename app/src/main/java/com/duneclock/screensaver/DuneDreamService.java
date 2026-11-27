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
    public static final String KEY_TEXT_SIZE = "text_size";
    public static final String KEY_SPEED = "speed";
    public static final String KEY_BRIGHTNESS = "brightness";

    public static final float DEFAULT_TEXT_SIZE = 325f;
    public static final float DEFAULT_SPEED = 0.88f;
    public static final int DEFAULT_BRIGHTNESS = 150;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            setInteractive(false);
            setFullscreen(true);
            setContentView(new BouncingClockView(this));
        } catch (Throwable ignored) {
        }
    }

    public static class BouncingClockView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.getDefault());
        private final Rect textBounds = new Rect();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private float x = -1f;
        private float y = -1f;
        private float vx;
        private float vy;
        private boolean initialized = false;

        private final Runnable ticker = new Runnable() {
            @Override
            public void run() {
                invalidate();
                handler.postDelayed(this, 16);
            }
        };

        public BouncingClockView(Context context) {
            super(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            float textSize = prefs.getFloat(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE);
            float speed = prefs.getFloat(KEY_SPEED, DEFAULT_SPEED);
            int brightness = prefs.getInt(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);
            if (brightness < 40) brightness = 40;
            if (brightness > 255) brightness = 255;

            vx = speed;
            vy = speed;

            paint.setColor(Color.rgb(brightness, brightness, brightness));
            paint.setTextSize(textSize);
            try {
                paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            } catch (Throwable t) {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            handler.post(ticker);
        }

        @Override
        protected void onDetachedFromWindow() {
            handler.removeCallbacks(ticker);
            super.onDetachedFromWindow();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            try {
                canvas.drawColor(Color.BLACK);
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) {
                    return;
                }

                String time = sdf.format(new Date());
                paint.getTextBounds(time, 0, time.length(), textBounds);

                float textWidth = paint.measureText(time);
                float textHeight = textBounds.height();

                if (!initialized) {
                    Random r = new Random();
                    int maxX = Math.max(1, (int) (w - textWidth));
                    int maxY = Math.max(1, (int) (h - textHeight));
                    x = r.nextInt(maxX);
                    y = textHeight + r.nextInt(maxY);
                    initialized = true;
                }

                x += vx;
                y += vy;

                if (x <= 0) {
                    x = 0;
                    vx = Math.abs(vx);
                } else if (x + textWidth >= w) {
                    x = Math.max(0, w - textWidth);
                    vx = -Math.abs(vx);
                }

                if (y - textHeight <= 0) {
                    y = textHeight;
                    vy = Math.abs(vy);
                } else if (y >= h) {
                    y = h;
                    vy = -Math.abs(vy);
                }

                canvas.drawText(time, x, y, paint);
            } catch (Throwable ignored) {
            }
        }
    }
}
