package com.taikoo.watchwhat.ui;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taikoo.watchwhat.R;
import com.taikoo.watchwhat.RpApi.MovieInfoPlaybill;


public class WakaRecyclerViewAdapter extends RecyclerView.Adapter<WakaViewHolder> {

    private static final String TAG = "MyItemRecyclerViewAdapter";

    @SuppressLint("StaticFieldLeak")
    static WakaViewHolder CurrentViewHolder = null;

    public WakaRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public WakaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recommend_item, parent, false);
        return new WakaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WakaViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position + "/" + MovieInfoPlaybill.GetSize());
        holder.BindToView(MovieInfoPlaybill.GetItem(position));
    }

    @Override
    public void onViewRecycled(final WakaViewHolder holder) {
        Log.d(TAG, "onViewRecycled: ");
        holder.StopPlay();
    }

    @Override
    public int getItemCount() {
        return MovieInfoPlaybill.GetSize();
    }

//===============================================================================================





}