package com.taikoo.watchwhat.RpApi;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class rpApi {
    private static final String TAG = "rpApi";
    public static void index(GotDataRunnable<List<MovieInfoPlaybill.MovieInfo>> r) {
        api.MovieService srv=  new api.MovieService();
        try {
            String json= srv.getMovieList_Recommend();
            List<MovieInfoPlaybill.MovieInfo> rtData = decodeMovieList(json);
            r.gotData(rtData, null);
        } catch (Exception e) {
            e.printStackTrace();
            r.gotData(null, e);
        }
    }

    public static void quitPlay(String videoId) {
        api.FileReador fr=  new api.FileReador(videoId);
        fr.stopRead();
    }

    public static void report(String movId) {
        Log.d(TAG, "report");
        String apiUrl = "http://localhost:4050/api/report/"+movId;
        apiGet(apiUrl, (responseData, ex) -> {
            if (ex != null) {
            } else {
            }
        });
    }

    private static Drawable loadImageFromNetwork(String fileId) {
        String imageUrl = "http://localhost:4050/cover/" + fileId + ".jpg";
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromStream(
                    new URL(imageUrl).openStream(), imageUrl);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        if (drawable == null) {
            Log.d(TAG, "null drawable");
        }
        return drawable;
    }

    //=================================================================================================

    public static void detail(GotDataRunnable<MovieDetail> r) {
        Log.d(TAG, "detail");
        String apiurl = "http://localhost:4050/api/quitplay";
        apiGet(apiurl, new ResponseRunnable() {
            @Override
            public void run(String responseData, Exception ex) {
                if (ex != null) {
                    r.gotData(null, ex);
                } else {
                    try {
                        JSONObject obj = new JSONObject(responseData);
                        MovieDetail mdt = new MovieDetail();
                        mdt.MovId = obj.getString("mvid");
                        r.gotData(mdt, null);
                    } catch (Exception ex2) {
                        r.gotData(null, ex2);
                    }
                }
            }
        });
    }

    public static void index2(GotDataRunnable<List<MovieInfoPlaybill.MovieInfo>> r) {
        Log.d(TAG, "index");
        String apiUrl = "http://localhost:4050/api/index";
        apiGet(apiUrl, (responseData, ex) -> {
            if (ex != null) {
                r.gotData(null, ex);
            } else {
                try {
                    List<MovieInfoPlaybill.MovieInfo> rtdata = decodeMovieList(responseData);
                    r.gotData(rtdata, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    r.gotData(null, e);
                }
            }
        });
    }

    public static void apiPost() {
        Log.d(TAG, "index");
        String apiUrl = "http://localhost:4050/api/post";
        apiGet(apiUrl, (responseData, ex) -> {
            if (ex != null) {
            } else {
            }
        });
    }


    //=================================================================================================

    private static void apiGet(String apiUrl, ResponseRunnable r) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).method("GET", null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                r.run(data, null);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                //ShowErr(e.getMessage());
                r.run(null, e);
            }
        });
    }

    static List<MovieInfoPlaybill.MovieInfo> decodeMovieList(String ss) throws Exception {
        List<MovieInfoPlaybill.MovieInfo> returnData = new ArrayList<MovieInfoPlaybill.MovieInfo>();
        JSONArray array = new JSONArray(ss);
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            MovieInfoPlaybill.MovieInfo itemDat = new MovieInfoPlaybill.MovieInfo(
                    item.getString("id"),
                    item.getString("coverId"),
                    item.getString("videoId"),
                    item.getString("title")
            );
            returnData.add(itemDat);
        }
        return returnData;
    }

//    public class ResponseData {
//        public Exception Exception;
//        public String RespnseData;
//    }

    public interface ResponseRunnable {
        public abstract void run(String responseData, Exception ex);
    }

    public interface GotDataRunnable<T> {
        public abstract void gotData(T data, Exception ex);
    }

    public static class MovieDetail {
        public String MovId;
    }
}
