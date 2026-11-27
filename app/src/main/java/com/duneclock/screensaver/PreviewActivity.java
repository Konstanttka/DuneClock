package com.duneclock.screensaver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

public class PreviewActivity extends Activity {

    private boolean isTesting = false;
    private SharedPreferences prefs;

    private static final float MIN_TEXT_SIZE = 228f; // 325 - 30%
    private static final float MAX_TEXT_SIZE = 423f; // 325 + 30%

    private static final float MIN_SPEED = 0.62f; // 0.88 - 30%
    private static final float MAX_SPEED = 1.14f; // 0.88 + 30%

    private static final int MIN_BRIGHTNESS = 60;
    private static final int MAX_BRIGHTNESS = 255;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(DuneDreamService.PREFS_NAME, MODE_PRIVATE);
        showSettingsScreen();
    }

    private void showSettingsScreen() {
        isTesting = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#121212"));
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = dp(32);
        root.setPadding(pad, dp(28), pad, dp(28));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(dp(560), LinearLayout.LayoutParams.WRAP_CONTENT));

        // Заголовок "Параметры"
        TextView titleView = new TextView(this);
        titleView.setText("Параметры");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = dp(28);
        container.addView(titleView, titleParams);

        // 1. Размер шрифта (+-30%)
        float currentSize = prefs.getFloat(DuneDreamService.KEY_TEXT_SIZE, DuneDreamService.DEFAULT_TEXT_SIZE);
        int sizeMaxProgress = Math.round(MAX_TEXT_SIZE - MIN_TEXT_SIZE);
        int sizeProgress = Math.max(0, Math.min(sizeMaxProgress, Math.round(currentSize - MIN_TEXT_SIZE)));

        final TextView sizeLabel = createLabel();
        updateSizeLabel(sizeLabel, currentSize);
        container.addView(sizeLabel);

        SeekBar sizeSeek = createStyledSeekBar(sizeMaxProgress, sizeProgress);
        sizeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val = MIN_TEXT_SIZE + progress;
                updateSizeLabel(sizeLabel, val);
                prefs.edit().putFloat(DuneDreamService.KEY_TEXT_SIZE, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        container.addView(sizeSeek, createSeekParams());

        // 2. Скорость движения (+-30%)
        float currentSpeed = prefs.getFloat(DuneDreamService.KEY_SPEED, DuneDreamService.DEFAULT_SPEED);
        int speedMaxProgress = Math.round((MAX_SPEED - MIN_SPEED) * 100f);
        int speedProgress = Math.max(0, Math.min(speedMaxProgress, Math.round((currentSpeed - MIN_SPEED) * 100f)));

        final TextView speedLabel = createLabel();
        updateSpeedLabel(speedLabel, currentSpeed);
        container.addView(speedLabel);

        SeekBar speedSeek = createStyledSeekBar(speedMaxProgress, speedProgress);
        speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val = MIN_SPEED + (progress / 100f);
                updateSpeedLabel(speedLabel, val);
                prefs.edit().putFloat(DuneDreamService.KEY_SPEED, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        container.addView(speedSeek, createSeekParams());

        // 3. Яркость цифр
        int currentBrightness = prefs.getInt(DuneDreamService.KEY_BRIGHTNESS, DuneDreamService.DEFAULT_BRIGHTNESS);
        int brightMaxProgress = MAX_BRIGHTNESS - MIN_BRIGHTNESS;
        int brightProgress = Math.max(0, Math.min(brightMaxProgress, currentBrightness - MIN_BRIGHTNESS));

        final TextView brightLabel = createLabel();
        updateBrightnessLabel(brightLabel, currentBrightness);
        container.addView(brightLabel);

        SeekBar brightSeek = createStyledSeekBar(brightMaxProgress, brightProgress);
        brightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = MIN_BRIGHTNESS + progress;
                updateBrightnessLabel(brightLabel, val);
                prefs.edit().putInt(DuneDreamService.KEY_BRIGHTNESS, val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        container.addView(brightSeek, createSeekParams());

        // Кнопка "Тест скринсейвера"
        Button testButton = new Button(this);
        testButton.setText("Тест скринсейвера");
        testButton.setAllCaps(false);
        testButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        testButton.setTypeface(Typeface.DEFAULT_BOLD);
        testButton.setTextColor(Color.WHITE);
        testButton.setFocusable(true);
        testButton.setFocusableInTouchMode(true);
        testButton.setPadding(dp(24), dp(14), dp(24), dp(14));

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#2E6F40"));
        btnBg.setCornerRadius(dp(10));
        testButton.setBackground(btnBg);

        testButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(hasFocus ? Color.parseColor("#3E9255") : Color.parseColor("#2E6F40"));
                bg.setStroke(hasFocus ? dp(2) : 0, Color.WHITE);
                bg.setCornerRadius(dp(10));
                v.setBackground(bg);
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTestMode();
            }
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = dp(20);
        container.addView(testButton, btnParams);

        root.addView(container);
        scrollView.addView(root);
        setContentView(scrollView);
    }

    private void startTestMode() {
        isTesting = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        DuneDreamService.BouncingClockView clockView = new DuneDreamService.BouncingClockView(this);
        clockView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsScreen();
            }
        });
        setContentView(clockView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isTesting) {
            showSettingsScreen();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private TextView createLabel() {
        TextView tv = new TextView(this);
        tv.setTextColor(Color.parseColor("#E0E0E0"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        tv.setLayoutParams(lp);
        return tv;
    }

    private SeekBar createStyledSeekBar(int max, int progress) {
        SeekBar sb = new SeekBar(this);
        sb.setMax(max);
        sb.setProgress(progress);
        sb.setFocusable(true);
        sb.setKeyProgressIncrement(Math.max(1, max / 30));
        sb.setPadding(dp(8), dp(8), dp(8), dp(8));
        return sb;
    }

    private LinearLayout.LayoutParams createSeekParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(22);
        return lp;
    }

    private void updateSizeLabel(TextView tv, float val) {
        int pct = Math.round(((val - DuneDreamService.DEFAULT_TEXT_SIZE) / DuneDreamService.DEFAULT_TEXT_SIZE) * 100f);
        String sign = pct > 0 ? "+" : "";
        tv.setText(String.format(Locale.US, "Размер шрифта: %d (%s%d%%)", Math.round(val), sign, pct));
    }

    private void updateSpeedLabel(TextView tv, float val) {
        int pct = Math.round(((val - DuneDreamService.DEFAULT_SPEED) / DuneDreamService.DEFAULT_SPEED) * 100f);
        String sign = pct > 0 ? "+" : "";
        tv.setText(String.format(Locale.US, "Скорость движения: %.2f (%s%d%%)", val, sign, pct));
    }

    private void updateBrightnessLabel(TextView tv, int val) {
        int pct = Math.round((val / 255f) * 100f);
        tv.setText(String.format(Locale.US, "Яркость цифр: %d%%", pct));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
