package com.duneclock.screensaver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
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

    private void showSettingsScreen() {
        isTesting = false;
        if (testView != null) {
            testView.stopAnimationLoop();
            testView = null;
        }

        final SharedPreferences prefs = getSharedPreferences(DuneDreamService.PREFS_NAME, MODE_PRIVATE);

        final int minSize = Math.round(DuneDreamService.DEFAULT_FONT_SIZE * 0.50f);
        final int maxSize = Math.round(DuneDreamService.DEFAULT_FONT_SIZE * 1.50f);
        final int stepSize = 16;
        int savedSize = prefs.getInt(DuneDreamService.KEY_FONT_SIZE, DuneDreamService.DEFAULT_FONT_SIZE);
        if (savedSize < minSize) savedSize = minSize;
        if (savedSize > maxSize) savedSize = maxSize;

        final float minSpeed = DuneDreamService.DEFAULT_SPEED * 0.50f;
        final float maxSpeed = DuneDreamService.DEFAULT_SPEED * 1.50f;
        final float stepSpeed = 0.044f;
        float savedSpeed = prefs.getFloat(DuneDreamService.KEY_SPEED, DuneDreamService.DEFAULT_SPEED);
        if (savedSpeed < minSpeed) savedSpeed = minSpeed;
        if (savedSpeed > maxSpeed) savedSpeed = maxSpeed;

        final int minBright = 40;
        final int maxBright = 255;
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
        root.setPadding(60, 36, 60, 36);

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
        titleParams.bottomMargin = 28;
        root.addView(title, titleParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(36, 28, 36, 28);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1E1E1E"));
        cardBg.setCornerRadius(16);
        card.setBackground(cardBg);

        // 1. Размер шрифта (+-50%)
        final TextView sizeLabel = new TextView(this);
        sizeLabel.setTextColor(Color.parseColor("#E0E0E0"));
        sizeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        updateSizeLabel(sizeLabel, savedSize);
        card.addView(sizeLabel);

        final SeekBar sizeSeek = new SeekBar(this);
        sizeSeek.setMax(maxSize - minSize);
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

        Button sizeMinus = createStepButton("−");
        sizeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = Math.max(0, sizeSeek.getProgress() - stepSize);
                sizeSeek.setProgress(next);
            }
        });

        Button sizePlus = createStepButton("+");
        sizePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = Math.min(sizeSeek.getMax(), sizeSeek.getProgress() + stepSize);
                sizeSeek.setProgress(next);
            }
        });

        card.addView(createControlRow(sizeMinus, sizeSeek, sizePlus), createRowParams(24));

        // 2. Скорость движения (+-50%)
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
                float val = minSpeed + ((maxSpeed - minSpeed) * progress / 100f);
                updateSpeedLabel(speedLabel, val);
                prefs.edit().putFloat(DuneDreamService.KEY_SPEED, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button speedMinus = createStepButton("−");
        speedMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stepPct = Math.max(1, Math.round((stepSpeed / (maxSpeed - minSpeed)) * 100f));
                int next = Math.max(0, speedSeek.getProgress() - stepPct);
                speedSeek.setProgress(next);
            }
        });

        Button speedPlus = createStepButton("+");
        speedPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stepPct = Math.max(1, Math.round((stepSpeed / (maxSpeed - minSpeed)) * 100f));
                int next = Math.min(100, speedSeek.getProgress() + stepPct);
                speedSeek.setProgress(next);
            }
        });

        card.addView(createControlRow(speedMinus, speedSeek, speedPlus), createRowParams(24));

        // 3. Яркость цифр
        final TextView brightLabel = new TextView(this);
        brightLabel.setTextColor(Color.parseColor("#E0E0E0"));
        brightLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        updateBrightLabel(brightLabel, savedBright);
        card.addView(brightLabel);

        final SeekBar brightSeek = new SeekBar(this);
        brightSeek.setMax(maxBright - minBright);
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

        Button brightMinus = createStepButton("−");
        brightMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = Math.max(0, brightSeek.getProgress() - stepBright);
                brightSeek.setProgress(next);
            }
        });

        Button brightPlus = createStepButton("+");
        brightPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = Math.min(brightSeek.getMax(), brightSeek.getProgress() + stepBright);
                brightSeek.setProgress(next);
            }
        });

        card.addView(createControlRow(brightMinus, brightSeek, brightPlus), createRowParams(8));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                900,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = 28;
        root.addView(card, cardParams);

        final Button testBtn = new Button(this);
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

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        root.addView(testBtn, btnParams);

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

    private LinearLayout createControlRow(Button minusBtn, SeekBar seekBar, Button plusBtn) {
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
        row.addView(seekBar, seekLp);

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
