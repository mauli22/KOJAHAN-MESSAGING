package com.android.koejahan.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.android.koejahan.MainActivity;
import com.android.koejahan.R;
import com.android.koejahan.data.SharedPreferenceHelper;

public class AboutActivity extends AppCompatActivity {
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferenceHelper.getInstance(this).cekDark()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.d("about","darkmode");
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.d("about","lightmode");
        }
        setContentView(R.layout.activity_about1);
        handler.postDelayed(runnable,1500);
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_about);
        }
    };
}
