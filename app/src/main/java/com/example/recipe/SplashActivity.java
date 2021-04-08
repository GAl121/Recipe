package com.example.recipe;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {

    public ProgressBar progressBar;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.splash_screen_progress_bar);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.BLACK));
        progressBar.setMax(200);
        progressBar.setProgress(0);

        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 200; i++) {
                        progressBar.setProgress(i);
                        sleep(5);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        thread.start();
    }
}