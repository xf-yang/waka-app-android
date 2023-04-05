package com.taikoo.watchwhat.RpApi;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MovieInfoPlaybill {
    private static final String TAG = "MovieInfoPlaybill";

    protected static final List<MovieInfo> ITEMS = new ArrayList<>();
    protected static final Map<String, MovieInfo> ITEM_MAP = new HashMap<>();
    static Lock lock = new ReentrantLock();
    static final int PreLoadNum = 10;
    static int Position = 0;// 当前位置

    static Runnable run_StartRp = null;
    static Runnable run_StartRp2 = null;

    public static void Start() {
        if (run_StartRp == null) {
            run_StartRp = () -> {
                if (Position + PreLoadNum > ITEMS.size()) {
                    new Handler().post(run_LoadPlayBill);
                }
                new Handler().postDelayed(run_StartRp2, 1000);
            };
            run_StartRp2 = () -> new Handler().postDelayed(run_StartRp, 50);
            new Handler().postDelayed(run_StartRp, 50);
        }
    }

    public static int GetSize() {
        int size;
        lock.lock();
        size = ITEMS.size();
        lock.unlock();
        return size;
    }

    public static MovieInfo GetItem(int index) {
        Position = index;
        Log.d(TAG, "GetItem: " + index + "/" + ITEMS.size());
        lock.lock();
        MovieInfo item = ITEMS.get(index);
        CutHead(index);
        lock.unlock();
        return item;
    }

    public static void AddItem(MovieInfo item) {
        addItem(item);
    }

    private static void addItem(MovieInfo item) {
        lock.lock();
//        Log.d(TAG, "addItem: ");
        if (ITEM_MAP.containsKey(item.Id)) return;
        ITEMS.add(item);
        ITEM_MAP.put(item.Id, item);
        Log.d(TAG, "addItem: 1 " + item.Id);
        lock.unlock();
    }

    public static void RemoveItem(MovieInfo item) {
        lock.lock();
        if (ITEM_MAP.containsKey(item.Id)) {
            boolean suc = ITEMS.remove(item);
            if (suc) {
                ITEM_MAP.remove(item.Id);
            }
        }
        lock.unlock();
    }


    public static void RemovePlayed() {
        lock.lock();
        List<MovieInfo> removeList = new ArrayList<>();
        for (int i = 0; i < ITEMS.size(); i++) {
            if (ITEMS.get(i).Played) {
                removeList.add(ITEMS.get(i));
            }
        }
        for (MovieInfo mi : removeList) {
            ITEMS.remove(mi);
        }
        lock.unlock();
    }

    static Runnable run_LoadPlayBill = () -> {
        Log.d(TAG, "run_LoadPlayBill: ");
        rpApi.index(getR());
    };

    static rpApi.GotDataRunnable<List<MovieInfoPlaybill.MovieInfo>> getR() {
        return (data, ex) -> {
            if (ex == null) {
                for (int i = 0; i < data.size(); i++) {
                    MovieInfoPlaybill.AddItem(data.get(i));
                }
            } else {
                Log.e(TAG, "gotData: ", ex);
            }
        };
    }

    static void CutHead(int index) {
        if (ITEMS.size() > 20 && index >= 20) {
            ITEM_MAP.remove(ITEMS.get(index - 20).Id);
        }
    }


    public static class MovieInfo {
        public final String Id;
        public final String CoverId;
        public final String VideoId;
        public final String Title;
        public boolean Played;

        public MovieInfo(String id, String coverId, String videoId, String title) {
            this.Id = id;
            this.CoverId = coverId;
            this.VideoId = videoId;
            this.Title = title;
            this.Played = false;
        }
    }
}