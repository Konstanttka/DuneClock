package com.duneclock.screensaver;

import android.content.Context;
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
    private BouncingClockView clockView;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        clockView = new BouncingClockView(this);
        setContentView(clockView);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        if (clockView != null) {
            clockView.startAnimation();
        }
    }

    @Override
    public void onDreamingStopped() {
        if (clockView != null) {
            clockView.stopAnimation();
        }
        super.onDreamingStopped();
    }

    private static class BouncingClockView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.US);
        private final Rect textBounds = new Rect();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private float x = -1f, y = -1f;
        private float vx = 2.5f, vy = 2.5f;
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
            paint.setColor(Color.parseColor("#B0B0B0"));
            paint.setTextSize(130f);
            try {
                paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            } catch (Exception e) {
                paint.setTypeface(Typeface.DEFAULT);
            }
        }

        public void startAnimation() {
            if (!running) {
                running = true;
                handler.post(frameRunnable);
            }
        }

        public void stopAnimation() {
            running = false;
            handler.removeCallbacks(frameRunnable);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.BLACK);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            String time = sdf.format(new Date());
            paint.getTextBounds(time, 0, time.length(), textBounds);

            float textWidth = paint.measureText(time);
            float textHeight = Math.max(1f, textBounds.height());

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
