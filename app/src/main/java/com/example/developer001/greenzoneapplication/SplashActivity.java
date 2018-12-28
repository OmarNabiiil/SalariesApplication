package com.example.developer001.greenzoneapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    private int currentApiVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_splash);

        Intent i = new Intent(SplashActivity.this, DateActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // close this activity
        finish();
    }

}
