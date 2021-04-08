package com.example.recipe;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class LogoutActivity extends AppCompatActivity {
    public ImageView aniView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);



        aniView = (ImageView) findViewById(R.id.logout_animation);
        ObjectAnimator mover = ObjectAnimator.ofFloat(aniView, "x", 1200f);
        mover.setDuration(2000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mover);
        animatorSet.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(2000);
                        Intent intent = new Intent (LogoutActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}