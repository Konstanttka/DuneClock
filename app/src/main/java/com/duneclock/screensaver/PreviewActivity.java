package com.duneclock.screensaver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PreviewActivity extends Activity {
    private boolean isTesting = false;
    private DuneDreamService.BouncingClockView testView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        showSettingsScreen();
    }

    private StateListDrawable createFocusButtonBg(int normalColor, int focusedColor, int radiusPx) {
        GradientDrawable focused = new GradientDrawable();
        focused.setColor(focusedColor);
        focused.setCornerRadius(radiusPx);
        focused.setStroke(4, Color.WHITE);

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(normalColor);
        normal.setCornerRadius(radiusPx);
        normal.setStroke(2, Color.parseColor("#555555"));

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused}, focused);
        states.addState(new int[]{android.R.attr.state_pressed}, focused);
        states.addState(new int[]{}, normal);
        return states;
    }

    private Button createStepButton(String symbol) {
        Button btn = new Button(this);
        btn.setId(View.generateViewId());
        btn.setText(symbol);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setFocusable(true);
        btn.setFocusableInTouchMode(false);
        btn.setPadding(0, 0, 0, 0);
        btn.setBackground(createFocusButtonBg(
                Color.parseColor("#2E2E2E"),
                Color.parseColor("#1E88E5"),
                12
        ));
        return btn;
    }

    private FrameLayout wrapSeekBarWithCenterMark(final SeekBar seekBar, final float defaultRatio) {
        FrameLayout wrapper = new FrameLayout(this);

        View tickView = new View(this) {
            private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int padLeft = seekBar.getPaddingLeft();
                int padRight = seekBar.getPaddingRight();
                int trackWidth = getWidth() - padLeft - padRight;
                if (trackWidth <= 0) return;

                float tickX = padLeft + trackWidth * defaultRatio;
                tickPaint.setColor(Color.parseColor("#FFFFFF"));
                tickPaint.setStrokeWidth(4f);

                float h = getHeight();
                canvas.drawLine(tickX, 2f, tickX, 14f, tickPaint);
                canvas.drawLine(tickX, h - 14f, tickX, h - 2f, tickPaint);
            }
        };

        FrameLayout.LayoutParams tickLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        wrapper.addView(tickView, tickLp);

        FrameLayout.LayoutParams seekLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        seekLp.gravity = Gravity.CENTER_VERTICAL;
        seekLp.topMargin = 8;
        seekLp.bottomMargin = 8;
        wrapper.addView(seekBar, seekLp);

        return wrapper;
    }

    private int stepWithCenterSnap(int current, int delta, int min, int max, int center) {
        int next = current + delta;
        if (next < min) next = min;
        if (next > max) next = max;
        if ((current < center && next > center) || (current > center && next < center)) {
            return center;
        }
        return next;
    }

    private void showSettingsScreen() {
        isTesting = false;
        if (testView != null) {
            testView.stopAnimationLoop();
            testView = null;
        }

        final SharedPreferences prefs = getSharedPreferences(DuneDreamService.PREFS_NAME, MODE_PRIVATE);

        // 1. Размер шрифта (+-50%)
        final int minSize = Math.round(DuneDreamService.DEFAULT_FONT_SIZE * 0.50f);
        final int maxSize = Math.round(DuneDreamService.DEFAULT_FONT_SIZE * 1.50f);
        final int defaultSizeProgress = DuneDreamService.DEFAULT_FONT_SIZE - minSize;
        final int maxSizeProgress = maxSize - minSize;
        final int stepSize = 16;

        int savedSize = prefs.getInt(DuneDreamService.KEY_FONT_SIZE, DuneDreamService.DEFAULT_FONT_SIZE);
        if (savedSize < minSize) savedSize = minSize;
        if (savedSize > maxSize) savedSize = maxSize;

        // 2. Скорость движения (+-50%)
        final float minSpeed = DuneDreamService.DEFAULT_SPEED * 0.50f;
        final float maxSpeed = DuneDreamService.DEFAULT_SPEED * 1.50f;
        final int defaultSpeedProgress = 50;
        final int stepSpeedPct = 5;

        float savedSpeed = prefs.getFloat(DuneDreamService.KEY_SPEED, DuneDreamService.DEFAULT_SPEED);
        if (savedSpeed < minSpeed) savedSpeed = minSpeed;
        if (savedSpeed > maxSpeed) savedSpeed = maxSpeed;

        // 3. Яркость цифр
        final int minBright = 40;
        final int maxBright = 255;
        final int defaultBrightProgress = DuneDreamService.DEFAULT_BRIGHTNESS - minBright;
        final int maxBrightProgress = maxBright - minBright;
        final int stepBright = 15;

        int savedBright = prefs.getInt(DuneDreamService.KEY_BRIGHTNESS, DuneDreamService.DEFAULT_BRIGHTNESS);
        if (savedBright < minBright) savedBright = minBright;
        if (savedBright > maxBright) savedBright = maxBright;

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#121212"));
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(60, 32, 60, 32);

        TextView title = new TextView(this);
        title.setText("Параметры");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = 24;
        root.addView(title, titleParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(36, 26, 36, 26);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1E1E1E"));
        cardBg.setCornerRadius(16);
        card.setBackground(cardBg);

        // --- Полоска 1: Размер шрифта ---
        final TextView sizeLabel = new TextView(this);
        sizeLabel.setTextColor(Color.parseColor("#E0E0E0"));
        sizeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        updateSizeLabel(sizeLabel, savedSize);
        card.addView(sizeLabel);

        final SeekBar sizeSeek = new SeekBar(this);
        sizeSeek.setMax(maxSizeProgress);
        sizeSeek.setProgress(savedSize - minSize);
        sizeSeek.setFocusable(false);
        sizeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = minSize + progress;
                updateSizeLabel(sizeLabel, val);
                prefs.edit().putInt(DuneDreamService.KEY_FONT_SIZE, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final Button sizeMinus = createStepButton("−");
        sizeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = stepWithCenterSnap(sizeSeek.getProgress(), -stepSize, 0, maxSizeProgress, defaultSizeProgress);
                sizeSeek.setProgress(next);
            }
        });

        final Button sizePlus = createStepButton("+");
        sizePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = stepWithCenterSnap(sizeSeek.getProgress(), stepSize, 0, maxSizeProgress, defaultSizeProgress);
                sizeSeek.setProgress(next);
            }
        });

        float sizeCenterRatio = (float) defaultSizeProgress / (float) maxSizeProgress;
        card.addView(createControlRow(sizeMinus, wrapSeekBarWithCenterMark(sizeSeek, sizeCenterRatio), sizePlus), createRowParams(20));

        // --- Полоска 2: Скорость движения ---
        final TextView speedLabel = new TextView(this);
        speedLabel.setTextColor(Color.parseColor("#E0E0E0"));
        speedLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        updateSpeedLabel(speedLabel, savedSpeed);
        card.addView(speedLabel);

        final SeekBar speedSeek = new SeekBar(this);
        speedSeek.setMax(100);
        speedSeek.setProgress(Math.round(((savedSpeed - minSpeed) / (maxSpeed - minSpeed)) * 100f));
        speedSeek.setFocusable(false);
        speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val = (progress == defaultSpeedProgress)
                        ? DuneDreamService.DEFAULT_SPEED
                        : minSpeed + ((maxSpeed - minSpeed) * progress / 100f);
                updateSpeedLabel(speedLabel, val);
                prefs.edit().putFloat(DuneDreamService.KEY_SPEED, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final Button speedMinus = createStepButton("−");
        speedMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = stepWithCenterSnap(speedSeek.getProgress(), -stepSpeedPct, 0, 100, defaultSpeedProgress);
                speedSeek.setProgress(next);
            }
        });

        final Button speedPlus = createStepButton("+");
        speedPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = stepWithCenterSnap(speedSeek.getProgress(), stepSpeedPct, 0, 100, defaultSpeedProgress);
                speedSeek.setProgress(next);
            }
        });

        card.addView(createControlRow(speedMinus, wrapSeekBarWithCenterMark(speedSeek, 0.50f), speedPlus), createRowParams(20));

        // --- Полоска 3: Яркость цифр ---
        final TextView brightLabel = new TextView(this);
        brightLabel.setTextColor(Color.parseColor("#E0E0E0"));
        brightLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        updateBrightLabel(brightLabel, savedBright);
        card.addView(brightLabel);

        final SeekBar brightSeek = new SeekBar(this);
        brightSeek.setMax(maxBrightProgress);
        brightSeek.setProgress(savedBright - minBright);
        brightSeek.setFocusable(false);
        brightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = minBright + progress;
                updateBrightLabel(brightLabel, val);
                prefs.edit().putInt(DuneDreamService.KEY_BRIGHTNESS, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final Button brightMinus = createStepButton("−");
        brightMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = stepWithCenterSnap(brightSeek.getProgress(), -stepBright, 0, maxBrightProgress, defaultBrightProgress);
                brightSeek.setProgress(next);
            }
        });

        final Button brightPlus = createStepButton("+");
        brightPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = stepWithCenterSnap(brightSeek.getProgress(), stepBright, 0, maxBrightProgress, defaultBrightProgress);
                brightSeek.setProgress(next);
            }
        });

        float brightCenterRatio = (float) defaultBrightProgress / (float) maxBrightProgress;
        card.addView(createControlRow(brightMinus, wrapSeekBarWithCenterMark(brightSeek, brightCenterRatio), brightPlus), createRowParams(6));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                900,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = 24;
        root.addView(card, cardParams);

        // --- Нижний ряд кнопок: Сброс и Тест скринсейвера ---
        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER);

        final Button resetBtn = new Button(this);
        resetBtn.setId(View.generateViewId());
        resetBtn.setText("Сброс");
        resetBtn.setTextColor(Color.WHITE);
        resetBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        resetBtn.setTypeface(Typeface.DEFAULT_BOLD);
        resetBtn.setPadding(44, 18, 44, 18);
        resetBtn.setFocusable(true);
        resetBtn.setFocusableInTouchMode(false);
        resetBtn.setBackground(createFocusButtonBg(
                Color.parseColor("#2A2A2A"),
                Color.parseColor("#D32F2F"),
                14
        ));
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sizeSeek.setProgress(defaultSizeProgress);
                speedSeek.setProgress(defaultSpeedProgress);
                brightSeek.setProgress(defaultBrightProgress);
            }
        });

        final Button testBtn = new Button(this);
        testBtn.setId(View.generateViewId());
        testBtn.setText("Тест скринсейвера");
        testBtn.setTextColor(Color.WHITE);
        testBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        testBtn.setTypeface(Typeface.DEFAULT_BOLD);
        testBtn.setPadding(48, 18, 48, 18);
        testBtn.setFocusable(true);
        testBtn.setFocusableInTouchMode(false);
        testBtn.setBackground(createFocusButtonBg(
                Color.parseColor("#2A2A2A"),
                Color.parseColor("#1E88E5"),
                14
        ));
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScreensaverTest();
            }
        });

        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        resetLp.rightMargin = 28;
        bottomRow.addView(resetBtn, resetLp);

        LinearLayout.LayoutParams testLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bottomRow.addView(testBtn, testLp);

        root.addView(bottomRow);

        // --- Навигация фокуса стрелками пульта Zidoo ---
        sizeMinus.setNextFocusRightId(sizePlus.getId());
        sizeMinus.setNextFocusDownId(speedMinus.getId());
        sizePlus.setNextFocusLeftId(sizeMinus.getId());
        sizePlus.setNextFocusDownId(speedPlus.getId());

        speedMinus.setNextFocusUpId(sizeMinus.getId());
        speedMinus.setNextFocusRightId(speedPlus.getId());
        speedMinus.setNextFocusDownId(brightMinus.getId());
        speedPlus.setNextFocusUpId(sizePlus.getId());
        speedPlus.setNextFocusLeftId(speedMinus.getId());
        speedPlus.setNextFocusDownId(brightPlus.getId());

        brightMinus.setNextFocusUpId(speedMinus.getId());
        brightMinus.setNextFocusRightId(brightPlus.getId());
        brightMinus.setNextFocusDownId(resetBtn.getId());
        brightPlus.setNextFocusUpId(speedPlus.getId());
        brightPlus.setNextFocusLeftId(brightMinus.getId());
        brightPlus.setNextFocusDownId(testBtn.getId());

        resetBtn.setNextFocusUpId(brightMinus.getId());
        resetBtn.setNextFocusRightId(testBtn.getId());
        testBtn.setNextFocusUpId(brightPlus.getId());
        testBtn.setNextFocusLeftId(resetBtn.getId());

        scrollView.addView(root);
        setContentView(scrollView);
        testBtn.requestFocus();
    }

    private LinearLayout.LayoutParams createRowParams(int bottomMargin) {
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = 10;
        rowParams.bottomMargin = bottomMargin;
        return rowParams;
    }

    private LinearLayout createControlRow(Button minusBtn, View seekWrapper, Button plusBtn) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(68, 56);
        row.addView(minusBtn, btnLp);

        LinearLayout.LayoutParams seekLp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        seekLp.leftMargin = 16;
        seekLp.rightMargin = 16;
        row.addView(seekWrapper, seekLp);

        row.addView(plusBtn, btnLp);
        return row;
    }

    private void updateSizeLabel(TextView label, int val) {
        int pct = Math.round(((val - DuneDreamService.DEFAULT_FONT_SIZE) * 100f) / DuneDreamService.DEFAULT_FONT_SIZE);
        String sign = pct > 0 ? "+" : "";
        label.setText("Размер шрифта: " + val + " (" + sign + pct + "%)");
    }

    private void updateSpeedLabel(TextView label, float val) {
        int pct = Math.round(((val - DuneDreamService.DEFAULT_SPEED) * 100f) / DuneDreamService.DEFAULT_SPEED);
        String sign = pct > 0 ? "+" : "";
        label.setText(String.format(java.util.Locale.US, "Скорость движения: %.2f (%s%d%%)", val, sign, pct));
    }

    private void updateBrightLabel(TextView label, int val) {
        int pct = Math.round((val * 100f) / 255f);
        label.setText("Яркость цифр: " + pct + "%");
    }

    private void startScreensaverTest() {
        isTesting = true;
        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(Color.BLACK);

        testView = new DuneDreamService.BouncingClockView(this);
        testView.setKeepScreenOn(true);
        testView.setFocusable(true);
        testView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsScreen();
            }
        });

        container.addView(testView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        setContentView(container);
        testView.requestFocus();
        testView.startAnimationLoop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isTesting) {
            showSettingsScreen();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (isTesting) {
            showSettingsScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (testView != null) {
            testView.stopAnimationLoop();
        }
    }
}
