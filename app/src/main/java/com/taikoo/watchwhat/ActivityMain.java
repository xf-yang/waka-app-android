package com.taikoo.watchwhat;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.taikoo.watchwhat.RpApi.MovieInfoPlaybill;

public class ActivityMain extends ActivityBase {
    private static final String TAG = "ActivityMain";

    View vHost;
    View vBoxFragment;
    View vNaviBar;

    int flagNormalRes = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    int flagNormal = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        if (false) {
            NavController navController = Navigation.findNavController(this, R.id.host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        vHost = findViewById(R.id.host_fragment);
        vBoxFragment = findViewById(R.id.box_fragment);
        vNaviBar = findViewById(R.id.my_navi_bar);

        changeViewC();

        findViewById(R.id.btn_post).setOnClickListener(btn_click);
        findViewById(R.id.btn_user).setOnClickListener(btn_click);

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart: ");
        MovieInfoPlaybill.RemovePlayed();

        View v = findViewById(R.id.host_fragment);
        v.postDelayed(() -> Log.d(TAG, "ResetVideoView: Action :" + v.getWidth() + "x" + v.getHeight()), 500);
        new Handler().postDelayed(this::changeViewC, 10);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: ");
        super.onConfigurationChanged(newConfig);

        Runnable r;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            r = this::changeViewR;
        } else {
            r = this::changeViewC;
        }
        new Handler().postDelayed(r, 10);
    }

    @UiThread
    void changeViewR() {
        vNaviBar.setVisibility(View.INVISIBLE);
        vHost.setPadding(0, 0, 0, 0);
        vHost.setSystemUiVisibility(flagNormal | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @UiThread
    void changeViewC() {
        vNaviBar.setVisibility(View.VISIBLE);
        int navHeight = vNaviBar.getHeight();
        int stateBarHeight = getStateBarHeight();
        vHost.setPadding(0, stateBarHeight, 0, navHeight);
        vHost.setSystemUiVisibility(flagNormal);
    }

    @UiThread
    int getStateBarHeight() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        Log.d(TAG, "rectangle: " + rectangle);
        return rectangle.top;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
    }

    View.OnClickListener btn_click = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_post:
                    run_GoPost.run();
                    break;
                case R.id.btn_user:
                    run_GoUser.run();
                    break;
            }
        }
    };

    Runnable run_GoPost = () -> {
        Log.d(TAG, "run_GoPost: ");
        Intent i = new Intent(getApplicationContext(), ActivityPost.class);
        i.addFlags(FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    };

    Runnable run_GoUser = () -> {
        Log.d(TAG, "run_GoPost: ");
        Intent i = new Intent(getApplicationContext(), ActivityUser.class);
        i.addFlags(FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    };

}