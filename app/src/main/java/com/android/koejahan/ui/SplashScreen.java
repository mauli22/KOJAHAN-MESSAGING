package com.android.koejahan.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.koejahan.R;

public class SplashScreen extends AppCompatActivity {
    private int waktu_loading=2500;
    //4000=4 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("SPLASH","SPLASH BERJALAN DENGAN BAIK");


        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //setelah loading maka akan langsung berpindah ke home activity
                Intent start =new Intent(SplashScreen.this, Slider.class);
                startActivity(start);
                finish();

            }
        },waktu_loading);
    }


}
