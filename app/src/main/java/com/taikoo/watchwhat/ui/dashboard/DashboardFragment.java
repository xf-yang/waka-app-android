package com.taikoo.watchwhat.ui.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.taikoo.watchwhat.R;


import VideoHandle.CmdList;
import VideoHandle.EpEditor;
import VideoHandle.OnEditorListener;

import static android.app.Activity.RESULT_OK;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";
    //    private DashboardViewModel dashboardViewModel;
    ProgressBar mProgressBar = null;
    TextView mTextView = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // clone the inflater using the ContextThemeWrapper
//        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_WatchWhat_dark);
//        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

//        dashboardViewModel =
//                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        mProgressBar = root.findViewById(R.id.post_progressBar2);
        mTextView = root.findViewById(R.id.post_tv_progress);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}