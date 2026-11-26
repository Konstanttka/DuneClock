package com.duneclock.screensaver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.service.dreams.DreamService;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DuneDreamService extends DreamService {
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        try {
            setContentView(new BouncingClockView(this));
        } catch (Exception ignored) {
        }
    }

    public static class BouncingClockView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.getDefault());
        private final Rect textBounds = new Rect();
        private float x = -1, y = -1;
        private float vx = 0.88f, vy = 0.88f;

        public BouncingClockView(Context context) {
            super(context);
            paint.setColor(Color.parseColor("#969696"));
            paint.setTextSize(325f);
            paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            try {
                canvas.drawColor(Color.BLACK);
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) {
                    postInvalidateDelayed(50);
                    return;
                }

                String time = sdf.format(new Date());
                paint.getTextBounds(time, 0, time.length(), textBounds);

                float textWidth = paint.measureText(time);
                float textHeight = textBounds.height();

                if (x < 0 || y < 0) {
                    Random r = new Random();
                    int maxX = Math.max(1, (int) (w - textWidth));
                    int maxY = Math.max(1, (int) (h - textHeight));
                    x = r.nextInt(maxX);
                    y = textHeight + r.nextInt(maxY);
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
            } catch (Exception ignored) {
            }
            postInvalidateDelayed(16);
        }
    }
}
