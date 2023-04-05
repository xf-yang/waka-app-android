package com.taikoo.watchwhat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;

import java.util.Objects;

public class ActivityUser extends ActivityBase {
    private static final String TAG = "ActivityUser";
    WebView mWebView;
    String action = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }

        mWebView = findViewById(R.id.mWebView);
        mWebView.setWebViewClient(new MyWebViewClient());

        action = getIntent().getStringExtra("action");
        if (action == null) action = "";
        Log.d(TAG, "onCreate: action:" + action);

    }


    @Override
    protected void onStart() {
        super.onStart();

        mWebView.loadUrl("http://localhost:4050/dashboard/" + action);

    }

    @UiThread
    void GoMain() {
        this.onBackPressed();
    }



    class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            Log.d(TAG, "onPageStarted: "+url);
            if (Objects.equals(url, "http://home/")){
                GoMain();return;
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url= request.getUrl().toString();
//            Log.d(TAG, "shouldOverrideUrlLoading: "+url);
            if (Objects.equals(url, "http://home/")){
                view.goBack();
                GoMain();return true;
            }
           return super.shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

    }
}