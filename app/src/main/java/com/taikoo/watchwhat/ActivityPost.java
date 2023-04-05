package com.taikoo.watchwhat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.taikoo.utils.FileHelper;
import com.taikoo.watchwhat.RpApi.MovieInfoPlaybill;
import com.taikoo.watchwhat.RpApi.rpApi;

import java.io.File;
import java.io.FileWriter;

import VideoHandle.CmdList;
import VideoHandle.EpEditor;
import VideoHandle.OnEditorListener;

public class ActivityPost extends ActivityBase {
    static ActivityPost.VideoInfo viSel = null;

    private static final String TAG = "ActivityPost";
    String mDirPost = "";
    boolean isPublishing = false;//发布中
    boolean isGetMovInfo = false;//已经读取到视频时长等信息

    ProgressBar mProgressBar = null;
    TextView mTvProgress = null;
    EditText mTextMemo = null;
    CheckBox mCheckBox_accept_policy = null;
    Button mBtnPublish = null;

    TextView mBtn_add_tag = null;
    TextView mBtn_clear_memo = null;

    VideoView mPlayer = null;
    MediaPlayer mMPlay = null;
    SeekBar mSeekBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_post);

        mTextMemo = findViewById(R.id.editTextTextMultiLine);
        mBtn_add_tag = findViewById(R.id.btn_add_tag);
        mBtn_add_tag.setOnClickListener(v -> {
            mTextMemo.setText(mTextMemo.getText() + " #");
            mTextMemo.setSelection(mTextMemo.getText().length());
            mTextMemo.setFocusable(true);
            mTextMemo.setFocusableInTouchMode(true);
            mTextMemo.requestFocus();
            mTextMemo.requestFocusFromTouch();
        });
        mBtn_clear_memo = findViewById(R.id.btn_clear_memo);
        mBtn_clear_memo.setOnClickListener(v -> mTextMemo.setText(""));

        mPlayer = findViewById(R.id.post_player);
        mPlayer.setOnPreparedListener(mp -> {
            mMPlay = mp;
            mSeekBar.setVisibility(View.VISIBLE);
            mSeekBar.setMax(mp.getDuration());
            mSeekBar.setProgress(1000);
        });
        mSeekBar = findViewById(R.id.post_seekBar);
        mSeekBar.setVisibility(View.INVISIBLE);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMPlay != null) {
                    mMPlay.seekTo(progress);
                    viSel.coverPosition = progress;
                    if (mMPlay.isPlaying()) {
                        mMPlay.pause();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mProgressBar = findViewById(R.id.post_progressBar2);
        mTvProgress = findViewById(R.id.post_tv_progress);

        mBtnPublish = findViewById(R.id.post_btn_publish);
        mBtnPublish.setOnClickListener(v -> {
            goPublish();
//            GetInfoVolume();
        });


        mCheckBox_accept_policy = findViewById(R.id.checkBox_accept_policy);
        mCheckBox_accept_policy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setPublishBtn();
            }
        });

        String dirCache = getApplicationContext().getCacheDir().toString();
        mDirPost = dirCache + "/post/";

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        goStart();
        BuildTags();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        viSel = null;
    }

    LinearLayout mBoxTags = null;

    void BuildTags() {
        if (mBoxTags != null) return;
        mBoxTags = findViewById(R.id.box_tags);

        api.MovieService ms = new api.MovieService();
        String tagsStr = ms.myPostTags();

        Log.d(TAG, "onStart: tags " + tagsStr);

        String[] tags = tagsStr.split(",");

        for (int i = 0; i < tags.length; i++) {
            TextView tv = new TextView(getApplicationContext());
            tv.setText(tags[i]);

            tv.setPadding(8, 2, 8, 2);
//            tv.setTextColor();

            tv.setOnClickListener(v -> {
                TextView tvv = (TextView) v;
                mTextMemo.setText(mTextMemo.getText() + " " + tvv.getText());
                mTextMemo.setSelection(mTextMemo.getText().length());
                mTextMemo.setFocusable(true);
                mTextMemo.setFocusableInTouchMode(true);
                mTextMemo.requestFocus();
                mTextMemo.requestFocusFromTouch();
            });
            mBoxTags.addView(tv);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode);
        if (requestCode == 403) {
            goStart();
        }

        if (requestCode == 66 && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

//    String vdoPath = data.getStringExtra("path");

            Cursor cursor = this.getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String vdoPath = cursor.getString(columnIndex);
            cursor.close();

            createTask(vdoPath);

            goStart();
        } else {
            ActivityPost.this.onBackPressed();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void goPublish() {
        isPublishing = true;
        setPublishBtn();
//        Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG);
        new Handler().post(() -> {
            try {
                SaveCover();
                SaveMemo();
                GoEncode();
                waitForDone();
            } catch (Exception e) {
                Log.e(TAG, "goPublish: ", e);
            }
        });
    }

    @UiThread
    public void setPublishBtn() {
        boolean enable =mCheckBox_accept_policy.isChecked() && isGetMovInfo && !isPublishing;
        mBtnPublish.setEnabled(enable);
    }


    @UiThread
    public void waitForDone() {
        new Handler().postDelayed(() -> {
            if (viSel.movDone) {
                Log.d(TAG, "waitForDone: POST DONE !!");
                ActivityPost.this.onBackPressed();
                mTvProgress.setText("ALL DONE!!");
                isPublishing = false;
                setPublishBtn();
                viSel = null;
            } else {
                waitForDone();
            }
        }, 1000);
    }

    Handler hd_openXc = null;

    void goStart() {
        if (!HavePermission()) {
            return;
        }

        CheckPostDir();

        if (hd_openXc == null) {
            hd_openXc = new Handler();
            hd_openXc.postDelayed(() -> {
                if (viSel == null) {
                    OpenXc();
                } else {

                }
            }, 100);
        }
    }


    public boolean HavePermission() {
        boolean haveP = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            haveP = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            haveP = false;
        }
        if (!haveP) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission_group.STORAGE
                    }, 403);
        }

        return haveP;
    }


    private void CheckPostDir() {
        String dirPost = mDirPost;
        File dir = new File(dirPost);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    private void createTask(String vdoPath) {
        viSel = new VideoInfo();
        viSel.fileName = System.currentTimeMillis() + "";
        viSel.videoPath = vdoPath;
        viSel.ListenerCover = lisCover;
        viSel.ListenerVideo = lis;

        CopyToCache();
        GetInfo();

        mPlayer.setVisibility(View.VISIBLE);
        mPlayer.setVideoPath(viSel.videoPath);
        mPlayer.start();

        new Handler().postDelayed(() -> {
            mPlayer.seekTo(1000);
            mPlayer.pause();

            setPublishBtn();
        }, 100);
    }


    void OpenXc() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 66);
    }


    public void GetInfo() {
        Log.d(TAG, "GetInfo: ");
        // TODO: 2022/11/21 获取视频时长，高宽
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(viSel.videoPath);

            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); //宽
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); //高
            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);//视频的方向角度
            long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;//视频的长

            viSel.width = Integer.parseInt(width);
            viSel.height = Integer.parseInt(height);
            viSel.rotation = Integer.parseInt(rotation);
            viSel.duration = duration;

            isGetMovInfo = true;
            setPublishBtn();
            Log.d(TAG, "GetInfo: width:" + width + " height:" + height + " rotation:" + rotation + " duration:" + duration);
        } catch (Exception ex) {
            Log.e(TAG, "GetInfo: error: " + ex.getMessage());
        }
    }

    void GetInfoVolume() {
        CmdList sbArg = new CmdList();
        sbArg.append("-i").append(viSel.videoPath);
//-filter_complex volumedetect -c:v copy -f null /dev/null
        sbArg.append("-filter_complex").append("volumedetect");
        sbArg.append("-c:v").append("copy");
        sbArg.append("-f").append("null /dev/null");

        EpEditor.execCmd(sbArg.toString(), viSel.duration, lisVol);

//        Debug.startMethodTracing(getApplicationContext().getCacheDir().toString()+ "vo.trace");

    }

    public void CopyToCache() {
        File fileRes = new File(viSel.videoPath);
        viSel.memo = FileHelper.getPrefix(fileRes);
        String outPath = mDirPost + viSel.fileName + "_res" + FileHelper.getExt(fileRes);
        FileHelper.copy(fileRes, new File(outPath));
        viSel.videoPath = outPath;
        mTextMemo.setText(viSel.memo);
    }


    public void GoEncode() throws Exception {
        while (!viSel.coverDone) {
            Thread.sleep(500);
        }

        String outPath = mDirPost + viSel.fileName + ".mp4";
        Log.d(TAG, "GoEncode: path: " + outPath);

        CmdList sbArg = new CmdList();

//        sbArg.append("-ss " + positionStart + "s");
        sbArg.append("-i").append(viSel.videoPath);
//        sbArg.append("-t " + duration + "s");

        {//audio
            sbArg.append("-c:a").append("aac");
            sbArg.append("-ac").append(1);
            sbArg.append("-ar").append(11025);
            sbArg.append("-b:a").append("16k");
        }

        {//video

            sbArg.append("-c:v").append("libx264"); //code

            if (viSel.width > viSel.height) {
                sbArg.append("-vf").append("scale=-2:720");
            } else {
                sbArg.append("-vf").append("scale=720:-2");
            }

            sbArg.append("-r").append(30);  //frame rate

            sbArg.append("-b:v").append("1480k");//980
            sbArg.append("-maxrate:v").append("1890k");//1490
            sbArg.append("-minrate:v").append("50k");

            sbArg.append("-profile:v").append("main");//画质级别
            sbArg.append("-bsf:v").append("h264_mp4toannexb");
        }
        sbArg.append("-y");
        sbArg.append(outPath);

        EpEditor.execCmd(sbArg.toString(), viSel.duration, lis);
    }


    void SaveMemo() throws Exception {
        Log.d(TAG, "SaveMemo: ");
        String outPath = mDirPost + viSel.fileName + ".txt";

        viSel.memo = mTextMemo.getText().toString();

        FileWriter fWriter = new FileWriter(outPath, true);
        fWriter.write(viSel.memo);
        fWriter.flush();
        fWriter.close();
    }

    @UiThread
    void SaveDone() throws Exception {
        Log.d(TAG, "SaveDone: ");
        if (viSel.movDone) {
            Log.d(TAG, "SaveDone: 0");
            String outPath = mDirPost + viSel.fileName + ".done";
            File f = new File(outPath);
            if (!f.exists()) {
                f.createNewFile();
            }
            Log.d(TAG, "SaveDone: 1");
            rpApi.apiPost();
            Log.d(TAG, "SaveDone: 2");
            MovieInfoPlaybill.RemovePlayed();
        }
    }

    void SaveCover() throws Exception {

        String outPath = mDirPost + viSel.fileName + ".jpg";

        float ff = viSel.coverPosition;

        CmdList sbArg = new CmdList();
        sbArg.append("-ss").append(ff / 1000);
        sbArg.append("-i").append(viSel.videoPath);
        if (viSel.width > viSel.height) {
            sbArg.append("-vf").append("scale=-2:400");
        } else {
            sbArg.append("-vf").append("scale=400:-2");
        }
        sbArg.append("-r").append(1);
        sbArg.append("-vframes").append(1);
        sbArg.append("-an");
        sbArg.append("-f").append("mjpeg");
        sbArg.append(outPath);

        EpEditor.execCmd(sbArg.toString(), viSel.duration, lisCover);
    }


    OnEditorListener lis = new OnEditorListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess: ");

            /* Toast.makeText(getContext(), "成功", Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
            Intent v = new Intent(Intent.ACTION_VIEW);
            v.setDataAndType(Uri.parse(outPath), "video/mp4");
            startActivity(v);*/

            viSel.movDone = true;
            try {
                SaveDone();
            } catch (Exception ex) {
                Log.e(TAG, "onSuccess: ", ex);
            }
            new File(viSel.videoPath).delete();
        }

        @Override
        public void onFailure() {
            Log.e(TAG, "onFailure: ");
            new File(viSel.videoPath).delete();
        }

        @Override
        public void onProgress(float v) {
            Log.d(TAG, "onProgress: " + v);
            mTvProgress.setText(" " + (int) (v * 100) + "%");
            mProgressBar.setProgress((int) (v * 100));
//            mProgressDialog.setProgress((int) (v * 100));
        }
    };
    OnEditorListener lisCover = new OnEditorListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess: ");
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                Log.e(TAG, "onSuccess: ", ex);
            }
            viSel.coverDone = true;
        }

        @Override
        public void onFailure() {
            Log.e(TAG, "onFailure: ");
            viSel.coverDone = true;
        }

        @Override
        public void onProgress(float v) {
            Log.d(TAG, "onProgress: " + v);
        }
    };
    OnEditorListener lisVol = new OnEditorListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess: ");
//            Debug.stopMethodTracing();
        }

        @Override
        public void onFailure() {
            Log.e(TAG, "onFailure: ");
//            Debug.stopMethodTracing();
        }

        @Override
        public void onProgress(float v) {
            Log.d(TAG, "onProgress: " + v);
        }
    };

    class VideoInfo {
        String videoPath;
        int width;
        int height;
        int rotation;
        long duration;
        long coverPosition;  //微妙
        String fileName;
        String memo;

        boolean movDone;
        boolean coverDone;

        OnEditorListener ListenerCover;
        OnEditorListener ListenerVideo;
    }
}