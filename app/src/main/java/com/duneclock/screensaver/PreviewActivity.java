package com.duneclock.screensaver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PreviewActivity extends Activity {
    private boolean isTesting = false;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(DuneDreamService.PREFS_NAME, MODE_PRIVATE);
        showSettingsScreen();
    }

    private void showSettingsScreen() {
        isTesting = false;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#121212"));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(80, 45, 80, 45);

        TextView title = new TextView(this);
        title.setText("Параметры");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 35);
        root.addView(title);

        // 1. Размер шрифта: от -50% (163) до +50% (488), база 325, шаг 16 (~5%)
        int savedSize = prefs.getInt(DuneDreamService.KEY_FONT_SIZE, DuneDreamService.DEFAULT_FONT_SIZE);
        final int minSize = 163;
        final int maxSize = 488;
        final int stepSize = 16;

        final TextView sizeLabel = createLabel(root, formatSizeText(savedSize));
        final SeekBar sizeSeek = new SeekBar(this);
        sizeSeek.setMax(maxSize - minSize);
        sizeSeek.setProgress(clamp(savedSize - minSize, 0, maxSize - minSize));
        sizeSeek.setFocusable(false);

        addControlRow(root, sizeSeek, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = clamp(sizeSeek.getProgress() - stepSize, 0, maxSize - minSize);
                sizeSeek.setProgress(next);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = clamp(sizeSeek.getProgress() + stepSize, 0, maxSize - minSize);
                sizeSeek.setProgress(next);
            }
        });

        sizeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = minSize + progress;
                sizeLabel.setText(formatSizeText(val));
                prefs.edit().putInt(DuneDreamService.KEY_FONT_SIZE, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 2. Скорость движения: от -50% (0.44) до +50% (1.32), база 0.88, шаг 0.04 (~5%)
        float savedSpeed = prefs.getFloat(DuneDreamService.KEY_SPEED, DuneDreamService.DEFAULT_SPEED);
        final int minSpeedInt = 44;
        final int maxSpeedInt = 132;
        final int stepSpeedInt = 4;

        final TextView speedLabel = createLabel(root, formatSpeedText(savedSpeed));
        final SeekBar speedSeek = new SeekBar(this);
        speedSeek.setMax(maxSpeedInt - minSpeedInt);
        speedSeek.setProgress(clamp(Math.round(savedSpeed * 100f) - minSpeedInt, 0, maxSpeedInt - minSpeedInt));
        speedSeek.setFocusable(false);

        addControlRow(root, speedSeek, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = clamp(speedSeek.getProgress() - stepSpeedInt, 0, maxSpeedInt - minSpeedInt);
                speedSeek.setProgress(next);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = clamp(speedSeek.getProgress() + stepSpeedInt, 0, maxSpeedInt - minSpeedInt);
                speedSeek.setProgress(next);
            }
        });

        speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val = (minSpeedInt + progress) / 100f;
                speedLabel.setText(formatSpeedText(val));
                prefs.edit().putFloat(DuneDreamService.KEY_SPEED, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 3. Яркость цифр: от 60 (24%) до 255 (100%), база 150 (59%), шаг 10 (~5%)
        int savedBright = prefs.getInt(DuneDreamService.KEY_BRIGHTNESS, DuneDreamService.DEFAULT_BRIGHTNESS);
        final int minBright = 60;
        final int maxBright = 255;
        final int stepBright = 10;

        final TextView brightLabel = createLabel(root, formatBrightText(savedBright));
        final SeekBar brightSeek = new SeekBar(this);
        brightSeek.setMax(maxBright - minBright);
        brightSeek.setProgress(clamp(savedBright - minBright, 0, maxBright - minBright));
        brightSeek.setFocusable(false);

        addControlRow(root, brightSeek, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = clamp(brightSeek.getProgress() - stepBright, 0, maxBright - minBright);
                brightSeek.setProgress(next);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int next = clamp(brightSeek.getProgress() + stepBright, 0, maxBright - minBright);
                brightSeek.setProgress(next);
            }
        });

        brightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = minBright + progress;
                brightLabel.setText(formatBrightText(val));
                prefs.edit().putInt(DuneDreamService.KEY_BRIGHTNESS, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Кнопка "Тест скринсейвера"
        final Button testBtn = createStyledButton("Тест скринсейвера", 22f, Color.parseColor("#2B6CB0"), Color.parseColor("#3B82F6"));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.topMargin = 35;
        testBtn.setPadding(60, 22, 60, 22);
        testBtn.setLayoutParams(btnParams);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreview();
            }
        });
        root.addView(testBtn);

        scroll.addView(root);
        setContentView(scroll);
        testBtn.requestFocus();
    }

    private void addControlRow(LinearLayout parent, SeekBar seekBar, View.OnClickListener onMinus, View.OnClickListener onPlus) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = 28;
        row.setLayoutParams(rowParams);

        Button minusBtn = createStyledButton("−", 26f, Color.parseColor("#262626"), Color.parseColor("#3B82F6"));
        LinearLayout.LayoutParams sideBtnParams = new LinearLayout.LayoutParams(86, 68);
        minusBtn.setLayoutParams(sideBtnParams);
        minusBtn.setOnClickListener(onMinus);

        LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        seekParams.leftMargin = 20;
        seekParams.rightMargin = 20;
        seekBar.setLayoutParams(seekParams);

        Button plusBtn = createStyledButton("+", 26f, Color.parseColor("#262626"), Color.parseColor("#3B82F6"));
        LinearLayout.LayoutParams plusBtnParams = new LinearLayout.LayoutParams(86, 68);
        plusBtn.setLayoutParams(plusBtnParams);
        plusBtn.setOnClickListener(onPlus);

        row.addView(minusBtn);
        row.addView(seekBar);
        row.addView(plusBtn);
        parent.addView(row);
    }

    private Button createStyledButton(String text, float textSize, final int normalColor, final int focusColor) {
        final Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(textSize);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setFocusable(true);
        btn.setFocusableInTouchMode(false);
        btn.setPadding(0, 0, 0, 0);

        final GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(12f);
        bg.setColor(normalColor);
        bg.setStroke(3, Color.parseColor("#404040"));
        btn.setBackground(bg);

        btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bg.setColor(focusColor);
                    bg.setStroke(4, Color.WHITE);
                } else {
                    bg.setColor(normalColor);
                    bg.setStroke(3, Color.parseColor("#404040"));
                }
            }
        });
        return btn;
    }

    private TextView createLabel(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#E0E0E0"));
        tv.setTextSize(21f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = 10;
        tv.setLayoutParams(lp);
        parent.addView(tv);
        return tv;
    }

    private String formatSizeText(int val) {
        int pct = Math.round(((val - DuneDreamService.DEFAULT_FONT_SIZE) * 100f) / DuneDreamService.DEFAULT_FONT_SIZE);
        String sign = pct > 0 ? "+" : "";
        return "Размер шрифта: " + val + " (" + sign + pct + "%)";
    }

    private String formatSpeedText(float val) {
        int pct = Math.round(((val - DuneDreamService.DEFAULT_SPEED) * 100f) / DuneDreamService.DEFAULT_SPEED);
        String sign = pct > 0 ? "+" : "";
        return String.format(java.util.Locale.US, "Скорость движения: %.2f (%s%d%%)", val, sign, pct);
    }

    private String formatBrightText(int val) {
        int pct = Math.round((val * 100f) / 255f);
        return "Яркость цифр: " + pct + "%";
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private void startPreview() {
        isTesting = true;
        DuneDreamService.BouncingClockView view = new DuneDreamService.BouncingClockView(this);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsScreen();
            }
        });
        setContentView(view);
        view.startAnimationLoop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isTesting && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
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
}
