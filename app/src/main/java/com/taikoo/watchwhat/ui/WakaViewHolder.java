package com.taikoo.watchwhat.ui;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import com.taikoo.watchwhat.R;
import com.taikoo.watchwhat.RpApi.Http206MediaDataSourceV2;
import com.taikoo.watchwhat.RpApi.MovieInfoPlaybill;
import com.taikoo.watchwhat.RpApi.rpApi;

public class WakaViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "WakaViewHolder";

    //    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
//    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private int mCurrentState = STATE_IDLE;

    MovieInfoPlaybill.MovieInfo mItem;
    final View mView;
    final View mLayer_info;
    final View mLayer_mask;
    final TextView cMemo;
    final ImageView mImageCover;
    final FrameLayout mItemBox;
    final SurfaceView mSurfaceView;
    MediaPlayer mMediaPlayer = null;
    SurfaceHolder mSurfaceHolder;

    ProgressBar mLoading;
    ImageButton mBtnPlay;
    final ImageButton mBtnMore;
    final ImageButton mBtnReport;
    final ImageButton mBtnLike;

    View mBoxMenu;

    boolean isCountPlay;
    int mRePlayCount;

    public WakaViewHolder(View view) {
        super(view);
        Log.d(TAG, " new : " + view);

        mView = view;

        mImageCover = view.findViewById(R.id.imageView);
        cMemo = view.findViewById(R.id.item_memo);
        mLayer_info = view.findViewById(R.id.info_layer);
        mLayer_mask = view.findViewById(R.id.layer_mask);
        mSurfaceView = view.findViewById(R.id.surfaceView);

        mItemBox = view.findViewById(R.id.bottom_layer);
        mLoading = view.findViewById(R.id.player_loading);
        mBoxMenu = view.findViewById(R.id.box_menu);

        mBtnPlay = view.findViewById(R.id.btn_play);
        mBtnMore = view.findViewById(R.id.btn_more);
        mBtnReport = view.findViewById(R.id.btn_report);
        mBtnLike = view.findViewById(R.id.btn_like);

        {
            mSurfaceView.setOnClickListener(on_view_click);
            mBtnPlay.setOnClickListener(on_view_click);
            mBtnMore.setOnClickListener(on_view_click);
            mBtnReport.setOnClickListener(on_view_click);
            mBtnLike.setOnClickListener(on_view_click);
        }
    }

    public void BindToView(MovieInfoPlaybill.MovieInfo data) {
        mItem = data;
        isCountPlay = false;
        mRePlayCount = 1;
        mCurrentState = STATE_IDLE;

        InitView();
        new Thread(() -> mItemBox.post(this::initPlayer)).start();
    }

    @UiThread
    private void InitView() {
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSHCallback);
        mBoxMenu.setVisibility(View.GONE);
        mLoading.setVisibility(VISIBLE);
        mBtnPlay.setVisibility(INVISIBLE);
        cMemo.setText(mItem.Title);
    }


    void loadCover() {
        Drawable drawable = loadImageFromNativeApi(mItem.CoverId);
        if (drawable != null) {
            int img_w = drawable.getIntrinsicWidth();
            int img_h = drawable.getIntrinsicHeight();
            int img_box_w = mImageCover.getWidth();
            int to_h = img_box_w * img_h / img_w;
            mImageCover.setMinimumHeight(to_h);
            mImageCover.setImageDrawable(drawable);
        }
    }

    void initPlayer() {
        Log.d(TAG, "Play: " + mItem.VideoId);

        if (mMediaPlayer == null && mCurrentState == STATE_IDLE) {
            mCurrentState = STATE_PREPARING;
            try {
                mMediaPlayer = new MediaPlayer();
                Uri mUri = Uri.parse("http://localhost:4050/video/" + mItem.VideoId + ".mp4");
                Http206MediaDataSourceV2 mMyMediaDataSource_2 = new Http206MediaDataSourceV2(mUri);
                mMediaPlayer.setDataSource(mMyMediaDataSource_2);
                mMediaPlayer.setDisplay(mSurfaceHolder);
                mMediaPlayer.setOnPreparedListener(listener_MP_Prepared);
                mMediaPlayer.setOnBufferingUpdateListener(listener_MP_onBuffering);
                mMediaPlayer.setOnCompletionListener(listener_MP_onCompletion);
                mMediaPlayer.setOnInfoListener(listener_MP_OnInfo);
                mMediaPlayer.setOnErrorListener(listener_MP_OnError);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e(TAG, "initPlayer: ", e);
            }
        }
    }

    //==============================================================================================

    MediaPlayer.OnPreparedListener listener_MP_Prepared = mp -> {
        Log.d(TAG, "onPrepared." + mp.getVideoWidth() + "x" + mp.getVideoHeight());
        mCurrentState = STATE_PREPARED;
        new android.os.Handler().postDelayed(this::goPlay, 50);
        new android.os.Handler().postDelayed(() -> ResetVideoViewLayout(), 100);
    };

    MediaPlayer.OnBufferingUpdateListener listener_MP_onBuffering = (mp, percent) -> Log.d(TAG, "onBufferingUpdate: " + percent + "%");

    MediaPlayer.OnCompletionListener listener_MP_onCompletion = (mp) -> {
        Log.d(TAG, "OnCompletionListener. " + mItem.Id);
        countPlay(true);
        if (mRePlayCount > 2) {
            mMediaPlayer.seekTo(0);
            goPause();
        } else {
            mMediaPlayer.start();
            isCountPlay = false;
        }
        mRePlayCount++;
    };

    MediaPlayer.OnInfoListener listener_MP_OnInfo = (mp, what, extra) -> {
        Log.d(TAG, "OnInfoListener. what:" + what + " extra:" + extra);
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            mLoading.setVisibility(INVISIBLE);
        }
        return true;
    };

    MediaPlayer.OnErrorListener listener_MP_OnError = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mLoading.setVisibility(INVISIBLE);
            return false;
        }
    };

    //==============================================================================================

    private void countPlay(boolean isCompleted) {
        if (!isCountPlay) {
            float progress = 100;
            if (!isCompleted) {
                progress = (float) (mMediaPlayer.getCurrentPosition() * 100) / (float) mMediaPlayer.getDuration();
            }
            Log.d(TAG, "countPlay: " + progress + "  " + mItem.Id);
            new api.MovieService().countPlay(mItem.Id, progress);
            isCountPlay = true;
        }
    }

    @UiThread
    private void onReportButtonClick() {
        Context ctx = mView.getContext();
        String title = ctx.getResources().getString(R.string.player_report_title);
        String message = ctx.getResources().getString(R.string.player_report_message);
        String btn_title = ctx.getResources().getString(R.string.player_report_button);
        AlertDialog alertDialog = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_report)
                .setPositiveButton(btn_title, (dialog, which) -> {
                    rpApi.report(mItem.Id);
                    MovieInfoPlaybill.RemoveItem(mItem); // 删除本地列表成员
                    tryCloseMenu();
                })
                .create();
        alertDialog.show();
    }

    @UiThread
    private void onLikeButtonClick() {
        tryCloseMenu();
    }

    @UiThread
    private void onSurfaceViewClick() {
        if (mCurrentState != STATE_PAUSED) {
            goPause();
        } else {
            goPlay();
        }
    }

    @UiThread
    private void goPlay() {
        Log.d(TAG, "goPlay. ");
        mItem.Played = true;
        if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_PREPARED) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            mBtnPlay.setVisibility(INVISIBLE);

            WakaRecyclerViewAdapter.CurrentViewHolder = WakaViewHolder.this;
        }
    }

    private void goPause() {
        mMediaPlayer.pause();
        mCurrentState = STATE_PAUSED;
        mBtnPlay.setVisibility(VISIBLE);
    }

    public void StopPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "Player.surface.Created: " + mItem.Id);
            if (WakaRecyclerViewAdapter.CurrentViewHolder != null && WakaRecyclerViewAdapter.CurrentViewHolder != WakaViewHolder.this) {
                WakaRecyclerViewAdapter.CurrentViewHolder.StopPlay();
            }
            initPlayer();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.d(TAG, "Player.surface.Changed: " + mItem.Id);
            new android.os.Handler().postDelayed(() -> ResetVideoViewLayout(), 400);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "Player.surface.Destroyed: " + mItem.Id);
            countPlay(false);
            isCountPlay = false;
            release();
        }
    };

    @UiThread
    void ResetVideoViewLayout() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.getVideoHeight() <= 0) return;

        int whr_box = mLayer_mask.getWidth() * 1000 / mLayer_mask.getHeight();
        int whr_player = mMediaPlayer.getVideoWidth() * 1000 / mMediaPlayer.getVideoHeight();

        int newWidth;
        int newHeight;

        if (whr_box > whr_player) {
            newHeight = mLayer_mask.getHeight();
            newWidth = newHeight * mMediaPlayer.getVideoWidth() / mMediaPlayer.getVideoHeight();
        } else {
            newWidth = mLayer_mask.getWidth();
            newHeight = newWidth * mMediaPlayer.getVideoHeight() / mMediaPlayer.getVideoWidth();
        }

        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        if (lp.width == newWidth && lp.height == newHeight) return;

        Log.d(TAG, "ResetVideoView: " + mLayer_mask.getWidth() + "x" + mLayer_mask.getHeight());
        lp.width = newWidth;
        lp.height = newHeight;
        mSurfaceView.setLayoutParams(lp);
    }


    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
//            mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
        }
    }

    View.OnClickListener on_view_click = new View.OnClickListener() {

        @SuppressLint("NonConstantResourceId")
        @UiThread
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_play:
                    goPlay();
                    break;
                case R.id.btn_more:
                    run_more.run();
                    break;
                case R.id.btn_report:
                    onReportButtonClick();
                    break;
                case R.id.btn_like:
                    onLikeButtonClick();
                    break;
                case R.id.surfaceView:
                    onSurfaceViewClick();
                    break;
                default:
            }
        }
    };

    Runnable run_more = () -> {
        if (mBoxMenu.isShown()) {
            mBoxMenu.setVisibility(View.GONE);
        } else {
            mBoxMenu.setVisibility(VISIBLE);
        }
    };

    private void tryCloseMenu() {
        if (mBoxMenu.isShown()) {
            mBoxMenu.setVisibility(View.GONE);
        }
    }

    //===============================================================================================

    private static Drawable loadImageFromNativeApi(String fileId) {
        Drawable drawable = null;
        try {
            java.io.InputStream is = new com.taikoo.watchwhat.RpApi.NativeApiInputSteam(fileId).GetInputStream();
            drawable = Drawable.createFromStream(is, fileId + "aa.jpg");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        if (drawable == null) {
            Log.d(TAG, "null drawable");
        }
        return drawable;
    }


}
