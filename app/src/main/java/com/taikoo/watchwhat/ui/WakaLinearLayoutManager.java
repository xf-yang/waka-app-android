package com.taikoo.watchwhat.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class WakaLinearLayoutManager extends LinearLayoutManager implements RecyclerView.OnChildAttachStateChangeListener {
    private static final String TAG = "WakaLinearLayoutManager";

    //传进来的监听接口类
    private OnViewPagerListener onViewPagerListener;
    //解决吸顶或者洗低的对象
    private final PagerSnapHelper pagerSnapHelper;


    public WakaLinearLayoutManager(Context context) {
        super(context);
        pagerSnapHelper = new PagerSnapHelper();
    }


    public void setOnViewPagerListener(OnViewPagerListener li){
        onViewPagerListener =li;
    }

    /**
     * 当MyLayoutManager完全放入到RecyclerView中的时候会被调用
     */
    @Override
    public void onAttachedToWindow(RecyclerView view) {
        view.addOnChildAttachStateChangeListener(this);
        pagerSnapHelper.attachToRecyclerView(view);
        super.onAttachedToWindow(view);
        Log.d(TAG, "onAttachedToWindow: ");
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }


    /**
     * 监听滑动的状态
     */
    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {//现在拿到的就是当前显示的这个item
            View snapView = pagerSnapHelper.findSnapView(this);
            assert snapView != null;
            if (onViewPagerListener != null) {
//                onViewPagerListener.onPageSelected(snapView);
            }
        }
        super.onScrollStateChanged(state);
    }

    //====================================================================

    /**
     * 将Item添加进来的时候  调用这个方法
     */
    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        if (onViewPagerListener != null) {
            onViewPagerListener.onPageSelected(view);
        }
    }

    /**
     * 将Item移除出去的时候  调用这个方法
     */
    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        if (onViewPagerListener != null) {
            onViewPagerListener.onPageRelease(view);
        }
    }

    public interface OnViewPagerListener {
        //停止播放的监听方法
        void onPageRelease(View itemView);

        //播放的监听方法
        void onPageSelected(View itemView);
    }

}



