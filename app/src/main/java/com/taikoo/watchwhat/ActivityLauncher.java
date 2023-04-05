package com.taikoo.watchwhat;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;

import com.taikoo.watchwhat.RpApi.MovieInfoPlaybill;

public class ActivityLauncher extends ActivityBase {
    private static final String TAG = "LauncherActivity";


    int waitCount = 0;
    int minimalMovieNum = 5;//进入播放前至少拥有的视频数, 不要超过后端推送的量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        new Handler().post(run_StartRp);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();

        TextView tv_info = this.findViewById(R.id.launcher_info);
        tv_info.setText(R.string.connecting);

        new Handler().postDelayed(run_LoadPlayBill, 1000);
    }

    void LoadPlaybill() {
        Log.d(TAG, "LoadPlaybill: ");
        int movNum = MovieInfoPlaybill.GetSize();

        Log.e(TAG, "LoadPlaybill:movNum:" + movNum);
        if (movNum < minimalMovieNum) {  //至少加载5条
            if (waitCount >= 3) {
                long NodeCount = api.Api.onlineNodes();
                if (NodeCount < 1) {
                    Log.e(TAG, "LoadPlaybill:NodeCount:" + NodeCount);
                    GoDashboard();
                    return;
                }
            }
            new Handler().postDelayed(run_LoadPlayBill, 1000);
            ShowPr(movNum);
        } else {
            GoMain();
        }
    }

    Runnable run_LoadPlayBill = () -> {
        Log.d(TAG, "run_LoadPlayBill ");
        LoadPlaybill();
    };

    @SuppressLint("StringFormatMatches")
    @UiThread
    void ShowPr(int movNum) {
        TextView tv_info = this.findViewById(R.id.launcher_info);
        //CharSequence text_info = tv_info.getText();

        tv_info.setText(String.format(getString(R.string.updating), movNum, minimalMovieNum));

        waitCount++;
        int p = waitCount;
        Log.d(TAG, "ShowPr: " + p);

        // ProgressBar pb = this.findViewById(R.id.progressBar);
        // pb.setProgress(p);
    }


    @UiThread
    void GoMain() {
        Log.d(TAG, "run_GoMain: ");
        Intent i = new Intent(getApplicationContext(), ActivityMain.class);
        i.addFlags(FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    }

    @UiThread
    void GoDashboard() {
        Log.d(TAG, "GoDashboard: ");
        Intent i = new Intent(getApplicationContext(), ActivityUser.class);
        i.addFlags(FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("action", "diagnose");
        getApplicationContext().startActivity(i);
    }

    Runnable run_StartRp = () -> {
        StartRp();
        Log.d(TAG, "StartRp Done ");
        MovieInfoPlaybill.Start();
        Log.d(TAG, "run_ MovieInfoPlaybill Done ");
    };

    boolean _isRpRun = false;

    void StartRp() {
        if (_isRpRun) return;
        _isRpRun = true;
        try {
            android.content.Context cont = this.getApplicationContext();
            String dirRoot = cont.getDatabasePath("root").toString();
            String dirData = cont.getDatabasePath("data").toString();
            String dirCache = cont.getCacheDir().toString();

//            , MovieManager , DownloadManager,PackDownloader,MovieDownloader
//            api.Api.hideLog("FileReador,KanshaUrlAdapter, MovieManager");
//            api.Api.setDownloadThread(0);

            api.Api.start(dirRoot, dirData, dirCache);
            api.Api.startWeb();
        } catch (Exception ex) {
            Log.e(TAG, "StartRp: " + ex.getMessage());
            _isRpRun = false;
        }
    }


}