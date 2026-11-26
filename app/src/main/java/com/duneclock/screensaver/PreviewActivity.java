package com.duneclock.screensaver;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class PreviewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(new DuneDreamService.BouncingClockView(this));
    }
}
