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
        setContentView(new BouncingClockView(this));
    }

    private static class BouncingClockView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.getDefault());
        private final Rect textBounds = new Rect();
        private float x = -1, y = -1;
        private float vx = 2.2f, vy = 2.2f; // Скорость полёта

        public BouncingClockView(Context context) {
            super(context);
            paint.setColor(Color.parseColor("#B0B0B0")); // Светло-серый цвет
            paint.setTextSize(130f); // Размер шрифта
            paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
            String time = sdf.format(new Date());
            paint.getTextBounds(time, 0, time.length(), textBounds);

            float textWidth = paint.measureText(time);
            float textHeight = textBounds.height();
            int w = getWidth();
            int h = getHeight();

            if (x < 0 || y < 0) {
                Random r = new Random();
                x = r.nextInt(Math.max(1, (int) (w - textWidth)));
                y = textHeight + r.nextInt(Math.max(1, (int) (h - textHeight)));
            }

            x += vx;
            y += vy;

            if (x <= 0) {
                x = 0;
                vx = Math.abs(vx);
            } else if (x + textWidth >= w) {
                x = w - textWidth;
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
            postInvalidateDelayed(16);
        }
    }
}
