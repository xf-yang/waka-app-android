package com.taikoo.watchwhat.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.taikoo.watchwhat.R;


public class FragmentHome extends Fragment {
    private static final String TAG = "FragmentHome";

    RecyclerView mRecyclerView;
    WakaRecyclerViewAdapter mListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView ");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = view.findViewById(R.id.list);
        WakaLinearLayoutManager myLinearLayoutManager =new WakaLinearLayoutManager(container.getContext());
        mRecyclerView.setLayoutManager(myLinearLayoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        this.mListAdapter = new WakaRecyclerViewAdapter();
        mRecyclerView.setAdapter(this.mListAdapter);
    }

}