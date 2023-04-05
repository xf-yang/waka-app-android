package com.taikoo.watchwhat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.taikoo.watchwhat.RpApi.MovieInfoPlaybill;

import java.util.Locale;

import api.Api;

public class ActivityBase extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        run_StartRp.run();
    }

    Runnable run_StartRp = () -> {
        try {
            Api.getSdkVersion();
        } catch (Exception ex) {

        }
    };


    void aaa(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences("key", MODE_PRIVATE);
        int language = sharedPreferences.getInt("language", 0);

        Locale locale;

        for (int i = 0; i < context.getResources().getConfiguration().getLocales().size(); i++) {
            locale = context.getResources().getConfiguration().getLocales().get(i);
        }

    }

    void LoadLocation() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        String language = locale.getLanguage() + "-" + locale.getCountry();

    }

}
